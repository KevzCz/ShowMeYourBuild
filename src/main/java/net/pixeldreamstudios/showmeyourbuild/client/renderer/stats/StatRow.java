package net.pixeldreamstudios.showmeyourbuild.client.renderer.stats;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class StatRow extends StatEntry {
    private final Identifier icon;
    private final String label;
    private final String value;
    final String tooltip;
    private final boolean indent;

    private final double delta; // 🔸 Add delta
    private final int color;    // 🔸 Color based on delta

    public StatRow(Identifier icon, String label, String value, String tooltip, boolean indent, int column, double delta) {
        this.icon = icon;
        this.label = label;
        this.value = value;
        this.tooltip = tooltip;
        this.indent = indent;
        this.column = column;
        this.delta = delta;
        this.color = delta > 0 ? 0x00FF00 : delta < 0 ? 0xFF5555 : 0xAAAAAA;
    }
    public double getDelta() {
        return delta;
    }

    public int getColor() {
        return color;
    }

    @Override
    public void render(DrawContext ctx, TextRenderer tr, int x, int y) {
        int offset = indent ? 10 : 0;
        int drawX = x + offset;

        if (icon != null) {
            ctx.drawTexture(icon, drawX, y, 0, 0, 10, 10, 10, 10);
            drawX += 14;
        }

        int maxLabelWidth = tr.getWidth("This is quite long");
        String truncatedLabel = StatsRenderUtils.truncateToWidth(tr, label, maxLabelWidth);

        StatsRenderUtils.drawScaledText(ctx, tr, truncatedLabel + ":", drawX, y + 2, 0xFFFFFF);

        if (value != null && !value.isBlank()) {
            int valueX = drawX + (int) (tr.getWidth(truncatedLabel + ":") * 0.7f) + 5;
            StatsRenderUtils.drawScaledText(ctx, tr, value, valueX, y + 2, color); // 👈 color is based on delta
        }
    }


    @Override
    public void renderTooltip(DrawContext ctx, TextRenderer tr, int mouseX, int mouseY, int x, int y, float scrollY) {
        if (tooltip == null) return;

        int offset = indent ? 10 : 0;
        int iconX = x + offset;
        int drawX = icon != null ? iconX + 10 + 4 : iconX;
        float scale = 0.9f;

        int[] bounds = StatsRenderUtils.computeTruncatedLabelAndValueBounds(
                tr, label, value, drawX, scale, tr.getWidth("This is quite long")
        );

        boolean hoverIcon = icon != null && mouseX >= iconX && mouseX <= iconX + 10 && mouseY >= y && mouseY <= y + 10;
        boolean hoverLabel = mouseX >= bounds[0] && mouseX <= bounds[1] && mouseY >= y && mouseY <= y + 10;
        boolean hoverValue = mouseX >= bounds[2] && mouseX <= bounds[3] && mouseY >= y && mouseY <= y + 10;

        if (hoverIcon || hoverLabel) {
            ctx.drawTooltip(tr, Text.literal(tooltip), mouseX, mouseY);
        } else if (hoverValue && value != null && !value.isBlank()) {
            ctx.drawTooltip(tr, Text.literal(value.trim()), mouseX, mouseY);
        }
    }
}
