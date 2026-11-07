package me.bluejay.levelheaded;

import me.bluejay.levelheaded.core.data.DistanceScale;
import me.bluejay.levelheaded.core.data.OccupyPoint;
import me.bluejay.levelheaded.core.data.Units;
import me.bluejay.levelheaded.core.engine.SurveyEngine;

public final class SurveyState {
    public static final SurveyEngine ENGINE = new SurveyEngine();
    public static final DistanceScale SCALE = ENGINE.getScale();

    private SurveyState() {}

    public static void reset() {
        ENGINE.clearOccupy();
        ENGINE.setUnits(Units.METERS);
        SCALE.setGrid();
    }

    public static void setOccupy(double x, double y, double z, String description) {
        ENGINE.setOccupy(new OccupyPoint(x, y, z, description));
    }

    public static boolean hasOccupy() {
        return ENGINE.getOccupy() != null;
    }

    public static OccupyPoint getOccupy() {
        return ENGINE.getOccupy();
    }
}