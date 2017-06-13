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

package io.perbone.udao.spi.internal;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.perbone.toolbox.provider.NotEnoughResourceException;
import io.perbone.toolbox.provider.OperationTimeoutException;
import io.perbone.udao.DataConstraintViolationException;
import io.perbone.udao.DataException;
import io.perbone.udao.KeyViolationException;
import io.perbone.udao.NotFoundException;
import io.perbone.udao.spi.DataProvider;
import io.perbone.udao.spi.DataProviderException;
import io.perbone.udao.spi.DataSource;
import io.perbone.udao.transaction.Transaction;
import io.perbone.udao.transaction.TransactionException;

/**
 * This class provides a skeletal implementation of the {@code DataProvider} interface, to minimize
 * the effort required to implement this interface.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public abstract class AbstractDataProvider implements DataProvider
{
    protected String MESSAGE_FAIL_CANNOT_ACTIVATE = "Cannot activate this provider";

    protected String MESSAGE_FAIL_CANNOT_SHUTDOWN = "Cannot shutdown this provider";

    protected String MESSAGE_FAIL_ALREADY_ACTIVE = "Attempt to activate already active provider object";

    protected String MESSAGE_INVALID_ARGUMENT_GRACE_TIME = "Invalid grace time value; cannot be negative";

    protected String MESSAGE_INVALID_ARGUMENT_UNIT = "Invalid unit value; cannot be null";

    protected String MESSAGE_FAIL_CHECK_MANAGED_TYPE = "Not a managed type";

    protected String MESSAGE_FAIL_CHECK_ACTIVE = "Attempt to use non-active provider object";

    protected String MESSAGE_FAIL_CHECK_SHUTDOWN_IN_PROGRESS = "Illegal invocation; shutdown is already in progress";

    protected String MESSAGE_FAIL_UNSUPPORTED_OPERATION = "Data Provider feature not supported by this implementation";

    protected String BACKEND_NAME = "undefined";

    protected final AtomicBoolean active = new AtomicBoolean(false);

    protected final AtomicBoolean shutdownInProgress = new AtomicBoolean(false);

    protected String providerId = this.getClass().getName();

    protected final AtomicBoolean readOnly = new AtomicBoolean(false);

    protected final Set<Class<?>> managedTypes = new ConcurrentSkipListSet<Class<?>>();

    protected final Set<WeakReference<DataSource>> dsInUse = Collections
            .synchronizedSet(new HashSet<WeakReference<DataSource>>());

    public AbstractDataProvider()
    {
        // do nothing
    }

    @Override
    public String id()
    {
        return providerId;
    }

    @Override
    public String backendName()
    {
        return BACKEND_NAME;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DataProvider> T activate() throws IllegalStateException, NotEnoughResourceException, DataException
    {
        checkShutdownInProgress();

        if (active.compareAndSet(false, true))
        {
            try
            {
                this.onActivate();
            }
            catch (final IllegalStateException e)
            {
                active.set(false); // Activation roll back
                throw e;
            }
            catch (final DataProviderException e)
            {
                active.set(false); // Activation roll back
                throw e;
            }
            catch (final Exception e)
            {
                active.set(false); // Activation roll back
                throw new DataProviderException(MESSAGE_FAIL_CANNOT_ACTIVATE, e);
            }
        }
        else
        {
            throw new IllegalStateException(MESSAGE_FAIL_ALREADY_ACTIVE);
        }

        return (T) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DataProvider> T shutdown(long graceTime, TimeUnit unit)
            throws IllegalArgumentException, IllegalStateException, DataException
    {
        checkActive();

        if (graceTime < 0)
            throw new IllegalArgumentException(MESSAGE_INVALID_ARGUMENT_GRACE_TIME);
        if (unit == null)
            throw new IllegalArgumentException(MESSAGE_INVALID_ARGUMENT_UNIT);

        if (shutdownInProgress.compareAndSet(false, true))
        {
            try
            {
                for (WeakReference<DataSource> ref : dsInUse)
                {
                    DataSource ds = ref.get();
                    if (ds != null)
                        ds.close();
                }

                this.onShutdown(graceTime, unit);

                dsInUse.clear();
                active.set(false);
            }
            catch (final IllegalArgumentException | IllegalStateException | DataProviderException e)
            {
                shutdownInProgress.set(false); // Shutdown roll back
                throw e;
            }
            catch (final Exception e)
            {
                shutdownInProgress.set(false); // Shutdown roll back
                throw new DataProviderException(MESSAGE_FAIL_CANNOT_SHUTDOWN, e);
            }
        }

        return (T) this;
    }

    @Override
    public boolean isShutdownInProgress()
    {
        return shutdownInProgress.get();
    }

    @Override
    public boolean isActive()
    {
        return active.get();
    }

    @Override
    public boolean isReadOnly()
    {
        return readOnly.get();
    }

    @Override
    public void setReadOnly(final boolean value)
    {
        readOnly.set(value);
    }

    @Override
    public Set<Class<?>> getTypes() throws IllegalStateException, DataProviderException
    {
        checkShutdownInProgress();
        checkActive();

        return Collections.unmodifiableSet(managedTypes);
    }

    @Override
    public boolean isTransactionSupported() throws IllegalStateException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void checkType(final Class<?> type)
            throws IllegalStateException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public boolean isSchemaCreationSupported() throws IllegalStateException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void createType(final Class<?> type)
            throws IllegalStateException, OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public DataSource openDataSource(final Class<?> type) throws IllegalStateException, IllegalArgumentException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void flush(final Transaction txn) throws UnsupportedOperationException, IllegalStateException,
            TransactionException, NotFoundException, KeyViolationException, DataConstraintViolationException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public List<Transaction> recover()
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public Transaction begin(final String id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public Transaction join(final String id)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void suspend(final Transaction txn)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void resume(final Transaction txn)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void prepare(final Transaction txn) throws UnsupportedOperationException, IllegalStateException,
            IllegalArgumentException, TransactionException, OperationTimeoutException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void commit(final Transaction txn)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void rollback(final Transaction txn)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    @Override
    public void forget(final Transaction txn)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException
    {
        throw new UnsupportedOperationException(MESSAGE_FAIL_UNSUPPORTED_OPERATION);
    }

    /**
     * Checks if this provider is currently active.
     * 
     * @throws IllegalStateException
     *             if it is not active
     */
    protected void checkActive() throws IllegalStateException
    {
        if (!active.get())
            throw new IllegalStateException(MESSAGE_FAIL_CHECK_ACTIVE);
    }

    /**
     * Checks if the shutdown is already in progress.
     * 
     * @throws IllegalStateException
     *             if the shutdown is already in progress
     */
    protected void checkShutdownInProgress() throws IllegalStateException
    {
        if (shutdownInProgress.get())
            throw new IllegalStateException(MESSAGE_FAIL_CHECK_SHUTDOWN_IN_PROGRESS);
    }

    /**
     * Activates the concrete provider implementation.
     * 
     * @throws IllegalStateException
     *             if shutdown is in progress
     * @throws IllegalStateException
     *             if this provider is already active
     * @throws NotEnoughResourceException
     *             if there is no enough resources to activate it
     * @throws DataException
     *             if cannot activate this provider
     * 
     * @see #isShutdown
     * @see #isActive
     */
    protected abstract void onActivate() throws IllegalStateException, NotEnoughResourceException, DataException;

    /**
     * Shuts down the concrete provider implementation.
     * <p>
     * Initiates an orderly shutdown in which previously opened data sources will all be closed and
     * no new data sources can be requested for this provider.
     * <p>
     * Invocation has no additional effect if shutdown is already in progress but will raise an
     * exception if this provider is inactive. Once inactive it remains inactive until
     * {@link #activate} is invoked again.
     * 
     * @param graceTime
     *            The period allowed for housekeeping before forced shutdown is assumed
     * @param unit
     *            The grace time unit
     * 
     * @throws IllegalArgumentException
     *             if either graceTime or unit are invalid
     * @throws IllegalStateException
     *             if this provider is inactive
     * @throws DataException
     *             if cannot shutdown this provider
     * 
     * @see #isShutdown
     * @see #isActive
     */
    protected abstract void onShutdown(final long graceTime, final TimeUnit unit)
            throws IllegalArgumentException, IllegalStateException, DataException;
}