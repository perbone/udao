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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.perbone.udao.spi.Cache;

/**
 * This is a Null Object pattern implementation of the {@link Cache} interface.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
final class NoCacheImpl implements Cache
{
    @Override
    public void add(final Object bean) throws IllegalArgumentException
    {

    }

    @Override
    public void add(final Object bean, final long ttl, final TimeUnit unit) throws IllegalArgumentException
    {

    }

    @Override
    public void set(final Object bean) throws IllegalArgumentException
    {

    }

    @Override
    public void set(final Object bean, long ttl, final TimeUnit unit) throws IllegalArgumentException
    {

    }

    @Override
    public boolean contains(final Object bean)
    {
        return false;
    }

    @Override
    public boolean containsA(final String name, final Object... keys)
    {
        return false;
    }

    @Override
    public boolean containsI(final Object id)
    {
        return false;
    }

    @Override
    public boolean containsP(final Object... keys)
    {
        return false;
    }

    @Override
    public long count()
    {
        return 0;
    }

    @Override
    public void delete(final Object bean)
    {

    }

    @Override
    public void deleteA(final String name, final Object... keys)
    {

    }

    @Override
    public void deleteI(final Object id)
    {

    }

    @Override
    public void deleteP(final Object... keys)
    {

    }

    @Override
    public void evict()
    {

    }

    @Override
    public <T> T getA(final String name, final Object... keys)
    {
        return null;
    }

    @Override
    public <T> List<T> getAll()
    {
        return Collections.emptyList();
    }

    @Override
    public <T> T getI(final Object id)
    {
        return null;
    }

    @Override
    public <T> T getP(final Object... keys)
    {
        return null;
    }

    @Override
    public void invalidate()
    {

    }

    @Override
    public void invalidate(final Object criteria)
    {

    }

    @Override
    public void prune(final Object criteria)
    {

    }

    @Override
    public <T> T removeA(final String name, final Object... keys)
    {
        return null;
    }

    @Override
    public <T> T removeI(final Object id)
    {
        return null;
    }

    @Override
    public <T> T removeP(final Object... keys)
    {
        return null;
    }
}