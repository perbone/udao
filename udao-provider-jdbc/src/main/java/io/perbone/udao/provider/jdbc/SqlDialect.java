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

package io.perbone.udao.provider.jdbc;

/**
 * List of known dialects.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public enum SqlDialect
{
    UNKNOWN, DB2, DERBY, FIREBIRD, HSQLDB, MSSQL, MYSQL, ORACLE, POSTGRESQL, SYBASE, TSQL, JEJDBC;

    public static SqlDialect parse(final String value)
    {
        if (value == null)
            return UNKNOWN;

        String lower = value.toLowerCase();

        if (lower.indexOf("db2") != -1)
            return DB2;
        else if (lower.indexOf("derby") != -1)
            return DERBY;
        else if (lower.indexOf("firebird") != -1)
            return FIREBIRD;
        else if (lower.indexOf("hsqldb") != -1)
            return HSQLDB;
        else if (lower.indexOf("mssql") != -1)
            return MSSQL;
        else if (lower.indexOf("mysql") != -1)
            return MYSQL;
        else if (lower.indexOf("oracle") != -1)
            return ORACLE;
        else if (lower.indexOf("postgresql") != -1)
            return POSTGRESQL;
        else if (lower.indexOf("sybase") != -1)
            return SYBASE;
        else if (lower.indexOf("net.sourceforge.jtds") != -1)
            return TSQL;
        else if (lower.indexOf("jejdbc") != -1)
            return JEJDBC;
        else
            return UNKNOWN;
    }
}