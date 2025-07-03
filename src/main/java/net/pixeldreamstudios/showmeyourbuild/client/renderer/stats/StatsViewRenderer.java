package net.pixeldreamstudios.showmeyourbuild.client.renderer.stats;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.attributepanel.api.AttributePanelAPI;
import net.pixeldreamstudios.showmeyourbuild.util.ModCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Environment(EnvType.CLIENT)
public class StatsViewRenderer {
    private static boolean isSnapshot = false;

    private static final int BOX_WIDTH = 250;
    private static final int BOX_HEIGHT = 135;
    private static final int CLIP_MARGIN = 20;
    private static final boolean DEBUG = false;
    private static PlayerEntity liveTargetPlayer = null;
    private static final List<StatEntry> rawEntries = new ArrayList<>();
    private static final List<StatEntry> visibleEntries = new ArrayList<>();
    public static void setLiveTargetPlayer(PlayerEntity player) {
        liveTargetPlayer = player;
    }
    private static NbtCompound currentAttributes = new NbtCompound();
    public static NbtCompound getCurrentAttributes() {
        return currentAttributes;
    }
    public static void render(DrawContext context, int centerX, int centerY, TextRenderer tr, int mouseX, int mouseY) {
        int x = centerX - BOX_WIDTH / 2;
        int y = centerY - BOX_HEIGHT / 2 + 20;
        int clipX = x + CLIP_MARGIN;
        int clipY = y + 10;

        if (!isSnapshot && ModCompat.ATTRIBUTE_PANEL_LOADED) {
            PlayerEntity target = liveTargetPlayer != null ? liveTargetPlayer : MinecraftClient.getInstance().player;
            if (target != null) {
                loadAttributes(AttributePanelAPI.getAttributeSnapshot(target), false);
            }
        }



        if (rawEntries.isEmpty()) {

            initializeAttributeGroups();
        }

        refreshExpandedEntries();

        context.drawTextWithShadow(tr, "Stats", centerX - (tr.getWidth("Stats") / 2), centerY - 55, 0xAAAAAA);

        if (DEBUG) {
            int boxCenterX = x + BOX_WIDTH / 2;
            context.fill(x, y, x + BOX_WIDTH, y + BOX_HEIGHT, 0x44FF0000);
            context.fill(clipX, clipY, x + BOX_WIDTH - CLIP_MARGIN, y + BOX_HEIGHT - 10, 0x440000FF);
            context.drawVerticalLine(boxCenterX, y, y + BOX_HEIGHT, 0x8800FF00);
        }

        StatBoxRenderer.renderContents(context, tr, clipX, clipY, mouseX, mouseY, currentAttributes);
    }

    public static void loadAttributes(NbtCompound attributesNbt, boolean snapshot) {
        currentAttributes = attributesNbt.copy();
        isSnapshot = snapshot;
        if (snapshot) liveTargetPlayer = null;
        initializeAttributeGroups();
    }

    public static void loadAttributes(NbtCompound attributesNbt) {
        loadAttributes(attributesNbt, false);
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

        final Set<String> expandedLabels = new HashSet<>();
        for (StatEntry entry : rawEntries) {
            if (entry instanceof StatGroup group && group.isExpanded()) {
                expandedLabels.add(group.getLabel());
            }
        }

        rawEntries.clear();
        AttributeGroupFactory.excludeModNamespace("puffish_attributes");
        AttributeGroupFactory.excludeModNamespace("spell_power");
        AttributeGroupFactory.excludeAttribute(Identifier.of("some_mod", "some_attribute"));

        if (currentAttributes != null && !currentAttributes.isEmpty()) {
            StatGroupColumnAssigner columnAssigner = new StatGroupColumnAssigner();
            if(ModCompat.SPELLPOWER_LOADED) {
                StatGroup spellGroup = SpellStatsFactory.createFromAttributes(currentAttributes);
                if (!spellGroup.getChildren().isEmpty()) {
                    spellGroup.column = columnAssigner.assignColumn(spellGroup);
                    spellGroup.propagateColumnToChildren();
                    spellGroup.setExpanded(expandedLabels.contains(spellGroup.getLabel()));
                    rawEntries.add(spellGroup);
                }
            }
            List<StatGroup> groups = AttributeGroupFactory.buildAttributeGroups(currentAttributes);

            for (StatGroup group : groups) {
                group.column = columnAssigner.assignColumn(group);
                group.propagateColumnToChildren();
                group.setExpanded(expandedLabels.contains(group.getLabel()));
                rawEntries.add(group);
            }
        }

        refreshExpandedEntries();
    }


}
