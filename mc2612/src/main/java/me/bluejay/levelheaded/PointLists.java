package me.bluejay.levelheaded;

import me.bluejay.levelheaded.core.data.PointStore;

/**
 * Single shared point list. Lives in main sources so the transit block
 * and the client screens can both reach it.
 */
public final class PointLists {

    public static final PointStore STORE = new PointStore();

    private static Runnable onChange;

    private PointLists() {}

    /** Client installs this so a main-source change can trigger a save. */
    public static void setOnChange(Runnable listener) {
        onChange = listener;
    }

    /** Call after any store or occupy change that originated in main sources. */
    public static void changed() {
        if (onChange != null) onChange.run();
    }
}