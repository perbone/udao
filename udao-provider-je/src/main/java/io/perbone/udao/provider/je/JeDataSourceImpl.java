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

package io.perbone.udao.provider.je;

import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

import io.perbone.toolbox.serialization.Serializer;
import io.perbone.udao.Cursor;
import io.perbone.udao.DataConstraintViolationException;
import io.perbone.udao.KeyViolationException;
import io.perbone.udao.NotEnoughResourceException;
import io.perbone.udao.NotFoundException;
import io.perbone.udao.OperationTimeoutException;
import io.perbone.udao.query.NativeQuery;
import io.perbone.udao.query.Query;
import io.perbone.udao.spi.Cache;
import io.perbone.udao.spi.DataProviderException;
import io.perbone.udao.spi.DataSource;
import io.perbone.udao.spi.internal.AbstractDataSource;
import io.perbone.udao.spi.internal.SimpleCursor;
import io.perbone.udao.transaction.Transaction;
import io.perbone.udao.transaction.TransactionException;
import io.perbone.udao.util.EntityUtils;
import io.perbone.udao.util.StorableInfo;

/**
 * Concrete implementation of {@link DataSource} for JE storage.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
@SuppressWarnings("unchecked")
class JeDataSourceImpl extends AbstractDataSource
{
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String DEFAULT_TARGET_NAME = "nosql";

    private final JeDataProviderImpl provider;

    private final Serializer serializer;

    private final Charset charset = Charset.forName(CHARSET_UTF8);

    private final io.perbone.mkey.Cache secondaryKeyCreatorCache;

    public JeDataSourceImpl(final JeDataProviderImpl provider)
    {
        super();

        this.provider = provider;
        this.serializer = provider.getSerializer();
        this.secondaryKeyCreatorCache = provider.getSecondaryKeyCreatorCache();
    }

    @Override
    public boolean accepts(final Class<?> type) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.accepts(type);
    }

    @Override
    public <T> T create(final Transaction txn, final Cache cache, final T bean)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        if (cache.contains(bean))
            throw new KeyViolationException(MESSAGE_KEY_VIOLATION);

        final Class<?> type = bean.getClass();

        final StorableInfo sinfo = EntityUtils.info(type);

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        final List<String> alternateTableNames = parseAlternateTableNames(DEFAULT_TARGET_NAME, sinfo);

        final Database db = provider.openDatabase(txn, tableName);

        // FIXME should this be in the provider?
        final SecondaryKeyCreator creator = getSecondaryKeyCreator(sinfo);

        try
        {
            final byte[] bytes = serializer.deflate(bean);

            final String skey = EntityUtils.surrogateKeyHash(bean);
            final String pkey = EntityUtils.primaryKeyHash(bean);
            final List<String> akeys = EntityUtils.alternateKeyHashes(bean);

            final DatabaseEntry key = new DatabaseEntry(skey == null ? getBytes(pkey) : getBytes(skey));
            final DatabaseEntry data = new DatabaseEntry(bytes);

            final OperationStatus status = db.putNoOverwrite(getTransaction(txn), key, data);

            if (status == OperationStatus.KEYEXIST)
                throw new KeyViolationException(MESSAGE_KEY_VIOLATION);

            /* Caches it */
            cacheIt(txn, cache, bean);
        }
        catch (final DatabaseException dbe)
        {
            throw new DataProviderException(dbe);
        }
        finally
        {
            provider.closeDatabase(txn, db);
        }

        return bean;
    }

    @Override
    public <T> T create(final Transaction txn, final Cache cache, final T bean, final long ttl, final TimeUnit unit)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.create(txn, cache, bean, ttl, unit);
    }

    @Override
    public <T> List<T> create(final Transaction txn, final Cache cache, final List<T> beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.create(txn, cache, beans);
    }

    @Override
    public <T> List<T> create(final Transaction txn, final Cache cache, final List<T> beans, final long ttl,
            final TimeUnit unit) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.create(txn, cache, beans, ttl, unit);
    }

    @Override
    public <T> T save(final Transaction txn, final Cache cache, final T bean)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.save(txn, cache, bean);
    }

    @Override
    public <T> T save(final Transaction txn, final Cache cache, final T bean, final long ttl, final TimeUnit unit)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.save(txn, cache, bean, ttl, unit);
    }

    @Override
    public <T> List<T> save(final Transaction txn, final Cache cache, final List<T> beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.save(txn, cache, beans);
    }

    @Override
    public <T> List<T> save(final Transaction txn, final Cache cache, final List<T> beans, final long ttl,
            final TimeUnit unit) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.save(txn, cache, beans, ttl, unit);
    }

    @Override
    public <T> T fetchI(final Transaction txn, final Cache cache, final Class<T> type, final Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        T bean = cache.getI(id);

        if (bean != null)
            return bean;

        final StorableInfo sinfo = EntityUtils.info(type);

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        final Database db = provider.openDatabase(txn, tableName);

        try
        {
            final String skey = EntityUtils.surrogateKeyHash(type, id);

            final DatabaseEntry key = new DatabaseEntry(getBytes(skey));
            final DatabaseEntry data = new DatabaseEntry();

            final OperationStatus status = db.get(getTransaction(txn), key, data, LockMode.DEFAULT);

            if (status == OperationStatus.NOTFOUND)
                throw new NotFoundException("The surrogate key did not match any bean");

            // Instantiate and populate a new bean
            bean = serializer.inflate(type, data.getData());

            /* Caches it */
            cacheIt(txn, cache, bean);
        }
        catch (final DatabaseException dbe)
        {
            throw new DataProviderException(dbe);
        }
        finally
        {
            provider.closeDatabase(txn, db);
        }

        return bean;
    }

    @Override
    public <T> List<T> fetchI(final Transaction txn, final Cache cache, final Class<T> type, final Object... ids)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.fetchI(txn, cache, type, ids);
    }

    @Override
    public <T> T fetchP(final Transaction txn, final Cache cache, final Class<T> type, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.fetchP(txn, cache, type, keys);
    }

    @Override
    public <T> T fetchA(final Transaction txn, final Cache cache, final Class<T> type, final String name,
            final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.fetchA(txn, cache, type, name, keys);
    }

    @Override
    public boolean containsI(final Transaction txn, final Cache cache, final Class<?> type, final Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.containsI(txn, cache, type, id);
    }

    @Override
    public boolean containsP(final Transaction txn, final Cache cache, final Class<?> type, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.containsP(txn, cache, type, keys);
    }

    @Override
    public boolean containsA(final Transaction txn, final Cache cache, final Class<?> type, final String name,
            final Object... keys) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.containsA(txn, cache, type, name, keys);
    }

    @Override
    public <T> T updateI(final Transaction txn, final Cache cache, final T bean, final Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.updateI(txn, cache, bean, id);
    }

    @Override
    public <T> T updateI(final Transaction txn, final Cache cache, final T bean, final long ttl, final TimeUnit unit,
            Object id) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, NotFoundException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.updateI(txn, cache, bean, ttl, unit, id);
    }

    @Override
    public <T> T updateP(final Transaction txn, final Cache cache, final T bean, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.updateP(txn, cache, bean, keys);
    }

    @Override
    public <T> T updateP(final Transaction txn, final Cache cache, final T bean, final long ttl, final TimeUnit unit,
            Object... keys) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, NotFoundException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.updateP(txn, cache, bean, ttl, unit, keys);
    }

    @Override
    public <T> T updateA(final Transaction txn, final Cache cache, final T bean, final String name,
            final Object... keys) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, NotFoundException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.updateA(txn, cache, bean, name, keys);
    }

    @Override
    public <T> T updateA(final Transaction txn, final Cache cache, final T bean, final long ttl, final TimeUnit unit,
            final String name, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.updateA(txn, cache, bean, ttl, unit, name, keys);
    }

    @Override
    public <T> T patchI(final Transaction txn, final Cache cache, final T bean, final Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.patchI(txn, cache, bean, id);
    }

    @Override
    public <T> T patchI(final Transaction txn, final Cache cache, final T bean, final long ttl, final TimeUnit unit,
            Object id) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, NotFoundException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.patchI(txn, cache, bean, ttl, unit, id);
    }

    @Override
    public <T> T patchP(final Transaction txn, final Cache cache, final T bean, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.patchP(txn, cache, bean, keys);
    }

    @Override
    public <T> T patchP(final Transaction txn, final Cache cache, final T bean, final long ttl, final TimeUnit unit,
            Object... keys) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, NotFoundException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.patchP(txn, cache, bean, ttl, unit, keys);
    }

    @Override
    public <T> T patchA(final Transaction txn, final Cache cache, final T bean, final String name, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.patchA(txn, cache, bean, name, keys);
    }

    @Override
    public <T> T patchA(final Transaction txn, final Cache cache, final T bean, final long ttl, final TimeUnit unit,
            final String name, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.patchA(txn, cache, bean, ttl, unit, name, keys);
    }

    @Override
    public void touchI(final Transaction txn, final Cache cache, final Class<?> type, final Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException,
            DataProviderException
    {
        // TODO Auto-generated method stub
        super.touchI(txn, cache, type, id);
    }

    @Override
    public void touchI(final Transaction txn, final Cache cache, final Class<?> type, final long ttl,
            final TimeUnit unit, final Object id) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, NotFoundException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        super.touchI(txn, cache, type, ttl, unit, id);
    }

    @Override
    public void touchP(final Transaction txn, final Cache cache, final Class<?> type, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException,
            DataProviderException
    {
        // TODO Auto-generated method stub
        super.touchP(txn, cache, type, keys);
    }

    @Override
    public void touchP(final Transaction txn, final Cache cache, final Class<?> type, final long ttl,
            final TimeUnit unit, final Object... keys) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, NotFoundException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        super.touchP(txn, cache, type, ttl, unit, keys);
    }

    @Override
    public void touchA(final Transaction txn, final Cache cache, final Class<?> type, final String name,
            final Object... keys) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, NotFoundException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        super.touchA(txn, cache, type, name, keys);
    }

    @Override
    public void touchA(final Transaction txn, final Cache cache, final Class<?> type, final long ttl,
            final TimeUnit unit, final String name, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, DataConstraintViolationException, OperationTimeoutException, NotEnoughResourceException,
            DataProviderException
    {
        // TODO Auto-generated method stub
        super.touchA(txn, cache, type, ttl, unit, name, keys);
    }

    @Override
    public void deleteI(final Transaction txn, final Cache cache, final Class<?> type, final Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        super.deleteI(txn, cache, type, id);
    }

    @Override
    public void deleteP(final Transaction txn, final Cache cache, final Class<?> type, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        super.deleteP(txn, cache, type, keys);
    }

    @Override
    public void deleteA(final Transaction txn, final Cache cache, final Class<?> type, final String name,
            final Object... keys) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, NotFoundException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        super.deleteA(txn, cache, type, name, keys);
    }

    @Override
    public void deleteX(final Transaction txn, final Cache cache, final Class<?> type, final Object... beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        super.deleteX(txn, cache, type, beans);
    }

    @Override
    public <T> T removeI(final Transaction txn, final Cache cache, final Class<T> type, final Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.removeI(txn, cache, type, id);
    }

    @Override
    public <T> T removeP(final Transaction txn, final Cache cache, final Class<T> type, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.removeP(txn, cache, type, keys);
    }

    @Override
    public <T> T removeA(final Transaction txn, final Cache cache, final Class<T> type, final String name,
            final Object... keys) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, NotFoundException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.removeA(txn, cache, type, name, keys);
    }

    @Override
    public <T> Cursor<T> cursorI(final Transaction txn, final Cache cache, final Class<T> type)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        Cursor<T> result = null;

        final StorableInfo sinfo = EntityUtils.info(type);

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        final Database db = provider.openDatabase(txn, tableName);

        try
        {
            final List<T> lrs = new ArrayList<T>((int) db.count());

            final DatabaseEntry key = new DatabaseEntry();
            final DatabaseEntry data = new DatabaseEntry();

            com.sleepycat.je.Cursor cursor = db.openCursor(getTransaction(txn), null);

            while (cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS)
            {
                // Instantiate and populate a new bean
                T bean = serializer.inflate(type, data.getData());
                // Caches the new bean
                cacheIt(txn, cache, bean);
                // Adds to the cursor collection
                lrs.add(bean);
            }

            cursor.close();

            T[] resultSet = (T[]) Array.newInstance(type, lrs.size());
            System.arraycopy(lrs.toArray(), 0, resultSet, 0, lrs.size());

            lrs.clear();

            result = new SimpleCursor<T>(resultSet);
        }
        catch (final DatabaseException dbe)
        {
            throw new DataProviderException(dbe);
        }
        finally
        {
            provider.closeDatabase(txn, db);
        }

        return result;
    }

    @Override
    public <T> Cursor<T> cursorP(final Transaction txn, final Cache cache, final Class<T> type)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.cursorP(txn, cache, type);
    }

    @Override
    public <T> Cursor<T> cursorA(final Transaction txn, final Cache cache, final Class<T> type, final String name)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.cursorA(txn, cache, type, name);
    }

    @Override
    public <T> Cursor<T> cursorX(final Transaction txn, final Cache cache, final Class<T> type, final Object... beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.cursorX(txn, cache, type, beans);
    }

    @Override
    public <T> Cursor<T> cursorQ(final Transaction txn, final Cache cache, final Class<T> type, final Query query)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO for now supports only offset and limit
        if (!query.hasOffset() || !query.hasLimit())
            throw new UnsupportedOperationException(
                    MESSAGE_FAIL_UNSUPPORTED_OPERATION + "; missing offset and/or limit");

        Cursor<T> result = null;

        final StorableInfo sinfo = EntityUtils.info(type);

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        final Database db = provider.openDatabase(txn, tableName);

        try
        {
            final List<T> lrs = new ArrayList<T>();

            final DatabaseEntry key = new DatabaseEntry();
            final DatabaseEntry data = new DatabaseEntry();

            com.sleepycat.je.Cursor cursor = db.openCursor(getTransaction(txn), null);

            long count = 1;

            /* Skips records from first to offset */
            while (count < query.offset()
                    && cursor.getNext(key, data, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS)
            {
                count++;
            }

            count = 1;

            while (count <= query.limit() && cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS)
            {
                // Instantiate and populate a new bean
                T bean = serializer.inflate(type, data.getData());
                // Caches the new bean
                cacheIt(txn, cache, bean);
                // Adds to the cursor collection
                lrs.add(bean);

                count++;
            }

            cursor.close();

            T[] resultSet = (T[]) Array.newInstance(type, lrs.size());
            System.arraycopy(lrs.toArray(), 0, resultSet, 0, lrs.size());

            lrs.clear();

            result = new SimpleCursor<T>(resultSet);
        }
        catch (final DatabaseException dbe)
        {
            throw new DataProviderException(dbe);
        }
        finally
        {
            provider.closeDatabase(txn, db);
        }

        return result;
    }

    @Override
    public <T> Cursor<T> cursorN(final Transaction txn, final Cache cache, final Class<T> type, String nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.cursorN(txn, cache, type, nquery);
    }

    @Override
    public <T> Cursor<T> cursorN(final Transaction txn, final Cache cache, final Class<T> type, NativeQuery<T> nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.cursorN(txn, cache, type, nquery);
    }

    @Override
    public long count(final Transaction txn, final Cache cache, final Class<?> type)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        long result;

        final StorableInfo sinfo = EntityUtils.info(type);

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        final Database db = provider.openDatabase(txn, tableName);

        try
        {
            result = db.count();
        }
        catch (final DatabaseException dbe)
        {
            throw new DataProviderException(dbe);
        }
        finally
        {
            provider.closeDatabase(txn, db);
        }

        return result;
    }

    @Override
    public long countX(final Transaction txn, final Cache cache, final Class<?> type, final Object... beans)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.countX(txn, cache, type, beans);
    }

    @Override
    public long countQ(final Transaction txn, final Cache cache, final Class<?> type, final Query query)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.countQ(txn, cache, type, query);
    }

    @Override
    public long countN(final Transaction txn, final Cache cache, final Class<?> type, String nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.countN(txn, cache, type, nquery);
    }

    @Override
    public long countN(final Transaction txn, final Cache cache, final Class<?> type, final NativeQuery<?> nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.countN(txn, cache, type, nquery);
    }

    @Override
    public void expires(Cache cache, final Class<?> type) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        super.expires(cache, type);
    }

    @Override
    public void expires(Cache cache, final Class<?> type, final Object criteria)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        super.expires(cache, type, criteria);
    }

    @Override
    public void invalidate(Cache cache, final Class<?> type)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        super.invalidate(cache, type);
    }

    @Override
    public long prune(Cache cache, final Class<?> type, final Object criteria)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.prune(cache, type, criteria);
    }

    @Override
    public void clear(Cache cache, final Class<?> type) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        super.clear(cache, type);
    }

    @Override
    public void evict() throws UnsupportedOperationException, IllegalStateException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        super.evict();
    }

    @Override
    protected void onOpen()
            throws IllegalStateException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // do nothing
    }

    @Override
    protected void onClose() throws DataProviderException
    {
        // do nothing
    }

    /**
     * Encodes the given {@code String} into a sequence of bytes using the proper charset.
     * 
     * @param value
     *            the string to encode
     * 
     * @return the encoded string into a new byte array
     */
    private byte[] getBytes(final String value)
    {
        return value.getBytes(charset);
    }

    /**
     * Returns the underling JE thansaction object.
     * 
     * @param txn
     *            the udao transaction object
     * 
     * @return the JE transaction object
     */
    private com.sleepycat.je.Transaction getTransaction(final Transaction txn)
    {
        return txn == null ? null : ((JeTransactionImpl) txn).getTransaction();
    }

    /**
     * Parses the alternate table names for the entity type.
     * 
     * @param sinfo
     *            the {@link StorableInfo} for the entity type
     * 
     * @return a list for alternate table names
     */
    private List<String> parseAlternateTableNames(final String target, final StorableInfo sinfo)
    {
        List<String> names = new ArrayList<>();

        final String mainTableName = parseTableName(target, sinfo);

        for (String alternateName : sinfo.alternateKeys().keySet())
        {
            names.add(String.format("%s.%s", mainTableName, alternateName));
        }

        return names;
    }

    /**
     * Caches the given bean into the given cache only if there is no current transaction in
     * progress.
     * 
     * @param cache
     *            the cache instance
     * @param eban
     *            the bean to be cached
     */
    private void cacheIt(final Transaction txn, final Cache cache, final Object bean)
    {
        if (!transactionInProgress(txn))
            cache.add(bean); // Save to cache it
    }

    /**
     * 
     * @param sinfo
     * @return
     */
    private SecondaryKeyCreator getSecondaryKeyCreator(final StorableInfo sinfo)
    {
        SecondaryKeyCreator creator = secondaryKeyCreatorCache.get(sinfo.type());

        if (creator == null)
        {
            creator = new SecondaryKeyCreatorImpl(sinfo, serializer);
            secondaryKeyCreatorCache.put(creator, sinfo.type());
        }

        return creator;
    }

    /**
     * Concrete implementation of {@link SecondaryKeyCreator} for JE storage.
     * 
     * @author Paulo Perbone <pauloperbone@yahoo.com>
     * @since 0.1.0
     */
    static class SecondaryKeyCreatorImpl implements SecondaryKeyCreator
    {
        final StorableInfo sinfo;
        final Serializer serializer;

        public SecondaryKeyCreatorImpl(final StorableInfo sinfo, final Serializer serializer)
        {
            this.sinfo = sinfo;
            this.serializer = serializer;
        }

        @Override
        public boolean createSecondaryKey(final SecondaryDatabase secondary, final DatabaseEntry key,
                final DatabaseEntry data, final DatabaseEntry result)
        {
            // TODO Auto-generated method stub
            return false;
        }
    }
}
