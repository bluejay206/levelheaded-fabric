package me.bluejay.levelheaded.math;

import net.minecraft.world.World;

public class SurveyMath {

    public record SurveyResult(
            double bearing,
            double azimuth,
            double zenithAngle,
            double slopeDistance,
            double horizontalDistance,
            double relativeNorthing,
            double relativeEasting,
            double transitElevation,
            double targetElevation
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
            double normalized = (deg % 360 + 360) % 360;   // Force 0-360
            double absDeg = normalized;

            // Determine quadrant letters
            String ns = (absDeg <= 90 || absDeg >= 270) ? "N" : "S";
            String ew = (absDeg <= 180) ? "E" : "W";

            // Convert to quadrant angle (0-90°)
            double quad = absDeg;
            if (absDeg > 90 && absDeg <= 180) quad = 180 - absDeg;
            else if (absDeg > 180 && absDeg <= 270) quad = absDeg - 180;
            else if (absDeg > 270) quad = 360 - absDeg;

            // Now calculate DMS from the quadrant angle
            int d = (int) quad;
            double min = (quad - d) * 60;
            int m = (int) min;
            double sec = (min - m) * 60;
            int s = (int) Math.round(sec);

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
    }

    public static SurveyResult compute(double x1, double y1, double z1, double x2, double y2, double z2, World world) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        double slopeDistance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        double bearing = Math.toDegrees(Math.atan2(dx, -dz));
        if (bearing < 0) bearing += 360.0;

        double azimuth = bearing;

        double zenithAngle;
        if (horizontalDistance < 0.001) {
            zenithAngle = (dy >= 0) ? 0.0 : 180.0;
        } else {
            double verticalFromHorizontal = Math.toDegrees(Math.atan2(dy, horizontalDistance));
            zenithAngle = 90.0 - verticalFromHorizontal;
        }

        return new SurveyResult(
                bearing, azimuth, zenithAngle, slopeDistance, horizontalDistance,
                -dz, dx, y1, y2
        );
    }
}