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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.perbone.udao.spi.Cache;
import io.perbone.udao.util.EntityUtils;

/**
 * Concrete {@link Cache} implementation for cacheable bean types.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
final class CacheImpl implements Cache
{
    private final Class<?> type;
    private final io.perbone.mkey.Cache cache; // The backing cache store

    public CacheImpl(final Class<?> type, final io.perbone.mkey.Cache cache) throws IllegalArgumentException
    {
        if (!EntityUtils.isStorable(type))
            throw new IllegalArgumentException("Not a storable bean type");
        if (cache == null)
            throw new IllegalArgumentException("Cache must not be null");

        this.type = type;
        this.cache = cache;
    }

    @Override
    public void add(final Object bean) throws IllegalArgumentException
    {
        if (bean == null)
            throw new IllegalArgumentException("Cannot cache a null bean");

        final String skey = EntityUtils.surrogateKeyHash(bean);
        final String pkey = EntityUtils.primaryKeyHash(bean);
        final List<String> akeys = EntityUtils.alternateKeyHashes(bean);

        final List<String> keys = new ArrayList<String>();

        if (skey != null)
            keys.add(skey);
        if (pkey != null)
            keys.add(pkey);
        for (String akey : akeys)
            keys.add(akey);

        cache.put(EntityUtils.clone(bean), keys.toArray());
    }

    @Override
    public void add(final Object bean, final long ttl, final TimeUnit unit) throws IllegalArgumentException
    {
        throw new UnsupportedOperationException("Operation not supported by this implementation");
    }

    @Override
    public void set(final Object bean) throws IllegalArgumentException
    {
        throw new UnsupportedOperationException("Operation not supported by this implementation");
    }

    @Override
    public void set(final Object bean, final long ttl, final TimeUnit unit) throws IllegalArgumentException
    {
        throw new UnsupportedOperationException("Operation not supported by this implementation");
    }

    @Override
    public <T> T getI(final Object id)
    {
        if (id == null)
            throw new IllegalArgumentException("ID must not be null");

        final String skey = EntityUtils.surrogateKeyHash(type, id);

        return get(skey);
    }

    @Override
    public <T> T getP(final Object... keys)
    {
        if (keys.length == 0)
            throw new IllegalArgumentException("Primary key must not be empty neither null");

        final String pkey = EntityUtils.primaryKeyHash(type, keys);

        return get(pkey);
    }

    @Override
    public <T> T getA(final String name, final Object... keys)
    {
        if (name == null)
            throw new IllegalArgumentException("Alternate name must not be null");
        if (keys.length == 0)
            throw new IllegalArgumentException("Alternate keys must not be empty neither null");

        final String akey = EntityUtils.alternateKeyHash(type, name, keys);

        return get(akey);
    }

    @Override
    public <T> List<T> getAll()
    {
        throw new UnsupportedOperationException("Operation not supported by this implementation");
    }

    @Override
    public boolean contains(final Object bean)
    {
        final String skey;
        final String pkey;
        final List<String> akeys;

        if ((skey = EntityUtils.surrogateKeyHash(bean)) != null)
        {
            return cache.contains(skey);
        }
        else if ((pkey = EntityUtils.primaryKeyHash(bean)) != null)
        {
            return cache.contains(pkey);
        }
        else if (!(akeys = EntityUtils.alternateKeyHashes(bean)).isEmpty())
        {
            for (final String akey : akeys)
            {
                if (cache.contains(akey))
                    return true;
            }

            return false;
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean containsI(final Object id)
    {
        final String skey = EntityUtils.surrogateKeyHash(type, id);

        return skey == null ? false : cache.contains(skey);
    }

    @Override
    public boolean containsP(final Object... keys)
    {
        final String pkey = EntityUtils.primaryKeyHash(type, keys);

        return pkey == null ? false : cache.contains(pkey);
    }

    @Override
    public boolean containsA(final String name, final Object... keys)
    {
        final String akey = EntityUtils.alternateKeyHash(type, name, keys);

        return akey == null ? false : cache.contains(akey);
    }

    @Override
    public void delete(final Object bean)
    {
        if (bean == null)
            throw new IllegalArgumentException("Bean must not be null");

        final String skey = EntityUtils.surrogateKeyHash(bean);
        final String pkey = EntityUtils.primaryKeyHash(bean);
        final List<String> akeys = EntityUtils.alternateKeyHashes(bean);

        if (skey != null)
            cache.remove(skey);
        if (pkey != null)
            cache.remove(pkey);
        for (final String akey : akeys)
            cache.remove(akey);
    }

    @Override
    public void deleteI(Object id)
    {
        if (id == null)
            throw new IllegalArgumentException("ID must not be null");

        final String skey = EntityUtils.surrogateKeyHash(type, id);

        cache.remove(skey);
    }

    @Override
    public void deleteP(final Object... keys)
    {
        if (keys.length == 0)
            throw new IllegalArgumentException("Primary key must not be empty neither null");

        final String pkey = EntityUtils.primaryKeyHash(type, keys);

        cache.remove(pkey);
    }

    @Override
    public void deleteA(final String name, Object... keys)
    {
        if (name == null)
            throw new IllegalArgumentException("Alternate name must not be null");
        if (keys.length == 0)
            throw new IllegalArgumentException("Alternate keys must not be empty neither null");

        final String akey = EntityUtils.alternateKeyHash(type, name, keys);

        cache.remove(akey);
    }

    @Override
    public <T> T removeI(final Object id)
    {
        if (id == null)
            throw new IllegalArgumentException("ID must not be null");

        final String skey = EntityUtils.surrogateKeyHash(type, id);

        return cache.remove(skey);
    }

    @Override
    public <T> T removeP(final Object... keys)
    {
        if (keys.length == 0)
            throw new IllegalArgumentException("Primary key must not be empty neither null");

        final String pkey = EntityUtils.primaryKeyHash(type, keys);

        return cache.remove(pkey);
    }

    @Override
    public <T> T removeA(final String name, final Object... keys)
    {
        if (name == null)
            throw new IllegalArgumentException("Alternate name must not be null");
        if (keys.length == 0)
            throw new IllegalArgumentException("Alternate keys must not be empty neither null");

        final String akey = EntityUtils.alternateKeyHash(type, name, keys);

        return cache.remove(akey);
    }

    @Override
    public void invalidate()
    {
        cache.clear();
    }

    @Override
    public void invalidate(final Object criteria)
    {
        throw new UnsupportedOperationException("Operation not supported by this implementation");
    }

    @Override
    public void prune(final Object criteria)
    {
        throw new UnsupportedOperationException("Operation not supported by this implementation");
    }

    @Override
    public void evict()
    {
        cache.evict();
    }

    @Override
    public long count()
    {
        return cache.size();
    }

    /**
     * Retrieves the bean from the cache with the given key.
     * 
     * @param key
     *            the bean key
     * 
     * @return a clone of the bean from the cache; null if the cache does not contains it
     */
    @SuppressWarnings("unchecked")
    private <T> T get(final String key)
    {
        final Object bean = cache.get(key);

        return (T) (bean == null ? null : EntityUtils.clone(bean));
    }
}