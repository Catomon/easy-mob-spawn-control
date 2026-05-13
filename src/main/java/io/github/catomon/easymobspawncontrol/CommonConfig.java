package io.github.catomon.easymobspawncontrol;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.common.ForgeConfigSpec;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class CommonConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ForgeConfigSpec SPEC;

    public static Map<String, Double> spawnRates = new HashMap<>();
    public static Map<String, Integer> spawnCaps = new HashMap<>();

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BANNED_MOBS_LIST;
    public static List<String> bannedMobs = List.of();

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SPAWN_RATES_LIST;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> SPAWN_CAPS_LIST;  // NEW

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("config_v1");

        BANNED_MOBS_LIST = builder.comment("Banned mobs (ex: \"minecraft:creeper\", \"minecraft:zombie\"")
                .defineList("bannedMobs", List.of(), s -> true);

        SPAWN_RATES_LIST = builder.comment("Mob spawn rates (0.0–1.0, empty = default 1.0)")
                .defineList("spawnRates", List.of(), s -> true);

        builder.push("spawn_caps_config_v1");

        // Default values from MobCategory (will be 70, 10, 15, 5, 20, 5, 70 in 1.20.1)
        Map<String, Integer> defaults = new LinkedHashMap<>();
        for (MobCategory mc : MobCategory.values()) {
            String name = mc.getName().toLowerCase();
            defaults.put(name, mc.getMaxInstancesPerChunk());
        }

        List<String> defaultCaps = defaults.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.toList());

        SPAWN_CAPS_LIST = builder
                .comment("Per‑category spawn caps per chunk (ex: \"monster=70\", \"creature=10\"). Values outside 0–999 are ignored.")
                .defineList("spawnCaps", defaultCaps, s -> true);
        builder.pop(); // spawn_caps

        builder.pop(); // config_v1
        SPEC = builder.build();
    }

    public static void parseConfig() {
        spawnRates.clear();
        spawnCaps.clear();

        bannedMobs = new ArrayList<>(BANNED_MOBS_LIST.get().stream()
                .filter(s -> !s.isBlank()).toList());
        LOGGER.debug("Loaded {} banned mobs", bannedMobs.size());

        // Parse spawnRates
        for (String entry : SPAWN_RATES_LIST.get()) {
            String[] parts = entry.split("=", 2);
            if (parts.length == 2) {
                try {
                    String mobId = parts[0].trim();
                    double rate = Double.parseDouble(parts[1].trim());
                    if (mobId.isBlank()) continue;
                    spawnRates.put(mobId, Math.max(0.0, Math.min(1.0, rate)));
                } catch (NumberFormatException ignored) {
                    LOGGER.warn("Invalid spawn rate entry: {}", entry);
                }
            }
        }
        LOGGER.debug("Loaded {} spawn rates", spawnRates.size());

        // Parse spawnCaps
        for (String entry : SPAWN_CAPS_LIST.get()) {
            String[] parts = entry.split("=", 2);
            if (parts.length == 2) {
                try {
                    String catName = parts[0].trim().toLowerCase();
                    int cap = Integer.parseInt(parts[1].trim());

                    if (cap < 0 || cap > 999) {
                        LOGGER.warn("Spawn cap for category {} is {} (out of 1–999); skipped.", catName, cap);
                        continue;
                    }

                    spawnCaps.put(catName, cap);
                } catch (NumberFormatException ignored) {
                    LOGGER.warn("Invalid spawn cap entry: {}", entry);
                }
            } else {
                LOGGER.warn("Malformed spawn cap entry (expected 'category=cap'): {}", entry);
            }
        }
    }

    public static Map<String, Integer> getDefaultCaps() {
        Map<String, Integer> defaults = new LinkedHashMap<>();
        for (MobCategory mc : MobCategory.values()) {
            String name = mc.getName().toLowerCase();
            defaults.put(name, mc.getMaxInstancesPerChunk());
        }

        return defaults;
    }

    public static double getSpawnRate(String mobId) {
        if (bannedMobs.contains(mobId)) return 0.0;
        return spawnRates.getOrDefault(mobId, 1.0);
    }

    public static int getSpawnCap(MobCategory category) {
        String catName = category.getName().toLowerCase();
        return spawnCaps.getOrDefault(catName, category.getMaxInstancesPerChunk());
    }

    public static void saveSpawnRates(Map<String, Double> rates) {
        List<String> newList = rates.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.toList());
        SPAWN_RATES_LIST.set(newList);
        if (SPEC.isLoaded()) SPEC.save();
    }

    public static void saveBannedMobs(List<String> mobs) {
        BANNED_MOBS_LIST.set(mobs);
        if (SPEC.isLoaded()) SPEC.save();
    }

    public static void saveSpawnCaps(Map<String, Integer> caps) {
        List<String> newList = caps.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.toList());
        SPAWN_CAPS_LIST.set(newList);
        if (SPEC.isLoaded()) SPEC.save();
    }

    public static void resetToDefaultCaps() {
        SPAWN_CAPS_LIST.set(SPAWN_CAPS_LIST.getDefault());
        if (SPEC.isLoaded()) SPEC.save();
    }

    public static void reload() {
        parseConfig();
    }
}
