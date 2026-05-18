package org.pickaid.pibrary.api.generated;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks generated access classes intended for internal Pi library consumption.
 * Application code should use public registry constants, typed references, or
 * public DSLs instead of depending on generated helper class names.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface PiGeneratedAccess {
}
