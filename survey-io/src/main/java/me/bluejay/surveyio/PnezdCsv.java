package me.bluejay.surveyio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pure-Java reader/writer for LevelHeaded-style PNEZD CSV.
 * Header: Point,Northing,Easting,Elevation,Description,...
 * Compatible with the CSV produced by LevelHeaded ShotLog.
 */
public final class PnezdCsv {

    public static final String HEADER =
            "Point,Northing,Easting,Elevation,Description";

    private PnezdCsv() {}

    public static List<SurveyPoint> read(Path path) throws IOException {
        String content = Files.readString(path, StandardCharsets.UTF_8);
        return read(content);
    }

    public static List<SurveyPoint> read(String csv) {
        List<SurveyPoint> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(csv))) {
            String line;
            boolean headerSkipped = false;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (!headerSkipped && line.startsWith("Point")) {
                    headerSkipped = true;
                    continue;
                }
                headerSkipped = true;
                SurveyPoint p = parseLine(line);
                if (p != null) result.add(p);
            }
        } catch (IOException ignored) {
            // StringReader does not throw
        }
        return result;
    }

    private static SurveyPoint parseLine(String line) {
        String[] parts = splitCsv(line);
        if (parts.length < 5) return null;
        try {
            int number = Integer.parseInt(parts[0].trim());
            double n = Double.parseDouble(parts[1].trim());
            double e = Double.parseDouble(parts[2].trim());
            double z = Double.parseDouble(parts[3].trim());
            String desc = unquote(parts[4]);
            return new SurveyPoint(number, n, e, z, desc);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String[] splitCsv(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(String[]::new);
    }

    private static String unquote(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s;
    }

    public static String write(List<SurveyPoint> points) {
        StringBuilder sb = new StringBuilder();
        sb.append(HEADER).append(System.lineSeparator());
        for (SurveyPoint p : points) {
            sb.append(format(p)).append(System.lineSeparator());
        }
        return sb.toString();
    }

    public static void write(Path path, List<SurveyPoint> points) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, write(points), StandardCharsets.UTF_8);
    }

    private static String format(SurveyPoint p) {
        return String.join(",",
                Integer.toString(p.number()),
                num(p.northing()),
                num(p.easting()),
                num(p.elevation()),
                csv(p.description())
        );
    }

    private static String num(double v) {
        return String.format(Locale.US, "%.4f", v);
    }

    private static String csv(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        if (raw.indexOf(',') >= 0 || raw.indexOf('"') >= 0 || raw.indexOf('\n') >= 0) {
            return "\"" + raw.replace("\"", "\"\"") + "\"";
        }
        return raw;
    }
}
