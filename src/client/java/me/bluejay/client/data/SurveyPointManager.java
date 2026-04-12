package me.bluejay.client.data;

import me.bluejay.math.SurveyMath;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SurveyPointManager {

    private static final AtomicInteger idCounter = new AtomicInteger(1);
    private static final List<SurveyPoint> points = new ArrayList<>();

    private static String sessionPrefix = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyMMdd-HHmm"));

    private static Path currentCsvFile = FabricLoader.getInstance()
            .getGameDir()
            .resolve("surveys")
            .resolve(sessionPrefix + "-survey_shots.csv");

    private static boolean firstWrite = true;

    public static SurveyPoint addPoint(Vec3d occupyPoint, Vec3d shotPoint, SurveyMath.SurveyResult result, String source) {
        if (result == null || shotPoint == null) return null;

        int id = idCounter.getAndIncrement();
        String label = (source != null && !source.isBlank()) ? source : "Survey Shot";

        SurveyPoint point = new SurveyPoint(id, shotPoint, occupyPoint, result, label, LocalDateTime.now());

        points.add(point);
        appendSinglePointToCsv(point);

        return point;
    }

    private static void appendSinglePointToCsv(SurveyPoint point) {
        try {
            Files.createDirectories(currentCsvFile.getParent());

            String line = point.toPNEZD() + System.lineSeparator();
            Files.writeString(currentCsvFile, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

            if (firstWrite) {
                firstWrite = false;
                showCsvCreatedMessage();
            }
        } catch (IOException e) {
            System.err.println("[SurveyorSays] Failed to append point: " + e.getMessage());
        }
    }

    private static void showCsvCreatedMessage() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            String fullPath = currentCsvFile.toAbsolutePath().toString();
            client.player.sendMessage(
                    Text.literal("§6[SurveyorSays] §aCSV created: §f" + fullPath),
                    false
            );
        }
    }

    /** Called by /ss new command */
    public static void startNewSession() {
        sessionPrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd-HHmm"));
        currentCsvFile = FabricLoader.getInstance()
                .getGameDir()
                .resolve("surveys")
                .resolve(sessionPrefix + "-survey_shots.csv");

        firstWrite = true;
        idCounter.set(1);
        points.clear();

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("§6[SurveyorSays] §aNew survey session started. Next shot will create a new CSV."),
                    false
            );
        }
    }

    public static int getNextShotNumber() {
        return idCounter.get();
    }

    public static void shutdown() {
        System.out.println("[SurveyorSays] Shutdown - " + points.size() + " points saved.");
    }

    public static void clear() {
        points.clear();
        idCounter.set(1);
    }

    public static List<SurveyPoint> getRecentShots(int count) {
        if (points.isEmpty()) return List.of();
        int start = Math.max(0, points.size() - count);
        return points.subList(start, points.size());
    }
}