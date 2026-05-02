package org.pickaid.pibrary.api.text.component;

import java.util.Objects;
import net.minecraft.network.chat.MutableComponent;

/**
 * Entry point for Pibrary rich text markup.
 *
 * <p>The default implementation uses CommonMark, then maps the parsed AST into
 * Minecraft {@link net.minecraft.network.chat.Component} nodes. Standard
 * Markdown handles nested bold/italic/code/link structure; Pibrary-specific
 * inline content is handled by {@link PiTextMarkupScope}.</p>
 */
public final class PiTextMarkup {
    private static final PiTextMarkupParser DEFAULT_PARSER = PiCommonmarkTextMarkupParser.defaultParser();

    private PiTextMarkup() {
    }

    public static MutableComponent parse(String markup) {
        return parse(markup, PiTextMarkupScope.empty());
    }

    public static MutableComponent parse(String markup, PiTextMarkupScope scope) {
        return DEFAULT_PARSER.parse(markup, scope);
    }

    public static MutableComponent parse(String markup, PiTextMarkupScope scope, PiTextMarkupParser parser) {
        return Objects.requireNonNull(parser, "parser").parse(markup, scope);
    }
}
