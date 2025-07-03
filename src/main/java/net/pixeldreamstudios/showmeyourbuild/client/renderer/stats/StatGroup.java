package net.pixeldreamstudios.showmeyourbuild.client.renderer.stats;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.showmeyourbuild.client.gui.BuildViewScreen;

import java.util.ArrayList;
import java.util.List;

public class StatGroup extends StatEntry {
    private final Identifier icon;
    final String label;
    private final String value;
    final String tooltip;
    private final List<StatRow> children = new ArrayList<>();
    private boolean expanded;
    private static final int SLOT_WIDTH = 100;
    private static final int SLOT_HEIGHT = 18;

    public StatGroup(Identifier icon, String label, String value, boolean expanded, String tooltip, int column) {
        this.icon = icon;
        this.label = label;
        this.value = value;
        this.tooltip = tooltip;
        this.expanded = expanded;
        this.column = column;
    }

    @Override
    public void render(DrawContext ctx, TextRenderer tr, int columnX, int y) {
        final int iconSize = 10;
        final float scale = 0.7f;

        int slotX = columnX + ((100 - SLOT_WIDTH) / 2);
        ctx.drawTexture(BuildViewScreen.STATS_SLOT, slotX, y, 0, 0, SLOT_WIDTH, 16, SLOT_WIDTH, 16);

        // Fixed position for arrow (left margin)
        int arrowX = slotX + 8;
        int centerY = y + 4;
        ctx.drawTextWithShadow(tr, expanded ? "▼" : "▶", arrowX, centerY, 0xAAAAAA);

        // --- Center the icon + label + value block ---
        int labelWidth = (int) (tr.getWidth(label + ":") * scale);
        int valueWidth = value != null ? (int) (tr.getWidth(value) * scale) : 0;
        int iconWidth = icon != null ? iconSize + 4 : 0;
        int totalContentWidth = iconWidth + labelWidth + (valueWidth > 0 ? 5 + valueWidth : 0);

        int contentStartX = slotX + (SLOT_WIDTH - totalContentWidth) / 2;

        int nextX = contentStartX;

        // Icon
        if (icon != null) {
            ctx.getMatrices().push();
            ctx.getMatrices().translate(nextX, centerY, 0);
            ctx.getMatrices().scale(1.0f, 1.0f, 1.0f);
            ctx.drawTexture(icon, 0, -2, 0, 0, iconSize, iconSize, iconSize, iconSize);
            ctx.getMatrices().pop();
            nextX += iconSize + 4;
        }

        // Label
        StatsRenderUtils.drawScaledText(ctx, tr, label + ":", nextX, centerY, 0xFFFFFF);
        nextX += labelWidth;

        // Value (optional)
        if (value != null && !value.isBlank()) {
            nextX += 5;
            StatsRenderUtils.drawScaledText(ctx, tr, value, nextX, centerY, 0xAAAAAA);
        }
    }

    public void propagateColumnToChildren() {
        for (StatRow child : children) {
            child.column = this.column;
        }
    }

    @Override
    public void renderTooltip(DrawContext ctx, TextRenderer tr, int mouseX, int mouseY, int x, int y, float scrollY) {
        if (tooltip == null) return;

        int iconSize = 10;
        int arrowWidth = tr.getWidth("▶");
        int iconX = x + arrowWidth + 2;
        int drawX = iconX + iconSize + 4;
        float scale = 0.9f;

        int[] bounds = StatsRenderUtils.computeTruncatedLabelAndValueBounds(
                tr, label, value != null ? value : "", drawX, scale, tr.getWidth("This is quite long")
        );

        boolean hoverIcon = mouseX >= iconX && mouseX <= iconX + iconSize && mouseY >= y && mouseY <= y + iconSize;
        boolean hoverLabel = mouseX >= bounds[0] && mouseX <= bounds[1] && mouseY >= y && mouseY <= y + iconSize;
        boolean hoverValue = mouseX >= bounds[2] && mouseX <= bounds[3] && mouseY >= y && mouseY <= y + iconSize;

        if (hoverIcon || hoverLabel) {
            ctx.drawTooltip(tr, Text.literal(tooltip), mouseX, mouseY);
        } else if (hoverValue && value != null && !value.isBlank()) {
            ctx.drawTooltip(tr, Text.literal(value.trim()), mouseX, mouseY);
        }
    }

    public void toggle() {
        this.expanded = !this.expanded;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public List<StatRow> getChildren() {
        return children;
    }

    public void addChild(StatRow row) {
        children.add(row);
    }
}
