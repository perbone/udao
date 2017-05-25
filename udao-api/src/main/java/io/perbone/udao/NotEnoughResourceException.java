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
 * Not enough resources exception.
 * <p>
 * It's an specialization of {@link DataException} thrown to indicate that the system does not have
 * enough resources to complete the requested operation.
 * <p>
 * Example of such resource could be a database connection or any other pooled and limited resource.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public class NotEnoughResourceException extends DataException
{
    /** Class {@code NotEnoughResourceException} serial version identifier. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a default {@code NotEnoughResourceException} object without neither a message nor a
     * root exception.
     */
    public NotEnoughResourceException()
    {
        super();
    }

    /**
     * Creates an {@code NotEnoughResourceException} object with a custom message.
     * 
     * @param message
     *            The exception message
     */
    public NotEnoughResourceException(final String message)
    {
        super(message);
    }

    /**
     * Creates an {@code NotEnoughResourceException} object with a custom root exception.
     * 
     * @param cause
     */
    public NotEnoughResourceException(final Throwable cause)
    {
        super(cause);
    }

    /**
     * Creates an {@code NotEnoughResourceException} object with a custom message and a custom root
     * exception.
     * 
     * @param message
     * @param cause
     */
    public NotEnoughResourceException(final String message, final Throwable cause)
    {
        super(message, cause);
    }
}