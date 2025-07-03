package net.pixeldreamstudios.showmeyourbuild.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.pixeldreamstudios.showmeyourbuild.client.gui.BuildViewScreen;
@Environment(EnvType.CLIENT)
public class SlotRenderer {

    private static final int[][] ARMOR_SLOTS = new int[][] {
            {-57, -33},
            { 40, -33},
            {-57,  20},
            { 40,  20},
    };

    private static final int[] MAIN_HAND_SLOT = {-90, -15};
    private static final int[] OFF_HAND_SLOT  = { 58, -15};
    private static final int HAND_SLOT_SIZE = 32;

    public static void renderSlots(DrawContext context, PlayerEntity player, PlayerEntity snapshot,
                                   ItemStack[] armorStacks, ItemStack mainHand, ItemStack offHand,
                                   int centerX, int centerY, TextRenderer textRenderer, int mouseX, int mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();

        for (int i = 0; i < 4; i++) {
            int x = centerX + ARMOR_SLOTS[i][0];
            int y = centerY + ARMOR_SLOTS[i][1];

            context.drawTexture(
                    BuildViewScreen.SLOT_BACKGROUND,
                    x, y, 0, 0, 16, 16, 16, 16
            );

            ItemStack stack = (snapshot != null && armorStacks != null)
                    ? armorStacks[3 - i]
                    : player.getInventory().armor.get(3 - i);

            context.drawItem(stack, x, y);
            context.drawItemInSlot(textRenderer, stack, x, y);

            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                if (!stack.isEmpty()) {
                    context.drawItemTooltip(textRenderer, stack, mouseX, mouseY);
                } else {
                    String slotName = slotNameForArmor(3 - i);
                    context.drawTooltip(textRenderer, Text.literal(slotName + " (Empty)"), mouseX, mouseY);
                }
            }
        }

        int mx = centerX + MAIN_HAND_SLOT[0];
        int my = centerY + MAIN_HAND_SLOT[1];
        context.drawTexture(BuildViewScreen.SLOT_BACKGROUND, mx, my, 0, 0, HAND_SLOT_SIZE, HAND_SLOT_SIZE, HAND_SLOT_SIZE, HAND_SLOT_SIZE);
        context.drawItem(mainHand, mx + 8, my + 8);
        context.drawItemInSlot(textRenderer, mainHand, mx + 8, my + 8);

        if (mouseX >= mx && mouseX < mx + HAND_SLOT_SIZE && mouseY >= my && mouseY < my + HAND_SLOT_SIZE) {
            if (!mainHand.isEmpty()) {
                context.drawItemTooltip(textRenderer, mainHand, mouseX, mouseY);
            } else {
                context.drawTooltip(textRenderer, Text.literal("Main Hand (Empty)"), mouseX, mouseY);
            }
        }

        mx = centerX + OFF_HAND_SLOT[0];
        my = centerY + OFF_HAND_SLOT[1];
        context.drawTexture(BuildViewScreen.SLOT_BACKGROUND, mx, my, 0, 0, HAND_SLOT_SIZE, HAND_SLOT_SIZE, HAND_SLOT_SIZE, HAND_SLOT_SIZE);
        context.drawItem(offHand, mx + 8, my + 8);
        context.drawItemInSlot(textRenderer, offHand, mx + 8, my + 8);

        if (mouseX >= mx && mouseX < mx + HAND_SLOT_SIZE && mouseY >= my && mouseY < my + HAND_SLOT_SIZE) {
            if (!offHand.isEmpty()) {
                context.drawItemTooltip(textRenderer, offHand, mouseX, mouseY);
            } else {
                context.drawTooltip(textRenderer, Text.literal("Offhand (Empty)"), mouseX, mouseY);
            }
        }
    }

    private static String slotNameForArmor(int index) {
        return switch (index) {
            case 0 -> "Boots Slot";
            case 1 -> "Leggings Slot";
            case 2 -> "Chestplate Slot";
            case 3 -> "Helmet Slot";
            default -> "Armor Slot";
        };
    }
}
