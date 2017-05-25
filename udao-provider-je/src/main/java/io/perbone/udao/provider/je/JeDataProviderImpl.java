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

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.TransactionConfig;

import io.perbone.mkey.CacheBuilder;
import io.perbone.mkey.EvictionPolicy;
import io.perbone.mkey.GarbagePolicy;
import io.perbone.toolbox.serialization.JSONSerializer;
import io.perbone.toolbox.serialization.Serializer;
import io.perbone.toolbox.settings.Property;
import io.perbone.toolbox.validation.StringValidations;
import io.perbone.udao.DataConstraintViolationException;
import io.perbone.udao.DataException;
import io.perbone.udao.KeyViolationException;
import io.perbone.udao.NotEnoughResourceException;
import io.perbone.udao.NotFoundException;
import io.perbone.udao.OperationTimeoutException;
import io.perbone.udao.spi.DataProvider;
import io.perbone.udao.spi.DataProviderException;
import io.perbone.udao.spi.DataSource;
import io.perbone.udao.spi.internal.AbstractDataProvider;
import io.perbone.udao.transaction.InvalidTransactionException;
import io.perbone.udao.transaction.IsolationLevel;
import io.perbone.udao.transaction.Transaction;
import io.perbone.udao.transaction.TransactionException;

/**
 * Concrete implementation of {@link DataProvider} for JE storage.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public final class JeDataProviderImpl extends AbstractDataProvider
{
    private static final Logger logger = LoggerFactory.getLogger(JeDataProviderImpl.class);

    @Property(name = "execution-mode", value = "LOCAL")
    private String executionMode;

    @Property(name = "database-home")
    private String databaseHome;

    @Property(name = "allow-create", value = "true")
    private Boolean allowCreate;

    @Property(name = "shared-cache", value = "true")
    private Boolean sharedCache;

    @Property(name = "cache-percentage", value = "60")
    private Integer cachePercentage;

    @Property(name = "max-file-size")
    private Integer maxFileSize;

    @Property(name = "transactional", value = "false")
    private Boolean transactional;

    @Property(name = "auto-commit", value = "false")
    private Boolean autoCommit;

    @Property(name = "isolation-level", value = "READ_COMMITTED")
    private IsolationLevel isolationLevel;

    @Property(name = "fetch-size", value = "100")
    private Long fetchSize;

    @Property(name = "query-timeout", value = "0")
    private Long queryTimeout;

    @Property(name = "list-cache-enabled", value = "false")
    private Boolean listCacheEnabled;

    @Property(name = "compress-payload", value = "false")
    private Boolean compressPayload;

    @Property(name = "application-name")
    private String applicationName;

    @Property(name = "client-user")
    private String clientUser;

    @Property(name = "client-hostname")
    private String clientHostname;

    private Map<Transaction, ConcurrentMap<String, Deque<Database>>> activeTransactions = new ConcurrentHashMap<>();

    private Environment dbenv;

    private Serializer serializer;

    private io.perbone.mkey.Cache secondaryKeyCreatorCache;

    public JeDataProviderImpl()
    {
        super();

        BACKEND_NAME = "JE";
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

        DataSource ds = new JeDataSourceImpl(this);

        ds.open();

        dsInUse.add(new WeakReference<DataSource>(ds));

        return ds;
    }

    @Override
    public void flush(Transaction txn) throws UnsupportedOperationException, IllegalStateException,
            TransactionException, NotFoundException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        // TODO Auto-generated method stub
    }

    @Override
    public Transaction begin(String id)
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

        try
        {
            // FIXME set txnConfig properties
            TransactionConfig txnConfig = new TransactionConfig();

            // TODO support for nested (parent) transaction
            com.sleepycat.je.Transaction internalTxn = dbenv.beginTransaction(null, txnConfig);

            Transaction txn = new JeTransactionImpl(id, isolationLevel, internalTxn);

            // Initialize support structures for the new transaction
            activeTransactions.put(txn, new ConcurrentHashMap<>());

            return txn;
        }
        catch (final DatabaseException dbe)
        {
            throw new DataProviderException(dbe);
        }
    }

    @Override
    public void commit(Transaction txn)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        checkActive();
        checkShutdownInProgress();

        checkTransaction(txn);

        final ConcurrentMap<String, Deque<Database>> databases = activeTransactions.remove(txn);
        final com.sleepycat.je.Transaction internalTxn = ((JeTransactionImpl) txn).getTransaction();

        try
        {
            internalTxn.commit();
        }
        catch (final DatabaseException dbe)
        {
            try
            {
                internalTxn.abort();
            }
            catch (final DatabaseException e)
            {
                // Cannot do anything other than logging...
                logger.warn("Abort attempted and failed", e);
            }

            throw new TransactionException("Unable to commit transaction", dbe);
        }
        finally
        {
            if (databases != null)
            {
                for (Deque<Database> deque : databases.values())
                {
                    Iterator<Database> it = deque.iterator();
                    while (it.hasNext())
                    {
                        try
                        {
                            it.next().close();
                        }
                        catch (final DatabaseException dbe)
                        {
                            logger.warn("Unable to close the databases; " + dbe.getMessage());
                        }
                    }

                    deque.clear();
                }
            }

            ((JeTransactionImpl) txn).invalidate();
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

        final ConcurrentMap<String, Deque<Database>> databases = activeTransactions.remove(txn);
        final com.sleepycat.je.Transaction internalTxn = ((JeTransactionImpl) txn).getTransaction();

        try
        {
            internalTxn.abort();
        }
        catch (final DatabaseException dbe)
        {
            // Cannot do anything other than logging...
            logger.warn("Abort attempted and failed", dbe);

            throw new TransactionException("Unable to commit transaction", dbe);
        }
        finally
        {
            if (databases != null)
            {
                for (Deque<Database> deque : databases.values())
                {
                    Iterator<Database> it = deque.iterator();
                    while (it.hasNext())
                    {
                        try
                        {
                            it.next().close();
                        }
                        catch (final DatabaseException dbe)
                        {
                            logger.warn("Unable to close the databases; " + dbe.getMessage());
                        }
                    }

                    deque.clear();
                }
            }

            ((JeTransactionImpl) txn).invalidate();
        }
    }

    @Override
    protected void onActivate() throws IllegalStateException, NotEnoughResourceException, DataException
    {
        if (!StringValidations.isValid(executionMode))
            throw new IllegalStateException(
                    "Cannot initialize database environment; invalid parameter 'executionMode'");

        if (!StringValidations.isValid(databaseHome))
            throw new IllegalStateException("Cannot initialize database environment; invalid parameter 'databaseHome'");

        try
        {
            final EnvironmentConfig envConfig = new EnvironmentConfig();

            envConfig.setAllowCreate(allowCreate && !isReadOnly());
            envConfig.setSharedCache(sharedCache);
            envConfig.setCachePercent(cachePercentage);
            envConfig.setTransactional(transactional);
            if (maxFileSize != null)
                envConfig.setConfigParam(EnvironmentConfig.LOG_FILE_MAX, maxFileSize.toString());

            dbenv = new Environment(new File(databaseHome), envConfig);
            serializer = new JSONSerializer(); // FIXME use compressPayload flag
            secondaryKeyCreatorCache = CacheBuilder
                    .newInstance()
                    .hardLimitSize(100L)
                    .garbagePolicy(GarbagePolicy.ACCESS_TIMEOUT)
                    .accessTimeout(3L, TimeUnit.MINUTES)
                    .evictionPolicy(EvictionPolicy.LRU)
                    .build();
        }
        catch (final Exception e)
        {
            throw new DataProviderException("Provider activation aborted; could initialize database environment", e);
        }
    }

    @Override
    protected void onShutdown(final long graceTime, final TimeUnit unit)
            throws IllegalArgumentException, IllegalStateException, DataException
    {
        try
        {
            if (dbenv != null)
            {
                // FIXME should handle open resources (database, cursors etc)
                dbenv.sync();
                dbenv.close();
            }
        }
        catch (final Exception e)
        {
            throw new DataProviderException("Provider shutdown fail; could not close the database environment", e);
        }
    }

    Serializer getSerializer()
    {
        return serializer;
    }

    io.perbone.mkey.Cache getSecondaryKeyCreatorCache()
    {
        return secondaryKeyCreatorCache;
    }

    Database openDatabase(final Transaction txn, final String tableName) throws DataProviderException
    {
        Database db = null;

        try
        {
            if (txn == null)
            {
                db = openoOrCreateDatabase(txn, tableName);
            }
            else
            {
                ConcurrentMap<String, Deque<Database>> databases = activeTransactions.get(txn);
                Deque<Database> newDeque = new ConcurrentLinkedDeque<>();
                Deque<Database> deque = databases.putIfAbsent(tableName, newDeque);

                if (deque == null)
                {
                    deque = newDeque;
                }

                db = deque.poll();

                if (db == null)
                {
                    db = openoOrCreateDatabase(txn, tableName);
                }
            }
        }
        catch (final DatabaseException dbe)
        {
            throw new DataProviderException("Could not open the database", dbe);
        }

        return db;
    }

    /**
     * Closes the given database object.
     * <p>
     * If there is an active transaction the database closing will be deferred.
     * 
     * @param txn
     *            the active transaction
     * @param db
     *            the database to close
     * 
     * @throws DataProviderException
     *             if an error occurs during this operation
     */
    void closeDatabase(final Transaction txn, final Database db) throws DataProviderException
    {
        if (txn == null)
        {
            try
            {
                db.close();
            }
            catch (final DatabaseException dbe)
            {
                throw new DataProviderException("Could not close the database", dbe);
            }
        }
        else
        {
            activeTransactions.get(txn).get(db.getDatabaseName()).add(db);
        }
    }

    /**
     * Opens, and optionally creates, a Database.
     * 
     * @param txn
     *            the active transaction
     * @param tableName
     *            the database (table) name
     * 
     * @return the opened database object
     * 
     * @throws DatabaseException
     *             if an error occurs during this operation
     */
    private Database openoOrCreateDatabase(final Transaction txn, final String tableName) throws DatabaseException
    {
        final DatabaseConfig dbConfig = new DatabaseConfig();

        dbConfig.setReadOnly(isReadOnly());
        dbConfig.setAllowCreate(allowCreate && !isReadOnly());
        dbConfig.setTransactional(txn != null);

        return dbenv.openDatabase(txn == null ? null : ((JeTransactionImpl) txn).getTransaction(), tableName, dbConfig);
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
        if (!(txn instanceof JeTransactionImpl))
            throw new InvalidTransactionException(
                    "Invalid transaction; object is not an instance of JeTransactionImpl");
        if (!txn.isActive())
            throw new InvalidTransactionException("Invalid transaction; object is no longer active");
    }
}