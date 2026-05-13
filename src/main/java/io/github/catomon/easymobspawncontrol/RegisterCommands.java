package io.github.catomon.easymobspawncontrol;

import com.mojang.brigadier.Command;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

import static net.minecraft.commands.Commands.literal;

@EventBusSubscriber(Dist.CLIENT)
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
