package me.bluejay.levelheaded.core.math;

import me.bluejay.levelheaded.core.data.Units;


public final class SurveyMath {

    private SurveyMath() {}

    public record Result(
            double deltaNorth,      // +N = north
            double deltaEast,       // +E = east
            double deltaUp,         // +U = up (cut/fill related)
            double horizontalDistance,
            double slopeDistance,
            double azimuthDeg,      // 0–360, clockwise from north
            double zenithDeg,       // 0 = straight up, 90 = horizontal, 180 = straight down
            double bearingDeg       // -180 to +180 (optional convenience)
    ) {
        public String formatAzimuthDMS() {
            return toDMS(azimuthDeg);
        }

        public String formatZenithDMS() {
            return toDMS(zenithDeg);
        }

        /** Quadrant bearing, e.g. N28°36'25"W. */
        public String formatQuadrantBearing() {
            return AngleFormat.toQuadrantBearing(azimuthDeg);
        }

        public String formatBearingDMS() {
            return toDMS(Math.abs(bearingDeg)) + (bearingDeg < 0 ? " W" : " E");
        }

        public double horizontal(Units unit) {
            return unit.toUnit(horizontalDistance);
        }

        public double slope(Units unit) {
            return unit.toUnit(slopeDistance);
        }

        public double northing(Units unit) {
            return unit.toUnit(deltaNorth);
        }

        public double easting(Units unit) {
            return unit.toUnit(deltaEast);
        }

        public double cutFill(Units unit) {
            return unit.toUnit(deltaUp);   // positive = fill needed, negative = cut
        }
    }

    /**
     * Compute survey result from occupy point to target.
     * Coordinates are in Minecraft space (Y-up).
     * North = -Z, East = +X (standard Minecraft orientation).
     */
    public static Result compute(double fromX, double fromY, double fromZ,
                                 double toX, double toY, double toZ) {

        double dx = toX - fromX;          // East
        double dy = toY - fromY;          // Up
        double dz = toZ - fromZ;          // South (because +Z is south)

        double deltaEast  = dx;
        double deltaNorth = -dz;          // invert so +N is north
        double deltaUp    = dy;

        double horizontal = Math.sqrt(deltaEast * deltaEast + deltaNorth * deltaNorth);
        double slope      = Math.sqrt(horizontal * horizontal + deltaUp * deltaUp);

        // Azimuth: 0 = North, 90 = East, clockwise
        double azimuth = Math.toDegrees(Math.atan2(deltaEast, deltaNorth));
        if (azimuth < 0) azimuth += 360.0;

        // Zenith: 0 = up, 90 = horizontal, 180 = down
        double zenith;
        if (slope < 1e-9) {
            zenith = 90.0;
        } else {
            zenith = Math.toDegrees(Math.acos(deltaUp / slope));
        }

        // Bearing: -180..+180 (negative = west)
        double bearing = azimuth > 180 ? azimuth - 360 : azimuth;

        return new Result(
                deltaNorth,
                deltaEast,
                deltaUp,
                horizontal,
                slope,
                azimuth,
                zenith,
                bearing
        );
    }

    public static String toDMS(double degrees) {
        double abs = Math.abs(degrees);
        int d = (int) abs;
        double minFloat = (abs - d) * 60.0;
        int m = (int) minFloat;
        double s = (minFloat - m) * 60.0;
        return String.format("%d°%02d'%04.1f\"", d, m, s);
    }
}