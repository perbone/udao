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

/**
 * A cursor is a control object that enables traversal over the beans from the underling storage.
 * Cursors facilitate subsequent processing in conjunction with the traversal, such as retrieval,
 * addition and removal of beans.
 * <p>
 * It extends {@link Iterable} so a concrete implementation can be used in a for/in construction.
 * 
 * @param <T>
 *            The bean type returned
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public interface Cursor<T> extends Iterable<T>, AutoCloseable
{
    /**
     * Specifies the type of scrollable cursor to use underneath a {@code Cursor} implementation.
     */
    public enum ScrollMode
    {
        /**
         * The enumeration indicating the mode for a {@code Cursor} object whose cursor position may
         * move only forward.
         * <p>
         * This is the firehose cursor mode and generally indicates the fastest cursor you'll get.
         */
        TYPE_FORWARD_ONLY,

        /**
         * The enumeration indicating the mode for a {@code Cursor} object whose cursor position is
         * scrollable but generally not sensitive to changes to the data that underlies the
         * {@code Cursor}.
         * <p>
         * This is the default scroll mode when none is specified.
         */
        TYPE_SCROLL_INSENSITIVE,

        /**
         * The enumeration indicating the mode for a {@code Cursor} object whose cursor position is
         * scrollable and sensitive to changes to the data that underlies the {@code Cursor}.
         */
        TYPE_SCROLL_SENSITIVE
    }

    /**
     * Retrieves the cursor scroll mode.
     * 
     * @return this cursor scroll mode
     * 
     * @throws IllegalStateException
     *             if this cursor is already closed
     */
    ScrollMode scrollMode() throws IllegalStateException;

    /**
     * Retrieves the bean at the current position from the underling result set.
     * 
     * @return The current bean object; null if the result set is empty or if {@code bos} is
     *         <tt>true</tt> or if {@code eos} is <tt>true</tt>
     * 
     * @throws IllegalStateException
     *             if this cursor is already closed
     * @throws DataException
     *             if cannot retrieves the bean at current position
     */
    T fetch() throws IllegalStateException, DataException;

    /**
     * Retrieves the bean at the given position from the underling result set.
     * <p>
     * This call does not change the current position.
     * 
     * @param index
     *            the bean's position
     * @return The current bean object; null if the result set is empty or if {@code bos} is
     *         <tt>true</tt> or if {@code eos} is <tt>true</tt>
     * 
     * @throws IllegalStateException
     *             if this cursor is already closed
     * @throws IllegalArgumentException
     *             if the given position is invalid or if the result set is empty
     * @throws DataException
     *             if cannot retrieves the bean at the given position
     */
    T fetch(long index) throws IllegalStateException, IllegalArgumentException, DataException;

    /**
     * Returns an array containing all of the beans in this cursor in proper sequence (from first to
     * last element).
     * <p>
     * The returned array will be "safe" in that no references to it are maintained by this list.
     * (In other words, this method must allocate a new array even if this list is backed by an
     * array). The caller is thus free to modify the returned array.
     * 
     * @return an array containing all of the beans in this cursor in proper sequence
     */
    T[] toArray();

    /**
     * Returns the current position.
     * 
     * @return the current position or <tt>-1</tt> if the result set is empty
     * 
     * @throws IllegalStateException
     *             if this cursor is already closed
     * @throws DataException
     *             if cannot retrieves the current position
     */
    long position() throws IllegalStateException, DataException;

    /**
     * Moves the current position to the first bean from the result set.
     * <p>
     * This call has no effect if the result set is empty.
     * 
     * @throws IllegalStateException
     *             if this cursor is already closed
     * @throws DataException
     *             if cannot moves the current position
     */
    void first() throws IllegalStateException, DataException;

    /**
     * Moves the current position to the last bean from the result set.
     * <p>
     * This call has no effect if the result set is empty.
     * 
     * @throws IllegalStateException
     *             if this cursor is already closed
     * @throws DataException
     *             if cannot moves the current position
     */
    void last() throws IllegalStateException, DataException;

    /**
     * Moves the current position to the next bean from the result set.
     * <p>
     * This call has no effect if the result set is empty.
     * 
     * @throws IllegalStateException
     *             if this cursor is already closed
     * @throws DataException
     *             if cannot moves the current position
     */
    void next() throws IllegalStateException, DataException;

    /**
     * Moves the current position to the previous bean from the result set.
     * <p>
     * This call has no effect if the result set is empty.
     * 
     * @throws IllegalStateException
     *             if this cursor is already closed
     * @throws DataException
     *             if cannot moves the current position
     * 
     * @since 0.7.0
     */
    void previous() throws IllegalStateException, DataException;

    /**
     * Move the cursor to an absolute position, forward or backward, from the first position or from
     * the last position respectively.
     * <p>
     * This call has no effect if the result set is empty.
     * 
     * @param offset
     *            the position
     * 
     * 
     * @throws IllegalStateException
     *             if this cursor is already closed
     * @throws DataException
     *             if cannot moves the current position
     * 
     * @since 0.7.0
     */
    void absolute(long offset) throws IllegalStateException, DataException;

    /**
     * Move the cursor by a relative amount, forward or backward, from the current position.
     * <p>
     * This call has no effect if the result set is empty.
     * 
     * @param offset
     *            the position
     * 
     * @throws IllegalStateException
     *             if this cursor is already closed
     * @throws DataException
     *             if cannot moves the current position
     */
    void relative(long offset) throws IllegalStateException, DataException;

    /**
     * Tells whether or not this cursor position is at <i>begin of set</i>.
     * <p>
     * A cursor is at begin of set if either it is empty or was moved before the first bean.
     * 
     * @return <tt>true</tt> if it is at begin of set; <tt>false</tt> otherwise
     * 
     * @throws IllegalStateException
     */
    boolean bos() throws IllegalStateException;

    /**
     * Tells whether or not this cursor position is at <i>end of set</i>.
     * <p>
     * A cursor is at end of set if either it is empty or was moved after the last bean.
     * 
     * @return <tt>true</tt> if it is at end of set; <tt>false</tt> otherwise
     * 
     * @throws IllegalStateException
     */
    boolean eos() throws IllegalStateException;

    /**
     * Tells whether or not this cursor can move in all directions.
     * <p>
     * With a <i>non-scrollable</i> cursor, also known as forward-only, one can <i>fetch</i> each
     * bean at most once, and the cursor automatically moves to the immediately following bean. A
     * fetch operation after the last bean has been retrieved positions the cursor after the last
     * bean and returns {@code null}.
     * 
     * @return <tt>true</tt> if it is scrollable; <tt>false</tt> otherwise
     * 
     * @throws IllegalStateException
     *             if this cursor is already closed
     */
    boolean scrollable() throws IllegalStateException;

    /**
     * Tells whether or not this cursor supports fetching at a given position.
     * 
     * @return <tt>true</tt> if it is indexable; <tt>false</tt> otherwise
     * 
     * @throws IllegalStateException
     *             if this cursor is already closed
     */
    boolean indexable() throws IllegalStateException;

    /**
     * Tells whether or not this cursor can count the result set before it reaches {@code eos}.
     * <p>
     * 
     * @return <tt>true</tt> if it is countable; <tt>false</tt> otherwise
     * 
     * @throws IllegalStateException
     *             if this cursor is already closed
     */
    boolean countable() throws IllegalStateException;

    /**
     * Returns a count of the number of beans this cursor refers.
     * <p>
     * Only countable cursors can give this information.
     * 
     * @return a count of the number of beans in this cursor
     * 
     * @throws IllegalStateException
     *             if this cursor is already closed
     * @throws DataException
     *             if cannot retrieves the the number of beans
     */
    long count() throws IllegalStateException, DataException;

    /**
     * Closes this cursor.
     * <p>
     * Invocation has no additional effect if already closed and once closed it remains closed.
     * 
     * @throws DataException
     *             if cannot closes this cursor
     * 
     * @see #isOpen
     */
    void close() throws DataException;

    /**
     * Tells whether or not this cursor is open.
     * <p>
     * It is assumed that after a successful object instantiation this method will return
     * <tt>true</tt>. Conversely for fail object instantiation this method should return
     * <tt>false</tt> despite the fact that this object may still be valid.
     * <p>
     * 
     * @return <tt>true</tt> if it is active; <tt>false</tt> otherwise
     * 
     * @see #close
     */
    boolean isOpen();
}