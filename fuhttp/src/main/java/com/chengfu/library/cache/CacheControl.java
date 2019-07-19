package com.chengfu.library.cache;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * 缓存控制器
 */
public final class CacheControl {
    private static final String CACHE_NAME = "fu_http_cache";

    private final ACache cache;
    private final String key;
    private final boolean store;//存储（在Converter返回的数据不为空时才会存储）
    private final @CacheType
    int cacheType;//缓存策略
    private final int maxAgeSeconds;//缓存过期时间

    private CacheControl(@NotNull ACache cache, String key, boolean store, @CacheType int cacheType, int maxAgeSeconds) {
        this.cache = cache;
        this.key = key;
        this.store = store;
        this.cacheType = cacheType;
        this.maxAgeSeconds = maxAgeSeconds;
    }

    private CacheControl(Builder builder) {
        this.cache = ACache.get(builder.context, CACHE_NAME);
        this.key = builder.key;
        this.store = builder.store;
        this.cacheType = builder.cacheType;
        this.maxAgeSeconds = builder.maxAgeSeconds;

    }

    public @NotNull
    ACache cache() {
        return cache;
    }

    public String key() {
        return key;
    }

    public boolean store() {
        return store;
    }

    public @CacheType
    int cacheType() {
        return cacheType;
    }

    public int maxAgeSeconds() {
        return maxAgeSeconds;
    }

    public static final class Builder {
        Context context;
        String key;
        boolean store;
        @CacheType
        int cacheType;
        int maxAgeSeconds;

        public Builder(@NotNull Context context) {
            this.context = context.getApplicationContext();
        }

        public Builder store(boolean store) {
            this.store = store;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder cacheType(@CacheType int cacheType) {
            this.cacheType = cacheType;
            return this;
        }

        public Builder maxAge(int maxAge, TimeUnit timeUnit) {
            if (maxAge < 0) throw new IllegalArgumentException("maxAge < 0: " + maxAge);
            long maxAgeSecondsLong = timeUnit.toSeconds(maxAge);
            this.maxAgeSeconds = maxAgeSecondsLong > Integer.MAX_VALUE
                    ? Integer.MAX_VALUE
                    : (int) maxAgeSecondsLong;
            return this;
        }

        public CacheControl build() {
            return new CacheControl(this);
        }
    }
}
