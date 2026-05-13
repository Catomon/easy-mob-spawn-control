package io.github.catomon.easymobspawncontrol.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MobListWidget extends ObjectSelectionList<MobListWidget.MobEntryWidget> {
    private final SpawnControlScreen parent;
    public String selected = null;

    public MobListWidget(SpawnControlScreen parent) {
        super(parent.getMinecraft(), 270, parent.height, 80, parent.height - 50, 18);
        this.parent = parent;
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
    }

    public void refresh(List<SpawnControlScreen.MobEntry> entries) {
        clearEntries();
        for (SpawnControlScreen.MobEntry entry : entries) {
            addEntry(new MobEntryWidget(this, entry));
        }
    }

    public static class MobEntryWidget extends ObjectSelectionList.Entry<MobEntryWidget> {
        private final MobListWidget list;
        private final SpawnControlScreen.MobEntry entry;

        MobEntryWidget(MobListWidget list, SpawnControlScreen.MobEntry entry) {
            this.list = list;
            this.entry = entry;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            if (list.parent.viewMode == SpawnControlScreen.ViewMode.IN_WORLD) {
                graphics.drawString(list.parent.getMinecraft().font, entry.displayName + " " + list.parent.inWorldEntries.getOrDefault(entry.id, 0), left + 4, top + 4, 0xFFFFFF);
            } else {
                graphics.drawString(list.parent.getMinecraft().font, entry.displayName, left + 4, top + 4, 0xFFFFFF);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            list.parent.selectMob(entry.id);
            list.selected = entry.id;
            return true;
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.literal(entry.displayName);
        }
    }
}
