package io.github.catomon.easymobspawncontrol;

import io.github.catomon.easymobspawncontrol.gui.SpawnControlScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.util.*;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEventHandler {

    private static final Map<String, SpawnControlScreen.MobEntry> recentSpawns =
            new LinkedHashMap<>(30, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, SpawnControlScreen.MobEntry> eldest) {
                    return size() > 30;
                }
            };

    public static List<SpawnControlScreen.MobEntry> getRecentMobs() {
        List<SpawnControlScreen.MobEntry> reversed = new ArrayList<>(recentSpawns.values());
        Collections.reverse(reversed);
        return reversed;
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event ) {
        Entity entity = event.getEntity();
        Level level = event.getLevel();
        if (!level.isClientSide()) return;

        EntityType<?> type = entity.getType();
        var id = EntityType.getKey(type);
        if (id == null) return;
        String idString = id.toString();

        if (type.getCategory() != MobCategory.MISC) {
            String name = type.getDescription().getString();
            recentSpawns.put(idString, new SpawnControlScreen.MobEntry(name, id.toString()));
        }
    }
}
