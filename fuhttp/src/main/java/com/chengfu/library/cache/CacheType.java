package com.chengfu.library.cache;

import android.support.annotation.IntDef;

/**
 * 缓存策略
 */
@IntDef({CacheType.NO_CACHE, CacheType.REQUEST_FAILED_READ_CACHE, CacheType.IF_NONE_CACHE_REQUEST, CacheType.FIRST_CACHE_THEN_REQUEST})
public @interface CacheType {
    /**
     * 不使用缓存
     */
    int NO_CACHE = 0;

    /**
     * 请求网络失败后，读取缓存
     */
    int REQUEST_FAILED_READ_CACHE = 1;

    /**
     * 如果缓存不存在才请求网络，否则使用缓存
     */
    int IF_NONE_CACHE_REQUEST = 2;

    /**
     * 先使用缓存，不管是否存在，仍然请求网络
     */
    int FIRST_CACHE_THEN_REQUEST = 3;
}
