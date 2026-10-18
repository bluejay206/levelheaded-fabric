package me.bluejay.levelheaded.core.data;

public record OccupyPoint(
        double x,
        double y,
        double z,
        String description
) {
    public OccupyPoint(double x, double y, double z) {
        this(x, y, z, "");
    }
}
