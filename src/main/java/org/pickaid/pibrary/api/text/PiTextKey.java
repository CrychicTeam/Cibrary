package org.pickaid.pibrary.api.text;

import java.util.Objects;
import net.minecraft.network.chat.MutableComponent;
import org.pickaid.pibrary.api.text.component.PiTextMarkupScope;
import org.pickaid.pibrary.api.text.lang.PiLanguageEntry;

/**
 * A translation key paired with its default English-like text.
 *
 * <p>Use this when runtime code needs a stable key and a readable fallback at
 * the same call site. The default is also useful for language data generation,
 * because placeholder consistency can be checked before the JSON file is
 * written.</p>
 */
public record PiTextKey(String key, String defaultText) {
    public PiTextKey {
        key = PiLanguageEntry.requireKey(key);
        defaultText = Objects.requireNonNull(defaultText, "defaultText");
    }

    public MutableComponent component(PiTextArgs args) {
        return PiTexts.translatable(key, defaultText, args);
    }

    public MutableComponent component() {
        return component(PiTextArgs.empty());
    }

    public MutableComponent markup(PiTextMarkupScope scope, PiTextArgs args) {
        return PiTexts.markupTranslatable(key, defaultText, scope, args);
    }

    public MutableComponent markup(PiTextMarkupScope scope) {
        return markup(scope, PiTextArgs.empty());
    }
}
