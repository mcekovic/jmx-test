package org.strangeforest.jmx.annotation;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

@Retention(value = RUNTIME)
@Target(value = {TYPE, PARAMETER})
public @interface MBeanDescription {
	String value();
}