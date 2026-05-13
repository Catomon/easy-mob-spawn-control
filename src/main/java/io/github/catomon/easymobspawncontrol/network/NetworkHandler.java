package io.github.catomon.easymobspawncontrol.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION)
                .optional()
                .executesOn(HandlerThread.NETWORK);

        // Server → Client (Clientbound)
        registrar.playToClient(
                MobCountListPacket.TYPE,
                MobCountListPacket.STREAM_CODEC,
                MobCountListPacket::handle
        );

        registrar.playToClient(
                ConfigPacket.TYPE,
                ConfigPacket.STREAM_CODEC,
                ConfigPacket::handle
        );

        // Client → Server (Serverbound)
        registrar.playToServer(
                ConfigRequestPacket.TYPE,
                ConfigRequestPacket.STREAM_CODEC,
                ConfigRequestPacket::handle
        );

        registrar.playToServer(
                SaveConfigPacket.TYPE,
                SaveConfigPacket.STREAM_CODEC,
                SaveConfigPacket::handle
        );

        registrar.playToServer(
                MobCountListRequest.TYPE,
                MobCountListRequest.STREAM_CODEC,
                MobCountListRequest::handle
        );
    }
}