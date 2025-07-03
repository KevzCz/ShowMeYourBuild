package net.pixeldreamstudios.showmeyourbuild.client.renderer.stats;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.List;

public class StatBoxRenderer {
    private static final int ENTRY_HEIGHT = 18;
    private static final int COLUMN_GAP = 10;
    private static final int ENTRIES_PER_COLUMN = 4;
    private static float scrollY = 0;

    private static final List<RenderedEntry> rendered = new ArrayList<>();

    private record RenderedEntry(StatEntry entry, int x, int y, int width, int height) {}

    public static void renderContents(DrawContext ctx, TextRenderer tr, int x, int y, int mouseX, int mouseY, NbtCompound attributes) {
        CustomStatsLayoutRenderer.render(ctx, tr, x, y, mouseX, mouseY, attributes);
        rendered.clear();

        List<StatEntry> entries = StatsViewRenderer.getVisibleEntries();

        int layoutOffsetY = y + 32;
        int columnWidth = 100;
        int leftX = x;
        int rightX = x + columnWidth + COLUMN_GAP;

        int scrollIndex = (int) Math.floor(scrollY / ENTRY_HEIGHT);
        int leftRendered = 0;
        int rightRendered = 0;

        for (StatEntry entry : entries) {
            int col = entry.column % 2;
            int rowInCol = (col == 0) ? leftRendered : rightRendered;

            if (rowInCol < scrollIndex) {
                if (col == 0) leftRendered++;
                else rightRendered++;
                continue;
            }

            if ((col == 0 && leftRendered - scrollIndex >= ENTRIES_PER_COLUMN) ||
                    (col == 1 && rightRendered - scrollIndex >= ENTRIES_PER_COLUMN)) {
                continue;
            }

            int drawX = (col == 0) ? leftX : rightX;
            int drawY = layoutOffsetY + (ENTRY_HEIGHT * (rowInCol - scrollIndex));

            entry.render(ctx, tr, drawX, drawY);
            entry.renderTooltip(ctx, tr, mouseX, mouseY, drawX, drawY, scrollY);
            rendered.add(new RenderedEntry(entry, drawX, drawY, 100, ENTRY_HEIGHT));

            if (col == 0) leftRendered++;
            else rightRendered++;
        }
    }

    public static void scroll(double amount) {
        scrollY -= amount * ENTRY_HEIGHT;

        List<StatEntry> entries = StatsViewRenderer.getVisibleEntries();
        int leftCount = 0;
        int rightCount = 0;

        for (StatEntry entry : entries) {
            if (entry.column % 2 == 0) leftCount++;
            else rightCount++;
        }

        int maxScrollRows = Math.max(leftCount, rightCount) - ENTRIES_PER_COLUMN;
        float maxScroll = Math.max(0, maxScrollRows * ENTRY_HEIGHT);
        scrollY = Math.max(0, Math.min(scrollY, maxScroll));
    }

    public static void resetScroll() {
        scrollY = 0;
    }

    public static void handleClick(int mouseX, int mouseY, int originX, int originY) {
        for (RenderedEntry r : rendered) {

            if (mouseX >= r.x && mouseX <= r.x + r.width &&
                    mouseY >= r.y && mouseY <= r.y + r.height) {

                if (r.entry instanceof StatGroup group) {
                    group.toggle();
                    StatsViewRenderer.refreshExpandedEntries();
                    return;
                }
            }
        }
    }


}
