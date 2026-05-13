package io.github.catomon.easymobspawncontrol.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class Util {
    public static boolean isNotGameMaster(ServerPlayer sender, MinecraftServer server) {
        PlayerList playerList = server.getPlayerList();
        int permLevel = playerList.getServer().getProfilePermissions(sender.getGameProfile());

        if (permLevel < 2) {
            return true;
        } else {
            return false;
        }
    }
}
