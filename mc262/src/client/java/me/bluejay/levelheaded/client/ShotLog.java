package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.ShotDescription;
import me.bluejay.levelheaded.SurveyState;
import me.bluejay.levelheaded.core.data.OccupyPoint;
import me.bluejay.levelheaded.core.data.Units;
import me.bluejay.levelheaded.core.math.SurveyMath;
import me.bluejay.levelheaded.core.math.projection.Projections;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Last right-click / command shot, and CSV append for the V keybind.
 * File: {@code <gameDir>/levelheaded/shots_<yyyyMMdd_HHmmss>.csv}
 * First V press of a session creates the file; later presses append.
 * {@code /lh reset} and {@code /lhc reset} start a new file on the next V.
 */
public final class ShotLog {

    /** PNEZD first, then the rest of the shot record. */
    private static final String HEADER =
            "Point,Northing,Easting,Elevation,Description,"
                    + "timestamp_utc,kind,from_x,from_y,from_z,to_x,to_y,to_z,"
                    + "azimuth_deg,azimuth_dms,hz_m,dn_m,de_m,du_m,slope_m,"
                    + "units,scale_mode,projection,world_scale,note";

    private static final DateTimeFormatter ISO =
            DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    private static Shot last;
    private static Path current;
    private static int nextNumber = 1;

    private ShotLog() {}

    public record Shot(
            String kind,
            OccupyPoint from,
            double toX,
            double toY,
            double toZ,
            SurveyMath.Result result,
            Units units,
            String scaleMode,
            String projectionId,
            double worldScale,
            String note,
            long timestamp
    ) {}

    public static void capture(
            String kind,
            double toX,
            double toY,
            double toZ,
            SurveyMath.Result result,
            String note
    ) {
        OccupyPoint from = SurveyState.ENGINE.getOccupy();
        if (from == null || result == null) return;
        last = new Shot(
                kind == null ? "" : kind,
                from,
                toX, toY, toZ,
                result,
                SurveyState.ENGINE.getUnits(),
                SurveyState.SCALE.mode().name(),
                Projections.current().id(),
                SurveyState.SCALE.worldScale(),
                note == null ? "" : note,
                System.currentTimeMillis()
        );
    }

    public static boolean hasLast() {
        return last != null;
    }

    /** Append the last captured shot. Returns the shot number, or -1 if none. */
    public static int saveLast() {
        if (last == null) return -1;
        try {
            Path file = currentFile();
            Files.createDirectories(file.getParent());
            if (!Files.exists(file) || Files.size(file) == 0) {
                Files.writeString(file, HEADER + System.lineSeparator(),
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                nextNumber = 1;
            }
            int n = nextNumber++;
            Files.writeString(file, row(n, last) + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            feedback("Saved shot " + n + " → " + file.getFileName());
            return n;
        } catch (IOException e) {
            feedback("Shot save failed: " + e.getMessage());
            return -1;
        }
    }

    /**
     * First V of a session (or the first after reset) creates
     * {@code shots_yyyyMMdd_HHmmss.csv}. Later presses append to it.
     */
    private static Path currentFile() {
        boolean fresh = SurveyState.consumeNewLog();
        if (current == null || fresh) {
            String stamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                    .withZone(ZoneOffset.UTC)
                    .format(Instant.now());
            current = FabricLoader.getInstance().getGameDir()
                    .resolve("assets/levelheaded")
                    .resolve("shots_" + stamp + ".csv");
            nextNumber = 1;
        }
        return current;
    }

    /**
     * PNEZD of the shot point in survey space:
     * Point = shot number, Northing = -toZ (+N is north), Easting = toX,
     * Elevation = toY, Description = ShotDescription (kind if blank).
     */
    private static String row(int n, Shot s) {
        double northing = -s.toZ();
        double easting = s.toX();
        double elevation = s.toY();
        String desc = ShotDescription.get();
        if (desc.isEmpty()) {
            desc = s.kind == null ? "" : s.kind;
            if (s.note != null && !s.note.isBlank()) {
                desc = desc.isEmpty() ? s.note : desc + " " + s.note;
            }
        }
        return String.join(",",
                Integer.toString(n),
                num(northing),
                num(easting),
                num(elevation),
                csv(desc),
                ISO.format(Instant.ofEpochMilli(s.timestamp)),
                csv(s.kind),
                num(s.from.x()), num(s.from.y()), num(s.from.z()),
                num(s.toX), num(s.toY), num(s.toZ),
                num(s.result.azimuthDeg()),
                csv(s.result.formatAzimuthDMS()),
                num(s.result.horizontalDistance()),
                num(s.result.deltaNorth()),
                num(s.result.deltaEast()),
                num(s.result.deltaUp()),
                num(s.result.slopeDistance()),
                csv(s.units.name()),
                csv(s.scaleMode),
                csv(s.projectionId),
                num(s.worldScale),
                csv(s.note)
        );
    }

    private static String num(double v) {
        return String.format(java.util.Locale.US, "%.4f", v);
    }

    private static String csv(String raw) {
        if (raw == null) return "";
        if (raw.indexOf(',') >= 0 || raw.indexOf('"') >= 0 || raw.indexOf('\n') >= 0) {
            return "\"" + raw.replace("\"", "\"\"") + "\"";
        }
        return raw;
    }

    private static void feedback(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui != null && mc.gui.hud != null) {
            mc.gui.hud.setOverlayMessage(Component.literal(message), false);
        }
    }
}