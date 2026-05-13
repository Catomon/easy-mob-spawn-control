package io.github.catomon.easymobspawncontrol.network;

import io.github.catomon.easymobspawncontrol.ModCommon;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Optional;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ModCommon.MODID, "main"),
            () -> PROTOCOL_VERSION,
            (it) -> true,
            (it) -> true
    );

    private static int id = 0;

    public static void register(FMLCommonSetupEvent event) {
        INSTANCE.registerMessage(id++,
                MobCountListRequest.class,
                MobCountListRequest::encode,
                MobCountListRequest::new,
                MobCountListRequest::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        INSTANCE.registerMessage(id++,
                ConfigRequestPacket.class,
                ConfigRequestPacket::encode,
                ConfigRequestPacket::new,
                ConfigRequestPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        INSTANCE.registerMessage(id++,
                SaveConfigPacket.class,
                SaveConfigPacket::encode,
                SaveConfigPacket::new,
                SaveConfigPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));

        INSTANCE.registerMessage(id++,
                ConfigPacket.class,
                ConfigPacket::encode,
                ConfigPacket::new,
                ConfigPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        INSTANCE.registerMessage(id++,
                MobCountListPacket.class,
                MobCountListPacket::encode,
                MobCountListPacket::new,
                MobCountListPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}