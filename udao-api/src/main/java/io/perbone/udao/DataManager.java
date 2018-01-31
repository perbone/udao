/*
 * This file is part of UDAO 
 * https://github.com/perbone/udao/
 * 
 * Copyright 2013-2018 Paulo Perbone
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

package io.perbone.udao;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.perbone.toolbox.provider.NotEnoughResourceException;
import io.perbone.toolbox.provider.OperationTimeoutException;
import io.perbone.udao.query.NativeQuery;
import io.perbone.udao.query.Query;
import io.perbone.udao.transaction.Transaction;
import io.perbone.udao.transaction.TransactionException;

/**
 * Data Manager interface.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public interface DataManager extends AutoCloseable
{
    <T> T create(T bean) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException;

    <T> T create(T bean, long ttl, TimeUnit unit) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> List<T> create(List<T> beans) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> List<T> create(List<T> beans, long ttl, TimeUnit unit) throws UnsupportedOperationException,
            IllegalStateException, IllegalArgumentException, TransactionException, KeyViolationException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException;

    /** Save methods will create or update */

    <T> T save(T bean) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException;

    <T> T save(T bean, long ttl, TimeUnit unit) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> List<T> save(List<T> beans) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> List<T> save(List<T> beans, long ttl, TimeUnit unit) throws UnsupportedOperationException,
            IllegalStateException, IllegalArgumentException, TransactionException, KeyViolationException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> T fetchI(Class<T> type, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> List<T> fetchI(Class<T> type, Object... ids)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> T fetchP(Class<T> type, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> T fetchA(Class<T> type, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataException;

    boolean containsI(Class<?> type, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    boolean containsP(Class<?> type, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    boolean containsA(Class<?> type, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> T updateI(T bean, Object id) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, NotFoundException, KeyViolationException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> T updateI(T bean, long ttl, TimeUnit unit, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException;

    <T> T updateP(T bean, Object... keys) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, NotFoundException, KeyViolationException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> T updateP(T bean, long ttl, TimeUnit unit, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException;

    <T> T updateA(T bean, String name, Object... keys) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, NotFoundException, KeyViolationException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> T updateA(T bean, long ttl, TimeUnit unit, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException;

    <T> T patchI(T bean, Object id) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, NotFoundException, KeyViolationException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> T patchI(T bean, long ttl, TimeUnit unit, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException;

    <T> T patchP(T bean, Object... keys) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, NotFoundException, KeyViolationException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> T patchP(T bean, long ttl, TimeUnit unit, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException;

    <T> T patchA(T bean, String name, Object... keys) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, NotFoundException, KeyViolationException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> T patchA(T bean, long ttl, TimeUnit unit, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException;

    void touchI(Class<?> type, Object id) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, NotFoundException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    void touchI(Class<?> type, long ttl, TimeUnit unit, Object id) throws UnsupportedOperationException,
            IllegalStateException, IllegalArgumentException, TransactionException, NotFoundException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException;

    void touchP(Class<?> type, Object... keys) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, NotFoundException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    void touchP(Class<?> type, long ttl, TimeUnit unit, Object... keys) throws UnsupportedOperationException,
            IllegalStateException, IllegalArgumentException, TransactionException, NotFoundException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException;

    void touchA(Class<?> type, String name, Object... keys) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, NotFoundException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    void touchA(Class<?> type, long ttl, TimeUnit unit, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException,
            DataException;

    void deleteI(Class<?> type, Object id) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, NotFoundException, KeyViolationException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException;

    void deleteP(Class<?> type, Object... keys) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, NotFoundException, KeyViolationException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException;

    void deleteA(Class<?> type, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException;

    void deleteX(Class<?> type, Object... beans) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, NotFoundException, KeyViolationException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> T removeI(Class<T> type, Object id) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, NotFoundException, KeyViolationException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> T removeP(Class<T> type, Object... keys) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, NotFoundException, KeyViolationException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> T removeA(Class<T> type, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException;

    /** Cursor operations support */

    <T> Cursor<T> cursorI(Class<T> type)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> Cursor<T> cursorP(Class<T> type)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> Cursor<T> cursorA(Class<T> type, String name)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> Cursor<T> cursorX(Class<T> type, Object... beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> Cursor<T> cursorQ(Class<T> type, Query query)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> Cursor<T> cursorN(Class<T> type, String nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    <T> Cursor<T> cursorN(Class<T> type, NativeQuery<T> nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    /** Counting operations support */

    long count(Class<?> type) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, OperationTimeoutException, NotEnoughResourceException, DataException;

    long countX(Class<?> type, Object... beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    long countQ(Class<?> type, Query query)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    long countN(Class<?> type, String nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    long countN(Class<?> type, NativeQuery<?> nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    /** Transaction operations support */

    Transaction begin() throws UnsupportedOperationException, IllegalStateException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    Transaction current() throws UnsupportedOperationException, IllegalStateException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    void commit(Transaction txn) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, OperationTimeoutException, NotEnoughResourceException, DataException;

    void rollback(Transaction txn)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException;

    /**
     * Tells whether or not this data manager is open.
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

    /**
     * Releases this {@code DataManager} object's resources immediately instead of waiting for them
     * to be automatically released.
     * <P>
     * Calling the method {@code close} on a {@code DataManager} object that is already closed is a
     * no-op.
     * <P>
     * It is <b>strongly recommended</b> that an application explicitly commits or rolls back an
     * active transaction prior to calling the {@code close} method. If the {@code close} method is
     * called and there is an active transaction, the results are implementation-defined.
     * 
     * @throws DataException
     */
    void close() throws DataException;
}