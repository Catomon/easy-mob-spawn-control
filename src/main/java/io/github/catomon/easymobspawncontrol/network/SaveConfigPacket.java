package io.github.catomon.easymobspawncontrol.network;

import io.github.catomon.easymobspawncontrol.CommonConfig;
import io.github.catomon.easymobspawncontrol.ModCommon;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.catomon.easymobspawncontrol.network.Util.isNotGameMaster;

public record SaveConfigPacket(
        Map<String, Double> spawnRates,
        Map<String, Integer> spawnCaps,
        List<String> bannedMobs
) implements CustomPacketPayload {

    public static final Type<SaveConfigPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModCommon.MODID, "save_config"));

    public static final StreamCodec<ByteBuf, SaveConfigPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.DOUBLE),
                    SaveConfigPacket::spawnRates,

                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT),
                    SaveConfigPacket::spawnCaps,

                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
                    SaveConfigPacket::bannedMobs,

                    SaveConfigPacket::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
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

            // Update server config data from client
            CommonConfig.spawnRates.clear();
            CommonConfig.spawnRates.putAll(spawnRates);

            CommonConfig.spawnCaps.clear();
            CommonConfig.spawnCaps.putAll(spawnCaps);

            CommonConfig.bannedMobs.clear();
            CommonConfig.bannedMobs.addAll(bannedMobs);

            // Save to config file
            CommonConfig.saveSpawnRates(spawnRates);
            CommonConfig.saveSpawnCaps(spawnCaps);
            CommonConfig.saveBannedMobs(bannedMobs);

            // Reload for immediate effect
            CommonConfig.reload();

            // Remove already‑spawned banned mobs
            server.executeBlocking(() -> {
                for (ServerLevel serverLevel : server.getAllLevels()) {
                    List<Entity> bannedEntities = new ArrayList<>();
                    for (Entity entity : serverLevel.getEntities().getAll()) {
                        var key = EntityType.getKey(entity.getType());
                        if (key != null && CommonConfig.bannedMobs.contains(key.toString())) {
                            bannedEntities.add(entity);
                        }
                    }
                    for (Entity entity : bannedEntities) {
                        entity.remove(Entity.RemovalReason.DISCARDED);
                    }
                }
            });

            sender.sendSystemMessage(Component.translatable("easy_mob_spawn_control.sys_msg.server_config_updated"));
        });
    }
}