package com.chengfu.library;

import android.text.TextUtils;

import com.chengfu.library.cache.CacheControl;
import com.chengfu.library.cache.CacheType;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FuHttpClient {

    private volatile static FuHttpClient mSingleton = null;

    private OkHttpClient mOkHttpClient;

    private FuHttpClient() {
        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new LoggingInterceptor())
                .build();
    }

    public static FuHttpClient getInstance() {
        if (mSingleton == null) {
            synchronized (FuHttpClient.class) {
                if (mSingleton == null) {
                    mSingleton = new FuHttpClient();
                }
            }
        }
        return mSingleton;
    }

    /**
     * 同步请求
     *
     * @param request   请求
     * @param converter 转换器
     * @param <T>       数据类型
     * @return 数据
     */
    public <T> T requestAsync(@NotNull Request request, @NotNull Converter<T> converter) {
        return requestAsync(request, null, converter);
    }

    /**
     * 同步请求（同步请求因为没有回调，所以FIRST_CACHE_THEN_REQUEST与NO_CACHE缓存策略效果是一样的
     * 如果需要实现FIRST_CACHE_THEN_REQUEST缓存策略，可以用先调用getFromeCache判断是否为空后再调用requestAsync）
     *
     * @param request      请求
     * @param cacheControl 缓存控制器
     * @param converter    转换器
     * @param <T>          数据类型
     * @return 数据
     */
    public <T> T requestAsync(@NotNull Request request, CacheControl cacheControl, @NotNull Converter<T> converter) {
        if (cacheControl.cacheType() == CacheType.IF_NONE_CACHE_REQUEST) {
            String cache = cacheControl.cache().getAsString(cacheControl.key());
            T t = converter.convert(cache);
            if (converter.isSuccessful()) {
                return t;
            }
        }
        Call call = mOkHttpClient.newCall(request);
        try {
            Response response = call.execute();
            if (response.code() != 200) {
                if (cacheControl.cacheType() == CacheType.REQUEST_FAILED_READ_CACHE) {
                    return getFromCache(cacheControl, converter);
                }
                return null;
            }
            String string = response.body().string();
            T t = converter.convert(string);
            if (t != null && cacheControl.store()) {
                cacheControl.cache().put(cacheControl.key(), string);
            }
            if (t == null && cacheControl.cacheType() == CacheType.REQUEST_FAILED_READ_CACHE) {
                return getFromCache(cacheControl, converter);
            }
            return t;
        } catch (IOException e) {
            e.printStackTrace();
            if (cacheControl.cacheType() == CacheType.REQUEST_FAILED_READ_CACHE) {
                return getFromCache(cacheControl, converter);
            }
            return null;
        }
    }

    public <T> T getFromCache(CacheControl cacheControl, @NotNull Converter<T> converter) {
        String cache = cacheControl.cache().getAsString(cacheControl.key());
        if (!TextUtils.isEmpty(cache)) {
            return converter.convert(cache);
        }
        return null;
    }

    /**
     * 异步请求
     *
     * @param request   请求
     * @param converter 转换器
     * @param callback  回调
     * @param <T>       数据类型
     */
    public <T> void request(Request request, CacheControl cacheControl, @NotNull final Converter<T> converter, @NotNull final Callback<T> callback) {
        if (cacheControl.cacheType() == CacheType.IF_NONE_CACHE_REQUEST) {
            String cache = cacheControl.cache().getAsString(cacheControl.key());
            T t = converter.convert(cache);
            if (t != null) {

            }
        }
        Call call = mOkHttpClient.newCall(request);
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
//                callback.onSuccess(call, converter.convert(response));
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onError(call, e);
            }
        });
    }

    /**
     * 根据Tag取消请求
     *
     * @param tag 需要取消的tag
     */
    public void cancelTag(Object tag) {
        if (tag == null) {
            return;
        }
        for (Call call : mOkHttpClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : mOkHttpClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    /**
     * 取消所有请求请求
     */
    public void cancelAll() {
        for (Call call : mOkHttpClient.dispatcher().queuedCalls()) {
            call.cancel();
        }
        for (Call call : mOkHttpClient.dispatcher().runningCalls()) {
            call.cancel();
        }
    }
}
