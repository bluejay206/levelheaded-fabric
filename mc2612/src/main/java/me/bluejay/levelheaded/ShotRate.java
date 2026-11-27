package me.bluejay.levelheaded;

/**
 * Client shot-rate limiter. 0 means unlimited.
 * Default 60 Hz. The gate uses client tick time, not wall clock.
 */
public final class ShotRate {

    private static int rateHz = 60;
    private static double lastAcceptedTicks = Double.NEGATIVE_INFINITY;

    private ShotRate() {}

    public static int rateHz() {
        return rateHz;
    }

    /** 0 = unlimited. Negative values are treated as unlimited. */
    public static void setRateHz(int hz) {
        rateHz = Math.max(0, hz);
    }

    public static boolean unlimited() {
        return rateHz <= 0;
    }

    /**
     * @param gameTicks client tick count plus partial tick
     * @return true if a shot may be saved now
     */
    public static boolean allow(double gameTicks) {
        if (rateHz <= 0) {
            lastAcceptedTicks = gameTicks;
            return true;
        }
        double minInterval = 20.0 / rateHz;
        if (gameTicks - lastAcceptedTicks < minInterval) {
            return false;
        }
        lastAcceptedTicks = gameTicks;
        return true;
    }

    public static String statusLine() {
        return rateHz <= 0 ? "Shot rate: unlimited" : "Shot rate: " + rateHz + " Hz";
    }
}