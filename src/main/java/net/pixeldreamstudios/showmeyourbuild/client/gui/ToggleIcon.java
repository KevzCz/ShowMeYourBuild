package net.pixeldreamstudios.showmeyourbuild.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ToggleIcon {

    private static final Identifier ICON_BAG = Identifier.of("showmeyourbuild", "textures/button/bag.png");
    private static final Identifier ICON_STAR = Identifier.of("showmeyourbuild", "textures/button/star.png");

    private final int x, y, width, height;
    private final BooleanSupplier stateSupplier;
    private final Consumer<Boolean> onClick;

    public ToggleIcon(int x, int y, BooleanSupplier stateSupplier, Consumer<Boolean> onClick) {
        this.x = x;
        this.y = y;
        this.width = 16;
        this.height = 16;
        this.stateSupplier = stateSupplier;
        this.onClick = onClick;
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        boolean hovered = isHovered(mouseX, mouseY);
        Identifier icon = stateSupplier.getAsBoolean() ? ICON_BAG : ICON_STAR;

        RenderSystem.setShaderTexture(0, icon);
        if (hovered) {
            RenderSystem.setShaderColor(0.6f, 0.6f, 0.6f, 1f);
        }
        context.drawTexture(icon, x, y, 0, 0, width, height, width, height);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f); // reset

        if (hovered) {
            context.drawTooltip(
                    MinecraftClient.getInstance().textRenderer,
                    Text.literal(stateSupplier.getAsBoolean() ? "Show Armor" : "Show Accessories"),
                    mouseX, mouseY
            );
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            boolean newState = !stateSupplier.getAsBoolean();
            onClick.accept(newState);
            return true;
        }
        return false;
    }

    private boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
