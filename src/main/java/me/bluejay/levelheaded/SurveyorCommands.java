package me.bluejay.levelheaded;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class SurveyorCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("lh")
                        .requires(source -> source.hasPermissionLevel(2))

                        .then(CommandManager.literal("help")
                                .executes(SurveyorCommands::showHelp))

                        .then(CommandManager.literal("new")
                                .executes(SurveyorCommands::startNewSession))

                        .then(CommandManager.literal("reset")
                                .executes(SurveyorCommands::resetTransit))

                        .then(CommandManager.literal("status")
                                .executes(SurveyorCommands::showStatus))

                        .then(CommandManager.literal("units")
                                .then(CommandManager.literal("meters")
                                        .executes(ctx -> setUnits(ctx, "METERS")))
                                .then(CommandManager.literal("feet")
                                        .executes(ctx -> setUnits(ctx, "FEET")))
                                .then(CommandManager.literal("chains")
                                        .executes(ctx -> setUnits(ctx, "CHAINS")))
                        )
        );
    }

    private static int showHelp(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        source.sendFeedback(() -> Text.literal("§6=== LevelHeaded Commands ==="), false);
        source.sendFeedback(() -> Text.literal("§e/lh help          §7- Show this help"), false);
        source.sendFeedback(() -> Text.literal("§e/lh new           §7- Start new CSV session"), false);
        source.sendFeedback(() -> Text.literal("§e/lh reset         §7- Clear transit point"), false);
        source.sendFeedback(() -> Text.literal("§e/lh status        §7- Show occupy point"), false);
        source.sendFeedback(() -> Text.literal("§e/lh units <meters|feet|chains>"), false);
        return 1;
    }

    private static int startNewSession(CommandContext<ServerCommandSource> ctx) {
        try {
            Class.forName("me.bluejay.client.data.SurveyPointManager")
                    .getMethod("resetPointCounter").invoke(null);
            ctx.getSource().sendFeedback(() -> Text.literal("§aNew session started."), false);
        } catch (Exception e) {
            ctx.getSource().sendFeedback(() -> Text.literal("§aNew session started."), false);
        }
        return 1;
    }

    private static int resetTransit(CommandContext<ServerCommandSource> ctx) {
        ServerTransitManager.setOccupyPos(ctx.getSource().getServer(), BlockPos.ORIGIN);
        LevelHeaded.broadcastOccupyPos(ctx.getSource().getServer(), BlockPos.ORIGIN);
        ctx.getSource().sendFeedback(() -> Text.literal("§aTransit point cleared."), false);
        return 1;
    }

    private static int showStatus(CommandContext<ServerCommandSource> ctx) {
        BlockPos pos = ServerTransitManager.getOccupyPos(ctx.getSource().getServer());
        ctx.getSource().sendFeedback(() -> Text.literal("§6=== LevelHeaded Status ==="), false);
        ctx.getSource().sendFeedback(() -> Text.literal("§eOccupy: §f" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), false);
        return 1;
    }

    private static int setUnits(CommandContext<ServerCommandSource> ctx, String unitStr) {
        try {
            // Correct target: SurveyPointManager.setCurrentUnit()
            Class<?> managerClass = Class.forName("me.bluejay.client.data.SurveyPointManager");

            // Get the Unit enum from SurveyPoint
            Class<?> surveyPointClass = Class.forName("me.bluejay.client.data.SurveyPoint");
            Class<?> unitEnumClass = surveyPointClass.getDeclaredClasses()[0];

            Object unitEnum = Enum.valueOf((Class<Enum>) unitEnumClass, unitStr.toUpperCase());

            managerClass.getMethod("setCurrentUnit", unitEnumClass)
                    .invoke(null, unitEnum);

            ctx.getSource().sendFeedback(() ->
                    Text.literal("§aSurvey units set to " + unitStr.toLowerCase() + "."), false);
        } catch (Exception e) {
            e.printStackTrace();
            ctx.getSource().sendFeedback(() -> Text.literal("§cFailed to set units. Try again in-game."), false);
        }
        return 1;
    }
}