package me.bluejay.client.config;

import net.fabricmc.loader.api.FabricLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SurveyorConfig {

    // Coord mode feature paused - everything commented out for stability
    /*
    public enum CoordMode {
        GLOBAL,
        LOCAL
    }

    private static CoordMode coordMode = CoordMode.LOCAL;

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("levelleaded.properties");

    public static void load() { ... }
    public static void save() { ... }
    public static CoordMode getCoordMode() { ... }
    public static void setCoordMode(CoordMode mode) { ... }
    public static String getCoordModeName() { return "local"; }
    */
}