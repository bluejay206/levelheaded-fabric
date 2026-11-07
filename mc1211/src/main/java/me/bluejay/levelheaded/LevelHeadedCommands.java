package me.bluejay.levelheaded;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import me.bluejay.levelheaded.core.data.DistanceScale;
import me.bluejay.levelheaded.core.data.Units;
import me.bluejay.levelheaded.core.math.AngleFormat;
import me.bluejay.levelheaded.core.math.projection.MapProjection;
import me.bluejay.levelheaded.core.math.projection.Projections;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.entity.Entity;

import java.util.List;
import java.util.Locale;

public final class LevelHeadedCommands {

    private static final List<String> UNIT_ALIASES = List.of(
            "m", "meters", "metre", "metres",
            "ft", "feet", "foot",
            "ch", "chains", "chain",
            "mi", "miles", "mile"
    );

    private static final SuggestionProvider<ServerCommandSource> UNIT_SUGGESTIONS =
            (ctx, builder) -> CommandSource.suggestMatching(UNIT_ALIASES, builder);

    private LevelHeadedCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<ServerCommandSource> root = CommandManager.literal("lh")
                    .executes(ctx -> {
                        ctx.getSource().sendFeedback(() -> Text.literal(statusSummary()), false);
                        return 1;
                    })
                    .then(CommandManager.literal("status")
                            .executes(ctx -> {
                                ctx.getSource().sendFeedback(() -> Text.literal(statusSummary()), false);
                                return 1;
                            }))
                    .then(CommandManager.literal("help")
                            .executes(ctx -> {
                                ctx.getSource().sendFeedback(() -> Text.literal(helpText()), false);
                                return 1;
                            }))
                    .then(unitsBranch())
                    .then(scaleBranch())
                    .then(hudBranch())
                    .then(CommandManager.literal("reset")
                            .executes(ctx -> {
                                SurveyState.reset();
                                ctx.getSource().sendFeedback(
                                        () -> Text.literal("Occupy cleared. Next V starts a new CSV."), false);
                                return 1;
                            }));
            dispatcher.register(root);
        });
    }

    private static LiteralArgumentBuilder<ServerCommandSource> hudBranch() {
        return CommandManager.literal("hud")
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(() -> Text.literal(HudSettings.statusLine()), false);
                    return 1;
                })
                .then(CommandManager.literal("angle")
                        .executes(ctx -> {
                            ctx.getSource().sendFeedback(() -> Text.literal(HudSettings.statusLine()), false);
                            return 1;
                        })
                        .then(CommandManager.literal("azimuth")
                                .executes(ctx -> {
                                    HudSettings.setAngleMode(HudSettings.AngleMode.AZIMUTH);
                                    ctx.getSource().sendFeedback(
                                            () -> Text.literal("HUD angle: azimuth"), false);
                                    return 1;
                                }))
                        .then(CommandManager.literal("bearing")
                                .executes(ctx -> {
                                    HudSettings.setAngleMode(HudSettings.AngleMode.BEARING);
                                    ctx.getSource().sendFeedback(
                                            () -> Text.literal("HUD angle: quadrant bearing"), false);
                                    return 1;
                                }))
                        .then(CommandManager.literal("toggle")
                                .executes(ctx -> {
                                    HudSettings.AngleMode mode = HudSettings.toggleAngle();
                                    ctx.getSource().sendFeedback(
                                            () -> Text.literal("HUD angle: "
                                                    + mode.name().toLowerCase()), false);
                                    return 1;
                                })));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> unitsBranch() {
        return CommandManager.literal("units")
                .executes(ctx -> {
                    Units u = SurveyState.ENGINE.getUnits();
                    ctx.getSource().sendFeedback(
                            () -> Text.literal("Units: " + u.name().toLowerCase(Locale.ROOT)
                                    + " (" + u.symbol() + ")"),
                            false);
                    return 1;
                })
                .then(CommandManager.argument("unit", StringArgumentType.word())
                        .suggests(UNIT_SUGGESTIONS)
                        .executes(ctx -> {
                            String raw = StringArgumentType.getString(ctx, "unit")
                                    .toLowerCase(Locale.ROOT);
                            Units parsed = parseUnits(raw);
                            if (parsed == null) {
                                ctx.getSource().sendError(Text.literal(
                                        "Unknown unit '" + raw + "'. Use: m|ft|ch|mi"));
                                return 0;
                            }
                            SurveyState.ENGINE.setUnits(parsed);
                            ctx.getSource().sendFeedback(
                                    () -> Text.literal("Units set to "
                                            + parsed.name().toLowerCase(Locale.ROOT)
                                            + " (" + parsed.symbol() + ")"),
                                    false);
                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<ServerCommandSource> scaleBranch() {
        return CommandManager.literal("scale")
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(() -> Text.literal(SurveyState.SCALE.statusLine()), false);
                    return 1;
                })
                .then(CommandManager.literal("grid")
                        .executes(ctx -> {
                            SurveyState.SCALE.setGrid();
                            ctx.getSource().sendFeedback(() -> Text.literal(SurveyState.SCALE.statusLine()), false);
                            return 1;
                        }))
                .then(CommandManager.literal("geodesic")
                        .executes(ctx -> {
                            SurveyState.SCALE.setGeodesic();
                            ctx.getSource().sendFeedback(() -> Text.literal(SurveyState.SCALE.statusLine()), false);
                            return 1;
                        }))
                .then(CommandManager.literal("projection")
                        .executes(ctx -> {
                            MapProjection proj = Projections.current();
                            ctx.getSource().sendFeedback(
                                    () -> Text.literal(
                                            "Projection: " + proj.displayName()
                                                    + " (" + proj.id() + ")\nAvailable: "
                                                    + Projections.listLine()),
                                    false);
                            return 1;
                        })
                        .then(CommandManager.argument("id", StringArgumentType.word())
                                .executes(ctx -> {
                                    String raw = StringArgumentType.getString(ctx, "id");
                                    if (!Projections.setById(raw)) {
                                        ctx.getSource().sendError(Text.literal(
                                                "Unknown projection '" + raw + "'. Available: "
                                                        + Projections.listLine()));
                                        return 0;
                                    }
                                    MapProjection proj = Projections.current();
                                    DistanceScale scale = SurveyState.SCALE;
                                    if (scale.mode() == DistanceScale.Mode.GROUND && scale.latitudeDeg() != null) {
                                        scale.setGroundFromLatitude(scale.latitudeDeg());
                                    }
                                    ctx.getSource().sendFeedback(
                                            () -> Text.literal(
                                                    "Projection set to " + proj.displayName()
                                                            + " (" + proj.id() + ")\n"
                                                            + scale.statusLine()),
                                            false);
                                    return 1;
                                })))
                .then(CommandManager.literal("client")
                        .executes(ctx -> {
                            ctx.getSource().sendFeedback(
                                    () -> Text.literal(
                                            "Client tools are registered as /lhc (client-only).\n"
                                                    + "/lhc occupy | rod | scope | hud rod|scope|off | status\n"
                                                    + "Server /lh owns the 'lh' root, so /lh client cannot run here."),
                                    false);
                            return 1;
                        }))
                .then(CommandManager.literal("world")
                        .executes(ctx -> {
                            ctx.getSource().sendFeedback(
                                    () -> Text.literal(String.format(
                                            "worldScale = %.6f m/block (WebMercator World Scale)",
                                            SurveyState.SCALE.worldScale())),
                                    false);
                            return 1;
                        })
                        .then(CommandManager.argument("metresPerBlock", DoubleArgumentType.doubleArg(1e-6, 1000.0))
                                .executes(ctx -> {
                                    double ws = DoubleArgumentType.getDouble(ctx, "metresPerBlock");
                                    SurveyState.SCALE.setWorldScale(ws);
                                    ctx.getSource().sendFeedback(
                                            () -> Text.literal(SurveyState.SCALE.statusLine()),
                                            false);
                                    return 1;
                                })))
                .then(CommandManager.literal("ground")
                        .then(CommandManager.argument("factor", DoubleArgumentType.doubleArg(1e-6, 10.0))
                                .executes(ctx -> {
                                    double factor = DoubleArgumentType.getDouble(ctx, "factor");
                                    SurveyState.SCALE.setGround(factor);
                                    ctx.getSource().sendFeedback(() -> Text.literal(SurveyState.SCALE.statusLine()), false);
                                    return 1;
                                }))
                        .then(CommandManager.literal("lat")
                                .then(CommandManager.argument("degrees", DoubleArgumentType.doubleArg(-85.0, 85.0))
                                        .executes(ctx -> {
                                            double lat = DoubleArgumentType.getDouble(ctx, "degrees");
                                            SurveyState.SCALE.setGroundFromLatitude(lat);
                                            DistanceScale s = SurveyState.SCALE;
                                            String msg = s.statusLine()
                                                    + String.format(" | packed %s",
                                                    AngleFormat.formatPacked(AngleFormat.toPackedDddMmss(lat)));
                                            ctx.getSource().sendFeedback(() -> Text.literal(msg), false);
                                            return 1;
                                        })))
                        .then(CommandManager.literal("here")
                                .executes(ctx -> {
                                    Entity entity = ctx.getSource().getEntity();
                                    if (entity == null) {
                                        ctx.getSource().sendError(Text.literal(
                                                "No entity position (run as a player)."));
                                        return 0;
                                    }
                                    double z = entity.getZ();
                                    SurveyState.SCALE.setGroundFromBlockZ(z);
                                    DistanceScale s = SurveyState.SCALE;
                                    double lat = s.latitudeDeg() != null ? s.latitudeDeg() : 0.0;
                                    String msg = s.statusLine()
                                            + String.format(" | from Z=%.1f → lat %s",
                                            z, AngleFormat.toDms(lat, true));
                                    ctx.getSource().sendFeedback(() -> Text.literal(msg), false);
                                    return 1;
                                }))
                        .then(CommandManager.literal("z")
                                .then(CommandManager.argument("blockZ", DoubleArgumentType.doubleArg())
                                        .executes(ctx -> {
                                            double z = DoubleArgumentType.getDouble(ctx, "blockZ");
                                            SurveyState.SCALE.setGroundFromBlockZ(z);
                                            DistanceScale s = SurveyState.SCALE;
                                            double lat = s.latitudeDeg() != null ? s.latitudeDeg() : 0.0;
                                            String msg = s.statusLine()
                                                    + String.format(" | from Z=%.1f → lat %s",
                                                    z, AngleFormat.toDms(lat, true));
                                            ctx.getSource().sendFeedback(() -> Text.literal(msg), false);
                                            return 1;
                                        }))));
    }

    private static Units parseUnits(String raw) {
        return switch (raw) {
            case "m", "meter", "meters", "metre", "metres" -> Units.METERS;
            case "ft", "foot", "feet" -> Units.FEET;
            case "ch", "chain", "chains" -> Units.CHAINS;
            case "mi", "mile", "miles" -> Units.MILES;
            default -> null;
        };
    }

    private static String statusSummary() {
        Units u = SurveyState.ENGINE.getUnits();
        String occupy = SurveyState.hasOccupy()
                ? "set"
                : "none (right-click a Transit)";
        return String.format(
                "LevelHeaded — occupy: %s | units: %s (%s) | %s%nUse /lh help for commands.",
                occupy,
                u.name().toLowerCase(Locale.ROOT),
                u.symbol(),
                SurveyState.SCALE.statusLine()
        );
    }

    private static String helpText() {
        return """
                LevelHeaded commands:
                  /lh                 — status
                  /lh status          — same as /lh
                  /lh help            — this help
                  /lh units           — show units
                  /lh units <unit>    — m|ft|ch|mi
                  /lh scale           — show scale
                  /lh scale grid      — 1 block = 1 m
                  /lh scale geodesic  — WGS84 Vincenty (matches Google Earth)
                  /lh scale world <m> — projected metres per block
                  /lh scale projection <id> — webmercator | equirectangular | equalearth
                  /lh client — points at client-only /lhc
                  /lh reset                    — clear occupy
                  /lh scale ground <f>         — local cos factor
                  /lh scale ground lat <deg>
                  /lh scale ground here        — from your Z
                  /lh scale ground z <Z>
                  /lh hud                      — HUD element settings
                  /lh hud angle                — show angle mode
                  /lh hud angle azimuth        — azimuth (0° = north)
                  /lh hud angle bearing        — quadrant bearing (N28°36'25"W)
                  /lh hud angle toggle         — flip azimuth / bearing
                """;
    }
}