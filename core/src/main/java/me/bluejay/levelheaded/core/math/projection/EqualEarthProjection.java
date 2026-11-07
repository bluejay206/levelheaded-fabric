package me.bluejay.levelheaded.core.math.projection;

/**
 * Equal Earth (Šavrič, Jenny, Patterson 2018), spherical form.
 * Unit formulas × {@link #R} so {@code worldScale} is projected metres/block.
 */
public final class EqualEarthProjection implements MapProjection {

    public static final EqualEarthProjection INSTANCE = new EqualEarthProjection();

    private static final double A1 = 1.340264;
    private static final double A2 = -0.081106;
    private static final double A3 = 0.000893;
    private static final double A4 = 0.003796;
    private static final double M = Math.sqrt(3.0) / 2.0;

    public static final double R = WebMercatorProjection.METERS_PER_DEGREE * 180.0 / Math.PI;

    private static final int NEWTON_ITERS = 12;
    private static final double NEWTON_EPS = 1e-12;

    private EqualEarthProjection() {}

    @Override public String id() { return "equalearth"; }
    @Override public String displayName() { return "Equal Earth"; }
    @Override public String shortTag() { return "ee"; }

    @Override
    public double blockXToLongitude(double blockX, double worldScale) {
        return blockXToLongitude(blockX, 0.0, worldScale);
    }

    @Override
    public double blockXToLongitude(double blockX, double blockZ, double worldScale) {
        if (!(worldScale > 0.0)) return 0.0;
        double xUnit = blockX * worldScale / R;
        double yUnit = -blockZ * worldScale / R;
        double theta = invertTheta(yUnit);
        double denom = thetaDenom(theta);
        if (Math.abs(Math.cos(theta)) < 1e-12 || denom == 0.0) return 0.0;
        double lam = xUnit * M * denom / Math.cos(theta);
        return wrapLon(Math.toDegrees(lam));
    }

    @Override
    public double blockZToLat(double blockZ, double worldScale) {
        return blockZToLat(0.0, blockZ, worldScale);
    }

    @Override
    public double blockZToLat(double blockX, double blockZ, double worldScale) {
        if (!(worldScale > 0.0)) return 0.0;
        double yUnit = -blockZ * worldScale / R;
        double theta = invertTheta(yUnit);
        double s = Math.max(-1.0, Math.min(1.0, Math.sin(theta) / M));
        return Math.toDegrees(Math.asin(s));
    }

    @Override
    public double longitudeToBlockX(double longitudeDeg, double worldScale) {
        return longitudeToBlockX(longitudeDeg, 0.0, worldScale);
    }

    @Override
    public double longitudeToBlockX(double longitudeDeg, double latitudeDeg, double worldScale) {
        if (!(worldScale > 0.0)) return 0.0;
        return forwardUnit(Math.toRadians(latitudeDeg), Math.toRadians(longitudeDeg))[0] * R / worldScale;
    }

    @Override
    public double latToBlockZ(double latitudeDeg, double worldScale) {
        return latToBlockZ(latitudeDeg, 0.0, worldScale);
    }

    @Override
    public double latToBlockZ(double latitudeDeg, double longitudeDeg, double worldScale) {
        if (!(worldScale > 0.0)) return 0.0;
        return -forwardUnit(Math.toRadians(latitudeDeg), Math.toRadians(longitudeDeg))[1] * R / worldScale;
    }

    @Override
    public double groundMetersPerBlockX(double blockX, double blockZ, double worldScale) {
        if (!(worldScale > 0.0)) return 0.0;
        double lat = blockZToLat(blockX, blockZ, worldScale);
        double lon = blockXToLongitude(blockX, blockZ, worldScale);
        double phi = Math.toRadians(lat);
        double lam = Math.toRadians(lon);
        double dLam = 1e-7;
        double[] xy = forwardUnit(phi, lam);
        double[] xy2 = forwardUnit(phi, lam + dLam);
        double dxMetersPerRad = R * (xy2[0] - xy[0]) / dLam;
        if (Math.abs(dxMetersPerRad) < 1e-9) return 0.0;
        return Math.abs(R * Math.cos(phi) * (worldScale / dxMetersPerRad));
    }

    @Override
    public double groundMetersPerBlockZ(double blockX, double blockZ, double worldScale) {
        if (!(worldScale > 0.0)) return 0.0;
        double lat = blockZToLat(blockX, blockZ, worldScale);
        double lon = blockXToLongitude(blockX, blockZ, worldScale);
        double phi = Math.toRadians(lat);
        double lam = Math.toRadians(lon);
        double dPhi = 1e-7;
        double[] xy = forwardUnit(phi, lam);
        double[] xy2 = forwardUnit(phi + dPhi, lam);
        double dyMetersPerRad = R * (xy2[1] - xy[1]) / dPhi;
        if (Math.abs(dyMetersPerRad) < 1e-9) return 0.0;
        return Math.abs(R * (worldScale / dyMetersPerRad));
    }

    private static double[] forwardUnit(double phi, double lam) {
        phi = Math.max(-Math.PI / 2, Math.min(Math.PI / 2, phi));
        double arg = Math.max(-1.0, Math.min(1.0, M * Math.sin(phi)));
        double theta = Math.asin(arg);
        double t2 = theta * theta;
        double t6 = t2 * t2 * t2;
        double denom = A1 + 3.0 * A2 * t2 + t6 * (7.0 * A3 + 9.0 * A4 * t2);
        double x = lam * Math.cos(theta) / (M * denom);
        double y = theta * (A1 + A2 * t2 + t6 * (A3 + A4 * t2));
        return new double[] {x, y};
    }

    private static double invertTheta(double yUnit) {
        double theta = yUnit;
        for (int i = 0; i < NEWTON_ITERS; i++) {
            double t2 = theta * theta;
            double t6 = t2 * t2 * t2;
            double fy = theta * (A1 + A2 * t2 + t6 * (A3 + A4 * t2)) - yUnit;
            double fpy = A1 + 3.0 * A2 * t2 + t6 * (7.0 * A3 + 9.0 * A4 * t2);
            if (Math.abs(fpy) < 1e-18) break;
            double d = fy / fpy;
            theta -= d;
            if (Math.abs(d) < NEWTON_EPS) break;
        }
        return theta;
    }

    private static double thetaDenom(double theta) {
        double t2 = theta * theta;
        double t6 = t2 * t2 * t2;
        return A1 + 3.0 * A2 * t2 + t6 * (7.0 * A3 + 9.0 * A4 * t2);
    }

    private static double wrapLon(double lon) {
        return ((lon + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
    }
}