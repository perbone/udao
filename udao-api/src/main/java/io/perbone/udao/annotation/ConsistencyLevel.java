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
 * Consistency Level enumeration.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public enum ConsistencyLevel
{
    /*
     * Write Level Behavior ZERO Ensure nothing. A write happens asynchronously in background ANY
     * Ensure that the write has been written to at least 1 node, including hinted recipients. ONE
     * Ensure that the write has been written to at least 1 node's commit log and memory table
     * before responding to the client. QUORUM Ensure that the write has been written to
     * <ReplicationFactor> / 2 + 1 nodes before responding to the client. ALL Ensure that the write
     * is written to all <ReplicationFactor> nodes before responding to the client. Any unresponsive
     * nodes will fail the operation. Read Level Behavior ZERO Not supported, because it doesn't
     * make sense. ANY Not supported. You probably want ONE instead. ONE Will return the record
     * returned by the first node to respond. A consistency check is always done in a background
     * thread to fix any consistency issues when ConsistencyLevel.ONE is used. This means subsequent
     * calls will have correct data even if the initial read gets an older value. (This is called
     * read�repair.) QUORUM Will query all nodes and return the record with the most recent
     * timestamp once it has at least a majority of replicas reported. Again, the remaining replicas
     * will be checked in the background. ALL Will query all nodes and return the record with the
     * most recent timestamp once all nodes have replied. Any unresponsive nodes will fail the
     * operation.
     */
    ZERO, ANY, ONE, TWO, THREE, QUORUM, ALL, LOCAL_QUORUM, EACH_QUORUM, SERIAL, LOCAL_SERIAL, LOCAL_ONE
}