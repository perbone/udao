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

package io.perbone.udao.provider.jdbc;

import static io.perbone.udao.provider.jdbc.SqlDialect.DERBY;
import static io.perbone.udao.provider.jdbc.SqlDialect.MYSQL;
import static io.perbone.udao.provider.jdbc.SqlDialect.ORACLE;
import static io.perbone.udao.provider.jdbc.SqlDialect.POSTGRESQL;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.perbone.toolbox.collection.Pair;
import io.perbone.toolbox.provider.NotEnoughResourceException;
import io.perbone.toolbox.provider.OperationTimeoutException;
import io.perbone.toolbox.validation.StringValidations;
import io.perbone.udao.Cursor;
import io.perbone.udao.DataConstraintViolationException;
import io.perbone.udao.KeyViolationException;
import io.perbone.udao.NotFoundException;
import io.perbone.udao.annotation.DataType;
import io.perbone.udao.query.Expression;
import io.perbone.udao.query.NativeQuery;
import io.perbone.udao.query.Query;
import io.perbone.udao.spi.Cache;
import io.perbone.udao.spi.DataProviderException;
import io.perbone.udao.spi.DataSource;
import io.perbone.udao.spi.internal.AbstractDataSource;
import io.perbone.udao.spi.internal.SimpleCursor;
import io.perbone.udao.transaction.Transaction;
import io.perbone.udao.transaction.TransactionException;
import io.perbone.udao.util.ElementInfo;
import io.perbone.udao.util.EntityUtils;
import io.perbone.udao.util.StorableInfo;

/**
 * Concrete implementation of {@link DataSource} for JDBC storage.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
@SuppressWarnings("unchecked")
public class JdbcDataSourceImpl extends AbstractDataSource
{
    private static final String SQL_SELECT_COUNT = "SELECT COUNT(1) FROM %s";
    private static final String SQL_SELECT_ALL = "SELECT * FROM %s ORDER BY %s";
    private static final String SQL_SELECT_ONE = "SELECT * FROM %s WHERE %s";
    // private static final String SQL_SELECT_EXISTS = "SELECT 1 FROM %s WHERE %s";
    private static final String SQL_SELECT_BY_EXAMPLE = "SELECT * FROM %s WHERE %s ORDER BY %s";
    private static final String SQL_INSERT = "INSERT INTO %s (%s) VALUES (%s)";
    private static final String SQL_UPDATE = "UPDATE %s SET %s WHERE %s";
    private static final String SQL_DELETE = "DELETE FROM %s WHERE %s";

    private static final String DEFAULT_TARGET_NAME = "sql";

    private final JdbcDataProviderImpl provider;

    private final SqlDialect dialect;

    private final Long fetchSize;

    private final Long queryTimeout;

    public JdbcDataSourceImpl(final JdbcDataProviderImpl provider, final SqlDialect dialect, final Long fetchSize,
            final Long queryTimeout)
    {
        super();

        this.provider = provider;
        this.dialect = dialect;
        this.fetchSize = fetchSize;
        this.queryTimeout = queryTimeout;
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

        final Class<T> type = (Class<T>) bean.getClass();

        final StorableInfo sinfo = EntityUtils.info(type);

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        String columns = null;
        String placehoders = null;
        for (final ElementInfo einfo : sinfo.elements())
        {
            if (!einfo.virtual())
            {
                columns = (columns == null ? parseColumnName(einfo) : columns + "," + parseColumnName(einfo));
                placehoders = (placehoders == null ? "?" : placehoders + ",?");
            }
        }

        // FIXME sql statement string should be cached
        final String sql = String.format(SQL_INSERT, tableName, columns, placehoders);

        final Connection conn = getConnection(txn);

        try
        {
            final PreparedStatement pst = conn.prepareStatement(sql);

            setQueryTimeout(pst);

            int parameterIndex = 1;
            for (final ElementInfo einfo : sinfo.elements())
            {
                if (!einfo.virtual())
                {
                    final Object value = EntityUtils.value(bean, einfo.name());
                    if (value == null)
                    {
                        pst.setObject(parameterIndex++, value);
                    }
                    else if (value instanceof TimeUnit)
                    {
                        final String unit = EntityUtils.parseTimeUnit((TimeUnit) value);
                        pst.setObject(parameterIndex++, unit);
                    }
                    else if (value instanceof Enum<?>)
                    {
                        pst.setObject(parameterIndex++, value.toString());
                    }
                    else if (value instanceof Date && einfo.dataType() == DataType.DATE)
                    {
                        Timestamp ts = new Timestamp(((Date) value).getTime());
                        pst.setTimestamp(parameterIndex++, ts);
                    }
                    else if (value instanceof Date && einfo.dataType() == DataType.LONG)
                    {
                        Long tmp = ((Date) value).getTime();
                        pst.setLong(parameterIndex++, tmp);
                    }
                    else
                        pst.setObject(parameterIndex++, value);
                }
            }

            pst.executeUpdate();

            pst.close();
            commit(txn, conn);
        }
        catch (final SQLTimeoutException sqle)
        {
            String msg = "The currently executing 'create' operation is timed out";
            try
            {
                rollback(txn, conn);
            }
            catch (final SQLException e)
            {
                msg = msg + " and could not roll back the transaction; there can be inconsistencies";
                sqle.setNextException(e);
            }
            throw new OperationTimeoutException(msg, sqle);
        }
        catch (final SQLException sqle)
        {
            String msg = sqle.getSQLState().startsWith("23") ? MESSAGE_KEY_VIOLATION
                    : "Could not execute the database statement";
            try
            {
                rollback(txn, conn);
            }
            catch (final SQLException e)
            {
                msg = msg + " and could not roll back the transaction; there can be inconsistencies";
                sqle.setNextException(e);
            }

            if (sqle.getSQLState().startsWith("23"))
                throw new KeyViolationException(msg, sqle);
            else
                throw new DataProviderException(msg, sqle);
        }
        // FIXME handle all exceptions otherwise the transaction state can become out of sync
        finally
        {
            close(txn, conn);
        }

        /* Caches it */
        cacheIt(txn, cache, bean);

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
        for (T bean : beans)
        {
            if (cache.contains(bean))
                throw new KeyViolationException(MESSAGE_FOREIGN_KEY_VIOLATION);
        }

        final Class<?> type = beans.get(0).getClass();

        final StorableInfo sinfo = EntityUtils.info(type);

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        String columns = null;
        String placehoders = null;
        for (final ElementInfo einfo : sinfo.elements())
        {
            if (!einfo.virtual())
            {
                columns = (columns == null ? parseColumnName(einfo) : columns + "," + parseColumnName(einfo));
                placehoders = (placehoders == null ? "?" : placehoders + ",?");
            }
        }

        // FIXME sql statement string should be cached
        final String sql = String.format(SQL_INSERT, tableName, columns, placehoders);

        Connection conn = getConnection(txn);

        try
        {
            final PreparedStatement pst = conn.prepareStatement(sql);

            setQueryTimeout(pst);

            for (T bean : beans)
            {
                int parameterIndex = 1;
                for (final ElementInfo einfo : sinfo.elements())
                {
                    if (!einfo.virtual())
                    {
                        final Object value = EntityUtils.value(bean, einfo.name());
                        if (value == null)
                        {
                            pst.setObject(parameterIndex++, value);
                        }
                        else if (value instanceof TimeUnit)
                        {
                            final String unit = EntityUtils.parseTimeUnit((TimeUnit) value);
                            pst.setObject(parameterIndex++, unit);
                        }
                        else if (value instanceof Enum<?>)
                        {
                            pst.setObject(parameterIndex++, value.toString());
                        }
                        else if (value instanceof Date && einfo.dataType() == DataType.DATE)
                        {
                            Timestamp ts = new Timestamp(((Date) value).getTime());
                            pst.setTimestamp(parameterIndex++, ts);
                        }
                        else if (value instanceof Date && einfo.dataType() == DataType.LONG)
                        {
                            Long tmp = ((Date) value).getTime();
                            pst.setLong(parameterIndex++, tmp);
                        }
                        else
                            pst.setObject(parameterIndex++, value);
                    }
                }
                pst.addBatch();
            }

            pst.executeBatch();

            pst.close();
            commit(txn, conn);
        }
        catch (final SQLTimeoutException sqle)
        {
            String msg = "The currently executing 'create' operation is timed out";
            try
            {
                rollback(txn, conn);
            }
            catch (final SQLException e)
            {
                msg = msg + " and could not roll back the transaction; there can be inconsistencies";
                sqle.setNextException(e);
            }
            throw new OperationTimeoutException(msg, sqle);
        }
        catch (final SQLException sqle)
        {
            String msg = sqle.getSQLState().startsWith("23") ? MESSAGE_KEY_VIOLATION
                    : "Could not execute the database statement";
            try
            {
                rollback(txn, conn);
            }
            catch (final SQLException e)
            {
                msg = msg + " and could not roll back the transaction; there can be inconsistencies";
                sqle.setNextException(e);
            }

            if (sqle.getSQLState().startsWith("23"))
                throw new KeyViolationException(msg, sqle);
            else
                throw new DataProviderException(msg, sqle);
        }
        finally
        {
            close(txn, conn);
        }

        /* Caches it */
        for (T bean : beans)
            cacheIt(txn, cache, bean);

        return beans;
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

        final String where = parseColumnName(sinfo.surrogateKey()) + "=?";

        // FIXME sql statement string should be cached
        final String sql = String.format(SQL_SELECT_ONE, tableName, where);

        final Connection conn = getConnection(txn);

        try
        {
            final PreparedStatement pst = conn.prepareStatement(sql);

            setQueryTimeout(pst);

            // Where column value
            pst.setObject(1, id);

            final ResultSet rs = pst.executeQuery();

            if (!rs.next())
            {
                rs.close();
                pst.close();

                throw new NotFoundException("The surrogate key did not match any bean");
            }

            bean = makeEntity(type, rs); // Instantiate and populate a new bean
            cacheIt(txn, cache, bean); // Caches the new bean

            rs.close();
            pst.close();
        }
        catch (final SQLTimeoutException e)
        {
            throw new OperationTimeoutException("The currently executing 'fetchI' operation is timed out", e);
        }
        catch (final SQLException sqle)
        {
            throw new DataProviderException("Could not execute the database statement", sqle);
        }
        finally
        {
            close(txn, conn);
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
        final StorableInfo sinfo = EntityUtils.info(type);

        T bean = cache.getP(keys);

        if (bean != null)
            return bean;

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        String where = "";
        for (final ElementInfo einfo : sinfo.primaryKey())
        {
            if (StringValidations.isValid(where))
                where += " AND ";
            where = where + parseColumnName(einfo) + "=?";
        }

        // FIXME sql statement string should be cached
        final String sql = String.format(SQL_SELECT_ONE, tableName, where);

        final Connection conn = getConnection(txn);

        try
        {
            final PreparedStatement pst = conn.prepareStatement(sql);

            setQueryTimeout(pst);

            int parameterIndex = 1;
            for (int i = 0; i < keys.length; i++)
            {
                final Object value = keys[i];
                if (value instanceof TimeUnit)
                {
                    final String unit = EntityUtils.parseTimeUnit((TimeUnit) value);
                    pst.setObject(parameterIndex++, unit);
                }
                else if (value instanceof Enum<?>)
                {
                    pst.setObject(parameterIndex++, value.toString());
                }
                else if (value instanceof Long && sinfo.primaryKey().get(i).dataType() == DataType.DATE)
                {
                    final Date dt = new Date((Long) value);
                    pst.setObject(parameterIndex++, dt);
                }
                else if (value instanceof Date && sinfo.primaryKey().get(i).dataType() == DataType.DATE)
                {
                    final Timestamp ts = new Timestamp(((Date) value).getTime());
                    pst.setTimestamp(parameterIndex++, ts);
                }
                else if (value instanceof Date && sinfo.primaryKey().get(i).dataType() == DataType.LONG)
                {
                    final Long tmp = ((Date) value).getTime();
                    pst.setLong(parameterIndex++, tmp);
                }
                else
                    pst.setObject(parameterIndex++, value);
            }

            final ResultSet rs = pst.executeQuery();

            if (!rs.next())
            {
                rs.close();
                pst.close();

                throw new NotFoundException("The primary key did not match any bean");
            }

            bean = makeEntity(type, rs); // Instantiate and populate a new bean
            cacheIt(txn, cache, bean); // Caches the new bean

            rs.close();
            pst.close();
        }
        catch (final SQLTimeoutException e)
        {
            throw new OperationTimeoutException("The currently executing 'fetchP' operation is timed out", e);
        }
        catch (final SQLException sqle)
        {
            throw new DataProviderException("Could not execute the database statement", sqle);
        }
        finally
        {
            close(txn, conn);
        }

        return bean;
    }

    @Override
    public <T> T fetchA(final Transaction txn, final Cache cache, final Class<T> type, final String name,
            final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        final StorableInfo sinfo = EntityUtils.info(type);

        T bean = cache.getA(name, keys);

        if (bean != null)
            return bean;

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        String where = "";
        for (final ElementInfo einfo : sinfo.alternateKey(name))
        {
            if (StringValidations.isValid(where))
                where += " AND ";
            where = where + parseColumnName(einfo) + "=?";
        }

        // FIXME sql statement string should be cached
        final String sql = String.format(SQL_SELECT_ONE, tableName, where);

        final Connection conn = getConnection(txn);

        try
        {
            final PreparedStatement pst = conn.prepareStatement(sql);

            setQueryTimeout(pst);

            int parameterIndex = 1;
            for (int i = 0; i < keys.length; i++)
            {
                final Object value = keys[i];
                if (value instanceof TimeUnit)
                {
                    final String unit = EntityUtils.parseTimeUnit((TimeUnit) value);
                    pst.setObject(parameterIndex++, unit);
                }
                else if (value instanceof Enum<?>)
                {
                    pst.setObject(parameterIndex++, value.toString());
                }
                else if (value instanceof Long && sinfo.alternateKey(name).get(i).dataType() == DataType.DATE)
                {
                    final Date dt = new Date((Long) value);
                    pst.setObject(parameterIndex++, dt);
                }
                else if (value instanceof Date && sinfo.alternateKey(name).get(i).dataType() == DataType.DATE)
                {
                    final Timestamp ts = new Timestamp(((Date) value).getTime());
                    pst.setTimestamp(parameterIndex++, ts);
                }
                else if (value instanceof Date && sinfo.alternateKey(name).get(i).dataType() == DataType.LONG)
                {
                    final Long tmp = ((Date) value).getTime();
                    pst.setLong(parameterIndex++, tmp);
                }
                else
                    pst.setObject(parameterIndex++, value);
            }

            final ResultSet rs = pst.executeQuery();

            if (!rs.next())
            {
                rs.close();
                pst.close();

                throw new NotFoundException("The alternate key did not match any bean");
            }

            bean = makeEntity(type, rs); // Instantiate and populate a new bean
            cacheIt(txn, cache, bean); // Caches the new bean

            rs.close();
            pst.close();
        }
        catch (final SQLTimeoutException e)
        {
            throw new OperationTimeoutException("The currently executing 'fetchA' operation is timed out", e);
        }
        catch (final SQLException sqle)
        {
            throw new DataProviderException("Could not execute the database statement", sqle);
        }
        finally
        {
            close(txn, conn);
        }

        return bean;
    }

    @Override
    public boolean containsI(final Transaction txn, final Cache cache, final Class<?> type, final Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        if (cache.containsI(id))
            return true;

        // FIXME should use SQL_SELECT_EXISTS
        try
        {
            fetchI(txn, cache, type, id);
        }
        catch (final NotFoundException e)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean containsP(final Transaction txn, final Cache cache, final Class<?> type, final Object... keys)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        if (cache.containsP(keys))
            return true;

        // FIXME should use SQL_SELECT_EXISTS
        try
        {
            fetchP(txn, cache, type, keys);
        }
        catch (final NotFoundException e)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean containsA(final Transaction txn, final Cache cache, final Class<?> type, final String name,
            final Object... keys) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        if (cache.containsA(name, keys))
            return true;

        // FIXME should use SQL_SELECT_EXISTS
        try
        {
            fetchA(txn, cache, type, name, keys);
        }
        catch (final NotFoundException e)
        {
            return false;
        }

        return true;
    }

    @Override
    public <T> T updateI(final Transaction txn, final Cache cache, final T bean, final Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        final Class<T> type = (Class<T>) bean.getClass();

        final StorableInfo sinfo = EntityUtils.info(type);

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        String setColumns = "";
        for (final ElementInfo einfo : sinfo.elements())
        {
            if (!einfo.virtual()
                    && (!einfo.metadata() || (einfo.metadata() && EntityUtils.value(bean, einfo.name()) != null)))
            {
                if (StringValidations.isValid(setColumns))
                    setColumns = setColumns + ", ";
                setColumns = setColumns + parseColumnName(einfo) + "=?";
            }
        }

        final String where = parseColumnName(sinfo.surrogateKey()) + "=?";

        // FIXME sql statement string should be cached
        final String sql = String.format(SQL_UPDATE, tableName, setColumns, where);

        final Connection conn = getConnection(txn);

        try
        {
            final PreparedStatement pst = conn.prepareStatement(sql);

            setQueryTimeout(pst);

            // Columns values
            int parameterIndex = 1;
            for (final ElementInfo einfo : sinfo.elements())
            {
                if (!einfo.virtual()
                        && (!einfo.metadata() || (einfo.metadata() && EntityUtils.value(bean, einfo.name()) != null)))
                {
                    final Object value = EntityUtils.value(bean, einfo.name());
                    if (value == null)
                    {
                        pst.setObject(parameterIndex++, value);
                    }
                    else if (value instanceof TimeUnit)
                    {
                        final String unit = EntityUtils.parseTimeUnit((TimeUnit) value);
                        pst.setObject(parameterIndex++, unit);
                    }
                    else if (value instanceof Enum<?>)
                    {
                        pst.setObject(parameterIndex++, value.toString());
                    }
                    else if (value instanceof Date && einfo.dataType() == DataType.DATE)
                    {
                        final Timestamp ts = new Timestamp(((Date) value).getTime());
                        pst.setTimestamp(parameterIndex++, ts);
                    }
                    else if (value instanceof Date && einfo.dataType() == DataType.LONG)
                    {
                        final Long tmp = ((Date) value).getTime();
                        pst.setLong(parameterIndex++, tmp);
                    }
                    else
                        pst.setObject(parameterIndex++, value);
                }
            }

            // Where column value
            pst.setObject(parameterIndex++, id);

            final int affectedRows = pst.executeUpdate();

            pst.close();
            commit(txn, conn);

            if (affectedRows == 0)
                throw new NotFoundException("The surrogate key did not match any bean");

            cache.removeI(id); // Clear this (potentially dirty) bean from cache
        }
        catch (final SQLTimeoutException sqle)
        {
            String msg = "The currently executing 'updateI' operation is timed out";
            try
            {
                rollback(txn, conn);
            }
            catch (final SQLException e)
            {
                msg = msg + " and could not roll back the transaction; there can be inconsistencies";
                sqle.setNextException(e);
            }
            throw new OperationTimeoutException(msg, sqle);
        }
        catch (final SQLException sqle)
        {
            // FIXME support sql state 23502 (not null constraint fails)

            String msg = sqle.getSQLState().startsWith("23") ? MESSAGE_KEY_VIOLATION
                    : "Could not execute the database statement";
            try
            {
                rollback(txn, conn);
            }
            catch (final SQLException e)
            {
                msg = msg + " and could not roll back the transaction; there can be inconsistencies";
                sqle.setNextException(e);
            }

            if (sqle.getSQLState().equals("23502")) // NOT NULL FAIL
                throw new DataConstraintViolationException(msg, sqle);
            else if (sqle.getSQLState().startsWith("23"))
                throw new KeyViolationException(msg, sqle);
            else
                throw new DataProviderException(msg, sqle);
        }
        finally
        {
            close(txn, conn);
        }

        return bean;
    }

    @Override
    public <T> T updateI(final Transaction txn, final Cache cache, final T bean, final long ttl, final TimeUnit unit,
            final Object id) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
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
        final Class<T> type = (Class<T>) bean.getClass();

        final StorableInfo sinfo = EntityUtils.info(type);

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        String setColumns = "";
        for (final ElementInfo einfo : sinfo.elements())
        {
            if (!einfo.virtual()
                    && (!einfo.metadata() || (einfo.metadata() && EntityUtils.value(bean, einfo.name()) != null)))
            {
                if (StringValidations.isValid(setColumns))
                    setColumns = setColumns + ", ";
                setColumns = setColumns + parseColumnName(einfo) + "=?";
            }
        }

        String where = "";
        for (final ElementInfo einfo : sinfo.primaryKey())
        {
            if (StringValidations.isValid(where))
                where += " AND ";
            where = where + parseColumnName(einfo) + "=?";
        }

        // FIXME sql statement string should be cached
        final String sql = String.format(SQL_UPDATE, tableName, setColumns, where);

        final Connection conn = getConnection(txn);

        try
        {
            final PreparedStatement pst = conn.prepareStatement(sql);

            setQueryTimeout(pst);

            // Columns values
            int parameterIndex = 1;
            for (final ElementInfo einfo : sinfo.elements())
            {
                if (!einfo.virtual()
                        && (!einfo.metadata() || (einfo.metadata() && EntityUtils.value(bean, einfo.name()) != null)))
                {
                    final Object value = EntityUtils.value(bean, einfo.name());
                    if (value == null)
                    {
                        pst.setObject(parameterIndex++, value);
                    }
                    else if (value instanceof TimeUnit)
                    {
                        final String unit = EntityUtils.parseTimeUnit((TimeUnit) value);
                        pst.setObject(parameterIndex++, unit);
                    }
                    else if (value instanceof Enum<?>)
                    {
                        pst.setObject(parameterIndex++, value.toString());
                    }
                    else if (value instanceof Date && einfo.dataType() == DataType.DATE)
                    {
                        final Timestamp ts = new Timestamp(((Date) value).getTime());
                        pst.setTimestamp(parameterIndex++, ts);
                    }
                    else if (value instanceof Date && einfo.dataType() == DataType.LONG)
                    {
                        final Long tmp = ((Date) value).getTime();
                        pst.setLong(parameterIndex++, tmp);
                    }
                    else
                        pst.setObject(parameterIndex++, value);
                }
            }

            // Where columns values
            for (int i = 0; i < keys.length; i++)
            {
                final Object value = keys[i];
                if (value instanceof Enum<?>)
                    pst.setObject(parameterIndex++, value.toString());
                else
                    pst.setObject(parameterIndex++, value);
            }

            final int affectedRows = pst.executeUpdate();

            pst.close();
            commit(txn, conn);

            if (affectedRows == 0)
                throw new NotFoundException("The primary key did not match any bean");

            cache.removeP(keys); // Clear this (potentially dirty) bean from cache
        }
        catch (final SQLTimeoutException sqle)
        {
            String msg = "The currently executing 'updateP' operation is timed out";
            try
            {
                rollback(txn, conn);
            }
            catch (final SQLException e)
            {
                msg = msg + " and could not roll back the transaction; there can be inconsistencies";
                sqle.setNextException(e);
            }
            throw new OperationTimeoutException(msg, sqle);
        }
        catch (final SQLException sqle)
        {
            // FIXME support sql state 23502 (not null constraint fails)

            String msg = sqle.getSQLState().startsWith("23") ? MESSAGE_KEY_VIOLATION
                    : "Could not execute the database statement";
            try
            {
                rollback(txn, conn);
            }
            catch (final SQLException e)
            {
                msg = msg + " and could not roll back the transaction; there can be inconsistencies";
                sqle.setNextException(e);
            }

            if (sqle.getSQLState().equals("23502")) // NOT NULL FAIL
                throw new DataConstraintViolationException(msg, sqle);
            else if (sqle.getSQLState().startsWith("23"))
                throw new KeyViolationException(msg, sqle);
            else
                throw new DataProviderException(msg, sqle);
        }
        finally
        {
            close(txn, conn);
        }

        return bean;
    }

    @Override
    public <T> T updateP(final Transaction txn, final Cache cache, final T bean, final long ttl, final TimeUnit unit,
            final Object... keys) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
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
        Class<?> type = bean.getClass();

        StorableInfo sinfo = EntityUtils.info(type);

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        String setColumns = "";
        for (final ElementInfo einfo : sinfo.elements())
        {
            if (!einfo.virtual()
                    && (!einfo.metadata() || (einfo.metadata() && EntityUtils.value(bean, einfo.name()) != null)))
            {
                if (StringValidations.isValid(setColumns))
                    setColumns = setColumns + ", ";
                setColumns = setColumns + parseColumnName(einfo) + "=?";
            }
        }

        String where = "";
        for (final ElementInfo einfo : sinfo.alternateKey(name))
        {
            if (StringValidations.isValid(where))
                where += " AND ";
            where = where + parseColumnName(einfo) + "=?";
        }

        // FIXME sql statement string should be cached
        final String sql = String.format(SQL_UPDATE, tableName, setColumns, where);

        Connection conn = getConnection(txn);

        try
        {
            PreparedStatement pst = conn.prepareStatement(sql);

            setQueryTimeout(pst);

            // Columns values
            int parameterIndex = 1;
            for (final ElementInfo einfo : sinfo.elements())
            {
                if (!einfo.virtual()
                        && (!einfo.metadata() || (einfo.metadata() && EntityUtils.value(bean, einfo.name()) != null)))
                {
                    final Object value = EntityUtils.value(bean, einfo.name());
                    if (value == null)
                    {
                        pst.setObject(parameterIndex++, value);
                    }
                    else if (value instanceof TimeUnit)
                    {
                        final String unit = EntityUtils.parseTimeUnit((TimeUnit) value);
                        pst.setObject(parameterIndex++, unit);
                    }
                    else if (value instanceof Enum<?>)
                    {
                        pst.setObject(parameterIndex++, value.toString());
                    }
                    else if (value instanceof Date && einfo.dataType() == DataType.DATE)
                    {
                        final Timestamp ts = new Timestamp(((Date) value).getTime());
                        pst.setTimestamp(parameterIndex++, ts);
                    }
                    else if (value instanceof Date && einfo.dataType() == DataType.LONG)
                    {
                        final Long tmp = ((Date) value).getTime();
                        pst.setLong(parameterIndex++, tmp);
                    }
                    else
                        pst.setObject(parameterIndex++, value);
                }
            }

            // Where column value
            for (int i = 0; i < keys.length; i++)
            {
                final Object value = keys[i];
                if (value instanceof Enum<?>)
                    pst.setObject(parameterIndex++, value.toString());
                else
                    pst.setObject(parameterIndex++, value);
            }

            final int affectedRows = pst.executeUpdate();

            pst.close();
            pst = null;
            commit(txn, conn);

            if (affectedRows == 0)
                throw new NotFoundException("The alternate key did not match any bean");

            cache.removeA(name, keys); // Clears the cache for this bean
        }
        catch (final SQLTimeoutException sqle)
        {
            String msg = "The currently executing 'updateA' operation is timed out";
            try
            {
                rollback(txn, conn);
            }
            catch (final SQLException e)
            {
                msg = msg + " and could not roll back the transaction; there can be inconsistencies";
                sqle.setNextException(e);
            }
            throw new OperationTimeoutException(msg, sqle);
        }
        catch (final SQLException sqle)
        {
            // FIXME support sql state 23502 (not null constraint fails)

            String msg = sqle.getSQLState().startsWith("23") ? MESSAGE_KEY_VIOLATION
                    : "Could not execute the database statement";
            try
            {
                rollback(txn, conn);
            }
            catch (final SQLException e)
            {
                msg = msg + " and could not roll back the transaction; there can be inconsistencies";
                sqle.setNextException(e);
            }

            if (sqle.getSQLState().equals("23502")) // NOT NULL FAIL
                throw new DataConstraintViolationException(msg, sqle);
            else if (sqle.getSQLState().startsWith("23"))
                throw new KeyViolationException(msg, sqle);
            else
                throw new DataProviderException(msg, sqle);
        }
        finally
        {
            close(txn, conn);
        }

        return bean;
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
    public <T> T patchI(Transaction txn, final Cache cache, final T bean, final Object id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException
    {
        final Class<T> type = (Class<T>) bean.getClass();

        final StorableInfo sinfo = EntityUtils.info(type);

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        String setColumns = "";
        for (final ElementInfo einfo : sinfo.elements())
        {
            if (!einfo.virtual() && EntityUtils.value(bean, einfo.name()) != null)
            {
                if (StringValidations.isValid(setColumns))
                    setColumns = setColumns + ", ";
                setColumns = setColumns + parseColumnName(einfo) + "=?";
            }
        }

        final String where = parseColumnName(sinfo.surrogateKey()) + "=?";

        // FIXME sql statement string should be cached
        final String sql = String.format(SQL_UPDATE, tableName, setColumns, where);

        final Connection conn = getConnection(txn);

        try
        {
            final PreparedStatement pst = conn.prepareStatement(sql);

            setQueryTimeout(pst);

            // Columns values
            int parameterIndex = 1;
            for (final ElementInfo einfo : sinfo.elements())
            {
                if (!einfo.virtual())
                {
                    final Object value = EntityUtils.value(bean, einfo.name());
                    if (value == null)
                    {
                        // do nothing (patch behavior)
                    }
                    else if (value instanceof TimeUnit)
                    {
                        final String unit = EntityUtils.parseTimeUnit((TimeUnit) value);
                        pst.setObject(parameterIndex++, unit);
                    }
                    else if (value instanceof Enum<?>)
                    {
                        pst.setObject(parameterIndex++, value.toString());
                    }
                    else if (value instanceof Date && einfo.dataType() == DataType.DATE)
                    {
                        final Timestamp ts = new Timestamp(((Date) value).getTime());
                        pst.setTimestamp(parameterIndex++, ts);
                    }
                    else if (value instanceof Date && einfo.dataType() == DataType.LONG)
                    {
                        final Long tmp = ((Date) value).getTime();
                        pst.setLong(parameterIndex++, tmp);
                    }
                    else
                        pst.setObject(parameterIndex++, value);
                }
            }

            // Where column value
            pst.setObject(parameterIndex++, id);

            final int affectedRows = pst.executeUpdate();

            pst.close();
            commit(txn, conn);

            if (affectedRows == 0)
                throw new NotFoundException("The surrogate key did not match any bean");

            cache.removeI(id); // Clear this (potentially dirty) bean from cache
        }
        catch (final SQLTimeoutException sqle)
        {
            String msg = "The currently executing 'updateI' operation is timed out";
            try
            {
                rollback(txn, conn);
            }
            catch (final SQLException e)
            {
                msg = msg + " and could not roll back the transaction; there can be inconsistencies";
                sqle.setNextException(e);
            }
            throw new OperationTimeoutException(msg, sqle);
        }
        catch (final SQLException sqle)
        {
            // FIXME support sql state 23502 (not null constraint fails)

            String msg = sqle.getSQLState().startsWith("23") ? MESSAGE_KEY_VIOLATION
                    : "Could not execute the database statement";
            try
            {
                rollback(txn, conn);
            }
            catch (final SQLException e)
            {
                msg = msg + " and could not roll back the transaction; there can be inconsistencies";
                sqle.setNextException(e);
            }

            if (sqle.getSQLState().equals("23502")) // NOT NULL FAIL
                throw new DataConstraintViolationException(msg, sqle);
            else if (sqle.getSQLState().startsWith("23"))
                throw new KeyViolationException(msg, sqle);
            else
                throw new DataProviderException(msg, sqle);
        }
        finally
        {
            close(txn, conn);
        }

        return bean;
    }

    @Override
    public <T> T patchI(final Transaction txn, final Cache cache, final T bean, final long ttl, final TimeUnit unit,
            final Object id) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
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
        final Class<T> type = (Class<T>) bean.getClass();

        final StorableInfo sinfo = EntityUtils.info(type);

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        String setColumns = "";
        for (final ElementInfo einfo : sinfo.elements())
        {
            if (!einfo.virtual() && EntityUtils.value(bean, einfo.name()) != null)
            {
                if (StringValidations.isValid(setColumns))
                    setColumns = setColumns + ", ";
                setColumns = setColumns + parseColumnName(einfo) + "=?";
            }
        }

        String where = "";
        for (final ElementInfo einfo : sinfo.primaryKey())
        {
            if (StringValidations.isValid(where))
                where += " AND ";
            where = where + parseColumnName(einfo) + "=?";
        }

        // FIXME sql statement string should be cached
        final String sql = String.format(SQL_UPDATE, tableName, setColumns, where);

        final Connection conn = getConnection(txn);

        try
        {
            final PreparedStatement pst = conn.prepareStatement(sql);

            setQueryTimeout(pst);

            // Columns values
            int parameterIndex = 1;
            for (final ElementInfo einfo : sinfo.elements())
            {
                if (!einfo.virtual())
                {
                    final Object value = EntityUtils.value(bean, einfo.name());
                    if (value == null)
                    {
                        // do nothing (patch behavior)
                    }
                    else if (value instanceof TimeUnit)
                    {
                        final String unit = EntityUtils.parseTimeUnit((TimeUnit) value);
                        pst.setObject(parameterIndex++, unit);
                    }
                    else if (value instanceof Enum<?>)
                    {
                        pst.setObject(parameterIndex++, value.toString());
                    }
                    else if (value instanceof Date && einfo.dataType() == DataType.DATE)
                    {
                        final Timestamp ts = new Timestamp(((Date) value).getTime());
                        pst.setTimestamp(parameterIndex++, ts);
                    }
                    else if (value instanceof Date && einfo.dataType() == DataType.LONG)
                    {
                        final Long tmp = ((Date) value).getTime();
                        pst.setLong(parameterIndex++, tmp);
                    }
                    else
                        pst.setObject(parameterIndex++, value);
                }
            }

            // Where columns values
            for (int i = 0; i < keys.length; i++)
            {
                final Object value = keys[i];
                if (value instanceof Enum<?>)
                    pst.setObject(parameterIndex++, value.toString());
                else
                    pst.setObject(parameterIndex++, value);
            }

            final int affectedRows = pst.executeUpdate();

            pst.close();
            commit(txn, conn);

            if (affectedRows == 0)
                throw new NotFoundException("The primary key did not match any bean");

            cache.removeP(keys); // Clear this (potentially dirty) bean from cache
        }
        catch (final SQLTimeoutException sqle)
        {
            String msg = "The currently executing 'updateP' operation is timed out";
            try
            {
                rollback(txn, conn);
            }
            catch (final SQLException e)
            {
                msg = msg + " and could not roll back the transaction; there can be inconsistencies";
                sqle.setNextException(e);
            }
            throw new OperationTimeoutException(msg, sqle);
        }
        catch (final SQLException sqle)
        {
            // FIXME support sql state 23502 (not null constraint fails)

            String msg = sqle.getSQLState().startsWith("23") ? MESSAGE_KEY_VIOLATION
                    : "Could not execute the database statement";
            try
            {
                rollback(txn, conn);
            }
            catch (final SQLException e)
            {
                msg = msg + " and could not roll back the transaction; there can be inconsistencies";
                sqle.setNextException(e);
            }

            if (sqle.getSQLState().equals("23502")) // NOT NULL FAIL
                throw new DataConstraintViolationException(msg, sqle);
            else if (sqle.getSQLState().startsWith("23"))
                throw new KeyViolationException(msg, sqle);
            else
                throw new DataProviderException(msg, sqle);
        }
        finally
        {
            close(txn, conn);
        }

        return bean;
    }

    @Override
    public <T> T patchP(final Transaction txn, final Cache cache, final T bean, final long ttl, final TimeUnit unit,
            final Object... keys) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
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
            final String name, Object... keys)
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
        final StorableInfo sinfo = EntityUtils.info(type);

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        final String where = parseColumnName(sinfo.surrogateKey()) + "=?";

        // FIXME sql statement string should be cached
        final String sql = String.format(SQL_DELETE, tableName, where);

        final Connection conn = getConnection(txn);

        try
        {
            final PreparedStatement pst = conn.prepareStatement(sql);

            setQueryTimeout(pst);

            // Where column value
            pst.setObject(1, id);

            final int affectedRows = pst.executeUpdate();

            pst.close();
            commit(txn, conn);

            if (affectedRows == 0)
                throw new NotFoundException("The surrogate key did not match any bean");
        }
        catch (final SQLTimeoutException sqle)
        {
            String msg = "The currently executing 'deleteI' operation is timed out";
            try
            {
                rollback(txn, conn);
            }
            catch (final SQLException e)
            {
                msg = msg + " and could not roll back the transaction; there can be inconsistencies";
                sqle.setNextException(e);
            }
            throw new OperationTimeoutException(msg, sqle);
        }
        catch (final SQLException sqle)
        {
            String msg = sqle.getSQLState().startsWith("23") ? MESSAGE_FOREIGN_KEY_VIOLATION
                    : "Could not execute the database statement";
            try
            {
                rollback(txn, conn);
            }
            catch (final SQLException e)
            {
                msg = msg + " and could not roll back the transaction; there can be inconsistencies";
                sqle.setNextException(e);
            }

            if (sqle.getSQLState().startsWith("23"))
                throw new KeyViolationException(msg, sqle);
            else
                throw new DataProviderException(msg, sqle);
        }
        finally
        {
            close(txn, conn);
        }

        /* Deletes from cache */
        cache.deleteI(id);
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
        final StorableInfo sinfo = EntityUtils.info(type);

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        final Map<String, List<Object>> whereValues = parseWhereValuesByExample(type, beans);

        final String where = parseWhereStatement(whereValues);

        // FIXME sql statement string should be cached
        final String sql = String.format(SQL_DELETE, tableName, where);

        final Connection conn = getConnection(txn);

        try
        {
            final PreparedStatement pst = conn.prepareStatement(sql);

            setQueryTimeout(pst);

            int parameterIndex = 1;
            for (List<Object> values : whereValues.values())
            {
                for (Object value : values)
                {
                    if (value instanceof Enum<?>)
                        pst.setObject(parameterIndex++, value.toString());
                    else
                        pst.setObject(parameterIndex++, value);
                }
            }

            final int affectedRows = pst.executeUpdate();

            pst.close();
            commit(txn, conn);

            if (affectedRows == 0)
                throw new NotFoundException("The surrogate keys did not match any bean");

            /* Delete from cache */
            cache.invalidate(); // FIXME do not invalidate all cache for the bean type
        }
        catch (final SQLTimeoutException sqle)
        {
            String msg = "The currently executing 'deleteX' operation is timed out";
            try
            {
                rollback(txn, conn);
            }
            catch (final SQLException e)
            {
                msg = msg + " and could not roll back the transaction; there can be inconsistencies";
                sqle.setNextException(e);
            }
            throw new OperationTimeoutException(msg, sqle);
        }
        catch (final SQLException sqle)
        {
            String msg = sqle.getSQLState().startsWith("23") ? MESSAGE_FOREIGN_KEY_VIOLATION
                    : "Could not execute the database statement";
            try
            {
                rollback(txn, conn);
            }
            catch (final SQLException e)
            {
                msg = msg + " and could not roll back the transaction; there can be inconsistencies";
                sqle.setNextException(e);
            }

            if (sqle.getSQLState().startsWith("23"))
                throw new KeyViolationException(msg, sqle);
            else
                throw new DataProviderException(msg, sqle);
        }
        finally
        {
            close(txn, conn);
        }
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
        final StorableInfo sinfo = EntityUtils.info(type);

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        final String orderBy = parseColumnName(sinfo.surrogateKey());

        // FIXME sql statement string should be cached
        final String sql = String.format(SQL_SELECT_ALL, tableName, orderBy);

        return openCursor(txn, cache, type, sql);
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
        final StorableInfo sinfo = EntityUtils.info(type);

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        final Map<String, List<Object>> whereValues = parseWhereValuesByExample(type, beans);

        final String where = parseWhereStatement(whereValues);

        final String orderBy = parseOrderByStatement(sinfo);

        // FIXME sql statement string should be cached
        final String sql = String.format(SQL_SELECT_BY_EXAMPLE, tableName, where, orderBy);

        final List<Object> values = new ArrayList<Object>();

        for (List<Object> wvalues : whereValues.values())
        {
            for (Object value : wvalues)
                values.add(value);
        }

        return openCursor(txn, cache, type, sql, values);
    }

    @Override
    public <T> Cursor<T> cursorQ(final Transaction txn, final Cache cache, final Class<T> type, final Query query)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        checkDialectSupport(DERBY, MYSQL, ORACLE, POSTGRESQL);

        final StorableInfo sinfo = EntityUtils.info(type);

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        final String where = parseQueryWhere(type, query);

        final String limit = parseQueryLimit(query);

        final String order = parseQueryOrder(type, query);

        // FIXME sql statement string should be cached
        final String sql = parseQuerySelect("SELECT * FROM " + tableName, where, order, limit);

        return openCursor(txn, cache, type, sql);
    }

    @Override
    public <T> Cursor<T> cursorN(final Transaction txn, final Cache cache, final Class<T> type, final String nquery)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.cursorN(txn, cache, type, nquery);
    }

    @Override
    public <T> Cursor<T> cursorN(final Transaction txn, final Cache cache, final Class<T> type,
            final NativeQuery<T> nquery)
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
        StorableInfo sinfo = EntityUtils.info(type);

        final String tableName = parseTableName(DEFAULT_TARGET_NAME, sinfo);

        // FIXME sql statement string should be cached
        final String sql = String.format(SQL_SELECT_COUNT, tableName);

        long count = -1;

        Connection conn = getConnection(txn);

        try
        {
            final PreparedStatement pst = conn.prepareStatement(sql);

            setQueryTimeout(pst);

            final ResultSet rs = pst.executeQuery();

            if (rs.next())
                count = rs.getLong(1);

            rs.close();
            pst.close();
        }
        catch (final SQLTimeoutException e)
        {
            throw new OperationTimeoutException("The currently executing 'count' operation is timed out", e);
        }
        catch (final SQLException sqle)
        {
            throw new DataProviderException("Could not execute the database statement", sqle);
        }
        finally
        {
            close(txn, conn);
        }

        return count;
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
    public long countN(final Transaction txn, final Cache cache, Class<?> type, final String nquery)
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
    public void expires(final Cache cache, final Class<?> type)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        super.expires(cache, type);
    }

    @Override
    public void expires(final Cache cache, final Class<?> type, final Object criteria)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        super.expires(cache, type, criteria);
    }

    @Override
    public void invalidate(final Cache cache, final Class<?> type)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        cache.invalidate();
    }

    @Override
    public long prune(final Cache cache, final Class<?> type, final Object criteria)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
        return super.prune(cache, type, criteria);
    }

    @Override
    public void clear(final Cache cache, final Class<?> type) throws UnsupportedOperationException,
            IllegalStateException, IllegalArgumentException, KeyViolationException, DataConstraintViolationException,
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
     * Checks for the given set of dialects with they are supported by this implementation.
     * 
     * @param dialects
     *            the set of supported dialects
     * 
     * @throws UnsupportedOperationException
     *             if the dialect is not supported
     */
    private void checkDialectSupport(final SqlDialect... dialects) throws UnsupportedOperationException
    {
        boolean supported = false;

        for (SqlDialect d : dialects)
            if (d == dialect)
                supported = true;

        if (!supported)
            throw new UnsupportedOperationException(
                    String.format("This method call is not yet supported for %s dialect", dialect));
    }

    /**
     * Parses the where statement.
     * 
     * @param whereValues
     *            the columns and values for the where clause
     * 
     * @return the where statement with value place holders
     */
    private String parseWhereStatement(final Map<String, List<Object>> whereValues)
    {
        String where = "";

        for (final String name : whereValues.keySet())
        {
            boolean useInOperator = false;

            // Optimisation for IN clauses as it is much more expensive for the DBMS than an '='
            if (whereValues.get(name).size() > 1)
                useInOperator = true;

            if (StringValidations.isValid(where))
                where += " AND " + name + (useInOperator ? " IN (" : " = ");
            else
                where = name + (useInOperator ? " IN (" : " = ");

            for (int i = 0; i < whereValues.get(name).size(); i++)
            {
                if (useInOperator)
                {
                    boolean first = !"?".equals(where.substring(where.length() - 1));
                    if (first)
                        where += "?";
                    else
                        where += ",?";
                }
                else
                {
                    where += "?";
                }
            }

            if (useInOperator)
                where += ")";
        }

        return where;
    }

    /**
     * Parses the natural order for the bean type.
     * <p>
     * It'll try surrogate key first and them primary key.
     * 
     * @param sinfo
     *            the bean storable info object
     * 
     * @return a SQL string with all the collumn names for use with the ORDER BY command
     */
    private String parseOrderByStatement(final StorableInfo sinfo)
    {
        String orderBy = "";

        final List<ElementInfo> elements = new ArrayList<ElementInfo>();

        if (sinfo.surrogateKey() != null)
            elements.add(sinfo.surrogateKey());
        else
            for (final ElementInfo einfo : sinfo.primaryKey())
                elements.add(einfo);

        for (final ElementInfo einfo : elements)
        {
            if (StringValidations.isValid(orderBy))
                orderBy = orderBy + ", ";
            orderBy = orderBy + parseColumnName(einfo);
        }

        return orderBy;
    }

    /**
     * Parses the values for the where clause.
     * 
     * @param type
     *            the bean type
     * @param beans
     *            sample beans to use as example
     * 
     * @return the where values
     */
    private Map<String, List<Object>> parseWhereValuesByExample(final Class<?> type, final Object... beans)
    {
        final Map<String, List<Object>> result = new Hashtable<String, List<Object>>();

        final StorableInfo sinfo = EntityUtils.info(type);

        for (final Object bean : beans)
        {
            for (final ElementInfo einfo : sinfo.nonVirtualElements())
            {
                final String name = einfo.firstAliasForTarget(DEFAULT_TARGET_NAME);
                final Object value = EntityUtils.value(bean, einfo.name());
                if (value != null)
                {
                    if (result.containsKey(name))
                        result.get(name).add(value);
                    else
                    {
                        List<Object> colValues = new ArrayList<Object>();
                        colValues.add(value);
                        result.put(name, colValues);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Parses the table name for the entity type.
     * <p>
     * It will try for the target alias first and if not present it will use the type name as the
     * table name.
     * 
     * @param sinfo
     *            the {@link StorableInfo} for the bean type
     * 
     * @return the table name
     */
    // private String parseTableName(final StorableInfo sinfo)
    // {
    // assert (sinfo != null);
    //
    // String schema = sinfo.schema();
    // String name = sinfo.firstAliasForTarget(DEFAULT_TARGET_NAME);
    //
    // if (name == null)
    // name = sinfo.name();
    //
    // return schema == null ? name.toLowerCase() : schema.toLowerCase() + "." + name.toLowerCase();
    // }

    /**
     * Parses the column name for the given {@link ElementInfo}.
     * <p>
     * It will try for the target alias first and if not present it will use the element name as the
     * column name.
     * 
     * @param einfo
     *            the {@link ElementInfo}
     * 
     * @return the column name
     */
    private String parseColumnName(final ElementInfo einfo)
    {
        String columnName = einfo.firstAliasForTarget(DEFAULT_TARGET_NAME);

        if (columnName == null)
            return columnName = einfo.name();

        return columnName.toUpperCase();
    }

    /**
     * 
     * @param type
     * @param query
     * @return
     */
    private String parseQueryWhere(final Class<?> type, final Query query)
    {
        if (!query.hasWhere())
            return null;

        String where = "WHERE ";
        ElementInfo einfo;

        for (Expression exp : query.where())
        {
            switch (exp.type())
            {
            case EQUAL:
                einfo = EntityUtils.info(type, exp.name());
                where += String.format("%s%s%s", einfo.firstAliasForTarget(DEFAULT_TARGET_NAME), exp.not() ? "<>" : "=",
                        toJdbcValue(einfo, exp.value()));
                break;
            case LESS:
                einfo = EntityUtils.info(type, exp.name());
                where += String.format("%s%s%s", einfo.firstAliasForTarget(DEFAULT_TARGET_NAME), exp.not() ? ">=" : "<",
                        toJdbcValue(einfo, exp.value()));
                break;
            case LESS_EQUAL:
                einfo = EntityUtils.info(type, exp.name());
                where += String.format("%s%s%s", einfo.firstAliasForTarget(DEFAULT_TARGET_NAME), exp.not() ? ">" : "<=",
                        toJdbcValue(einfo, exp.value()));
                break;
            case GREATER:
                einfo = EntityUtils.info(type, exp.name());
                where += String.format("%s%s%s", einfo.firstAliasForTarget(DEFAULT_TARGET_NAME), exp.not() ? "<=" : ">",
                        toJdbcValue(einfo, exp.value()));
                break;
            case GREATER_EQUAL:
                einfo = EntityUtils.info(type, exp.name());
                where += String.format("%s%s%s", einfo.firstAliasForTarget(DEFAULT_TARGET_NAME), exp.not() ? "<" : ">=",
                        toJdbcValue(einfo, exp.value()));
                break;
            case IN:
                einfo = EntityUtils.info(type, exp.name());
                where += String.format("%s %s (%s)", einfo.firstAliasForTarget(DEFAULT_TARGET_NAME),
                        exp.not() ? "NOT IN" : "IN", toJdbcValues(einfo, exp.values()));
                break;
            case BETWEEN:
                einfo = EntityUtils.info(type, exp.name());
                where += String.format("%s %s %s AND %s", einfo.firstAliasForTarget(DEFAULT_TARGET_NAME),
                        exp.not() ? "NOT BETWEEN" : "BETWEEN", toJdbcValues(einfo, exp.begin()),
                        toJdbcValues(einfo, exp.end()));
                break;
            case IS_NULL:
                einfo = EntityUtils.info(type, exp.name());
                where += String.format("%s %s", einfo.firstAliasForTarget(DEFAULT_TARGET_NAME),
                        exp.not() ? "IS NOT NULL" : "IS NULL");
                break;
            case IS_NOT_NULL:
                einfo = EntityUtils.info(type, exp.name());
                where += String.format("%s %s", einfo.firstAliasForTarget(DEFAULT_TARGET_NAME),
                        exp.not() ? "IS NULL" : "IS NOT NULL");
                break;
            case AND:
                where += " AND ";
                break;
            case OR:
                where += " OR ";
                break;
            case OPEN_PARENTHESIS:
                where += "(";
                break;
            case CLOSE_PARENTHESIS:
                where += ")";
                break;
            default:
                break;
            }
        }

        return where;
    }

    private String toJdbcValue(final ElementInfo einfo, final Object value)
    {
        if (value == null)
            return "null";

        switch (einfo.dataType())
        {
        case BOOLEAN:
            if (value instanceof Boolean)
                return (Boolean) value ? "true" : "false";
            else if (value instanceof Integer)
                return ((Integer) value == 1) ? "1" : "0";
            else if (value instanceof Long)
                return ((Long) value == 1) ? "1" : "0";
            else if (value instanceof String)
                return "" + Boolean.parseBoolean((String) value);
            else
                return value.toString();
        case BYTE:
        case BYTES:
        case LONG:
        case SHORT:
        case INT:
        case FLOAT:
        case DOUBLE:
        case ID:
        case REF:
        case TUPLE:
            return value.toString();
        case CHAR:
        case STRING:
        case ENUM:
            return "'" + value.toString() + "'";
        case DATE:
            if (dialect == POSTGRESQL)
            {
                if (value instanceof Date)
                    // PostgreSQL to_timestamp has only seconds resolution
                    return "to_timestamp(" + ((Date) value).getTime() / 1000 + ")";
                else if (value instanceof Long)
                    // PostgreSQL to_timestamp has only seconds resolution
                    return "to_timestamp(" + ((Long) value) / 1000 + ")";
                else
                    return "{d '" + value.toString() + "'}";
            }
            else
                return "{d '" + value.toString() + "'}";
        case TIME:
            return "{t '" + value.toString() + "'}";
        case TIMESTAMP:
            return "{ts '" + value.toString() + "'}";
        case LIST:
        case UNKNOWN:
            return value.toString();
        default:
            return value.toString();
        }
    }

    private String toJdbcValues(final ElementInfo einfo, final Object... values)
    {
        String result = null;
        for (final Object value : values)
        {
            final String tmp = toJdbcValue(einfo, value);
            result = result == null ? tmp : result + ", " + tmp;
        }
        return result;
    }

    private String parseQueryLimit(final Query query)
    {
        if (!query.hasLimit())
            return null;

        String limit = null;

        switch (dialect)
        {
        case ORACLE:
            limit = String.format(" ROWNUM < %d", query.limit());
            break;
        case POSTGRESQL:
            limit = String.format(" LIMIT %d OFFSET %d", query.limit(),
                    query.hasOffset() ? (query.offset() == 1 ? 0L : query.offset() - 1) : 0L);
            break;
        case MYSQL:
            limit = String.format(" LIMIT %d,%d",
                    query.hasOffset() ? (query.offset() == 1 ? 0L : query.offset() - 1) : 0L, query.limit());
            break;
        case DERBY:
            limit = String.format(" OFFSET %d ROWS FETCH NEXT %d ROWS ONLY",
                    query.hasOffset() ? query.offset() - 1 : 0L, query.limit());
            break;
        default:
            // do nothing
        }

        return limit;
    }

    private String parseQueryOrder(final Class<?> type, final Query query)
    {
        if (!query.hasOrder())
            return null;

        String order = null;

        for (final Pair<String, Boolean> element : query.order())
        {
            final String column = EntityUtils.info(type, element.first()).firstAliasForTarget(DEFAULT_TARGET_NAME);
            final String asc = element.second() ? "ASC" : "DESC";

            if (order == null)
                order = String.format(" ORDER BY %s %s", column, asc);
            else
                order = String.format("%s, %s %s", order, column, asc);
        }

        return order;
    }

    private String parseQuerySelect(final String baseSelect, final String where, final String order, final String limit)
    {
        String sql = baseSelect;

        if (dialect == SqlDialect.ORACLE)
            sql = sql + (where == null ? "" : " " + where) + (order == null ? "" : " " + order)
                    + (limit == null ? "" : " " + where == null ? "WHERE " + limit : limit);
        else
            sql = sql + (where == null ? "" : " " + where) + (order == null ? "" : " " + order)
                    + (limit == null ? "" : " " + limit);

        return sql;
    }

    private <T> Cursor<T> openCursor(final Transaction txn, final Cache cache, final Class<T> type, final String sql)
            throws OperationTimeoutException, DataProviderException
    {
        return openCursor(txn, cache, type, sql, Collections.emptyList());
    }

    private <T> Cursor<T> openCursor(final Transaction txn, final Cache cache, final Class<T> type, final String sql,
            List<Object> values) throws OperationTimeoutException, DataProviderException
    {
        Cursor<T> cursor = null;

        final Connection conn = getConnection(txn);

        try
        {
            final List<T> lrs = new ArrayList<T>();

            final PreparedStatement pst = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);

            pst.setFetchSize(fetchSize.intValue());

            setQueryTimeout(pst);

            if (!values.isEmpty())
            {
                int parameterIndex = 1;
                for (final Object value : values)
                {
                    if (value instanceof TimeUnit)
                    {
                        final String unit = EntityUtils.parseTimeUnit((TimeUnit) value);
                        pst.setObject(parameterIndex++, unit);
                    }
                    else if (value instanceof Enum<?>)
                    {
                        pst.setObject(parameterIndex++, value.toString());
                    }
                    else
                    {
                        pst.setObject(parameterIndex++, value);
                    }
                }
            }

            final ResultSet rs = pst.executeQuery();

            while (rs.next())
            {
                final T bean = makeEntity(type, rs); // Instantiate and populate a new bean
                cacheIt(txn, cache, bean); // Caches the new bean
                lrs.add(bean); // Adds to the cursor collection
            }

            rs.close();

            T[] resultSet = (T[]) Array.newInstance(type, lrs.size());
            System.arraycopy(lrs.toArray(), 0, resultSet, 0, lrs.size());

            lrs.clear();

            cursor = new SimpleCursor<T>(resultSet);
        }
        catch (final SQLTimeoutException e)
        {
            throw new OperationTimeoutException("The currently executing 'cursor*' operation is timed out", e);
        }
        catch (final SQLException sqle)
        {
            throw new DataProviderException("Could not execute the database statement", sqle);
        }
        finally
        {
            close(txn, conn);
        }

        return cursor;
    }

    /**
     * Assembles a new entity object based on the content of the current result set position.
     * 
     * @param type
     * @param rs
     * 
     * @return
     * 
     * @throws IllegalArgumentException
     * @throws SQLException
     */
    @SuppressWarnings("rawtypes")
    private <T> T makeEntity(final Class<T> type, final ResultSet rs) throws IllegalArgumentException, SQLException
    {
        T bean = null;

        final ResultSetMetaData meta = rs.getMetaData();

        try
        {
            bean = type.newInstance();
        }
        catch (final InstantiationException e)
        {
            return null;
        }
        catch (final IllegalAccessException e)
        {
            return null;
        }

        // FIXME support for all SQL/Java types
        for (int i = 1; i <= meta.getColumnCount(); i++)
        {
            final Object value = rs.getObject(i);
            if (rs.wasNull())
                continue;

            final String colName = meta.getColumnName(i);

            final ElementInfo einfo = EntityUtils.info(bean.getClass(), colName);

            if (einfo == null)
                continue;

            final Class<?> beanFieldType = einfo.type();

            if (beanFieldType.equals(TimeUnit.class))
            {
                EntityUtils.value(bean, colName, EntityUtils.parseTimeUnit((String) value));
            }
            else if (beanFieldType.isEnum())
            {
                EntityUtils.value(bean, colName, Enum.valueOf((Class<Enum>) beanFieldType, (String) value));
            }
            // else if (value instanceof Date)
            // {
            // BeanUtils.value(bean, colName, ((Date) value).getTime());
            // }
            // else if (value instanceof Timestamp)
            // {
            // BeanUtils.value(bean, colName, ((Timestamp) value).getTime());
            // }
            else if (value instanceof Integer)
            {
                if (beanFieldType.equals(Integer.class))
                    EntityUtils.value(bean, colName, value);
                else if (beanFieldType.equals(Long.class))
                    EntityUtils.value(bean, colName, new Long(((Integer) value)));
                else if (beanFieldType.equals(Boolean.class))
                    EntityUtils.value(bean, colName, ((Integer) value) == 1 ? true : false);
                else
                    EntityUtils.value(bean, colName, value);
            }
            else if (value instanceof Long)
            {
                if (beanFieldType.equals(Integer.class))
                    EntityUtils.value(bean, colName, ((Long) value).intValue());
                else if (beanFieldType.equals(Long.class))
                    EntityUtils.value(bean, colName, value);
                else
                    EntityUtils.value(bean, colName, value);
            }
            else if (value instanceof BigDecimal)
            {
                if (beanFieldType.equals(Integer.class))
                    EntityUtils.value(bean, colName, ((BigDecimal) value).intValue());
                else if (beanFieldType.equals(Long.class))
                    EntityUtils.value(bean, colName, ((BigDecimal) value).longValue());
                else
                    EntityUtils.value(bean, colName, value);
            }
            else
            {
                EntityUtils.value(bean, colName, value);
            }
        }

        return bean;
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
     * Retrieves the connection from the current transaction. If there is no transaction, try to
     * acquire a transaction from the providers pool.
     * 
     * @param txn
     *            the current transaction object; can be {@code null} with there is no transaction
     * 
     * @return the connection
     * 
     * @throws NotEnoughResourceException
     *             if a database access error occurs
     */
    private Connection getConnection(final Transaction txn) throws NotEnoughResourceException
    {
        return txn == null ? provider.getConnection() : ((JdbcTransactionImpl) txn).getConnection();
    }

    /**
     * Sets the query timeout to the given Statement object.
     * 
     * @param st
     *            the Statement object to set
     * 
     * @throws SQLException
     *             if a database access error occurs
     * @throws SQLException
     *             if this method is called on a closed Statement
     * @throws SQLException
     *             if the condition seconds >= 0 is not satisfied
     */
    private void setQueryTimeout(final Statement st) throws SQLException
    {
        if (queryTimeout > 0L)
        {
            st.setQueryTimeout(queryTimeout.intValue());
        }
    }

    /**
     * Commits the given connection. If the connection is participating in a transaction this call
     * has no effect.
     * 
     * @param txn
     *            the current transaction or {@code null} when there is no active transaction
     * @param conn
     *            the {@link Connection} to commit
     * 
     * @throws SQLException
     *             if a database access error occurs
     */
    private void commit(final Transaction txn, final Connection conn) throws SQLException
    {
        if (txn == null)
        {
            conn.commit();
        }
    }

    /**
     * Roll backs the given connection. If the connection is participating in a transaction this
     * call has no effect.
     * 
     * @param txn
     *            the current transaction or {@code null} when there is no active transaction
     * @param conn
     *            the {@link Connection} to roll back
     * 
     * @throws SQLException
     *             if a database access error occurs
     */
    private void rollback(final Transaction txn, final Connection conn) throws SQLException
    {
        if (txn == null)
        {
            conn.rollback();
        }
    }

    /**
     * Closes the given connection. If the connection is participating in a transaction this call
     * has no effect.
     * 
     * @param txn
     *            the current transaction or {@code null} when there is no active transaction
     * @param conn
     *            the {@link Connection} to close
     * 
     * @throws DataProviderException
     *             if a database access error occurs
     */
    private void close(final Transaction txn, final Connection conn) throws DataProviderException
    {
        if (txn == null)
        {
            try
            {
                conn.close();
            }
            catch (final SQLException e)
            {
                throw new DataProviderException("Could not close the database connection", e);
            }
        }
    }
}
