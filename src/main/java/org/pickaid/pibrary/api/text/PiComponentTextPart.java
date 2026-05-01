package org.pickaid.pibrary.api.text;

import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A rich text part backed directly by a vanilla component.
 *
 * @param component component to display
 */
public record PiComponentTextPart(Component component) implements PiTextPart {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("pibrary", "text");

    public PiComponentTextPart {
        component = Objects.requireNonNull(component, "component").copy();
    }

    @Override
    public ResourceLocation type() {
        return TYPE;
    }

    @Override
    public Component fallback() {
        return component.copy();
    }
}
