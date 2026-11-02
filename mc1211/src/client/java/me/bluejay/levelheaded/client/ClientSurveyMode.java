package me.bluejay.levelheaded.client;

/**
 * Client-side HUD mode. OFF means items drive the HUD (Rod/Scope held).
 * ROD and SCOPE force the mode regardless of held item, for third-party SMP.
 */
public final class ClientSurveyMode {

    public enum HudMode {
        OFF,
        ROD,
        SCOPE
    }

    private static HudMode mode = HudMode.OFF;

    private ClientSurveyMode() {}

    public static HudMode hudMode() {
        return mode;
    }

    public static void setHudMode(HudMode next) {
        mode = next == null ? HudMode.OFF : next;
    }
}
