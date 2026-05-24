package me.bluejay.client.data;

import me.bluejay.levelheaded.math.SurveyMath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Manages survey point recording and CSV export in PNEZD format.
 */
public class SurveyPointManager {

    private static int currentPointNumber = 1;
    private static File currentSessionFile;

    // Unit state (default METERS) - kept for HUD display only
    private static SurveyPoint.Unit currentUnit = SurveyPoint.Unit.METERS;

    private static File ensureSessionFile() {
        File surveysDir = new File("surveys");
        if (!surveysDir.exists()) {
            boolean created = surveysDir.mkdirs();
            System.out.println("[LevelHeaded] Created surveys directory: " + surveysDir.getAbsolutePath() + " (success=" + created + ")");
        } else {
            System.out.println("[LevelHeaded] Using existing surveys directory: " + surveysDir.getAbsolutePath());
        }

        if (currentSessionFile == null || !currentSessionFile.exists()) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            currentSessionFile = new File(surveysDir, "surveyor_shots_" + timestamp + ".csv");

            try (FileWriter writer = new FileWriter(currentSessionFile)) {
                MinecraftClient mc = MinecraftClient.getInstance();
                long seed = 0L;

                if (mc.world != null) {
                    if (mc.getServer() != null) {
                        try {
                            seed = mc.getServer().getOverworld().getSeed();
                        } catch (Exception ignored) {}
                    } else {
                        seed = mc.world.hashCode();
                    }
                }

                writer.append("LevelHeaded Survey Export\n");
                writer.append("World Seed: " + seed + "\n");
                writer.append("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
                writer.append("\n");
                writer.append("Point,Northing,Easting,Elevation,Description\n");

                System.out.println("[LevelHeaded] ✅ New session CSV created: " + currentSessionFile.getAbsolutePath());
                System.out.println("[LevelHeaded] Seed: " + seed);
            } catch (IOException e) {
                System.err.println("[LevelHeaded] ❌ Failed to create CSV: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return currentSessionFile;
    }

    public static void addPoint(Vec3d occupyPos, Vec3d shotPos, SurveyMath.SurveyResult result, String source, String description) {
        if (result == null) {
            System.out.println("[LevelHeaded] addPoint skipped - null result");
            return;
        }

        String desc = (description == null || description.trim().isEmpty()) ? "Survey Shot" : description.trim();

        try (FileWriter writer = new FileWriter(ensureSessionFile(), true)) {
            writer.append(String.format("%d,%.3f,%.3f,%.3f,%s\n",
                    currentPointNumber++,
                    shotPos.z,   // Always meters (Northing)
                    shotPos.x,   // Always meters (Easting)
                    shotPos.y,   // Always meters (Elevation)
                    desc));
            System.out.println("[LevelHeaded] ✅ Shot saved to CSV (#" + (currentPointNumber-1) + ")");
        } catch (IOException e) {
            System.err.println("[LevelHeaded] ❌ Failed to write shot to CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void addPoint(Vec3d occupyPos, Vec3d shotPos, SurveyMath.SurveyResult result, String source) {
        addPoint(occupyPos, shotPos, result, source, null);
    }

    public static int getNextShotNumber() {
        return currentPointNumber;
    }

    public static void resetPointCounter() {
        currentPointNumber = 1;
        // Do NOT reset currentSessionFile → keeps appending to same CSV
    }

    public static int getCurrentPointNumber() {
        return currentPointNumber;
    }

    public static SurveyPoint.Unit getCurrentUnitStatic() {
        return currentUnit;
    }

    public static SurveyPoint.Unit getCurrentUnit() {
        return getCurrentUnitStatic();
    }

    public static void setCurrentUnit(SurveyPoint.Unit unit) {
        if (unit != null) {
            currentUnit = unit;
            // No new session on unit change
        }
    }
}