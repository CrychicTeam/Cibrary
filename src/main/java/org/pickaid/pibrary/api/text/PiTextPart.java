package org.pickaid.pibrary.api.text;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * One renderable piece inside a rich text flow.
 *
 * <p>This is the extension point for content that cannot be represented by a
 * vanilla {@link Component}, such as images, animated images, badges, icons, or
 * future effect-backed text. Implementations must still provide a vanilla
 * fallback for narration, search, tooltips, logs, and adapters that do not know
 * the custom part type.</p>
 */
public interface PiTextPart {
    ResourceLocation type();

    Component fallback();
}
