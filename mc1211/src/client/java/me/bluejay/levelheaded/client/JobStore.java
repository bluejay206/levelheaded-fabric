package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.HudSettings;
import me.bluejay.levelheaded.PointLists;
import me.bluejay.levelheaded.ShotRate;
import me.bluejay.levelheaded.SurveyState;
import me.bluejay.levelheaded.core.data.DistanceScale;
import me.bluejay.levelheaded.core.data.OccupyPoint;
import me.bluejay.levelheaded.core.data.Units;
import me.bluejay.levelheaded.core.math.projection.Projections;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Point list, occupy, and configuration, one folder per world.
 */
public final class JobStore {

    private static String worldId = "none";

    private JobStore() {}

    public static void enter(String id) {
        worldId = sanitize(id);
        PointLists.STORE.clear();
        SurveyState.ENGINE.clearOccupy();
        applyDefaults();
        load();
    }

    public static void leave() {
        save();
        PointLists.STORE.clear();
        SurveyState.ENGINE.clearOccupy();
        applyDefaults();
        worldId = "none";
    }

    private static String sanitize(String id) {
        if (id == null || id.isBlank()) return "unknown";
        return id.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static Path dir() {
        return MinecraftClient.getInstance().runDirectory.toPath()
                .resolve("levelheaded").resolve("worlds").resolve(worldId);
    }

    private static Path pointsFile() { return dir().resolve("points.csv"); }
    private static Path occupyFile() { return dir().resolve("occupy.txt"); }
    private static Path configFile() { return dir().resolve("config.txt"); }

    public static void load() {
        loadPoints();
        loadOccupy();
        loadConfig();
    }

    private static void loadPoints() {
        try {
            Path points = pointsFile();
            if (Files.exists(points)) {
                PointLists.STORE.importCsv(Files.readString(points, StandardCharsets.UTF_8));
            }
        } catch (IOException ignored) {
            // start empty
        }
    }

    private static void loadOccupy() {
        try {
            Path occupy = occupyFile();
            if (!Files.exists(occupy)) return;
            String[] c = Files.readString(occupy, StandardCharsets.UTF_8).trim().split(",", -1);
            if (c.length >= 3) {
                double x = Double.parseDouble(c[0].trim());
                double y = Double.parseDouble(c[1].trim());
                double z = Double.parseDouble(c[2].trim());
                String desc = c.length > 3 ? c[3] : "";
                SurveyState.setOccupy(x, y, z, desc);
            }
        } catch (Exception ignored) {
            // no occupy
        }
    }

    private static void loadConfig() {
        try {
            Path config = configFile();
            if (!Files.exists(config)) return;
            for (String raw : Files.readAllLines(config, StandardCharsets.UTF_8)) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq < 0) continue;
                String key = line.substring(0, eq).trim().toLowerCase(Locale.ROOT);
                String value = line.substring(eq + 1).trim();
                apply(key, value);
            }
        } catch (Exception ignored) {
            // keep defaults
        }
    }

    private static void apply(String key, String value) {
        switch (key) {
            case "units" -> {
                for (Units u : Units.values()) {
                    if (u.name().equalsIgnoreCase(value) || u.symbol().equalsIgnoreCase(value)) {
                        SurveyState.ENGINE.setUnits(u);
                        break;
                    }
                }
            }
            case "scale" -> {
                switch (value.toUpperCase(Locale.ROOT)) {
                    case "GRID" -> SurveyState.SCALE.setGrid();
                    case "GEODESIC" -> SurveyState.SCALE.setGeodesic();
                    default -> { /* ground handled by factor */ }
                }
            }
            case "factor" -> {
                try {
                    SurveyState.SCALE.setGround(Double.parseDouble(value));
                } catch (NumberFormatException ignored) {
                    // leave scale
                }
            }
            case "projection" -> Projections.setById(value);
            case "hud" -> {
                if ("bearing".equalsIgnoreCase(value)) {
                    HudSettings.setAngleMode(HudSettings.AngleMode.BEARING);
                } else {
                    HudSettings.setAngleMode(HudSettings.AngleMode.AZIMUTH);
                }
            }
            case "rate" -> {
                try {
                    ShotRate.setRateHz(Integer.parseInt(value));
                } catch (NumberFormatException ignored) {
                    // leave rate
                }
            }
            default -> { }
        }
    }

    public static void save() {
        if ("none".equals(worldId)) return;
        try {
            Files.createDirectories(dir());
            Files.writeString(pointsFile(), PointLists.STORE.toCsv(), StandardCharsets.UTF_8);
            saveOccupy();
            saveConfig();
        } catch (IOException ignored) {
            // keep the in-memory state
        }
    }

    private static void saveOccupy() throws IOException {
        OccupyPoint o = SurveyState.ENGINE.getOccupy();
        if (o == null) {
            Files.deleteIfExists(occupyFile());
            return;
        }
        String line = o.x() + "," + o.y() + "," + o.z() + ","
                + (o.description() == null ? "" : o.description());
        Files.writeString(occupyFile(), line, StandardCharsets.UTF_8);
    }

    private static void saveConfig() throws IOException {
        DistanceScale s = SurveyState.SCALE;
        String text = ""
                + "units=" + SurveyState.ENGINE.getUnits().name() + "\n"
                + "scale=" + s.mode().name() + "\n"
                + "factor=" + s.factor() + "\n"
                + "projection=" + Projections.current().id() + "\n"
                + "hud=" + (HudSettings.angleMode() == HudSettings.AngleMode.BEARING
                ? "bearing" : "azimuth") + "\n"
                + "rate=" + ShotRate.rateHz() + "\n";
        Files.writeString(configFile(), text, StandardCharsets.UTF_8);
    }

    public static void clear() {
        PointLists.STORE.clear();
        SurveyState.ENGINE.clearOccupy();
        applyDefaults();
        save();
    }

    private static void applyDefaults() {
        SurveyState.ENGINE.setUnits(Units.METERS);
        SurveyState.SCALE.setGrid();
        Projections.setById("webmercator");
        HudSettings.setAngleMode(HudSettings.AngleMode.AZIMUTH);
        ShotRate.setRateHz(60);
    }
}