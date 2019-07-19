package com.chengfu.fuhttp.convert;

import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.ResponseBody;

public interface Converter<T> {
    @Nullable
    T convert(ResponseBody body) throws IOException;
}
