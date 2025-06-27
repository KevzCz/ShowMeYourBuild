package net.pixeldreamstudios.showmeyourbuild.client.renderer;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.pixeldreamstudios.showmeyourbuild.client.gui.BuildViewScreen;

import java.util.List;

public class LegacyAccessorySlotRenderer {

    private static final int SLOT_SIZE = 16;
    private static final int SLOT_PADDING = 4;
    private static final int LEFT_AREA_X_OFFSET = -70;
    private static final int RIGHT_AREA_X_OFFSET = 70;
    private static final int AREA_WIDTH = 48;
    private static final int AREA_HEIGHT = 100;
    private static final boolean DEBUG_SLOT_AREAS = false;


    public static void renderAccessorySlots(DrawContext context, PlayerEntity entity, int centerX, int centerY,
                                            TextRenderer textRenderer, int mouseX, int mouseY, int page) {

        TrinketsApi.getTrinketComponent(entity).ifPresent(component -> {
            List<Pair<SlotReference, ItemStack>> equipped = new java.util.ArrayList<>(component.getAllEquipped());
            List<Pair<SlotReference, ItemStack>> leftItems = new java.util.ArrayList<>();
            List<Pair<SlotReference, ItemStack>> rightItems = new java.util.ArrayList<>();
            for (int i = 0; i < equipped.size(); i++) {
                (i % 2 == 0 ? leftItems : rightItems).add(equipped.get(i));
            }

            renderSide(context, textRenderer, leftItems, centerX + LEFT_AREA_X_OFFSET, centerY, true, mouseX, mouseY);
            renderSide(context, textRenderer, rightItems, centerX + RIGHT_AREA_X_OFFSET, centerY, false, mouseX, mouseY);
        });

    }


    private static void renderSide(DrawContext context, TextRenderer textRenderer,
                                   List<Pair<SlotReference, ItemStack>> items, int originX, int originY,
                                   boolean isLeft, int mouseX, int mouseY) {

        final int maxPerColumn = 8;

        if (items.size() <= maxPerColumn) {
            // Centered layout (use full height)
            int layoutCenterX = originX;
            int layoutCenterY = originY;

            if (DEBUG_SLOT_AREAS) {
                int areaX = layoutCenterX - AREA_WIDTH / 2;
                int areaY = layoutCenterY - AREA_HEIGHT / 2;
                context.fill(areaX, areaY, areaX + AREA_WIDTH, areaY + AREA_HEIGHT, 0x2200FF00);
            }

            List<Position> layout = generateLayout(items.size());

            for (int i = 0; i < items.size(); i++) {
                renderSlot(context, textRenderer, items.get(i), layoutCenterX + layout.get(i).dx - SLOT_SIZE / 2,
                        layoutCenterY + layout.get(i).dy - SLOT_SIZE / 2, mouseX, mouseY);
            }

        } else {
            // Split into pages (top and bottom halves)
            int totalPages = (int) Math.ceil(items.size() / (double) maxPerColumn);

            for (int page = 0; page < totalPages; page++) {
                int from = page * maxPerColumn;
                int to = Math.min(from + maxPerColumn, items.size());
                List<Pair<SlotReference, ItemStack>> pageItems = items.subList(from, to);

                int pageOffsetY = (page == 0) ? -AREA_HEIGHT / 4 : AREA_HEIGHT / 4;
                int layoutCenterX = originX;
                int layoutCenterY = originY + pageOffsetY;

                if (DEBUG_SLOT_AREAS) {
                    int areaX = layoutCenterX - AREA_WIDTH / 2;
                    int areaY = layoutCenterY - AREA_HEIGHT / 2;
                    context.fill(areaX, areaY, areaX + AREA_WIDTH, areaY + AREA_HEIGHT, 0x220000FF);
                }

                List<Position> layout = generateLayout(pageItems.size());

                for (int i = 0; i < pageItems.size(); i++) {
                    renderSlot(context, textRenderer, pageItems.get(i), layoutCenterX + layout.get(i).dx - SLOT_SIZE / 2,
                            layoutCenterY + layout.get(i).dy - SLOT_SIZE / 2, mouseX, mouseY);
                }
            }
        }
    }
    private static void renderSlot(DrawContext context, TextRenderer textRenderer,
                                   Pair<SlotReference, ItemStack> pair, int x, int y,
                                   int mouseX, int mouseY) {

        ItemStack stack = pair.getRight();

        context.drawTexture(BuildViewScreen.SLOT_BACKGROUND, x, y, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
        context.drawItem(stack, x, y);
        context.drawItemInSlot(textRenderer, stack, x, y);

        if (mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE) {
            if (!stack.isEmpty()) {
                context.drawItemTooltip(textRenderer, stack, mouseX, mouseY);
            } else if (pair.getLeft() != null && pair.getLeft().inventory() != null) {
                var slotType = pair.getLeft().inventory().getSlotType();
                String slotName = slotType.getGroup() + "/" + slotType.getName();
                context.drawTooltip(textRenderer, Text.literal(slotName + " (Empty)"), mouseX, mouseY);
            } else if (DEBUG_SLOT_AREAS) {
                context.drawTooltip(textRenderer, Text.literal("Dummy Slot"), mouseX, mouseY);
            }
        }
    }




    private record Position(int dx, int dy) {}

    private static List<Position> generateLayout(int count) {
        int spacing = SLOT_SIZE + SLOT_PADDING;
        List<Position> pos = new java.util.ArrayList<>();

        switch (count) {
            case 1 -> pos.add(new Position(0, 0));
            case 2 -> {
                pos.add(new Position(-spacing / 2, 0));
                pos.add(new Position(spacing / 2, 0));
            }
            case 3 -> {
                pos.add(new Position(0, -spacing));
                pos.add(new Position(-spacing, spacing / 2));
                pos.add(new Position(spacing, spacing / 2));
            }
            case 4 -> {
                pos.add(new Position(-spacing / 2, -spacing / 2));
                pos.add(new Position(spacing / 2, -spacing / 2));
                pos.add(new Position(-spacing / 2, spacing / 2));
                pos.add(new Position(spacing / 2, spacing / 2));
            }
            case 5 -> {
                pos.add(new Position(0, -spacing * 2 / 3));
                pos.add(new Position(-spacing, 0));
                pos.add(new Position(spacing, 0));
                pos.add(new Position(-spacing / 2, spacing));
                pos.add(new Position(spacing / 2, spacing));
            }
            case 6 -> {
                pos.add(new Position(0, -spacing));
                pos.add(new Position(-spacing, 0));
                pos.add(new Position(spacing, 0));
                pos.add(new Position(-spacing, spacing));
                pos.add(new Position(0, spacing));
                pos.add(new Position(spacing, spacing));
            }
            default -> {
                // Circle layout or rows for 7+
                double angleStep = 2 * Math.PI / count;
                int radius = spacing;
                for (int i = 0; i < count; i++) {
                    double angle = i * angleStep;
                    int dx = (int) (Math.cos(angle) * radius);
                    int dy = (int) (Math.sin(angle) * radius);
                    pos.add(new Position(dx, dy));
                }
            }
        }
        return pos;
    }

    public static int getTotalPages(PlayerEntity entity) {
        return 1;
    }
}

