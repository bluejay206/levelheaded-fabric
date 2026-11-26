package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.ModItems;
import me.bluejay.levelheaded.PointLists;
import me.bluejay.levelheaded.client.screen.DataCollectorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.TypedActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelHeadedClient implements ClientModInitializer {
    private static final Logger LOG = LoggerFactory.getLogger("levelheaded");

    @Override
    public void onInitializeClient() {
        LOG.info("LevelHeaded client initializing");
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

        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.isOf(ModItems.COLLECTOR) && world.isClient()) {
                MinecraftClient.getInstance().setScreen(new DataCollectorScreen());
                return TypedActionResult.success(stack);
            }
            return TypedActionResult.pass(stack);
        });
        LOG.info("LevelHeaded client ready");
    }
}
