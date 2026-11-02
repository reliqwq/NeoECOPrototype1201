package cn.dancingsnow.neoecoprototype1201.api;

/**
 * How a host that grew a mind feels about its situation, and what that does to its idle draw.
 *
 * <p>Deliberately a pure function of what the machine can already read about itself: no event counters, no
 * history, nothing to persist. A mood that accumulates becomes a stat the player farms while standing
 * next to it, and a mood with history becomes a field that has to survive every save change. Recomputed
 * from the present, an old world simply starts having moods on its next tick.</p>
 *
 * <p>Work is the rung that decides, then coolant: something running is happy whatever the bath holds,
 * nothing running with coolant in the bath is slacking, and neither is calm. It used to add the two axes
 * as 0.6 and 0.4, which paid a discount to a dry machine and gave a bath with no work the middle face -
 * so the face and the number printed beside it in the same tooltip line told different stories.</p>
 *
 * <p>Only ever saves power: the factor is capped at one, so a machine in a bad situation costs exactly
 * what a machine without a mind costs, and never more. That is what keeps this a reward for building the
 * structure well rather than a tax on the 1/16 roll.</p>
 */
public final class MachineMood {

    /** Nothing running and nothing to cool it with: the bottom face, and no saving at all. */
    public static final double CALM = 0.0D;
    /** Nothing running, but the bath has coolant in it: the middle face, half the saving. */
    public static final double SLACKING = 0.5D;
    /** Something running: the top face whatever the bath holds, and the full saving. */
    public static final double HAPPY = 1.0D;

    /** The three mood words, in rung order. Indexed, never switched over, like everything else here. */
    private static final String[] WORD_KEY = {
            "machine_mood.neoecoprototype1201.calm",
            "machine_mood.neoecoprototype1201.slacking",
            "machine_mood.neoecoprototype1201.happy",
    };

    /** The translation key for the mood word of a display band, clamped so a stray tier cannot read out. */
    public static String wordKey(int tier) {
        return WORD_KEY[Math.max(0, Math.min(WORD_KEY.length - 1, tier))];
    }

    private MachineMood() {
    }

    /**
     * @param hasMind           whether this host rolled a face; without one it is an ordinary machine
     * @param coolantFraction   how full the coolant bath is; above zero is what the middle rung asks for
     * @param busy              whether it is running work right now
     * @param formed            whether the structure is up at all
     * @param maxSavingPercent  the largest share of idle draw a good mood may take off
     * @return the multiplier for the baseline idle draw, in {@code [1 - maxSavingPercent, 1]}
     */
    public static double idlePowerFactor(boolean hasMind, double coolantFraction, boolean busy,
                                         boolean formed, double maxSavingPercent) {
        if (!hasMind || !formed) {
            return 1.0D;
        }
        return 1.0D - clamp01(maxSavingPercent) * mood(coolantFraction, busy);
    }

    /** The mood itself, one of the three rungs, for the face band and the tooltip word alike. */
    public static double mood(double coolantFraction, boolean busy) {
        if (busy) {
            return HAPPY;
        }
        return coolantFraction > 0.0D ? SLACKING : CALM;
    }

    /** How many faces the art has: tier 0 is the one that shipped first. */
    public static final int TIER_COUNT = 3;

    /**
     * Mood needed to <em>reach</em> tier 1 and tier 2, and the lower mood at which each is <em>kept</em>.
     * The gap is the point: a bath crosses empty while the machine is running, and a job starts and stops,
     * so the mood walks straight across whatever threshold is set - two thresholds would let it rewrite
     * its block state once a second.
     */
    private static final double[] RISE = {0.34D, 0.67D};
    private static final double[] FALL = {0.30D, 0.63D};

    /**
     * The display band for a mood, given the band it is already showing. Monotonic in mood, and a value
     * inside the gap between {@link #FALL} and {@link #RISE} leaves the previous answer alone.
     */
    public static int tierOf(double mood, int previous) {
        int tier = previous < 0 ? 0 : Math.min(TIER_COUNT - 1, previous);
        while (tier < RISE.length && mood >= RISE[tier]) {
            tier++;
        }
        while (tier > 0 && mood < FALL[tier - 1]) {
            tier--;
        }
        return tier;
    }

    private static double clamp01(double value) {
        return value < 0.0D ? 0.0D : Math.min(1.0D, value);
    }
}
