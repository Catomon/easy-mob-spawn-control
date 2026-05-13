package io.github.catomon.easymobspawncontrol;

import io.github.catomon.easymobspawncontrol.gui.SpawnControlScreen;
import io.github.catomon.easymobspawncontrol.network.ConfigRequestPacket;
import io.github.catomon.easymobspawncontrol.network.NetworkHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ModCommon.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, screen) -> new SpawnControlScreen())
        );
    }

    public static void tryOpenControlGui(boolean reasonCommand) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && (reasonCommand || mc.screen == null)) {
            if (!mc.hasSingleplayerServer()) {
                if (mc.getConnection() != null) {
                    NetworkHandler.INSTANCE.sendToServer(new ConfigRequestPacket());
                }
            } else {
                mc.setScreen(new SpawnControlScreen());
            }
        }
    }
}
