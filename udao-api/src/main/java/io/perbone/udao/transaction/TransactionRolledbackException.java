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

package io.perbone.udao.transaction;

/**
 * Transaction Rolled Back exception.
 * <p>
 * It's an specialization of {@link TransactionException} thrown to indicates that the transaction
 * associated with processing of the request has been rolled back, or marked to roll back. Thus the
 * requested operation either could not be performed or was not performed because further
 * computation on behalf of the transaction would be fruitless.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 * 
 * @see #TransactionException
 */
public class TransactionRolledbackException extends TransactionException
{
    /** Class {@code InvalidTransactionException} serial version identifier. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a default {@code InvalidTransactionException} object without neither a message nor a
     * root exception.
     */
    public TransactionRolledbackException()
    {
        super();
    }

    /**
     * Creates an {@code InvalidTransactionException} object with a custom message.
     * 
     * @param message
     *            The exception message
     */
    public TransactionRolledbackException(String message)
    {
        super(message);
    }

    /**
     * Creates an {@code InvalidTransactionException} object with a custom root exception.
     * 
     * @param cause
     */
    public TransactionRolledbackException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates an {@code InvalidTransactionException} object with a custom message and a custom root
     * exception.
     * 
     * @param message
     * @param cause
     */
    public TransactionRolledbackException(String message, Throwable cause)
    {
        super(message, cause);
    }
}