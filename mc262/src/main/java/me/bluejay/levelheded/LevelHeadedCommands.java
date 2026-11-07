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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Locale;

public final class LevelHeadedCommands {

    private static final List<String> UNIT_ALIASES = List.of(
            "m", "meters", "metre", "metres",
            "ft", "feet", "foot",
            "ch", "chains", "chain",
            "mi", "miles", "mile"
    );

    private static final SuggestionProvider<CommandSourceStack> UNIT_SUGGESTIONS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(UNIT_ALIASES, builder);

    private LevelHeadedCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("lh")
                    .executes(ctx -> {
                        ctx.getSource().sendSuccess(() -> Component.literal(statusSummary()), false);
                        return 1;
                    })
                    .then(Commands.literal("status")
                            .executes(ctx -> {
                                ctx.getSource().sendSuccess(() -> Component.literal(statusSummary()), false);
                                return 1;
                            }))
                    .then(Commands.literal("help")
                            .executes(ctx -> {
                                ctx.getSource().sendSuccess(() -> Component.literal(helpText()), false);
                                return 1;
                            }))
                    .then(unitsBranch())
                    .then(scaleBranch())
                    .then(hudBranch())
                    .then(Commands.literal("reset")
                            .executes(ctx -> {
                                SurveyState.resetSession();
                                ctx.getSource().sendSuccess(
                                        () -> Component.literal("Occupy cleared. Next V starts a new CSV."), true);
                                return 1;
                            }));
            dispatcher.register(root);
        });
    }


    private static LiteralArgumentBuilder<CommandSourceStack> hudBranch() {
        return Commands.literal("hud")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(HudSettings.statusLine()), false);
                    return 1;
                })
                .then(Commands.literal("angle")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> Component.literal(HudSettings.statusLine()), false);
                            return 1;
                        })
                        .then(Commands.literal("azimuth")
                                .executes(ctx -> {
                                    HudSettings.setAngleMode(HudSettings.AngleMode.AZIMUTH);
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("HUD angle: azimuth"), true);
                                    return 1;
                                }))
                        .then(Commands.literal("bearing")
                                .executes(ctx -> {
                                    HudSettings.setAngleMode(HudSettings.AngleMode.BEARING);
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("HUD angle: quadrant bearing"), true);
                                    return 1;
                                }))
                        .then(Commands.literal("toggle")
                                .executes(ctx -> {
                                    HudSettings.AngleMode mode = HudSettings.toggleAngle();
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("HUD angle: "
                                                    + mode.name().toLowerCase()), true);
                                    return 1;
                                })));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> unitsBranch() {
        return Commands.literal("units")
                .executes(ctx -> {
                    Units u = SurveyState.ENGINE.getUnits();
                    ctx.getSource().sendSuccess(
                            () -> Component.literal("Units: " + u.name().toLowerCase(Locale.ROOT)
                                    + " (" + u.symbol() + ")"),
                            false);
                    return 1;
                })
                .then(Commands.argument("unit", StringArgumentType.word())
                        .suggests(UNIT_SUGGESTIONS)
                        .executes(ctx -> {
                            String raw = StringArgumentType.getString(ctx, "unit")
                                    .toLowerCase(Locale.ROOT);
                            Units parsed = parseUnits(raw);
                            if (parsed == null) {
                                ctx.getSource().sendFailure(Component.literal(
                                        "Unknown unit '" + raw + "'. Use: m|ft|ch|mi"));
                                return 0;
                            }
                            SurveyState.ENGINE.setUnits(parsed);
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("Units set to "
                                            + parsed.name().toLowerCase(Locale.ROOT)
                                            + " (" + parsed.symbol() + ")"),
                                    true);
                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> scaleBranch() {
        return Commands.literal("scale")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.literal(SurveyState.SCALE.statusLine()), false);
                    return 1;
                })
                .then(Commands.literal("grid")
                        .executes(ctx -> {
                            SurveyState.SCALE.setGrid();
                            ctx.getSource().sendSuccess(() -> Component.literal(SurveyState.SCALE.statusLine()), true);
                            return 1;
                        }))
                .then(Commands.literal("geodesic")
                        .executes(ctx -> {
                            SurveyState.SCALE.setGeodesic();
                            ctx.getSource().sendSuccess(() -> Component.literal(SurveyState.SCALE.statusLine()), true);
                            return 1;
                        }))
                .then(Commands.literal("projection")
                        .executes(ctx -> {
                            MapProjection proj = Projections.current();
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal(
                                            "Projection: " + proj.displayName()
                                                    + " (" + proj.id() + ")\nAvailable: "
                                                    + Projections.listLine()),
                                    false);
                            return 1;
                        })
                        .then(Commands.argument("id", StringArgumentType.word())
                                .executes(ctx -> {
                                    String raw = StringArgumentType.getString(ctx, "id");
                                    if (!Projections.setById(raw)) {
                                        ctx.getSource().sendFailure(Component.literal(
                                                "Unknown projection '" + raw + "'. Available: "
                                                        + Projections.listLine()));
                                        return 0;
                                    }
                                    MapProjection proj = Projections.current();
                                    DistanceScale scale = SurveyState.SCALE;
                                    if (scale.mode() == DistanceScale.Mode.GROUND && scale.latitudeDeg() != null) {
                                        scale.setGroundFromLatitude(scale.latitudeDeg());
                                    }
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal(
                                                    "Projection set to " + proj.displayName()
                                                            + " (" + proj.id() + ")\n"
                                                            + scale.statusLine()),
                                            true);
                                    return 1;
                                })))
                .then(Commands.literal("client")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal(
                                            "Client tools are registered as /lhc (client-only).\n"
                                                    + "/lhc occupy | rod | scope | hud rod|scope|off | status\n"
                                                    + "Server /lh owns the 'lh' root, so /lh client cannot run here."),
                                    false);
                            return 1;
                        }))
                .then(Commands.literal("world")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal(String.format(
                                            "worldScale = %.6f m/block (WebMercator World Scale)",
                                            SurveyState.SCALE.worldScale())),
                                    false);
                            return 1;
                        })
                        .then(Commands.argument("metresPerBlock", DoubleArgumentType.doubleArg(1e-6, 1000.0))
                                .executes(ctx -> {
                                    double ws = DoubleArgumentType.getDouble(ctx, "metresPerBlock");
                                    SurveyState.SCALE.setWorldScale(ws);
                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal(SurveyState.SCALE.statusLine()),
                                            true);
                                    return 1;
                                })))
                .then(Commands.literal("ground")
                        .then(Commands.argument("factor", DoubleArgumentType.doubleArg(1e-6, 10.0))
                                .executes(ctx -> {
                                    double factor = DoubleArgumentType.getDouble(ctx, "factor");
                                    SurveyState.SCALE.setGround(factor);
                                    ctx.getSource().sendSuccess(() -> Component.literal(SurveyState.SCALE.statusLine()), true);
                                    return 1;
                                }))
                        .then(Commands.literal("lat")
                                .then(Commands.argument("degrees", DoubleArgumentType.doubleArg(-85.0, 85.0))
                                        .executes(ctx -> {
                                            double lat = DoubleArgumentType.getDouble(ctx, "degrees");
                                            SurveyState.SCALE.setGroundFromLatitude(lat);
                                            DistanceScale s = SurveyState.SCALE;
                                            String msg = s.statusLine()
                                                    + String.format(" | packed %s",
                                                    AngleFormat.formatPacked(AngleFormat.toPackedDddMmss(lat)));
                                            ctx.getSource().sendSuccess(() -> Component.literal(msg), true);
                                            return 1;
                                        })))
                        .then(Commands.literal("here")
                                .executes(ctx -> {
                                    Entity entity = ctx.getSource().getEntity();
                                    if (entity == null) {
                                        ctx.getSource().sendFailure(Component.literal(
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
                                    ctx.getSource().sendSuccess(() -> Component.literal(msg), true);
                                    return 1;
                                }))
                        .then(Commands.literal("z")
                                .then(Commands.argument("blockZ", DoubleArgumentType.doubleArg())
                                        .executes(ctx -> {
                                            double z = DoubleArgumentType.getDouble(ctx, "blockZ");
                                            SurveyState.SCALE.setGroundFromBlockZ(z);
                                            DistanceScale s = SurveyState.SCALE;
                                            double lat = s.latitudeDeg() != null ? s.latitudeDeg() : 0.0;
                                            String msg = s.statusLine()
                                                    + String.format(" | from Z=%.1f → lat %s",
                                                    z, AngleFormat.toDms(lat, true));
                                            ctx.getSource().sendSuccess(() -> Component.literal(msg), true);
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