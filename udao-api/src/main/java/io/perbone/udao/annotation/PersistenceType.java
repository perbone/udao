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

/**
 * Persistence Type anumeration.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public enum PersistenceType
{
    /**
     * Resource is maintained if no guarantee that future requests for that same resource will be
     * served. The behavior of this kind of storage is most like a cache pool and in fact it behaves
     * like so. In the presence of a resource miss, the user should be capable to either recompute
     * the data or fetch it from its original storage location. The Storage Manager is in charge and
     * it will automatically purge expired resources, enforcing any final resource life cycle
     * constraint if such exists.
     */
    VOLATILE,

    /**
     * Resource is maintained for a well defined and short period of time and as long as the
     * resource does not expire it will be served in all the requests. The Storage Manager is in
     * charge and it will automatically purge expired resources, enforcing any final resource life
     * cycle constraint if such exists.
     */
    TEMPORARY,

    /**
     * Resource is maintained indefinitely until it expires. This is the second most flexible
     * persistence type of all kind and should be used as a helper storage for business processes
     * that have a complex work flow. It should be viewed as a partner to the much robust and
     * trustworthy durable persistence. The Storage Manager is in charge and it will automatically
     * purge expired resources, enforcing any final resource life cycle constraint if such exists.
     */
    TRANSIENT,

    /**
     * Resource is maintained indefinitely until it expires. This is the most flexible persistence
     * type of all kind and should be used for prime business information storage. Implementations
     * of this kind of persistence is encouraged to use the most respected physical storage, like a
     * well known database vendor. The Storage Manager is in charge and it will automatically purge
     * expired resources, enforcing any final resource life cycle constraint if such exists.
     */
    DURABLE,

    /**
     * Resource is maintained indefinitely until the user specifically purges the data. It is a long
     * term storage as it has no time to live capabilities and it is up to the user to enforce such
     * resource life cycle policy. That sad, the Storage Manager still give to the user the
     * guarantee that any final resource life cycle constraint will be enforced if such exists.
     */
    PERMANENT
}