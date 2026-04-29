package org.pickaid.pibrary.api.math.curve;

import java.util.List;
import java.util.Objects;
import org.pickaid.pibrary.api.math.core.PiRange;

/**
 * A multi-phase curve made from ordered segments.
 *
 * <p>Use this class when one curve shape is not enough for the whole domain.
 * Common cases are:</p>
 * <ul>
 *     <li>a charge-up that is linear early and slows near the end;</li>
 *     <li>a damage falloff that has one behavior at close range and another at
 *     long range;</li>
 *     <li>a UI or animation progress shape that changes behavior halfway
 *     through.</li>
 * </ul>
 *
 * <p>Each segment range acts like a local input window. When you sample this
 * curve, the matching segment first normalizes the input into that segment's
 * local {@code 0.0} to {@code 1.0} coordinate system and only then delegates
 * to the segment curve.</p>
 *
 * <p>Segments must be listed in ascending order and must touch exactly at their
 * boundaries. That rule keeps the curve easy to reason about: no gaps, no
 * ambiguous overlap, and one predictable answer for each interior input. When
 * two segments share a boundary, the earlier segment owns that shared boundary
 * value.</p>
 *
 * <p>Inputs below the first segment or above the last segment are handled by
 * the nearest edge segment. That still uses local normalization, so callers
 * get predictable extrapolation beyond the declared domain.</p>
 */
public final class PiPiecewiseCurve implements PiCurve {
    /**
     * One range-and-curve pair inside a piecewise curve.
     *
     * @param range the inclusive input range covered by this segment
     * @param curve the curve sampled after the input is normalized within this
     *     range
     */
    public record Segment(PiRange range, PiCurve curve) {
        /**
         * Creates one validated segment.
         *
         * @param range the inclusive input range for this segment
         * @param curve the curve used for this segment's inputs
         * @throws NullPointerException if {@code range} or {@code curve} is
         *     {@code null}
         */
        public Segment {
            Objects.requireNonNull(range, "range");
            Objects.requireNonNull(curve, "curve");
        }
    }

    private final List<Segment> segments;

    private PiPiecewiseCurve(List<Segment> segments) {
        this.segments = segments;
    }

    /**
     * Creates a validated piecewise curve from ordered segments.
     *
     * <p>The list must not be empty. Adjacent segments must touch exactly: the
     * next segment's minimum must equal the previous segment's maximum. That
     * rule rejects both gaps and overlaps up front, so later callers do not
     * have to guess which segment owns a value.</p>
     *
     * <p>Example:</p>
     * <pre>{@code
     * PiPiecewiseCurve curve = PiPiecewiseCurve.of(List.of(
     *     new PiPiecewiseCurve.Segment(new PiRange(0.0, 0.5), PiCurves.linear()),
     *     new PiPiecewiseCurve.Segment(new PiRange(0.5, 1.0), PiCurves.easeOutQuad())
     * ));
     * }</pre>
     *
     * @param segments the ordered segment list
     * @return a validated piecewise curve
     * @throws NullPointerException if {@code segments} is {@code null} or
     *     contains {@code null}
     * @throws IllegalArgumentException if the list is empty, contains gaps, or
     *     contains overlaps
     */
    public static PiPiecewiseCurve of(List<Segment> segments) {
        Objects.requireNonNull(segments, "segments");
        List<Segment> copy = List.copyOf(segments);
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("segments must not be empty");
        }

        for (int index = 1; index < copy.size(); index++) {
            PiRange previous = copy.get(index - 1).range();
            PiRange current = copy.get(index).range();
            if (current.min() < previous.max()) {
                throw new IllegalArgumentException("segments must not overlap");
            }
            if (current.min() > previous.max()) {
                throw new IllegalArgumentException("segments must not contain gaps");
            }
        }
        return new PiPiecewiseCurve(copy);
    }

    /**
     * Samples the piecewise curve.
     *
     * <p>If {@code input} lands inside a segment range, this method normalizes
     * that input inside the segment and then samples the segment curve with the
     * normalized value. In plain terms: each segment behaves like its own local
     * {@code 0..1} curve window.</p>
     *
     * @param input the scalar position to sample
     * @return the sampled curve value
     */
    @Override
    public double sample(double input) {
        for (Segment segment : this.segments) {
            if (segment.range().contains(input)) {
                return sampleSegment(segment, input);
            }
        }

        Segment first = this.segments.get(0);
        if (input < first.range().min()) {
            return sampleSegment(first, input);
        }

        return sampleSegment(this.segments.get(this.segments.size() - 1), input);
    }

    private static double sampleSegment(Segment segment, double input) {
        return segment.curve().sample(segment.range().normalize(input));
    }
}
