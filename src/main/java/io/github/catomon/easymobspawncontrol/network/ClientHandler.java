package io.github.catomon.easymobspawncontrol.network;

import io.github.catomon.easymobspawncontrol.gui.SpawnControlScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientHandler {
    public static void handle(ConfigPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new SpawnControlScreen(
                    msg.spawnRates, msg.bannedMobs, msg.spawnCaps, true
            ));
        });
    }

    public static void handle(MobCountListPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            SpawnControlScreen.setMobCounts(msg.mobCounts);
        });
    }
}
