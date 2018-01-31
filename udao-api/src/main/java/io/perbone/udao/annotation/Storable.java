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

package io.perbone.udao.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import io.perbone.mkey.EvictionPolicy;

/**
 * Specifies that the class is an entity. This annotation is applied to the entity class.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Storable
{
    /**
     * The entity's schema name.
     */
    String schema() default "";

    /**
     * (Optional) The entity name. Defaults to the unqualified name of the entity class. This name
     * is used to refer to the entity in queries.
     */
    String name() default "";

    Alias[] aliases() default {};

    String version() default "1.0";

    String domain() default "";

    String resourceType() default "";

    String path() default "";

    boolean authentication() default false;

    boolean mutable() default true;

    boolean dirtyChecking() default false;

    boolean cacheable() default true;

    boolean sharedNothing() default false;

    EvictionPolicy evictionPolicy() default EvictionPolicy.LRU;

    PersistenceType persistenceType() default PersistenceType.PERMANENT;

    InstanceType defaultInstanceType() default InstanceType.PRIME;

    ConsistencyLevel consistencyLevel() default ConsistencyLevel.ZERO;
}