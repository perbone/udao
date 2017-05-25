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
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.perbone.udao.DataConstraintViolationException;
import io.perbone.udao.DataManager;
import io.perbone.udao.KeyViolationException;
import io.perbone.udao.NotEnoughResourceException;
import io.perbone.udao.NotFoundException;
import io.perbone.udao.OperationTimeoutException;
import io.perbone.udao.DataException;
import io.perbone.udao.transaction.Transaction;
import io.perbone.udao.transaction.TransactionException;

/**
 * A <code>DataProvider</code> is a factory for concrete classes of the {@link DataSource}
 * interface.
 * <p>
 * It serves as an intermediate between {@link DataManager} and {@link DataSource} acting in the
 * role of life-cycle manager for all {@link DataSource} it creates.
 * <p>
 * A provider is either active or inactive. A provider is active upon creation, and once it shuts
 * down it remains inactive. Once a provider is inactive, any attempt to invoke a storage operation
 * upon it will cause a {@link IllegalStateException} to be thrown. Whether or not a provider is
 * active may be tested by invoking its {@link #isActive} method.
 * <p>
 * <code>DataProvider</code>s are, in general, intended to be safe for multithreaded access but it
 * is really up to the implementation to support this feature or not. In any case, it should be
 * documented so the developers using the concrete classes do not make mistakes.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public interface DataProvider
{
    /**
     * The provider id.
     * <p>
     * This id should to be unique application wise.
     * 
     * @return the provider id
     */
    String id();

    /**
     * Returns the name for the underling storage back-end.
     * <p>
     * This name must be unique between all providers implementations.
     * 
     * @return the back-end name
     */
    String backendName();

    /**
     * Activates this provider.
     * 
     * @return this concrete {@link DataProvider} implementation
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
     * @see #isShutdownInProgress
     * @see #isActive
     */
    <T extends DataProvider> T activate() throws IllegalStateException, NotEnoughResourceException, DataException;

    /**
     * Shuts down this provider.
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
     * @return this concrete {@link DataProvider} implementation
     * 
     * @throws IllegalArgumentException
     *             if either graceTime or unit are invalid
     * @throws IllegalStateException
     *             if this provider is inactive
     * @throws DataException
     *             if cannot shutdown this provider
     * 
     * @see #isShutdownInProgress
     * @see #isActive
     */
    <T extends DataProvider> T shutdown(final long graceTime, final TimeUnit unit)
            throws IllegalArgumentException, IllegalStateException, DataException;

    /**
     * Returns the status of the shutdown process.
     * 
     * @return <tt>true</tt> if the shutdown is in progress; <tt>false</tt> otherwise
     * 
     * @see #isActive
     */
    boolean isShutdownInProgress();

    /**
     * Tells whether or not this provider is active.
     * <p>
     * It is assumed that after a successful object instantiation this method will return
     * <tt>true</tt>. Conversely for fail object instantiation this method should return
     * <tt>false</tt> despite the fact that this object may still be valid.
     * <p>
     * For an active provider asked for shutdown, it will return <tt>true</tt> until
     * {@link #isShutdownInProgress} returns <tt>true</tt>; after that it will returns
     * <tt>false</tt>.
     * 
     * @return <tt>true</tt> if it is active; <tt>false</tt> otherwise
     * 
     * @see #isShutdownInProgress
     */
    boolean isActive();

    /**
     * Tells whether or not this provider is read-only.
     * 
     * @return <tt>true</tt> if it is read-only; <tt>false</tt> otherwise
     */
    boolean isReadOnly();

    /**
     * Sets the read-only value for this provider.
     * 
     * @param value
     *            the read-only value fo this provider
     */
    void setReadOnly(boolean value);

    /**
     * Returns a collection of resource class types this provider is capable to manage through its
     * data sources.
     * 
     * @return a collection of supported bean types
     * 
     * @throws IllegalStateException
     *             if shutdown is in progress or this provider is inactive
     * @throws DataProviderException
     * 
     * @see #isShutdown
     * @see #isActive
     */
    Set<Class<?>> getTypes() throws IllegalStateException, DataProviderException;

    /**
     * Tells whether or not this provider supports transactions.
     * 
     * @return <tt>true</tt> if it supports; <tt>false</tt> otherwise
     * 
     * @throws IllegalStateException
     *             if shutdown is in progress or this provider is inactive
     * @throws DataProviderException
     */
    boolean isTransactionSupported() throws IllegalStateException, DataProviderException;

    /**
     * Checks the given bean type upon the underling storage.
     * <p>
     * Providers should look if the bean exists into its declared schema.
     * 
     * @param type
     *            the bean type to check
     * 
     * @throws IllegalStateException
     *             if shutdown is in progress or this provider is inactive
     * @throws OperationTimeoutException
     *             if the operation is timed out
     * @throws NotEnoughResourceException
     *             if the is not enough resources for a new bean
     * @throws DataProviderException
     */
    void checkType(Class<?> type)
            throws IllegalStateException, OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    /**
     * Tells whether or not this provider supports schema creation.
     * 
     * @return <tt>true</tt> if it supports; <tt>false</tt> otherwise
     * 
     * @throws IllegalStateException
     *             if shutdown is in progress or this provider is inactive
     * @throws DataProviderException
     */
    boolean isSchemaCreationSupported() throws IllegalStateException, DataProviderException;

    /**
     * Creates the given type upon the underling storage.
     * <p>
     * If possible providers should create the bean into its declared schema.
     * 
     * @param type
     *            the bean type to create
     * 
     * @throws IllegalStateException
     *             if shutdown is in progress or this provider is inactive
     * @throws OperationTimeoutException
     *             if the operation is timed out
     * @throws NotEnoughResourceException
     *             if the is not enough resources for a new bean
     * @throws DataProviderException
     *             if this provider cannot perform the action
     */
    void createType(Class<?> type)
            throws IllegalStateException, OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    /**
     * Opens a data source.
     * <p>
     * The brand new {@link DataSource} object can access all the underling storage under its
     * management.
     * 
     * @param type
     *            The target resource class type for the underling storage
     * @return A new {@link DataSource} object that represents a connection to the underling storage
     * 
     * @throws IllegalStateException
     *             if shutdown is in progress or this provider is inactive
     * @throws IllegalArgumentException
     *             if type is invalid
     * @throws OperationTimeoutException
     *             if the operation is timed out
     * @throws NotEnoughResourceException
     *             if the is not enough resources for a new bean
     * @throws DataProviderException
     *             if cannot create a new {@link DataSource}
     * 
     * @see #isShutdown
     * @see #isActive
     */
    DataSource openDataSource(Class<?> type) throws IllegalStateException, IllegalArgumentException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    /////// ** Transaction support operations *///////

    /**
     * 
     * @param txn
     * @throws UnsupportedOperationException
     * @throws IllegalStateException
     * @throws TransactionException
     * @throws NotFoundException
     * @throws KeyViolationException
     * @throws DataConstraintViolationException
     * @throws OperationTimeoutException
     * @throws NotEnoughResourceException
     * @throws DataProviderException
     */
    void flush(Transaction txn) throws UnsupportedOperationException, IllegalStateException, TransactionException,
            NotFoundException, KeyViolationException, DataConstraintViolationException, OperationTimeoutException,
            NotEnoughResourceException, DataProviderException;

    /**
     * 
     * @return
     * @throws UnsupportedOperationException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @throws TransactionException
     * @throws OperationTimeoutException
     * @throws NotEnoughResourceException
     * @throws DataProviderException
     */
    List<Transaction> recover() throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    /**
     * 
     * @param id
     * @return
     * @throws UnsupportedOperationException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @throws TransactionException
     * @throws OperationTimeoutException
     * @throws NotEnoughResourceException
     * @throws DataProviderException
     */
    Transaction begin(String id) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    /**
     * 
     * @param id
     * @return
     * @throws UnsupportedOperationException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @throws TransactionException
     * @throws OperationTimeoutException
     * @throws NotEnoughResourceException
     * @throws DataProviderException
     */
    Transaction join(String id) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    /**
     * 
     * @param txn
     * @throws UnsupportedOperationException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @throws TransactionException
     * @throws OperationTimeoutException
     * @throws NotEnoughResourceException
     * @throws DataProviderException
     */
    void suspend(Transaction txn) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    /**
     * 
     * @param txn
     * @throws UnsupportedOperationException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @throws TransactionException
     * @throws OperationTimeoutException
     * @throws NotEnoughResourceException
     * @throws DataProviderException
     */
    void resume(Transaction txn) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    /**
     * 
     * @param txn
     * @throws UnsupportedOperationException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @throws TransactionException
     * @throws OperationTimeoutException
     * @throws DataProviderException
     */
    void prepare(Transaction txn) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, OperationTimeoutException, DataProviderException;

    /**
     * 
     * @param txn
     * @throws UnsupportedOperationException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @throws TransactionException
     * @throws OperationTimeoutException
     * @throws NotEnoughResourceException
     * @throws DataProviderException
     */
    void commit(Transaction txn) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    /**
     * 
     * @param txn
     * @throws UnsupportedOperationException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @throws TransactionException
     * @throws OperationTimeoutException
     * @throws NotEnoughResourceException
     * @throws DataProviderException
     */
    void rollback(Transaction txn)
            throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException, TransactionException,
            OperationTimeoutException, NotEnoughResourceException, DataProviderException;

    /**
     * 
     * @param txn
     * @throws UnsupportedOperationException
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     * @throws TransactionException
     * @throws OperationTimeoutException
     * @throws NotEnoughResourceException
     * @throws DataProviderException
     */
    void forget(Transaction txn) throws UnsupportedOperationException, IllegalStateException, IllegalArgumentException,
            TransactionException, OperationTimeoutException, NotEnoughResourceException, DataProviderException;
}