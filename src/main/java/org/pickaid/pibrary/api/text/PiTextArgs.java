package org.pickaid.pibrary.api.text;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Named values for Pibrary text templates.
 *
 * <p>Placeholders use {@code {name}}. Plain text values are escaped when used
 * inside markup, resource values are kept raw so they can be used inside link
 * targets, and component values are inserted back into the parsed component
 * tree without losing their style.</p>
 */
public final class PiTextArgs {
    private static final char TOKEN_START = '\uE000';
    private static final char TOKEN_END = '\uE001';

    private final Map<String, Value> values = new LinkedHashMap<>();

    private PiTextArgs() {
    }

    public static PiTextArgs of() {
        return new PiTextArgs();
    }

    public static PiTextArgs empty() {
        return new PiTextArgs();
    }

    public PiTextArgs text(String name, String value) {
        return put(name, Value.text(value));
    }

    public PiTextArgs number(String name, Number value) {
        Objects.requireNonNull(value, "value");
        return put(name, Value.text(value.toString()));
    }

    public PiTextArgs resource(String name, ResourceLocation value) {
        Objects.requireNonNull(value, "value");
        return put(name, Value.raw(value.toString()));
    }

    public PiTextArgs raw(String name, String value) {
        return put(name, Value.raw(value));
    }

    public PiTextArgs markup(String name, String value) {
        return put(name, Value.markup(value));
    }

    public PiTextArgs component(String name, Component value) {
        return put(name, Value.component(value));
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public boolean contains(String name) {
        return values.containsKey(requireName(name));
    }

    public Set<String> names() {
        return Set.copyOf(values.keySet());
    }

    public Resolved resolvePlain(String template) {
        return resolve(template, Mode.PLAIN);
    }

    public Resolved resolveMarkup(String template) {
        return resolve(template, Mode.MARKUP);
    }

    private PiTextArgs put(String name, Value value) {
        String cleanName = requireName(name);
        Objects.requireNonNull(value, "value");
        Value previous = values.putIfAbsent(cleanName, value);
        if (previous != null) {
            throw new IllegalArgumentException("duplicate text argument: " + cleanName);
        }
        return this;
    }

    private Resolved resolve(String template, Mode mode) {
        Objects.requireNonNull(template, "template");
        StringBuilder out = new StringBuilder(template.length());
        Map<String, Component> components = new LinkedHashMap<>();
        int tokenIndex = 0;
        for (int i = 0; i < template.length(); i++) {
            char current = template.charAt(i);
            if (current != '{') {
                out.append(current);
                continue;
            }
            int end = template.indexOf('}', i + 1);
            if (end < 0) {
                throw new IllegalArgumentException("unclosed text placeholder in: " + template);
            }
            String name = template.substring(i + 1, end).trim();
            requireName(name);
            Value value = values.get(name);
            if (value == null) {
                throw new IllegalArgumentException("missing text argument: " + name);
            }
            if (value.component() == null) {
                out.append(value.render(mode));
            } else {
                String token = token(tokenIndex++);
                out.append(token);
                components.put(token, value.component());
            }
            i = end;
        }
        return new Resolved(out.toString(), Map.copyOf(components));
    }

    public static Set<String> placeholders(String template) {
        Objects.requireNonNull(template, "template");
        Set<String> result = new LinkedHashSet<>();
        for (int i = 0; i < template.length(); i++) {
            if (template.charAt(i) != '{') {
                continue;
            }
            int end = template.indexOf('}', i + 1);
            if (end < 0) {
                throw new IllegalArgumentException("unclosed text placeholder in: " + template);
            }
            result.add(requireName(template.substring(i + 1, end).trim()));
            i = end;
        }
        return Set.copyOf(result);
    }

    public static boolean containsComponentToken(String text) {
        Objects.requireNonNull(text, "text");
        return text.indexOf(TOKEN_START) >= 0 && text.indexOf(TOKEN_END) >= 0;
    }

    private static String token(int index) {
        return Character.toString(TOKEN_START) + index + TOKEN_END;
    }

    private static String requireName(String name) {
        String result = Objects.requireNonNull(name, "name").trim();
        if (result.isEmpty()) {
            throw new IllegalArgumentException("text argument name must not be blank");
        }
        for (int i = 0; i < result.length(); i++) {
            char c = result.charAt(i);
            boolean valid = c >= 'a' && c <= 'z'
                    || c >= 'A' && c <= 'Z'
                    || c >= '0' && c <= '9'
                    || c == '_'
                    || c == '-'
                    || c == '.';
            if (!valid) {
                throw new IllegalArgumentException("invalid text argument name: " + result);
            }
        }
        return result;
    }

    private static String escapeMarkdown(String value) {
        String text = Objects.requireNonNull(value, "value");
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\'
                    || c == '*'
                    || c == '_'
                    || c == '['
                    || c == ']'
                    || c == '('
                    || c == ')'
                    || c == '#'
                    || c == '`'
                    || c == '<'
                    || c == '>'
                    || c == '!') {
                out.append('\\');
            }
            out.append(c);
        }
        return out.toString();
    }

    public record Resolved(String text, Map<String, Component> components) {
        public Resolved {
            text = Objects.requireNonNull(text, "text");
            components = Map.copyOf(Objects.requireNonNull(components, "components"));
        }

        public boolean hasComponents() {
            return !components.isEmpty();
        }
    }

    private enum Mode {
        PLAIN,
        MARKUP
    }

    private record Value(String plain, String markup, Component component) {
        private Value {
            if (component == null) {
                Objects.requireNonNull(plain, "plain");
                Objects.requireNonNull(markup, "markup");
            }
        }

        static Value text(String value) {
            Objects.requireNonNull(value, "value");
            return new Value(value, escapeMarkdown(value), null);
        }

        static Value raw(String value) {
            Objects.requireNonNull(value, "value");
            return new Value(value, value, null);
        }

        static Value markup(String value) {
            Objects.requireNonNull(value, "value");
            return new Value(value, value, null);
        }

        static Value component(Component value) {
            return new Value(null, null, Objects.requireNonNull(value, "value"));
        }

        String render(Mode mode) {
            return mode == Mode.MARKUP ? markup : plain;
        }
    }
}
