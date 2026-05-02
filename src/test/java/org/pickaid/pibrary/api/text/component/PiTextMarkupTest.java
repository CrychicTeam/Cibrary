package org.pickaid.pibrary.api.text.component;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.text.PiTextArgs;
import org.pickaid.pibrary.api.text.PiTexts;

class PiTextMarkupTest {
    @Test
    void parsesMarkupIntoMinecraftComponentsAndVisualContents() {
        MutableComponent component = PiTexts.markup(
                "Cast **Fireball** [image](example:textures/gui/spell/fireball.png#size=24x12)");

        assertEquals("Cast Fireball [image]", component.getString());
        assertEquals(4, component.getSiblings().size());

        Component bold = component.getSiblings().get(1);
        assertEquals("Fireball", bold.getString());
        assertEquals(true, bold.getStyle().isBold());

        Component image = component.getSiblings().get(3);
        PiImageTextContents contents = assertInstanceOf(PiImageTextContents.class, image.getContents());
        assertEquals(ResourceLocation.parse("example:textures/gui/spell/fireball.png"), contents.texture());
        assertEquals(24, contents.width());
        assertEquals(12, contents.height());
        assertEquals("[image]", image.getString());
    }

    @Test
    void parsesNestedCommonmarkFormatting() {
        MutableComponent component = PiTexts.markup("Cast **Fire*ball*** and `blink`");

        assertEquals("Cast Fireball and blink", component.getString());
        Component strongText = component.getSiblings().get(1);
        assertEquals("Fire", strongText.getString());
        assertEquals(true, strongText.getStyle().isBold());
        Component nestedText = component.getSiblings().get(2);
        assertEquals("ball", nestedText.getString());
        assertEquals(true, nestedText.getStyle().isBold());
        assertEquals(true, nestedText.getStyle().isItalic());
        Component code = component.getSiblings().get(4);
        assertEquals(ChatFormatting.GRAY.getColor(), code.getStyle().getColor().getValue());
    }

    @Test
    void createsVanillaFallbackBeforeJsonSerializationOrNetworkUse() {
        MutableComponent component = PiTexts.markup("[image](example:textures/gui/spell/fireball.png)");

        assertThrows(IllegalArgumentException.class, () -> Component.Serializer.toJson(component));

        MutableComponent fallback = PiTextComponents.vanillaFallback(component);

        assertEquals("[image]", fallback.getString());
        assertDoesNotThrow(() -> Component.Serializer.toJson(fallback));
    }

    @Test
    void scopeCanRegisterCustomInlineHandlersWithoutChangingTheParser() {
        PiTextMarkupScope scope = PiTextMarkupScope.builder("example")
                .inline("cost", (inline, localScope) -> Component.literal("8 mana").withStyle(ChatFormatting.AQUA))
                .build();

        MutableComponent component = PiTexts.markup("Requires **[cost](example:fireball)**", scope);

        assertEquals("Requires 8 mana", component.getString());
        assertEquals(ChatFormatting.AQUA.getColor(), component.getSiblings().get(1).getStyle().getColor().getValue());
        assertEquals(true, component.getSiblings().get(1).getStyle().isBold());
    }

    @Test
    void supportsStandardMarkdownImageSyntax() {
        MutableComponent component = PiTexts.markup("Icon ![spell](example:textures/gui/spell/fireball.png)");

        PiImageTextContents contents = assertInstanceOf(
                PiImageTextContents.class,
                component.getSiblings().get(1).getContents());
        assertEquals(ResourceLocation.parse("example:textures/gui/spell/fireball.png"), contents.texture());
    }

    @Test
    void rendersNormalWebLinksAsClickableText() {
        MutableComponent component = PiTexts.markup("[manual](https://example.invalid/manual)");

        Component link = component.getSiblings().get(0);
        assertEquals("manual", link.getString());
        assertEquals(true, link.getStyle().isUnderlined());
        assertEquals(ClickEvent.Action.OPEN_URL, link.getStyle().getClickEvent().getAction());
    }

    @Test
    void rendersCommonmarkListsAsPlainComponentLines() {
        MutableComponent component = PiTexts.markup("- Fireball\n- Blink");

        assertEquals("- Fireball\n- Blink", component.getString());
    }

    @Test
    void escapedNewlineCreatesActualLineBreak() {
        MutableComponent component = PiTexts.markup("Line one\\nLine two");

        assertEquals("Line one\nLine two", component.getString());
    }

    @Test
    void parsesAnimatedImagesAsComponentContents() {
        MutableComponent component = PiTexts.markup(
                "[gif](example:textures/gui/spell/fireball_loop.png#size=20x10;frames=8;ticks=2;loop=false)");

        PiAnimatedImageTextContents contents = assertInstanceOf(
                PiAnimatedImageTextContents.class,
                component.getSiblings().get(0).getContents());
        assertEquals(20, contents.width());
        assertEquals(10, contents.height());
        assertEquals(8, contents.frameCount());
        assertEquals(2, contents.ticksPerFrame());
        assertEquals(false, contents.loop());
    }

    @Test
    void parserBackendCanBeReplacedForSpecializedTools() {
        MutableComponent component = PiTexts.markup("raw", (markup, scope) -> Component.literal("parsed:" + markup));

        assertEquals("parsed:raw", component.getString());
    }

    @Test
    void markupTranslatableUsesFallbackNamedArgsAndScopeRules() {
        ResourceLocation fireball = ResourceLocation.parse("example:fireball");
        PiTextMarkupScope scope = PiTextMarkupScope.builder("example")
                .inline("mana", (inline, localScope) -> {
                    assertEquals(fireball, localScope.resolveResource(inline.target()));
                    return Component.literal("8 mana").withStyle(ChatFormatting.AQUA);
                })
                .build();

        MutableComponent component = PiTexts.markupTranslatable(
                "tooltip.example.spell.fireball",
                "**{name}** ![image]({icon}#size=18x18)\\nCost: [mana]({spell})",
                scope,
                PiTextArgs.of()
                        .text("name", "Fire*ball")
                        .resource("icon", ResourceLocation.parse("example:textures/gui/spell/fireball.png"))
                        .resource("spell", fireball));

        assertEquals("Fire*ball [image]\nCost: 8 mana", component.getString());
        assertEquals(true, component.getSiblings().get(0).getStyle().isBold());
        assertEquals(false, component.getSiblings().get(0).getStyle().isItalic());
    }

    @Test
    void componentArgsKeepTheirStyleInsideMarkupFormatting() {
        MutableComponent component = PiTexts.markup(
                "Cast **{name}**",
                PiTextMarkupScope.empty(),
                PiTextArgs.of().component("name", Component.literal("Fireball").withStyle(ChatFormatting.RED)));

        assertEquals("Cast Fireball", component.getString());
        Component inserted = component.getSiblings().get(1).getSiblings().get(0);
        assertEquals(ChatFormatting.RED.getColor(), inserted.getStyle().getColor().getValue());
        assertEquals(true, inserted.getStyle().isBold());
    }
}
