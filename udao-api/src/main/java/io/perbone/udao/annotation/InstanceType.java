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
 * Instance Type enumeration.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public enum InstanceType
{
    /**
     * The only instance always guarantee to be available for all persistence types. Though it
     * serves the system own needs, it is up to the user to accept it as replacement for other
     * instance types when they are missing or do not exist at all. It is expected for this instance
     * to have above average performance as well optimized stability and availability.
     */
    SYSTEM,

    /**
     * Despite the fact that it is probably the most requested instance of all kind, it isn't
     * guarantee to exist at all. So it is really expected for this instance to be available for the
     * user and not only that but to have an optimized performance, stability and availability
     */
    PRIME,

    /**
     * This is the second most requested instance and like the prime instance it isn't guarantee to
     * exist. Most probably it will be functioning as a partner for the prime instance and likewise
     * it requires the same robustness on performance, stability and availability.
     */
    AUXILIARY,

    /**
     * This is probably the least requested instance and most certain it wont be available at system
     * rollout. That sad, when integrated to the system, there are no standard expectations
     * concerning performance, stability as well availability due to the fact that this is a very
     * specialized instance and doing so most probably it will be tailor tuned for each case
     * scenario.
     */
    HISTORY
}