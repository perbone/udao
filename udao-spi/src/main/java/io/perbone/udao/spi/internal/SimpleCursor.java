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

package io.perbone.udao.spi.internal;

import static io.perbone.udao.Cursor.ScrollMode.TYPE_SCROLL_INSENSITIVE;
import static io.perbone.udao.Cursor.ScrollMode.TYPE_SCROLL_SENSITIVE;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;

import io.perbone.udao.Cursor;
import io.perbone.udao.DataException;

/**
 * Simple concrete {@link Cursor} implementation that uses an immutable array as its backing store.
 * It's scrollable, indexable and countable.
 * <p>
 * For performance reasons this class is not thread safe although it has a immutable iterator.
 * <p>
 * Again for performance reasons this implementation uses a singleton (and immutable) iterator to
 * keep garbage at a minimum. So the use of the iterator have to be synchronized between all
 * contender threads.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public class SimpleCursor<T> implements Cursor<T>
{
    private ScrollMode scrollMode = TYPE_SCROLL_INSENSITIVE;

    private static int INVALID_POSITION = -1;

    private static int FIRST_POSITION = 0;

    private boolean closed = false;

    private final T[] table;

    private final int size;

    private int currentPos = INVALID_POSITION;

    final CursorIterator it = new CursorIterator();

    /**
     * Creates a concrete {@link Cursor} object.
     * 
     * @param resultSet
     *            the array of bean for this cursor navigate
     * 
     * @throws IllegalArgumentException
     *             if the array is invalid
     */
    public SimpleCursor(final T[] resultSet) throws IllegalArgumentException
    {
        if (resultSet == null)
            throw new IllegalArgumentException("Result set is null");

        table = resultSet;
        size = resultSet.length;
        if (size > 0)
            currentPos = FIRST_POSITION;
    }

    @Override
    public ScrollMode scrollMode() throws IllegalStateException
    {
        checkOpen();

        return scrollMode;
    }

    @Override
    public boolean bos() throws IllegalStateException
    {
        checkOpen();

        return size == 0 ? true : currentPos < FIRST_POSITION;
    }

    @Override
    public void close() throws DataException
    {
        if (!closed)
        {
            closed = true;
            for (int i = 0; i < table.length; i++)
                table[i] = null;
        }
    }

    @Override
    public long count() throws IllegalStateException, DataException
    {
        checkOpen();

        return size;
    }

    @Override
    public boolean countable() throws IllegalStateException
    {
        checkOpen();

        return true;
    }

    @Override
    public boolean eos() throws IllegalStateException
    {
        checkOpen();

        return size == 0 ? true : currentPos > size - 1;
    }

    @Override
    public T fetch() throws IllegalStateException, DataException
    {
        checkOpen();

        if (bos() || eos())
            return null;
        else
            return table[currentPos];
    }

    @Override
    public T fetch(final long index) throws IllegalStateException, IllegalArgumentException, DataException
    {
        checkOpen();

        if (index < FIRST_POSITION || index >= size)
            throw new IllegalArgumentException("Index out of bound");

        if (bos() || eos())
            return null;

        return table[(int) index];
    }

    @Override
    public T[] toArray()
    {
        checkOpen();

        return Arrays.copyOf(table, size);
    }

    @Override
    public void first() throws IllegalStateException, DataException
    {
        checkOpen();

        currentPos = FIRST_POSITION;
    }

    @Override
    public boolean indexable() throws IllegalStateException
    {
        checkOpen();

        return true;
    }

    @Override
    public boolean isOpen()
    {
        return !closed;
    }

    @Override
    public void last() throws IllegalStateException, DataException
    {
        checkOpen();

        currentPos = size - 1;
    }

    @Override
    public void next() throws IllegalStateException, DataException
    {
        checkOpen();

        if (!eos())
            currentPos++;
    }

    @Override
    public void absolute(final long offset) throws IllegalStateException, DataException
    {
        checkOpen();

        if (offset > 0)
            currentPos = (int) offset;
        else if (offset < 0)
            currentPos = size - (int) Math.abs(offset);
    }

    @Override
    public void relative(final long offset) throws IllegalStateException, DataException
    {
        checkOpen();

        currentPos += (int) offset;
    }

    @Override
    public long position() throws IllegalStateException, DataException
    {
        checkOpen();

        return currentPos;
    }

    @Override
    public void previous() throws IllegalStateException, DataException
    {
        checkOpen();

        if (!bos())
            currentPos--;
    }

    @Override
    public boolean scrollable() throws IllegalStateException
    {
        checkOpen();

        return scrollMode == TYPE_SCROLL_INSENSITIVE || scrollMode == TYPE_SCROLL_SENSITIVE;
    }

    @Override
    public Iterator<T> iterator()
    {
        checkOpen();

        return it;
    }

    @Override
    public Spliterator<T> spliterator()
    {
        final Spliterator<T> spliterator = Spliterators.spliterator(table, Spliterator.IMMUTABLE);

        return spliterator;
    }

    /**
     * Implements an immutable iterator.
     * 
     * @author Paulo Perbone <pauloperbone@yahoo.com>
     * @since 0.1.0
     */
    class CursorIterator implements Iterator<T>
    {
        private CursorIterator()
        {
            // do nothing
        }

        @Override
        public boolean hasNext()
        {
            return size == 0 ? false : !eos();
        }

        @Override
        public T next()
        {
            T result = fetch();
            if (!eos())
                currentPos++;
            return result;
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException("Cannot remove from an immutable iterator");
        }
    }

    private void checkOpen() throws IllegalStateException
    {
        if (closed)
            throw new IllegalStateException("Cursor is closed");
    }
}