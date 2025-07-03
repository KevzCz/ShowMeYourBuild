package net.pixeldreamstudios.showmeyourbuild.client.renderer.stats;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.attributepanel.api.AttributePanelAPI;
import net.pixeldreamstudios.showmeyourbuild.util.ModCompat;

import java.util.ArrayList;
import java.util.List;

public class StatsViewRenderer {
    private static final int BOX_WIDTH = 250;
    private static final int BOX_HEIGHT = 135;
    private static final int CLIP_MARGIN = 20;
    private static final boolean DEBUG = false;

    private static final List<StatEntry> rawEntries = new ArrayList<>();
    private static final List<StatEntry> visibleEntries = new ArrayList<>();

    private static NbtCompound currentAttributes = new NbtCompound();

    public static void render(DrawContext context, int centerX, int centerY, TextRenderer tr, int mouseX, int mouseY) {
        int x = centerX - BOX_WIDTH / 2;
        int y = centerY - BOX_HEIGHT / 2 + 20;
        int clipX = x + CLIP_MARGIN;
        int clipY = y + 10;

        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null && ModCompat.ATTRIBUTE_PANEL_LOADED) {
            loadAttributes(AttributePanelAPI.getAttributeSnapshot(player));
        }


        if (rawEntries.isEmpty()) {

            initializeAttributeGroups();
        }

        refreshExpandedEntries(); // Always refresh before rendering

        context.drawTextWithShadow(tr, "Stats", centerX - (tr.getWidth("Stats") / 2), centerY - 55, 0xAAAAAA);

        if (DEBUG) {
            int boxCenterX = x + BOX_WIDTH / 2;
            context.fill(x, y, x + BOX_WIDTH, y + BOX_HEIGHT, 0x44FF0000);
            context.fill(clipX, clipY, x + BOX_WIDTH - CLIP_MARGIN, y + BOX_HEIGHT - 10, 0x440000FF);
            context.drawVerticalLine(boxCenterX, y, y + BOX_HEIGHT, 0x8800FF00);
        }

        StatBoxRenderer.renderContents(context, tr, clipX, clipY, mouseX, mouseY, currentAttributes);
    }

    public static void loadAttributes(NbtCompound attributesNbt) {
        currentAttributes = attributesNbt.copy();
    }

    public static void handleScroll(double amount) {
        StatBoxRenderer.scroll(amount);
    }

    public static void handleClick(int mouseX, int mouseY, int centerX, int centerY) {
        int x = centerX - BOX_WIDTH / 2;
        int y = centerY - BOX_HEIGHT / 2 + 20;
        int clipX = x + CLIP_MARGIN;
        int clipY = y + 10;

        StatBoxRenderer.handleClick(mouseX, mouseY, clipX, clipY);
    }

    public static List<StatEntry> getVisibleEntries() {
        return visibleEntries;
    }

    public static void refreshExpandedEntries() {
        visibleEntries.clear();
        for (StatEntry entry : rawEntries) {
            visibleEntries.add(entry);
            if (entry instanceof StatGroup group && group.isExpanded()) {
                visibleEntries.addAll(group.getChildren());
            }
        }
    }

    private static void initializeAttributeGroups() {
        rawEntries.clear();
        AttributeGroupFactory.excludeModNamespace("puffish_attributes");
        AttributeGroupFactory.excludeModNamespace("spell_power");
        AttributeGroupFactory.excludeAttribute(Identifier.of("some_mod", "some_attribute"));

        if (currentAttributes != null && !currentAttributes.isEmpty()) {
            StatGroupColumnAssigner columnAssigner = new StatGroupColumnAssigner();

            // === Include Spell Power if available
            StatGroup spellGroup = SpellStatsFactory.createFromAttributes(currentAttributes);
            if (!spellGroup.getChildren().isEmpty()) {
                spellGroup.column = columnAssigner.assignColumn(spellGroup);
                spellGroup.propagateColumnToChildren();
                rawEntries.add(spellGroup);
            }

            // === General Groups
            List<StatGroup> groups = AttributeGroupFactory.buildAttributeGroups(currentAttributes);

            for (StatGroup group : groups) {
                group.column = columnAssigner.assignColumn(group);
                group.propagateColumnToChildren();
                rawEntries.add(group);
            }
        }

        refreshExpandedEntries();
    }

}
