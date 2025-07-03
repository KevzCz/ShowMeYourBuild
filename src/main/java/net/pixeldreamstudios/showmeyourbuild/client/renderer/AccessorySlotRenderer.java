    package net.pixeldreamstudios.showmeyourbuild.client.renderer;

    import dev.emi.trinkets.api.SlotReference;
    import dev.emi.trinkets.api.TrinketsApi;
    import net.fabricmc.api.EnvType;
    import net.fabricmc.api.Environment;
    import net.minecraft.client.MinecraftClient;
    import net.minecraft.client.font.TextRenderer;
    import net.minecraft.client.gui.DrawContext;
    import net.minecraft.entity.player.PlayerEntity;
    import net.minecraft.item.ItemStack;
    import net.minecraft.text.Text;
    import net.minecraft.util.Identifier;
    import net.minecraft.util.Pair;

    import java.util.ArrayList;
    import java.util.List;
    @Environment(EnvType.CLIENT)
    public class AccessorySlotRenderer {
        public static final Identifier BACKGROUND_TEXTURE_OVERFLOW = Identifier.of("showmeyourbuild", "textures/gui/gui2.png");

        private static final int SLOT_SIZE = 16;
        private static final int SLOT_PADDING = 4;
        private static final int MAX_VISIBLE = 20;
        private static boolean overflowExpanded = false;
        private static int overflowX = 0;
        private static int overflowY = 0;
        private static int overflowButtonX = 0;
        private static int overflowButtonY = 0;
        private static List<ItemStack> overflowStacks = new ArrayList<>();
        public static boolean debug_message = false;
        public static void render(
                DrawContext context,
                PlayerEntity entity,
                List<ItemStack> snapshotAccessories,
                int centerX, int centerY,
                TextRenderer textRenderer, int mouseX, int mouseY) {

            List<Pair<SlotReference, ItemStack>> equipped = new ArrayList<>();

            if (snapshotAccessories != null && !snapshotAccessories.isEmpty()) {
                for (ItemStack stack : snapshotAccessories) {
                    equipped.add(new Pair<>(null, stack));
                }
            } else {
                TrinketsApi.getTrinketComponent(entity).ifPresent(component -> {
                    equipped.addAll(component.getAllEquipped());
                });
            }
//            if (snapshotAccessories != null) {
//                if (debug_message)
//                    System.out.println("[AccessorySlotRenderer] Using snapshot accessories: count=" + snapshotAccessories.size());
//                for (ItemStack stack : snapshotAccessories) {
//                    if (debug_message) System.out.println("  [AccessorySlotRenderer] Stack: " + stack);
//                }
//            } else {
//                if (debug_message)
//                    System.out.println("[AccessorySlotRenderer] No snapshot accessories, falling back to live Trinkets data.");
//            }
            if (equipped.isEmpty()) return;


            int visibleCount = Math.min(equipped.size(), MAX_VISIBLE);
            int hiddenCount = equipped.size() - MAX_VISIBLE;

            final int slotsPerRow = 10;
            final int spacing = SLOT_SIZE + SLOT_PADDING;

            int totalRows = (int) Math.ceil(visibleCount / (float) slotsPerRow);
            int totalHeight = totalRows * spacing;
            int yStart = centerY - (totalHeight / 2);

            overflowX = 0;
            overflowY = -10;
//            if (debug_message){
//                TrinketsApi.getTrinketComponent(entity).ifPresentOrElse(
//                        c -> System.out.println("[AccessorySlotRenderer] Trinket slots: " + c.getAllEquipped().size()),
//                        () -> System.out.println("[AccessorySlotRenderer] No trinket component found!")
//                );
//            }
                for (int i = 0; i < visibleCount; i++) {
                    int row = i / slotsPerRow;
                    int col = i % slotsPerRow;
    
                    int rowSize = Math.min(slotsPerRow, visibleCount - row * slotsPerRow);
                    int rowStartX = centerX - (rowSize * spacing) / 2;
                    int x = rowStartX + col * spacing;
                    int y = yStart + row * spacing;
    
                    if (i == visibleCount - 1) {
                        overflowX = x + spacing;
                        overflowY = y;
                    }
    
                    renderSlot(context, textRenderer, equipped.get(i), x, y, mouseX, mouseY);
                }
    

            if (hiddenCount > 0) {
                overflowStacks.clear();
                for (int i = MAX_VISIBLE; i < equipped.size(); i++) {
                    overflowStacks.add(equipped.get(i).getRight());
                }

                int x = overflowX;
                int y = overflowY - 10;
                overflowButtonX = x;
                overflowButtonY = y;


                boolean hovered = mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE;
                int bgColor = hovered ? 0xFF404040 : 0xAA000000;
                context.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, bgColor);


                String moreText = "+" + hiddenCount;
                int textX = x + (SLOT_SIZE - textRenderer.getWidth(moreText)) / 2;
                int textY = y + 4;
                context.drawTextWithShadow(textRenderer, moreText, textX, textY, 0xFFFFFF);


                if (hovered && !overflowExpanded) {
                    context.drawTooltip(textRenderer, Text.literal("Click to show more"), mouseX, mouseY);
                }


                if (overflowExpanded) {
                    drawOverflowPanel(context, textRenderer, mouseX, mouseY);
                }
            }



        }
        private static void drawOverflowPanel(DrawContext context, TextRenderer textRenderer,
                                              int mouseX, int mouseY) {
            final int slotSize = 16;
            final int padding = 4;
            final int columns = 5;
            int rows = (int) Math.ceil(overflowStacks.size() / (float) columns);

            int panelWidth = columns * (slotSize + padding) + padding;
            int panelHeight = rows * (slotSize + padding) + padding;

            int panelX = overflowX + SLOT_SIZE + 4;
            int panelY = overflowY;

            int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
            int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();

            if (panelX + panelWidth > screenWidth) {
                panelX = screenWidth - panelWidth - 4;
            }
            if (panelY + panelHeight > screenHeight) {
                panelY = screenHeight - panelHeight - 4;
            }
            if (panelX < 0) panelX = 4;
            if (panelY < 0) panelY = 4;

            MinecraftClient.getInstance().getTextureManager().bindTexture(BACKGROUND_TEXTURE_OVERFLOW);
            context.drawTexture(
                    BACKGROUND_TEXTURE_OVERFLOW,
                    panelX - 2, panelY - 2,
                    0, 0,
                    panelWidth + 4, panelHeight + 4,
                    panelWidth + 4, panelHeight + 4
            );

            for (int i = 0; i < overflowStacks.size(); i++) {
                int row = i / columns;
                int col = i % columns;

                int x = panelX + padding + col * (slotSize + padding);
                int y = panelY + padding + row * (slotSize + padding);

                ItemStack stack = overflowStacks.get(i);
                context.drawItem(stack, x, y);
                context.drawItemInSlot(textRenderer, stack, x, y);

                if (mouseX >= x && mouseX < x + slotSize && mouseY >= y && mouseY < y + slotSize) {
                    context.drawItemTooltip(textRenderer, stack, mouseX, mouseY);
                }
            }
        }


        public static boolean mouseClicked(int mouseX, int mouseY) {

            if (mouseX >= overflowButtonX && mouseX < overflowButtonX + SLOT_SIZE &&
                    mouseY >= overflowButtonY && mouseY < overflowButtonY + SLOT_SIZE) {
                overflowExpanded = !overflowExpanded;
                return true;
            }



            if (overflowExpanded) {
                final int slotSize = 16;
                final int padding = 4;
                final int columns = 5;
                int rows = (int) Math.ceil(overflowStacks.size() / (float) columns);
    
                int panelWidth = columns * (slotSize + padding) + padding;
                int panelHeight = rows * (slotSize + padding) + padding;
    
                int panelX = overflowX + SLOT_SIZE + 4;
                int panelY = overflowY;
    
                boolean inside = mouseX >= panelX && mouseX < panelX + panelWidth &&
                        mouseY >= panelY && mouseY < panelY + panelHeight;
    
                if (!inside) {
                    overflowExpanded = false;
                }
            }
    
            return false;
        }
    
        private static void renderSlot(DrawContext context, TextRenderer textRenderer,
                                       Pair<SlotReference, ItemStack> pair, int x, int y,
                                       int mouseX, int mouseY) {
            ItemStack stack = pair.getRight();
            context.drawTexture(
                    net.pixeldreamstudios.showmeyourbuild.client.gui.BuildViewScreen.SLOT_BACKGROUND_ACCESSORY,
                    x, y, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
            context.drawItem(stack, x, y);
            context.drawItemInSlot(textRenderer, stack, x, y);
    
            if (mouseX >= x && mouseX < x + SLOT_SIZE && mouseY >= y && mouseY < y + SLOT_SIZE) {
                context.drawItemTooltip(textRenderer, stack, mouseX, mouseY);
            }
        }
    }
