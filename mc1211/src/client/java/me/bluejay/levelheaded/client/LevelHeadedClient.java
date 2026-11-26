package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.ModItems;
import me.bluejay.levelheaded.PointLists;
import me.bluejay.levelheaded.client.screen.DataCollectorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import net.minecraft.item.ItemStack;
import net.minecraft.util.TypedActionResult;

/**
 * Client entry point.
 * Registers the HUD, client commands, keybinds, and the data collector menu.
 */
public class LevelHeadedClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PointLists.setOnChange(JobStore::save);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String id;
            if (client.getServer() != null) {
                id = client.getServer().getSaveProperties().getLevelName();
            } else if (client.getCurrentServerEntry() != null) {
                id = client.getCurrentServerEntry().address;
            } else {
                id = "unknown";
            }
            JobStore.enter(id);
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> JobStore.leave());
        ModKeybinds.register();
        SurveyHud.register();
        LevelHeadedClientCommands.register();

        // Only intercept the collector. Everything else (including the spyglass) passes through.
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.isOf(ModItems.COLLECTOR) && world.isClient()) {
                MinecraftClient.getInstance().setScreen(new DataCollectorScreen());
                return TypedActionResult.success(stack);
            }
            return TypedActionResult.pass(stack);
        });
    }
}