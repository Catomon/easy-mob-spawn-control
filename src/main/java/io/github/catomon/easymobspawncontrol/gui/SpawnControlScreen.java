package io.github.catomon.easymobspawncontrol.gui;

import io.github.catomon.easymobspawncontrol.ClientEventHandler;
import io.github.catomon.easymobspawncontrol.CommonConfig;
import io.github.catomon.easymobspawncontrol.ModCommon;
import io.github.catomon.easymobspawncontrol.network.ConfigRequestPacket;
import io.github.catomon.easymobspawncontrol.network.MobCountListRequest;
import io.github.catomon.easymobspawncontrol.network.NetworkHandler;
import io.github.catomon.easymobspawncontrol.network.SaveConfigPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class SpawnControlScreen extends Screen {
    enum ViewMode {
        ALL_MOBS("gui.easy_mob_spawn_control.view_all"),
        RECENT("gui.easy_mob_spawn_control.view_recent"),
        BANNED("gui.easy_mob_spawn_control.view_banned"),
        EDITED("gui.easy_mob_spawn_control.view_edited"),
        IN_WORLD("gui.easy_mob_spawn_control.view_in_world");

        public final String activeText;

        ViewMode(String active) {
            this.activeText = active;
        }
    }

    private MobListWidget mobList;
    private EditBox searchBox;
    private Button toggleButton;
    private Button banCheckbox;
    ViewMode viewMode = ViewMode.ALL_MOBS;
    private String selectedMob = null;
    private int selectedPercent = 100;
    private boolean selectedSpawnDisabled = false;

    private ForgeSlider spawnrateSlider;

    boolean isDedicatedServerConfig;

    public final Map<String, Double> previewSpawnRates = new HashMap<>();
    public final ArrayList<String> previewBans = new ArrayList<>();
    public final Map<String, Integer> previewCaps = new HashMap<>();

    Map<String, Integer> inWorldEntries = Collections.emptyMap();

    private Button updateInWorldEntriesButton;

    private LivingEntity previewEntity = null;

    private static Map<String, Integer> receivedMobCounts = null;

    public static void setMobCounts(Map<String, Integer> mobCounts) {
        receivedMobCounts = mobCounts;
    }

    public SpawnControlScreen() {
        super(Component.translatable("screen.easy_mob_spawn_control.config"));

        receivedMobCounts = null;

        previewEntity = null;

        isDedicatedServerConfig = false;

        previewSpawnRates.putAll(CommonConfig.spawnRates);
        previewBans.addAll(CommonConfig.bannedMobs);
        previewCaps.putAll(CommonConfig.spawnCaps);
    }

    public SpawnControlScreen(
            Map<String, Double> previewSpawnRates,
            List<String> previewBans,
            Map<String, Integer> previewCaps,
            boolean isDedicatedServerConfig
    ) {
        super(Component.translatable("screen.easy_mob_spawn_control.config"));

        previewEntity = null;

        this.isDedicatedServerConfig = isDedicatedServerConfig;

        this.previewSpawnRates.putAll(previewSpawnRates);
        this.previewBans.addAll(previewBans);
        this.previewCaps.putAll(previewCaps);
    }

    @Override
    protected void init() {
        super.init();

        receivedMobCounts = null;

        // List toggle button
        updateToggleButton();

        updateInWorldEntriesButton = Button.builder(
                        Component.translatable("easy_mob_spawn_control.update"),
                        btn -> {
                            try {
                                if (minecraft == null) return;
                                Level level = minecraft.level;
                                if (level == null) return;
                                if (minecraft.player == null) return;
                                HashMap<String, Integer> mobs = new HashMap<>();
                                if (minecraft.hasSingleplayerServer()) {
                                    var serverPlayer = minecraft.getSingleplayerServer().getPlayerList().getPlayers().stream().filter(p -> p.getUUID().equals(minecraft.player.getUUID())).findFirst().orElse(null);
                                    if (serverPlayer == null) return;
                                    var serverLevel = serverPlayer.serverLevel();
                                    for (Entity entity : serverLevel.getEntities().getAll()) {
                                        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                                        if (id == null) continue;

                                        String key = id.toString();
                                        mobs.merge(key, 1, Integer::sum);
                                    }
                                } else {
                                    NetworkHandler.INSTANCE.sendToServer(new MobCountListRequest());

//                                    for (Entity entity : ((ClientLevel) level).entitiesForRendering()) {
//                                        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
//                                        if (id == null) continue;
//
//                                        String key = id.toString();
//                                        mobs.merge(key, 1, Integer::sum);
//                                    }
                                }

                                inWorldEntries = mobs;
                                refreshList();
                            } catch (Exception e) {
                                System.err.println(ModCommon.MODID + ": failed to update IN_WORLD mob list.");
                                e.printStackTrace();
                            }

                            btn.visible = false;
                            btn.active = false;
                        })
                .bounds(
                        width / 5 - 30, height / 2 - 10, 60, 20
                ).build();
        addRenderableWidget(updateInWorldEntriesButton);
        updateUpdateButtonVisibility();

        searchBox = new EditBox(font, 30, 50, 150, 20,
                Component.translatable("gui.easy_mob_spawn_control.search_placeholder"));
        searchBox.setResponder(text -> refreshList());
        addRenderableWidget(searchBox);

        mobList = new MobListWidget(this);
        addWidget(mobList);

        addRenderableWidget(Button.builder(
                        Component.translatable("gui.easy_mob_spawn_control.apply"),
                        btn -> {
                            applyChanges();

                            this.onClose();
                        })
                .bounds(10, height - 30, 100, 20).build());

        addRenderableWidget(Button.builder(
                        Component.translatable("gui.easy_mob_spawn_control.cancel"),
                        btn -> this.onClose())
                .bounds(135, height - 30, 90, 20).build());

        addRenderableWidget(Button.builder(
                        Component.translatable("gui.easy_mob_spawn_control.edit_caps"),
                        btn -> {
                            if (minecraft != null) {
                                minecraft.setScreen(new SpawnCapEditScreen(SpawnControlScreen.this));
                            }
                        })
                .bounds(260, height - 30, 90, 20).build());

        refreshList();
    }

    private void updateUpdateButtonVisibility() {
        if (inWorldEntries.isEmpty() && Minecraft.getInstance().level != null) {
            updateInWorldEntriesButton.active = viewMode == ViewMode.IN_WORLD;
            updateInWorldEntriesButton.visible = viewMode == ViewMode.IN_WORLD;
        } else {
            updateInWorldEntriesButton.active = false;
            updateInWorldEntriesButton.visible = false;
        }
    }

    public void applyChanges() {
        if (isDedicatedServerConfig) {
            NetworkHandler.INSTANCE.sendToServer(new SaveConfigPacket(previewSpawnRates, previewCaps, previewBans));
        } else {
            CommonConfig.saveSpawnRates(previewSpawnRates);
            CommonConfig.saveBannedMobs(previewBans);
            CommonConfig.saveSpawnCaps(previewCaps);
            CommonConfig.parseConfig();

            var minecraft = Minecraft.getInstance();
            var server = minecraft.getSingleplayerServer();
            if (server != null) {
                server.executeBlocking(() -> {
                    for (ServerLevel serverLevel : server.getAllLevels()) {
                        List<Entity> bannedEntities = new ArrayList<>();
                        for (Entity entity : serverLevel.getEntities().getAll()) {
                            var key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
                            if (key != null) {
                                String id = key.toString();
                                if (CommonConfig.bannedMobs.contains(id)) {
                                    bannedEntities.add(entity);
                                }
                            }
                        }
                        for (Entity entity : bannedEntities) {
                            entity.remove(Entity.RemovalReason.DISCARDED);
                        }
                    }
                });
            }
        }
    }

    private void updateToggleButton() {
        if (toggleButton != null) {
            removeWidget(toggleButton);
        }

        toggleButton = Button.builder(
                        Component.translatable("gui.easy_mob_spawn_control.view_toggle",
                                Component.translatable(viewMode.activeText)),
                        btn -> {
                            ViewMode[] modes2 = ViewMode.values();
                            viewMode = modes2[(viewMode.ordinal() + 1) % modes2.length];
                            updateToggleButton();
                            refreshList();

                            updateUpdateButtonVisibility();
                        })
                .bounds(30, 20, 180, 20)
                .build();
        addRenderableWidget(toggleButton);
    }

    private void refreshList() {
        mobList.setScrollAmount(0);
        List<MobEntry> mobs;
        if (viewMode == ViewMode.IN_WORLD) {
            mobs = new ArrayList<>(
                    getMobEntries().stream()
                            .sorted(Comparator.comparing(mob -> inWorldEntries.getOrDefault(mob.id, 0), Integer::compareTo))
                            .toList()
            );
            Collections.reverse(mobs);
        } else {
            mobs = getMobEntries().stream()
                    .sorted(Comparator.comparing(mob -> mob.displayName, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        }
        mobList.refresh(mobs);
    }

    private List<MobEntry> getMobEntries() {
        List<MobEntry> entries = new ArrayList<>();
        String query = searchBox.getValue().toLowerCase(Locale.ROOT);

        switch (viewMode) {
            case RECENT:
                for (MobEntry entry : ClientEventHandler.getRecentMobs()) {
                    if (query.isEmpty() || entry.id.toLowerCase().contains(query) || entry.displayName.toLowerCase().contains(query)) {
                        entries.add(entry);
                    }
                }
                break;

            case BANNED:
                for (String id : previewBans) {
                    if (query.isEmpty() || id.toLowerCase().contains(query)) {
                        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(id));
                        if (type != null && type.getCategory() != MobCategory.MISC) {
                            entries.add(new MobEntry(type.getDescription().getString(), id));
                        }
                    }
                }
                break;

            case EDITED:
                for (String id : previewSpawnRates.keySet()) {
                    if (query.isEmpty() || id.toLowerCase().contains(query)) {
                        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(id));
                        if (type != null && type.getCategory() != MobCategory.MISC) {
                            entries.add(new MobEntry(type.getDescription().getString(), id));
                        }
                    }
                }
                break;

            case IN_WORLD:
                for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                    if (type.getCategory() != MobCategory.MISC) {
                        var key = ForgeRegistries.ENTITY_TYPES.getKey(type);
                        String id = key != null ? key.toString() : "unknown";
                        var name = type.getDescription().getString();
                        if (!inWorldEntries.containsKey(id)) continue;
                        if (query.isEmpty() || id.toLowerCase().contains(query) || name.toLowerCase().contains(query)) {
                            entries.add(new MobEntry(name, id));
                        }
                    }
                }
                break;

            case ALL_MOBS:
            default:
                for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                    if (type.getCategory() != MobCategory.MISC) {
                        var key = ForgeRegistries.ENTITY_TYPES.getKey(type);
                        String id = key != null ? key.toString() : "unknown";
                        var name = type.getDescription().getString();
                        if (query.isEmpty() || id.toLowerCase().contains(query) || name.toLowerCase().contains(query)) {
                            entries.add(new MobEntry(name, id));
                        }
                    }
                }
                break;
        }
        return entries;
    }

    public void selectMob(String mobId) {
        selectedMob = mobId;
        selectedPercent = (int) ((previewSpawnRates.getOrDefault(mobId, 1.0) * 100.0));
        selectedSpawnDisabled = selectedPercent <= 0;
        mobList.selected = mobId;

        updatePreviewEntity();

        initEntryWidgets();
    }

    private boolean checkedConn = false;

    @Override
    public void tick() {
        if (!checkedConn && !isDedicatedServerConfig) {
            if (minecraft != null) {
                if (!minecraft.hasSingleplayerServer()) {
                    if (minecraft.getConnection() != null) {
                        onClose();
                        NetworkHandler.INSTANCE.sendToServer(new ConfigRequestPacket());
                    }

                    checkedConn = true;
                }
            }
        }

        if (receivedMobCounts != null) {
            inWorldEntries = receivedMobCounts;

            refreshList();

            receivedMobCounts = null;
        }

        super.tick();
    }

    private boolean previewNotAwailable = false;

    private void updatePreviewEntity() {
        previewEntity = null;

        if (selectedMob == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            previewNotAwailable = true;
            return;
        }

        ResourceLocation rl = ResourceLocation.parse(selectedMob);
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(rl);
        if (type == null) return;
        if (!(type.create(mc.level) instanceof LivingEntity living)) return;

        if (living instanceof Mob mob) {
            mob.setNoAi(true);
        }

        previewNotAwailable = false;
        previewEntity = living;
    }

    private void initEntryWidgets() {
        // Remove existing widgets
        if (spawnrateSlider != null) {
            removeWidget(spawnrateSlider);
        }
        if (banCheckbox != null) {
            removeWidget(banCheckbox);
        }

        spawnrateSlider = new ForgeSlider(width - 195, 85, 180, 20,
                Component.translatable("gui.easy_mob_spawn_control.spawn_rate_label"),
                Component.translatable("gui.easy_mob_spawn_control.spawn_rate_value", selectedPercent),
                0.0, 100.0, selectedPercent, 1.0, 0, true) {
            @Override
            protected void updateMessage() {
                selectedPercent = (int) (this.value * 100.0);
                selectedSpawnDisabled = selectedPercent <= 0;
                this.setMessage(Component.translatable("gui.easy_mob_spawn_control.spawn_rate_value", selectedPercent));
            }

            @Override
            protected void applyValue() {
                selectedPercent = (int) (this.value * 100.0);
                selectedSpawnDisabled = selectedPercent <= 0;
                double spawnRate = selectedPercent / 100.0;
                if (spawnRate != 1.0)
                    previewSpawnRates.put(selectedMob, selectedSpawnDisabled ? 0.0 : spawnRate);
                else
                    previewSpawnRates.remove(selectedMob);
            }
        };
        addRenderableWidget(spawnrateSlider);

        banCheckbox = Button.builder(
                        Component.translatable(previewBans.contains(selectedMob)
                                ? "gui.easy_mob_spawn_control.ban_checkbox_checked"
                                : "gui.easy_mob_spawn_control.ban_checkbox_unchecked"),
                        btn -> {
                            boolean isBanned = previewBans.contains(selectedMob);
                            if (isBanned) {
                                previewBans.remove(selectedMob);
                            } else {
                                previewBans.add(selectedMob);
                            }
                            btn.setMessage(Component.translatable(previewBans.contains(selectedMob)
                                    ? "gui.easy_mob_spawn_control.ban_checkbox_checked"
                                    : "gui.easy_mob_spawn_control.ban_checkbox_unchecked"));
                        })
                .bounds(width - 195, 115, 180, 20)
                .build();
        addRenderableWidget(banCheckbox);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        mobList.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);

        if (selectedMob != null) {
            graphics.fill(width - 200, 20, width - 10, height - 40, 0x80000000);
            graphics.drawString(font, selectedMob, width - 195, 25, 0xFFFFFF);
            graphics.drawString(font,
                    Component.translatable("gui.easy_mob_spawn_control.natural_spawn_rate"),
                    width - 195, 70, 0xFFFFFF);

            if (previewEntity != null) {
                int x = width - 100;
                int y = height - 60;

                float mouseXAngle = (float) ((x - mouseX) / 40.0);
                float mouseYAngle = (float) ((y - 50 - mouseY) / 40.0);

                InventoryScreen.renderEntityInInventoryFollowsMouse(
                        graphics, x, y, 30,
                        mouseXAngle, mouseYAngle,
                        previewEntity
                );
            } else {
                if (previewNotAwailable)
                    graphics.drawString(font,
                            Component.translatable("gui.easy_mob_spawn_control.preview_unavailable"),
                            width - 195, height - 60, 0xFFFFFF);
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    public static class MobEntry {
        public final String displayName;
        public final String id;

        public MobEntry(String name, String id) {
            this.displayName = name;
            this.id = id;
        }
    }
}
