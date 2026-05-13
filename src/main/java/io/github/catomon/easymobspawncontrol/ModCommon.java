package io.github.catomon.easymobspawncontrol;

import com.mojang.logging.LogUtils;
import io.github.catomon.easymobspawncontrol.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ModCommon.MODID)
public class ModCommon {

    public static final String MODID = "easy_mob_spawn_control";

    private static final Logger LOGGER = LogUtils.getLogger();

    public ModCommon(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        NetworkHandler.register(event);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }
}