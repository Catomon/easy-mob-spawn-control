package io.github.catomon.easymobspawncontrol.network;

import io.github.catomon.easymobspawncontrol.ModCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record MobCountListPacket(
        Map<String, Integer> mobCounts
) implements CustomPacketPayload {

    public static final Type<MobCountListPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModCommon.MODID, "mob_count_list"));

    public static final StreamCodec<ByteBuf, MobCountListPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT),
                    MobCountListPacket::mobCounts,
                    MobCountListPacket::new
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