package org.pickaid.pibrary.api.text.component;

import java.util.Objects;

/**
 * One parsed inline markup expression.
 *
 * <p>{@code [image](example:path.png)} becomes label {@code image} and target
 * {@code example:path.png}. The parser does not decide every possible meaning;
 * the active {@link PiTextMarkupScope} handles that.</p>
 */
public final class PiTextInline {
    private final String label;
    private final String target;

    public PiTextInline(String label, String target) {
        this.label = require(label, "label");
        this.target = require(target, "target");
    }

    public String label() {
        return label;
    }

    public String target() {
        return target;
    }

    private static String require(String value, String name) {
        String result = Objects.requireNonNull(value, name).trim();
        if (result.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return result;
    }
}
