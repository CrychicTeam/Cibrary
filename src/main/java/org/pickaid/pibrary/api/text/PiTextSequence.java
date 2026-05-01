package org.pickaid.pibrary.api.text;

import java.util.List;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Ordered rich text parts using a composite shape.
 *
 * @param parts ordered parts
 */
public record PiTextSequence(List<PiTextPart> parts) implements PiTextPart {
    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("pibrary", "sequence");

    public PiTextSequence {
        Objects.requireNonNull(parts, "parts");
        parts.forEach(part -> Objects.requireNonNull(part, "part"));
        parts = List.copyOf(parts);
    }

    public static PiTextSequence of(PiTextPart... parts) {
        Objects.requireNonNull(parts, "parts");
        return new PiTextSequence(List.of(parts));
    }

    @Override
    public ResourceLocation type() {
        return TYPE;
    }

    @Override
    public Component fallback() {
        MutableComponent component = Component.empty();
        for (PiTextPart part : parts) {
            component.append(part.fallback());
        }
        return component;
    }
}
