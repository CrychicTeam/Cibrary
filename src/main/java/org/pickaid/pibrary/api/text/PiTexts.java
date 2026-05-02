package org.pickaid.pibrary.api.text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.locale.Language;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.text.component.PiTextMarkup;
import org.pickaid.pibrary.api.text.component.PiTextMarkupParser;
import org.pickaid.pibrary.api.text.component.PiTextMarkupScope;

/**
 * Small helpers around vanilla {@link Component}.
 *
 * <p>This class deliberately returns Minecraft components instead of wrapping
 * them. Callers can pass the result directly to item tooltips, screens, chat,
 * JEI adapters, and vanilla rendering code.</p>
 */
public final class PiTexts {
    private PiTexts() {
    }

    public static Namespace namespace(String modid) {
        return new Namespace(modid);
    }

    public static PiTextKey textKey(String key, String defaultText) {
        return new PiTextKey(key, defaultText);
    }

    public static String translationKey(String prefix, ResourceLocation id) {
        Objects.requireNonNull(id, "id");
        return translationKey(prefix, id.getNamespace(), id.getPath());
    }

    public static String translationKey(String prefix, String modid, String path) {
        return key(prefix, modid, path);
    }

    public static String tooltipKey(String modid, String path) {
        return key("tooltip", modid, path);
    }

    public static String tooltipKey(ResourceLocation id) {
        return translationKey("tooltip", id);
    }

    public static String titleKey(String modid, String path) {
        return key("title", modid, path);
    }

    public static String titleKey(ResourceLocation id) {
        return translationKey("title", id);
    }

    public static String messageKey(String modid, String path) {
        return key("message", modid, path);
    }

    public static String messageKey(ResourceLocation id) {
        return translationKey("message", id);
    }

    public static MutableComponent tooltip(String modid, String path, Object... args) {
        return translatable(tooltipKey(modid, path), args);
    }

    public static MutableComponent tooltip(ResourceLocation id, Object... args) {
        return translatable(tooltipKey(id), args);
    }

    public static MutableComponent title(String modid, String path, Object... args) {
        return translatable(titleKey(modid, path), args);
    }

    public static MutableComponent title(ResourceLocation id, Object... args) {
        return translatable(titleKey(id), args);
    }

    public static MutableComponent message(String modid, String path, Object... args) {
        return translatable(messageKey(modid, path), args);
    }

    public static MutableComponent message(ResourceLocation id, Object... args) {
        return translatable(messageKey(id), args);
    }

    public static MutableComponent literal(String text) {
        return Component.literal(Objects.requireNonNull(text, "text"));
    }

    public static MutableComponent translatable(String key, Object... args) {
        return Component.translatable(Objects.requireNonNull(key, "key"), args);
    }

    public static MutableComponent translatable(String key, String defaultText) {
        return Component.translatableWithFallback(
                Objects.requireNonNull(key, "key"),
                Objects.requireNonNull(defaultText, "defaultText"));
    }

    public static MutableComponent translatable(String key, String defaultText, PiTextArgs args) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(defaultText, "defaultText");
        Objects.requireNonNull(args, "args");
        if (args.isEmpty()) {
            return translatable(key, defaultText);
        }
        PiTextArgs.Resolved resolved = args.resolvePlain(resolveLanguageText(key, defaultText));
        return insertComponentArgs(literal(resolved.text()), resolved.components());
    }

    public static MutableComponent markup(String markup) {
        return PiTextMarkup.parse(markup);
    }

    public static MutableComponent markup(String markup, PiTextMarkupScope scope) {
        return PiTextMarkup.parse(markup, scope);
    }

    public static MutableComponent markup(String markup, PiTextMarkupScope scope, PiTextArgs args) {
        Objects.requireNonNull(markup, "markup");
        Objects.requireNonNull(scope, "scope");
        PiTextArgs.Resolved resolved = Objects.requireNonNull(args, "args").resolveMarkup(markup);
        return insertComponentArgs(markup(resolved.text(), scope), resolved.components());
    }

    public static MutableComponent markup(String markup, PiTextMarkupParser parser) {
        return PiTextMarkup.parse(markup, PiTextMarkupScope.empty(), parser);
    }

    public static MutableComponent markup(String markup, PiTextMarkupScope scope, PiTextMarkupParser parser) {
        return PiTextMarkup.parse(markup, scope, parser);
    }

    public static MutableComponent markupTranslatable(
            String key,
            String defaultText,
            PiTextMarkupScope scope,
            PiTextArgs args
    ) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(defaultText, "defaultText");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(args, "args");
        PiTextArgs.Resolved resolved = args.resolveMarkup(resolveLanguageText(key, defaultText));
        MutableComponent parsed = markup(resolved.text(), scope);
        return insertComponentArgs(parsed, resolved.components());
    }

    public static MutableComponent markupTranslatable(String key, String defaultText, PiTextMarkupScope scope) {
        return markupTranslatable(key, defaultText, scope, PiTextArgs.empty());
    }

    public static MutableComponent join(Component delimiter, Iterable<? extends Component> parts) {
        Objects.requireNonNull(delimiter, "delimiter");
        Objects.requireNonNull(parts, "parts");
        Iterator<? extends Component> iterator = parts.iterator();
        MutableComponent joined = Component.empty();
        if (!iterator.hasNext()) {
            return joined;
        }
        joined.append(iterator.next().copy());
        while (iterator.hasNext()) {
            joined.append(delimiter.copy());
            joined.append(iterator.next().copy());
        }
        return joined;
    }

    public static List<Component> lines(Component... lines) {
        Objects.requireNonNull(lines, "lines");
        List<Component> result = new ArrayList<>(lines.length);
        for (Component line : lines) {
            result.add(Objects.requireNonNull(line, "line").copy());
        }
        return List.copyOf(result);
    }

    public static MutableComponent keyValue(Component label, Component value) {
        MutableComponent row = Objects.requireNonNull(label, "label").copy().withStyle(ChatFormatting.GRAY);
        row.append(literal(": ").withStyle(ChatFormatting.GRAY));
        row.append(Objects.requireNonNull(value, "value").copy().withStyle(ChatFormatting.AQUA));
        return row;
    }

    public static MutableComponent muted(Component component) {
        return styled(component, ChatFormatting.DARK_GRAY);
    }

    public static MutableComponent copyable(String label, String value) {
        Objects.requireNonNull(value, "value");
        return literal(label).withStyle(Style.EMPTY
                .withColor(ChatFormatting.AQUA)
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, literal(value))));
    }

    private static MutableComponent styled(Component component, ChatFormatting formatting) {
        return Objects.requireNonNull(component, "component").copy().withStyle(formatting);
    }

    private static String resolveLanguageText(String key, String defaultText) {
        return Language.getInstance().getOrDefault(
                Objects.requireNonNull(key, "key"),
                Objects.requireNonNull(defaultText, "defaultText"));
    }

    private static MutableComponent insertComponentArgs(Component component, Map<String, Component> replacements) {
        Objects.requireNonNull(component, "component");
        Objects.requireNonNull(replacements, "replacements");
        if (replacements.isEmpty()) {
            return component.copy();
        }
        MutableComponent result = copyContentsWithComponentArgs(
                component.getContents(),
                component.getStyle(),
                replacements);
        for (Component sibling : component.getSiblings()) {
            result.append(insertComponentArgs(sibling, replacements));
        }
        return result;
    }

    private static MutableComponent copyContentsWithComponentArgs(
            ComponentContents contents,
            Style style,
            Map<String, Component> replacements
    ) {
        MutableComponent own = contents == ComponentContents.EMPTY
                ? Component.empty()
                : MutableComponent.create(contents);
        String text = own.getString();
        if (!PiTextArgs.containsComponentToken(text)) {
            return own.withStyle(style);
        }
        MutableComponent result = Component.empty().withStyle(style);
        int cursor = 0;
        while (cursor < text.length()) {
            Match match = findNextReplacement(text, cursor, replacements);
            if (match == null) {
                appendLiteralRange(result, text, cursor, text.length(), style);
                break;
            }
            appendLiteralRange(result, text, cursor, match.start(), style);
            MutableComponent inserted = match.component().copy();
            inserted.withStyle(inserted.getStyle().applyTo(style));
            result.append(inserted);
            cursor = match.end();
        }
        return result;
    }

    private static void appendLiteralRange(MutableComponent out, String text, int start, int end, Style style) {
        if (start >= end) {
            return;
        }
        out.append(literal(text.substring(start, end)).withStyle(style));
    }

    private static Match findNextReplacement(String text, int start, Map<String, Component> replacements) {
        Match result = null;
        for (Map.Entry<String, Component> entry : replacements.entrySet()) {
            int index = text.indexOf(entry.getKey(), start);
            if (index < 0) {
                continue;
            }
            if (result == null || index < result.start()) {
                result = new Match(index, index + entry.getKey().length(), entry.getValue());
            }
        }
        return result;
    }

    private static String key(String prefix, String modid, String path) {
        String cleanedPrefix = Objects.requireNonNull(prefix, "prefix").trim();
        String cleanedModid = Objects.requireNonNull(modid, "modid").trim();
        String cleanedPath = Objects.requireNonNull(path, "path").trim().replace('/', '.');
        if (cleanedPrefix.isEmpty()) {
            throw new IllegalArgumentException("prefix must not be blank");
        }
        if (cleanedModid.isEmpty()) {
            throw new IllegalArgumentException("modid must not be blank");
        }
        if (cleanedPath.isEmpty()) {
            throw new IllegalArgumentException("path must not be blank");
        }
        return cleanedPrefix + "." + cleanedModid + "." + cleanedPath;
    }

    public record Namespace(String modid) {
        public Namespace {
            modid = Objects.requireNonNull(modid, "modid").trim();
            if (modid.isEmpty()) {
                throw new IllegalArgumentException("modid must not be blank");
            }
        }

        public String translationKey(String prefix, String path) {
            return PiTexts.translationKey(prefix, modid, path);
        }

        public String tooltipKey(String path) {
            return PiTexts.tooltipKey(modid, path);
        }

        public String titleKey(String path) {
            return PiTexts.titleKey(modid, path);
        }

        public String messageKey(String path) {
            return PiTexts.messageKey(modid, path);
        }

        public MutableComponent tooltip(String path, Object... args) {
            return PiTexts.tooltip(modid, path, args);
        }

        public MutableComponent title(String path, Object... args) {
            return PiTexts.title(modid, path, args);
        }

        public MutableComponent message(String path, Object... args) {
            return PiTexts.message(modid, path, args);
        }
    }

    private record Match(int start, int end, Component component) {
    }
}
