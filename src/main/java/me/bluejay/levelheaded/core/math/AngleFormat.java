package me.bluejay.levelheaded.core.math;

/**
 * Decimal degrees ↔ DMS / packed DDD.mmss helpers.
 * Packed DDD.mmss: 46.301500 → 46° 30' 15.00" (mm = minutes, ss = seconds).
 */
public final class AngleFormat {

    private AngleFormat() {}

    public static String toDms(double decimalDegrees, boolean withHemisphere) {
        boolean neg = decimalDegrees < 0;
        double abs = Math.abs(decimalDegrees);

        int deg = (int) Math.floor(abs);
        double minFull = (abs - deg) * 60.0;
        int min = (int) Math.floor(minFull);
        double sec = (minFull - min) * 60.0;

        if (sec >= 59.995) {
            sec = 0.0;
            min += 1;
        }
        if (min >= 60) {
            min = 0;
            deg += 1;
        }

        String body = String.format("%d°%02d'%05.2f\"", deg, min, sec);
        if (!withHemisphere) {
            return (neg ? "-" : "") + body;
        }
        return body + (neg ? "S" : "N");
    }

    public static String toDms(double decimalDegrees) {
        return toDms(decimalDegrees, false);
    }

    public static double toPackedDddMmss(double decimalDegrees) {
        boolean neg = decimalDegrees < 0;
        double abs = Math.abs(decimalDegrees);
        int deg = (int) Math.floor(abs);
        double minFull = (abs - deg) * 60.0;
        int min = (int) Math.floor(minFull);
        double sec = (minFull - min) * 60.0;
        double packed = deg + (min / 100.0) + (sec / 10000.0);
        return neg ? -packed : packed;
    }

    public static double fromPackedDddMmss(double packed) {
        boolean neg = packed < 0;
        double abs = Math.abs(packed);
        int deg = (int) Math.floor(abs);
        double frac = abs - deg;
        int min = (int) Math.floor(frac * 100.0 + 1e-9);
        double sec = (frac * 100.0 - min) * 100.0;
        double dec = deg + min / 60.0 + sec / 3600.0;
        return neg ? -dec : dec;
    }

    public static String formatPacked(double packed) {
        return String.format("%.6f", packed);
    }

    /**
     * Azimuth (0 = north, clockwise) → quadrant bearing, e.g. {@code N28°36'25"W}.
     * Cardinals collapse to N / E / S / W.
     */
    public static String toQuadrantBearing(double azimuthDeg) {
        double az = azimuthDeg % 360.0;
        if (az < 0) az += 360.0;

        if (az < 1e-4 || az > 360.0 - 1e-4) return "N";
        if (Math.abs(az - 90.0) < 1e-4) return "E";
        if (Math.abs(az - 180.0) < 1e-4) return "S";
        if (Math.abs(az - 270.0) < 1e-4) return "W";

        String ns;
        String ew;
        double angle;
        if (az < 90.0) {
            ns = "N"; ew = "E"; angle = az;
        } else if (az < 180.0) {
            ns = "S"; ew = "E"; angle = 180.0 - az;
        } else if (az < 270.0) {
            ns = "S"; ew = "W"; angle = az - 180.0;
        } else {
            ns = "N"; ew = "W"; angle = 360.0 - az;
        }
        int deg = (int) Math.floor(angle);
        double minFull = (angle - deg) * 60.0;
        int min = (int) Math.floor(minFull);
        double sec = (minFull - min) * 60.0;
        int secI = (int) Math.round(sec);
        if (secI >= 60) { secI = 0; min += 1; }
        if (min >= 60) { min = 0; deg += 1; }
        return String.format("%s%d°%02d'%02d\"%s", ns, deg, min, secI, ew);
    }
}
