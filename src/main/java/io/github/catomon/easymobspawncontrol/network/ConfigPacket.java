package io.github.catomon.easymobspawncontrol.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

// Server-to-Client packet sending current config
public class ConfigPacket {
    public final Map<String, Double> spawnRates;
    public final Map<String, Integer> spawnCaps;
    public final List<String> bannedMobs;

    public ConfigPacket(Map<String, Double> spawnRates, Map<String, Integer> spawnCaps, List<String> bannedMobs) {
        this.spawnRates = new HashMap<>(spawnRates);
        this.spawnCaps = new HashMap<>(spawnCaps);
        this.bannedMobs = new ArrayList<>(bannedMobs);
    }

    public ConfigPacket(FriendlyByteBuf buf) {
        this.spawnRates = buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readDouble);
        this.spawnCaps = buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readInt);
        this.bannedMobs = buf.readList(FriendlyByteBuf::readUtf);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(this.spawnRates, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeDouble);
        buf.writeMap(this.spawnCaps, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeInt);
        buf.writeCollection(this.bannedMobs, FriendlyByteBuf::writeUtf);
    }

    public static void handle(ConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();

        if (!context.getDirection().getReceptionSide().isClient()) {
            context.setPacketHandled(false);
            return;
        }

        ClientHandler.handle(msg, ctx);

        context.setPacketHandled(true);
    }
}