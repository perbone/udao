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

package io.perbone.udao.annotation;

/**
 * Data type enumeration.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public enum DataType
{
    /**
     * UNKNOWN means "no data type defined" so will try to guess from host data type. Used as
     * default data type in design time.
     */
    UNKNOWN,

    /**
     * Void.
     */
    VOID,

    /**
     * PRIMITIVE TYPES
     * <p>
     * Type Contains Default Size Range
     * 
     * <pre>
     * BOOLEAN true or false false 1 bit NA 
     * CHAR Unicode character \u0000 16 bits \u0000 to \uFFFF
     * BYTE Signed integer 0 8 bits -128 to 127 
     * SHORT Signed integer 0 16 bits -32768 to 32767 
     * INT Signed integer 0 32 bits -2147483648 to 2147483647 
     * LONG Signed integer 0 64 bits -9223372036854775808 to 9223372036854775807
     * BIGINTEGER 
     * FLOAT IEEE 754 floating point 0.0 32 bits +-1.4E-45 to +-3.4028235E+38 
     * DOUBLE IEEE 754 floating point 0.0 64 bits +-.9E-324 to +-.7976931348623157E+308
     * BIGDECIMAL
     * </pre>
     * 
     * Any primitive type can be defined as an array
     */

    BOOLEAN, CHAR, BYTE, SHORT, INT, LONG, BIGINTEGER, FLOAT, DOUBLE, BIGDECIMAL,

    /**
     * Date and time types
     */
    DATE, TIME, TIMESTAMP,

    /**
     * COMPLEX TYPES
     * <p>
     * Type Contain
     * 
     * <pre>
     * BYTES Alias for array of BYTE 
     * DATE Alias for LONG 
     * STRING Alias for array of CHAR 
     * ID Alias for bytes (current implementation uses the string representation of UUID - 32 bytes) 
     * REF Alias for ID, used to references another resource as a child 
     * LIST Alias for array of REF 
     * ENUM Alias for array of STRING, with all possible values defined on schema creation 
     * TUPLE Collection of key/data values. The collection map cannot contain duplicate keys; each key can map to at
     * most one value. The value type can be any other type supported.
     * </pre>
     */

    BYTES, STRING, ID, REF, LIST, ENUM, TUPLE, SET, MAP
}