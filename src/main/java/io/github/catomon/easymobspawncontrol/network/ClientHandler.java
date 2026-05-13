package io.github.catomon.easymobspawncontrol.network;

import io.github.catomon.easymobspawncontrol.gui.SpawnControlScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientHandler {
    public static void handle(ConfigPacket msg) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            minecraft.setScreen(new SpawnControlScreen(
                    msg.spawnRates(),
                    msg.bannedMobs(),
                    msg.spawnCaps(),
                    true
            ));
        });
    }

    public static void handle(MobCountListPacket msg) {
        SpawnControlScreen.setMobCounts(msg.mobCounts());
    }
}