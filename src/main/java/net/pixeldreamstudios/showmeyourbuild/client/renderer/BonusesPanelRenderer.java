package net.pixeldreamstudios.showmeyourbuild.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.pixeldreamstudios.showmeyourbuild.client.BonusDataStore;
import net.pixeldreamstudios.showmeyourbuild.client.renderer.stats.StatsViewRenderer;

import java.util.List;
import java.util.Map;
@Environment(EnvType.CLIENT)
public class BonusesPanelRenderer {

    public static void render(
            DrawContext context,
            int anchorX,
            int anchorY,
            int maxWidth,
            TextRenderer textRenderer,
            int mouseX,
            int mouseY
    ) {
        Map<Text, Double> condensedBonuses = BonusDataStore.getCondensedBonusesFrom(
                StatsViewRenderer.getCurrentAttributes()
        );

        float scale = 0.75f;
        int padding = 6;
        int spacingBetween = 6;
        int scaledFontHeight = (int)(textRenderer.fontHeight * scale);
        int lineHeight = scaledFontHeight + 4;
        int panelWidth = maxWidth;
        int panelHeight = condensedBonuses.size() * lineHeight + padding * 2;


        context.fillGradient(anchorX, anchorY, anchorX + panelWidth, anchorY + panelHeight, 0xF0101010, 0xF0101010);
        context.fill(anchorX, anchorY, anchorX + panelWidth, anchorY + 1, 0xFF555555);
        context.fill(anchorX, anchorY + panelHeight - 1, anchorX + panelWidth, anchorY + panelHeight, 0xFF555555);
        context.fill(anchorX, anchorY, anchorX + 1, anchorY + panelHeight, 0xFF555555);
        context.fill(anchorX + panelWidth - 1, anchorY, anchorX + panelWidth, anchorY + panelHeight, 0xFF555555);

        int y = anchorY + padding;
        MatrixStack matrices = context.getMatrices();
        int row = 0;

        for (var entry : condensedBonuses.entrySet()) {
            Text labelText = entry.getKey();
            String fullLabel = labelText.getString();
            double value = entry.getValue();
            int color = value > 0 ? 0x00FF00 : 0xFF5555;
            String valueStr = String.format("%+,.2f", value);
            int valuePixelWidth = (int)(textRenderer.getWidth(valueStr) * scale);

            int totalInnerWidth = (int)((panelWidth - padding * 2) / scale);
            int maxLabelWidth = totalInnerWidth - valuePixelWidth - (int)(spacingBetween / scale);

            String trimmedLabel = fullLabel;
            boolean wasTruncated = textRenderer.getWidth(fullLabel) > maxLabelWidth;
            if (wasTruncated) {
                String ellipsis = "…";
                int ellipsisWidth = textRenderer.getWidth(ellipsis);
                trimmedLabel = textRenderer.trimToWidth(fullLabel, maxLabelWidth - ellipsisWidth) + ellipsis;
            }

            if (row % 2 == 1) {
                context.fill(anchorX + 1, y, anchorX + panelWidth - 1, y + lineHeight - 1, 0x10FFFFFF);
            }

            matrices.push();
            matrices.translate(anchorX + padding, y + (lineHeight - scaledFontHeight) / 2f, 0);
            matrices.scale(scale, scale, 1.0f);

            context.drawText(textRenderer, Text.literal(trimmedLabel), 0, 0, color, false);

            int valueX = totalInnerWidth - valuePixelWidth;
            context.drawText(textRenderer, Text.literal(valueStr), valueX, 0, color, false);

            matrices.pop();

            int mouseRelativeX = mouseX - anchorX - padding;
            int mouseRelativeY = mouseY - y;

            boolean mouseOverRow = mouseX >= anchorX + 1 && mouseX < anchorX + panelWidth - 1
                    && mouseY >= y && mouseY < y + lineHeight;

            if (mouseOverRow && (mouseRelativeX / scale) < textRenderer.getWidth(trimmedLabel)) {
                context.drawTooltip(textRenderer, List.of(Text.literal(fullLabel)), mouseX, mouseY);
            }

            y += lineHeight;
            row++;
        }
    }

}
