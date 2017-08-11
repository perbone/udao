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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.perbone.toolbox.annotation.AnnotationScanner;
import io.perbone.toolbox.id.IdFactory;
import io.perbone.toolbox.provider.NotEnoughResourceException;
import io.perbone.toolbox.provider.OperationTimeoutException;
import io.perbone.toolbox.security.Fortuna;
import io.perbone.toolbox.validation.StringValidations;
import io.perbone.udao.Cursor;
import io.perbone.udao.DataConstraintViolationException;
import io.perbone.udao.DataException;
import io.perbone.udao.DataManager;
import io.perbone.udao.KeyViolationException;
import io.perbone.udao.NotFoundException;
import io.perbone.udao.annotation.AlternateKey;
import io.perbone.udao.annotation.AlternateKeys;
import io.perbone.udao.annotation.DataType;
import io.perbone.udao.annotation.Immutable;
import io.perbone.udao.annotation.Metadata;
import io.perbone.udao.annotation.Metadata.MetadataType;
import io.perbone.udao.annotation.PrimaryKey;
import io.perbone.udao.annotation.Storable;
import io.perbone.udao.annotation.SurrogateKey;
import io.perbone.udao.query.NativeQuery;
import io.perbone.udao.query.Query;
import io.perbone.udao.spi.Cache;
import io.perbone.udao.spi.DataProvider;
import io.perbone.udao.spi.DataProviderException;
import io.perbone.udao.spi.DataSource;
import io.perbone.udao.transaction.InvalidTransactionException;
import io.perbone.udao.transaction.Transaction;
import io.perbone.udao.transaction.TransactionException;
import io.perbone.udao.util.ElementInfo;
import io.perbone.udao.util.EntityUtils;
import io.perbone.udao.util.StorableInfo;

/**
 * Concrete implementation of {@link DataManager} interface.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public final class DataManagerImpl implements DataManager
{
    private static final Logger logger = LoggerFactory.getLogger(DataManagerImpl.class);

    private final String MESSAGE_FAIL_UNSUPPORTED_OPERATION = "Data manager feature not supported by this implementation";

    private AtomicBoolean closed = new AtomicBoolean(false);

    /** READ-WRITE providers for this manager */
    private final List<DataProvider> rwProviders = new ArrayList<>();

    /** READ-ONLY providers for this manager */
    private final List<DataProvider> roProviders = new ArrayList<>();

    /** Place holder for all active data sources */
    private final Set<DataSource> activeDataSources = Collections.synchronizedSet(new HashSet<DataSource>());

    /** Controls the transaction in progress */
    private final AtomicBoolean transactionInProgress = new AtomicBoolean(false);
    private final AtomicBoolean transactionEnding = new AtomicBoolean(false);
    private DataProvider txnProvider;
    private Transaction txn = null;

    /** Cache pool to be used for all data sources */
    private final CachePool cachePool;

    /**
     * Creates a {@code DataManagerImpl} object.
     * 
     * @param providers
     *            the set of providers to be used by this storage manager
     * @param cachePool
     *            the cache pool to be used ny this manager
     */
    DataManagerImpl(final Set<DataProvider> providers, final CachePool cachePool)
    {
        if (providers.isEmpty())
            throw new IllegalArgumentException("Data Providers set is empty");

        for (DataProvider provider : providers)
        {
            if (provider.isReadOnly())
                roProviders.add(provider);
            else
                rwProviders.add(provider);
        }

        this.cachePool = cachePool;
    }

    @Override
    public <T> T create(final T bean) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(bean);

        T result;

        final Class<?> type = bean.getClass();

        enforceAnnotationsOnCreation(bean);

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(bean);

        try
        {
            result = ds.create(txn, cache, bean);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T create(final T bean, final long ttl, final TimeUnit unit) throws UnsupportedOperationException,
            IllegalStateException, IllegalArgumentException, TransactionException, KeyViolationException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException
    {
        // FIXME should enforce ttl

        checkOpen();
        checkManagedType(bean);

        T result;

        final Class<?> type = bean.getClass();

        enforceAnnotationsOnCreation(bean);

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(bean);

        try
        {
            result = ds.create(txn, cache, bean, ttl, unit);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> List<T> create(final List<T> beans) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(beans.toArray());

        List<T> result;

        final Class<?> type = beans.get(0).getClass();

        for (T bean : beans)
            enforceAnnotationsOnCreation(bean);

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(beans);

        try
        {
            result = ds.create(txn, cache, beans);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> List<T> create(final List<T> beans, final long ttl, final TimeUnit unit)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException
    {
        // FIXME should enforce ttl

        checkOpen();
        checkManagedType(beans.toArray());

        List<T> result;

        final Class<?> type = beans.get(0).getClass();

        for (T bean : beans)
            enforceAnnotationsOnCreation(bean);

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(beans);

        try
        {
            result = ds.create(txn, cache, beans, ttl, unit);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T save(final T bean) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        // FIXME should create or update

        checkOpen();
        checkManagedType(bean);

        T result;

        final Class<?> type = bean.getClass();

        enforceAnnotationsOnCreation(bean);

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(bean);

        try
        {
            result = ds.save(txn, cache, bean);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T save(final T bean, final long ttl, final TimeUnit unit) throws UnsupportedOperationException,
            IllegalStateException, IllegalArgumentException, TransactionException, KeyViolationException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException
    {
        // FIXME should enforce ttl
        // FIXME should create or update

        checkOpen();
        checkManagedType(bean);

        T result;

        final Class<?> type = bean.getClass();

        enforceAnnotationsOnCreation(bean);

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(bean);

        try
        {
            result = ds.save(txn, cache, bean, ttl, unit);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> List<T> save(final List<T> beans) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        // FIXME should create or update

        checkOpen();
        checkManagedType(beans.toArray());

        List<T> result;

        final Class<?> type = beans.get(0).getClass();

        for (T bean : beans)
            enforceAnnotationsOnCreation(bean);

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(beans);

        try
        {
            result = ds.save(txn, cache, beans);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> List<T> save(final List<T> beans, final long ttl, final TimeUnit unit)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException
    {
        // FIXME should enforce ttl
        // FIXME should create or update

        checkOpen();
        checkManagedType(beans.toArray());

        List<T> result;

        final Class<?> type = beans.get(0).getClass();

        for (T bean : beans)
            enforceAnnotationsOnCreation(bean);

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(beans);

        try
        {
            result = ds.save(txn, cache, beans, ttl, unit);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T fetchI(final Class<T> type, final Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkSurrogateKey(type, id);

        T result;

        final DataSource ds = openDataSource(type, false);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.fetchI(txn, cache, type, id);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> List<T> fetchI(final Class<T> type, final Object... ids)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkSurrogateKey(type, ids);

        List<T> result;

        final DataSource ds = openDataSource(type, false);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.fetchI(txn, cache, type, ids);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T fetchP(final Class<T> type, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkPrimaryKey(type, keys);

        T result;

        final DataSource ds = openDataSource(type, false);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.fetchP(txn, cache, type, keys);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T fetchA(final Class<T> type, final String name, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkAlternateKey(type, name, keys);

        T result;

        final DataSource ds = openDataSource(type, false);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.fetchA(txn, cache, type, name, keys);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public boolean containsI(final Class<?> type, final Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkSurrogateKey(type, id);

        boolean result = false;

        final DataSource ds = openDataSource(type, false);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.containsI(txn, cache, type, id);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public boolean containsP(final Class<?> type, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkPrimaryKey(type, keys);

        boolean result = false;

        final DataSource ds = openDataSource(type, false);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.containsP(txn, cache, type, keys);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public boolean containsA(final Class<?> type, final String name, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkAlternateKey(type, name, keys);

        boolean result = false;

        final DataSource ds = openDataSource(type, false);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.containsA(txn, cache, type, name, keys);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T updateI(final T bean, final Object id) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, NotFoundException, KeyViolationException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(bean);

        final Class<?> type = bean.getClass();

        checkMutable(type);
        checkSurrogateKey(type, id);

        enforceAnnotationsOnMutation(bean);

        if (!isDirty(bean))
            return bean;

        T result;

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(bean);

        try
        {
            result = ds.updateI(txn, cache, bean, id);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T updateI(final T bean, final long ttl, final TimeUnit unit, final Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(bean);

        final Class<?> type = bean.getClass();

        checkMutable(type);
        checkSurrogateKey(type, id);

        enforceAnnotationsOnMutation(bean);

        if (!isDirty(bean))
            return bean;

        T result;

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(bean);

        try
        {
            result = ds.updateI(txn, cache, bean, ttl, unit, id);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T updateP(final T bean, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(bean);

        final Class<?> type = bean.getClass();

        checkMutable(type);
        checkPrimaryKey(type, keys);

        enforceAnnotationsOnMutation(bean);

        if (!isDirty(bean))
            return bean;

        T result;

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(bean);

        try
        {
            result = ds.updateP(txn, cache, bean, keys);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T updateP(final T bean, final long ttl, final TimeUnit unit, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(bean);

        final Class<?> type = bean.getClass();

        checkMutable(type);
        checkPrimaryKey(type, keys);

        enforceAnnotationsOnMutation(bean);

        if (!isDirty(bean))
            return bean;

        T result;

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(bean);

        try
        {
            result = ds.updateP(txn, cache, bean, ttl, unit, keys);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T updateA(final T bean, final String name, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(bean);

        final Class<?> type = bean.getClass();

        checkMutable(type);
        checkAlternateKey(type, name, keys);

        enforceAnnotationsOnMutation(bean);

        if (!isDirty(bean))
            return bean;

        T result;

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(bean);

        try
        {
            result = ds.updateA(txn, cache, bean, name, keys);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T updateA(final T bean, final long ttl, final TimeUnit unit, final String name, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(bean);

        final Class<?> type = bean.getClass();

        checkMutable(type);
        checkAlternateKey(type, name, keys);

        enforceAnnotationsOnMutation(bean);

        if (!isDirty(bean))
            return bean;

        T result;

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(bean);

        try
        {
            result = ds.updateA(txn, cache, bean, ttl, unit, name, keys);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T patchI(final T bean, final Object id) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, NotFoundException, KeyViolationException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(bean);

        final Class<?> type = bean.getClass();

        checkMutable(type);
        checkSurrogateKey(type, id);

        enforceAnnotationsOnMutation(bean);

        if (!isDirty(bean))
            return bean;

        T result;

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(bean);

        try
        {
            result = ds.patchI(txn, cache, bean, id);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T patchI(final T bean, final long ttl, final TimeUnit unit, final Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(bean);

        final Class<?> type = bean.getClass();

        checkMutable(type);
        checkSurrogateKey(type, id);

        enforceAnnotationsOnMutation(bean);

        if (!isDirty(bean))
            return bean;

        T result;

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(bean);

        try
        {
            result = ds.patchI(txn, cache, bean, ttl, unit, id);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T patchP(final T bean, final Object... keys) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, NotFoundException, KeyViolationException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(bean);

        final Class<?> type = bean.getClass();

        checkMutable(type);
        checkPrimaryKey(type, keys);

        enforceAnnotationsOnMutation(bean);

        if (!isDirty(bean))
            return bean;

        T result;

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(bean);

        try
        {
            result = ds.patchP(txn, cache, bean, keys);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T patchP(final T bean, final long ttl, final TimeUnit unit, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(bean);

        final Class<?> type = bean.getClass();

        checkMutable(type);
        checkPrimaryKey(type, keys);

        enforceAnnotationsOnMutation(bean);

        if (!isDirty(bean))
            return bean;

        T result;

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(bean);

        try
        {
            result = ds.patchP(txn, cache, bean, ttl, unit, keys);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T patchA(final T bean, final String name, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(bean);

        final Class<?> type = bean.getClass();

        checkMutable(type);
        checkAlternateKey(type, name, keys);

        enforceAnnotationsOnMutation(bean);

        if (!isDirty(bean))
            return bean;

        T result;

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(bean);

        try
        {
            result = ds.patchA(txn, cache, bean, name, keys);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T patchA(final T bean, final long ttl, final TimeUnit unit, final String name, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(bean);

        final Class<?> type = bean.getClass();

        checkMutable(type);
        checkAlternateKey(type, name, keys);

        enforceAnnotationsOnMutation(bean);

        if (!isDirty(bean))
            return bean;

        T result;

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(bean);

        try
        {
            result = ds.patchA(txn, cache, bean, ttl, unit, name, keys);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public void touchI(final Class<?> type, final Object id) throws UnsupportedOperationException,
            IllegalStateException, IllegalArgumentException, TransactionException, NotFoundException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkMutable(type);
        checkSurrogateKey(type, id);

        // FIXME needs to enforce metadata update

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(type);

        try
        {
            ds.touchI(txn, cache, type, id);
        }
        finally
        {
            closeDataSource(ds);
        }
    }

    @Override
    public void touchI(final Class<?> type, final long ttl, final TimeUnit unit, final Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException,
            DataException
    {
        checkOpen();
        checkManagedType(type);
        checkMutable(type);
        checkSurrogateKey(type, id);

        // FIXME needs to enforce metadata update

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(type);

        try
        {
            ds.touchI(txn, cache, type, ttl, unit, id);
        }
        finally
        {
            closeDataSource(ds);
        }
    }

    @Override
    public void touchP(final Class<?> type, final Object... keys) throws UnsupportedOperationException,
            IllegalStateException, IllegalArgumentException, TransactionException, NotFoundException,
            DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkMutable(type);
        checkPrimaryKey(type, keys);

        // FIXME needs to enforce metadata update

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(type);

        try
        {
            ds.touchP(txn, cache, type, keys);
        }
        finally
        {
            closeDataSource(ds);
        }
    }

    @Override
    public void touchP(final Class<?> type, final long ttl, final TimeUnit unit, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException,
            DataException
    {
        checkOpen();
        checkManagedType(type);
        checkMutable(type);
        checkPrimaryKey(type, keys);

        // FIXME needs to enforce metadata update

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(type);

        try
        {
            ds.touchP(txn, cache, type, ttl, unit, keys);
        }
        finally
        {
            closeDataSource(ds);
        }
    }

    @Override
    public void touchA(final Class<?> type, final String name, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException,
            DataException
    {
        checkOpen();
        checkManagedType(type);
        checkMutable(type);
        checkAlternateKey(type, name, keys);

        // FIXME needs to enforce metadata update

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(type);

        try
        {
            ds.touchA(txn, cache, type, name, keys);
        }
        finally
        {
            closeDataSource(ds);
        }
    }

    @Override
    public void touchA(final Class<?> type, final long ttl, final TimeUnit unit, final String name,
            final Object... keys) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, NotFoundException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkMutable(type);
        checkAlternateKey(type, name, keys);

        // FIXME needs to enforce metadata update

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(type);

        try
        {
            ds.touchA(txn, cache, type, ttl, unit, name, keys);
        }
        finally
        {
            closeDataSource(ds);
        }
    }

    @Override
    public void deleteI(final Class<?> type, final Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkSurrogateKey(type, id);

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(type);

        try
        {
            ds.deleteI(txn, cache, type, id);
        }
        finally
        {
            closeDataSource(ds);
        }
    }

    @Override
    public void deleteP(final Class<?> type, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkPrimaryKey(type, keys);

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(type);

        try
        {
            ds.deleteP(txn, cache, type, keys);
        }
        finally
        {
            closeDataSource(ds);
        }
    }

    @Override
    public void deleteA(final Class<?> type, final String name, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkAlternateKey(type, name, keys);

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(type);

        try
        {
            ds.deleteA(txn, cache, type, name, keys);
        }
        finally
        {
            closeDataSource(ds);
        }
    }

    @Override
    public void deleteX(final Class<?> type, final Object... beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkByExample(type, beans);

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(type);

        try
        {
            ds.deleteX(txn, cache, type, beans);
        }
        finally
        {
            closeDataSource(ds);
        }
    }

    @Override
    public <T> T removeI(final Class<T> type, final Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkSurrogateKey(type, id);

        T result;

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.removeI(txn, cache, type, id);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T removeP(final Class<T> type, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkPrimaryKey(type, keys);

        T result;

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.removeP(txn, cache, type, keys);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> T removeA(final Class<T> type, final String name, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkAlternateKey(type, name, keys);

        T result;

        final DataSource ds = openDataSource(type, true);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.removeA(txn, cache, type, name, keys);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> Cursor<T> cursorI(final Class<T> type)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkSurrogateKey(type);

        if (!AnnotationScanner.isAnnotationPresent(type, SurrogateKey.class))
            throw new IllegalArgumentException("Surrogate key annotation is not present");

        Cursor<T> result = null;

        final DataSource ds = openDataSource(type, false);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.cursorI(txn, cache, type);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> Cursor<T> cursorP(final Class<T> type)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkPrimaryKey(type);

        if (!AnnotationScanner.isAnnotationPresent(type, PrimaryKey.class))
            throw new IllegalArgumentException("Primary key annotation is not present");

        Cursor<T> result = null;

        final DataSource ds = openDataSource(type, false);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.cursorP(txn, cache, type);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> Cursor<T> cursorA(final Class<T> type, final String name)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);

        if (!AnnotationScanner.isAnnotationPresent(type, AlternateKey.class))
            throw new IllegalArgumentException("Alternate key annotation is not present");

        Cursor<T> result = null;

        final DataSource ds = openDataSource(type, false);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.cursorA(txn, cache, type, name);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> Cursor<T> cursorX(final Class<T> type, final Object... beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkByExample(type, beans);

        Cursor<T> result = null;

        final DataSource ds = openDataSource(type, false);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.cursorX(txn, cache, type, beans);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> Cursor<T> cursorQ(final Class<T> type, final Query query)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);
        checkQuery(type, query);

        Cursor<T> result = null;

        final DataSource ds = openDataSource(type, false);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.cursorQ(txn, cache, type, query);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> Cursor<T> cursorN(final Class<T> type, final String nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkManagedType(type);

        if (!StringValidations.isValid(nquery))
            throw new IllegalArgumentException("Invalid native query statement");

        Cursor<T> result = null;

        final DataSource ds = openDataSource(type, false);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.cursorN(txn, cache, type, nquery);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public <T> Cursor<T> cursorN(final Class<T> type, final NativeQuery<T> nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public long count(final Class<?> type)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        // TODO count value should be cached by type as it's too expensive to always call

        checkOpen();
        checkManagedType(type);

        long result;

        final DataSource ds = openDataSource(type, false);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.count(txn, cache, type);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public long countX(final Class<?> type, final Object... beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        // TODO count value should be cached by type as it's too expensive to always call

        checkOpen();
        checkManagedType(type);
        checkByExample(type, beans);

        long result;

        final DataSource ds = openDataSource(type, false);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.countX(txn, cache, type, beans);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public long countQ(final Class<?> type, final Query query)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        // TODO count value should be cached by type as it's too expensive to always call

        checkOpen();
        checkManagedType(type);
        checkQuery(type, query);

        long result;

        final DataSource ds = openDataSource(type, false);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.countQ(txn, cache, type, query);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public long countN(final Class<?> type, final String nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        // TODO count value should be cached by type as it's too expensive to always call

        checkOpen();
        checkManagedType(type);

        long result;

        final DataSource ds = openDataSource(type, false);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.countN(txn, cache, type, nquery);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public long countN(final Class<?> type, final NativeQuery<?> nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        // TODO count value should be cached by type as it's too expensive to always call

        checkOpen();
        checkManagedType(type);

        long result;

        final DataSource ds = openDataSource(type, false);
        final Cache cache = cachePool.get(type);

        try
        {
            result = ds.countN(txn, cache, type, nquery);
        }
        finally
        {
            closeDataSource(ds);
        }

        return result;
    }

    @Override
    public Transaction begin() throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();

        if (transactionInProgress.compareAndSet(false, true))
        {
            try
            {
                final String id = IdFactory.uuid();

                txnProvider = getProvider(true);
                txn = txnProvider.begin(id);

                return txn;
            }
            catch (final DataProviderException e)
            {
                transactionInProgress.set(false);

                throw e;
            }
        }
        else
        {
            throw new IllegalStateException("A transaction is already in progress");
        }
    }

    @Override
    public void commit(final Transaction txn)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkTransaction(txn);

        if (transactionEnding.compareAndSet(false, true))
        {
            try
            {
                txnProvider.commit(txn);
                this.txn = null;
                txnProvider = null;
                transactionInProgress.set(false);
            }
            catch (final DataProviderException e)
            {
                throw e;
            }
            finally
            {
                transactionEnding.set(false);
            }
        }
        else
        {
            throw new IllegalStateException("Transaction ending already in progress");
        }
    }

    @Override
    public void rollback(final Transaction txn)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataException
    {
        checkOpen();
        checkTransaction(txn);

        if (transactionEnding.compareAndSet(false, true))
        {
            try
            {
                txnProvider.rollback(txn);
            }
            catch (final DataProviderException e)
            {
                /*
                 * Provider should never fail to roll back otherwise it can have an invalid internal
                 * state and we can do little to nothing about it.
                 */
                throw e;
            }
            finally
            {
                /* We assume everything happens evenly */
                this.txn = null;
                txnProvider = null;
                transactionInProgress.set(false);

                transactionEnding.set(false);
            }
        }
        else
        {
            throw new IllegalStateException("Transaction ending already in progress");
        }
    }

    @Override
    public boolean isOpen()
    {
        return !closed.get();
    }

    @Override
    public void close() throws DataException
    {
        if (closed.compareAndSet(false, true))
        {
            if (transactionInProgress.get())
            {
                try
                {
                    txnProvider.rollback(txn);
                }
                catch (final DataException e)
                {
                    // Close quietly
                    logger.warn("Unable to rollback the last pending transaction; " + e.getMessage());
                }

                txn = null;
                txnProvider = null;
                transactionInProgress.set(false);
            }

            /* Release all active data sources */
            for (DataSource ds : activeDataSources)
            {
                try
                {
                    ds.close();
                }
                catch (final DataException e)
                {
                    // Close quietly
                    logger.warn("Could not close the data manager properly; " + e.getMessage());
                }
            }

            activeDataSources.clear();
        }
    }

    /**
     * Checks if this manager is currently open.
     * 
     * @throws IllegalStateException
     *             if this manager is closed
     */
    private void checkOpen() throws IllegalStateException
    {
        if (closed.get())
            throw new IllegalStateException("Illegal invocation; this manager is closed");
    }

    /**
     * Checks if the given type is known to this manager.
     * 
     * @param type
     *            The bean type
     * 
     * @throws IllegalArgumentException
     *             if the type either is null or is unknown to this manager
     */
    private void checkManagedType(final Class<?> type) throws IllegalArgumentException
    {
        if (type == null)
            throw new IllegalStateException("Bean type cannot be null");

        if (!EntityUtils.isStorable(type))
            throw new IllegalStateException("Not a valid bean type");

        // TODO match for managed beans only (from XML etc)
        // throw new IllegalStateException("Not a managed bean type");
    }

    /**
     * Checks if the examplo set contains any valid samples.
     * 
     * @param type
     *            the bean type
     * @param beans
     *            the example set to be checked
     * @throws IllegalArgumentException
     *             if the example set is empty
     */
    private void checkByExample(final Class<?> type, final Object... beans) throws IllegalArgumentException
    {
        if (beans == null || beans.length == 0)
            throw new IllegalArgumentException("Invalid example samples: cannot be null");

        boolean hasValue = false;

        for (Object bean : beans)
        {
            checkManagedType(bean.getClass());

            if (!type.equals(bean.getClass()))
                throw new IllegalArgumentException("All example samples have to be the same type");

            if (!EntityUtils.values(bean).isEmpty())
                hasValue = true;
        }

        if (!hasValue)
            throw new IllegalArgumentException("Example samples have no useful values only null ones");
    }

    /**
     * Checks the bean set for managed beans.
     * 
     * @param bean
     *            the bean set
     * 
     * @throws IllegalArgumentException
     *             if any object from the set is null or the type is unknown to this manager
     */
    private void checkManagedType(final Object... beans) throws IllegalArgumentException
    {
        if (beans == null)
            throw new IllegalStateException("Beans cannot be null");
        if (beans.length == 0)
            throw new IllegalStateException("Beans cannot empty");

        for (Object bean : beans)
            checkManagedType(bean.getClass());
    }

    /**
     * Checks if the given query is valid.
     * 
     * @param type
     *            the bean type
     * @param query
     *            the query
     * 
     * @throws IllegalArgumentException
     *             if the query is invalid
     */
    private void checkQuery(final Class<?> type, final Query query) throws IllegalArgumentException
    {
        if (query == null)
            throw new IllegalArgumentException("Query cannot be null");
        // FIXME proper validate the query object
    }

    /**
     * Checks if the bean type is decorated if {@link PrimaryKey} annotation and if given keys match
     * the annotation.
     * 
     * @param type
     *            the bean type
     * @param keys
     *            the keys set
     * @throws IllegalArgumentException
     *             if the bean type is not decorated with {@link PrimaryKey} or if the keys do no
     *             match the annotation
     */
    private void checkPrimaryKey(final Class<?> type, final Object... keys) throws IllegalArgumentException
    {
        if (!AnnotationScanner.isAnnotationPresent(type, PrimaryKey.class))
            throw new IllegalArgumentException("Primary key annotation is not present");

        // TODO match keys with the ones specified (values count, types etc)
    }

    /**
     * Checks if the bean type is decorated if {@link AlternateKey} annotation and if given keys
     * match the annotation.
     * 
     * @param type
     *            the bean type
     * @param name
     *            the alternate key name
     * @param keys
     *            the keys set
     * @throws IllegalArgumentException
     *             if the bean type is not decorated with {@link AlternateKey} or if the keys do no
     *             match the annotation
     */
    private void checkAlternateKey(final Class<?> type, final String name, final Object... keys)
            throws IllegalArgumentException
    {
        if (!AnnotationScanner.isAnnotationPresent(type, AlternateKeys.class)
                && !AnnotationScanner.isAnnotationPresent(type, AlternateKey.class))
            throw new IllegalArgumentException("Alternate key annotation is not present");

        if (!StringValidations.isValid(name) || EntityUtils.info(type).alternateKey(name).isEmpty())
            throw new IllegalArgumentException("Alternate key name is invalid");

        // FIXME match keys with the ones specified (values count, types etc)
    }

    /**
     * Validates that the entity type is mutable, otherwise throws an ConstraintViolationException.
     * <p>
     * InvalidOperationException.
     * 
     * @param type
     *            the entity type
     * @throws ConstraintViolationException
     */
    private void checkMutable(final Class<?> type) throws ConstraintViolationException
    {
        if (AnnotationScanner.isAnnotationPresent(type, Immutable.class)
                || !AnnotationScanner.getAnnotation(type, Storable.class).mutable())
            throw new DataConstraintViolationException("Cannot change the content of an immutable type");
    }

    /**
     * Checks if the bean type is decorated if {@link SurrogateKey} annotation and if given id is
     * valid.
     * 
     * @param type
     *            the bean type
     * @param ids
     *            the key set
     * 
     * @throws IllegalArgumentException
     *             if the bean type is not decorated with {@link SurrogateKey} or if id is invalid
     */
    private void checkSurrogateKey(final Class<?> type, final Object... ids) throws IllegalArgumentException
    {
        if (!AnnotationScanner.isAnnotationPresent(type, SurrogateKey.class))
            throw new IllegalArgumentException("Surrogate key annotation is not present");

        for (final Object id : ids)
        {
            if (id == null)
                throw new IllegalArgumentException("Surrogate key identifier cannot be null");
        }
    }

    /**
     * Enforces annotations on bean creation.
     * 
     * @param bean
     *            the bean to be enforced
     */
    private void enforceAnnotationsOnCreation(final Object bean)
    {
        final Class<?> type = bean.getClass();

        final StorableInfo sinfo = EntityUtils.info(type);

        final long now = System.currentTimeMillis();

        /**
         * Surrogate key generation (when the field value is null). Supports only String and Long.
         */
        final ElementInfo einfo = sinfo.surrogateKey();

        if (einfo != null && EntityUtils.value(bean, einfo.name()) == null)
        {
            if (einfo.dataType() == DataType.STRING)
            {
                EntityUtils.value(bean, einfo.name(), IdFactory.uuid());
            }
            else if (einfo.dataType() == DataType.LONG)
            {
                // FIXME should use id(divisor) method
                EntityUtils.value(bean, einfo.name(), IdFactory.id());
            }
            else
            {
                throw new UnsupportedOperationException(String.format(
                        "Unsupported type [%s] for surrogate key generation. Expecting types [java.lang.String, java.lang.Long]",
                        einfo.type().getName()));
            }
        }

        /**
         * Meta-data info.
         */
        for (final Field field : AnnotationScanner.scanFields(type, Metadata.class))
        {
            final Class<?> ftype = field.getType();
            final Object value = EntityUtils.value(bean, field.getName());

            final Metadata.MetadataType mtype = field.getAnnotation(Metadata.class).value();

            /* CREATION_DATE */
            if (mtype == MetadataType.CREATED_DATE && value == null)
            {
                checkSupportedTypes(field, mtype, Date.class, Long.class);

                if (ftype.equals(Date.class))
                    EntityUtils.value(bean, field.getName(), new Date(now));
                else if (ftype.equals(Long.class))
                    EntityUtils.value(bean, field.getName(), now);
            }
            /* CREATION_AGENT */
            else if (mtype == MetadataType.CREATED_SERVICE && value == null)
            {
                checkSupportedTypes(field, mtype, String.class);

                if (ftype.equals(String.class))
                    EntityUtils.value(bean, field.getName(), EntityUtils.hostName());
            }
            /* EXPIRES */
            else if (mtype == MetadataType.EXPIRES && EntityUtils.hasTimeToLive(bean))
            {
                checkSupportedTypes(field, mtype, Date.class, Long.class);

                Long ttl = EntityUtils.timeToLive(bean);
                if (ttl != null)
                {
                    TimeUnit unit = TimeUnit.MILLISECONDS; // Defaults to MILLISECONDS
                    if (EntityUtils.hasTimeToLiveUnit(bean))
                    {
                        TimeUnit tmp = EntityUtils.timeToLiveUnit(bean);
                        unit = tmp == null ? unit : tmp;
                    }
                    long expires = now + TimeUnit.MILLISECONDS.convert(ttl, unit);

                    if (ftype.equals(Date.class))
                        EntityUtils.value(bean, field.getName(), new Date(expires));
                    else if (ftype.equals(Long.class))
                        EntityUtils.value(bean, field.getName(), expires);
                }
            }
        }
    }

    /**
     * Enforces annotations on bean access (fetch etc).
     * 
     * @param bean
     *            the bean to be enforced
     */
    @SuppressWarnings("unused")
    private void enforceAnnotationsOnAccess(final Object bean)
    {
        final Class<?> type = bean.getClass();

        final long now = System.currentTimeMillis();

        /**
         * Meta-data info.
         */
        for (final Field field : AnnotationScanner.scanFields(type, Metadata.class))
        {
            final Class<?> ftype = field.getType();

            final Metadata.MetadataType mtype = field.getAnnotation(Metadata.class).value();

            /* LAST_ACCESS_DATE */
            if (mtype == MetadataType.LAST_ACCESS_DATE)
            {
                checkSupportedTypes(field, mtype, Date.class, Long.class);

                if (ftype.equals(Date.class))
                    EntityUtils.value(bean, field.getName(), new Date(now));
                else if (ftype.equals(Long.class))
                    EntityUtils.value(bean, field.getName(), now);
            }
            /* LAST_ACCESS_AGENT */
            else if (mtype == MetadataType.LAST_ACCESS_SERVICE)
            {
                checkSupportedTypes(field, mtype, String.class);

                if (ftype.equals(String.class))
                    EntityUtils.value(bean, field.getName(), EntityUtils.hostName());
            }
        }
    }

    /**
     * Enforces annotations on bean update, patch and touch events.
     * 
     * @param bean
     *            the bean to be enforced
     */
    private void enforceAnnotationsOnMutation(final Object bean)
    {
        final Class<?> type = bean.getClass();

        final long now = System.currentTimeMillis();

        /**
         * Meta-data info.
         */
        for (final Field field : AnnotationScanner.scanFields(type, Metadata.class))
        {
            final Class<?> ftype = field.getType();

            final Metadata.MetadataType mtype = field.getAnnotation(Metadata.class).value();

            /* LAST_ACCESS_DATE */
            if (mtype == MetadataType.LAST_ACCESS_DATE)
            {
                checkSupportedTypes(field, mtype, Date.class, Long.class);

                if (ftype.equals(Date.class))
                    EntityUtils.value(bean, field.getName(), new Date(now));
                else if (ftype.equals(Long.class))
                    EntityUtils.value(bean, field.getName(), now);
            }
            /* LAST_ACCESS_AGENT */
            else if (mtype == MetadataType.LAST_ACCESS_SERVICE)
            {
                checkSupportedTypes(field, mtype, String.class);

                if (ftype.equals(String.class))
                    EntityUtils.value(bean, field.getName(), EntityUtils.hostName());
            }
            /* LAST_MODIFIED_DATE */
            else if (mtype == MetadataType.LAST_MODIFIED_DATE)
            {
                checkSupportedTypes(field, mtype, Date.class, Long.class);

                if (ftype.equals(Date.class))
                    EntityUtils.value(bean, field.getName(), new Date(now));
                else if (ftype.equals(Long.class))
                    EntityUtils.value(bean, field.getName(), now);
            }
            /* LAST_MODIFIED_AGENT */
            else if (mtype == MetadataType.LAST_MODIFIED_SERVICE)
            {
                checkSupportedTypes(field, mtype, String.class);

                if (ftype.equals(String.class))
                    EntityUtils.value(bean, field.getName(), EntityUtils.hostName());
            }
            /* EXPIRES */
            else if (mtype == MetadataType.EXPIRES && EntityUtils.hasTimeToLive(bean))
            {
                checkSupportedTypes(field, mtype, Date.class, Long.class);

                final Long ttl = EntityUtils.timeToLive(bean);

                if (ttl != null)
                {
                    TimeUnit unit = TimeUnit.MILLISECONDS; // Defaults to MILLISECONDS

                    if (EntityUtils.hasTimeToLiveUnit(bean))
                    {
                        TimeUnit tmp = EntityUtils.timeToLiveUnit(bean);
                        unit = tmp == null ? unit : tmp;
                    }

                    final long expires = now + TimeUnit.MILLISECONDS.convert(ttl, unit);

                    if (ftype.equals(Date.class))
                        EntityUtils.value(bean, field.getName(), new Date(expires));
                    else if (ftype.equals(Long.class))
                        EntityUtils.value(bean, field.getName(), expires);
                }
            }
        }
    }

    /**
     * Checks if the given field type is as expected for the meta-data annotation.
     * 
     * @param field
     *            the field to check
     * @param mtype
     *            the meta-data type
     * @param types
     *            the supported types
     * 
     * @throws UnsupportedOperationException
     *             if the field has an unsupported type
     */
    private void checkSupportedTypes(final Field field, final MetadataType mtype, final Class<?>... types)
            throws UnsupportedOperationException
    {
        String names = null;

        for (final Class<?> type : types)
        {
            if (field.getType().equals(type))
                return;

            names = (names == null ? type.getName() : names + ", " + type.getName());
        }

        throw new UnsupportedOperationException(
                String.format("Unsupported type [%s] for Metadata.%s. Expecting types [%s]", field.getType().getName(),
                        mtype, names));
    }

    /**
     * Checks if the given transaction is valid.
     * 
     * @param txn
     *            the transaction
     * @throws IllegalStateException
     *             if there is no transaction in progress
     * @throws InvalidTransactionException
     *             if the transaction id is invalid
     */
    private void checkTransaction(Transaction txn) throws IllegalStateException, InvalidTransactionException
    {
        if (!transactionInProgress.get())
            throw new IllegalStateException("There is no transaction in progress");
        if (txn == null)
            throw new InvalidTransactionException("Invalid transaction; object is null");
        if (!this.txn.getId().equals(txn.getId()))
            throw new InvalidTransactionException("Invalid transaction; object is unknown");
        if (!txn.isActive())
            throw new InvalidTransactionException("Invalid transaction; object is no longer active");
    }

    /**
     * Checks if the given bean is dirty.
     * 
     * @param bean
     *            the bean to check
     * 
     * @return true when the bean is dirty; false otherwise
     */
    // TODO implements isDisty
    private <T> boolean isDirty(final T bean)
    {
        return true;
    }

    /**
     * Opens a concrete {@link DataSource} implementation that matches the given bean type for
     * read-only operations.
     * 
     * @param type
     *            the bean type
     * @param readWrite
     *            the kind of operation
     * 
     * @return a {@link DataSource} implementation object
     * 
     * @throws IllegalArgumentException
     *             of the bean type has no match provider
     */
    // FIXME currently the parameter type is ignored
    private DataSource openDataSource(final Class<?> type, final boolean readWrite) throws IllegalArgumentException
    {
        // FIXME after a transaction commit it can be good to peek the same provider for the next
        // operations to avoid missing records due replication delay between replicas. Let's user
        // settings control this behavior.
        DataProvider provider = transactionInProgress.get() && !transactionEnding.get() ? txnProvider
                : getProvider(readWrite);

        if (provider == null) // Can happen due high concurrency (but should not!)
            provider = getProvider(readWrite);

        final DataSource ds = provider.openDataSource(type);

        activeDataSources.add(ds);

        return ds;
    }

    /**
     * CLoses the given {@link DataSource} object.
     * 
     * @param ds
     *            the {@link DataSource} to be closed
     */
    private void closeDataSource(final DataSource ds)
    {
        activeDataSources.remove(ds);

        ds.close();
    }

    /**
     * Retrieves a {@link DataProvider} instance from the available set.
     * 
     * @param readWrite
     *            set to {@code true} for a read and write provider
     * 
     * @return a instance to be used
     * 
     * @throws IllegalStateException
     *             if a read-write provider is requested but there is no one available
     */
    private DataProvider getProvider(final boolean readWrite) throws IllegalStateException
    {
        if (rwProviders.isEmpty() && roProviders.isEmpty())
            throw new IllegalStateException("No data provider available");

        if (readWrite && rwProviders.isEmpty())
            throw new IllegalStateException("Read-Write data provider not available");

        // FIXME have to be cache friendly (memcached like algorithm based on partition key)
        return readWrite ? rwProviders.get(Fortuna.random().nextInt(rwProviders.size()))
                : roProviders.isEmpty() ? rwProviders.get(Fortuna.random().nextInt(rwProviders.size()))
                        : roProviders.get(Fortuna.random().nextInt(roProviders.size()));
    }
}