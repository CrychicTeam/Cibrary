package org.pickaid.pibrary.api.text.lang;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.network.chat.MutableComponent;
import org.pickaid.pibrary.api.text.PiTextArgs;
import org.pickaid.pibrary.api.text.PiTextKey;
import org.pickaid.pibrary.api.text.PiTexts;
import org.pickaid.pibrary.api.text.component.PiTextMarkupScope;

/**
 * Translation catalog used by data generators.
 *
 * <p>The bundle is key-first: each key owns all known locale values. That keeps
 * generated text readable in code review and avoids long locale switch blocks.
 * Locale handling is open; callers can use any {@link PiLocale} code.</p>
 */
public final class PiLanguageBundle {
    private final String modid;
    private final List<PiLanguageEntry> entries;

    private PiLanguageBundle(Builder<?> builder) {
        this.modid = builder.modid;
        this.entries = validate(builder.entries);
    }

    public String modid() {
        return modid;
    }

    public List<PiLanguageEntry> allEntries() {
        return entries;
    }

    public Set<PiLocale> locales() {
        Set<PiLocale> result = new LinkedHashSet<>();
        for (PiLanguageEntry entry : entries) {
            result.addAll(entry.values().keySet());
        }
        return Set.copyOf(result);
    }

    public Map<String, String> entries(PiLocale locale) {
        Objects.requireNonNull(locale, "locale");
        Map<String, String> result = new LinkedHashMap<>();
        for (PiLanguageEntry entry : entries) {
            String value = entry.values().get(locale);
            if (value != null) {
                result.put(entry.key(), value);
            }
        }
        return Map.copyOf(result);
    }

    public Optional<String> text(PiLocale locale, String key) {
        Objects.requireNonNull(locale, "locale");
        String cleanKey = PiLanguageEntry.requireKey(key);
        for (PiLanguageEntry entry : entries) {
            if (entry.key().equals(cleanKey)) {
                return Optional.ofNullable(entry.values().get(locale));
            }
        }
        return Optional.empty();
    }

    public MutableComponent markup(PiLocale locale, String key, PiTextMarkupScope scope) {
        String value = text(locale, key).orElseThrow(() ->
                new IllegalArgumentException("missing translation " + key + " for locale " + locale));
        return PiTexts.markup(value, scope);
    }

    public MutableComponent markup(PiLocale locale, PiTextKey key, PiTextMarkupScope scope, PiTextArgs args) {
        Objects.requireNonNull(key, "key");
        String value = text(locale, key.key()).orElse(key.defaultText());
        return PiTexts.markup(value, scope, args);
    }

    private static List<PiLanguageEntry> validate(List<PiLanguageEntry> entries) {
        Map<String, Set<PiLocale>> seen = new LinkedHashMap<>();
        for (PiLanguageEntry entry : entries) {
            Set<PiLocale> locales = seen.computeIfAbsent(entry.key(), key -> new LinkedHashSet<>());
            for (PiLocale locale : entry.values().keySet()) {
                if (!locales.add(locale)) {
                    throw new IllegalArgumentException(
                            "duplicate locale " + locale + " for translation key " + entry.key());
                }
            }
        }
        return entries.stream()
                .sorted(Comparator.comparing(PiLanguageEntry::key))
                .toList();
    }

    public static class Builder<B extends Builder<B>> {
        private final String modid;
        private final List<PiLanguageEntry> entries = new ArrayList<>();

        protected Builder(String modid) {
            this.modid = PiLanguageEntry.requireKey(modid);
        }

        public final String modid() {
            return modid;
        }

        public final String key(String prefix, String path) {
            return PiTexts.translationKey(prefix, modid, path);
        }

        public B add(PiLanguageEntry entry) {
            entries.add(Objects.requireNonNull(entry, "entry"));
            return self();
        }

        public B entry(String key, Consumer<PiLanguageEntry.Builder> configure) {
            Objects.requireNonNull(configure, "configure");
            PiLanguageEntry.Builder builder = PiLanguageEntry.key(key);
            configure.accept(builder);
            return add(builder.build());
        }

        public B entry(PiTextKey key, Consumer<PiLanguageEntry.Builder> configure) {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(configure, "configure");
            PiLanguageEntry.Builder builder = PiLanguageEntry.key(key.key()).defaultText(key.defaultText());
            configure.accept(builder);
            return add(builder.build());
        }

        public B generic(String prefix, String path, Consumer<PiLanguageEntry.Builder> configure) {
            return entry(key(prefix, path), configure);
        }

        public B item(String path, Consumer<PiLanguageEntry.Builder> configure) {
            return generic("item", path, configure);
        }

        public B block(String path, Consumer<PiLanguageEntry.Builder> configure) {
            return generic("block", path, configure);
        }

        public B tooltip(String path, Consumer<PiLanguageEntry.Builder> configure) {
            return entry(PiTexts.tooltipKey(modid, path), configure);
        }

        public B message(String path, Consumer<PiLanguageEntry.Builder> configure) {
            return entry(PiTexts.messageKey(modid, path), configure);
        }

        public B title(String path, Consumer<PiLanguageEntry.Builder> configure) {
            return entry(PiTexts.titleKey(modid, path), configure);
        }

        public PiLanguageBundle build() {
            return new PiLanguageBundle(this);
        }

        @SuppressWarnings("unchecked")
        protected final B self() {
            return (B) this;
        }
    }

    public static final class RootBuilder extends Builder<RootBuilder> {
        public RootBuilder(String modid) {
            super(modid);
        }
    }
}
