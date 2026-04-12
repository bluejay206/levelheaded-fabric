package me.bluejay;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

public class ServerTransitManager {

    private static final String STATE_ID = "surveyorsays_transit";

    public static class TransitState extends PersistentState {
        public BlockPos occupyPos = BlockPos.ORIGIN;

        @Override
        public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
            nbt.putIntArray("occupy", new int[]{
                    occupyPos.getX(),
                    occupyPos.getY(),
                    occupyPos.getZ()
            });
            return nbt;
        }

        public static TransitState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
            TransitState state = new TransitState();
            if (nbt.contains("occupy")) {
                int[] pos = nbt.getIntArray("occupy");
                if (pos.length == 3) {
                    state.occupyPos = new BlockPos(pos[0], pos[1], pos[2]);
                }
            }
            return state;
        }

        public static TransitState get(MinecraftServer server) {
            PersistentStateManager manager = server.getOverworld().getPersistentStateManager();
            return manager.getOrCreate(
                    new PersistentState.Type<>(
                            TransitState::new,
                            TransitState::fromNbt,
                            null
                    ),
                    STATE_ID
            );
        }
    }

    public static void register() {
        // No extra registration needed
    }

    public static void setOccupyPos(MinecraftServer server, BlockPos pos) {
        if (server == null) return;
        TransitState state = TransitState.get(server);
        state.occupyPos = (pos != null) ? pos : BlockPos.ORIGIN;
        state.markDirty();
    }

    public static BlockPos getOccupyPos(MinecraftServer server) {
        if (server == null) return BlockPos.ORIGIN;
        return TransitState.get(server).occupyPos;
    }

    /**
     * Client-safe accessor for HUDs.
     * Returns ORIGIN when no integrated server is running (e.g. multiplayer without sync).
     */
    /**
     * Client-safe accessor (fallback). The payload system is now the primary sync method.
     */
    public static BlockPos getClientOccupyPos() {
        return BlockPos.ORIGIN;   // Payload system handles real sync
    }
}