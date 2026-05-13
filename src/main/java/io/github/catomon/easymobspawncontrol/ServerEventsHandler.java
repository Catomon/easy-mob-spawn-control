package io.github.catomon.easymobspawncontrol;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

@EventBusSubscriber
public class ServerEventsHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        EntityType<?> type = entity.getType();
        var id = EntityType.getKey(type);
        if (id == null) return;
        String idString = id.toString();

        if (CommonConfig.bannedMobs.contains(idString)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMobFinalizeSpawnEvent(FinalizeSpawnEvent event) {
        Entity entity = event.getEntity();
        Level level = event.getLevel().getLevel();
        if (level.isClientSide()) return;

        EntityType<?> type = entity.getType();
        var id = EntityType.getKey(type);
        if (id == null) return;
        String idString = id.toString();

        if (event.getSpawnType() == MobSpawnType.NATURAL) {
            double spawnRate = CommonConfig.getSpawnRate(idString);

            if (spawnRate <= 0.0) {
                event.setSpawnCancelled(true);
            } else if (spawnRate < 1.0) {
                if (level.random.nextDouble() > spawnRate) {
                    event.setSpawnCancelled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        Field mobCategoryMaxField = null;
        try {
            mobCategoryMaxField = ObfuscationReflectionHelper.findField(MobCategory.class, "max");
            mobCategoryMaxField.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("Failed to access MobCategory.max field", e);
        }

        for (MobCategory category : MobCategory.values()) {
            if (category == MobCategory.MISC) continue;

            String catName = category.getName().toLowerCase();
            int vanillaCap = category.getMaxInstancesPerChunk();
            int configuredCap = CommonConfig.spawnCaps.getOrDefault(catName, vanillaCap);

            if (configuredCap < 0 || configuredCap > 999 || configuredCap == vanillaCap) {
                continue;
            }

            try {
                mobCategoryMaxField.set(category, configuredCap);
            } catch (Exception e) {
                LOGGER.error("Failed to set cap for {}: {}", catName, e.getMessage());
            }
        }
    }
}
