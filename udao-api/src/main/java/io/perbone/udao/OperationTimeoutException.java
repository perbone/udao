/*
 * This file is part of ToolBox
 * https://github.com/perbone/toolbox/
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

package io.perbone.udao;

/**
 * Defines the exception thrown when an operation times out.
 * <p>
 * Generally OperationTimeoutException occurs when no response is received within a given time.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 * 
 * @see #ProviderException
 */
public class OperationTimeoutException extends DataException
{
    /** Class {@code OperationTimeoutException} serial version identifier. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a default {@code OperationTimeoutException} object without neither a message nor a
     * root exception.
     */
    public OperationTimeoutException()
    {
        super();
    }

    /**
     * Creates an {@code OperationTimeoutException} object with a custom message.
     * 
     * @param message
     *            The exception message
     */
    public OperationTimeoutException(final String message)
    {
        super(message);
    }

    /**
     * Creates an {@code OperationTimeoutException} object with a custom root exception.
     * 
     * @param cause
     */
    public OperationTimeoutException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates an {@code OperationTimeoutException} object with a custom message and a custom root
     * exception.
     * 
     * @param message
     * @param cause
     */
    public OperationTimeoutException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}