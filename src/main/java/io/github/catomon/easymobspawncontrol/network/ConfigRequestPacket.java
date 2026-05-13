package io.github.catomon.easymobspawncontrol.network;

import io.github.catomon.easymobspawncontrol.CommonConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

import static io.github.catomon.easymobspawncontrol.network.Util.isNotGameMaster;

// Client-to-Server packet requesting config from server
public class ConfigRequestPacket {
    public ConfigRequestPacket() {

    }

    public ConfigRequestPacket(FriendlyByteBuf buf) {

    }

    public void encode(FriendlyByteBuf buf) {

    }

    public static void handle(ConfigRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();

        if (!context.getDirection().getReceptionSide().isServer()) {
            context.setPacketHandled(false);
            return;
        }

        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null) return;

            if (isNotGameMaster(sender, sender.server)) {
                sender.sendSystemMessage(Component.translatable("easy_mob_spawn_control.sys_msg.no_permission"));
                return;
            }

            ConfigPacket configPacket = new ConfigPacket(
                    CommonConfig.spawnRates, CommonConfig.spawnCaps, CommonConfig.bannedMobs
            );
            NetworkHandler.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> sender),
                    configPacket
            );
        });

        context.setPacketHandled(true);
    }
}