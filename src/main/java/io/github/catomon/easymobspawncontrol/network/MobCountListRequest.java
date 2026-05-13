package io.github.catomon.easymobspawncontrol.network;

import io.github.catomon.easymobspawncontrol.ModCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.HashMap;

import static io.github.catomon.easymobspawncontrol.network.Util.isNotGameMaster;

public record MobCountListRequest() implements CustomPacketPayload {

    public static final Type<MobCountListRequest> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModCommon.MODID, "mob_count_list_request"));

    public static final StreamCodec<ByteBuf, MobCountListRequest> STREAM_CODEC =
            StreamCodec.unit(new MobCountListRequest());

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

            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return;

            if (isNotGameMaster(sender, sender.level().getServer())) {
                sender.sendSystemMessage(Component.translatable("easy_mob_spawn_control.sys_msg.no_permission"));
                return;
            }

            var serverLevel = sender.serverLevel();
            HashMap<String, Integer> mobs = new HashMap<>();
            for (Entity entity : serverLevel.getAllEntities()) {
                ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                if (id == null) continue;

                String key = id.toString();
                mobs.merge(key, 1, Integer::sum);
            }

            MobCountListPacket configPacket = new MobCountListPacket(mobs);
            context.reply(configPacket);
        });
    }
}