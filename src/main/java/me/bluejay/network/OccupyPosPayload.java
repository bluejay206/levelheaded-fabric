package me.bluejay.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public record OccupyPosPayload(BlockPos pos) implements CustomPayload {

    public static final Id<OccupyPosPayload> ID = new Id<>(Identifier.of("surveyorsays", "occupy_pos"));

    public static final PacketCodec<RegistryByteBuf, OccupyPosPayload> CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC, OccupyPosPayload::pos,
                    OccupyPosPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}