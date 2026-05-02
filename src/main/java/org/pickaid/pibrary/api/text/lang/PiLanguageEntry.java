package org.pickaid.pibrary.api.text.lang;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.pickaid.pibrary.api.text.PiTextArgs;

/**
 * One translation key with its locale values kept together.
 */
public final class PiLanguageEntry {
    private final String key;
    private final String defaultText;
    private final Map<PiLocale, String> values;

    private PiLanguageEntry(Builder builder) {
        this.key = builder.key;
        this.defaultText = builder.defaultText;
        if (builder.values.isEmpty()) {
            throw new IllegalArgumentException("translation entry must have at least one locale value: " + key);
        }
        validatePlaceholders(key, defaultText, builder.values);
        this.values = Map.copyOf(builder.values);
    }

    public static Builder key(String key) {
        return new Builder(key);
    }

    public String key() {
        return key;
    }

    public Optional<String> defaultText() {
        return Optional.ofNullable(defaultText);
    }

    public Map<PiLocale, String> values() {
        return values;
    }

    public static String requireKey(String key) {
        String result = Objects.requireNonNull(key, "key").trim();
        if (result.isEmpty()) {
            throw new IllegalArgumentException("translation key must not be blank");
        }
        return result;
    }

    private static void validatePlaceholders(String key, String defaultText, Map<PiLocale, String> values) {
        Set<String> expected = defaultText == null ? null : PiTextArgs.placeholders(defaultText);
        String expectedSource = defaultText == null ? null : "default text";
        for (Map.Entry<PiLocale, String> entry : values.entrySet()) {
            Set<String> placeholders = PiTextArgs.placeholders(entry.getValue());
            if (expected == null) {
                expected = placeholders;
                expectedSource = "locale " + entry.getKey();
                continue;
            }
            if (!expected.equals(placeholders)) {
                throw new IllegalArgumentException(
                        "translation key " + key + " has placeholders " + placeholders
                                + " for locale " + entry.getKey()
                                + ", expected " + expected + " from " + expectedSource);
            }
        }
    }

    public static final class Builder {
        private final String key;
        private String defaultText;
        private final Map<PiLocale, String> values = new LinkedHashMap<>();

        private Builder(String key) {
            this.key = requireKey(key);
        }

        public Builder defaultText(String defaultText) {
            this.defaultText = Objects.requireNonNull(defaultText, "defaultText");
            return this;
        }

        public Builder locale(String locale, String value) {
            return locale(PiLocale.of(locale), value);
        }

        public Builder locale(PiLocale locale, String value) {
            Objects.requireNonNull(locale, "locale");
            Objects.requireNonNull(value, "value");
            String previous = values.putIfAbsent(locale, value);
            if (previous != null) {
                throw new IllegalArgumentException("duplicate locale " + locale + " for translation key " + key);
            }
            return this;
        }

        public PiLanguageEntry build() {
            return new PiLanguageEntry(this);
        }
    }
}
