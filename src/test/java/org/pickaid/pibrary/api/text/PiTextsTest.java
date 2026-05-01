package org.pickaid.pibrary.api.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class PiTextsTest {
    @Test
    void buildsCommonTranslationKeysAndComponents() {
        assertEquals("tooltip.example.fireball", PiTexts.tooltipKey("example", "fireball"));
        assertEquals("title.example.spell_book", PiTexts.titleKey("example", "spell_book"));
        assertEquals("message.example.cast_failed", PiTexts.messageKey("example", "cast_failed"));
        assertEquals("tooltip.example.fireball.cooldown", PiTexts.tooltipKey("example", "fireball/cooldown"));
        assertEquals("tooltip.example.fireball", PiTexts.tooltip("example", "fireball").getString());
        assertEquals("Counter", PiTexts.literal("Counter").getString());
    }

    @Test
    void buildsTranslationKeysFromResourceIds() {
        ResourceLocation fireball = ResourceLocation.fromNamespaceAndPath("example", "spell/fireball");

        assertEquals("spell.example.spell.fireball", PiTexts.translationKey("spell", fireball));
        assertEquals("tooltip.example.spell.fireball", PiTexts.tooltipKey(fireball));
        assertEquals("tooltip.example.spell.fireball", PiTexts.tooltip(fireball).getString());
    }

    @Test
    void namespaceShortcutKeepsModidOutOfCallSites() {
        PiTexts.Namespace text = PiTexts.namespace("example");

        assertEquals("tooltip.example.fireball", text.tooltipKey("fireball"));
        assertEquals("title.example.spell_book", text.title("spell_book").getString());
        assertEquals("message.example.cast_failed", text.message("cast_failed").getString());
    }

    @Test
    void joinsComponentsWithoutMutatingInputs() {
        Component joined = PiTexts.join(
                PiTexts.literal(", "),
                List.of(PiTexts.literal("fire"), PiTexts.literal("ice"), PiTexts.literal("arcane")));

        assertEquals("fire, ice, arcane", joined.getString());
    }

    @Test
    void buildsTooltipLinesAndStyledKeyValueRows() {
        List<Component> lines = PiTexts.lines(
                PiTexts.tooltip("example", "fireball"),
                PiTexts.keyValue(PiTexts.literal("Mana"), PiTexts.literal("8")),
                PiTexts.muted(PiTexts.literal("Hold shift for details")));

        assertEquals(3, lines.size());
        assertEquals("Mana: 8", lines.get(1).getString());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.GRAY), lines.get(1).getStyle().getColor());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.AQUA),
                lines.get(1).getSiblings().get(1).getStyle().getColor());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY), lines.get(2).getStyle().getColor());
        assertThrows(UnsupportedOperationException.class, () -> lines.add(PiTexts.literal("extra")));
    }

    @Test
    void buildsCopyableStyledText() {
        Component component = PiTexts.copyable("Copy id", "example:fireball");

        assertEquals("Copy id", component.getString());
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.AQUA), component.getStyle().getColor());
        assertEquals(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "example:fireball"),
                component.getStyle().getClickEvent());
        assertEquals(HoverEvent.Action.SHOW_TEXT, component.getStyle().getHoverEvent().getAction());
    }
}
