package me.bluejay.levelheaded;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemGroups;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import me.bluejay.levelheaded.network.OccupyPosPayload;

public class LevelHeaded implements ModInitializer {

    public static final String MOD_ID = "levelheaded";

    @Override
    public void onInitialize() {
        System.out.println("[LevelHeaded] Initializing...");

        ModItems.register();
        TransitRegistry.register();

        // Assign items to vanilla creative tabs
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
                .register(entries -> entries.add(TransitRegistry.TRANSIT_BLOCK_ITEM));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register(entries -> {
                    entries.add(ModItems.ROD);
                    entries.add(ModItems.SCOPE2);        // Only Scope2 now
                    entries.add(TransitRegistry.TRANSIT_BLOCK_ITEM);
                });

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            SurveyorCommands.register(dispatcher);
        });

        // Register server -> client payload for occupy position sync
        PayloadTypeRegistry.playS2C().register(OccupyPosPayload.ID, OccupyPosPayload.CODEC);

        System.out.println("[LevelHeaded] Initialized successfully.");
    }

    // In broadcastOccupyPos(...)
    public static void broadcastOccupyPos(MinecraftServer server, BlockPos pos) {
        if (server == null) return;
        OccupyPosPayload payload = new OccupyPosPayload(pos);
        for (var player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
        System.out.println("[LevelHeaded] Broadcasted occupy pos: " + pos);
    }

    public static void updateClientTransitCache(BlockPos pos) {
        if (pos == null) return;
        try {
            Class<?> cacheClass = Class.forName("me.bluejay.client.ClientTransitCache");
            cacheClass.getMethod("setOccupyPos", net.minecraft.util.math.BlockPos.class)
                    .invoke(null, pos);
            System.out.println("[LevelHeaded] Client cache updated via bridge → " + pos);
        } catch (Exception ignored) {
            // Not on client or class not found
        }
    }
}