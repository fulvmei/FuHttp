package com.chengfu.library;

import okhttp3.Response;

public interface Converter<T> {

    T convert(String string);

    boolean isSuccessful();

    T convert(String string, Callback<T> callback);
}
