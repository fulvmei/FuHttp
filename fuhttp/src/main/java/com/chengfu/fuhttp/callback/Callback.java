package com.chengfu.fuhttp.callback;

import com.chengfu.fuhttp.Call;
import com.chengfu.fuhttp.ResponseException;
import com.chengfu.fuhttp.convert.Converter;
import com.chengfu.fuhttp.Response;

public interface Callback<T> extends Converter<T> {

    void onResponse(Call<T> call, Response<T> response);

    void onFailure(Call<T> call, ResponseException e);
}
