package io.github.catomon.easymobspawncontrol.network;

import io.github.catomon.easymobspawncontrol.CommonConfig;
import io.github.catomon.easymobspawncontrol.ModCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static io.github.catomon.easymobspawncontrol.network.Util.isNotGameMaster;

public record ConfigRequestPacket() implements CustomPacketPayload {

    public static final Type<ConfigRequestPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModCommon.MODID, "config_request"));

    public static final StreamCodec<ByteBuf, ConfigRequestPacket> STREAM_CODEC =
            StreamCodec.unit(new ConfigRequestPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        if (!context.flow().isServerbound()) {
            return;
        }

        context.enqueueWork(() -> {
            ServerPlayer sender = (ServerPlayer) context.player();

            MinecraftServer server = sender.getServer();
            if (server == null) return;

            if (isNotGameMaster(sender, sender.level().getServer())) {
                sender.sendSystemMessage(Component.translatable("easy_mob_spawn_control.sys_msg.no_permission"));
                return;
            }

            ConfigPacket configPacket = new ConfigPacket(
                    CommonConfig.spawnRates, CommonConfig.spawnCaps, CommonConfig.bannedMobs
            );
            context.reply(configPacket);
        });
    }
}