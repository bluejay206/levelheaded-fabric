package me.bluejay.levelheaded;

/**
 * Pending PNEZD description. Any platform sets this: Java from the
 * description screen, Bedrock from its own form. The next shot save
 * reads it.
 */
public final class ShotDescription {

    private static String value = "";

    private ShotDescription() {}

    public static String get() {
        return value == null ? "" : value;
    }

    public static void set(String next) {
        value = next == null ? "" : next.trim();
    }

    public static boolean isSet() {
        return !get().isEmpty();
    }
}