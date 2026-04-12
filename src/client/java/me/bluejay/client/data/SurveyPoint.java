package me.bluejay.client.data;

import me.bluejay.math.SurveyMath;
import net.minecraft.util.math.Vec3d;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record SurveyPoint(
        int id,
        Vec3d position,
        Vec3d occupyPoint,
        SurveyMath.SurveyResult result,
        String description,
        LocalDateTime timestamp
) {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public String toPNEZD() {
        // Coord mode feature paused - using standard Minecraft PNEZD for now
        double northing = -position.z;   // North = -Z
        double easting = position.x;
        double elevation = position.y;

        String desc = (description != null && !description.isBlank()) ? description : "Survey Shot";
        desc = desc.replace(",", ";");

        return String.format("%d,%.3f,%.3f,%.3f,%s", id, northing, easting, elevation, desc);
    }

    @Override
    public String toString() {
        // Clean output for /ss list command
        double northing = -position.z;
        double easting = position.x;
        double elevation = position.y;

        String desc = (description != null && !description.isBlank()) ? description : "Survey Shot";
        String time = timestamp.format(TIME_FORMAT);

        return String.format("§e#%d  §fN: %.2f  E: %.2f  Z: %.2f  §7%s §8(%s)",
                id, northing, easting, elevation, desc, time);
    }
}