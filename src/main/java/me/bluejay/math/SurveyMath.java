package me.bluejay.math;

import net.minecraft.world.World;

public class SurveyMath {

    public record SurveyResult(
            double bearing,           // 0-360°
            double azimuth,           // 0-360°
            double zenithAngle,       // Now correct: 0° = zenith (up), 90° = horizontal
            double slopeDistance,
            double horizontalDistance,
            double relativeNorthing,  // horizontal northing (+ = North)
            double relativeEasting,   // horizontal easting (+ = East)
            double transitElevation,  // Y of the occupy/transit point
            double targetElevation    // Y of the shot point
    ) {

        public String bearingDMS() {
            return formatBearingDMS(bearing);
        }

        public String azimuthDMS() {
            return formatAzimuthDMS(azimuth);
        }

        public String zenithAngleDMS() {
            return formatDMS(zenithAngle);
        }

        private String formatBearingDMS(double deg) {
            double absDeg = Math.abs(deg % 360);
            int d = (int) absDeg;
            double min = (absDeg - d) * 60;
            int m = (int) min;
            double sec = (min - m) * 60;
            int s = (int) Math.round(sec);

            String ns = (absDeg <= 90 || absDeg >= 270) ? "N" : "S";
            String ew = (absDeg <= 180) ? "E" : "W";

            double quad = absDeg;
            if (absDeg > 90 && absDeg <= 180) quad = 180 - absDeg;
            else if (absDeg > 180 && absDeg <= 270) quad = absDeg - 180;
            else if (absDeg > 270) quad = 360 - absDeg;

            d = (int) quad;
            return String.format("%s %02d°%02d'%02d\"%s", ns, d, m, s, ew);
        }

        private String formatAzimuthDMS(double deg) {
            double normalized = (deg % 360 + 360) % 360;
            int d = (int) normalized;
            double min = (normalized - d) * 60;
            int m = (int) min;
            double sec = (min - m) * 60;
            int s = (int) Math.round(sec);
            return String.format("%02d°%02d'%02d\"", d, m, s);
        }

        private String formatDMS(double degrees) {
            double absDeg = Math.abs(degrees);
            int d = (int) absDeg;
            double min = (absDeg - d) * 60;
            int m = (int) min;
            double sec = (min - m) * 60;
            int s = (int) Math.round(sec);
            return String.format("%02d°%02d'%02d\"", d, m, s);
        }

        // Fixed: Cut/Fill now relative to transit Y elevation
        public String cutFillString(double targetY) {
            double diff = targetY - this.transitElevation;
            if (Math.abs(diff) < 0.01) return "0.00";
            return diff < 0
                    ? String.format("C-%.2f", Math.abs(diff))
                    : String.format("F+%.2f", diff);
        }
    }

    public static SurveyResult compute(double x1, double y1, double z1, double x2, double y2, double z2, World world) {
        double dx = x2 - x1;   // East
        double dy = y2 - y1;   // Vertical difference (positive = up)
        double dz = z2 - z1;   // South

        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        double slopeDistance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        // Bearing & Azimuth (0° = North, clockwise)
        double bearing = Math.toDegrees(Math.atan2(dx, -dz));
        if (bearing < 0) bearing += 360.0;

        double azimuth = bearing;

        // === FIXED ZENITH ANGLE ===
        // Standard surveying convention:
        // 0°   = straight up (zenith)
        // 90°  = horizontal
        // 180° = straight down (nadir)
        double zenithAngle;
        if (horizontalDistance < 0.001) {
            // Straight up or down
            zenithAngle = (dy >= 0) ? 0.0 : 180.0;
        } else {
            // Normal case: angle from zenith
            double verticalAngleFromHorizontal = Math.toDegrees(Math.atan2(dy, horizontalDistance));
            zenithAngle = 90.0 - verticalAngleFromHorizontal;
        }

        double relativeNorthing = -dz;
        double relativeEasting  = dx;

        return new SurveyResult(
                bearing,
                azimuth,
                zenithAngle,
                slopeDistance,
                horizontalDistance,
                relativeNorthing,
                relativeEasting,
                y1,      // transitElevation = occupy Y
                y2       // targetElevation = shot Y
        );
    }
}