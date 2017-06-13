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

package io.perbone.udao.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import io.perbone.udao.annotation.ConsistencyLevel;
import io.perbone.udao.annotation.InstanceType;
import io.perbone.udao.annotation.PersistenceType;
import io.perbone.udao.annotation.Storable;

/**
 * Utility class for easy access of {@link Storable} bean object information.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public final class StorableInfo
{
    private Class<?> type;
    private String name;
    private Map<String, List<String>> aliases;
    private String version;
    private String domain;
    private String schema;
    private String resourceType;
    private String path;
    private Boolean authentication;
    private Boolean dirtyChecking;
    private Boolean cacheable;
    private Boolean sharedNothing;
    private PersistenceType persistenceType;
    private InstanceType defaultInstanceType;
    private ConsistencyLevel consistencyLevel;
    private ElementInfo surrogateKey;
    private List<ElementInfo> primaryKey;
    private Map<String, List<ElementInfo>> alternateKeys;
    private List<ElementInfo> elements;
    private List<ElementInfo> nonVirtualElements;

    public StorableInfo()
    {
        type = null;
        name = null;
        aliases = new ConcurrentHashMap<String, List<String>>();
        version = null;
        domain = null;
        schema = null;
        resourceType = null;
        path = null;
        authentication = null;
        dirtyChecking = null;
        cacheable = null;
        sharedNothing = null;
        persistenceType = null;
        defaultInstanceType = null;
        consistencyLevel = null;
        surrogateKey = null;
        primaryKey = new CopyOnWriteArrayList<ElementInfo>();
        alternateKeys = new ConcurrentHashMap<String, List<ElementInfo>>();
        elements = new CopyOnWriteArrayList<ElementInfo>();
    }

    public Class<?> type()
    {
        return type;
    }

    public StorableInfo type(final Class<?> type)
    {
        this.type = type;
        return this;
    }

    public Map<String, List<String>> aliases()
    {
        return aliases;
    }

    public StorableInfo aliases(final Map<String, List<String>> aliases)
    {
        this.aliases = aliases;
        return this;
    }

    public List<ElementInfo> alternateKey(final String name)
    {
        return alternateKeys.containsKey(name) ? alternateKeys.get(name) : new ArrayList<ElementInfo>();
    }

    public Map<String, List<ElementInfo>> alternateKeys()
    {
        return alternateKeys;
    }

    public StorableInfo alternateKeys(final Map<String, List<ElementInfo>> alternateKeys)
    {
        this.alternateKeys = alternateKeys;
        return this;
    }

    public Boolean authentication()
    {
        return authentication;
    }

    public StorableInfo authentication(final Boolean authentication)
    {
        this.authentication = authentication;
        return this;
    }

    public Boolean dirtyChecking()
    {
        return dirtyChecking;
    }

    public StorableInfo dirtyChecking(final Boolean dirtyChecking)
    {
        this.dirtyChecking = dirtyChecking;
        return this;
    }

    public Boolean cacheable()
    {
        return cacheable;
    }

    public StorableInfo cacheable(final Boolean cacheable)
    {
        this.cacheable = cacheable;
        return this;
    }

    public ConsistencyLevel consistencyLevel()
    {
        return consistencyLevel;
    }

    public StorableInfo consistencyLevel(final ConsistencyLevel consistencyLevel)
    {
        this.consistencyLevel = consistencyLevel;
        return this;
    }

    public InstanceType defaultInstanceType()
    {
        return defaultInstanceType;
    }

    public StorableInfo defaultInstanceType(final InstanceType defaultInstanceType)
    {
        this.defaultInstanceType = defaultInstanceType;
        return this;
    }

    public String domain()
    {
        return domain;
    }

    public StorableInfo domain(final String domain)
    {
        this.domain = domain;
        return this;
    }

    public List<ElementInfo> elements()
    {
        return elements;
    }

    public StorableInfo elements(final List<ElementInfo> elements)
    {
        this.elements = elements;
        return this;
    }

    public String firstAliasForTarget(final String target)
    {
        if (aliases.containsKey(target))
            return aliases.get(target).get(0);
        else
            return null;
    }

    public String name()
    {
        return name;
    }

    public StorableInfo name(final String name)
    {
        this.name = name;
        return this;
    }

    public List<ElementInfo> nonVirtualElements()
    {
        return nonVirtualElements;
    }

    public StorableInfo nonVirtualElements(final List<ElementInfo> nonVirtualElements)
    {
        this.nonVirtualElements = nonVirtualElements;
        return this;
    }

    public String path()
    {
        return path;
    }

    public StorableInfo path(final String path)
    {
        this.path = path;
        return this;
    }

    public PersistenceType persistenceType()
    {
        return persistenceType;
    }

    public StorableInfo persistenceType(final PersistenceType persistenceType)
    {
        this.persistenceType = persistenceType;
        return this;
    }

    public List<ElementInfo> primaryKey()
    {
        return primaryKey;
    }

    public StorableInfo primaryKey(final List<ElementInfo> primaryKey)
    {
        this.primaryKey = primaryKey;
        return this;
    }

    public String resourceType()
    {
        return resourceType;
    }

    public StorableInfo resourceType(final String resourceType)
    {
        this.resourceType = resourceType;
        return this;
    }

    public String schema()
    {
        return schema;
    }

    public StorableInfo schema(final String schema)
    {
        this.schema = schema;
        return this;
    }

    public Boolean sharedNothing()
    {
        return sharedNothing;
    }

    public StorableInfo sharedNothing(final Boolean sharedNothing)
    {
        this.sharedNothing = sharedNothing;
        return this;
    }

    public ElementInfo surrogateKey()
    {
        return surrogateKey;
    }

    public StorableInfo surrogateKey(final ElementInfo surrogateKey)
    {
        this.surrogateKey = surrogateKey;
        return this;
    }

    public String version()
    {
        return version;
    }

    public StorableInfo version(final String version)
    {
        this.version = version;
        return this;
    }
}