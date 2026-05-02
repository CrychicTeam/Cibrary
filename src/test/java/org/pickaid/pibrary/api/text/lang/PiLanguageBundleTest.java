package org.pickaid.pibrary.api.text.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.Set;
import net.minecraft.network.chat.MutableComponent;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.text.PiTextKey;
import org.pickaid.pibrary.api.text.PiTexts;
import org.pickaid.pibrary.api.text.component.PiTextMarkupScope;

class PiLanguageBundleTest {
    @Test
    void groupsTranslationsByKeyAndKeepsLocalesOpen() {
        PiLocale enUs = PiLocale.of("en_us");
        PiLocale zhCn = PiLocale.of("zh_cn");
        PiLocale pirate = PiLocale.of("pirate");

        PiLanguageBundle bundle = PiLanguages.bundle("example")
                .item("spell_book", entry -> entry
                        .locale(enUs, "Spell Book")
                        .locale(zhCn, "法术书"))
                .tooltip("spell.fireball", entry -> entry
                        .locale(enUs, "Cast **Fireball** [image](example:textures/gui/spell/fireball.png)")
                        .locale(zhCn, "释放**火球**"))
                .message("cast.failed", entry -> entry.locale(pirate, "Not enough shiny mana"))
                .build();

        assertEquals(Set.of(enUs, zhCn, pirate), bundle.locales());
        assertEquals(Map.of(
                "item.example.spell_book", "Spell Book",
                "tooltip.example.spell.fireball", "Cast **Fireball** [image](example:textures/gui/spell/fireball.png)"
        ), bundle.entries(enUs));
        assertEquals(Map.of(
                "item.example.spell_book", "法术书",
                "tooltip.example.spell.fireball", "释放**火球**"
        ), bundle.entries(zhCn));
        assertEquals(Map.of(
                "message.example.cast.failed", "Not enough shiny mana"
        ), bundle.entries(pirate));

        MutableComponent parsed = bundle.markup(
                enUs,
                "tooltip.example.spell.fireball",
                PiTextMarkupScope.empty());
        assertEquals("Cast Fireball [image]", parsed.getString());
    }

    @Test
    void rejectsDuplicateValuesForTheSameKeyAndLocaleAtBuildTime() {
        PiLanguageBundle.Builder<?> builder = PiLanguages.bundle("example")
                .entry("message.example.cast.failed", entry -> entry.locale("en_us", "Not enough mana"))
                .entry("message.example.cast.failed", entry -> entry.locale("en_us", "No mana"));

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void builderCanBeExtendedWithProjectSpecificShortcuts() {
        SpellLanguages.Builder builder = new SpellLanguages.Builder("example");

        PiLanguageBundle bundle = builder
                .item("spell_book", entry -> entry.locale("en_us", "Spell Book"))
                .trait("legendary.undying", entry -> entry.locale("en_us", "Undying"))
                .spell("fireball", entry -> entry.locale("en_us", "Fireball"))
                .generic("config", "spell/fireball/max_mana", entry -> entry.locale("en_us", "Max Mana"))
                .entry("screen.example.spell_editor", entry -> entry.locale("en_us", "Spell Editor"))
                .build();

        assertEquals("trait.example.legendary.undying", builder.key("trait", "legendary.undying"));
        assertEquals(Map.of(
                "config.example.spell.fireball.max_mana", "Max Mana",
                "item.example.spell_book", "Spell Book",
                "screen.example.spell_editor", "Spell Editor",
                "spell.example.fireball", "Fireball",
                "trait.example.legendary.undying", "Undying"
        ), bundle.entries(PiLocale.EN_US));
    }

    @Test
    void textKeysKeepDefaultsAndValidatePlaceholderShape() {
        PiTextKey tooltip = PiTexts.textKey(
                "tooltip.example.spell.fireball",
                "**{name}** costs {mana} mana");

        PiLanguageBundle bundle = PiLanguages.bundle("example")
                .entry(tooltip, entry -> entry
                        .locale("en_us", "**{name}** costs {mana} mana")
                        .locale("zh_cn", "**{name}** 消耗 {mana} 魔力"))
                .build();

        PiLanguageEntry entry = bundle.allEntries().get(0);
        assertEquals(tooltip.key(), entry.key());
        assertEquals(tooltip.defaultText(), entry.defaultText().orElseThrow());

        PiLanguageBundle.Builder<?> broken = PiLanguages.bundle("example");
        assertThrows(IllegalArgumentException.class, () -> broken
                .entry(tooltip, entryBuilder -> entryBuilder
                        .locale("en_us", "**{name}** costs {mana} mana")
                        .locale("zh_cn", "**{name}** 消耗魔力")));
    }

    private static final class SpellLanguages {
        private SpellLanguages() {
        }

        private static final class Builder extends PiLanguageBundle.Builder<Builder> {
            private Builder(String modid) {
                super(modid);
            }

            private Builder trait(String path, java.util.function.Consumer<PiLanguageEntry.Builder> configure) {
                return generic("trait", path, configure);
            }

            private Builder spell(String path, java.util.function.Consumer<PiLanguageEntry.Builder> configure) {
                return generic("spell", path, configure);
            }
        }
    }
}
