package org.pickaid.pibrary.api.math;

/**
 * Common curves for animation and gameplay falloff.
 *
 * <p>These helpers are intentionally simple and allocation-light. More complex
 * timeline systems can still build on the same {@link PiCurve} interface later.</p>
 */
public final class PiCurves {
    public static final PiCurve LINEAR = t -> t;
    public static final PiCurve SMOOTHSTEP = PiMath::smoothstep;
    public static final PiCurve SMOOTHERSTEP = PiMath::smootherstep;

    private PiCurves() {
    }

    /**
     * Constant curve.
     *
     * @param value value to return for every input
     * @return curve
     */
    public static PiCurve constant(double value) {
        return t -> value;
    }

    /**
     * Power ease-in curve.
     *
     * @param exponent exponent greater than zero
     * @return curve
     */
    public static PiCurve easeIn(double exponent) {
        requirePositiveExponent(exponent);
        return t -> Math.pow(PiMath.clamp01(t), exponent);
    }

    /**
     * Power ease-out curve.
     *
     * @param exponent exponent greater than zero
     * @return curve
     */
    public static PiCurve easeOut(double exponent) {
        requirePositiveExponent(exponent);
        return t -> 1.0D - Math.pow(1.0D - PiMath.clamp01(t), exponent);
    }

    /**
     * Symmetric power ease-in-out curve.
     *
     * @param exponent exponent greater than zero
     * @return curve
     */
    public static PiCurve easeInOut(double exponent) {
        requirePositiveExponent(exponent);
        return t -> {
            double x = PiMath.clamp01(t);
            if (x < 0.5D) {
                return 0.5D * Math.pow(x * 2.0D, exponent);
            }
            return 1.0D - 0.5D * Math.pow((1.0D - x) * 2.0D, exponent);
        };
    }

    /**
     * Cubic Bezier curve using only y control values.
     *
     * <p>This variant is intentionally small: it treats input progress as the
     * Bezier parameter and returns the Bezier y value. That is enough for most
     * game falloff and UI easing use cases without pulling in a heavier solver.</p>
     *
     * @param y1 first control point y
     * @param y2 second control point y
     * @return curve
     */
    public static PiCurve cubicBezierY(double y1, double y2) {
        return t -> {
            double x = PiMath.clamp01(t);
            double oneMinus = 1.0D - x;
            return 3.0D * oneMinus * oneMinus * x * y1
                    + 3.0D * oneMinus * x * x * y2
                    + x * x * x;
        };
    }

    private static void requirePositiveExponent(double exponent) {
        if (!(exponent > 0.0D) || !Double.isFinite(exponent)) {
            throw new IllegalArgumentException("exponent must be finite and > 0");
        }
    }
}
