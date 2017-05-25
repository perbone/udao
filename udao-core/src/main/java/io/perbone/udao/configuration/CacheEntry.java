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
import java.util.concurrent.TimeUnit;

import io.perbone.mkey.EvictionPolicy;

/**
 * Cache setting entry.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public class CacheEntry
{
    private String id;
    private Integer level;
    private Boolean defaultCache;
    private Long hardLimitSize;
    private Long ttl;
    private TimeUnit unit;
    private EvictionPolicy evictionPolicy;
    private Map<String, CacheNodeEntry> nodes;

    public CacheEntry()
    {
        id = null;
        level = null;
        defaultCache = false;
        // hardLimitSize = 0L;
        hardLimitSize = null;
        ttl = null;
        unit = null;
        evictionPolicy = null;
        nodes = new HashMap<>();
    }

    public String id()
    {
        return id;
    }

    public CacheEntry id(String id)
    {
        this.id = id;
        return this;
    }

    public Integer level()
    {
        return level;
    }

    public CacheEntry level(Integer level)
    {
        this.level = level;
        return this;
    }

    public Boolean defaultCache()
    {
        return defaultCache;
    }

    public CacheEntry defaultCache(Boolean defaultCache)
    {
        this.defaultCache = defaultCache;
        return this;
    }

    public Long hardLimitSize()
    {
        return hardLimitSize;
    }

    public CacheEntry hardLimitSize(Long hardLimitSize)
    {
        this.hardLimitSize = hardLimitSize;
        return this;
    }

    public Long ttl()
    {
        return ttl;
    }

    public CacheEntry ttl(Long ttl)
    {
        this.ttl = ttl;
        return this;
    }

    public TimeUnit unit()
    {
        return unit;
    }

    public CacheEntry unit(TimeUnit unit)
    {
        this.unit = unit;
        return this;
    }

    public EvictionPolicy evictionPolicy()
    {
        return evictionPolicy;
    }

    public CacheEntry evictionPolicy(EvictionPolicy evictionPolicy)
    {
        this.evictionPolicy = evictionPolicy;
        return this;
    }

    public Map<String, CacheNodeEntry> nodes()
    {
        return nodes;
    }

    public CacheEntry nodes(Map<String, CacheNodeEntry> nodes)
    {
        this.nodes = nodes;
        return this;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((defaultCache == null) ? 0 : defaultCache.hashCode());
        result = prime * result + ((evictionPolicy == null) ? 0 : evictionPolicy.hashCode());
        result = prime * result + ((hardLimitSize == null) ? 0 : hardLimitSize.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((level == null) ? 0 : level.hashCode());
        result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
        result = prime * result + ((ttl == null) ? 0 : ttl.hashCode());
        result = prime * result + ((unit == null) ? 0 : unit.hashCode());
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
        CacheEntry other = (CacheEntry) obj;
        if (defaultCache == null)
        {
            if (other.defaultCache != null)
                return false;
        }
        else if (!defaultCache.equals(other.defaultCache))
            return false;
        if (evictionPolicy != other.evictionPolicy)
            return false;
        if (hardLimitSize == null)
        {
            if (other.hardLimitSize != null)
                return false;
        }
        else if (!hardLimitSize.equals(other.hardLimitSize))
            return false;
        if (id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!id.equals(other.id))
            return false;
        if (level == null)
        {
            if (other.level != null)
                return false;
        }
        else if (!level.equals(other.level))
            return false;
        if (nodes == null)
        {
            if (other.nodes != null)
                return false;
        }
        else if (!nodes.equals(other.nodes))
            return false;
        if (ttl == null)
        {
            if (other.ttl != null)
                return false;
        }
        else if (!ttl.equals(other.ttl))
            return false;
        if (unit != other.unit)
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "CacheEntry [id=" + id + ", level=" + level + ", defaultCache=" + defaultCache + ", hardLimitSize="
                + hardLimitSize + ", ttl=" + ttl + ", unit=" + unit + ", evictionPolicy=" + evictionPolicy + ", nodes="
                + nodes + "]";
    }
}