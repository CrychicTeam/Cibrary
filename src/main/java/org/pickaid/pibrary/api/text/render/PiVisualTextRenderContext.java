package org.pickaid.pibrary.api.text.render;

import java.util.Objects;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Client draw context for one inline visual text node.
 *
 * @param graphics active GUI graphics
 * @param font font used by the surrounding line
 * @param x node left position
 * @param y node top position
 * @param color text color used by the surrounding line
 * @param shadow whether surrounding text is drawn with a shadow
 * @param gameTime current client/game tick used by animated contents
 */
public record PiVisualTextRenderContext(
        GuiGraphics graphics,
        Font font,
        int x,
        int y,
        int color,
        boolean shadow,
        long gameTime
) {
    public PiVisualTextRenderContext {
        Objects.requireNonNull(graphics, "graphics");
        Objects.requireNonNull(font, "font");
    }
}
