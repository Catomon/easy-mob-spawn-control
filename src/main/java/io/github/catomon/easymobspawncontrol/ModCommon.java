package io.github.catomon.easymobspawncontrol;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(ModCommon.MODID)
public class ModCommon {

    public static final String MODID = "easy_mob_spawn_control";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ModCommon(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
    }
}
