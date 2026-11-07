package me.bluejay.levelheaded.core.math.projection;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Registry + session current projection. Default is Web Mercator.
 */
public final class Projections {

    private static final Map<String, MapProjection> BY_ID = new LinkedHashMap<>();
    private static final Map<String, String> ALIASES = new LinkedHashMap<>();

    static {
        register(WebMercatorProjection.INSTANCE, "wm", "mercator", "web-mercator", "3857");
        register(EquirectangularProjection.INSTANCE, "eqc", "equirect", "platecarree", "plate-carree");
        register(EqualEarthProjection.INSTANCE, "ee", "eqearth", "equal-earth");
    }

    private static MapProjection current = WebMercatorProjection.INSTANCE;

    private Projections() {}

    private static void register(MapProjection p, String... aliases) {
        BY_ID.put(p.id(), p);
        for (String a : aliases) {
            ALIASES.put(a.toLowerCase(Locale.ROOT), p.id());
        }
        ALIASES.put(p.id(), p.id());
    }

    public static MapProjection current() {
        return current;
    }

    public static MapProjection find(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String key = raw.trim().toLowerCase(Locale.ROOT);
        String id = ALIASES.getOrDefault(key, key);
        return BY_ID.get(id);
    }

    public static boolean setById(String raw) {
        MapProjection p = find(raw);
        if (p == null) return false;
        current = p;
        return true;
    }

    public static Iterable<MapProjection> all() {
        return BY_ID.values();
    }

    public static String listLine() {
        StringBuilder sb = new StringBuilder();
        for (MapProjection p : BY_ID.values()) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(p.id());
            if (p == current) sb.append(" (current)");
        }
        return sb.toString();
    }
}