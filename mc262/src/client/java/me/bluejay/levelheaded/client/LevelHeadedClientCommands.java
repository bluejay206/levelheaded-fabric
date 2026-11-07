package me.bluejay.levelheaded.client;

import com.mojang.brigadier.CommandDispatcher;
import me.bluejay.levelheaded.HudSettings;
import me.bluejay.levelheaded.SurveyState;
import me.bluejay.levelheaded.core.data.DistanceScale;
import me.bluejay.levelheaded.core.data.Units;
import me.bluejay.levelheaded.core.math.SurveyMath;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Pure client-side command tree: {@code /lhc ...}
 *
 * <p>Registered via {@link ClientCommandRegistrationCallback}. Runs entirely on
 * the client, so it works when joining a third-party SMP that does not have
 * LevelHeaded installed. Does not require custom items or blocks.
 *
 * <pre>
 * /lhc occupy              — set occupy to current player position
 * /lhc occupy clear
 * /lhc rod                 — one-shot measure: player vs occupy
 * /lhc scope               — one-shot measure: raycast vs occupy
 * /lhc hud off|rod|scope   — continuous HUD mode
 * /lhc reset              — clear occupy
 * /lhc status
 * </pre>
 */
public final class LevelHeadedClientCommands {

    private LevelHeadedClientCommands() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(LevelHeadedClientCommands::register);
    }

    private static void register(CommandDispatcher<FabricClientCommandSource> dispatcher,
                                 CommandBuildContext buildContext) {
        // Distinct root so it does not collide with server-registered /lh.
        dispatcher.register(ClientCommands.literal("lhc")
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(Component.literal(statusText()));
                    return 1;
                })
                .then(occupyBranch())
                .then(ClientCommands.literal("rod")
                        .executes(ctx -> measureRod(ctx.getSource())))
                .then(ClientCommands.literal("scope")
                        .executes(ctx -> measureScope(ctx.getSource())))
                .then(hudBranch())
                .then(ClientCommands.literal("reset")
                        .executes(ctx -> {
                            SurveyState.resetSession();
                            ctx.getSource().sendFeedback(Component.literal("Occupy cleared. Next V starts a new CSV."));
                            return 1;
                        }))
                .then(ClientCommands.literal("status")
                        .executes(ctx -> {
                            ctx.getSource().sendFeedback(Component.literal(statusText()));
                            return 1;
                        })));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> occupyBranch() {
        return ClientCommands.literal("occupy")
                .executes(ctx -> setOccupyHere(ctx.getSource()))
                .then(ClientCommands.literal("here")
                        .executes(ctx -> setOccupyHere(ctx.getSource())))
                .then(ClientCommands.literal("clear")
                        .executes(ctx -> {
                            SurveyState.ENGINE.setOccupy(null);
                            ctx.getSource().sendFeedback(Component.literal("Occupy cleared."));
                            return 1;
                        }));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> hudBranch() {
        return ClientCommands.literal("hud")
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(Component.literal(
                            "HUD mode: " + ClientSurveyMode.hudMode().name().toLowerCase()
                                    + " | " + HudSettings.statusLine()));
                    return 1;
                })
                .then(angleBranch())
                .then(ClientCommands.literal("off")
                        .executes(ctx -> {
                            ClientSurveyMode.setHudMode(ClientSurveyMode.HudMode.OFF);
                            ctx.getSource().sendFeedback(Component.literal("Client HUD off."));
                            return 1;
                        }))
                .then(ClientCommands.literal("rod")
                        .executes(ctx -> {
                            ClientSurveyMode.setHudMode(ClientSurveyMode.HudMode.ROD);
                            ctx.getSource().sendFeedback(Component.literal(
                                    "Client HUD: ROD (player position vs occupy)."));
                            return 1;
                        }))
                .then(ClientCommands.literal("scope")
                        .executes(ctx -> {
                            ClientSurveyMode.setHudMode(ClientSurveyMode.HudMode.SCOPE);
                            ctx.getSource().sendFeedback(Component.literal(
                                    "Client HUD: SCOPE (raycast target vs occupy)."));
                            return 1;
                        }));
    }


    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> angleBranch() {
        return ClientCommands.literal("angle")
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(Component.literal(HudSettings.statusLine()));
                    return 1;
                })
                .then(ClientCommands.literal("azimuth")
                        .executes(ctx -> {
                            HudSettings.setAngleMode(HudSettings.AngleMode.AZIMUTH);
                            ctx.getSource().sendFeedback(Component.literal("HUD angle: azimuth"));
                            return 1;
                        }))
                .then(ClientCommands.literal("bearing")
                        .executes(ctx -> {
                            HudSettings.setAngleMode(HudSettings.AngleMode.BEARING);
                            ctx.getSource().sendFeedback(Component.literal("HUD angle: quadrant bearing"));
                            return 1;
                        }))
                .then(ClientCommands.literal("toggle")
                        .executes(ctx -> {
                            HudSettings.AngleMode mode = HudSettings.toggleAngle();
                            ctx.getSource().sendFeedback(Component.literal(
                                    "HUD angle: " + mode.name().toLowerCase()));
                            return 1;
                        }));
    }

    private static int setOccupyHere(FabricClientCommandSource source) {
        LocalPlayer player = source.getPlayer();
        if (player == null) {
            source.sendError(Component.literal("No local player."));
            return 0;
        }
        Vec3 pos = player.position();
        SurveyState.setOccupy(pos.x, pos.y, pos.z, "client");
        source.sendFeedback(Component.literal(String.format(
                "Occupy set (client) to %.2f, %.2f, %.2f", pos.x, pos.y, pos.z)));
        return 1;
    }

    private static int measureRod(FabricClientCommandSource source) {
        LocalPlayer player = source.getPlayer();
        if (player == null) {
            source.sendError(Component.literal("No local player."));
            return 0;
        }
        if (!SurveyState.hasOccupy()) {
            source.sendError(Component.literal("No occupy set. Use /lhc occupy first."));
            return 0;
        }
        Vec3 pos = player.position();
        SurveyMath.Result result = SurveyState.ENGINE.scaledResult(pos.x, pos.y, pos.z);
        ShotLog.capture("ROD", pos.x, pos.y, pos.z, result, "lhc");
        return report(source, result, "ROD");
    }

    private static int measureScope(FabricClientCommandSource source) {
        if (!SurveyState.hasOccupy()) {
            source.sendError(Component.literal("No occupy set. Use /lhc occupy first."));
            return 0;
        }
        HitResult hit = raycast();
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            source.sendError(Component.literal("No target block in sight."));
            return 0;
        }
        Vec3 loc = ((BlockHitResult) hit).getLocation();
        SurveyMath.Result result = SurveyState.ENGINE.scaledResult(loc.x, loc.y, loc.z);
        ShotLog.capture("SCOPE", loc.x, loc.y, loc.z, result, "lhc");
        return report(source, result, "SCOPE");
    }

    /**
     * Client raycast matching the original Scope (player.pick).
     */
    public static HitResult raycast() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return null;
        return player.pick(ClientRaycastRange.current(), 0.0f, false);
    }

    private static int report(FabricClientCommandSource source, SurveyMath.Result result, String kind) {
        Units unit = SurveyState.ENGINE.getUnits();
        String msg = String.format(
                "[%s] Az %s  |  Hz %.2f%s  |  ΔN %.2f  ΔE %.2f  |  Cut/Fill %.2f%s",
                kind,
                result.formatAzimuthDMS(),
                result.horizontal(unit), unit.symbol(),
                result.northing(unit),
                result.easting(unit),
                result.cutFill(unit), unit.symbol()
        );
        source.sendFeedback(Component.literal(msg));
        return 1;
    }

    private static String statusText() {
        Units u = SurveyState.ENGINE.getUnits();
        DistanceScale scale = SurveyState.SCALE;
        String occupy = SurveyState.hasOccupy() ? "set" : "none";
        return String.format(
                "LevelHeaded client — occupy: %s | units: %s (%s) | %s | HUD: %s%n" +
                        "/lhc occupy | rod | scope | reset | hud off|rod|scope | status",
                occupy,
                u.name().toLowerCase(),
                u.symbol(),
                scale.statusLine(),
                ClientSurveyMode.hudMode().name().toLowerCase()
        );
    }
}