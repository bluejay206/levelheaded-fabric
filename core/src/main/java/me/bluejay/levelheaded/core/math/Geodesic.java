package me.bluejay.levelheaded.core.math;

/**
 * WGS84 geodesic helpers (Vincenty inverse). Pure math, no Minecraft / WebMercator deps.
 *
 * <p>Used when DistanceScale is in GEODESIC mode so long baselines match
 * Google Earth / ellipsoid distance instead of a single cos(φ) factor.
 */
public final class Geodesic {

    /** WGS84 semi-major axis (m). */
    public static final double A = 6378137.0;
    /** WGS84 flattening. */
    public static final double F = 1.0 / 298.257223563;
    /** WGS84 semi-minor axis (m). */
    public static final double B = A * (1.0 - F);

    public record Inverse(
            double distanceMeters,
            double forwardAzimuthDeg,
            double backAzimuthDeg
    ) {}

    private Geodesic() {}

    /**
     * Vincenty inverse between two WGS84 positions.
     * Returns null if the formula fails to converge (near-antipodal rare cases).
     */
    public static Inverse inverse(double lat1Deg, double lon1Deg, double lat2Deg, double lon2Deg) {
        double L = Math.toRadians(lon2Deg - lon1Deg);
        double U1 = Math.atan((1.0 - F) * Math.tan(Math.toRadians(lat1Deg)));
        double U2 = Math.atan((1.0 - F) * Math.tan(Math.toRadians(lat2Deg)));
        double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

        double lam = L;
        double cos2Alpha = 0, sinSigma = 0, cosSigma = 0, sigma = 0, cos2SigmaM = 0, sinLam = 0, cosLam = 0;

        for (int i = 0; i < 100; i++) {
            sinLam = Math.sin(lam);
            cosLam = Math.cos(lam);
            double sinSigmaSq = (cosU2 * sinLam) * (cosU2 * sinLam)
                    + (cosU1 * sinU2 - sinU1 * cosU2 * cosLam) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLam);
            sinSigma = Math.sqrt(sinSigmaSq);
            if (sinSigma == 0.0) {
                return new Inverse(0.0, 0.0, 0.0);
            }
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLam;
            sigma = Math.atan2(sinSigma, cosSigma);
            double sinAlpha = cosU1 * cosU2 * sinLam / sinSigma;
            cos2Alpha = 1.0 - sinAlpha * sinAlpha;
            cos2SigmaM = (cos2Alpha != 0.0)
                    ? (cosSigma - 2.0 * sinU1 * sinU2 / cos2Alpha)
                    : 0.0;
            double C = F / 16.0 * cos2Alpha * (4.0 + F * (4.0 - 3.0 * cos2Alpha));
            double lamPrev = lam;
            lam = L + (1.0 - C) * F * sinAlpha * (sigma + C * sinSigma * (
                    cos2SigmaM + C * cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM)));
            if (Math.abs(lam - lamPrev) < 1e-12) {
                break;
            }
            if (i == 99) {
                return null;
            }
        }

        double uSq = cos2Alpha * (A * A - B * B) / (B * B);
        double Acoef = 1.0 + uSq / 16384.0 * (4096.0 + uSq * (-768.0 + uSq * (320.0 - 175.0 * uSq)));
        double Bcoef = uSq / 1024.0 * (256.0 + uSq * (-128.0 + uSq * (74.0 - 47.0 * uSq)));
        double deltaSigma = Bcoef * sinSigma * (cos2SigmaM + Bcoef / 4.0 * (
                cosSigma * (-1.0 + 2.0 * cos2SigmaM * cos2SigmaM)
                        - Bcoef / 6.0 * cos2SigmaM * (-3.0 + 4.0 * sinSigma * sinSigma)
                        * (-3.0 + 4.0 * cos2SigmaM * cos2SigmaM)));

        double distance = B * Acoef * (sigma - deltaSigma);

        double fwd = Math.atan2(cosU2 * sinLam, cosU1 * sinU2 - sinU1 * cosU2 * cosLam);
        double back = Math.atan2(cosU1 * sinLam, -sinU1 * cosU2 + cosU1 * sinU2 * cosLam);

        double fwdDeg = Math.toDegrees(fwd);
        if (fwdDeg < 0) fwdDeg += 360.0;
        double backDeg = Math.toDegrees(back);
        if (backDeg < 0) backDeg += 360.0;

        return new Inverse(distance, fwdDeg, backDeg);
    }

    public static double haversineMeters(double lat1Deg, double lon1Deg, double lat2Deg, double lon2Deg) {
        double R = 6371008.8;
        double p1 = Math.toRadians(lat1Deg);
        double p2 = Math.toRadians(lat2Deg);
        double dphi = Math.toRadians(lat2Deg - lat1Deg);
        double dl = Math.toRadians(lon2Deg - lon1Deg);
        double h = Math.sin(dphi / 2) * Math.sin(dphi / 2)
                + Math.cos(p1) * Math.cos(p2) * Math.sin(dl / 2) * Math.sin(dl / 2);
        return 2.0 * R * Math.asin(Math.min(1.0, Math.sqrt(h)));
    }
}