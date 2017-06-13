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

package io.perbone.udao;

import java.util.concurrent.TimeUnit;

import io.perbone.toolbox.provider.NotEnoughResourceException;
import io.perbone.toolbox.settings.BackingStoreException;
import io.perbone.toolbox.settings.InvalidSettingsException;

/**
 * Bootstrap class that is used to obtain an {@link DataManager}.
 * <p>
 * This class is the entry-point to the data API.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public interface DataManagerFactory<T extends DataManager>
{
    /**
     * Loads the settings from all default stores.
     * 
     * @return this instance
     * 
     * @throws IllegalStateException
     *             if either shutdown is in progress or this factory is inactive
     * @throws BackingStoreException
     *             if the backing store is invalid (not found, unreachable, etc)
     * @throws InvalidSettingsException
     *             if the settings from the store are invalid
     */
    DataManagerFactory<T> loadDefaultSettings()
            throws IllegalStateException, BackingStoreException, InvalidSettingsException;

    /**
     * Loads the settings from the given store.
     * 
     * @param path
     *            the backing store path
     * 
     * @return this instance
     * 
     * @throws IllegalStateException
     *             if either shutdown is in progress or this factory is inactive
     * @throws IllegalArgumentException
     *             if the path is invalid
     * @throws BackingStoreException
     *             if the backing store is invalid (not found, unreachable, etc)
     * @throws InvalidSettingsException
     *             if the settings from the store are invalid
     */
    DataManagerFactory<T> loadSettings(String path)
            throws IllegalStateException, IllegalArgumentException, BackingStoreException, InvalidSettingsException;

    /**
     * Creates an {@link DataManager} object for the default storage unit.
     * 
     * @return a new {@link DataManager} object
     * 
     * @throws IllegalStateException
     *             if either shutdown is in progress or this factory is inactive
     * @throws DataException
     *             if cannot create a new object
     */
    DataManager create() throws IllegalStateException, DataException;

    /**
     * Creates a {@link DataManager} object for the given storage unit name.
     * 
     * @param unitId
     *            the storage unit id
     * 
     * @return a new {@link dataManager} object
     * 
     * @throws IllegalStateException
     *             if either shutdown is in progress or this factory is inactive
     * @throws IllegalArgumentException
     *             if the storage unit name is invalid
     * @throws DataException
     *             if cannot create a new object
     */
    DataManager create(String unitId) throws IllegalStateException, IllegalArgumentException, DataException;

    /**
     * Destroys the given {@link DataManager} object.
     * 
     * @param dm
     *            the object to be destroyed
     * 
     * @throws IllegalStateException
     *             if either shutdown is in progress or this factory is inactive
     * @throws IllegalArgumentException
     *             if the object is invalid
     * @throws DataException
     *             if cannot destroy the object
     */
    void destroy(DataManager dm) throws IllegalStateException, IllegalArgumentException, DataException;

    /**
     * Activates this factory.
     * 
     * @return this instance
     * 
     * @throws IllegalStateException
     *             if either shutdown is in progress or this factory is already active
     * @throws DataException
     *             if cannot activate this factory
     * 
     * @see #isShutdown
     * @see #isActive
     */
    DataManagerFactory<T> activate() throws IllegalStateException, NotEnoughResourceException, DataException;

    /**
     * Shuts down this factory.
     * <p>
     * Initiates an orderly shutdown in which previously opened data sources will all be closed and
     * no new {@link dataManager} can be requested for this factory.
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
     * @return this instance
     * 
     * @throws IllegalArgumentException
     *             if either graceTime or unit are invalid
     * @throws IllegalStateException
     *             if this factory is inactive
     * @throws DataException
     *             if cannot shutdown this factory
     * 
     * @see #isShutdownInProgress
     * @see #isActive
     */
    DataManagerFactory<T> shutdown(long graceTime, TimeUnit unit)
            throws IllegalArgumentException, IllegalStateException, DataException;

    /**
     * Returns the status of the shutdown process.
     * 
     * @return <tt>true</tt> if the shutdown is in progress; <tt>false</tt> otherwise
     * 
     * @see #shutdown
     * @see #isActive
     */
    boolean isShutdownInProgress();

    /**
     * Tells whether or not this factory is active.
     * <p>
     * It is assumed that after a successful object instantiation this method will return
     * <tt>true</tt>. Conversely for fail object instantiation this method should return
     * <tt>false</tt> despite the fact that this object may still be valid.
     * <p>
     * For an active factory asked for shutdown, it will return <tt>true</tt> until
     * {@link #isShutdownInProgress} returns <tt>true</tt>; after that it will return
     * <tt>false</tt>.
     * 
     * @return <tt>true</tt> if it is active; <tt>false</tt> otherwise
     */
    boolean isActive();
}