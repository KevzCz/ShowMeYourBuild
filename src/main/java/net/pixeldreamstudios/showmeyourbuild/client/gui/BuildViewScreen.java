package net.pixeldreamstudios.showmeyourbuild.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.showmeyourbuild.client.renderer.AccessorySlotRenderer;
import net.pixeldreamstudios.showmeyourbuild.client.renderer.BuildViewModelRenderer;
import net.pixeldreamstudios.showmeyourbuild.client.renderer.PlayerSnapshot;
import net.pixeldreamstudios.showmeyourbuild.client.renderer.SlotRenderer;
import net.pixeldreamstudios.showmeyourbuild.util.ModCompat;

public class BuildViewScreen extends Screen {

    public static final Identifier BACKGROUND_TEXTURE = Identifier.of("showmeyourbuild", "textures/gui/gui2.png");
    public static final Identifier SLOT_BACKGROUND = Identifier.of("showmeyourbuild", "textures/gui/slot_gui.png");

    public final PlayerEntity player;
    public PlayerEntity snapshotPlayer = null;
    public ItemStack[] armorStacks = null;
    public ItemStack mainHand = ItemStack.EMPTY;
    public ItemStack offHand = ItemStack.EMPTY;
    public String displayNameOverride = null;
    private ToggleIcon toggleIcon;
    public float modelYaw = 0;
    public boolean dragging = false;
    public double lastMouseX;
    private static boolean showingAccessories = false;
    private int currentAccessoryPage = 0;

    public BuildViewScreen(PlayerEntity player) {
        super(Text.literal("Build Viewer"));
        this.player = player;
        this.displayNameOverride = player.getName().getString();
        this.armorStacks = player.getInventory().armor.toArray(new ItemStack[0]);
        this.mainHand = player.getMainHandStack();
        this.offHand = player.getOffHandStack();
    }

    public BuildViewScreen(NbtCompound data) {
        this(data.getString("Name"), data);
    }

    public BuildViewScreen(String playerName, NbtCompound data) {
        super(Text.literal("Build Viewer"));
        this.player = MinecraftClient.getInstance().player;

        var snapshot = PlayerSnapshot.fromNbt(data, playerName, player);
        this.snapshotPlayer = snapshot.player();
        this.armorStacks = snapshot.armor();
        this.mainHand = snapshot.mainHand();
        this.offHand = snapshot.offHand();
        this.displayNameOverride = playerName;
    }



    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        if (ModCompat.TRINKETS_LOADED) {
            PlayerEntity target = snapshotPlayer != null ? snapshotPlayer : player;

            boolean hasAccessories = TrinketsApi.getTrinketComponent(target)
                    .map(component -> !component.getAllEquipped().isEmpty())
                    .orElse(false);

            if (hasAccessories) {
                int iconX = centerX - 128 + 12;
                int iconY = centerY - 76 + 12;
                toggleIcon = new ToggleIcon(iconX, iconY,
                        () -> showingAccessories,
                        newState -> showingAccessories = newState
                );
            }
        }


    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        super.render(context, mouseX, mouseY, delta);
        // Draw background
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        context.drawTexture(BACKGROUND_TEXTURE, centerX - 128, centerY - 76, 0, 0, 256, 152, 256, 152);

        if (showingAccessories) {
            int totalPages = AccessorySlotRenderer.getTotalPages(snapshotPlayer != null ? snapshotPlayer : player);
            AccessorySlotRenderer.renderAccessorySlots(
                    context,
                    snapshotPlayer != null ? snapshotPlayer : player,
                    centerX,
                    centerY,
                    textRenderer,
                    mouseX,
                    mouseY,
                    currentAccessoryPage // track page state if needed
            );

        } else {
            SlotRenderer.renderSlots(
                    context,
                    player,
                    snapshotPlayer,
                    armorStacks,
                    mainHand,
                    offHand,
                    centerX,
                    centerY,
                    this.textRenderer,
                    mouseX,
                    mouseY
            );
        }


        // Draw 3D model
        BuildViewModelRenderer.drawEntity(centerX, centerY + 55, 50, modelYaw, snapshotPlayer != null ? snapshotPlayer : player);

        // Draw name
        String name = displayNameOverride != null ? displayNameOverride : player.getName().getString();
        String title = name + "'s Build";

        int titleWidth = textRenderer.getWidth(title);
        context.drawTextWithShadow(
                textRenderer,
                Text.literal(title),
                centerX - (titleWidth / 2),  // center horizontally
                centerY - 55,
                0xFFFFFF
        );

        if (toggleIcon != null) {
            toggleIcon.render(context, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (toggleIcon != null && toggleIcon.mouseClicked(mouseX, mouseY, button)) {
            return true; // Handle toggle icon first
        }

        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (button == 0) {
            dragging = true;
            lastMouseX = mouseX;
            return true;
        }

        return false;
    }



    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            dragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging && button == 0) {
            modelYaw += (float) (mouseX - lastMouseX) * 0.5f;
            lastMouseX = mouseX;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
