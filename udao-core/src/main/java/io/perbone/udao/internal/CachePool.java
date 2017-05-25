/*
 * This file is part of UDAO 
 * https://github.com/perbone/udao/
 * 
 * Copyright 2013-2017 Paulo Perbone
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package io.perbone.udao.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import io.perbone.mkey.CacheBuilder;
import io.perbone.mkey.GarbagePolicy;
import io.perbone.udao.configuration.CacheEntry;
import io.perbone.udao.spi.Cache;

/**
 * Cache pool is factory of {@link Cache} concrete implementations.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
final class CachePool
{
    private final AtomicBoolean open = new AtomicBoolean(true);

    private final ConcurrentHashMap<String, io.perbone.mkey.Cache> caches = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Class<?>, String> types = new ConcurrentHashMap<>();

    public CachePool()
    {
    }

    /**
     * Allocates a new {@link io.perbone.mkey.Cache} instance into the pool.
     * 
     * @param centry
     *            the cache definition
     * 
     * @throws IllegalStateException
     *             if this cache pool is closed
     */
    public void allocate(final CacheEntry centry) throws IllegalStateException
    {
        checkOpen();

        if (caches.containsKey(centry.id()))
            return;

        io.perbone.mkey.Cache cache = CacheBuilder
                .newInstance()
                .hardLimitSize(centry.hardLimitSize())
                .garbagePolicy(GarbagePolicy.TIME_TO_LIVE)
                .timeToLive(centry.ttl(), centry.unit())
                .evictionPolicy(centry.evictionPolicy())
                .build();

        caches.put(centry.id(), cache);
    }

    /**
     * Attaches the given bean type to the given cache id.
     * <p>
     * The cache id must be mapped to a previously allocated cache.
     * 
     * @param type
     *            the bean type
     * @param cacheId
     *            the cache id
     * @throws IllegalStateException
     *             if this cache pool is closed or if there is no cache mapped to the given cache id
     */
    public <T> void attach(final Class<T> type, final String cacheId) throws IllegalStateException
    {
        checkOpen();

        if (!caches.containsKey(cacheId))
            throw new IllegalStateException("Cache id matches no cache instance");

        types.put(type, cacheId);
    }

    /**
     * Retrieves a {@link Cache} instance compatible with the given bean type.
     * 
     * @param type
     *            the bean type
     * 
     * @return the cache instance
     * 
     * @throws IllegalStateException
     *             if this cache pool is closed
     */
    public Cache get(final Class<?> type) throws IllegalStateException
    {
        checkOpen();

        String cacheId = types.get(type);

        if (cacheId == null)
            return new NoCacheImpl();
        else
            return new CacheImpl(type, caches.get(cacheId));
    }

    /**
     * Retrieves a {@link Cache} instance compatible with the given bean type.
     * 
     * @param type
     *            the bean type
     * 
     * @return the cache instance
     */
    public <T> Cache get(final T bean)
    {
        checkOpen();

        return get(bean.getClass());
    }

    // /**
    // * Invalidates all caches.
    // *
    // * @deprecated
    // */
    // public void invalidateAll()
    // {
    // checkOpen();
    //
    // for (io.perbone.mkey.Cache cache : caches.values())
    // cache.clear();
    // }

    public void close() throws IllegalStateException
    {
        if (open.compareAndSet(true, false))
        {
            for (io.perbone.mkey.Cache cache : caches.values())
                cache.clear();

            types.clear();
            caches.clear();
        }
    }

    /**
     * Tells whether or not this cache pool is open.
     * <p>
     * It is assumed that after a successful object instantiation this method will return
     * <tt>true</tt>. Conversely for fail object instantiation this method should return
     * <tt>false</tt> despite the fact that this object may still be valid.
     * <p>
     * 
     * @return <tt>true</tt> if it is active; <tt>false</tt> otherwise
     * 
     * @see #close
     */
    public boolean isOpen()
    {
        return open.get();
    }

    /**
     * Checks if this cache pool is currently open.
     * 
     * @throws IllegalStateException
     *             if this manager is closed
     */
    private void checkOpen() throws IllegalStateException
    {
        if (!open.get())
            throw new IllegalStateException("Illegal invocation; cache pool is closed");
    }
}