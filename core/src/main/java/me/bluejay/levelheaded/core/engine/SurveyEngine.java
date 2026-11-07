package me.bluejay.levelheaded.core.engine;

import me.bluejay.levelheaded.core.data.DistanceScale;
import me.bluejay.levelheaded.core.data.OccupyPoint;
import me.bluejay.levelheaded.core.data.SurveyShot;
import me.bluejay.levelheaded.core.data.Units;
import me.bluejay.levelheaded.core.math.SurveyMath;

/**
 * Pure survey engine. No Minecraft imports, no SurveyState.
 * Owns its own DistanceScale.
 */
public final class SurveyEngine {
    private final DistanceScale scale = new DistanceScale();
    private Units units = Units.METERS;
    private OccupyPoint occupy;

    public DistanceScale getScale() {
        return scale;
    }

    public Units getUnits() {
        return units;
    }

    public void setUnits(Units units) {
        this.units = units != null ? units : Units.METERS;
    }

    public OccupyPoint getOccupy() {
        return occupy;
    }

    public void setOccupy(OccupyPoint occupy) {
        this.occupy = occupy;
    }

    public void clearOccupy() {
        this.occupy = null;
    }

    public SurveyShot measure(double targetX, double targetY, double targetZ, String description) {
        if (occupy == null) {
            return null;
        }

        SurveyMath.Result raw = SurveyMath.compute(
                occupy.x(), occupy.y(), occupy.z(),
                targetX, targetY, targetZ
        );

        double factor = scale.horizontalFactor();

        double scaledHz = raw.horizontalDistance() * factor;
        double scaledN  = raw.deltaNorth() * factor;
        double scaledE  = raw.deltaEast() * factor;
        double scaledSlope = Math.hypot(scaledHz, raw.deltaUp());

        return new SurveyShot(
                occupy,
                targetX,
                targetY,
                targetZ,
                description != null ? description : ""
        );
    }

    public SurveyShot measure(double targetX, double targetY, double targetZ) {
        return measure(targetX, targetY, targetZ, "");
    }

    public SurveyMath.Result scaledResult(double targetX, double targetY, double targetZ) {
        if (occupy == null) {
            return null;
        }

        SurveyMath.Result raw = SurveyMath.compute(
                occupy.x(), occupy.y(), occupy.z(),
                targetX, targetY, targetZ
        );

        double factor = scale.horizontalFactor();

        double scaledHz = raw.horizontalDistance() * factor;
        double scaledN  = raw.deltaNorth() * factor;
        double scaledE  = raw.deltaEast() * factor;
        double scaledSlope = Math.hypot(scaledHz, raw.deltaUp());

        return new SurveyMath.Result(
                scaledN,
                scaledE,
                raw.deltaUp(),
                scaledHz,
                scaledSlope,
                raw.azimuthDeg(),
                raw.zenithDeg(),
                raw.bearingDeg()
        );
    }
}