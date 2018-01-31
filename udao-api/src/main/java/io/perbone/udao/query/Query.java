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

package io.perbone.udao.query;

import static io.perbone.udao.query.Expression.ExpressionType.AND;
import static io.perbone.udao.query.Expression.ExpressionType.CLOSE_PARENTHESIS;
import static io.perbone.udao.query.Expression.ExpressionType.OPEN_PARENTHESIS;
import static io.perbone.udao.query.Expression.ExpressionType.OR;

import java.util.ArrayList;
import java.util.List;

import io.perbone.toolbox.collection.Pair;

/**
 * Represents storage access query.
 * <p>
 * Can be used for any storage access operation as a filter for the targets.
 * <p>
 * This is a stateful object with one way through creation. With you made mistakes just trash it and
 * create another one.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public final class Query
{
    private final List<Expression> where = new ArrayList<Expression>();

    private final List<Pair<String, Boolean>> order = new ArrayList<Pair<String, Boolean>>();

    private Long limit = null;

    private Long offset = null;

    private Expression current = null;

    /**
     * Open parenthesis.
     * 
     * @return this query object
     */
    public Query op()
    {
        where.add(new Expression(OPEN_PARENTHESIS));

        return this;
    }

    /**
     * Close parenthesis.
     * 
     * @return this query object
     */
    public Query cp()
    {
        where.add(new Expression(CLOSE_PARENTHESIS));

        return this;
    }

    public Query and()
    {
        checkCurrentNull();

        where.add(new Expression(AND));

        return this;
    }

    public Query between(Object begin, Object end) throws IllegalStateException, IllegalArgumentException
    {
        checkValue(begin);
        checkValue(end);
        checkCurrent();

        current.between(begin, end);
        where.add(current);
        current = null;

        return this;
    }

    public Query element(String name) throws IllegalStateException, IllegalArgumentException
    {
        checkCurrentNull();

        // if (!StringValidations.isValid(name))
        // throw new IllegalArgumentException("Invalid element name");

        current = new Expression(name);

        return this;
    }

    public Query equal(Object value) throws IllegalStateException, IllegalArgumentException
    {
        checkValue(value);
        checkCurrent();

        current.equal(value);
        where.add(current);
        current = null;

        return this;
    }

    public Query greater(Object value) throws IllegalStateException, IllegalArgumentException
    {
        checkValue(value);
        checkCurrent();

        current.greater(value);
        where.add(current);
        current = null;

        return this;
    }

    public Query greaterEqual(Object value) throws IllegalStateException, IllegalArgumentException
    {
        checkValue(value);
        checkCurrent();

        current.greaterEqual(value);
        where.add(current);
        current = null;

        return this;
    }

    public boolean hasLimit()
    {
        return limit != null;
    }

    public boolean hasOffset()
    {
        return offset != null;
    }

    public boolean hasOrder()
    {
        return !order.isEmpty();
    }

    public boolean hasWhere()
    {
        return !where.isEmpty();
    }

    public Query in(Object... values) throws IllegalStateException, IllegalArgumentException
    {
        checkCurrent();

        if (values.length == 0)
            throw new IllegalArgumentException("Element values cannot be empty");

        current.in(values);
        where.add(current);
        current = null;

        return this;
    }

    public Query isNotNull()
    {
        checkCurrent();

        current.isNotNull();
        where.add(current);
        current = null;

        return this;
    }

    public Query isNull()
    {
        checkCurrent();

        current.isNull();
        where.add(current);
        current = null;

        return this;
    }

    public Query less(Object value) throws IllegalStateException, IllegalArgumentException
    {
        checkValue(value);
        checkCurrent();

        current.less(value);
        where.add(current);
        current = null;

        return this;
    }

    public Query lessEqual(Object value) throws IllegalStateException, IllegalArgumentException
    {
        checkValue(value);
        checkCurrent();

        current.lessEqual(value);
        where.add(current);
        current = null;

        return this;
    }

    public Long limit()
    {
        return limit;
    }

    public Query limit(long value) throws IllegalStateException, IllegalArgumentException
    {
        if (value < 0)
            throw new IllegalArgumentException("Limit cannot be negative");

        limit = value;
        return this;
    }

    public Query not()
    {
        checkCurrent();

        current.negate();

        return this;
    }

    public Long offset()
    {
        return offset;
    }

    public Query offset(long value) throws IllegalStateException, IllegalArgumentException
    {
        if (value < 0)
            throw new IllegalArgumentException("Offset cannot be negative");

        offset = value;
        return this;
    }

    public Query or()
    {
        checkCurrentNull();

        where.add(new Expression(OR));

        return this;
    }

    public List<Pair<String, Boolean>> order()
    {
        return order;
    }

    public Query order(String... names) throws IllegalStateException, IllegalArgumentException
    {
        if (names.length == 0)
            throw new IllegalArgumentException("Names cannot be empty");

        order.clear();

        for (String n : names)
        {
            String[] values = n.split(" ");
            Pair<String, Boolean> pair = new Pair<String, Boolean>(values[0],
                    values.length == 1 ? true : "ASC".equalsIgnoreCase(values[1]));
            order.add(pair);
        }

        return this;
    }

    public List<Expression> where()
    {
        return where;
    }

    private void checkCurrent() throws IllegalStateException
    {
        if (current == null)
            throw new IllegalStateException("Invalid syntax; elemente name should be first");
    }

    private void checkCurrentNull() throws IllegalStateException
    {
        if (current != null)
            throw new IllegalStateException("Invalid syntax; previous expression is unfinished");
    }

    private void checkValue(Object value) throws IllegalArgumentException
    {
        if (value == null)
            throw new IllegalArgumentException("Element value cannot be null");
    }
}