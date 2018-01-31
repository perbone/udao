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

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Decorates a field annotated with {@link Element} whose state will not end up into the underling
 * storage, instead it will be available only within the bean life cycle it belongs to. Optionally
 * the state can be kept in a cache owned by the framework so the field value can be synchronized
 * with it when the bean is fetched from the underling storage. Be aware too that the framework
 * offers no guarantees on the availability for this cache so you have to be prepared to have
 * default state values for all the {@code Virtual} fields. The default behavior does not caches it
 * as this would requires more system resources (in this case memory for the state cache entry).
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
@Target({ ANNOTATION_TYPE, FIELD })
@Retention(RUNTIME)
public @interface Virtual
{
    boolean cached() default false;
}