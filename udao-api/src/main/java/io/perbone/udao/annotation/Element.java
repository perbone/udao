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
 * The Element annotation marks a class field member as a storable data element.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
@Target({ ANNOTATION_TYPE, FIELD })
@Retention(RUNTIME)
public @interface Element
{
    String name() default ""; // Defaults to the field member name

    DataType dataType() default DataType.UNKNOWN;

    InstanceType instanceType() default InstanceType.SYSTEM;

    /**
     * This is the element index inside an array of data, used for binary serialization. The fisrt
     * element index is 0 so -1 is the equivalent of null.
     * 
     * @return the element index
     */
    short index() default -1;
}