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

package io.perbone.udao;

import io.perbone.toolbox.provider.ProviderException;

/**
 * Root exception for UDAO framework.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public class DataException extends ProviderException
{
    /** Class {@code DataException} serial version identifier. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a default {@code DataException} object without neither a message nor a root
     * exception.
     */
    public DataException()
    {
        super();
    }

    /**
     * Creates an {@code DataException} object with a custom message.
     * 
     * @param message
     *            The exception message
     */
    public DataException(String message)
    {
        super(message);
    }

    /**
     * Creates an {@code DataException} object with a custom root exception.
     * 
     * @param cause
     */
    public DataException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates an {@code DataException} object with a custom message and a custom root exception.
     * 
     * @param message
     * @param cause
     */
    public DataException(String message, Throwable cause)
    {
        super(message, cause);
    }
}