package me.bluejay.levelheaded.core.math;

/**
 * Spherical Web Mercator (EPSG:3857 / OSM-style) projection.
 *
 * <p>Origin (block X=0, Z=0) = lon 0°, lat 0°. +X east, +Z south (MC north = −Z).
 * {@code worldScale} = real metres per block at the equator (projected).
 *
 * <p>{@link Mode#EQUIRECTANGULAR} is the simple plate-carrée fallback
 * (1° lat = {@link #blocksPerDegree} blocks) used by some earth generators.
 */
public final class WebMercator {

    public static final double METERS_PER_DEGREE = 111319.49166666667;
    public static final double EQUATOR_CIRCUMFERENCE_METERS = METERS_PER_DEGREE * 360.0;
    public static final double MAX_MERCATOR_LATITUDE = 85.05112878;

    private static final double EARTH_RADIUS_METERS = METERS_PER_DEGREE * 180.0 / Math.PI;
    private static final double MAX_MERCATOR_Y_RATIO = mercatorYRatio(MAX_MERCATOR_LATITUDE);

    public enum Mode {
        MERCATOR,
        EQUIRECTANGULAR
    }

    private WebMercator() {}

    public static double blockXToLongitude(double blockX, double worldScale) {
        if (!(worldScale > 0.0)) return 0.0;
        return blockX * worldScale / METERS_PER_DEGREE;
    }

    public static double longitudeToBlockX(double longitude, double worldScale) {
        if (!(worldScale > 0.0)) return 0.0;
        return longitude * METERS_PER_DEGREE / worldScale;
    }

    public static double blockZToLat(double blockZ, double worldScale, Mode mode) {
        if (!(worldScale > 0.0)) return 0.0;
        if (mode == Mode.EQUIRECTANGULAR) {
            return -blockZ / blocksPerDegree(worldScale);
        }
        double mercatorY = -blockZ * worldScale;
        double latitudeRad = Math.atan(Math.sinh(mercatorY / EARTH_RADIUS_METERS));
        return clampLatitude(Math.toDegrees(latitudeRad));
    }

    public static double blockZToLat(double blockZ, double worldScale) {
        return blockZToLat(blockZ, worldScale, Mode.MERCATOR);
    }

    public static double latToBlockZ(double latitude, double worldScale, Mode mode) {
        if (!(worldScale > 0.0)) return 0.0;
        if (mode == Mode.EQUIRECTANGULAR) {
            return -latitude * blocksPerDegree(worldScale);
        }
        double clamped = clampLatitude(latitude);
        double latitudeRad = Math.toRadians(clamped);
        double mercatorY = EARTH_RADIUS_METERS * Math.log(Math.tan(Math.PI * 0.25 + latitudeRad * 0.5));
        return -mercatorY / worldScale;
    }

    public static double latToBlockZ(double latitude, double worldScale) {
        return latToBlockZ(latitude, worldScale, Mode.MERCATOR);
    }

    public static double groundMetersPerBlock(double blockZ, double worldScale, Mode mode) {
        if (!(worldScale > 0.0)) return 0.0;
        if (mode == Mode.EQUIRECTANGULAR) return worldScale;
        double mercatorYRatio = clampMercatorYRatio(-blockZ * worldScale / EARTH_RADIUS_METERS);
        return worldScale / Math.cosh(mercatorYRatio);
    }

    public static double groundMetersPerBlock(double blockZ, double worldScale) {
        return groundMetersPerBlock(blockZ, worldScale, Mode.MERCATOR);
    }

    public static double groundFactorFromLatitude(double latitudeDeg, double worldScale, Mode mode) {
        if (!(worldScale > 0.0)) return 0.0;
        if (mode == Mode.EQUIRECTANGULAR) return worldScale;
        double lat = clampLatitude(latitudeDeg);
        return worldScale * Math.cos(Math.toRadians(lat));
    }

    public static double groundFactorFromLatitude(double latitudeDeg, double worldScale) {
        return groundFactorFromLatitude(latitudeDeg, worldScale, Mode.MERCATOR);
    }

    public static double blocksPerDegree(double worldScale) {
        return worldScale <= 0.0 ? 0.0 : METERS_PER_DEGREE / worldScale;
    }

    public static double clampLatitude(double latitude) {
        return Math.max(-MAX_MERCATOR_LATITUDE, Math.min(MAX_MERCATOR_LATITUDE, latitude));
    }

    private static double mercatorYRatio(double latitude) {
        double latitudeRadians = Math.toRadians(latitude);
        return Math.log(Math.tan(Math.PI * 0.25 + latitudeRadians * 0.5));
    }

    private static double clampMercatorYRatio(double value) {
        return Math.max(-MAX_MERCATOR_Y_RATIO, Math.min(MAX_MERCATOR_Y_RATIO, value));
    }
}