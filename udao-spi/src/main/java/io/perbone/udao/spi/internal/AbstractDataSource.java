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

package io.perbone.udao.spi.internal;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.perbone.toolbox.provider.NotEnoughResourceException;
import io.perbone.toolbox.provider.OperationTimeoutException;
import io.perbone.udao.Cursor;
import io.perbone.udao.DataConstraintViolationException;
import io.perbone.udao.KeyViolationException;
import io.perbone.udao.NotFoundException;
import io.perbone.udao.query.NativeQuery;
import io.perbone.udao.query.Query;
import io.perbone.udao.spi.Cache;
import io.perbone.udao.spi.DataProviderException;
import io.perbone.udao.spi.DataSource;
import io.perbone.udao.transaction.Transaction;
import io.perbone.udao.transaction.TransactionException;
import io.perbone.udao.util.StorableInfo;

/**
 * This class provides a skeletal implementation of the {@code DataSource} interface, to minimize
 * the effort required to implement this interface.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public abstract class AbstractDataSource implements DataSource
{
    protected String MESSAGE_FAIL_CANNOT_OPEN = "Cannot open this data source";

    protected String MESSAGE_FAIL_CANNOT_CLOSE = "Cannot close this data source";

    protected String MESSAGE_FAIL_ALREADY_OPEN = "Attempt to open already open data source object";

    protected String MESSAGE_FAIL_CHECK_OPEN = "Attempt to use non-open data source object";

    protected String MESSAGE_FAIL_CHECK_MANAGED_TYPE = "Not a managed bean type";

    protected String MESSAGE_FAIL_UNSUPPORTED_OPERATION = "Data source feature not supported by this implementation";

    protected String MESSAGE_KEY_VIOLATION = "Key constraint fails";

    protected String MESSAGE_FOREIGN_KEY_VIOLATION = "Foreign key constraint fails";

    protected String MESSAGE_COULD_NOT_UPDATE = "Could not update an entity in the store";

    private final AtomicBoolean open = new AtomicBoolean(false);

    public AbstractDataSource()
    {
        // do nothing
    }

    @Override
    public boolean accepts(Class<?> type) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T create(Transaction txn, Cache cache, T bean)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T create(Transaction txn, Cache cache, T bean, long ttl, TimeUnit unit)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> List<T> create(Transaction txn, Cache cache, List<T> beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> List<T> create(Transaction txn, Cache cache, List<T> beans, long ttl, TimeUnit unit)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T save(Transaction txn, Cache cache, T bean) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T save(Transaction txn, Cache cache, T bean, long ttl, TimeUnit unit)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> List<T> save(Transaction txn, Cache cache, List<T> beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> List<T> save(Transaction txn, Cache cache, List<T> beans, long ttl, TimeUnit unit)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T fetchI(Transaction txn, Cache cache, Class<T> type, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> List<T> fetchI(Transaction txn, Cache cache, Class<T> type, Object... ids)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T fetchP(Transaction txn, Cache cache, Class<T> type, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T fetchA(Transaction txn, Cache cache, Class<T> type, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public boolean containsI(Transaction txn, Cache cache, Class<?> type, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public boolean containsP(Transaction txn, Cache cache, Class<?> type, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public boolean containsA(Transaction txn, Cache cache, Class<?> type, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T updateI(Transaction txn, Cache cache, T bean, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T updateI(Transaction txn, Cache cache, T bean, long ttl, TimeUnit unit, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T updateP(Transaction txn, Cache cache, T bean, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T updateP(Transaction txn, Cache cache, T bean, long ttl, TimeUnit unit, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T updateA(Transaction txn, Cache cache, T bean, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T updateA(Transaction txn, Cache cache, T bean, long ttl, TimeUnit unit, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T patchI(Transaction txn, Cache cache, T bean, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T patchI(Transaction txn, Cache cache, T bean, long ttl, TimeUnit unit, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T patchP(Transaction txn, Cache cache, T bean, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T patchP(Transaction txn, Cache cache, T bean, long ttl, TimeUnit unit, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T patchA(Transaction txn, Cache cache, T bean, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T patchA(Transaction txn, Cache cache, T bean, long ttl, TimeUnit unit, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void touchI(Transaction txn, Cache cache, Class<?> type, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException,
            DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void touchI(Transaction txn, Cache cache, Class<?> type, long ttl, TimeUnit unit, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException,
            DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void touchP(Transaction txn, Cache cache, Class<?> type, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException,
            DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void touchP(Transaction txn, Cache cache, Class<?> type, long ttl, TimeUnit unit, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException,
            DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void touchA(Transaction txn, Cache cache, Class<?> type, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException,
            DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void touchA(Transaction txn, Cache cache, Class<?> type, long ttl, TimeUnit unit, String name,
            Object... keys) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, NotFoundException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void deleteI(Transaction txn, Cache cache, Class<?> type, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void deleteP(Transaction txn, Cache cache, Class<?> type, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void deleteA(Transaction txn, Cache cache, Class<?> type, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void deleteX(Transaction txn, Cache cache, Class<?> type, Object... beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T removeI(Transaction txn, Cache cache, Class<T> type, Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T removeP(Transaction txn, Cache cache, Class<T> type, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> T removeA(Transaction txn, Cache cache, Class<T> type, String name, Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> Cursor<T> cursorI(Transaction txn, Cache cache, Class<T> type)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> Cursor<T> cursorP(Transaction txn, Cache cache, Class<T> type)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> Cursor<T> cursorA(Transaction txn, Cache cache, Class<T> type, String name)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> Cursor<T> cursorX(Transaction txn, Cache cache, Class<T> type, Object... beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> Cursor<T> cursorQ(Transaction txn, Cache cache, Class<T> type, Query query)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> Cursor<T> cursorN(Transaction txn, Cache cache, Class<T> type, String nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public <T> Cursor<T> cursorN(Transaction txn, Cache cache, Class<T> type, NativeQuery<T> nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public long count(Transaction txn, Cache cache, Class<?> type)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public long countX(Transaction txn, Cache cache, Class<?> type, Object... beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public long countQ(Transaction txn, Cache cache, Class<?> type, Query query)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public long countN(Transaction txn, Cache cache, Class<?> type, String nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public long countN(Transaction txn, Cache cache, Class<?> type, NativeQuery<?> nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void expires(Cache cache, Class<?> type) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void expires(Cache cache, Class<?> type, Object criteria)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void invalidate(Cache cache, Class<?> type) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public long prune(Cache cache, Class<?> type, Object criteria)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void clear(Cache cache, Class<?> type) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void evict() throws UnsupportedOperationException, IllegalStateException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void open()
            throws IllegalStateException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        if (open.compareAndSet(false, true))
        {
            try
            {
                this.onOpen();
            }
            catch (final DataProviderException e)
            {
                open.set(false); // Abort open
                throw e;
            }
            catch (final Exception e)
            {
                open.set(false); // Abort open
                throw new DataProviderException(MESSAGE_FAIL_CANNOT_OPEN, e);
            }
        }
        else
        {
            throw new IllegalStateException(MESSAGE_FAIL_ALREADY_OPEN);
        }
    }

    @Override
    public void close() throws DataProviderException
    {
        if (open.compareAndSet(true, false))
        {
            try
            {
                this.onClose();
            }
            catch (final DataProviderException e)
            {
                open.set(true); // Abort close
                throw e;
            }
            catch (final Exception e)
            {
                open.set(true); // Abort close
                throw new DataProviderException(MESSAGE_FAIL_CANNOT_CLOSE, e);
            }
        }
    }

    @Override
    public boolean isOpen()
    {
        return open.get();
    }

    /**
     * Checks if this data source can manage the bean class type.
     * 
     * @param type
     *            The bean class type
     * @param types
     *            Collection of managed bean class types
     * 
     * @throws IllegalArgumentException
     *             if either the bean class type is null or it is not managed by this data source
     */
    protected void checkManagedType(final Class<?> type, final Class<?>... types) throws IllegalArgumentException
    {
        assert (type != null);

        List<Class<?>> list = Arrays.asList(types);
        if (!list.contains(type))
            throw new IllegalStateException(MESSAGE_FAIL_CHECK_MANAGED_TYPE);
    }

    /**
     * Checks if this data source can manage the bean class type.
     * 
     * @param bean
     *            The bean object
     * @param types
     *            Collection of managed bean class types
     * 
     * @throws IllegalArgumentException
     *             if either the bean class type is null or it is not managed by this data source
     */
    protected void checkManagedType(final Object bean, final Class<?>... types) throws IllegalArgumentException
    {
        assert (bean != null);

        checkManagedType(bean.getClass(), types);
    }

    /**
     * Checks if this data source is currently open
     * 
     * @throws IllegalStateException
     *             when it is not open
     */
    protected void checkOpen() throws IllegalStateException
    {
        if (!open.get())
            throw new IllegalStateException(MESSAGE_FAIL_CHECK_OPEN);
    }

    /**
     * Tells if the current is executing inside a transaction.
     * 
     * @param txn
     *            the transaction to test for active transaction
     * 
     * @return {@code true} if there is a transaction in progress; {@code false} otherwise
     */
    protected boolean transactionInProgress(Transaction txn)
    {
        return txn == null ? false : txn.isActive();
    }

    /**
     * Parses the table name for the entity type.
     * <p>
     * It will try for the target alias first and if not present it will use the type name as the
     * table name.
     * 
     * @param sinfo
     *            the {@link StorableInfo} for the entity type
     * 
     * @return the table name
     */
    protected String parseTableName(final String target, final StorableInfo sinfo)
    {
        String schema = sinfo.schema();
        String name = sinfo.firstAliasForTarget(target);

        if (name == null)
            name = sinfo.name();

        return schema == null ? name : schema + "." + name;
    }

    /**
     * Open entry point for concrete implementations.
     * 
     * @throws IllegalStateException
     *             if this data source is already open
     * @throws OperationTimeoutException
     *             if the operation times out
     * @throws StorageProviderException
     *             if cannot open this data source
     */
    protected abstract void onOpen()
            throws IllegalStateException, OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    /**
     * Close entry point for concrete implementations.
     * 
     * @throws StorageProviderException
     *             if cannot closes the this data source
     */
    protected abstract void onClose() throws DataProviderException;
}