package io.github.catomon.easymobspawncontrol.network;

import io.github.catomon.easymobspawncontrol.ModCommon;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.function.Supplier;

import static io.github.catomon.easymobspawncontrol.network.Util.isNotGameMaster;

// Client-to-Server packet requesting mob count from server
public class MobCountListRequest {
    public MobCountListRequest() {

    }

    public MobCountListRequest(FriendlyByteBuf buf) {

    }

    public void encode(FriendlyByteBuf buf) {

    }

    public static void handle(MobCountListRequest msg, Supplier<NetworkEvent.Context> ctx) {
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

            try {
                var serverLevel = sender.serverLevel();
                HashMap<String, Integer> mobs = new HashMap<>();
                for (Entity entity : serverLevel.getEntities().getAll()) {
                    ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                    if (id == null) continue;

                    String key = id.toString();
                    mobs.merge(key, 1, Integer::sum);
                }

                MobCountListPacket configPacket = new MobCountListPacket(
                        mobs
                );
                NetworkHandler.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> sender),
                        configPacket
                );
            } catch (Exception e) {
                System.err.println(ModCommon.MODID + ": failed to update IN_WORLD mob list.");
                e.printStackTrace();
            }
        });

        context.setPacketHandled(true);
    }
}