package net.pixeldreamstudios.showmeyourbuild.client.renderer.stats;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
@Environment(EnvType.CLIENT)
public class StatsRenderUtils {
    public static void drawScaledText(DrawContext ctx, TextRenderer tr, String text, int x, int y, int color) {
        ctx.getMatrices().push();
        ctx.getMatrices().scale(0.7f, 0.7f, 1.0f);
        ctx.drawTextWithShadow(tr, text, (int) (x / 0.7f), (int) (y / 0.7f), color);
        ctx.getMatrices().pop();
    }
    public static String truncateToWidth(TextRenderer tr, String text, int maxWidth) {
        if (tr.getWidth(text) <= maxWidth) return text;

        String ellipsis = "...";
        int ellipsisWidth = tr.getWidth(ellipsis);

        int i = text.length();
        while (i > 0 && tr.getWidth(text.substring(0, i)) + ellipsisWidth > maxWidth) {
            i--;
        }

        return (i <= 0) ? ellipsis : text.substring(0, i) + ellipsis;
    }

    public static int[] computeTruncatedLabelAndValueBounds(
            TextRenderer tr, String label, String value, int startX, float scale, int maxLabelWidth
    ) {
        String labelText = label.replace(":", "").trim();
        String truncated = StatsRenderUtils.truncateToWidth(tr, labelText, maxLabelWidth);
        int labelWidth = (int) (tr.getWidth(truncated + ":") * scale);
        int valueWidth = (int) (tr.getWidth(value.trim()) * scale);

        int labelStart = startX;
        int labelEnd = labelStart + labelWidth;

        int valueStart = labelEnd + 5;
        int valueEnd = valueStart + valueWidth;

        return new int[]{labelStart, labelEnd, valueStart, valueEnd};
    }



}
