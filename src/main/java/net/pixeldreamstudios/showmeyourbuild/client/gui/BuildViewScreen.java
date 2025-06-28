package net.pixeldreamstudios.showmeyourbuild.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.showmeyourbuild.client.renderer.AccessorySlotRenderer;
import net.pixeldreamstudios.showmeyourbuild.client.renderer.BuildViewModelRenderer;
import net.pixeldreamstudios.showmeyourbuild.client.renderer.PlayerSnapshot;
import net.pixeldreamstudios.showmeyourbuild.client.renderer.SlotRenderer;
import net.pixeldreamstudios.showmeyourbuild.network.CategoryCache;
import net.pixeldreamstudios.showmeyourbuild.network.ClientToServerSkillTreeRequest;
import net.pixeldreamstudios.showmeyourbuild.network.payload.OpenSkillsPayload;
import net.pixeldreamstudios.showmeyourbuild.util.ModCompat;

import java.util.List;

public class BuildViewScreen extends Screen {
    public PlayerSnapshot.SnapshotData snapshot = null;
    public static final Identifier BACKGROUND_TEXTURE = Identifier.of("showmeyourbuild", "textures/gui/gui2.png");
    public static final Identifier SLOT_BACKGROUND = Identifier.of("showmeyourbuild", "textures/gui/slot_gui.png");
    public static final Identifier SLOT_BACKGROUND_ACCESSORY = Identifier.of("showmeyourbuild", "textures/gui/slot_gui3.png");
    public static final Identifier SKILL_BUTTON = Identifier.of("showmeyourbuild", "textures/button/sword16x16.png");

    public final PlayerEntity player;
    public PlayerEntity snapshotPlayer = null;
    public ItemStack[] armorStacks = null;
    public ItemStack mainHand = ItemStack.EMPTY;
    public ItemStack offHand = ItemStack.EMPTY;
    public String displayNameOverride = null;
    public float modelYaw = 0;
    public boolean dragging = false;
    public double lastMouseX;
    public static boolean debug_message = false;
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

        this.snapshot = PlayerSnapshot.fromNbt(data, playerName, player);
        this.snapshotPlayer = snapshot.player();
        this.armorStacks = snapshot.armor();
        this.mainHand = snapshot.mainHand();
        this.offHand = snapshot.offHand();
        this.displayNameOverride = playerName;
        if (ModCompat.PUFFISH_LOADED && data.contains("Skills")) {
            var skillData = data.getCompound("Skills");
            var categories = net.pixeldreamstudios.showmeyourbuild.client.SkillTreeSnapshotLoader.load(skillData);
            CategoryCache.clear();
            categories.values().forEach(net.pixeldreamstudios.showmeyourbuild.network.CategoryCache::put);
        }
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        super.render(context, mouseX, mouseY, delta);

        PlayerEntity target = snapshotPlayer != null ? snapshotPlayer : player;

        List<ItemStack> accessoriesToRender = null;
        int accessoryCount = 0;

        if (snapshot != null) {
            accessoriesToRender = snapshot.accessories();
            accessoryCount = accessoriesToRender != null ? accessoriesToRender.size() : 0;
        } else if (ModCompat.TRINKETS_LOADED) {
            accessoriesToRender = TrinketsApi.getTrinketComponent(player)
                    .map(c -> c.getAllEquipped().stream().map(p -> p.getRight()).toList())
                    .orElse(null);
            accessoryCount = accessoriesToRender != null ? accessoriesToRender.size() : 0;
        }

        boolean hasAccessories = accessoryCount > 0;



        int accessoryRows = (int) Math.ceil(Math.min(accessoryCount, 20) / 10.0);
        int extraHeight = 0;
        if (accessoryRows > 1)
        {
            extraHeight =  25;
        }
        else if (accessoryRows == 1)
        {
            extraHeight = 15;
        }
        int baseHeight = 152;
        int textureHeight = baseHeight + extraHeight;
        int verticalOffset = -textureHeight / 2;

        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        context.drawTexture(BACKGROUND_TEXTURE, centerX - 128, centerY + verticalOffset, 0, 0, 256, textureHeight, 256, textureHeight);
        if (ModCompat.PUFFISH_LOADED) {

            int skillBtnX = centerX - 120 + 5;
            int skillBtnY = centerY + verticalOffset + 20;
            int skillBtnSize = 16;

            boolean hoveringSkill = mouseX >= skillBtnX && mouseX < skillBtnX + skillBtnSize &&
                    mouseY >= skillBtnY && mouseY < skillBtnY + skillBtnSize;

            if (hoveringSkill) {
                RenderSystem.setShaderColor(1.25f, 1.25f, 1.25f, 1.0f);
            } else {
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }

            RenderSystem.setShaderTexture(0, SKILL_BUTTON);
            context.drawTexture(SKILL_BUTTON, skillBtnX, skillBtnY, 0, 0, 16, 16, 16, 16);

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            if (hoveringSkill) {
                context.drawTooltip(textRenderer, Text.literal("Skills"), mouseX, mouseY);
            }
        }


        int adjustedCenterY = centerY + verticalOffset + 76;

        SlotRenderer.renderSlots(
                context,
                player,
                snapshotPlayer,
                armorStacks,
                snapshot != null ? mainHand : player.getMainHandStack(),
                snapshot != null ? offHand  : player.getOffHandStack(),
                centerX,
                adjustedCenterY,
                textRenderer,
                mouseX,
                mouseY
        );


        if (hasAccessories) {
            int accessoryAreaTop = adjustedCenterY + 57;
            int accessoryAreaHeight = 40;
            int accessoryAreaCenterY = accessoryAreaTop + accessoryAreaHeight / 2;

            if (debug_message) System.out.println("[BuildViewScreen] Rendering accessories. Snapshot size: " + (accessoriesToRender != null ? accessoriesToRender.size() : "null"));

            AccessorySlotRenderer.render(
                    context,
                    target,
                    accessoriesToRender,
                    centerX,
                    accessoryAreaCenterY,
                    textRenderer,
                    mouseX,
                    mouseY
            );
        }


        BuildViewModelRenderer.drawEntity(centerX, adjustedCenterY + 55, 50, modelYaw, snapshotPlayer != null ? snapshotPlayer : player);

        String name = displayNameOverride != null ? displayNameOverride : player.getName().getString();
        String title = name + "'s Build";
        int titleWidth = textRenderer.getWidth(title);

        context.drawTextWithShadow(
                textRenderer,
                Text.literal(title),
                centerX - (titleWidth / 2),
                adjustedCenterY - 55,
                0xFFFFFF
        );

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (AccessorySlotRenderer.mouseClicked((int) mouseX, (int) mouseY)) return true;

        if (ModCompat.PUFFISH_LOADED) {
            int centerX = this.width / 2;
            int centerY = this.height / 2;


            int accessoryCount = 0;
            if (snapshot != null) {
                accessoryCount = snapshot.accessories() != null ? snapshot.accessories().size() : 0;
            } else if (ModCompat.TRINKETS_LOADED) {
                accessoryCount = TrinketsApi.getTrinketComponent(player)
                        .map(c -> c.getAllEquipped().size())
                        .orElse(0);
            }

            int accessoryRows = (int) Math.ceil(Math.min(accessoryCount, 20) / 10.0);
            int extraHeight = 0;
            if (accessoryRows > 1) {
                extraHeight = 25;
            } else if (accessoryRows == 1) {
                extraHeight = 15;
            }

            int baseHeight = 152;
            int textureHeight = baseHeight + extraHeight;
            int verticalOffset = -textureHeight / 2;

            int skillBtnX = centerX - 120 + 5;
            int skillBtnY = centerY + verticalOffset + 20;

            if (button == 0 &&
                    mouseX >= skillBtnX && mouseX < skillBtnX + 16 &&
                    mouseY >= skillBtnY && mouseY < skillBtnY + 16) {

                if (snapshot != null) {
                    var preloadedCategories = CategoryCache.getAll();
                    var maybeFirst = preloadedCategories.keySet().stream().findFirst();
                    ReadOnlySkillsScreen.open(preloadedCategories.values().stream().toList(), maybeFirst);
                }
                else {

                    ClientPlayNetworking.send(new OpenSkillsPayload(displayNameOverride));
                }
                return true;
            }
        }

        if (super.mouseClicked(mouseX, mouseY, button)) return true;
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
