package me.bluejay.levelheaded.core.math.projection;

/**
 * Plate carrée / equirectangular: x ∝ lon, z ∝ −lat.
 */
public final class EquirectangularProjection implements MapProjection {

    public static final EquirectangularProjection INSTANCE = new EquirectangularProjection();

    private static final double METERS_PER_DEGREE = WebMercatorProjection.METERS_PER_DEGREE;

    private EquirectangularProjection() {}

    @Override public String id() { return "equirectangular"; }
    @Override public String displayName() { return "Equirectangular"; }
    @Override public String shortTag() { return "eqc"; }

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
        return -blockZ * worldScale / METERS_PER_DEGREE;
    }

    @Override
    public double latToBlockZ(double latitudeDeg, double worldScale) {
        if (!(worldScale > 0.0)) return 0.0;
        return -latitudeDeg * METERS_PER_DEGREE / worldScale;
    }

    @Override
    public double groundMetersPerBlockX(double blockX, double blockZ, double worldScale) {
        if (!(worldScale > 0.0)) return 0.0;
        double lat = blockZToLat(blockZ, worldScale);
        return worldScale * Math.cos(Math.toRadians(Math.max(-90.0, Math.min(90.0, lat))));
    }

    @Override
    public double groundMetersPerBlockZ(double blockX, double blockZ, double worldScale) {
        return worldScale > 0.0 ? worldScale : 0.0;
    }

    @Override
    public double groundFactorFromLatitude(double latitudeDeg, double worldScale) {
        if (!(worldScale > 0.0)) return 0.0;
        return worldScale * Math.cos(Math.toRadians(Math.max(-90.0, Math.min(90.0, latitudeDeg))));
    }
}