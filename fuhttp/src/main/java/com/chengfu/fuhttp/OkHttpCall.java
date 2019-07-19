package com.chengfu.fuhttp;

import android.support.annotation.GuardedBy;
import android.support.annotation.Nullable;

import com.chengfu.fuhttp.callback.Callback;
import com.chengfu.fuhttp.convert.Converter;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

import static com.chengfu.fuhttp.Utils.checkNotNull;

final class OkHttpCall<T> implements Call<T> {

    private final Request request;
    private final okhttp3.Call.Factory callFactory;
    private final Converter<T> responseConverter;

    private volatile boolean canceled;

    @GuardedBy("this")
    private @Nullable
    okhttp3.Call rawCall;
    //    @GuardedBy("this") // Either a RuntimeException, non-fatal Error, or IOException.
//    private @Nullable
//    Throwable creationFailure;
    @GuardedBy("this")
    private boolean executed;

    OkHttpCall(Request request,
               okhttp3.Call.Factory callFactory, Converter<T> responseConverter) {
        this.request = request;
        this.callFactory = callFactory;
        this.responseConverter = responseConverter;
    }

    @Override
    public Response<T> execute() throws IOException {
        okhttp3.Call call;

        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed.");
            executed = true;
            call = rawCall;
            if (call == null) {
                call = rawCall = createRawCall();
            }
        }

        if (canceled) {
            call.cancel();
        }

        return parseResponse(call.execute());
    }

    private okhttp3.Call createRawCall() {
        okhttp3.Call call = callFactory.newCall(request);
        return call;
    }

    private Response<T> parseResponse(okhttp3.Response rawResponse) throws ResponseException {
        ResponseBody rawBody = rawResponse.body();

        // Remove the body's source (the only stateful object) so we can pass the response along.
        rawResponse = rawResponse.newBuilder()
                .body(new NoContentResponseBody(rawBody.contentType(), rawBody.contentLength()))
                .build();

        int code = rawResponse.code();
        if (code < 200 || code >= 300) {
            try {
                // Buffer the entire body to avoid future I/O.
                ResponseBody bufferedBody = Utils.buffer(rawBody);
                return Response.error(bufferedBody, rawResponse);
            } finally {
                rawBody.close();
            }
        }

        if (code == 204 || code == 205) {
            rawBody.close();
            return Response.success(null, rawResponse);
        }

        ExceptionCatchingResponseBody catchingBody = new ExceptionCatchingResponseBody(rawBody);
        try {
            T body = responseConverter.convert(catchingBody);
            return Response.success(body, rawResponse);
        } catch (IOException e) {
            // If the underlying source threw an exception, propagate that rather than indicating it was
            // a runtime exception.
            catchingBody.throwIfCaught();
            throw e;
        }
    }

    @Override
    public void enqueue(final Callback<T> callback) {
        checkNotNull(callback, "callback == null");

        okhttp3.Call call;

        synchronized (this) {
            if (executed) throw new IllegalStateException("Already executed.");
            executed = true;

            call = rawCall;
            if (call == null) {
                call = rawCall = createRawCall();
            }
        }

        if (canceled) {
            call.cancel();
        }

        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response rawResponse) {
                Response<T> response;
                try {
                    response = parseResponse(rawResponse);
                } catch (ResponseException e) {
                    callFailure(e);
                    return;
                }

                try {
                    callback.onResponse(OkHttpCall.this, response);
                } catch (Throwable t) {
                    t.printStackTrace(); // TODO this is not great
                }
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                callFailure(e);
            }

            private void callFailure(Throwable e) {
                try {
                    callback.onFailure(OkHttpCall.this, e);
                } catch (Throwable t) {
                    t.printStackTrace(); // TODO this is not great
                }
            }
        });
    }

    @Override
    public boolean isExecuted() {
        return executed;
    }

    @Override
    public void cancel() {
        canceled = true;

        okhttp3.Call call;
        synchronized (this) {
            call = rawCall;
        }
        if (call != null) {
            call.cancel();
        }
    }

    @Override
    public boolean isCanceled() {
        if (canceled) {
            return true;
        }
        synchronized (this) {
            return rawCall != null && rawCall.isCanceled();
        }
    }

    @Override
    public Call<T> clone() {
        return new OkHttpCall<>(request, callFactory, responseConverter);
    }

    @Override
    public Request request() {
        return null;
    }


    static final class ExceptionCatchingResponseBody extends ResponseBody {
        private final ResponseBody delegate;
        private final BufferedSource delegateSource;
        @Nullable
        IOException thrownException;

        ExceptionCatchingResponseBody(ResponseBody delegate) {
            this.delegate = delegate;
            this.delegateSource = Okio.buffer(new ForwardingSource(delegate.source()) {
                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    try {
                        return super.read(sink, byteCount);
                    } catch (IOException e) {
                        thrownException = e;
                        throw e;
                    }
                }
            });
        }

        @Override
        public MediaType contentType() {
            return delegate.contentType();
        }

        @Override
        public long contentLength() {
            return delegate.contentLength();
        }

        @Override
        public BufferedSource source() {
            return delegateSource;
        }

        @Override
        public void close() {
            delegate.close();
        }

        void throwIfCaught() throws IOException {
            if (thrownException != null) {
                throw thrownException;
            }
        }
    }
}
