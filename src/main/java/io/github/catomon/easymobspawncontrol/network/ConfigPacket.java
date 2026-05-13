package io.github.catomon.easymobspawncontrol.network;

import io.github.catomon.easymobspawncontrol.ModCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ConfigPacket(
        Map<String, Double> spawnRates,
        Map<String, Integer> spawnCaps,
        List<String> bannedMobs
) implements CustomPacketPayload {
    public static final Type<ConfigPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModCommon.MODID, "config"));

    public static final StreamCodec<ByteBuf, ConfigPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.DOUBLE),
            ConfigPacket::spawnRates,

            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT),
            ConfigPacket::spawnCaps,

            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
            ConfigPacket::bannedMobs,

            ConfigPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        if (!context.flow().isClientbound()) {
            return;
        }

        context.enqueueWork(() -> {
            ClientHandler.handle(this);
        });
    }
}