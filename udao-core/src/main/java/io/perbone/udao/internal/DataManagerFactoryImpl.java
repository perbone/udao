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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.perbone.toolbox.provider.NotEnoughResourceException;
import io.perbone.toolbox.settings.BackingStoreException;
import io.perbone.toolbox.settings.InvalidSettingsException;
import io.perbone.toolbox.settings.Settings;
import io.perbone.toolbox.validation.StringValidations;
import io.perbone.udao.DataException;
import io.perbone.udao.DataManager;
import io.perbone.udao.DataManagerFactory;
import io.perbone.udao.configuration.CacheEntry;
import io.perbone.udao.configuration.Configuration;
import io.perbone.udao.configuration.EntityEntry;
import io.perbone.udao.configuration.ProviderEntry;
import io.perbone.udao.configuration.SchemaEntry;
import io.perbone.udao.configuration.StorageUnitEntry;
import io.perbone.udao.spi.DataProvider;
import io.perbone.udao.spi.DataProviderException;
import io.perbone.udao.util.EntityUtils;

/**
 * DataManagerFactory concrete implementation.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public final class DataManagerFactoryImpl implements DataManagerFactory<DataManager>
{
    private static final String DEFAULT_SETTINGNS_URI = "META-INF/udao.xml";
    private final static String DEFAULT_STORAGE_UNIT_KEY = "__DefaultProviderKey";

    private static final int CACHE_LEVEL_MIN = 1;
    private static final int CACHE_LEVEL_MAX = 3;
    private static final int CACHE_LEVEL_L1 = 1;
    private static final int CACHE_LEVEL_L2 = 2;
    private static final int CACHE_LEVEL_L3 = 3;

    private final AtomicBoolean active = new AtomicBoolean(false);

    private final AtomicBoolean shutdownInProgress = new AtomicBoolean(false);

    private final Configuration configuration = new Configuration();

    /** List of attached units and its providers */
    private final Map<String, Set<DataProvider>> units = new ConcurrentHashMap<>();

    private final CachePool cachePool = new CachePool();

    public DataManagerFactoryImpl()
    {
        // do nothing
    }

    @Override
    public DataManagerFactory<DataManager> loadDefaultSettings()
            throws IllegalStateException, BackingStoreException, InvalidSettingsException
    {
        checkInactive();

        try
        {
            /* Loads all settings file available to this class loader through its class path */
            final ClassLoader cl = ClassLoader.getSystemClassLoader();

            /* Find out the local settings url (may not exist at all!) */
            final URL defaultSettings = cl.getResource(DEFAULT_SETTINGNS_URI);
            if (defaultSettings != null)
            {
                String localSettingsUrl = defaultSettings.toExternalForm();

                /* First we load all but the local settings */
                final Enumeration<URL> resources = cl.getResources(DEFAULT_SETTINGNS_URI);
                while (resources.hasMoreElements())
                {
                    final URL url = resources.nextElement();
                    if (!url.toExternalForm().equals(localSettingsUrl))
                        loadSettings(url.toExternalForm());
                }

                /*
                 * Finally we process only the local settings so to enforce it over all others
                 * previously loaded jar's settings.
                 */
                if (localSettingsUrl != null)
                    loadSettings(localSettingsUrl);
            }
        }
        catch (final IOException e)
        {
            throw new BackingStoreException(String.format(
                    "Default preferences store [%s] could not be found or is unreachable", DEFAULT_SETTINGNS_URI), e);
        }

        return this;
    }

    @Override
    public DataManagerFactory<DataManager> loadSettings(final String path)
            throws IllegalStateException, IllegalArgumentException, BackingStoreException, InvalidSettingsException
    {
        checkInactive();

        if (path == null)
            throw new IllegalArgumentException("Invalid settings path; cannot be null");

        configuration.load(path);

        /* Validate settings */
        for (CacheEntry centry : configuration.caches())
        {
            if (centry.level() < CACHE_LEVEL_MIN || centry.level() > CACHE_LEVEL_MAX)
            {
                throw new InvalidSettingsException(String.format(
                        "Invalid cache level [%d]; this implementation supports only levels from [%d] to [%d]",
                        centry.level(), CACHE_LEVEL_MIN, CACHE_LEVEL_MAX));
            }
        }
        // TODO validate more settings

        return this;
    }

    @Override
    public DataManager create() throws IllegalStateException, DataException
    {
        checkActive();
        checkShutdownInProgress();

        if (!units.containsKey(DEFAULT_STORAGE_UNIT_KEY))
            throw new IllegalStateException("Default data provider not available");//

        final Set<DataProvider> providers = units.get(DEFAULT_STORAGE_UNIT_KEY);

        return new DataManagerImpl(providers, cachePool);

    }

    @Override
    public DataManager create(final String unitId) throws IllegalStateException, IllegalArgumentException, DataException
    {
        checkActive();
        checkShutdownInProgress();

        if (!StringValidations.isValid(unitId))
            throw new IllegalArgumentException("Invalid storage unit id");

        final String key = unitId.toLowerCase();

        if (!units.containsKey(key))
            throw new IllegalStateException("Storage unit id does not match any provider");

        Set<DataProvider> providers = units.get(key);

        return new DataManagerImpl(providers, cachePool);
    }

    @Override
    public void destroy(final DataManager dm) throws IllegalStateException, IllegalArgumentException, DataException
    {
        checkActive();
        checkShutdownInProgress();

        if (dm == null)
            throw new IllegalArgumentException("Data manager is null");

        if (dm.isOpen())
            dm.close();
    }

    @Override
    public DataManagerFactory<DataManager> activate()
            throws IllegalStateException, NotEnoughResourceException, DataException
    {
        checkShutdownInProgress();

        if (active.compareAndSet(false, true))
        {
            /* CACHES initialization */
            try
            {
                for (CacheEntry centry : configuration.caches())
                {
                    if (centry.level() == CACHE_LEVEL_L1)
                    {
                        cachePool.allocate(centry);
                    }
                    else if (centry.level() == CACHE_LEVEL_L2)
                    {
                        // TODO initialize L2 caches
                    }
                    else if (centry.level() == CACHE_LEVEL_L3)
                    {
                        // TODO initialize L3 caches
                    }
                }
            }
            catch (final Exception e)
            {
                abortActivation();
                throw new DataException("Could not initialize all caches; activation aborted", e);
            }

            /* SCHEMAS initialization */
            try
            {
                /* SCHEMAS initialization */
                for (SchemaEntry sentry : configuration.schemas())
                {
                    for (EntityEntry eentry : sentry.entities().values())
                    {
                        final Object entity = loadType(eentry.type());
                        final String cacheId = parseL1CacheId(entity.getClass(), eentry, sentry, null, null);
                        if (cacheId != null)
                            cachePool.attach(entity.getClass(), cacheId);
                    }
                }
            }
            catch (final Exception e)
            {
                abortActivation();
                throw new DataException("Could not initialize all schemas; activation aborted", e);
            }

            /* STORAGE UNITS initialization */
            try
            {
                for (StorageUnitEntry suentry : configuration.units())
                {
                    final Set<DataProvider> unitProviders = units.containsKey(suentry.id()) ? units.get(suentry.id())
                            : new HashSet<DataProvider>();
                    /* PROVIDERS initialization */
                    for (ProviderEntry pentry : suentry.providers().values())
                    {
                        final DataProvider provider = createProvider(pentry);
                        injectProperties(provider, pentry.properties());
                        provider.setReadOnly(pentry.readOnly());
                        provider.activate();

                        unitProviders.add(provider);

                        /* Cache id override */
                        for (SchemaEntry sentry : suentry.schemas().values())
                        {
                            for (EntityEntry eentry : sentry.entities().values())
                            {
                                Object entity = loadType(eentry.type());
                                String cacheId = parseL1CacheId(entity.getClass(), eentry, sentry, suentry, pentry);
                                if (cacheId != null)
                                    cachePool.attach(entity.getClass(), cacheId);
                            }
                        }
                    }
                    units.put(suentry.id().toLowerCase(), unitProviders);
                    if (suentry.defaultUnit()) // Put again with the alias 'default'
                        units.put(DEFAULT_STORAGE_UNIT_KEY, unitProviders);
                }
            }
            catch (final Exception e)
            {
                abortActivation();
                throw new DataException("Could not initialize all storage units; activation aborted", e);
            }
        }
        else
        {
            throw new IllegalStateException("Illegal invocation; this factory is already active");
        }

        return this;
    }

    @Override
    public DataManagerFactory<DataManager> shutdown(final long graceTime, final TimeUnit unit)
            throws IllegalArgumentException, IllegalStateException, DataException
    {
        checkActive();
        checkShutdownInProgress(); // panic check

        if (graceTime < 0)
            throw new IllegalArgumentException("Invalid grace time value; cannot be negative");
        if (unit == null)
            throw new IllegalArgumentException("Invalid grace time unit value; cannot be null");

        if (shutdownInProgress.compareAndSet(false, true))
        {
            try
            {
                for (Set<DataProvider> providers : units.values())
                {
                    for (DataProvider provider : providers)
                    {
                        if (provider.isActive())
                        {
                            provider.shutdown(graceTime, unit);
                        }
                    }
                }
            }
            catch (final Exception e)
            {
                throw new DataException("Cannot shutdown this factory", e);
            }

            cachePool.close();

            active.set(false);
        }

        return this;
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

    /**
     * Checks if this provider is currently inactive.
     * 
     * @throws IllegalStateException
     *             if it is active
     */
    private void checkInactive() throws IllegalStateException
    {
        if (active.get())
            throw new IllegalStateException("Illegal invocation; this factory is already active");
    }

    /**
     * Checks if this provider is currently active.
     * 
     * @throws IllegalStateException
     *             if it is inactive
     */
    private void checkActive() throws IllegalStateException
    {
        if (!active.get())
            throw new IllegalStateException("Illegal invocation; this factory is inactive");
    }

    /**
     * Checks if the shutdown is already in progress.
     * 
     * @throws IllegalStateException
     *             when the shutdown is already in progress
     */
    private void checkShutdownInProgress() throws IllegalStateException
    {
        if (shutdownInProgress.get())
            throw new IllegalStateException("Illegal invocation; shutdown is already in progress");
    }

    /**
     * 
     * @param type
     * @param eentry
     * @param sentry
     * @param suentry
     * @param pentry
     * @return
     */
    private String parseL1CacheId(final Class<?> type, final EntityEntry eentry, final SchemaEntry sentry,
            final StorageUnitEntry suentry, final ProviderEntry pentry)
    {
        String cacheId = null;

        /* First we get what the entity meta data says */
        boolean cacheable = EntityUtils.info(type).cacheable();

        /* Than we override it when necessary to obey what the XML says */
        if (eentry != null && eentry.cacheable() != null)
            cacheable = eentry.cacheable();

        if (cacheable)
        {
            /* The order is: entity, schema, storage unit and provider */
            if (eentry != null && eentry.cacheId() != null)
                cacheId = eentry.cacheId();
            if (suentry != null && suentry.cacheId() != null)
                cacheId = suentry.cacheId();
            if (pentry != null && pentry.cacheId() != null)
                cacheId = pentry.cacheId();
        }

        /* If no cache was set try to return default cache if it exists */
        return cacheable && cacheId == null ? configuration.defaultCaheId(CACHE_LEVEL_L1) : cacheId;
    }

    /**
     * Creates a new concrete {@link StorageProvider} object based on the given settings.
     * 
     * @param providerEntry
     *            the settings entry for the provider to be created
     * 
     * @return a brand new {@link StorageProvider} object
     * 
     * @throws NotEnoughResourceException
     *             if there are no resources available for the new provider
     * @throws DataProviderException
     *             if there are errors upon the provider instantiation
     */
    private DataProvider createProvider(final ProviderEntry entry)
            throws IllegalAccessException, NotEnoughResourceException, DataProviderException
    {
        final DataProvider provider = (DataProvider) loadType(entry.type());

        return provider;
    }

    private Object loadType(final String name) throws IllegalAccessException
    {
        Object instance;

        try
        {
            Class<?> clazz = Class.forName(name);
            instance = clazz.newInstance();
        }
        catch (final ClassNotFoundException e)
        {
            throw new IllegalAccessException(e.getMessage());
        }
        catch (final InstantiationException e)
        {
            throw new IllegalAccessException(e.getMessage());
        }

        return instance;
    }

    /**
     * Injects values into fields decorated with {@link StorageProperty} annotation.
     * 
     * @param injectee
     *            the point of injection
     * @param props
     *            the set of properties
     * 
     * @throws IllegalArgumentException
     *             if there is any constraint violation (e.g. nullable)
     * @throws StorageException
     *             if cannot inject the properties
     */
    private void injectProperties(final Object injectee, final Map<String, String> props)
            throws IllegalArgumentException, DataException
    {
        final Settings settings = new Settings();
        settings.load(props);
        settings.inject(injectee);
    }

    /**
     * Aborts the activation process.
     */
    private void abortActivation()
    {
        try
        {
            for (Set<DataProvider> providers : units.values())
            {
                for (DataProvider provider : providers)
                {
                    if (provider.isActive())
                    {
                        provider.shutdown(1L, TimeUnit.SECONDS);
                    }
                }
            }
            cachePool.close();
            active.set(false);
        }
        catch (final Exception e)
        {
            throw new DataProviderException("Cannot shutdown this factory", e);
        }
    }
}