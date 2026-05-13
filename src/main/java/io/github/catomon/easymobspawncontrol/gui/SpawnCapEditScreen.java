package io.github.catomon.easymobspawncontrol.gui;

import com.mojang.blaze3d.vertex.Tesselator;
import io.github.catomon.easymobspawncontrol.CommonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;
import net.neoforged.neoforge.client.gui.widget.ScrollPanel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SpawnCapEditScreen extends Screen {

    private final Map<String, Integer> previewCaps;
    private final Map<MobCategory, ExtendedSlider> capSliders = new LinkedHashMap<>();
    private Button applyButton;
    private Button cancelButton;
    private Button resetButton;
    private final boolean isDedicatedServer;
    private final SpawnControlScreen spawnControlScreen;
    private CapScrollPanel capPanel;

    public SpawnCapEditScreen(@NotNull SpawnControlScreen spawnControlScreen) {
        super(Component.translatable("screen.easy_mob_spawn_control.spawn_caps"));
        this.previewCaps = spawnControlScreen.previewCaps;
        this.isDedicatedServer = spawnControlScreen.isDedicatedServerConfig;
        this.spawnControlScreen = spawnControlScreen;
    }

    @Override
    protected void init() {
        super.init();

        capSliders.clear();
        clearWidgets();

        int panelX = 15;
        int panelY = 35;
        int panelW = width - 30;
        int panelH = height - 75;

        capPanel = new CapScrollPanel(Minecraft.getInstance(), panelW, panelH, panelY, panelX);
        addRenderableWidget(capPanel);

        int rowY = panelY + 4;
        for (MobCategory category : capPanel.categories) {
            String name = category.getName().toLowerCase();
            int def = category.getMaxInstancesPerChunk();
            int current = previewCaps.getOrDefault(name, def);

            ExtendedSlider slider = createSlider(category, current, panelX + 185, rowY);
            capSliders.put(category, slider);
            addWidget(slider);  // Key: addWidget for input, not addRenderableWidget

            rowY += 25;
        }

        applyButton = Button.builder(
                        Component.translatable("gui.easy_mob_spawn_control.apply"),
                        btn -> {
                            spawnControlScreen.applyChanges();
                            onClose();
                        })
                .bounds(10, height - 30, 110, 20)
                .build();
        addRenderableWidget(applyButton);

        cancelButton = Button.builder(
                        Component.translatable("gui.easy_mob_spawn_control.cancel"),
                        btn -> onClose())
                .bounds(130, height - 30, 90, 20)
                .build();
        addRenderableWidget(cancelButton);

        resetButton = Button.builder(
                        Component.translatable("gui.easy_mob_spawn_control.reset_default"),
                        btn -> {
                            if (isDedicatedServer) {
                                previewCaps.clear();
                                previewCaps.putAll(CommonConfig.getDefaultCaps());
                                spawnControlScreen.applyChanges();
                            } else {
                                CommonConfig.resetToDefaultCaps();
                                CommonConfig.parseConfig();
                            }
                            onClose();
                        })
                .bounds(250, height - 30, 90, 20)
                .build();
        addRenderableWidget(resetButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawString(font, title, width / 2, 8, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("gui.easy_mob_spawn_control.help_text"), 20, 25, 0xCCCCCC);

        // Panel background
        int panelX = 15;
        int panelY = 35;
        int panelW = width - 30;
        int panelH = height - 75;
//        graphics.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0x70000000);

        // Clip everything to panel bounds (NeoForge 1.21.1 GuiGraphics)
        graphics.enableScissor(panelX, panelY, panelX + panelW, panelY + panelH);

        capPanel.render(graphics, mouseX, mouseY, partialTick);

        graphics.disableScissor();
    }

    private ExtendedSlider createSlider(MobCategory category, int currentCap, int x, int y) {
        return new ExtendedSlider(
                x, y, 180, 20,
                Component.literal(""),
                Component.literal(currentCap + ""),
                0.0, 300.0,
                currentCap, 1.0, 0, true
        ) {
            @Override
            protected void updateMessage() {
                int v = (int) (this.value * 300);
                this.setMessage(Component.literal(v + ""));
            }

            @Override
            protected void applyValue() {
                int v = (int) (this.value * 300);
                previewCaps.put(category.getName().toLowerCase(), v);
            }
        };
    }

    private final class CapScrollPanel extends ScrollPanel {
        private final List<MobCategory> categories = new ArrayList<>();

        public CapScrollPanel(Minecraft client, int width, int height, int top, int left) {
            super(client, width, height, top, left);
            for (MobCategory category : MobCategory.values()) {
                String name = category.getName().toLowerCase();
                if (!name.equals("misc")) {
                    categories.add(category);
                }
            }
        }

        @Override
        protected int getContentHeight() {
            return categories.size() * 25 + 8;
        }

        @Override
        protected void drawBackground(GuiGraphics guiGraphics, Tesselator tess, float partialTick) {
//            super.drawBackground(guiGraphics, tess, partialTick);
        }

        @Override
        protected void drawPanel(GuiGraphics guiGraphics, int entryRight, int relativeY, Tesselator tess, int mouseX, int mouseY) {
            int y = relativeY + 4;

            for (MobCategory category : categories) {
                guiGraphics.drawString(
                        font,
                        Component.literal(category.getName().toUpperCase()),
                        left + 5,
                        y + 5,
                        0xFFFFFF
                );

                ExtendedSlider slider = capSliders.get(category);
                if (slider != null) {
                    slider.setX(left + 185);
                    slider.setY(y);
                    slider.render(guiGraphics, mouseX, mouseY, 0);
                }

                y += 25;
            }
        }

        @Override
        public NarrationPriority narrationPriority() {
            return NarrationPriority.NONE;
        }

        @Override
        public void updateNarration(NarrationElementOutput output) {
        }
    }
}