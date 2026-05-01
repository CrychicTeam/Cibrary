package org.pickaid.pibrary.api.text;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextColor;
import org.junit.jupiter.api.Test;

class PiTextsTest {
    @Test
    void buildsCommonTranslationKeysAndComponents() {
        assertEquals("tooltip.example.fireball", PiTexts.tooltipKey("example", "fireball"));
        assertEquals("title.example.spell_book", PiTexts.titleKey("example", "spell_book"));
        assertEquals("message.example.cast_failed", PiTexts.messageKey("example", "cast_failed"));
        assertEquals("tooltip.example.fireball", PiTexts.tooltip("example", "fireball").getString());
        assertEquals("Counter", PiTexts.literal("Counter").getString());
    }

    @Test
    void joinsComponentsWithoutMutatingInputs() {
        Component joined = PiTexts.join(
                PiTexts.literal(", "),
                List.of(PiTexts.literal("fire"), PiTexts.literal("ice"), PiTexts.literal("arcane")));

        assertEquals("fire, ice, arcane", joined.getString());
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
