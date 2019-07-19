package com.chengfu.library;

import android.support.annotation.NonNull;

import com.chengfu.fuhttp.Response;

import okhttp3.Call;

public interface Callback<T> {

    /**
     * 网络请求成功回调
     *
     * @param response
     */
    void onSuccess(Response<T> response);

    void onFailure();

    /**
     * 对返回数据进行操作的回调
     */
    void onSuccess(Call call, @NonNull T result);

    /**
     * 缓存成功的回调
     */
    void onCacheSuccess(Call call, T result);

    /**
     * 请求失败，响应错误，数据解析错误等，都会回调该方法
     */
    void onError(Call call, Exception e);
}
