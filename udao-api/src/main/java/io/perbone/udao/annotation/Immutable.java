
package io.perbone.udao.annotation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.constraints.NotNull;

/**
 * Decorates a field annotated with {@link Element} whose state cannot be modified after it is
 * created.
 * <p>
 * Although {@code Immutable} does not impose a {@link NotNull} field, once the field state have
 * been persisted into the underling storage its value will remain for all the bean life cycle.
 * <p>
 * Be aware that the framework cannot guarantee this behavior alone. It depends on the
 * {@link DataProvider} implementation to be compliance with this contract.
 * 
 * @author Paulo Perbone <pauloperbone@yahoo.com>
 * @since 0.1.0
 */
@Target({ ANNOTATION_TYPE, TYPE, FIELD })
@Retention(RUNTIME)
public @interface Immutable
{
}