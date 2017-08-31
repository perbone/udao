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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.perbone.toolbox.annotation.AnnotationScanner;
import io.perbone.toolbox.collection.Pair;
import io.perbone.toolbox.formatter.HexFormatter;
import io.perbone.toolbox.formatter.NumberFormatter;
import io.perbone.toolbox.validation.StringValidations;
import io.perbone.udao.annotation.Alias;
import io.perbone.udao.annotation.Aliases;
import io.perbone.udao.annotation.AlternateKey;
import io.perbone.udao.annotation.AlternateKeys;
import io.perbone.udao.annotation.ConsistencyLevel;
import io.perbone.udao.annotation.DataType;
import io.perbone.udao.annotation.Element;
import io.perbone.udao.annotation.InstanceType;
import io.perbone.udao.annotation.Metadata;
import io.perbone.udao.annotation.PrimaryKey;
import io.perbone.udao.annotation.Storable;
import io.perbone.udao.annotation.SurrogateKey;
import io.perbone.udao.annotation.TimeToLive;
import io.perbone.udao.annotation.Virtual;

/**
 * Utility class that can be used to get information about the types and properties of the elements
 * in an object decorated with {@link Storable} annotation.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public final class EntityUtils
{
    public static final String CHARSET_UTF8 = "UTF-8";

    private static final Charset charset = Charset.forName(CHARSET_UTF8);

    private static final String KEY_ENCONDING = "ISO-8859-1";

    private static final String KEY_DIGEST_ALGORITHM = "SHA";

    /* Cache of types already scanned for StorableInfo annotation */
    private static final Map<Class<?>, StorableInfo> storablesCache = new ConcurrentHashMap<>();

    /* Cache of match fields already scanned */
    private static final Map<String, Field> fieldsCache = new ConcurrentHashMap<>();

    /* Cache of types with ttl fields */
    private static final Map<Class<?>, Field> ttlFields = new ConcurrentHashMap<>();

    /* Cache of types with ttl fields */
    private static final Map<Class<?>, Field> ttlUnitFields = new ConcurrentHashMap<>();

    /* Cache of MessageDigest instance */
    private static final AtomicReference<MessageDigest> messageDigestCache = new AtomicReference<>(null);

    private static String hostName = null;

    static
    {
        try
        {
            hostName = InetAddress.getLocalHost().getHostName();
        }
        catch (final UnknownHostException e)
        {
            hostName = "host name is unknown";
        }
    }

    /**
     * Retrieves the host name.
     * 
     * @return the host name
     */
    public static String hostName()
    {
        return hostName;
    }

    /**
     * Checks if the bean type is decorated with {@link Storable} annotation.
     * 
     * @param bean
     *            the bean object
     */
    public static boolean isStorable(final Object bean)
    {
        return bean == null ? false : isStorable(bean.getClass());
    }

    /**
     * Checks if the bean type is decorated with {@link Storable} annotation.
     * 
     * @param bean
     *            the bean type
     */
    public static boolean isStorable(final Class<?> type)
    {
        if (type == null)
            return false;

        try
        {
            checkStorable(type);
        }
        catch (final IllegalArgumentException e)
        {
            return false;
        }

        return true;
    }

    /**
     * Returns all the aliases names from a {@link Storable} bean class type.
     * 
     * @param type
     *            The {@link Storable} bean class type
     * 
     * @return A collection of all the bean class type aliases names
     * 
     * @throws IllegalArgumentException
     *             When the type is not an valid {@link Storable} class
     */
    public static Map<String, Set<Pair<String, String>>> aliases(final Class<?> type) throws IllegalArgumentException
    {
        // TODO implement this method

        return Collections.emptyMap();
    }

    /**
     * Returns all the aliases names from a {@link Storable} bean class type.
     * 
     * @param type
     *            The {@link Storable} bean class type
     * @param target
     *            The target name used as a filter
     * 
     * @return A collection of all the bean class type aliases names matching the filter
     * 
     * @throws IllegalArgumentException
     *             When the type is not an valid {@link Storable} class
     */
    public static Map<String, Set<Pair<String, String>>> aliases(final Class<?> type, final String target)
            throws IllegalArgumentException
    {
        // TODO implement this method

        return Collections.emptyMap();
    }

    /**
     * Returns all the elements names from a {@link Storable} bean class type.
     * 
     * @param type
     *            The {@link Storable} bean class type
     * 
     * @return A collection of all the bean class type elements names
     * 
     * @throws IllegalArgumentException
     *             When the type is not an valid {@link Storable} class
     */
    public static Set<String> elements(final Class<?> type) throws IllegalArgumentException
    {
        // TODO implement this method

        return Collections.emptySet();
    }

    /**
     * Extracts information from a {@link Storable} bean class type.
     * 
     * @param type
     *            the {@link Storable} bean class type
     * 
     * @return a {@link StorableInfo} object with information extracted from the bean class type
     * 
     * @throws IllegalArgumentException
     *             if the type is not an valid {@link Storable} class
     */
    public static StorableInfo info(final Class<?> type) throws IllegalArgumentException
    {
        checkStorable(type);

        StorableInfo sinfo = storablesCache.get(type);

        if (sinfo == null)
        {
            sinfo = new StorableInfo();

            final Storable storable = AnnotationScanner.getAnnotation(type, Storable.class);

            final String name = StringValidations.isValid(storable.name()) ? storable.name() : className(type);

            final Map<String, List<String>> aliases = parseAliases(storable.aliases());

            final String schema = StringValidations.isValid(storable.schema()) ? storable.schema() : null;

            final ConsistencyLevel consistencyLevel = storable.consistencyLevel();

            final ElementInfo surrogateKey = parseSurrogateKey(type);

            final List<ElementInfo> primaryKey = parsePrimaryKey(type);

            final Map<String, List<ElementInfo>> alternateKeys = parseAlternateKeys(type);

            final List<ElementInfo> elements = parseElements(type);

            final List<ElementInfo> nonVirtualElements = new ArrayList<>();

            for (final ElementInfo einfo : elements)
            {
                if (!einfo.virtual())
                    nonVirtualElements.add(einfo);
            }

            final Boolean dirtyChecking = storable.dirtyChecking();

            final Boolean cacheable = storable.cacheable();

            final Boolean sharedNothing = storable.sharedNothing();

            sinfo
                    .type(type)
                    .name(name)
                    .aliases(aliases)
                    .schema(schema)
                    .surrogateKey(surrogateKey)
                    .primaryKey(primaryKey)
                    .alternateKeys(alternateKeys)
                    .elements(elements)
                    .nonVirtualElements(nonVirtualElements)
                    .dirtyChecking(dirtyChecking)
                    .cacheable(cacheable)
                    .sharedNothing(sharedNothing)
                    .consistencyLevel(consistencyLevel);

            storablesCache.put(type, sinfo); // Caches it
        }

        return sinfo;
    }

    /**
     * Extracts information from a {@link Storable} bean class type.
     * 
     * @param type
     *            the {@link Storable} bean class type
     * @param name
     *            either the element name or one of its alias names
     * 
     * @return a {@link StorableInfo} object with information extracted from the bean class type
     * 
     * @throws IllegalArgumentException
     *             if the type is not an valid {@link Storable} class or it does not has the element
     */
    public static ElementInfo info(final Class<?> type, final String name) throws IllegalArgumentException
    {
        checkStorable(type);

        if (!StringValidations.isValid(name))
            throw new IllegalArgumentException("Name is invalid");

        for (final ElementInfo einfo : info(type).elements())
        {
            if (einfo.name().equalsIgnoreCase(name))
                return einfo;

            for (final List<String> aliases : einfo.aliases().values())
                for (final String alias : aliases)
                    if (alias.equalsIgnoreCase(name))
                        return einfo;
        }

        throw new IllegalArgumentException(String.format("Element [%s] not found", name));
    }

    /**
     * Returns the value for the bean field member from a {@link Storable} bean object.
     * 
     * @param bean
     *            the {@link Storable} bean object
     * @param name
     *            either the element name or one of its alias names
     * 
     * @return the bean field member value
     * 
     * @throws IllegalArgumentException
     *             if either the bean is not an valid {@link Storable} object or name does not
     *             references a bean field member. if the bean type doesn't have a element with the
     *             specified name
     */
    @SuppressWarnings("unchecked")
    public static <T> T value(final Object bean, final String name) throws IllegalArgumentException
    {
        if (bean == null)
            throw new IllegalArgumentException("Bean cannot be null");

        final Class<?> type = bean.getClass();

        checkStorable(type);

        if (!StringValidations.isValid(name))
            throw new IllegalArgumentException("Name is invalid");

        T result = null;

        try
        {
            final Field field = matchField(type, name);
            if (field == null)
                throw new IllegalArgumentException(String.format("Bean does not have such element [%s]", name));
            field.setAccessible(true);
            result = (T) field.get(bean);
        }
        catch (final IllegalAccessException e)
        {
            throw new IllegalArgumentException(String.format("Cannot set the value for element [%s]", name));
        }

        return result;
    }

    /**
     * Sets a new value for the bean field member for a {@link Storable} bean object.
     * 
     * @param bean
     *            The {@link Storable} bean object
     * @param name
     *            Either the element name or one of its alias names
     * @param value
     *            The new value to be set; can be null
     * 
     * @throws IllegalArgumentException
     *             if either the bean is not an valid {@link Storable} object or either name or
     *             value is invalid. if the bean type doesn't have a element with the specified
     *             name. if the field cannot be set to the new value
     */
    public static void value(final Object bean, final String name, final Object value) throws IllegalArgumentException
    {
        if (bean == null)
            throw new IllegalArgumentException("Bean cannot be null");

        final Class<?> type = bean.getClass();

        checkStorable(type);

        if (!StringValidations.isValid(name))
            throw new IllegalArgumentException("Name is invalid");

        final Field field = matchField(type, name);
        if (field == null)
            throw new IllegalArgumentException(String.format("Bean does not have such element [%s]", name));

        try
        {
            field.setAccessible(true);
            final DataType dt = parseDataType(field.getType(), field.getAnnotation(Element.class));
            switch (dt)
            {
            // FIXME the field type may or may not match the DataType

            case BOOLEAN:
                field.set(bean, value);
                break;
            case CHAR:
                field.set(bean, value);
                break;
            case BYTE:
                field.set(bean, value);
                break;
            case SHORT:
                field.set(bean, value);
                break;
            case INT:
                field.set(bean, value);
                break;
            case LONG:
                field.set(bean, value);
                break;
            case BIGINTEGER:
                field.set(bean, value);
                break;
            case FLOAT:
                if (value instanceof BigDecimal)
                    field.set(bean, ((BigDecimal) value).floatValue());
                else
                    field.set(bean, value);
                break;
            case DOUBLE:
                field.set(bean, value);
                break;
            case BIGDECIMAL:
                field.set(bean, value);
                break;
            case STRING:
                field.set(bean, value);
                break;
            case ENUM:
                field.set(bean, value);
                break;
            case DATE:
            case TIME:
            case TIMESTAMP:
                field.set(bean, value == null ? null : new Date(((Date) value).getTime()));
                break;
            default:
                field.set(bean, value);
            }
        }
        catch (final IllegalArgumentException | IllegalAccessException e)
        {
            throw new IllegalArgumentException(String.format("Cannot set the value for element [%s]", name));
        }
    }

    /**
     * Returns the values for all the bean field members from a {@link Storable} bean object.
     * 
     * @param bean
     *            the bean object
     * 
     * @return the set of all non null values paired as name and value
     * 
     * @throws IllegalArgumentException
     *             if the bean is not an valid {@link Storable} object
     */
    public static Map<String, Object> values(final Object bean) throws IllegalArgumentException
    {
        if (bean == null)
            throw new IllegalArgumentException("Bean cannot be null");

        final Class<?> type = bean.getClass();

        checkStorable(type);

        final Map<String, Object> result = new HashMap<>();

        for (final ElementInfo einfo : info(type).elements())
        {
            final String name = einfo.name();
            final Object value = value(bean, name);
            if (value != null)
                result.put(name, value);
        }

        return result;
    }

    /**
     * 
     * @param bean
     * @return
     * @throws IllegalArgumentException
     */
    public static Object surrogateKey(final Object bean) throws IllegalArgumentException
    {
        if (bean == null)
            throw new IllegalArgumentException("Bean cannot be null");

        final Class<?> type = bean.getClass();

        checkStorable(type);

        if (info(type).surrogateKey() == null)
            return null;

        final Object id = value(bean, info(type).surrogateKey().name());

        return id;
    }

    /**
     * 
     * @param bean
     * @return
     * @throws IllegalArgumentException
     */
    public static byte[] surrogateKeyBytes(final Object bean) throws IllegalArgumentException
    {
        Object id = surrogateKey(bean);

        if (id == null)
            return null;
        else if (id instanceof String)
            return ((String) id).getBytes(charset);
        else
            return null;
    }

    /**
     * Returns a hash code value for the given beans surrogate key.
     * 
     * @param bean
     *            the bean object
     * 
     * @return the surrogate key hash code or null if the bean does not have a surrogate key
     * 
     * @throws IllegalArgumentException
     *             if the bean is null or an invalid {@link Storable} object
     */
    public static String surrogateKeyHash(final Object bean) throws IllegalArgumentException
    {
        final Object id = surrogateKey(bean);

        if (id == null)
            return null;

        final Class<?> type = bean.getClass();

        final Object[] values = new Object[2];

        values[0] = info(type).name();
        values[1] = id;

        return createKey(values);
    }

    /**
     * Returns a hash code value for the given alternate key value.
     * 
     * @param type
     *            the bean type
     * @param id
     *            the surrogate key value
     * 
     * @return the surrogate key hash code
     * 
     * @throws IllegalArgumentException
     *             if the type is null or an invalid {@link Storable} object; if the primary key
     *             values are invalid
     */
    public static String surrogateKeyHash(final Class<?> type, final Object id) throws IllegalArgumentException
    {
        checkStorable(type);

        if (id == null)
            throw new IllegalArgumentException("Invalid surrogate key value");

        final Object[] values = new Object[2];

        values[0] = info(type).name();
        values[1] = id;

        return createKey(values);
    }

    /**
     * 
     * @param bean
     * @return
     * @throws IllegalArgumentException
     */
    public static Object[] primaryKeys(final Object bean) throws IllegalArgumentException
    {
        if (bean == null)
            throw new IllegalArgumentException("Bean cannot be null");

        final Class<?> type = bean.getClass();

        checkStorable(type);

        if (info(type).primaryKey().isEmpty())
            throw new IllegalArgumentException("PrimaryKey annotation is empty");

        final Object[] values = new Object[info(type).primaryKey().size()];

        int i = 0;

        for (final ElementInfo einfo : info(type).primaryKey())
        {
            values[i++] = value(bean, einfo.name());
        }

        return values;
    }

    /**
     * Returns a hash code value for the given beans primary key.
     * 
     * @param bean
     *            the bean object
     * 
     * @return the primary key hash code or null if the bean does not have a primary key
     * 
     * @throws IllegalArgumentException
     *             if the bean is null or an invalid {@link Storable} object
     */
    public static String primaryKeyHash(final Object bean) throws IllegalArgumentException
    {
        if (bean == null)
            throw new IllegalArgumentException("Bean cannot be null");

        final Class<?> type = bean.getClass();

        checkStorable(type);

        if (info(type).primaryKey().isEmpty())
            return null;

        final Object[] values = new Object[info(type).primaryKey().size() + 1];

        values[0] = info(type).name();

        int i = 0;

        for (final ElementInfo einfo : info(type).primaryKey())
            values[++i] = value(bean, einfo.name());

        return createKey(values);
    }

    /**
     * Returns a hash code value for the given primary key values.
     * 
     * @param type
     *            the bean type
     * @param keys
     *            the primary key values
     * 
     * @return the primary key hash code
     * 
     * @throws IllegalArgumentException
     *             if the type is null or an invalid {@link Storable} object; if the primary key
     *             values are invalid
     */
    public static String primaryKeyHash(final Class<?> type, final Object... keys) throws IllegalArgumentException
    {
        checkStorable(type);

        if (keys == null || keys.length == 0)
            throw new IllegalArgumentException("Invalid primary key values");

        if (keys.length != info(type).primaryKey().size())
            throw new IllegalArgumentException("Invalid primary key values");

        final Object[] values = new Object[keys.length + 1];

        values[0] = info(type).name();

        System.arraycopy(keys, 0, values, 1, keys.length);

        return createKey(values);
    }

    /**
     * Returns a set of hash code values for the given beans alternate keys.
     * 
     * @param bean
     *            the bean object
     * 
     * @return the set of alternate keys hash codes or an empty set if the bean does not have any
     *         alternate key
     * 
     * @throws IllegalArgumentException
     *             if the bean is null or an invalid {@link Storable} object
     */
    public static List<String> alternateKeyHashes(final Object bean) throws IllegalArgumentException
    {
        if (bean == null)
            throw new IllegalArgumentException("Bean cannot be null");

        final Class<?> type = bean.getClass();

        checkStorable(type);

        final List<String> hashes = new ArrayList<>();

        if (info(type).alternateKeys().isEmpty())
            return hashes;

        for (final String name : info(type).alternateKeys().keySet())
        {
            final List<ElementInfo> elements = info(type).alternateKeys().get(name);
            final List<Object> values = new ArrayList<>();

            for (final ElementInfo einfo : elements)
            {
                final Object value = value(bean, einfo.name());
                if (value == null)
                    continue;
                values.add(value);
            }

            if (!values.isEmpty())
                hashes.add(alternateKeyHash(type, name, values.toArray()));
        }

        return hashes;
    }

    /**
     * Returns a hash code value for the given alternate key values.
     * 
     * @param type
     *            the bean type
     * @param name
     *            the alternate key name
     * @param keys
     *            the alternate key values
     * 
     * @return the alternate key hash code
     * 
     * @throws IllegalArgumentException
     *             if the type is null or an invalid {@link Storable} object; if the alternate key
     *             name is null or invalid; if the alternate key values are invalid
     */
    public static String alternateKeyHash(final Class<?> type, final String name, final Object... keys)
            throws IllegalArgumentException
    {
        checkStorable(type);

        if (keys == null || keys.length == 0)
            throw new IllegalArgumentException("Invalid alternate key values");

        if (info(type).alternateKey(name).isEmpty())
            throw new IllegalArgumentException("Invalid alternate key name");

        final Object[] values = new Object[keys.length + 2];

        values[0] = info(type).name();
        values[1] = name;

        System.arraycopy(keys, 0, values, 2, keys.length);

        return createKey(values);
    }

    /**
     * 
     * @param bean
     * @return
     * @throws IllegalArgumentException
     */
    public static Object[] alternateKeys(final Object bean) throws IllegalArgumentException
    {
        if (bean == null)
            throw new IllegalArgumentException("Bean cannot be null");

        final Class<?> type = bean.getClass();

        checkStorable(type);

        if (info(type).primaryKey().isEmpty())
            throw new IllegalArgumentException("PrimaryKey annotation is empty");

        final Object[] values = new Object[info(type).primaryKey().size()];

        int i = 0;

        for (final ElementInfo einfo : info(type).primaryKey())
        {
            values[i++] = value(bean, einfo.name());
        }

        return values;
    }

    /**
     * Parses the string value for its TimeUnit equivalent value.
     * 
     * @param value
     *            the string value
     * 
     * @return the TimeUnit value
     */
    public static TimeUnit parseTimeUnit(final String value)
    {
        if (value == null)
            return null;

        switch (value.toUpperCase())
        {
        case "N":
        case "NANOSECONDS":
            return TimeUnit.NANOSECONDS;
        case "C":
        case "MICROSECONDS":
            return TimeUnit.MICROSECONDS;
        case "L":
        case "MILLISECONDS":
            return TimeUnit.MILLISECONDS;
        case "S":
        case "SECONDS":
            return TimeUnit.SECONDS;
        case "M":
        case "MINUTES":
            return TimeUnit.MINUTES;
        case "H":
        case "HOURS":
            return TimeUnit.HOURS;
        case "D":
        case "DAYS":
            return TimeUnit.DAYS;
        default:
            return null;
        }
    }

    /**
     * Parses the TimeUnit value to its string equivalent.
     * 
     * @param unit
     *            the time unit value
     * 
     * @return the string value
     */
    public static String parseTimeUnit(final TimeUnit unit)
    {
        if (unit == null)
            return null;

        switch (unit)
        {
        case NANOSECONDS:
            return "N";
        case MICROSECONDS:
            return "C";
        case MILLISECONDS:
            return "L";
        case SECONDS:
            return "S";
        case MINUTES:
            return "M";
        case HOURS:
            return "H";
        case DAYS:
            return "D";
        default:
            return null;
        }
    }

    /**
     * Deep copy the values from source to target overriding any existing values.
     * 
     * @param source
     *            the bean to copy the values from
     * @param target
     *            the bean to copy the values to
     */
    public static <T> void copy(final T source, final T target)
    {
        copy(source, target, true);
    }

    /**
     * Deep copy the values from source to target.
     * 
     * @param source
     *            the bean to copy the values from
     * @param target
     *            the bean to copy the values to
     * @param override
     *            controls if there should be overriding of any existing values
     */
    public static <T> void copy(final T source, final T target, final boolean override)
    {
        final Map<String, Object> values = values(source);

        if (override)
        {
            for (final String name : values.keySet())
            {
                value(target, name, values.get(name));
            }
        }
        else
        {
            final Map<String, Object> tvalues = values(target);

            for (final String name : values.keySet())
            {
                if (tvalues.containsKey(name))
                {
                    // keeps the value from target (no override)
                    continue;
                }

                value(target, name, values.get(name));
            }
        }
    }

    /**
     * Deep copy a bean object.
     * 
     * @param bean
     *            the bean object to be cloned
     * 
     * @return a new bean instance
     * 
     * @throws IllegalArgumentException
     *             if the bean is null or an invalid {@link Storable} object
     */
    public static <T> T clone(final T bean) throws IllegalArgumentException
    {
        if (bean == null)
            throw new IllegalArgumentException("Bean cannot be null");

        checkStorable(bean.getClass());

        T clone = newInstance(bean);

        copy(bean, clone);

        return clone;
    }

    /**
     * 
     * @param bean
     * @return
     * @throws IllegalArgumentException
     */
    public static boolean hasTimeToLive(final Object bean) throws IllegalArgumentException
    {
        if (bean == null)
            throw new IllegalArgumentException("Bean cannot be null");

        Class<?> type = bean.getClass();

        checkStorable(type);

        if (ttlFields.containsKey(type))
            return true;

        for (final Field field : AnnotationScanner.scanFields(type, TimeToLive.class))
        {
            if (!field.getAnnotation(TimeToLive.class).unit())
            {
                ttlFields.put(type, field);
                return true;
            }
        }

        for (final Field field : AnnotationScanner.scanFields(type, Metadata.class))
        {
            if (field.getAnnotation(Metadata.class).value() == Metadata.MetadataType.TIME_TO_LIVE)
            {
                ttlFields.put(type, field);
                return true;
            }
        }

        return false;
    }

    /**
     * 
     * @param bean
     * @return
     * @throws IllegalArgumentException
     */
    public static Long timeToLive(final Object bean) throws IllegalArgumentException
    {
        if (bean == null)
            throw new IllegalArgumentException("Bean cannot be null");

        if (!hasTimeToLive(bean))
            throw new IllegalArgumentException("Bean has no ttl element");

        try
        {
            final Field field = ttlFields.get(bean.getClass()); // Should never fail
            field.setAccessible(true);
            final Object value = field.get(bean);

            return NumberFormatter.asLong(value);
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalArgumentException(String.format("Cannot access ttl from bean"));
        }
    }

    /**
     * 
     * @param bean
     * @return
     * @throws IllegalArgumentException
     */
    public static boolean hasTimeToLiveUnit(final Object bean) throws IllegalArgumentException
    {
        if (bean == null)
            throw new IllegalArgumentException("Bean cannot be null");

        Class<?> type = bean.getClass();

        checkStorable(type);

        if (ttlUnitFields.containsKey(type))
            return true;

        for (final Field field : AnnotationScanner.scanFields(type, TimeToLive.class))
        {
            if (field.getAnnotation(TimeToLive.class).unit())
            {
                ttlUnitFields.put(type, field);
                return true;
            }
        }

        return false;
    }

    /**
     * 
     * @param bean
     * @return
     * @throws IllegalArgumentException
     */
    public static TimeUnit timeToLiveUnit(final Object bean) throws IllegalArgumentException
    {
        if (bean == null)
            throw new IllegalArgumentException("Bean cannot be null");

        if (!hasTimeToLiveUnit(bean))
            throw new IllegalArgumentException("Bean has no ttl unit element");

        try
        {
            final Field field = ttlUnitFields.get(bean.getClass()); // Should never fail
            field.setAccessible(true);
            final Object value = field.get(bean);
            if (value instanceof TimeUnit)
                return (TimeUnit) value;
            else if (value instanceof String)
                return parseTimeUnit((String) value);
        }
        catch (final IllegalAccessException e)
        {
            throw new IllegalArgumentException(String.format("Cannot set ttl to bean"));
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T newInstance(final T bean)
    {
        T instance = null;

        try
        {
            instance = (T) bean.getClass().newInstance();
        }
        catch (final InstantiationException | IllegalAccessException e)
        {
            // do nothing
        }

        return instance;
    }

    /**
     * Checks if the bean type is decorated with {@link Storable} annotation.
     * 
     * @param type
     *            the bean type
     * @throws IllegalArgumentException
     *             if the annotation is not present
     */
    private static void checkStorable(final Class<?> type) throws IllegalArgumentException
    {
        if (type == null)
            throw new IllegalStateException("Bean type cannot be null");

        if (!AnnotationScanner.isAnnotationPresent(type, Storable.class))
            throw new IllegalArgumentException("Not a storable bean type");
    }

    /**
     * Parses the class name without the package.
     * 
     * @param type
     *            the class type
     * @return the class name
     */
    private static String className(final Class<?> type)
    {
        String name = type.getName();

        int first;

        first = name.lastIndexOf('.') + 1;

        if (first > 0)
            name = name.substring(first);

        return name;
    }

    /**
     * Retrieves from the bean type the field that matches the given name.
     * 
     * @param type
     *            the bean type
     * @param elementName
     *            the element name to match
     * 
     * @return the match field or null if there is none
     */
    private static Field matchField(final Class<?> type, final String elementName)
    {
        final String key = type.getName() + elementName;

        if (fieldsCache.containsKey(key))
            return fieldsCache.get(key);

        for (final Field field : AnnotationScanner.scanFields(type, Element.class))
        {
            if (elementName.equals(field.getAnnotation(Element.class).name())
                    || elementName.equalsIgnoreCase(field.getName()))
            {
                fieldsCache.put(key, field);
                return field;
            }
            else if (field.isAnnotationPresent(Aliases.class))
            {
                for (final Alias alias : field.getAnnotation(Aliases.class).value())
                {
                    for (final String aliasName : alias.names())
                    {
                        if (elementName.equalsIgnoreCase(aliasName))
                        {
                            fieldsCache.put(key, field);
                            return field;
                        }
                    }
                }
            }
            else if (field.isAnnotationPresent(Alias.class))
            {
                for (final String aliasName : field.getAnnotation(Alias.class).names())
                {
                    if (elementName.equalsIgnoreCase(aliasName))
                    {
                        fieldsCache.put(key, field);
                        return field;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Parses the collection of {@link Alias} annotations.
     * 
     * @param aliases
     *            the collection to be parsed
     * 
     * @return the set of parsed aliases
     */
    private static Map<String, List<String>> parseAliases(final Alias[] aliases)
    {
        final Map<String, List<String>> parsedAliases = new ConcurrentHashMap<String, List<String>>();

        for (final Alias alias : aliases)
        {
            final List<String> names = parsedAliases.containsKey(alias.target()) ? parsedAliases.get(alias.target())
                    : new CopyOnWriteArrayList<String>();

            for (final String name : alias.names())
            {
                names.add(name);
            }

            parsedAliases.put(alias.target(), names);
        }

        return parsedAliases;
    }

    /**
     * Parses the elements from a bean type.
     * 
     * @param type
     *            the bean type
     * 
     * @return the parsed elements
     */
    private static List<ElementInfo> parseElements(final Class<?> type)
    {
        final List<ElementInfo> elements = new ArrayList<ElementInfo>();

        for (final Field field : AnnotationScanner.scanFields(type, Element.class))
        {
            final Element element = field.getAnnotation(Element.class);

            final String name = StringValidations.isValid(element.name()) ? element.name() : field.getName();

            final DataType dataType = parseDataType(field.getType(), element);

            final InstanceType instanceType = element.instanceType();

            final Map<String, List<String>> aliases = field.isAnnotationPresent(Aliases.class)
                    ? parseAliases(field.getAnnotation(Aliases.class).value())
                    : new ConcurrentHashMap<String, List<String>>();
            final Alias alias = field.getAnnotation(Alias.class);
            if (alias != null)
            {
                final List<String> names = aliases.containsKey(alias.target()) ? aliases.get(alias.target())
                        : new CopyOnWriteArrayList<String>();

                for (final String n : alias.names())
                {
                    names.add(n);
                }

                aliases.put(alias.target(), names);
            }

            final Boolean virtual = field.isAnnotationPresent(Virtual.class);

            final Boolean metadata = field.isAnnotationPresent(Metadata.class);

            final ElementInfo einfo = new ElementInfo()
                    .type(field.getType())
                    .name(name)
                    .dataType(dataType)
                    .instanceType(instanceType)
                    .aliases(aliases)
                    .virtual(virtual)
                    .metadata(metadata)
                    .metadataType(metadata ? field.getAnnotation(Metadata.class).value() : null);

            elements.add(einfo);
        }

        return elements;
    }

    /**
     * Parses the elements data type.
     * 
     * @param fieldType
     *            the elements field type
     * @param element
     *            the element
     * 
     * @return the elements data type
     */
    private static DataType parseDataType(final Class<?> fieldType, final Element element)
    {
        if (element.dataType() != DataType.UNKNOWN)
            return element.dataType();
        else if (String.class.equals(fieldType))
            return DataType.STRING;
        else if (Long.class.equals(fieldType) || long.class.equals(fieldType))
            return DataType.LONG;
        else if (Date.class.equals(fieldType))
            return DataType.DATE;
        else if (Integer.class.equals(fieldType) || int.class.equals(fieldType))
            return DataType.INT;
        else if (Boolean.class.equals(fieldType) || boolean.class.equals(fieldType))
            return DataType.BOOLEAN;
        else if (Float.class.equals(fieldType) || float.class.equals(fieldType))
            return DataType.FLOAT;
        else if (fieldType.isEnum())
            return DataType.ENUM;
        else if (Byte.class.equals(fieldType) || byte.class.equals(fieldType))
            return DataType.BYTE;
        else if (Short.class.equals(fieldType) || short.class.equals(fieldType))
            return DataType.SHORT;
        else if (Character.class.equals(fieldType) || char.class.equals(fieldType))
            return DataType.CHAR;
        else if (Double.class.equals(fieldType) || double.class.equals(fieldType))
            return DataType.DOUBLE;
        else if (Void.class.equals(fieldType))
            return DataType.VOID;
        else
            return DataType.UNKNOWN;
    }

    /**
     * Parses elements by its names.
     * 
     * @param type
     *            the bean type
     * @param names
     *            the given element names
     * 
     * @return the parsed elements
     */
    private static List<ElementInfo> parseElementByNames(final Class<?> type, final String... names)
    {
        final List<String> nameList = Arrays.asList(names);
        final List<ElementInfo> elements = parseElements(type);

        for (final Iterator<ElementInfo> i = elements.iterator(); i.hasNext();)
        {
            // FIXME support alias names
            if (!nameList.contains(i.next().name()))
                i.remove();
        }

        return elements;
    }

    /**
     * Parses the primary key from a bean type.
     * 
     * @param type
     *            the bean type
     * 
     * @return the parsed primary key
     */
    private static List<ElementInfo> parsePrimaryKey(final Class<?> type)
    {
        if (AnnotationScanner.isAnnotationPresent(type, PrimaryKey.class))
            return parseElementByNames(type, AnnotationScanner.getAnnotation(type, PrimaryKey.class).value());
        else
            return new ArrayList<ElementInfo>();
    }

    /**
     * 
     * @param type
     * @return
     */
    private static Map<String, List<ElementInfo>> parseAlternateKeys(final Class<?> type)
    {
        if (!AnnotationScanner.isAnnotationPresent(type, AlternateKeys.class)
                && !AnnotationScanner.isAnnotationPresent(type, AlternateKey.class))
            return new ConcurrentHashMap<>();

        final ConcurrentHashMap<String, List<ElementInfo>> result = new ConcurrentHashMap<>();

        final List<AlternateKey> annotations = new ArrayList<>();

        if (AnnotationScanner.isAnnotationPresent(type, AlternateKeys.class))
            for (AlternateKey a : AnnotationScanner.getAnnotation(type, AlternateKeys.class).value())
                annotations.add(a);

        if (AnnotationScanner.isAnnotationPresent(type, AlternateKey.class))
            annotations.add(AnnotationScanner.getAnnotation(type, AlternateKey.class));

        for (final AlternateKey a : annotations)
            result.put(a.name(), parseElementByNames(type, a.value()));

        return result;
    }

    /**
     * 
     * @param type
     * @return
     */
    private static ElementInfo parseSurrogateKey(final Class<?> type)
    {
        if (!AnnotationScanner.isAnnotationPresent(type, SurrogateKey.class))
            return null;

        final String idName = AnnotationScanner.getAnnotation(type, SurrogateKey.class).value();

        for (final ElementInfo einfo : parseElements(type))
        {
            if (einfo.name().equalsIgnoreCase(idName))
                return einfo;
        }

        return null;
    }

    /**
     * Create a key based upon the given key values.
     * 
     * @param values
     *            the key values
     * 
     * @return If the list contains one element returns its value; if the list is longer returns a
     *         hash of the appended values;
     * 
     * @throws IllegalStateException
     *             If the list of values are null or empty
     * @throws IllegalArgumentException
     *             If the list of values are null or empty
     */
    private static String createKey(final Object... values) throws IllegalStateException, IllegalArgumentException
    {
        for (final Object v : values)
        {
            if (v == null)
                throw new IllegalArgumentException("Cannot create key: value is null");
        }

        String result = null;

        final MessageDigest digest = getMessageDigestInstance();

        try
        {
            final StringBuilder buffer = new StringBuilder();

            for (final Object v : values)
                buffer.append(v.toString()).append(':');

            final byte[] hash = digest.digest(buffer.toString().getBytes(KEY_ENCONDING));

            result = HexFormatter.encode(hash);
        }
        catch (final UnsupportedEncodingException e)
        {
            throw new IllegalArgumentException("Cannot create key: unsupported encode", e);
        }

        disposeMessageDigestInstance(digest);

        return result;
    }

    /**
     * Returns a {@link MessageDigest} object that implements the specified digest algorithm.
     * 
     * @return a {@link MessageDigest} object that implements the specified algorithm; <tt>null</tt>
     *         if the digest instance cannot be obtained
     */
    private static MessageDigest getMessageDigestInstance()
    {
        final MessageDigest result = messageDigestCache.getAndSet(null);
        if (result == null)
        {
            try
            {
                return MessageDigest.getInstance(KEY_DIGEST_ALGORITHM);
            }
            catch (final NoSuchAlgorithmException e)
            {
                return null;
            }
        }
        return result;
    }

    /**
     * 
     * @param messageDigest
     */
    private static void disposeMessageDigestInstance(final MessageDigest messageDigest)
    {
        messageDigest.reset();
        messageDigestCache.set(messageDigest);
    }
}