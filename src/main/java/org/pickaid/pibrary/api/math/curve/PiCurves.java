package org.pickaid.pibrary.api.math.curve;

/**
 * Built-in curve shapes for the common "progress in, shaped progress out"
 * problem.
 *
 * <p>Use this class when you already have a normalized value such as charge
 * progress, fade progress, selection weight progress, or animation progress,
 * and a straight line feels wrong. Common modding cases are:</p>
 * <ul>
 *     <li>a spell charge-up that should start slow and ramp hard near the
 *     end;</li>
 *     <li>a HUD highlight that should appear fast and then settle softly;</li>
 *     <li>a world overlay alpha that should ease in and out instead of
 *     snapping linearly.</li>
 * </ul>
 *
 * <p>The returned curves are stateless and cheap to request inline. Inputs are
 * usually normalized to {@code 0.0} through {@code 1.0}, but the helpers do
 * not clamp for you because some callers want deliberate extrapolation outside
 * that range.</p>
 */
public final class PiCurves {
    private PiCurves() {
    }

    /**
     * Returns the identity curve.
     *
     * <p>Use this when you need a {@link PiCurve} instance but do not want any
     * shaping yet, or when one phase inside a piecewise curve should stay
     * perfectly linear.</p>
     *
     * <p>Example: if a bar fill is already correct as-is, {@code sample(0.25)}
     * returns {@code 0.25}.</p>
     *
     * @return a curve whose output is exactly the input
     */
    public static PiCurve linear() {
        return input -> input;
    }

    /**
     * Returns a quadratic ease-in curve.
     *
     * <p>Use this when something should feel restrained at first and gain force
     * near the end. Typical cases are spell wind-up, danger buildup, or an
     * overlay that should emerge gently before becoming obvious.</p>
     *
     * <p>Example: {@code sample(0.5)} returns {@code 0.25}, so halfway through
     * the input timeline the visible effect still feels early.</p>
     *
     * @return a curve that squares the input
     */
    public static PiCurve easeInQuad() {
        return input -> input * input;
    }

    /**
     * Returns a quadratic ease-out curve.
     *
     * <p>Use this when the effect should react immediately and spend the rest
     * of the time settling. Common cases are tooltip fade-outs, recoil recovery,
     * or screen markers that should snap near the target quickly and then stop
     * feeling harsh.</p>
     *
     * <p>Example: {@code sample(0.5)} returns {@code 0.75}, so the output feels
     * almost finished by the midpoint.</p>
     *
     * @return a curve that eases out with a quadratic shape
     */
    public static PiCurve easeOutQuad() {
        return input -> 1.0D - (1.0D - input) * (1.0D - input);
    }

    /**
     * Returns a quadratic ease-in-out curve.
     *
     * <p>Use this when both ends should feel soft but you still want a little
     * more punch through the middle than {@link #smoothstep()} gives. It fits
     * overlay alpha, HUD scale pulses, and simple timing curves that should
     * accelerate and then decelerate clearly.</p>
     *
     * @return a curve with soft start and end and a faster middle
     */
    public static PiCurve easeInOutQuad() {
        return input -> input < 0.5D
            ? 2.0D * input * input
            : 1.0D - Math.pow(-2.0D * input + 2.0D, 2.0D) * 0.5D;
    }

    /**
     * Returns a cubic ease-in curve.
     *
     * <p>Use this when the early part should stay even more restrained than
     * {@link #easeInQuad()}. This is useful for charge-up bars, danger buildup,
     * or effects that should feel dormant until late in the timeline.</p>
     *
     * @return a stronger ease-in curve
     */
    public static PiCurve easeInCubic() {
        return input -> input * input * input;
    }

    /**
     * Returns a cubic ease-out curve.
     *
     * <p>Use this when the response should happen very quickly and spend the
     * rest of the time settling. It works well for impact flashes, recoil
     * recovery, and markers that should snap near their final state almost
     * immediately.</p>
     *
     * @return a stronger ease-out curve
     */
    public static PiCurve easeOutCubic() {
        return input -> 1.0D - Math.pow(1.0D - input, 3.0D);
    }

    /**
     * Returns a cubic ease-in-out curve.
     *
     * <p>Use this when you want the same soft start and end as the quadratic
     * form, but with a harder contrast between the calm edges and the fast
     * middle. This is a good default for many "feels animated" numeric
     * transitions.</p>
     *
     * @return a stronger ease-in-out curve
     */
    public static PiCurve easeInOutCubic() {
        return input -> input < 0.5D
            ? 4.0D * input * input * input
            : 1.0D - Math.pow(-2.0D * input + 2.0D, 3.0D) * 0.5D;
    }

    /**
     * Returns the classic smoothstep curve.
     *
     * <p>Use this for the broad middle ground: not as aggressive as dedicated
     * ease-in or ease-out, but much softer than a straight line. It fits
     * alpha fades, scale pulses, and camera-adjacent UI transitions where both
     * the start and the end should feel calm.</p>
     *
     * <p>Example: {@code sample(0.5)} still returns {@code 0.5}, but values
     * near {@code 0.0} and {@code 1.0} flatten out so the transition does not
     * jerk at the ends.</p>
     *
     * @return a smooth cubic transition curve
     */
    public static PiCurve smoothstep() {
        return input -> input * input * (3.0D - 2.0D * input);
    }

    /**
     * Returns the classic smootherstep curve.
     *
     * <p>This is the "calmer than smoothstep" option. Use it when the ends
     * should flatten even more aggressively, such as delicate alpha fades,
     * tooltip movement, or camera-adjacent UI where harsh edge velocity looks
     * wrong immediately.</p>
     *
     * @return a quintic smooth transition curve
     */
    public static PiCurve smootherstep() {
        return input -> input * input * input * (input * (input * 6.0D - 15.0D) + 10.0D);
    }
}
