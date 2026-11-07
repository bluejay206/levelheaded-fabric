package me.bluejay.levelheaded.core.math.projection;

/**
 * Brand-agnostic geographic projection: block X/Z ↔ lon/lat.
 *
 * <p>Convention: origin (0, 0) = lon 0°, lat 0°; +X east; +Z south
 * (Minecraft north = −Z). {@code worldScale} is projected metres per block.
 */
public interface MapProjection {

    String id();

    String displayName();

    String shortTag();

    double blockXToLongitude(double blockX, double worldScale);

    double blockZToLat(double blockZ, double worldScale);

    default double blockXToLongitude(double blockX, double blockZ, double worldScale) {
        return blockXToLongitude(blockX, worldScale);
    }

    default double blockZToLat(double blockX, double blockZ, double worldScale) {
        return blockZToLat(blockZ, worldScale);
    }

    double longitudeToBlockX(double longitudeDeg, double worldScale);

    double latToBlockZ(double latitudeDeg, double worldScale);

    default double longitudeToBlockX(double longitudeDeg, double latitudeDeg, double worldScale) {
        return longitudeToBlockX(longitudeDeg, worldScale);
    }

    default double latToBlockZ(double latitudeDeg, double longitudeDeg, double worldScale) {
        return latToBlockZ(latitudeDeg, worldScale);
    }

    double groundMetersPerBlockX(double blockX, double blockZ, double worldScale);

    double groundMetersPerBlockZ(double blockX, double blockZ, double worldScale);

    default double groundFactorFromLatitude(double latitudeDeg, double worldScale) {
        double z = latToBlockZ(latitudeDeg, worldScale);
        double x = longitudeToBlockX(0.0, worldScale);
        return groundMetersPerBlockX(x, z, worldScale);
    }
}