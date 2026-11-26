package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.ModItems;
import me.bluejay.levelheaded.PointLists;
import me.bluejay.levelheaded.SurveyState;
import me.bluejay.levelheaded.block.TransitBlock;
import me.bluejay.levelheaded.client.screen.DataCollectorScreen;
import me.bluejay.levelheaded.core.data.ControlPoint;
import me.bluejay.levelheaded.core.data.PointKind;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

/**
 * Client entry point.
 * Registers the HUD, client commands, keybinds, the data collector menu,
 * and the client-side transit occupy that fills the point list.
 */
public final class LevelHeadedClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PointLists.setOnChange(JobStore::save);


        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String id;
            if (client.getSingleplayerServer() != null) {
                id = client.getSingleplayerServer().getWorldData().getLevelName();
            } else if (client.getCurrentServer() != null) {
                id = client.getCurrentServer().ip;
            } else {
                id = "unknown";
            }
            JobStore.enter(id);
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> JobStore.leave());
        SurveyHud.register();
        LevelHeadedClientCommands.register();
        ModKeybinds.register();

        // Only intercept the collector. Everything else (including the spyglass) passes through.
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (player.getItemInHand(hand).is(ModItems.COLLECTOR) && world.isClientSide()) {
                Minecraft.getInstance().gui.setScreen(new DataCollectorScreen());
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });

        // 26.x useWithoutItem runs on the server. Write the transit point here, on the client.
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (!world.isClientSide()) return InteractionResult.PASS;
            if (!(world.getBlockState(hit.getBlockPos()).getBlock() instanceof TransitBlock)) {
                return InteractionResult.PASS;
            }
            BlockPos pos = hit.getBlockPos();
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5;
            String id = PointLists.STORE.nextId();
            String desc = "TRANSIT#" + id;
            PointLists.STORE.put(new ControlPoint(
                    id, PointKind.CONTROL, -z, x, y, desc));
            SurveyState.setOccupy(x, y, z, desc);
            JobStore.save();
            if (player != null) {
                player.sendSystemMessage(Component.literal(
                        String.format("Occupied %s  %.2f, %.2f, %.2f", desc, x, y, z)));
            }
            return InteractionResult.PASS;
        });
    }
}