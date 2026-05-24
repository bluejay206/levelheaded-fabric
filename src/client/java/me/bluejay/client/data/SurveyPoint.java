package me.bluejay.client.data;

import me.bluejay.levelheaded.math.SurveyMath;
import net.minecraft.util.math.Vec3d;
import java.time.LocalDateTime;

public record SurveyPoint(
        int pointNumber,
        Vec3d occupyPos,
        Vec3d shotPos,
        SurveyMath.SurveyResult result,
        String source,
        String description,
        LocalDateTime timestamp
) {

    public enum Unit {
        METERS("Meters", 1.0, "m"),
        FEET("Feet", 3.28084, "ft"),
        CHAINS("Chains", 0.0497097, "ch");

        public final String name;
        public final double scale;
        public final String symbol;

        Unit(String name, double scale, String symbol) {
            this.name = name;
            this.scale = scale;
            this.symbol = symbol;
        }
    }

    public SurveyPoint(
            double occupyX, double occupyY, double occupyZ,
            double shotX, double shotY, double shotZ,
            SurveyMath.SurveyResult result,
            String source,
            String description) {

        this(
                SurveyPointManager.getNextShotNumber(),
                new Vec3d(occupyX, occupyY, occupyZ),
                new Vec3d(shotX, shotY, shotZ),
                result,
                source,
                (description == null || description.trim().isEmpty()) ? "Survey Shot" : description.trim(),
                LocalDateTime.now()
        );
    }

    public String toPNEZD() {
        return String.format("%d,%.3f,%.3f,%.3f,%s",
                pointNumber,
                shotPos.z,   // Northing
                shotPos.x,   // Easting
                shotPos.y,   // Elevation
                description
        );
    }

    public String getDescription() {
        return description;
    }

    public static SurveyPoint.Unit getCurrentUnit() {
        return SurveyPointManager.getCurrentUnitStatic();
    }
}