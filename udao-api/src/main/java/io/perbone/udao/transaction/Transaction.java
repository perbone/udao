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

package io.perbone.udao.transaction;

/**
 * Transaction interface definition.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public interface Transaction extends AutoCloseable
{
    /**
     * Returns this transaction id.
     * 
     * @return the transaction id
     * 
     * @throws IllegalStateException
     *             if the transaction object is invalid
     * @throws TransactionException
     *             if an error occurs during this operation
     */
    String getId() throws IllegalStateException, TransactionException;

    /**
     * Returns this transaction isolation level.
     * 
     * @return the isolation level
     * 
     * @throws IllegalStateException
     *             if the transaction object is invalid
     * @throws TransactionException
     *             if an error occurs during this operation
     */
    IsolationLevel getIsolationLevel() throws IllegalStateException, TransactionException;

    /**
     * Returns true if the transaction has not been invalidated and is still active.
     * 
     * @return {@code true} if the transaction is active, false otherwise
     */
    boolean isActive();
}