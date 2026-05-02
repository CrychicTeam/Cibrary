package org.pickaid.pibrary.api.text.render;

import org.pickaid.pibrary.api.text.component.PiVisualTextContents;

/**
 * Draws one custom visual {@link net.minecraft.network.chat.ComponentContents}
 * node inside a single GUI text line.
 *
 * @param <T> visual contents type handled by this renderer
 */
@FunctionalInterface
public interface PiVisualTextRenderer<T extends PiVisualTextContents> {
    void render(PiVisualTextRenderContext context, T contents);
}
