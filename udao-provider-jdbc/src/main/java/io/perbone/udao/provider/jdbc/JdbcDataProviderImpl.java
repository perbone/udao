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

import static io.perbone.udao.util.EntityUtils.hostName;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.perbone.toolbox.provider.NotEnoughResourceException;
import io.perbone.toolbox.provider.OperationTimeoutException;
import io.perbone.toolbox.settings.Property;
import io.perbone.toolbox.validation.StringValidations;
import io.perbone.udao.DataConstraintViolationException;
import io.perbone.udao.DataException;
import io.perbone.udao.KeyViolationException;
import io.perbone.udao.NotFoundException;
import io.perbone.udao.spi.DataProvider;
import io.perbone.udao.spi.DataProviderException;
import io.perbone.udao.spi.DataSource;
import io.perbone.udao.spi.internal.AbstractDataProvider;
import io.perbone.udao.transaction.InvalidTransactionException;
import io.perbone.udao.transaction.IsolationLevel;
import io.perbone.udao.transaction.Transaction;
import io.perbone.udao.transaction.TransactionException;

/**
 * Concrete implementation of {@link DataProvider} for JDBC storage.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public final class JdbcDataProviderImpl extends AbstractDataProvider
{
    private static final Logger logger = LoggerFactory.getLogger(JdbcDataProviderImpl.class);

    @Property
    private String driver;

    @Property
    private String uri;

    @Property
    private String user;

    @Property
    private String password;

    @Property(value = "2")
    private Integer min;

    @Property(value = "10")
    private Integer max;

    @Property(name = "max-wait", value = "2000")
    private Integer maxWait;

    @Property(name = "auto-commit", value = "false")
    private Boolean autoCommit;

    @Property(name = "isolation-level", value = "READ_COMMITTED")
    private IsolationLevel isolationLevel;

    @Property(name = "test-on-borrow", value = "true")
    private Boolean testOnBorrow;

    @Property(name = "test-on-return", value = "false")
    private Boolean testOnReturn;

    @Property(name = "test-while-idle", value = "false")
    private Boolean testWhileIdle;

    @Property(name = "validation-query", value = "SELECT 1")
    private String validationQuery;

    @Property(name = "validation-interval", value = "30000")
    private Long validationInterval;

    @Property(name = "time-between-eviction-runs", value = "15000")
    private Integer timeBetweenEvictionRuns;

    @Property(name = "remove-abandoned", value = "true")
    private Boolean removeAbandoned;

    @Property(name = "remove-abandoned-timeout", value = "60000")
    private Integer removeAbandonedTimeout;

    @Property(name = "min-evictable-idle-time", value = "60000")
    private Integer minEvictableIdleTime;

    @Property(name = "fetch-size", value = "100")
    private Long fetchSize;

    @Property(name = "query-timeout", value = "0")
    private Long queryTimeout;

    @Property(name = "list-cache-enabled", value = "false")
    private Boolean listCacheEnabled;

    @Property(name = "application-name")
    private String applicationName;

    @Property(name = "client-user")
    private String clientUser;

    @Property(name = "client-hostname")
    private String clientHostname;

    @Property(name = "jmx-enabled", value = "false")
    private Boolean jmxEnabled;

    private org.apache.tomcat.jdbc.pool.DataSource poolDS;

    private SqlDialect dialect;

    public JdbcDataProviderImpl()
    {
        super();

        BACKEND_NAME = "JDBC";
    }

    @Override
    public boolean isTransactionSupported() throws IllegalStateException, DataProviderException
    {
        checkActive();
        checkShutdownInProgress();

        return Boolean.valueOf(autoCommit) ? false : true;
    }

    @Override
    public DataSource openDataSource(final Class<?> type) throws IllegalStateException, IllegalArgumentException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        checkActive();
        checkShutdownInProgress();

        DataSource ds = new JdbcDataSourceImpl(this, dialect, fetchSize, queryTimeout);

        ds.open();

        dsInUse.add(new WeakReference<DataSource>(ds));

        return ds;
    }

    @Override
    public void flush(Transaction txn) throws UnsupportedOperationException, IllegalStateException,
            TransactionException, NotFoundException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        checkActive();
        checkShutdownInProgress();

        checkTransaction(txn);

        // TODO JdbcStorageProviderImpl.flush()
    }

    @Override
    public Transaction begin(final String id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        checkActive();
        checkShutdownInProgress();

        if (!isTransactionSupported())
            throw new UnsupportedOperationException(
                    "This provider instance seems not to support transaction. Check its configurations and make sure that the 'auto-commit' parameter is set to 'false'");

        if (!StringValidations.isValid(id))
            throw new IllegalArgumentException("Invalid transaction id");

        return new JdbcTransactionImpl(id, isolationLevel, getConnection());
    }

    @Override
    public void commit(Transaction txn)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        checkActive();
        checkShutdownInProgress();

        checkTransaction(txn);

        Connection conn = ((JdbcTransactionImpl) txn).getConnection();

        try
        {
            conn.commit();
        }
        catch (final SQLException sqle)
        {
            try
            {
                conn.rollback();
            }
            catch (final SQLException e)
            {
                sqle.setNextException(e);
            }

            throw new TransactionException("Unable to commit transaction", sqle);
        }
        finally
        {
            try
            {
                conn.close();
            }
            catch (final SQLException e)
            {
                // Close quietly
                logger.warn("Unable to close the database connection; " + e.getMessage());
            }

            ((JdbcTransactionImpl) txn).invalidate();
        }
    }

    @Override
    public void rollback(Transaction txn)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        checkActive();
        checkShutdownInProgress();

        checkTransaction(txn);

        Connection conn = ((JdbcTransactionImpl) txn).getConnection();

        try
        {
            conn.rollback();
        }
        catch (final SQLException sqle)
        {
            throw new TransactionException("Unable to rollback transaction", sqle);
        }
        finally
        {
            try
            {
                conn.close();
            }
            catch (final SQLException e)
            {
                // Close quietly
                logger.warn("Unable to close the database connection; " + e.getMessage());
            }

            ((JdbcTransactionImpl) txn).invalidate();
        }
    }

    @Override
    protected void onActivate() throws IllegalStateException, NotEnoughResourceException, DataException
    {
        if (!StringValidations.isValid(driver))
            throw new IllegalStateException("Cannot create connection pool; invalid parameter 'driver'");

        if (!StringValidations.isValid(driver) || !StringValidations.isValid(uri))
            throw new IllegalStateException("Cannot create connection pool; invalid parameter 'uri'");

        dialect = SqlDialect.parse(driver);

        String sessionInfo = isReadOnly() ? "Read-Only; " + hostName() : hostName();

        /* Dialect specifics settings */
        if (dialect == SqlDialect.POSTGRESQL)
        {
            /* Try to set application name and host name information */
            if (uri.contains("ApplicationName"))
            {
                uri = String.format("%s; %s", uri, sessionInfo);
            }
            else if (StringValidations.isValid(applicationName))
            {
                uri = String.format("%s?ApplicationName=%s; %s", uri, applicationName, sessionInfo);
                applicationName = null; // To avoid conflict later
            }
        }
        else if (StringValidations.isValid(applicationName))
        {
            applicationName += " " + sessionInfo;
        }

        poolDS = new org.apache.tomcat.jdbc.pool.DataSource();

        poolDS.setUrl(uri);
        poolDS.setDriverClassName(driver);
        if (StringValidations.isValid(user))
            poolDS.setUsername(user);
        if (StringValidations.isValid(password))
            poolDS.setPassword(password);
        poolDS.setMinIdle(min);
        poolDS.setInitialSize(min);
        poolDS.setMaxActive(max);
        poolDS.setMaxIdle(min);
        poolDS.setMaxWait(maxWait);
        poolDS.setDefaultAutoCommit(autoCommit);
        poolDS.setDefaultTransactionIsolation(isolationLevel.level());
        poolDS.setTestOnBorrow(testOnBorrow);
        poolDS.setTestOnReturn(testOnReturn);
        poolDS.setTestWhileIdle(testWhileIdle);
        if (StringValidations.isValid(validationQuery))
            poolDS.setValidationQuery(validationQuery);
        poolDS.setValidationInterval(validationInterval);
        poolDS.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRuns);
        poolDS.setRemoveAbandoned(removeAbandoned);
        poolDS.setRemoveAbandonedTimeout((int) TimeUnit.MILLISECONDS.toSeconds(removeAbandonedTimeout));
        poolDS.setMinEvictableIdleTimeMillis(minEvictableIdleTime);
        poolDS.setJmxEnabled(jmxEnabled);
        poolDS.setJdbcInterceptors("ConnectionState;StatementFinalizer");

        /* Self initialization; works as fast fail */
        try
        {
            poolDS.getConnection().close();
        }
        catch (final SQLException sqle)
        {
            try
            {
                poolDS.close();
                poolDS = null;
            }
            catch (final Exception e)
            {
                logger.error("Could not close the connection pool after activation abort; there can be leaks...", sqle);
            }

            throw new DataProviderException("Provider activation aborted; could not open the connection pool", sqle);
        }
    }

    @Override
    protected void onShutdown(final long graceTime, final TimeUnit unit)
            throws IllegalArgumentException, IllegalStateException, DataException
    {
        try
        {
            if (poolDS != null)
            {
                poolDS.close();
            }
        }
        catch (final Exception e)
        {
            throw new DataProviderException("Provider shutdown fail; could not close the connection pool evenly", e);
        }
    }

    /**
     * Retrieves a connection from the pool with all its client info attributes filled.
     * 
     * @return the connection
     * 
     * @throws NotEnoughResourceException
     *             if a database access error occurs
     */
    Connection getConnection() throws NotEnoughResourceException
    {
        try
        {
            Connection conn = poolDS.getConnection();

            if (StringValidations.isValid(applicationName))
                conn.setClientInfo("ApplicationName", applicationName);

            if (StringValidations.isValid(clientUser))
                conn.setClientInfo("ClientUser", clientUser);

            if (StringValidations.isValid(clientHostname))
                conn.setClientInfo("ClientHostname", clientHostname);
            else
                conn.setClientInfo("ClientHostname", hostName());

            return conn;
        }
        catch (final SQLException e)
        {
            throw new NotEnoughResourceException("Could not acquire a connection; probably pool is exhausted", e);
        }
    }

    /**
     * Checks if the given transaction object is valid for this provider.
     * 
     * @param txn
     *            the transaction object to check
     * 
     * @throws InvalidTransactionException
     *             if the transaction object is invalid
     */
    private void checkTransaction(final Transaction txn) throws InvalidTransactionException
    {
        if (txn == null)
            throw new InvalidTransactionException("Invalid transaction; object is null");
        if (!(txn instanceof JdbcTransactionImpl))
            throw new InvalidTransactionException(
                    "Invalid transaction; object is not an instance of JdbcTransactionImpl");
        if (!txn.isActive())
            throw new InvalidTransactionException("Invalid transaction; object is no longer active");
    }
}