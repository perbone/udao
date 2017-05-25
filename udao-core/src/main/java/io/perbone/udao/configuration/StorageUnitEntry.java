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

import java.util.HashMap;
import java.util.Map;

/**
 * Storage unit setting entry.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public class StorageUnitEntry
{
    private String id;
    private String uri;
    private Boolean defaultUnit;
    private String cacheId;
    private Map<String, SchemaEntry> schemas;
    private Map<String, ProviderEntry> providers;

    public StorageUnitEntry()
    {
        id = null;
        uri = null;
        defaultUnit = false;
        cacheId = null;
        schemas = new HashMap<>();
        providers = new HashMap<>();
    }

    public String id()
    {
        return id;
    }

    public StorageUnitEntry id(String id)
    {
        this.id = id;
        return this;
    }

    public String uri()
    {
        return uri;
    }

    public StorageUnitEntry uri(String uri)
    {
        this.uri = uri;
        return this;
    }

    public Boolean defaultUnit()
    {
        return defaultUnit;
    }

    public StorageUnitEntry defaultUnit(Boolean defaultUnit)
    {
        this.defaultUnit = defaultUnit;
        return this;
    }

    public String cacheId()
    {
        return cacheId;
    }

    public StorageUnitEntry cacheId(String cacheId)
    {
        this.cacheId = cacheId;
        return this;
    }

    public Map<String, SchemaEntry> schemas()
    {
        return schemas;
    }

    public StorageUnitEntry schemas(Map<String, SchemaEntry> schemas)
    {
        this.schemas = schemas;
        return this;
    }

    public Map<String, ProviderEntry> providers()
    {
        return providers;
    }

    public StorageUnitEntry providers(Map<String, ProviderEntry> providers)
    {
        this.providers = providers;
        return this;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cacheId == null) ? 0 : cacheId.hashCode());
        result = prime * result + ((defaultUnit == null) ? 0 : defaultUnit.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((providers == null) ? 0 : providers.hashCode());
        result = prime * result + ((schemas == null) ? 0 : schemas.hashCode());
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StorageUnitEntry other = (StorageUnitEntry) obj;
        if (cacheId == null)
        {
            if (other.cacheId != null)
                return false;
        }
        else if (!cacheId.equals(other.cacheId))
            return false;
        if (defaultUnit == null)
        {
            if (other.defaultUnit != null)
                return false;
        }
        else if (!defaultUnit.equals(other.defaultUnit))
            return false;
        if (id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!id.equals(other.id))
            return false;
        if (providers == null)
        {
            if (other.providers != null)
                return false;
        }
        else if (!providers.equals(other.providers))
            return false;
        if (schemas == null)
        {
            if (other.schemas != null)
                return false;
        }
        else if (!schemas.equals(other.schemas))
            return false;
        if (uri == null)
        {
            if (other.uri != null)
                return false;
        }
        else if (!uri.equals(other.uri))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "StorageUnitEntry [id=" + id + ", uri=" + uri + ", defaultUnit=" + defaultUnit + ", cacheId=" + cacheId
                + ", schemas=" + schemas + ", providers=" + providers + "]";
    }
}