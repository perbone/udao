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

package io.perbone.udao.configuration;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

import io.perbone.mkey.EvictionPolicy;
import io.perbone.toolbox.settings.BackingStoreException;
import io.perbone.toolbox.settings.InvalidSettingsException;

/**
 * Configuration utility class.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public class Configuration
{
    private final Map<String, CacheEntry> caches;
    private final Map<String, SchemaEntry> schemas;
    private final Map<String, ProviderEntry> providers;
    private final Map<String, StorageUnitEntry> units;
    private final Map<Integer, String> defaultCacheIds;

    /**
     * Creates a {@code StorageSettings} with default values.
     */
    public Configuration()
    {
        caches = new ConcurrentHashMap<>();
        schemas = new ConcurrentHashMap<>();
        providers = new ConcurrentHashMap<>();
        units = new ConcurrentHashMap<>();
        defaultCacheIds = new ConcurrentHashMap<>();
    }

    public CacheEntry cache(String id)
    {
        CacheEntry cache = caches.get(id);
        return cache == null ? new CacheEntry() : cache;
    }

    public Configuration cache(String id, CacheEntry cache)
    {
        caches.put(id, cache);
        return this;
    }

    public String defaultCaheId(int level)
    {
        return defaultCacheIds.get(level);
    }

    public final Collection<CacheEntry> caches()
    {
        return Collections.unmodifiableCollection(caches.values());
    }

    public final Collection<SchemaEntry> schemas()
    {
        return Collections.unmodifiableCollection(schemas.values());
    }

    /**
     * Loads the settings from the given store.
     * 
     * @param path
     *            the backing store path
     * 
     * @throws IllegalArgumentException
     *             if the path is invalid
     * @throws BackingStoreException
     *             if the backing store is invalid (not found, unreachable, etc)
     * @throws InvalidSettingsException
     *             if the settings from the store are invalid
     */
    public void load(String path) throws IllegalArgumentException, BackingStoreException, InvalidSettingsException
    {
        if (path == null)
            throw new IllegalArgumentException("Invalid preference path; cannot be null");

        try
        {
            XMLConfiguration config = new XMLConfiguration(path);
            parseSettings(config);
        }
        catch (ConfigurationException e)
        {
            throw new BackingStoreException(
                    String.format("Settings store [%s] could not be found or is unreachable", path));
        }
    }

    public ProviderEntry provider(String id)
    {
        ProviderEntry provider = providers.get(id);
        return provider == null ? new ProviderEntry() : provider;
    }

    public Configuration provider(String id, ProviderEntry provider)
    {
        providers.put(id, provider);
        return this;
    }

    public final Collection<ProviderEntry> providers()
    {
        return Collections.unmodifiableCollection(providers.values());
    }

    public StorageUnitEntry unit(String id)
    {
        StorageUnitEntry unit = units.get(id);
        return unit == null ? new StorageUnitEntry() : unit;
    }

    public Configuration unit(String id, StorageUnitEntry provider)
    {
        units.put(id, provider);
        return this;
    }

    public final Collection<StorageUnitEntry> units()
    {
        return Collections.unmodifiableCollection(units.values());
    }

    private final static String KEY_EXP_CACHES = "caches.cache";
    private final static String KEY_EXP_ID = "[@id]";
    private final static String KEY_EXP_BACKEND_NAME = "[@backend-name]";
    private final static String KEY_EXP_TYPE = "[@type]";
    private final static String KEY_EXP_HARD_LIMIT_SIZE = "hard-limit-size";
    private final static String KEY_EXP_TTL_VALUE = "time-to-live";
    private final static String KEY_EXP_TTL_UNIT = "time-to-live[@unit]";
    private final static String KEY_EXP_EVICTION_POLICY = "eviction-policy";
    private final static String KEY_EXP_LEVEL = "[@level]";
    private final static String KEY_EXP_URI = "[@uri]";
    private final static String KEY_EXP_DEFAULT = "[@default]";
    private final static String KEY_EXP_ENTITY = "entity";
    private final static String KEY_EXP_NODES = "nodes.node";
    private final static String KEY_EXP_DIRTYCHECKING = "[@dirty-checking]";
    private final static String KEY_EXP_CACHEABLE = "[@cacheable]";
    private final static String KEY_EXP_PROPERTIES = "properties.property";
    private final static String KEY_EXP_NAME = "[@name]";
    private final static String KEY_EXP_VALUE = "[@value]";
    private final static String KEY_EXP_SCHEMAS = "schemas.schema";
    private final static String KEY_EXP_PROVIDERS = "providers.provider";
    private final static String KEY_EXP_READ_ONLY = "[@read-only]";
    private final static String KEY_EXP_MANAGED_BEANS_ONLY = "[@managed-beans-only]";
    private final static String KEY_EXP_CACHE_ID = "[@cache-id]";
    private final static String KEY_EXP_STORAGE_UNITS = "storage-units.storage-unit";

    /**
     * Parses the settings an populates the internal state.
     * <p>
     * Some missing property will be filled with default values.
     * 
     * @param config
     *            the configuration to parse
     * 
     * @throws InvalidSettingsException
     *             if the preference store is invalid
     */
    private void parseSettings(XMLConfiguration config) throws InvalidSettingsException
    {
        try
        {
            /* First level caches */
            parseCacheEntries(config.configurationsAt(KEY_EXP_CACHES));

            /* First level schemas */
            parseSchemaEntries(config.configurationsAt(KEY_EXP_SCHEMAS));

            /* First level storage units */
            parseStorageUnitEntries(config.configurationsAt(KEY_EXP_STORAGE_UNITS));
        }
        catch (Exception e)
        {
            throw new InvalidSettingsException("Invalid settings", e);
        }
    }

    /**
     * Parses cache entries.
     * <p>
     * Missing property will be filled with default values coming from
     * {@link CacheEntry#CacheEntry()} constructor.
     * 
     * @param cacheNodes
     *            cache nodes to be parsed
     * 
     * @throws InvalidSettingsException
     *             if the nodes contains invalid settings
     */
    private void parseCacheEntries(List<?> cacheNodes) throws InvalidSettingsException
    {
        for (Iterator<?> it = cacheNodes.iterator(); it.hasNext();)
        {
            HierarchicalConfiguration cacheNode = (HierarchicalConfiguration) it.next();

            String id = cacheNode.getString(KEY_EXP_ID);

            CacheEntry cacheEntry = caches.containsKey(id) ? caches.get(id) : new CacheEntry();

            Integer level = cacheNode.containsKey(KEY_EXP_LEVEL) ? cacheNode.getInt(KEY_EXP_LEVEL) : cacheEntry.level();
            Boolean defaultCache = cacheNode.containsKey(KEY_EXP_DEFAULT)
                    ? Boolean.valueOf(cacheNode.getString(KEY_EXP_DEFAULT)) : cacheEntry.defaultCache();
            Long hardLimitSize = cacheNode.containsKey(KEY_EXP_HARD_LIMIT_SIZE)
                    ? (Long) cacheNode.getLong(KEY_EXP_HARD_LIMIT_SIZE) : // WARNING! do not remove
                                                                          // this casting!
                    cacheEntry.hardLimitSize();
            Long ttl = cacheNode.containsKey(KEY_EXP_TTL_VALUE) ? cacheNode.getLong(KEY_EXP_TTL_VALUE)
                    : cacheEntry.ttl();
            TimeUnit unit = cacheNode.containsKey(KEY_EXP_TTL_UNIT)
                    ? TimeUnit.valueOf(cacheNode.getString(KEY_EXP_TTL_UNIT)) : cacheEntry.unit();
            EvictionPolicy evictionPolicy = cacheNode.containsKey(KEY_EXP_EVICTION_POLICY)
                    ? EvictionPolicy.valueOf(EvictionPolicy.class, cacheNode.getString(KEY_EXP_EVICTION_POLICY))
                    : cacheEntry.evictionPolicy();

            /* Parses child nodes */
            Map<String, CacheNodeEntry> nodes = parseNodeEntries(cacheNode, cacheEntry.nodes());

            cacheEntry
                    .id(id)
                    .level(level)
                    .defaultCache(defaultCache)
                    .hardLimitSize(hardLimitSize)
                    .ttl(ttl)
                    .unit(unit)
                    .evictionPolicy(evictionPolicy)
                    .nodes(nodes);

            /* Updates caches set */
            caches.put(id, cacheEntry);

            /* Updates default cache id for current entry */
            if (defaultCache)
            {
                String cur = defaultCacheIds.put(level, id);
                if (cur != null && !cur.equals(id))
                    throw new InvalidSettingsException(String.format(
                            "Trying to set cache-id [%s] as default cache for level [%d] but cache-id [%s] is already set for it",
                            id, level, cur));
            }
        }
    }

    /**
     * Parses property entries.
     * 
     * @param parentNode
     *            parent node for all property nodes to be parsed
     * @param properties
     *            the set to be updated
     * 
     * @return an updated properties set
     * 
     * @throws InvalidSettingsException
     *             if the nodes contains invalid settings
     */
    private Map<String, String> parsePropertyEntries(HierarchicalConfiguration parentNode,
            Map<String, String> properties) throws InvalidSettingsException
    {
        List<?> propertyNodes = parentNode.configurationsAt(KEY_EXP_PROPERTIES);

        for (Iterator<?> it = propertyNodes.iterator(); it.hasNext();)
        {
            HierarchicalConfiguration propertyNode = (HierarchicalConfiguration) it.next();

            String name = propertyNode.getString(KEY_EXP_NAME);
            String value = propertyNode.getString(KEY_EXP_VALUE);

            properties.put(name, value);
        }

        return properties;
    }

    /**
     * Parses entity entries.
     * 
     * @param cacheNode
     *            the parent node for all entity nodes to be parsed
     * @param cacheNodes
     *            the set to be updated
     * 
     * @return an updated entity set
     * 
     * @throws InvalidSettingsException
     *             if the nodes contains invalid settings
     */
    private Map<String, CacheNodeEntry> parseNodeEntries(HierarchicalConfiguration cacheNode,
            Map<String, CacheNodeEntry> cacheNodes) throws InvalidSettingsException
    {
        List<?> cacheNodeNodes = cacheNode.configurationsAt(KEY_EXP_NODES);

        for (Iterator<?> it = cacheNodeNodes.iterator(); it.hasNext();)
        {
            HierarchicalConfiguration cacheNodeNode = (HierarchicalConfiguration) it.next();

            String id = cacheNodeNode.getString(KEY_EXP_ID);

            CacheNodeEntry cacheNodeEntry = cacheNodes.containsKey(id) ? cacheNodes.get(id) : new CacheNodeEntry();

            String type = cacheNodeNode.containsKey(KEY_EXP_TYPE) ? cacheNodeNode.getString(KEY_EXP_TYPE)
                    : cacheNodeEntry.type();

            /* Parses child properties */
            Map<String, String> properties = parsePropertyEntries(cacheNodeNode, cacheNodeEntry.properties());

            cacheNodeEntry.id(id).type(type).properties(properties);

            cacheNodes.put(id, cacheNodeEntry);
        }

        return cacheNodes;
    }

    /**
     * Parses entity entries.
     * 
     * @param schemaNode
     *            the parent node for all entity nodes to be parsed
     * @param entities
     *            the set to be updated
     * 
     * @return an updated entity set
     * 
     * @throws InvalidSettingsException
     *             if the nodes contains invalid settings
     */
    private Map<String, EntityEntry> parseEntityEntries(HierarchicalConfiguration schemaNode,
            Map<String, EntityEntry> entities) throws InvalidSettingsException
    {
        List<?> dataSourceNodes = schemaNode.configurationsAt(KEY_EXP_ENTITY);

        for (Iterator<?> it = dataSourceNodes.iterator(); it.hasNext();)
        {
            HierarchicalConfiguration entityNode = (HierarchicalConfiguration) it.next();

            String id = entityNode.getString(KEY_EXP_ID);

            EntityEntry entityEntry = entities.containsKey(id) ? entities.get(id) : new EntityEntry();

            String type = entityNode.containsKey(KEY_EXP_TYPE) ? entityNode.getString(KEY_EXP_TYPE)
                    : entityEntry.type();

            Boolean dirtyChecking = entityNode.containsKey(KEY_EXP_DIRTYCHECKING)
                    ? Boolean.valueOf(entityNode.getString(KEY_EXP_DIRTYCHECKING)) : entityEntry.dirtyChecking();

            Boolean cacheable = entityNode.containsKey(KEY_EXP_CACHEABLE)
                    ? Boolean.valueOf(entityNode.getString(KEY_EXP_CACHEABLE)) : entityEntry.cacheable();

            String cacheId = entityNode.containsKey(KEY_EXP_CACHE_ID) ? entityNode.getString(KEY_EXP_CACHE_ID)
                    : entityEntry.cacheId();

            entityEntry
                .id(id)
                .type(type)
                .dirtyChecking(dirtyChecking)
                .cacheable(cacheable)
                .cacheId(cacheId);

            entities.put(id, entityEntry);
        }

        return entities;
    }

    /**
     * Parses schema entries.
     * 
     * @param schemaNodes
     *            provider nodes to be parsed
     * 
     * @return the list of parsed schemas
     * 
     * @throws InvalidSettingsException
     *             if the nodes contains invalid settings
     */
    private Map<String, SchemaEntry> parseSchemaEntries(List<?> schemaNodes) throws InvalidSettingsException
    {
        Map<String, SchemaEntry> updatedSchemas = new Hashtable<>();

        for (Iterator<?> it = schemaNodes.iterator(); it.hasNext();)
        {
            HierarchicalConfiguration schemaNode = (HierarchicalConfiguration) it.next();

            String id = schemaNode.getString(KEY_EXP_ID);

            SchemaEntry schemaEntry = schemas.containsKey(id) ? schemas.get(id) : new SchemaEntry();

            /* Parses child entities */
            Map<String, EntityEntry> entities = parseEntityEntries(schemaNode, schemaEntry.entities());

            schemaEntry.id(id).entities(entities);

            updatedSchemas.put(id, schemaEntry);

            /* Updates schemas set */
            schemas.put(id, schemaEntry);
        }

        return updatedSchemas;
    }

    /**
     * Parses provider entries.
     * 
     * @param providerNodes
     *            provider nodes to be parsed
     * @param unitProviders
     *            the unit's providers
     * 
     * @return the list of parsed providers
     * 
     * @throws InvalidSettingsException
     *             if the nodes contains invalid settings
     */
    private Map<String, ProviderEntry> parseProviderEntries(List<?> providerNodes,
            Map<String, ProviderEntry> unitProviders) throws InvalidSettingsException
    {
        for (Iterator<?> it = providerNodes.iterator(); it.hasNext();)
        {
            HierarchicalConfiguration providerNode = (HierarchicalConfiguration) it.next();

            String id = providerNode.getString(KEY_EXP_ID);

            ProviderEntry providerEntry = unitProviders.containsKey(id) ? unitProviders.get(id)
                    : providers.containsKey(id) ? providers.get(id) : new ProviderEntry();

            String backendName = providerNode.containsKey(KEY_EXP_BACKEND_NAME)
                    ? providerNode.getString(KEY_EXP_BACKEND_NAME) : providerEntry.backendName();
            String type = providerNode.containsKey(KEY_EXP_TYPE) ? providerNode.getString(KEY_EXP_TYPE)
                    : providerEntry.type();
            Boolean readOnly = providerNode.containsKey(KEY_EXP_READ_ONLY)
                    ? Boolean.valueOf(providerNode.getString(KEY_EXP_READ_ONLY))
                    : (providerEntry.readOnly() == null ? false : Boolean.valueOf(providerEntry.readOnly()));
            Boolean managedBeansOnly = providerNode.containsKey(KEY_EXP_MANAGED_BEANS_ONLY)
                    ? Boolean.valueOf(providerNode.getString(KEY_EXP_MANAGED_BEANS_ONLY))
                    : (providerEntry.managedBeansOnly() == null ? false
                            : Boolean.valueOf(providerEntry.managedBeansOnly()));
            String cacheId = providerNode.containsKey(KEY_EXP_CACHE_ID) ? providerNode.getString(KEY_EXP_CACHE_ID)
                    : providerEntry.cacheId();
            /* Parses child properties */
            Map<String, String> properties = parsePropertyEntries(providerNode, providerEntry.properties());

            providerEntry
                    .id(id)
                    .backendName(backendName)
                    .type(type)
                    .readOnly(readOnly)
                    .managedBeansOnly(managedBeansOnly)
                    .cacheId(cacheId)
                    .properties(properties);

            unitProviders.put(id, providerEntry);

            /* Updates providers set */
            providers.put(id, providerEntry);
        }

        return unitProviders;
    }

    /**
     * Parses storage unit entries.
     * 
     * @param unitNodes
     *            storage unit nodes to be parsed
     * 
     * @throws InvalidSettingsException
     *             if the nodes contains invalid settings
     */
    private void parseStorageUnitEntries(List<?> unitNodes) throws InvalidSettingsException
    {
        for (Iterator<?> it = unitNodes.iterator(); it.hasNext();)
        {
            HierarchicalConfiguration unitNode = (HierarchicalConfiguration) it.next();

            String id = unitNode.getString(KEY_EXP_ID);

            StorageUnitEntry unitEntry = units.containsKey(id) ? units.get(id) : new StorageUnitEntry();

            String uri = unitNode.containsKey(KEY_EXP_URI) ? unitNode.getString(KEY_EXP_URI) : unitEntry.uri();
            Boolean defaultUnit = unitNode.containsKey(KEY_EXP_DEFAULT)
                    ? Boolean.valueOf(unitNode.getString(KEY_EXP_DEFAULT)) : unitEntry.defaultUnit();
            String cacheId = unitNode.containsKey(KEY_EXP_CACHE_ID) ? unitNode.getString(KEY_EXP_CACHE_ID)
                    : unitEntry.cacheId();

            /* Parses child schemas */
            Map<String, SchemaEntry> schemas = parseSchemaEntries(unitNode.configurationsAt(KEY_EXP_SCHEMAS));
            /* Parses child providers */
            Map<String, ProviderEntry> providers = parseProviderEntries(unitNode.configurationsAt(KEY_EXP_PROVIDERS),
                    unitEntry.providers());

            unitEntry.id(id).uri(uri).defaultUnit(defaultUnit).cacheId(cacheId).schemas(schemas).providers(providers);

            /* Updates storage units set */
            units.put(id, unitEntry);
        }
    }
}