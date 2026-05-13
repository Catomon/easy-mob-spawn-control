package io.github.catomon.easymobspawncontrol;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraftforge.coremod.api.ASMAPI;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEventsHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        EntityType<?> type = entity.getType();
        var id = ForgeRegistries.ENTITY_TYPES.getKey(type);
        if (id == null) return;
        String idString = id.toString();

        if (CommonConfig.bannedMobs.contains(idString)) {
            event.setCanceled(true);
            //LOGGER.debug("Blocked entity {} due to it being banned", idString);
        }
    }

    @SubscribeEvent
    public static void onMobFinalizeSpawnEvent(MobSpawnEvent.FinalizeSpawn event) {
        Entity entity = event.getEntity();
        Level level = event.getLevel().getLevel();
        if (level.isClientSide()) return;

        EntityType<?> type = entity.getType();
        var id = ForgeRegistries.ENTITY_TYPES.getKey(type);
        if (id == null) return;
        String idString = id.toString();

        //control spawn
        if (event.getSpawnType() == MobSpawnType.NATURAL) {

            double spawnRate = CommonConfig.getSpawnRate(idString);

            if (spawnRate <= 0.0) {
                event.setSpawnCancelled(true);
                //LOGGER.debug("Blocked spawn of {} due to config spawnRate 0", idString);
            } else if (spawnRate < 1.0) {
                if (level.random.nextDouble() > spawnRate) {
                    event.setSpawnCancelled(true);
                    // LOGGER.debug("Reduced spawn of {} (roll failed for spawnRate {})", idString, spawnRate);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        // obfuscated field name for MobCategory.max (1.20.1)
        String mappedFieldName = ASMAPI.mapField("f_21586_");
        Field mobCategoryMaxField;
        try {
            mobCategoryMaxField = MobCategory.class.getDeclaredField(mappedFieldName);
            mobCategoryMaxField.setAccessible(true);
        } catch (Exception e) {
            LOGGER.error("Failed to access MobCategory.max field", e);
            return;
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
