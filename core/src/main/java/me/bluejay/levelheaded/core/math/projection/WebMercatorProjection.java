package me.bluejay.levelheaded.core.math.projection;

/**
 * Spherical Web Mercator (EPSG:3857 / OSM-style).
 */
public final class WebMercatorProjection implements MapProjection {

    public static final WebMercatorProjection INSTANCE = new WebMercatorProjection();

    public static final double METERS_PER_DEGREE = 111319.49166666667;
    public static final double MAX_MERCATOR_LATITUDE = 85.05112878;

    private static final double EARTH_RADIUS_METERS = METERS_PER_DEGREE * 180.0 / Math.PI;
    private static final double MAX_MERCATOR_Y_RATIO = mercatorYRatio(MAX_MERCATOR_LATITUDE);

    private WebMercatorProjection() {}

    @Override public String id() { return "webmercator"; }
    @Override public String displayName() { return "Web Mercator"; }
    @Override public String shortTag() { return "wm"; }

    @Override
    public double blockXToLongitude(double blockX, double worldScale) {
        if (!(worldScale > 0.0)) return 0.0;
        return blockX * worldScale / METERS_PER_DEGREE;
    }

    @Override
    public double longitudeToBlockX(double longitudeDeg, double worldScale) {
        if (!(worldScale > 0.0)) return 0.0;
        return longitudeDeg * METERS_PER_DEGREE / worldScale;
    }

    @Override
    public double blockZToLat(double blockZ, double worldScale) {
        if (!(worldScale > 0.0)) return 0.0;
        double mercatorY = -blockZ * worldScale;
        double latitudeRad = Math.atan(Math.sinh(mercatorY / EARTH_RADIUS_METERS));
        return clampLatitude(Math.toDegrees(latitudeRad));
    }

    @Override
    public double latToBlockZ(double latitudeDeg, double worldScale) {
        if (!(worldScale > 0.0)) return 0.0;
        double clamped = clampLatitude(latitudeDeg);
        double latitudeRad = Math.toRadians(clamped);
        double mercatorY = EARTH_RADIUS_METERS * Math.log(Math.tan(Math.PI * 0.25 + latitudeRad * 0.5));
        return -mercatorY / worldScale;
    }

    @Override
    public double groundMetersPerBlockX(double blockX, double blockZ, double worldScale) {
        return isotropicGround(blockZ, worldScale);
    }

    @Override
    public double groundMetersPerBlockZ(double blockX, double blockZ, double worldScale) {
        return isotropicGround(blockZ, worldScale);
    }

    @Override
    public double groundFactorFromLatitude(double latitudeDeg, double worldScale) {
        if (!(worldScale > 0.0)) return 0.0;
        return worldScale * Math.cos(Math.toRadians(clampLatitude(latitudeDeg)));
    }

    public static double clampLatitude(double latitude) {
        return Math.max(-MAX_MERCATOR_LATITUDE, Math.min(MAX_MERCATOR_LATITUDE, latitude));
    }

    private static double isotropicGround(double blockZ, double worldScale) {
        if (!(worldScale > 0.0)) return 0.0;
        double mercatorYRatio = Math.max(-MAX_MERCATOR_Y_RATIO,
                Math.min(MAX_MERCATOR_Y_RATIO, -blockZ * worldScale / EARTH_RADIUS_METERS));
        return worldScale / Math.cosh(mercatorYRatio);
    }

    private static double mercatorYRatio(double latitude) {
        return Math.log(Math.tan(Math.PI * 0.25 + Math.toRadians(latitude) * 0.5));
    }
}