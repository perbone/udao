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

package io.perbone.udao.provider.je;

import java.util.concurrent.atomic.AtomicBoolean;

import io.perbone.udao.transaction.IsolationLevel;
import io.perbone.udao.transaction.Transaction;
import io.perbone.udao.transaction.TransactionException;

/**
 * Concrete {@link Transaction} interface implementation for JE storage.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
class JeTransactionImpl implements Transaction
{
    private String id;
    private IsolationLevel isolationLevel;
    private com.sleepycat.je.Transaction txn;

    private final AtomicBoolean active = new AtomicBoolean(true);

    public JeTransactionImpl(final String id, final IsolationLevel isolationLevel,
            final com.sleepycat.je.Transaction txn)
    {
        this.id = id;
        this.isolationLevel = isolationLevel;
        this.txn = txn;
    }

    @Override
    public String getId() throws IllegalStateException, TransactionException
    {
        checkValid();

        return id;
    }

    @Override
    public IsolationLevel getIsolationLevel() throws IllegalStateException, TransactionException
    {
        checkValid();

        return isolationLevel;
    }

    @Override
    public boolean isActive()
    {
        return active.get();
    }

    com.sleepycat.je.Transaction getTransaction()
    {
        checkValid();

        return txn;
    }

    void invalidate()
    {
        if (active.compareAndSet(true, false))
        {
            id = null;
            isolationLevel = null;
            txn = null;
        }
    }

    private void checkValid() throws IllegalStateException
    {
        if (!active.get())
            throw new IllegalStateException("Invalid transaction object");
    }
}