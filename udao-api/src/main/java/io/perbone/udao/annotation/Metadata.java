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

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Metadata annotation and enumeration.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
@Target({ ANNOTATION_TYPE, FIELD })
@Retention(RUNTIME)
public @interface Metadata
{
    public enum MetadataType
    {
        /**
         * This meta-data implies Immutable constraint.
         */
        ID,

        /**
         * The current resources variant <tt>Entity Tag</tt>.
         * <p>
         * This meta-data type should be considered for variant tracking. For resources version,
         * it's recommended to use the {@link #VERSION} meta-data type. Although subtle the
         * difference between them can be explored for advanced users.
         * 
         * @see {@link #VERSION}
         */
        ETAG,

        /**
         * The current resources version.
         * <p>
         * This meta-data type should be considered for version control. For resource variant
         * tracking, it's recommended to use {@link #ETAG} meta-data type. Although subtle the
         * difference between them can be explored for advanced users.
         * 
         * @see {@link #ETAG}
         */
        VERSION,

        /**
         * This meta-data type implies NotNull and Immutable constraints.
         */
        OWNER,

        /**
         * The bean creation principal.
         * <p>
         * This meta-data type implies Authentication and Immutable constraints.
         * 
         * @see MetadataType#CREATION_AUTHOR
         */
        PRINCIPAL,

        /**
         * The bean creation date and time. It assumes a date data type.
         * <p>
         * This meta-data type implies Immutable constraints.
         * 
         * @see MetadataType#CREATION_AUTHOR
         * @see MetadataType#CREATION_AGENT
         */
        CREATION_DATE,

        /**
         * The bean creation author.
         * <p>
         * This meta-data type implies NotNull, Immutable and Authentication constraints.
         * 
         * 
         * @see MetadataType#PRINCIPAL
         * @see MetadataType#CREATION_DATE
         * @see MetadataType#CREATION_AGENT
         */
        CREATION_AUTHOR,

        /**
         * The application agent creation author.
         * <p>
         * This meta-data type implies NotNull, Immutable and Authentication constraints.
         */
        CREATION_AGENT,

        /**
         * The bean last access date and time. It assumes a date data type.
         * 
         * @See {@link #LAST_ACCESS_AUTHOR LAST_ACCESS_AUTHOR}
         */
        LAST_ACCESS_DATE,

        /**
         * The bean last access author.
         * <p>
         * This meta-data type implies Authentication constraint.
         * 
         * @See {@link #LAST_ACCESS_DATE LAST_ACCESS_DATE}
         */
        LAST_ACCESS_AUTHOR,

        /**
         * The application agent last access author.
         */
        LAST_ACCESS_AGENT,

        /**
         * The bean last update date and time. It assumes a date data type.
         * 
         * @See {@link #LAST_MODIFIED_AUTHOR LAST_MODIFIED_AUTHOR}
         */
        LAST_MODIFIED_DATE,

        /**
         * The bean last update author.
         * <p>
         * This meta-data type implies Authentication constraint.
         * 
         * @See {@link #LAST_MODIFIED_DATE LAST_MODIFIED_DATE}
         */
        LAST_MODIFIED_AUTHOR,

        /**
         * The application agent last update author.
         */
        LAST_MODIFIED_AGENT,

        /**
         * Time to live constraint.
         * <p>
         * This meta-data type implies NotNull constraint.
         */
        TIME_TO_LIVE,

        /**
         * The entity expiration date and time. It assumes either a date or a long data type.
         * <p>
         * This meta-data type implies Immutable constraints.
         */
        EXPIRES,

        /**
         * 
         */
        CHECK_AND_SWAP
    }

    MetadataType value();
}