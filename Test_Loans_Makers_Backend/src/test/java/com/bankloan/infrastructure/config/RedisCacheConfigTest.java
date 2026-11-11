package com.bankloan.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.Cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedisCacheConfigTest {

    private final RedisCacheConfig config = new RedisCacheConfig();

    @Test
    void errorHandler_shouldNotBeNull() {
        CacheErrorHandler handler = config.errorHandler();
        assertThat(handler).isNotNull();
    }

    @Test
    void errorHandler_handleCacheGetError_shouldNotThrow() {
        CacheErrorHandler handler = config.errorHandler();
        Cache cache = mock(Cache.class);
        when(cache.getName()).thenReturn("test-cache");

        assertThatCode(() -> handler.handleCacheGetError(
                new RuntimeException("get error"), cache, "key1"
        )).doesNotThrowAnyException();
    }

    @Test
    void errorHandler_handleCachePutError_shouldNotThrow() {
        CacheErrorHandler handler = config.errorHandler();
        Cache cache = mock(Cache.class);
        when(cache.getName()).thenReturn("test-cache");

        assertThatCode(() -> handler.handleCachePutError(
                new RuntimeException("put error"), cache, "key1", "value1"
        )).doesNotThrowAnyException();
    }

    @Test
    void errorHandler_handleCacheEvictError_shouldNotThrow() {
        CacheErrorHandler handler = config.errorHandler();
        Cache cache = mock(Cache.class);
        when(cache.getName()).thenReturn("test-cache");

        assertThatCode(() -> handler.handleCacheEvictError(
                new RuntimeException("evict error"), cache, "key1"
        )).doesNotThrowAnyException();
    }

    @Test
    void errorHandler_handleCacheClearError_shouldNotThrow() {
        CacheErrorHandler handler = config.errorHandler();
        Cache cache = mock(Cache.class);
        when(cache.getName()).thenReturn("test-cache");

        assertThatCode(() -> handler.handleCacheClearError(
                new RuntimeException("clear error"), cache
        )).doesNotThrowAnyException();
    }
}
