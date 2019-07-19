package com.chengfu.fuhttp;


import com.chengfu.fuhttp.callback.Callback;

import java.io.IOException;

import okhttp3.Request;

/**
 * @param <T> Successful response body type.
 */
public interface Call<T> extends Cloneable {
    Response<T> execute() throws IOException;

    void enqueue(Callback<T> callback);

    boolean isExecuted();

    void cancel();

    boolean isCanceled();

    Call<T> clone();

    Request request();
}
