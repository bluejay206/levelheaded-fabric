package me.bluejay.levelheaded;

/**
 * Named input actions shared by every platform.
 * <p>
 * A platform adapter binds each action to whatever that platform has:
 * a Java key, a Bedrock key, or a controller button. Actions are single
 * triggers. Modifier combos (Ctrl, Shift) are not used — they do not
 * exist on Bedrock or on a controller.
 */
public final class SurveyInput {

    public enum Action {
        /** Open the description field for the next shot. */
        SET_DESCRIPTION,
        /** Save the live shot. */
        SAVE_SHOT
    }

    @FunctionalInterface
    public interface Listener {
        void on(Action action);
    }

    private static Listener listener;

    private SurveyInput() {}

    /** The platform layer installs this. Only one listener is active. */
    public static void setListener(Listener next) {
        listener = next;
    }

    /** A platform binding calls this when its key or button fires. */
    public static void fire(Action action) {
        if (listener != null && action != null) {
            listener.on(action);
        }
    }
}
