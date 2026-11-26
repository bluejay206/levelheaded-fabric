package me.bluejay.levelheaded.core.data;

/**
 * A named or numbered point in the project store.
 * Coordinates are in the engine's scaled northing / easting / elevation.
 */
public record ControlPoint(
        String id,
        PointKind kind,
        double northing,
        double easting,
        double elevation,
        String description,
        String code,
        long created
) {
    public ControlPoint(String id, PointKind kind,
                        double northing, double easting, double elevation,
                        String description) {
        this(id, kind, northing, easting, elevation,
                description == null ? "" : description, "", System.currentTimeMillis());
    }

    public ControlPoint withDescription(String description) {
        return new ControlPoint(id, kind, northing, easting, elevation,
                description == null ? "" : description, code, created);
    }
}