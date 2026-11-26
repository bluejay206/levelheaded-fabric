package me.bluejay.levelheaded.core.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory point list. Pure Java. Persistence and the field book
 * are handled by the platform layer.
 */
public final class PointStore {

    private final Map<String, ControlPoint> points = new LinkedHashMap<>();
    private int nextNumber = 1;
    private static final int MAX_ROWS = 50_000;

    public void put(ControlPoint point) {
        if (point == null || point.id() == null || point.id().isBlank()) return;
        points.put(point.id(), point);
    }

    public ControlPoint get(String id) {
        return points.get(id);
    }

    public boolean remove(String id) {
        return points.remove(id) != null;
    }

    public List<ControlPoint> list() {
        return new ArrayList<>(points.values());
    }

    public List<ControlPoint> list(PointKind kind) {
        List<ControlPoint> out = new ArrayList<>();
        for (ControlPoint p : points.values()) {
            if (p.kind() == kind) out.add(p);
        }
        return out;
    }

    public void clear() {
        points.clear();
        nextNumber = 1;
    }

    public int size() {
        return points.size();
    }

    /** Next unused numeric id ("1", "2", ...). */
    public String nextId() {
        while (points.containsKey(Integer.toString(nextNumber))) {
            nextNumber++;
        }
        return Integer.toString(nextNumber++);
    }

    /** PNEZD lines, no header. */
    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("Point,Northing,Easting,Elevation,Description,Kind\n");
        for (ControlPoint p : points.values()) {
            sb.append(csv(p.id())).append(',')
                    .append(num(p.northing())).append(',')
                    .append(num(p.easting())).append(',')
                    .append(num(p.elevation())).append(',')
                    .append(csv(p.description())).append(',')
                    .append(p.kind().name())
                    .append('\n');
        }
        return sb.toString();
    }

    /**
     * Read PNEZD CSV. Header optional. Kind column optional (defaults to TOPO).
     * Duplicate ids overwrite.
     */
    public int importCsv(String csv) {
        if (csv == null || csv.isBlank()) return 0;
        int added = 0;
        String[] lines = csv.split("\\R");
        for (String line : lines) {
            if (added >= MAX_ROWS) break;
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.toLowerCase().startsWith("point")) continue;
            String[] c = trimmed.split(",", -1);
            if (c.length < 4) continue;
            try {
                String id = c[0].trim();
                if (id.length() > 64) continue;
                double n = Double.parseDouble(c[1].trim());
                double e = Double.parseDouble(c[2].trim());
                double z = Double.parseDouble(c[3].trim());
                String desc = c.length > 4 ? c[4].trim() : "";
                if (desc.length() > 256) desc = desc.substring(0, 256);
                PointKind kind = PointKind.TOPO;
                if (c.length > 5) {
                    try {
                        kind = PointKind.valueOf(c[5].trim().toUpperCase());
                    } catch (IllegalArgumentException ignored) {
                        // keep TOPO
                    }
                }
                put(new ControlPoint(id, kind, n, e, z, desc));
                added++;
            } catch (NumberFormatException ignored) {
                // skip bad row
            }
        }
        return added;
    }

    private static String num(double v) {
        return String.format("%.2f", v);
    }

    private static String csv(String s) {
        if (s == null) return "";
        s = s.replace("\r", " ").replace("\n", " ");
        if (!s.isEmpty() && "=+-@".indexOf(s.charAt(0)) >= 0) {
            s = "'" + s;
        }
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}