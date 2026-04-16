package org.pickaid.pibrary.api.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a living service for descriptor generation.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface PiLivingService {
    /**
     * Namespace of the generated service id.
     *
     * @return id namespace
     */
    String namespace();

    /**
     * Path of the generated service id.
     *
     * @return id path
     */
    String path();
}
