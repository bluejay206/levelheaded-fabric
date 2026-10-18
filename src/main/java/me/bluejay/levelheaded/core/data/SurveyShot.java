package me.bluejay.levelheaded.core.data;

public record SurveyShot(
        OccupyPoint from,
        double targetX,
        double targetY,
        double targetZ,
        String description,
        long timestamp
) {
    public SurveyShot(OccupyPoint from, double targetX, double targetY, double targetZ, String description) {
        this(from, targetX, targetY, targetZ, description, System.currentTimeMillis());
    }
}
