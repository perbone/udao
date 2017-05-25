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

/**
 * Bean setting entry.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public class EntityEntry
{
    private String id;
    private String type;
    private Boolean dirtyChecking;
    private Boolean cacheable;
    private String cacheId;

    public EntityEntry()
    {
        id = null;
        type = null;
        dirtyChecking = null;
        cacheable = null;
        cacheId = null;
    }

    public String id()
    {
        return id;
    }

    public EntityEntry id(String id)
    {
        this.id = id;
        return this;
    }

    public String type()
    {
        return type;
    }

    public EntityEntry type(String type)
    {
        this.type = type;
        return this;
    }

    public Boolean dirtyChecking()
    {
        return dirtyChecking;
    }

    public EntityEntry dirtyChecking(final Boolean dirtyChecking)
    {
        this.dirtyChecking = dirtyChecking;
        return this;
    }

    public Boolean cacheable()
    {
        return cacheable;
    }

    public EntityEntry cacheable(Boolean cacheable)
    {
        this.cacheable = cacheable;
        return this;
    }

    public String cacheId()
    {
        return cacheId;
    }

    public EntityEntry cacheId(String cacheId)
    {
        this.cacheId = cacheId;
        return this;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cacheId == null) ? 0 : cacheId.hashCode());
        result = prime * result + ((cacheable == null) ? 0 : cacheable.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        EntityEntry other = (EntityEntry) obj;
        if (cacheId == null)
        {
            if (other.cacheId != null)
                return false;
        }
        else if (!cacheId.equals(other.cacheId))
            return false;
        if (cacheable == null)
        {
            if (other.cacheable != null)
                return false;
        }
        else if (!cacheable.equals(other.cacheable))
            return false;
        if (id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!id.equals(other.id))
            return false;
        if (type == null)
        {
            if (other.type != null)
                return false;
        }
        else if (!type.equals(other.type))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "EntityEntry [id=" + id + ", type=" + type + ", cacheable=" + cacheable + ", cacheId=" + cacheId + "]";
    }
}