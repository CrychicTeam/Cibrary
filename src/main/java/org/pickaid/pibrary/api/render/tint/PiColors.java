package org.pickaid.pibrary.api.render.tint;

import net.minecraft.util.Mth;
import org.pickaid.pibrary.api.math.PiMath;

/**
 * Small color helpers for model tint, HUD markers, and lightweight render code.
 *
 * <p>Minecraft tint handlers normally return {@code 0xRRGGBB}; returning
 * {@code -1} means "do not tint this layer". These helpers keep that convention
 * explicit and avoid copy-pasting channel math in every color provider.</p>
 */
public final class PiColors {
    public static final int NO_TINT = -1;
    public static final int WHITE = 0xFFFFFF;
    public static final int BLACK = 0x000000;

    private PiColors() {
    }

    public static int rgb(int red, int green, int blue) {
        return clamp8(red) << 16 | clamp8(green) << 8 | clamp8(blue);
    }

    public static int red(int color) {
        return color >> 16 & 0xFF;
    }

    public static int green(int color) {
        return color >> 8 & 0xFF;
    }

    public static int blue(int color) {
        return color & 0xFF;
    }

    /**
     * Linearly blends two RGB colors.
     *
     * @param from first color
     * @param to second color
     * @param t blend amount; values outside {@code [0, 1]} are clamped
     * @return blended RGB color
     */
    public static int mix(int from, int to, double t) {
        double amount = PiMath.clamp01(t);
        return rgb(
                (int) Math.round(PiMath.lerp(red(from), red(to), amount)),
                (int) Math.round(PiMath.lerp(green(from), green(to), amount)),
                (int) Math.round(PiMath.lerp(blue(from), blue(to), amount))
        );
    }

    /**
     * Multiplies two RGB colors channel by channel.
     *
     * <p>This is useful for combining a base animated color with a dye or block
     * entity color, like mana pools, flowers, or other dyeable blocks.</p>
     *
     * @param base first color
     * @param tint second color
     * @return multiplied RGB color
     */
    public static int multiply(int base, int tint) {
        return rgb(
                red(base) * red(tint) / 255,
                green(base) * green(tint) / 255,
                blue(base) * blue(tint) / 255
        );
    }

    public static int hsv(float hue, float saturation, float value) {
        return Mth.hsvToRgb(
                (float) PiMath.clamp01(hue),
                (float) PiMath.clamp01(saturation),
                (float) PiMath.clamp01(value)
        );
    }

    /**
     * Periodic color blend.
     *
     * @param first first color
     * @param second second color
     * @param time animation time
     * @param period animation period; must be positive
     * @return oscillating RGB color
     */
    public static int pulse(int first, int second, double time, double period) {
        if (!(period > 0.0D) || !Double.isFinite(period)) {
            throw new IllegalArgumentException("period must be finite and > 0");
        }
        double phase = (Math.sin(time / period * Math.PI * 2.0D) + 1.0D) * 0.5D;
        return mix(first, second, phase);
    }

    private static int clamp8(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
