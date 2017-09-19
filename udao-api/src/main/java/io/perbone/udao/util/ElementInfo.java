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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.perbone.udao.annotation.DataType;
import io.perbone.udao.annotation.InstanceType;
import io.perbone.udao.annotation.Metadata.MetadataType;

/**
 * Represents information about a bean element.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public final class ElementInfo
{
    private Class<?> type;
    private String name;
    private DataType dataType;
    private InstanceType instanceType;
    private Map<String, List<String>> aliases;
    private Boolean virtual;
    private Boolean metadata;
    private MetadataType metadataType;
    private Short index;
    private Boolean nullable;

    public ElementInfo()
    {
        type = null;
        name = null;
        dataType = null;
        instanceType = null;
        aliases = new ConcurrentHashMap<String, List<String>>();
        virtual = null;
        metadata = null;
        metadataType = null;
        index = null;
        nullable = null;
    }

    public Map<String, List<String>> aliases()
    {
        return aliases;
    }

    public ElementInfo aliases(final Map<String, List<String>> aliases)
    {
        this.aliases = aliases;
        return this;
    }

    public DataType dataType()
    {
        return dataType;
    }

    public ElementInfo dataType(final DataType dataType)
    {
        this.dataType = dataType;
        return this;
    }

    public String firstAliasForTarget(final String target)
    {
        if (aliases.containsKey(target))
            return aliases.get(target).get(0);
        else
            return null;
    }

    public InstanceType instanceType()
    {
        return instanceType;
    }

    public ElementInfo instanceType(final InstanceType instanceType)
    {
        this.instanceType = instanceType;
        return this;
    }

    public Boolean metadata()
    {
        return metadata;
    }

    public ElementInfo metadata(final Boolean metadata)
    {
        this.metadata = metadata;
        return this;
    }

    public MetadataType metadataType()
    {
        return metadataType;
    }

    public ElementInfo metadataType(final MetadataType metadataType)
    {
        this.metadataType = metadataType;
        return this;
    }

    public Short index()
    {
        return index;
    }

    public ElementInfo index(final Short index)
    {
        this.index = index;
        return this;
    }

    public Boolean nullable()
    {
        return nullable;
    }

    public ElementInfo nullable(final Boolean nullable)
    {
        this.nullable = nullable;
        return this;
    }

    public String name()
    {
        return name;
    }

    public ElementInfo name(final String name)
    {
        this.name = name;
        return this;
    }

    public Class<?> type()
    {
        return type;
    }

    public ElementInfo type(final Class<?> type)
    {
        this.type = type;
        return this;
    }

    public Boolean virtual()
    {
        return virtual;
    }

    public ElementInfo virtual(final Boolean virtual)
    {
        this.virtual = virtual;
        return this;
    }

    @Override
    public String toString()
    {
        return "ElementInfo [type=" + type + ", name=" + name + ", dataType=" + dataType + ", instanceType="
                + instanceType + ", aliases=" + aliases + ", virtual=" + virtual + ", metadata=" + metadata
                + ", metadataType=" + metadataType + "]";
    }
}