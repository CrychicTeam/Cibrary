package org.pickaid.pibrary.api.targeting;

import java.util.Objects;

public record PiTargetQuery(
        PiTargetAnchor anchor,
        double range,
        boolean requireLineOfSight,
        boolean livingOnly,
        boolean includeCaster
) {
    public PiTargetQuery {
        Objects.requireNonNull(anchor, "anchor");
        if (range < 0.0D) {
            throw new IllegalArgumentException("range must be >= 0");
        }
    }
}
