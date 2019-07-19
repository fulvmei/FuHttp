package com.chengfu.fuhttp;

import android.support.annotation.Nullable;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;

public final class NoContentResponseBody extends ResponseBody {
    private final @Nullable
    MediaType contentType;
    private final long contentLength;

    NoContentResponseBody(@Nullable MediaType contentType, long contentLength) {
        this.contentType = contentType;
        this.contentLength = contentLength;
    }

    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public long contentLength() {
        return contentLength;
    }

    @Override
    public BufferedSource source() {
        throw new IllegalStateException("Cannot read raw response body of a converted body.");
    }
}
