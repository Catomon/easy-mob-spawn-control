package io.github.catomon.easymobspawncontrol;

import io.github.catomon.easymobspawncontrol.gui.SpawnControlScreen;
import io.github.catomon.easymobspawncontrol.network.ConfigRequestPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod(value = ModCommon.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ModCommon.MODID, value = Dist.CLIENT)
public class ModClient {
    public ModClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, (modContainer, screen) -> new SpawnControlScreen());
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {

    }

    public static void tryOpenControlGui(boolean reasonCommand) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && (reasonCommand || mc.screen == null)) {
            if (!mc.hasSingleplayerServer()) {
                if (mc.getConnection() != null) {
                    PacketDistributor.sendToServer(new ConfigRequestPacket());
                }
            } else {
                mc.setScreen(new SpawnControlScreen());
            }
        }
    }
}
