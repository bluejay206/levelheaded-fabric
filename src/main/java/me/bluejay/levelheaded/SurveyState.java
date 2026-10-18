package me.bluejay.levelheaded;

import me.bluejay.levelheaded.core.data.OccupyPoint;
import me.bluejay.levelheaded.core.data.Units;
import me.bluejay.levelheaded.core.engine.SurveyEngine;
import me.bluejay.levelheaded.core.data.DistanceScale;

/**
 * Simple static holder for the current survey session.
 * Later this can be replaced with player-attached data or networking.
 */
public final class SurveyState {

    public static final SurveyEngine ENGINE = new SurveyEngine();

    static {
        ENGINE.setUnits(Units.METERS);
    }
    public static final DistanceScale SCALE = new DistanceScale();

    /** Next V press should start a new shots_<timestamp>.csv. */
    private static boolean newLog = true;

    private SurveyState() {}

    public static void setOccupy(double x, double y, double z, String description) {
        ENGINE.setOccupy(new OccupyPoint(x, y, z, description));
        LevelHeaded.LOGGER.info("Occupy set to {}, {}, {} ({})", x, y, z, description);
    }

    public static boolean hasOccupy() {
        return ENGINE.hasOccupy();
    }

    /** Clear occupy and start a new CSV on the next V press. */
    public static void resetSession() {
        ENGINE.setOccupy(null);
        newLog = true;
    }

    /** True once after {@link #resetSession()}. Cleared when read. */
    public static boolean consumeNewLog() {
        boolean v = newLog;
        newLog = false;
        return v;
    }
}
