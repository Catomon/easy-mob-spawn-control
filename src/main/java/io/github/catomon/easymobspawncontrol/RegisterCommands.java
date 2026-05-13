package io.github.catomon.easymobspawncontrol;

import com.mojang.brigadier.Command;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraft.commands.Commands.literal;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class RegisterCommands {
    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                literal("easymobspawncontrol")
                        .then(literal("gui")
                                .executes(context -> {
                                    Minecraft.getInstance().execute(() -> {
                                        ModClient.tryOpenControlGui(true);
                                    });
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
        );
    }
}
