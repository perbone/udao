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

import io.perbone.udao.Cursor;
import io.perbone.udao.DataConstraintViolationException;
import io.perbone.udao.KeyViolationException;
import io.perbone.udao.NotEnoughResourceException;
import io.perbone.udao.NotFoundException;
import io.perbone.udao.OperationTimeoutException;
import io.perbone.udao.query.NativeQuery;
import io.perbone.udao.query.Query;
import io.perbone.udao.transaction.Transaction;
import io.perbone.udao.transaction.TransactionException;

/**
 * A {@code DataSource} is a resource manager that operates upon objects annotated as
 * {@code Storable}.
 * <p>
 * It represents an open connection to an underling storage such as a database, a cache, a web
 * service, or a program component that is capable of performing one or more distinct storage
 * operations, for example resource persistence or resource fetching.
 * <p>
 * A data source is either open or closed. A data source is open upon creation, and once closed it
 * remains closed. Once a data source is closed, any attempt to invoke a storage operation upon it
 * will cause a {@link IllegalStateException} to be thrown. Whether or not a data source is open may
 * be tested by invoking its {@link #isOpen} method.
 * <p>
 * {@code DataSource}s are, in general, intended to be thread safe but it is really up to the
 * implementation to support this feature or not. In any case, it should be documented so the
 * developers using the concrete classes do not make mistakes.
 * <p>
 * FIXME maybe this interface should be broken in more than one specialized area of data sources.
 * SQL/NoSQL, read only methods vs mutator methods etc.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public interface DataSource
{
    boolean accepts(Class<?> type) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    <T> T create(Transaction txn, Cache cache, T bean) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    <T> T create(Transaction txn, Cache cache, T bean, long ttl, TimeUnit unit)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> List<T> create(Transaction txn, Cache cache, List<T> beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> List<T> create(Transaction txn, Cache cache, List<T> beans, long ttl, TimeUnit unit)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> T save(Transaction txn, Cache cache, T bean) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    <T> T save(Transaction txn, Cache cache, T bean, long ttl, TimeUnit unit)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> List<T> save(Transaction txn, Cache cache, List<T> beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> List<T> save(Transaction txn, Cache cache, List<T> beans, long ttl, TimeUnit unit)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> T fetchI(Transaction txn, Cache cache, Class<T> type, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    <T> List<T> fetchI(Transaction txn, Cache cache, Class<T> type, Object... ids)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    <T> T fetchP(Transaction txn, Cache cache, Class<T> type, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    <T> T fetchA(Transaction txn, Cache cache, Class<T> type, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    boolean containsI(Transaction txn, Cache cache, Class<?> type, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    boolean containsP(Transaction txn, Cache cache, Class<?> type, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    boolean containsA(Transaction txn, Cache cache, Class<?> type, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    <T> T updateI(Transaction txn, Cache cache, T bean, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> T updateI(Transaction txn, Cache cache, T bean, long ttl, TimeUnit unit, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> T updateP(Transaction txn, Cache cache, T bean, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> T updateP(Transaction txn, Cache cache, T bean, long ttl, TimeUnit unit, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> T updateA(Transaction txn, Cache cache, T bean, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> T updateA(Transaction txn, Cache cache, T bean, long ttl, TimeUnit unit, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> T patchI(Transaction txn, Cache cache, T bean, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> T patchI(Transaction txn, Cache cache, T bean, long ttl, TimeUnit unit, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> T patchP(Transaction txn, Cache cache, T bean, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> T patchP(Transaction txn, Cache cache, T bean, long ttl, TimeUnit unit, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> T patchA(Transaction txn, Cache cache, T bean, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> T patchA(Transaction txn, Cache cache, T bean, long ttl, TimeUnit unit, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    void touchI(Transaction txn, Cache cache, Class<?> type, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException,
            DataProviderException;

    void touchI(Transaction txn, Cache cache, Class<?> type, long ttl, TimeUnit unit, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException,
            DataProviderException;

    void touchP(Transaction txn, Cache cache, Class<?> type, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException,
            DataProviderException;

    void touchP(Transaction txn, Cache cache, Class<?> type, long ttl, TimeUnit unit, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException,
            DataProviderException;

    void touchA(Transaction txn, Cache cache, Class<?> type, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException,
            DataProviderException;

    void touchA(Transaction txn, Cache cache, Class<?> type, long ttl, TimeUnit unit, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException,
            DataProviderException;

    void deleteI(Transaction txn, Cache cache, Class<?> type, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    void deleteP(Transaction txn, Cache cache, Class<?> type, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    void deleteA(Transaction txn, Cache cache, Class<?> type, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    void deleteX(Transaction txn, Cache cache, Class<?> type, Object... beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> T removeI(Transaction txn, Cache cache, Class<T> type, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> T removeP(Transaction txn, Cache cache, Class<T> type, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    <T> T removeA(Transaction txn, Cache cache, Class<T> type, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    /** Cursor operations support */

    <T> Cursor<T> cursorI(Transaction txn, Cache cache, Class<T> type)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    <T> Cursor<T> cursorP(Transaction txn, Cache cache, Class<T> type)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    <T> Cursor<T> cursorA(Transaction txn, Cache cache, Class<T> type, String name)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    <T> Cursor<T> cursorX(Transaction txn, Cache cache, Class<T> type, Object... beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    <T> Cursor<T> cursorQ(Transaction txn, Cache cache, Class<T> type, Query query)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    <T> Cursor<T> cursorN(Transaction txn, Cache cache, Class<T> type, String nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    <T> Cursor<T> cursorN(Transaction txn, Cache cache, Class<T> type, NativeQuery<T> nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    /** Count operations support */

    long count(Transaction txn, Cache cache, Class<?> type)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    long countX(Transaction txn, Cache cache, Class<?> type, Object... beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    long countQ(Transaction txn, Cache cache, Class<?> type, Query query)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    long countN(Transaction txn, Cache cache, Class<?> type, String nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    long countN(Transaction txn, Cache cache, Class<?> type, NativeQuery<?> nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    /** Cache support operations */

    /**
     * Removes all expired beans.
     * 
     * @param type
     * @throws UnsupportedOperationException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @throws DataProviderException
     */
    void expires(Cache cache, Class<?> type) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    /**
     * Removes all expired beans matched by the criteria filter.
     * 
     * @param type
     * @param criteria
     * @throws UnsupportedOperationException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @throws DataProviderException
     */
    void expires(Cache cache, Class<?> type, Object criteria)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    /**
     * Invalidates all cached beans for the given type.
     * 
     * @param type
     * @throws UnsupportedOperationException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @throws DataProviderException
     */
    void invalidate(Cache cache, Class<?> type) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    /**
     * Remove all beans matched by the criteria.
     * 
     * @param type
     * @param criteria
     * @return
     * @throws UnsupportedOperationException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @throws DataProviderException
     */
    long prune(Cache cache, Class<?> type, Object criteria) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    /**
     * Removes all beans.
     * 
     * @param type
     * @throws UnsupportedOperationException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @throws KeyViolationException
     * @throws DataConstraintViolationException
     * @throws DataProviderException
     */
    void clear(Cache cache, Class<?> type) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    /**
     * Evicts beans when the cache is full.
     * <p>
     * This operation should be executed automatic by the {@code DataSource} implementation.
     * 
     * @throws UnsupportedOperationException
     * @throws IllegalStateException
     * @throws OperationTimeoutException
     * @throws DataProviderException
     */
    void evict() throws UnsupportedOperationException, IllegalStateException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    /**
     * Opens this data source.
     * 
     * @throws IllegalStateException
     *             When this data source is already open
     * @throws DataProviderException
     *             When cannot open this data source
     * 
     * @see #isOpen
     * @see #close
     */
    void open()
            throws IllegalStateException, OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    /**
     * Closes this data source.
     * <p>
     * Invocation has no additional effect if already closed and once closed it remains closed.
     * 
     * @throws StorageException
     *             when cannot closes the this data source
     * 
     * @see #isOpen
     */
    void close() throws DataProviderException;

    /**
     * Tells whether or not this data source is open.
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
    boolean isOpen();
}