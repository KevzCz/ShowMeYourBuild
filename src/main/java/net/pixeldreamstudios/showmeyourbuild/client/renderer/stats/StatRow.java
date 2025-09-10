package net.pixeldreamstudios.showmeyourbuild.client.renderer.stats;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class StatRow extends StatEntry {
    private static final int LABEL_COLOR = 0xB0B0B0;

    private final Identifier icon;
    private final String displayLabel;
    private final String value;
    final String tooltip;
    private final boolean indent;

    private final double delta;
    private final int color;

    private final String iconGlyph;

    public StatRow(Identifier icon, String label, String value, String tooltip, boolean indent, int column, double delta) {
        this.icon = icon;
        this.tooltip = tooltip;
        this.indent = indent;
        this.column = column;
        this.delta = delta;
        this.color = delta > 0 ? 0x00FF00 : delta < 0 ? 0xFF5555 : 0xAAAAAA;

        String glyph = null;
        String cleanedLabel = label;
        try {
            String stripped = net.pixeldreamstudios.iconleadingtooltip.util.IconLeadingUtil.stripSectionCodes(label);
            int[] span = net.pixeldreamstudios.iconleadingtooltip.util.IconLeadingUtil.firstIconSpan(stripped);
            if (span[0] >= 0) {
                glyph = stripped.substring(span[0], span[1]);
                cleanedLabel = (stripped.substring(0, span[0]) + stripped.substring(span[1])).trim();
            } else {
                cleanedLabel = stripped;
            }
        } catch (Throwable t) {
            cleanedLabel = label;
        }
        this.iconGlyph = glyph;
        this.displayLabel = cleanedLabel;
        this.value = value;
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
        int iconX = x + offset;
        int drawX = iconX;

        boolean drewIcon = false;
        if (iconGlyph != null && !iconGlyph.isEmpty()) {
            float s = 0.8f;
            ctx.getMatrices().push();
            ctx.getMatrices().scale(s, s, 1.0f);
            ctx.drawTextWithShadow(tr, iconGlyph, (int) (iconX / s), (int) ((y + 1) / s), 0xFFFFFF);
            ctx.getMatrices().pop();
            drawX = iconX + 10 + 4;
            drewIcon = true;
        } else if (icon != null) {
            ctx.drawTexture(icon, iconX, y, 0, 0, 10, 10, 10, 10);
            drawX = iconX + 10 + 4;
            drewIcon = true;
        }

        if (!drewIcon) {
            drawX = iconX;
        }

        int maxLabelWidth = tr.getWidth("This is quite long");
        String truncatedLabel = StatsRenderUtils.truncateToWidth(tr, displayLabel, maxLabelWidth);

        StatsRenderUtils.drawScaledText(ctx, tr, truncatedLabel + ":", drawX, y + 2, LABEL_COLOR);

        if (value != null && !value.isBlank()) {
            int valueX = drawX + (int) (tr.getWidth(truncatedLabel + ":") * 0.7f) + 5;
            StatsRenderUtils.drawScaledText(ctx, tr, value, valueX, y + 2, color);
        }
    }

    @Override
    public void renderTooltip(DrawContext ctx, TextRenderer tr, int mouseX, int mouseY, int x, int y, float scrollY) {
        if (tooltip == null) return;

        int offset = indent ? 10 : 0;
        int iconX = x + offset;
        int drawX = (icon != null || (iconGlyph != null && !iconGlyph.isEmpty())) ? iconX + 10 + 4 : iconX;
        float scale = 0.9f;

        int[] bounds = StatsRenderUtils.computeTruncatedLabelAndValueBounds(
                tr, displayLabel, value, drawX, scale, tr.getWidth("This is quite long")
        );

        boolean hasAnyIcon = icon != null || (iconGlyph != null && !iconGlyph.isEmpty());
        boolean hoverIcon = hasAnyIcon && mouseX >= iconX && mouseX <= iconX + 10 && mouseY >= y && mouseY <= y + 10;
        boolean hoverLabel = mouseX >= bounds[0] && mouseX <= bounds[1] && mouseY >= y && mouseY <= y + 10;
        boolean hoverValue = mouseX >= bounds[2] && mouseX <= bounds[3] && mouseY >= y && mouseY <= y + 10;

        if (hoverIcon || hoverLabel) {
            ctx.drawTooltip(tr, Text.literal(tooltip), mouseX, mouseY);
        } else if (hoverValue && value != null && !value.isBlank()) {
            ctx.drawTooltip(tr, Text.literal(value.trim()), mouseX, mouseY);
        }
    }
}
