package me.bluejay;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class SurveyorCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("ss")
                        .requires(source -> source.hasPermissionLevel(2))

                        .then(CommandManager.literal("help")
                                .executes(SurveyorCommands::showHelp))

                        .then(CommandManager.literal("new")
                                .executes(ctx -> {
                                    try {
                                        Class.forName("me.bluejay.client.data.SurveyPointManager")
                                                .getMethod("startNewSession").invoke(null);
                                        ctx.getSource().sendFeedback(() -> Text.literal("§aNew session started."), false);
                                    } catch (Exception ignored) {}
                                    return 1;
                                }))

                        .then(CommandManager.literal("reset")
                                .executes(SurveyorCommands::resetTransit))

                        .then(CommandManager.literal("status")
                                .executes(SurveyorCommands::showStatus))

                        .then(CommandManager.literal("list")
                                .executes(ctx -> listShots(ctx, 10))
                                .then(CommandManager.argument("count", IntegerArgumentType.integer(1, 50))
                                        .executes(ctx -> listShots(ctx, IntegerArgumentType.getInteger(ctx, "count")))))

                        .then(CommandManager.literal("clear")
                                .executes(SurveyorCommands::clearShots))

                        .then(CommandManager.literal("save")
                                .executes(SurveyorCommands::manualSave))

                        .then(CommandManager.literal("tp")
                                .executes(SurveyorCommands::teleportToTransit))
        );
    }

    private static int showHelp(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        source.sendFeedback(() -> Text.literal("§6=== SurveyorSays Commands ==="), false);
        source.sendFeedback(() -> Text.literal("§e/ss help          §7- Show this help"), false);
        source.sendFeedback(() -> Text.literal("§e/ss new           §7- Start new CSV session"), false);
        source.sendFeedback(() -> Text.literal("§e/ss reset         §7- Clear transit point"), false);
        source.sendFeedback(() -> Text.literal("§e/ss status        §7- Show occupy point"), false);
        source.sendFeedback(() -> Text.literal("§e/ss list [n]      §7- Show last N shots"), false);
        source.sendFeedback(() -> Text.literal("§e/ss clear         §7- Clear session shots"), false);
        source.sendFeedback(() -> Text.literal("§e/ss save          §7- Reminder: use V in SHOT mode"), false);
        source.sendFeedback(() -> Text.literal("§e/ss tp            §7- Teleport is client-side"), false);
        return 1;
    }

    private static int resetTransit(CommandContext<ServerCommandSource> ctx) {
        ServerTransitManager.setOccupyPos(ctx.getSource().getServer(), BlockPos.ORIGIN);
        SurveyorSays.broadcastOccupyPos(ctx.getSource().getServer(), BlockPos.ORIGIN);
        ctx.getSource().sendFeedback(() -> Text.literal("§aTransit point cleared."), false);
        return 1;
    }

    private static int showStatus(CommandContext<ServerCommandSource> ctx) {
        BlockPos pos = ServerTransitManager.getOccupyPos(ctx.getSource().getServer());
        ctx.getSource().sendFeedback(() -> Text.literal("§6=== SurveyorSays Status ==="), false);
        ctx.getSource().sendFeedback(() -> Text.literal("§eOccupy: §f" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), false);
        return 1;
    }

    private static int listShots(CommandContext<ServerCommandSource> ctx, int count) {
        ctx.getSource().sendFeedback(() -> Text.literal("§6=== Last " + count + " shots ==="), false);
        try {
            Class<?> managerClass = Class.forName("me.bluejay.client.data.SurveyPointManager");
            Object recent = managerClass.getMethod("getRecentShots", int.class).invoke(null, count);
            java.util.List<?> shots = (java.util.List<?>) recent;

            if (shots.isEmpty()) {
                ctx.getSource().sendFeedback(() -> Text.literal("§7No shots saved yet."), false);
                return 1;
            }

            for (Object shotObj : shots) {
                ctx.getSource().sendFeedback(() -> Text.literal(shotObj.toString()), false);
            }
        } catch (Exception e) {
            ctx.getSource().sendFeedback(() -> Text.literal("§7Shot list available in client HUD."), false);
        }
        return 1;
    }

    private static int clearShots(CommandContext<ServerCommandSource> ctx) {
        try {
            Class.forName("me.bluejay.client.data.SurveyPointManager").getMethod("clear").invoke(null);
            ctx.getSource().sendFeedback(() -> Text.literal("§aSession shots cleared."), false);
        } catch (Exception ignored) {
            ctx.getSource().sendFeedback(() -> Text.literal("§7Clear performed on client."), false);
        }
        return 1;
    }

    private static int manualSave(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(() -> Text.literal("§eUse V key while in SHOT mode."), false);
        return 1;
    }

    private static int teleportToTransit(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(() -> Text.literal("§eTeleport is client-side only."), false);
        return 1;
    }
}