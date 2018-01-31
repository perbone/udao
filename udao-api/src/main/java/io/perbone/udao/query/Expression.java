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

/**
 * Is a combination of symbols and operators that the provider evaluates to obtain a single data
 * value. Simple expressions can be a single constant, variable, element, or scalar function.
 * Operators can be used to join two or more simple expressions into a complex expression.
 * <p>
 * Besides the name and the {@link ExpressionType} attributes each expression has a set of value
 * attributes that can be present by a per type basis. Other than that it can be negated as well so
 * the logic can be evaluated negatively.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
public class Expression
{
    /**
     * The expression type. Basically it's is a clue for the intended operation cared on by a given
     * {@link Expression} object.
     */
    public enum ExpressionType
    {
        UNDEFINED,
        OPEN_PARENTHESIS,
        CLOSE_PARENTHESIS,
        EQUAL,
        IN,
        LESS,
        LESS_EQUAL,
        GREATER,
        GREATER_EQUAL,
        BETWEEN,
        IS_NULL,
        IS_NOT_NULL,
        AND,
        OR
    }

    private final String name;

    private ExpressionType type = ExpressionType.UNDEFINED;

    private boolean not = false;

    private Object value = null;

    private Object[] values = null;

    private Object begin = null;

    private Object end = null;

    /**
     * Creates an expression object for the given type.
     * 
     * @param type
     *            the expression's type
     */
    public Expression(ExpressionType type)
    {
        this.name = null;
        this.type = type;
    }

    /**
     * Creates an expression object with given the element name
     * 
     * @param name
     *            the element name
     */
    public Expression(String name)
    {
        this.name = name;
    }

    void and()
    {
        type = ExpressionType.AND;
    }

    void between(Object begin, Object end)
    {
        type = ExpressionType.BETWEEN;
        this.begin = begin;
        this.end = end;
    }

    void equal(Object value)
    {
        type = ExpressionType.EQUAL;
        this.value = value;
    }

    void greater(Object value)
    {
        type = ExpressionType.GREATER;
        this.value = value;
    }

    void greaterEqual(Object value)
    {
        type = ExpressionType.GREATER_EQUAL;
        this.value = value;
    }

    void in(Object... values)
    {
        type = ExpressionType.IN;
        this.values = values;
    }

    void isNotNull()
    {
        type = ExpressionType.IS_NOT_NULL;
    }

    void isNull()
    {
        type = ExpressionType.IS_NULL;
    }

    void less(Object value)
    {
        type = ExpressionType.LESS;
        this.value = value;
    }

    void lessEqual(Object value)
    {
        type = ExpressionType.LESS_EQUAL;
        this.value = value;
    }

    void negate()
    {
        not = true;
    }

    public Object begin()
    {
        return begin;
    }

    public Object end()
    {
        return end;
    }

    public String name()
    {
        return name;
    }

    public boolean not()
    {
        return not;
    }

    public ExpressionType type()
    {
        return type;
    }

    public Object value()
    {
        return value;
    }

    public Object[] values()
    {
        return values;
    }
}