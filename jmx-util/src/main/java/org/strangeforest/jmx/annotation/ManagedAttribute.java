package org.strangeforest.jmx.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Retention(value = RUNTIME)
@Target(value = METHOD)
public @interface ManagedAttribute {
	boolean readable() default true;
	boolean writable() default true;
	String description() default "";
}
