/*
 * This file is part of UDAO 
 * https://github.com/perbone/udao/
 * 
 * Copyright 2013-2018 Paulo Perbone
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
 * Data source setting entry.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public class SchemaEntry
{
    private String id;
    private Map<String, EntityEntry> entities;

    public SchemaEntry()
    {
        id = null;
        entities = new HashMap<>();
    }

    public String id()
    {
        return id;
    }

    public SchemaEntry id(String id)
    {
        this.id = id;
        return this;
    }

    public Map<String, EntityEntry> entities()
    {
        return entities;
    }

    public SchemaEntry entities(Map<String, EntityEntry> entities)
    {
        this.entities = entities;
        return this;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entities == null) ? 0 : entities.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        SchemaEntry other = (SchemaEntry) obj;
        if (entities == null)
        {
            if (other.entities != null)
                return false;
        }
        else if (!entities.equals(other.entities))
            return false;
        if (id == null)
        {
            if (other.id != null)
                return false;
        }
        else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString()
    {
        return "SchemaEntry [id=" + id + ", entities=" + entities + "]";
    }
}