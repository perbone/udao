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

package io.perbone.udao.spi;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Cache interface.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public interface Cache
{
    void add(Object bean) throws IllegalArgumentException;

    void add(Object bean, long ttl, TimeUnit unit) throws IllegalArgumentException;

    void set(Object bean) throws IllegalArgumentException;

    void set(Object bean, long ttl, TimeUnit unit) throws IllegalArgumentException;

    <T> T getI(Object id);

    <T> T getP(Object... keys);

    <T> T getA(String name, Object... keys);

    <T> List<T> getAll();

    boolean contains(Object bean);

    boolean containsI(Object id);

    boolean containsP(Object... keys);

    boolean containsA(String name, Object... keys);

    void delete(Object bean);

    void deleteI(Object id);

    void deleteP(Object... keys);

    void deleteA(String name, Object... keys);

    <T> T removeI(Object id);

    <T> T removeP(Object... keys);

    <T> T removeA(String name, Object... keys);

    long count();

    void invalidate();

    void invalidate(Object criteria);

    void prune(Object criteria);

    void evict();
}