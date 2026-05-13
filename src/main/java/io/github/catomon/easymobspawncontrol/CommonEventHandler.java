package io.github.catomon.easymobspawncontrol;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber
public class CommonEventHandler {
    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getType().equals(ModConfig.Type.COMMON)) {
            CommonConfig.reload();
        }
    }
}
