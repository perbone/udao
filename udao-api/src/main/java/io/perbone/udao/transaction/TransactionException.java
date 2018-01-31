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

import io.perbone.udao.DataException;

/**
 * Transaction root exception.
 * <p>
 * It's an specialization of {@link StorageException} thrown to inform the {@link StorageManager} of
 * an error encountered by the involved transaction. Any other exception from this framework related
 * to transaction <em>should</em> extends this class.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 * 
 * @see #StorageException
 */
public class TransactionException extends DataException
{
    /** Class {@code TransactionException} serial version identifier. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a default {@code TransactionException} object without neither a message nor a root
     * exception.
     */
    public TransactionException()
    {
        super();
    }

    /**
     * Creates an {@code TransactionException} object with a custom message.
     * 
     * @param message
     *            The exception message
     */
    public TransactionException(String message)
    {
        super(message);
    }

    /**
     * Creates an {@code TransactionException} object with a custom root exception.
     * 
     * @param cause
     */
    public TransactionException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates an {@code TransactionException} object with a custom message and a custom root
     * exception.
     * 
     * @param message
     * @param cause
     */
    public TransactionException(String message, Throwable cause)
    {
        super(message, cause);
    }
}