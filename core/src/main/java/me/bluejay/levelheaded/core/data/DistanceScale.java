package me.bluejay.levelheaded.core.data;

import me.bluejay.levelheaded.core.math.AngleFormat;
import me.bluejay.levelheaded.core.math.projection.MapProjection;
import me.bluejay.levelheaded.core.math.projection.Projections;

/**
 * Distance basis for horizontal measurements. Independent of {@link Units}.
 *
 * <ul>
 *   <li>{@link Mode#GRID} — 1 block = 1 m (no horizontal scale)</li>
 *   <li>{@link Mode#GROUND} — factor = worldScale × cos(φ) (local Mercator ground)</li>
 *   <li>{@link Mode#GEODESIC} — WGS84 Vincenty from block→lat/lon (matches Google Earth)</li>
 * </ul>
 * Vertical / cut-fill is never scaled by these modes.
 */
public final class DistanceScale {

    public enum Mode {
        GRID,
        GROUND,
        GEODESIC
    }

    private Mode mode = Mode.GRID;
    private double factor = 1.0;
    private Double latitudeDeg = null;
    private double worldScale = 1.0;

    public void setGrid() {
        mode = Mode.GRID;
        factor = 1.0;
        latitudeDeg = null;
    }

    public void setGround(double factor) {
        mode = Mode.GROUND;
        this.factor = clampFactor(factor);
        this.latitudeDeg = null;
    }

    public void setGroundFromLatitude(double latitudeDeg) {
        double lat = Math.max(-85.05112878, Math.min(85.05112878, latitudeDeg));
        mode = Mode.GROUND;
        this.latitudeDeg = lat;
        this.factor = clampFactor(
                Projections.current().groundFactorFromLatitude(lat, worldScale)
        );
    }

    public void setGroundFromBlockZ(double blockZ) {
        double lat = Projections.current().blockZToLat(0.0, blockZ, worldScale);
        setGroundFromLatitude(lat);
    }

    /** WGS84 Vincenty geodesic for horizontal distance (projected coords → lat/lon). */
    public void setGeodesic() {
        mode = Mode.GEODESIC;
        // factor unused; latitude informational only if previously set
    }

    public void setWorldScale(double worldScale) {
        if (!(worldScale > 0.0) || Double.isNaN(worldScale) || Double.isInfinite(worldScale)) {
            this.worldScale = 1.0;
        } else {
            this.worldScale = Math.min(1000.0, Math.max(1e-6, worldScale));
        }
        if (mode == Mode.GROUND && latitudeDeg != null) {
            this.factor = clampFactor(
                    Projections.current().groundFactorFromLatitude(latitudeDeg, this.worldScale)
            );
        }
    }

    public double worldScale() {
        return worldScale;
    }

    public Mode mode() {
        return mode;
    }

    public double factor() {
        return factor;
    }

    public Double latitudeDeg() {
        return latitudeDeg;
    }

    /** Multiplier for Hz / ΔN / ΔE when mode is GROUND. GRID/GEODESIC ignore this. */
    public double horizontalFactor() {
        return mode == Mode.GROUND ? factor : 1.0;
    }

    public boolean isGeodesic() {
        return mode == Mode.GEODESIC;
    }

    public String hudTag() {
        return switch (mode) {
            case GRID -> "GRID";
            case GROUND -> latitudeDeg != null
                    ? String.format("GND×%.6f@%s", factor, AngleFormat.toDms(latitudeDeg, true))
                    : String.format("GND×%.6f", factor);
            case GEODESIC -> String.format("GEO·%s·ws%.4g", Projections.current().shortTag(), worldScale);
        };
    }

    public String statusLine() {
        return switch (mode) {
            case GRID -> String.format(
                    "Scale: GRID (factor 1.0) — block distances | worldScale %.4f m/block",
                    worldScale);
            case GROUND -> latitudeDeg != null
                    ? String.format(
                    "Scale: GROUND × %.8f (lat %s / %.6f°, worldScale %.4f, cos×ws) — horizontal × factor",
                    factor, AngleFormat.toDms(latitudeDeg, true), latitudeDeg, worldScale)
                    : String.format(
                    "Scale: GROUND × %.8f (worldScale %.4f) — horizontal × factor",
                    factor, worldScale);
            case GEODESIC -> String.format(
                    "Scale: GEODESIC (WGS84 Vincenty) | worldScale %.4f m/block — Hz from lat/lon",
                    worldScale);
        };
    }

    private static double clampFactor(double f) {
        if (!(f > 0.0) || Double.isNaN(f) || Double.isInfinite(f)) {
            return 1.0;
        }
        return Math.min(10.0, Math.max(1e-6, f));
    }
}