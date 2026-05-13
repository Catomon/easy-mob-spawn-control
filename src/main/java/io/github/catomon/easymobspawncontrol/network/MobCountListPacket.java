package io.github.catomon.easymobspawncontrol.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

// Server-to-client mob count packet
public class MobCountListPacket {
    public final Map<String, Integer> mobCounts;

    public MobCountListPacket(Map<String, Integer> mobCounts) {
        this.mobCounts = new HashMap<>(mobCounts);
    }

    public MobCountListPacket(FriendlyByteBuf buf) {
        this.mobCounts = buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readInt);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(this.mobCounts, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeInt);
    }

    public static void handle(MobCountListPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();

        if (!context.getDirection().getReceptionSide().isClient()) {
            context.setPacketHandled(false);
            return;
        }

        ClientHandler.handle(msg, ctx);

        context.setPacketHandled(true);
    }
}