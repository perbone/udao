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

package io.perbone.udao.query;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Represents storage access query in its native dialect.
 * <p>
 * Can be used for any storage access operation as a filter for the targets.
 * <p>
 * This is a stateful object with one way through creation. With you made mistakes just trash it and
 * create another one.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public interface NativeQuery<T>
{
    Class<T> type();

    String query();

    NativeQuery<T> setNull(int offset);

    NativeQuery<T> setBoolean(int offset, boolean value);

    NativeQuery<T> setByte(int offset, byte value);

    NativeQuery<T> setShort(int offset, short value);

    NativeQuery<T> setInt(int offset, int value);

    NativeQuery<T> setLong(int offset, long value);

    NativeQuery<T> setFloat(int offset, float value);

    NativeQuery<T> setDouble(int offset, double value);

    NativeQuery<T> setBigDecimal(int offset, BigDecimal value);

    NativeQuery<T> setString(int offset, String value);

    NativeQuery<T> setBytes(int offset, byte value[]);

    NativeQuery<T> setDate(int offset, Date value);

    NativeQuery<T> setObject(int offset, Object value);
}