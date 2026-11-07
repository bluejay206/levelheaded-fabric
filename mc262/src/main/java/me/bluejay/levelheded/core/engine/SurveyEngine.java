package me.bluejay.levelheaded.core.engine;

import me.bluejay.levelheaded.SurveyState;
import me.bluejay.levelheaded.core.data.DistanceScale;
import me.bluejay.levelheaded.core.data.OccupyPoint;
import me.bluejay.levelheaded.core.data.Units;
import me.bluejay.levelheaded.core.math.Geodesic;
import me.bluejay.levelheaded.core.math.SurveyMath;
import me.bluejay.levelheaded.core.math.projection.MapProjection;
import me.bluejay.levelheaded.core.math.projection.Projections;

/**
 * Session survey engine: occupy + units + grid / ground / geodesic horizontal scale.
 */
public class SurveyEngine {

    private OccupyPoint occupy;
    private Units units = Units.METERS;

    public void setOccupy(OccupyPoint occupy) {
        this.occupy = occupy;
    }

    public OccupyPoint getOccupy() {
        return occupy;
    }

    public boolean hasOccupy() {
        return occupy != null;
    }

    public void setUnits(Units units) {
        if (units != null) {
            this.units = units;
        }
    }

    public Units getUnits() {
        return units;
    }

    /**
     * Measure from current occupy to target.
     * <ul>
     *   <li>GRID — raw block metres</li>
     *   <li>GROUND — horizontal × worldScale×cos(φ)</li>
     *   <li>GEODESIC — WGS84 Vincenty after active projection inverse</li>
     * </ul>
     * Vertical (ΔUp / cut-fill) is never scaled.
     */
    public SurveyMath.Result measure(double toX, double toY, double toZ) {
        if (occupy == null) {
            throw new IllegalStateException("No occupy point set");
        }

        DistanceScale scale = SurveyState.SCALE;

        if (scale.isGeodesic()) {
            return measureGeodesic(toX, toY, toZ, scale.worldScale());
        }

        SurveyMath.Result raw = SurveyMath.compute(
                occupy.x(), occupy.y(), occupy.z(),
                toX, toY, toZ
        );

        double f = scale.horizontalFactor();
        if (f == 1.0) {
            return raw;
        }

        double dN = raw.deltaNorth() * f;
        double dE = raw.deltaEast() * f;
        double dU = raw.deltaUp();
        double hz = raw.horizontalDistance() * f;
        double slope = Math.sqrt(hz * hz + dU * dU);

        double azimuth = raw.azimuthDeg();
        double bearing = azimuth > 180.0 ? azimuth - 360.0 : azimuth;

        double zenith;
        if (slope < 1e-9) {
            zenith = 90.0;
        } else {
            zenith = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dU / slope))));
        }

        return new SurveyMath.Result(dN, dE, dU, hz, slope, azimuth, zenith, bearing);
    }

    /**
     * Block coords → active projection lat/lon → Vincenty horizontal distance and azimuth.
     * ΔN / ΔE reconstructed from geodesic length and forward azimuth.
     * ΔUp remains block ΔY (metres if height is 1:1).
     */
    private SurveyMath.Result measureGeodesic(double toX, double toY, double toZ, double worldScale) {
        MapProjection proj = Projections.current();
        double lat1 = proj.blockZToLat(occupy.x(), occupy.z(), worldScale);
        double lon1 = proj.blockXToLongitude(occupy.x(), occupy.z(), worldScale);
        double lat2 = proj.blockZToLat(toX, toZ, worldScale);
        double lon2 = proj.blockXToLongitude(toX, toZ, worldScale);

        double dU = toY - occupy.y();

        Geodesic.Inverse inv = Geodesic.inverse(lat1, lon1, lat2, lon2);
        double hz;
        double azimuth;
        if (inv == null) {
            hz = Geodesic.haversineMeters(lat1, lon1, lat2, lon2);
            // fallback azimuth from local ENU
            SurveyMath.Result raw = SurveyMath.compute(
                    occupy.x(), occupy.y(), occupy.z(), toX, toY, toZ);
            azimuth = raw.azimuthDeg();
        } else {
            hz = inv.distanceMeters();
            azimuth = inv.forwardAzimuthDeg();
        }

        double azRad = Math.toRadians(azimuth);
        double dN = hz * Math.cos(azRad);
        double dE = hz * Math.sin(azRad);

        double slope = Math.sqrt(hz * hz + dU * dU);
        double bearing = azimuth > 180.0 ? azimuth - 360.0 : azimuth;

        double zenith;
        if (slope < 1e-9) {
            zenith = 90.0;
        } else {
            zenith = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dU / slope))));
        }

        return new SurveyMath.Result(dN, dE, dU, hz, slope, azimuth, zenith, bearing);
    }
}