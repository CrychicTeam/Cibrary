package org.pickaid.pibrary.api.text.component;

import net.minecraft.network.chat.MutableComponent;
import org.pickaid.pibrary.api.text.PiTexts;

/**
 * Parser backend for {@link PiTextMarkup}.
 *
 * <p>Pibrary ships a CommonMark-backed implementation by default. This
 * interface exists so tests, dev tools, or a project-specific screen system can
 * parse the same markup source through a stricter or more specialized backend
 * without changing {@link PiTexts#markup(String)} call sites.</p>
 */
@FunctionalInterface
public interface PiTextMarkupParser {
    MutableComponent parse(String markup, PiTextMarkupScope scope);
}
