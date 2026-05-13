package io.github.catomon.easymobspawncontrol;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(value = Dist.CLIENT)
public class Keybindings {

    public static final Lazy<KeyMapping> INFO_SCR_KEY = Lazy.of(() -> new KeyMapping(
            "key.easy_mob_spawn_control.openmobcrontrolscreen",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            "key.categories.easy_mob_spawn_control"
    ));

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(INFO_SCR_KEY.get());
    }
}

@EventBusSubscriber(value = Dist.CLIENT)
class KeyInputHandler {
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        KeyMapping key = Keybindings.INFO_SCR_KEY.get();
        if (key.isDown()) {
            ModClient.tryOpenControlGui(false);
        }
    }
}
