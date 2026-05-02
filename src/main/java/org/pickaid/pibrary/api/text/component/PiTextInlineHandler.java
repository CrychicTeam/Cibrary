package org.pickaid.pibrary.api.text.component;

import net.minecraft.network.chat.MutableComponent;

/**
 * Converts parsed inline markup into a Minecraft component.
 */
@FunctionalInterface
public interface PiTextInlineHandler {
    MutableComponent create(PiTextInline inline, PiTextMarkupScope scope);
}
