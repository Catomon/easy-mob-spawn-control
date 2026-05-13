package io.github.catomon.easymobspawncontrol;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.catomon.easymobspawncontrol.gui.SpawnControlScreen;
import io.github.catomon.easymobspawncontrol.network.ConfigRequestPacket;
import io.github.catomon.easymobspawncontrol.network.NetworkHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
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

@Mod.EventBusSubscriber(value = Dist.CLIENT)
class KeyInputHandler {
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        KeyMapping key = Keybindings.INFO_SCR_KEY.get();

        if (event.getKey() == key.getKey().getValue()) {
            if (event.getAction() == GLFW.GLFW_PRESS) {
                ModClient.tryOpenControlGui(false);
            }
        }
    }
}