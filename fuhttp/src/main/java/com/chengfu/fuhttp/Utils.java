package com.chengfu.fuhttp;

import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.ResponseBody;
import okio.Buffer;

public class Utils {

    public static <T> T checkNotNull(@Nullable T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }

    public static ResponseBody buffer(final ResponseBody body) throws IOException {
        Buffer buffer = new Buffer();
        body.source().readAll(buffer);
        return ResponseBody.create(body.contentType(), body.contentLength(), buffer);
    }
}
