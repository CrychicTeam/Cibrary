package org.pickaid.pibrary.api.facet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a living facet for descriptor generation.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface PiLivingFacet {
    /**
     * Namespace of the generated facet id.
     *
     * @return id namespace
     */
    String namespace();

    /**
     * Path of the generated facet id.
     *
     * @return id path
     */
    String path();
}
