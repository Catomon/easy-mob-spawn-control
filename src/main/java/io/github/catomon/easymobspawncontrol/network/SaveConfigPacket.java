package io.github.catomon.easymobspawncontrol.network;

import io.github.catomon.easymobspawncontrol.CommonConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static io.github.catomon.easymobspawncontrol.network.Util.isNotGameMaster;

// Client-to-Server packet requesting to save updated config on server
public class SaveConfigPacket {
    private final Map<String, Double> spawnRates;
    private final Map<String, Integer> spawnCaps;
    private final List<String> bannedMobs;

    public SaveConfigPacket(Map<String, Double> spawnRates, Map<String, Integer> spawnCaps, List<String> bannedMobs) {
        this.spawnRates = new HashMap<>(spawnRates);
        this.spawnCaps = new HashMap<>(spawnCaps);
        this.bannedMobs = new ArrayList<>(bannedMobs);
    }

    public SaveConfigPacket(FriendlyByteBuf buf) {
        this.spawnRates = buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readDouble);
        this.spawnCaps = buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readInt);
        this.bannedMobs = buf.readList(FriendlyByteBuf::readUtf);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(this.spawnRates, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeDouble);
        buf.writeMap(this.spawnCaps, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeInt);
        buf.writeCollection(this.bannedMobs, FriendlyByteBuf::writeUtf);
    }

    public static void handle(SaveConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
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

            // Update server config data from client
            CommonConfig.spawnRates.clear();
            CommonConfig.spawnRates.putAll(msg.spawnRates);

            CommonConfig.spawnCaps.clear();
            CommonConfig.spawnCaps.putAll(msg.spawnCaps);

            CommonConfig.bannedMobs.clear();
            CommonConfig.bannedMobs.addAll(msg.bannedMobs);

            // Save to Forge config file
            CommonConfig.saveSpawnRates(msg.spawnRates);
            CommonConfig.saveSpawnCaps(msg.spawnCaps);
            CommonConfig.saveBannedMobs(msg.bannedMobs);

            // Reload for immediate effect
            CommonConfig.reload();

            context.getSender().server.executeBlocking(() -> {
                for (ServerLevel serverLevel :  context.getSender().server.getAllLevels()) {
                    List<Entity> bannedEntities = new ArrayList<>();
                    for (Entity entity : serverLevel.getEntities().getAll()) {
                        var key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
                        if (key != null) {
                            String id = key.toString();
                            if (CommonConfig.bannedMobs.contains(id)) {
                                bannedEntities.add(entity);
                            }
                        }
                    }
                    for (Entity entity : bannedEntities) {
                        entity.remove(Entity.RemovalReason.DISCARDED);
                    }
                }
            });

            sender.sendSystemMessage(Component.translatable("easy_mob_spawn_control.sys_msg.server_config_updated"));
        });

        context.setPacketHandled(true);
    }
}