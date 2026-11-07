package me.bluejay.levelheaded.client;

import com.mojang.brigadier.CommandDispatcher;
import me.bluejay.levelheaded.HudSettings;
import me.bluejay.levelheaded.SurveyState;
import me.bluejay.levelheaded.core.data.DistanceScale;
import me.bluejay.levelheaded.core.data.Units;
import me.bluejay.levelheaded.core.math.SurveyMath;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

/**
 * Pure client-side command tree: {@code /lhc ...}
 */
public final class LevelHeadedClientCommands {

    private LevelHeadedClientCommands() {}

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(LevelHeadedClientCommands::register);
    }

    private static void register(CommandDispatcher<FabricClientCommandSource> dispatcher,
                                 CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("lhc")
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(Text.literal(statusText()));
                    return 1;
                })
                .then(occupyBranch())
                .then(ClientCommandManager.literal("rod")
                        .executes(ctx -> measureRod(ctx.getSource())))
                .then(ClientCommandManager.literal("scope")
                        .executes(ctx -> measureScope(ctx.getSource())))
                .then(hudBranch())
                .then(ClientCommandManager.literal("reset")
                        .executes(ctx -> {
                            SurveyState.reset();
                            ShotLog.reset();
                            ctx.getSource().sendFeedback(Text.literal("Occupy cleared. Next V starts a new CSV."));
                            return 1;
                        }))
                .then(ClientCommandManager.literal("status")
                        .executes(ctx -> {
                            ctx.getSource().sendFeedback(Text.literal(statusText()));
                            return 1;
                        })));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> occupyBranch() {
        return ClientCommandManager.literal("occupy")
                .executes(ctx -> setOccupyHere(ctx.getSource()))
                .then(ClientCommandManager.literal("here")
                        .executes(ctx -> setOccupyHere(ctx.getSource())))
                .then(ClientCommandManager.literal("clear")
                        .executes(ctx -> {
                            SurveyState.ENGINE.setOccupy(null);
                            ctx.getSource().sendFeedback(Text.literal("Occupy cleared."));
                            return 1;
                        }));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> hudBranch() {
        return ClientCommandManager.literal("hud")
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(Text.literal(
                            "HUD mode: " + ClientSurveyMode.hudMode().name().toLowerCase()
                                    + " | " + HudSettings.statusLine()));
                    return 1;
                })
                .then(angleBranch())
                .then(ClientCommandManager.literal("off")
                        .executes(ctx -> {
                            ClientSurveyMode.setHudMode(ClientSurveyMode.HudMode.OFF);
                            ctx.getSource().sendFeedback(Text.literal("Client HUD off."));
                            return 1;
                        }))
                .then(ClientCommandManager.literal("rod")
                        .executes(ctx -> {
                            ClientSurveyMode.setHudMode(ClientSurveyMode.HudMode.ROD);
                            ctx.getSource().sendFeedback(Text.literal(
                                    "Client HUD: ROD (player position vs occupy)."));
                            return 1;
                        }))
                .then(ClientCommandManager.literal("scope")
                        .executes(ctx -> {
                            ClientSurveyMode.setHudMode(ClientSurveyMode.HudMode.SCOPE);
                            ctx.getSource().sendFeedback(Text.literal(
                                    "Client HUD: SCOPE (raycast target vs occupy)."));
                            return 1;
                        }));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<FabricClientCommandSource> angleBranch() {
        return ClientCommandManager.literal("angle")
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(Text.literal(HudSettings.statusLine()));
                    return 1;
                })
                .then(ClientCommandManager.literal("azimuth")
                        .executes(ctx -> {
                            HudSettings.setAngleMode(HudSettings.AngleMode.AZIMUTH);
                            ctx.getSource().sendFeedback(Text.literal("HUD angle: azimuth"));
                            return 1;
                        }))
                .then(ClientCommandManager.literal("bearing")
                        .executes(ctx -> {
                            HudSettings.setAngleMode(HudSettings.AngleMode.BEARING);
                            ctx.getSource().sendFeedback(Text.literal("HUD angle: quadrant bearing"));
                            return 1;
                        }))
                .then(ClientCommandManager.literal("toggle")
                        .executes(ctx -> {
                            HudSettings.AngleMode mode = HudSettings.toggleAngle();
                            ctx.getSource().sendFeedback(Text.literal(
                                    "HUD angle: " + mode.name().toLowerCase()));
                            return 1;
                        }));
    }

    private static int setOccupyHere(FabricClientCommandSource source) {
        ClientPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("No local player."));
            return 0;
        }
        Vec3d pos = player.getPos();
        SurveyState.setOccupy(pos.x, pos.y, pos.z, "client");
        source.sendFeedback(Text.literal(String.format(
                "Occupy set (client) to %.2f, %.2f, %.2f", pos.x, pos.y, pos.z)));
        return 1;
    }

    private static int measureRod(FabricClientCommandSource source) {
        ClientPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("No local player."));
            return 0;
        }
        if (!SurveyState.hasOccupy()) {
            source.sendError(Text.literal("No occupy set. Use /lhc occupy first."));
            return 0;
        }
        Vec3d pos = player.getPos();
        SurveyMath.Result result = SurveyState.ENGINE.scaledResult(pos.x, pos.y, pos.z);
        // ShotLog.capture("ROD", pos.x, pos.y, pos.z, result, "lhc");
        return report(source, result, "ROD");
    }

    private static int measureScope(FabricClientCommandSource source) {
        if (!SurveyState.hasOccupy()) {
            source.sendError(Text.literal("No occupy set. Use /lhc occupy first."));
            return 0;
        }
        HitResult hit = raycast();
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            source.sendError(Text.literal("No target block in sight."));
            return 0;
        }
        Vec3d loc = ((BlockHitResult) hit).getPos();
        SurveyMath.Result result = SurveyState.ENGINE.scaledResult(loc.x, loc.y, loc.z);
        // ShotLog.capture("SCOPE", loc.x, loc.y, loc.z, result, "lhc");
        return report(source, result, "SCOPE");
    }

    public static HitResult raycast() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) return null;
        return player.raycast(ClientRaycastRange.current(), 0.0f, false);
    }

    private static int report(FabricClientCommandSource source, SurveyMath.Result result, String kind) {
        if (result == null) {
            source.sendError(Text.literal("No occupy set."));
            return 0;
        }
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
        source.sendFeedback(Text.literal(msg));
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