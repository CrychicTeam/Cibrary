package org.pickaid.pibrary.api.text.component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

final class PiTextTarget {
    private final String resource;
    private final Map<String, String> options;

    private PiTextTarget(String resource, Map<String, String> options) {
        this.resource = resource;
        this.options = options;
    }

    static PiTextTarget parse(String rawTarget) {
        String raw = Objects.requireNonNull(rawTarget, "rawTarget").trim();
        int fragment = raw.indexOf('#');
        if (fragment < 0) {
            return new PiTextTarget(raw, Map.of());
        }
        String resource = raw.substring(0, fragment).trim();
        String optionText = raw.substring(fragment + 1).trim();
        Map<String, String> options = new LinkedHashMap<>();
        if (!optionText.isEmpty()) {
            for (String entry : optionText.split("[;&]")) {
                if (entry.isBlank()) {
                    continue;
                }
                String[] pair = entry.split("=", 2);
                String key = pair[0].trim().toLowerCase(Locale.ROOT);
                String value = pair.length == 2 ? pair[1].trim() : "true";
                if (key.isEmpty()) {
                    throw new IllegalArgumentException("inline option key must not be blank: " + rawTarget);
                }
                String previous = options.putIfAbsent(key, value);
                if (previous != null) {
                    throw new IllegalArgumentException("duplicate inline option: " + key);
                }
            }
        }
        return new PiTextTarget(resource, Map.copyOf(options));
    }

    String resource() {
        return resource;
    }

    Optional<Integer> width() {
        Optional<Size> size = sizeOption();
        return intOption("width").or(() -> size.map(Size::width));
    }

    Optional<Integer> height() {
        Optional<Size> size = sizeOption();
        return intOption("height").or(() -> size.map(Size::height));
    }

    Optional<Integer> intOption(String name) {
        String value = options.get(name);
        if (value == null) {
            return Optional.empty();
        }
        try {
            int parsed = Integer.parseInt(value);
            PiImageTextContents.requirePositive(parsed, name);
            return Optional.of(parsed);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("inline option " + name + " must be an integer: " + value, exception);
        }
    }

    Optional<Boolean> booleanOption(String name) {
        String value = options.get(name);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(Boolean.parseBoolean(value));
    }

    private Optional<Size> sizeOption() {
        String value = options.get("size");
        if (value == null) {
            return Optional.empty();
        }
        String[] parts = value.toLowerCase(Locale.ROOT).split("x", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("size option must look like 16x16: " + value);
        }
        try {
            return Optional.of(new Size(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim())));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("size option must contain integers: " + value, exception);
        }
    }

    private record Size(int width, int height) {
        private Size {
            PiImageTextContents.requirePositive(width, "width");
            PiImageTextContents.requirePositive(height, "height");
        }
    }
}
