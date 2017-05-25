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
 * Provider setting entry.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public class CacheNodeEntry
{
    private String id;
    private String type;
    private Map<String, String> properties;

    public CacheNodeEntry()
    {
        id = null;
        type = null;
        properties = new HashMap<>();
    }

    public String id()
    {
        return id;
    }

    public CacheNodeEntry id(String id)
    {
        this.id = id;
        return this;
    }

    public String type()
    {
        return type;
    }

    public CacheNodeEntry type(String type)
    {
        this.type = type;
        return this;
    }

    public Map<String, String> properties()
    {
        return properties;
    }

    public CacheNodeEntry properties(Map<String, String> properties)
    {
        this.properties = properties;
        return this;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
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
        CacheNodeEntry other = (CacheNodeEntry) obj;
        if (id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!id.equals(other.id))
            return false;
        if (properties == null)
        {
            if (other.properties != null)
                return false;
        }
        else if (!properties.equals(other.properties))
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
        return "CacheNodeEntry [id=" + id + ", type=" + type + ", properties=" + properties + "]";
    }
}