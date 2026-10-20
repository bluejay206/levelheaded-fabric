package me.bluejay.levelheaded;

/**
 * Client-visible HUD preferences. Stored in common so {@code /lh hud}
 * (singleplayer) and {@code /lhc hud} (always client) share one setting.
 */
public final class HudSettings {

    public enum AngleMode {
        AZIMUTH,
        BEARING
    }

    private static AngleMode angleMode = AngleMode.AZIMUTH;

    private HudSettings() {}

    public static AngleMode angleMode() {
        return angleMode;
    }

    public static void setAngleMode(AngleMode mode) {
        angleMode = mode == null ? AngleMode.AZIMUTH : mode;
    }

    public static AngleMode toggleAngle() {
        angleMode = angleMode == AngleMode.AZIMUTH ? AngleMode.BEARING : AngleMode.AZIMUTH;
        return angleMode;
    }

    public static String statusLine() {
        return "HUD angle: " + (angleMode == AngleMode.BEARING
                ? "quadrant bearing (N28\u00b036'25\"W)"
                : "azimuth (0\u00b0 = north)");
    }
}
