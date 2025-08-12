    package net.pixeldreamstudios.showmeyourbuild.client.gui;

    import com.mojang.blaze3d.systems.RenderSystem;
    import dev.emi.trinkets.api.TrinketsApi;
    import net.fabricmc.api.EnvType;
    import net.fabricmc.api.Environment;
    import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
    import net.minecraft.client.MinecraftClient;
    import net.minecraft.client.gui.DrawContext;
    import net.minecraft.client.gui.screen.Screen;
    import net.minecraft.client.sound.PositionedSoundInstance;
    import net.minecraft.entity.effect.StatusEffectInstance;
    import net.minecraft.entity.player.PlayerEntity;
    import net.minecraft.item.ItemStack;
    import net.minecraft.nbt.NbtCompound;
    import net.minecraft.nbt.NbtElement;
    import net.minecraft.sound.SoundEvents;
    import net.minecraft.text.Text;
    import net.minecraft.util.Identifier;
    import net.minecraft.util.math.MathHelper;
    import net.pixeldreamstudios.attributepanel.api.AttributePanelAPI;
    import net.pixeldreamstudios.showmeyourbuild.Showmeyourbuild;
    import net.pixeldreamstudios.showmeyourbuild.client.BonusDataStore;
    import net.pixeldreamstudios.showmeyourbuild.client.LiveEffectStore;
    import net.pixeldreamstudios.showmeyourbuild.client.renderer.*;
    import net.pixeldreamstudios.showmeyourbuild.client.renderer.stats.StatsViewRenderer;
    import net.pixeldreamstudios.showmeyourbuild.network.CategoryCache;
    import net.pixeldreamstudios.showmeyourbuild.network.payload.OpenSkillsPayload;
    import net.pixeldreamstudios.showmeyourbuild.network.payload.RequestLiveEffectsPayload;
    import net.pixeldreamstudios.showmeyourbuild.util.ModCompat;

    import java.util.List;
    @Environment(EnvType.CLIENT)
    public class BuildViewScreen extends Screen {
        public PlayerSnapshot.SnapshotData snapshot = null;
        private long statsToggleTime = 0;
        private static final int BOUNCE_HEIGHT = 5;
        private long lastEffectRequestTime = 0;
        private boolean wasHoveringPotions = false;
        private static final long EFFECT_REQUEST_COOLDOWN_MS = 1000;
        public static final Identifier BACKGROUND_TEXTURE = Identifier.of("showmeyourbuild", "textures/gui/gui2.png");
        public static final Identifier SLOT_BACKGROUND = Identifier.of("showmeyourbuild", "textures/gui/slot_gui.png");
        public static final Identifier SLOT_BACKGROUND_ACCESSORY = Identifier.of("showmeyourbuild", "textures/gui/slot_gui3.png");
        public static final Identifier SKILL_BUTTON = Identifier.of("showmeyourbuild", "textures/button/sword16x16.png");
        public static final Identifier STATS_BUTTON = Identifier.of("showmeyourbuild", "textures/button/stat_button.png");
        public static final Identifier STATS_SLOT = Identifier.of("showmeyourbuild", "textures/gui/stat_slot.png");
        public static final Identifier BUILD_BUTTON = Identifier.of("showmeyourbuild", "textures/button/bag.png");
        public static final Identifier POTIONS_VIEWER = Identifier.of("showmeyourbuild", "textures/button/potions.png");
        public static final Identifier BONUSES_VIEWER = Identifier.of("showmeyourbuild", "textures/button/apple.png");

        public final PlayerEntity player;
        private boolean showPotionOverlay = false;
        private boolean showBonusPanel = false;
        private final String localPlayerName = MinecraftClient.getInstance().getSession().getUsername();

        public PlayerEntity snapshotPlayer = null;
        public ItemStack[] armorStacks = null;
        public ItemStack mainHand = ItemStack.EMPTY;
        public ItemStack offHand = ItemStack.EMPTY;
        public String displayNameOverride = null;
        public float modelYaw = 0;
        public boolean dragging = false;
        public double lastMouseX;
        private enum ViewMode {
            BUILD,
            STATS
        }

        private ViewMode currentView = ViewMode.BUILD;
        public BuildViewScreen(PlayerEntity player) {
            super(Text.literal("Build Viewer"));
            this.player = player;
            this.displayNameOverride = player.getName().getString();
            this.armorStacks = player.getInventory().armor.toArray(new ItemStack[0]);
            this.mainHand = player.getMainHandStack();
            this.offHand = player.getOffHandStack();

            if (ModCompat.ATTRIBUTE_PANEL_LOADED) {
                NbtCompound attrNbt = AttributePanelAPI.getAttributeSnapshot(player);
                StatsViewRenderer.setLiveTargetPlayer(player);
                StatsViewRenderer.loadAttributes(attrNbt);
                BonusDataStore.loadFromNbt(attrNbt);
            }
        }



        public BuildViewScreen(NbtCompound data) {
            this(data.getString("Name"), data);
        }

        public BuildViewScreen(String playerName, NbtCompound data) {
            super(Text.literal("Build Viewer"));
            this.player = MinecraftClient.getInstance().player;

            this.snapshot = PlayerSnapshot.fromNbt(data, playerName, player);

            if (data.contains("Attributes", NbtElement.COMPOUND_TYPE)) {
                NbtCompound attrNbt = data.getCompound("Attributes");
                StatsViewRenderer.loadAttributes(attrNbt, true);
                BonusDataStore.loadFromNbt(attrNbt);
            }
    
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
            if (accessoryRows > 1) {
                extraHeight = 25;
            } else if (accessoryRows == 1) {
                extraHeight = 15;
            }
            int baseHeight = 152;
            int textureHeight = baseHeight + extraHeight;
            int verticalOffset = -textureHeight / 2;
    
            RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
            context.drawTexture(BACKGROUND_TEXTURE, centerX - 128, centerY + verticalOffset, 0, 0, 256, textureHeight, 256, textureHeight);
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

            if (hoveringSkill & ModCompat.PUFFISH_LOADED) {
                context.drawTooltip(textRenderer, Text.translatable("gui.showmeyourbuild.skills"), mouseX, mouseY);
            } else if (hoveringSkill & !ModCompat.PUFFISH_LOADED) {
                context.drawTooltip(textRenderer, Text.translatable("gui.showmeyourbuild.install_puffish_skills"), mouseX, mouseY);
            }

            int statsBtnX = centerX + 120 - 21;
            int statsBtnY = centerY + verticalOffset + 20;
            int statsBtnSize = 16;
    
            boolean hoveringStats = mouseX >= statsBtnX && mouseX < statsBtnX + statsBtnSize &&
                    mouseY >= statsBtnY && mouseY < statsBtnY + statsBtnSize;
    
            RenderSystem.setShaderColor(
                    hoveringStats ? 1.25f : 1.0f,
                    hoveringStats ? 1.25f : 1.0f,
                    hoveringStats ? 1.25f : 1.0f,
                    1.0f
            );
    
            Identifier rightButtonIcon = (currentView == ViewMode.BUILD) ? STATS_BUTTON : BUILD_BUTTON;
            RenderSystem.setShaderTexture(0, rightButtonIcon);
            context.drawTexture(rightButtonIcon, statsBtnX, statsBtnY, 0, 0, 16, 16, 16, 16);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            if (hoveringStats) {
                context.drawTooltip(
                        textRenderer,
                        Text.translatable(currentView == ViewMode.BUILD ? "gui.showmeyourbuild.stats" : "gui.showmeyourbuild.build"),
                        mouseX,
                        mouseY
                );
            }
    
    
            int adjustedCenterY = centerY + verticalOffset + 76;
            boolean hoveringPotions = false;
            if (currentView == ViewMode.BUILD) {
                SlotRenderer.renderSlots(
                        context,
                        player,
                        snapshotPlayer,
                        armorStacks,
                        snapshot != null ? mainHand : player.getMainHandStack(),
                        snapshot != null ? offHand : player.getOffHandStack(),
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
                int potionsBtnX = centerX + 120 - 21;
                int potionsBtnY = centerY - textureHeight / 2 + 48;
    
                hoveringPotions = mouseX >= potionsBtnX && mouseX < potionsBtnX + 16 &&
                        mouseY >= potionsBtnY && mouseY < potionsBtnY + 16;
    
                RenderSystem.setShaderColor(hoveringPotions ? 1.25f : 1.0f, hoveringPotions ? 1.25f : 1.0f, hoveringPotions ? 1.25f : 1.0f, 1.0f);
                RenderSystem.setShaderTexture(0, POTIONS_VIEWER);
                context.drawTexture(POTIONS_VIEWER, potionsBtnX, potionsBtnY, 0, 0, 16, 16, 16, 16);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    

                int bonusBtnX = centerX - 120 + 5;
                int bonusBtnY = centerY - textureHeight / 2 + 48;
                boolean hoveringBonus = mouseX >= bonusBtnX && mouseX < bonusBtnX + 16 &&
                        mouseY >= bonusBtnY && mouseY < bonusBtnY + 16;
    
                RenderSystem.setShaderColor(hoveringBonus ? 1.25f : 1.0f, hoveringBonus ? 1.25f : 1.0f, hoveringBonus ? 1.25f : 1.0f, 1.0f);
                RenderSystem.setShaderTexture(0, BONUSES_VIEWER);
                context.drawTexture(BONUSES_VIEWER, bonusBtnX, bonusBtnY, 0, 0, 16, 16, 16, 16);
    
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

                if (hoveringBonus) {
                    context.drawTooltip(textRenderer, Text.translatable("gui.showmeyourbuild.attribute_bonuses"), mouseX, mouseY);
                }

                if (showBonusPanel) {
                    if (snapshot == null && ModCompat.ATTRIBUTE_PANEL_LOADED) {
                        NbtCompound liveAttrs = AttributePanelAPI.getAttributeSnapshot(player);
                        StatsViewRenderer.loadAttributes(liveAttrs);
                    }

                    final int bonusPanelWidth = 100;
                    final int panelX = centerX - 128 - bonusPanelWidth - 4;
                    final int panelY = bonusBtnY;

                    BonusesPanelRenderer.render(context, panelX, panelY, bonusPanelWidth, textRenderer, mouseX, mouseY);
                }

                BuildViewModelRenderer.drawEntity(centerX, adjustedCenterY + 55, 50, modelYaw, snapshotPlayer != null ? snapshotPlayer : player);

                if(snapshot != null) {
                    String name = displayNameOverride != null ? displayNameOverride : player.getName().getString();
                    context.drawTextWithShadow(
                            textRenderer,
                            Text.translatable("gui.showmeyourbuild.player_build", name),
                            centerX - (textRenderer.getWidth(Text.translatable("gui.showmeyourbuild.player_build", name)) / 2),
                            adjustedCenterY - 55,
                            0xFFFFFF
                    );
                }
            } else if (currentView == ViewMode.STATS) {
                if (snapshot == null && ModCompat.ATTRIBUTE_PANEL_LOADED) {
                    NbtCompound attrNbt = AttributePanelAPI.getAttributeSnapshot(player);
                    StatsViewRenderer.loadAttributes(attrNbt);
                    BonusDataStore.loadFromNbt(attrNbt);
                }
                StatsViewRenderer.render(context, centerX, centerY, textRenderer, mouseX, mouseY);

                long elapsed = System.currentTimeMillis() - statsToggleTime;
                final int duration = 10000;
                if (elapsed < duration) {
                    float bouncePhase = (elapsed % 1000) / 1000f;
                    double bounce = Math.sin(bouncePhase * Math.PI * 2) * BOUNCE_HEIGHT;
    
                    float alphaRatio = 1.0f - (elapsed / (float) duration);
                    alphaRatio = MathHelper.clamp(alphaRatio, 0.0f, 1.0f);
                    int alpha = (int) (alphaRatio * 255);
                    int color = (alpha << 24) | 0xFFFFFF;
                    String msg = Text.translatable("gui.showmeyourbuild.scroll_for_more").getString();
                    int msgWidth = textRenderer.getWidth(msg);
                    int msgX = centerX - msgWidth / 2;
                    int msgY = centerY + 90 + (int) bounce;
    
                    context.drawTextWithShadow(textRenderer, msg, msgX, msgY, color);
                }
            }

            if (hoveringPotions && snapshotPlayer == null && !displayNameOverride.equals(localPlayerName)) {

                long now = System.currentTimeMillis();

                if (now - lastEffectRequestTime > EFFECT_REQUEST_COOLDOWN_MS) {
                    lastEffectRequestTime = now;
                    ClientPlayNetworking.send(new RequestLiveEffectsPayload(displayNameOverride));
                }
            }


            if (hoveringPotions) {
                if (!wasHoveringPotions) {
                    MinecraftClient.getInstance().getSoundManager().play(
                            PositionedSoundInstance.master(SoundEvents.BLOCK_BREWING_STAND_BREW, 1f, 1f)
                    );
                }
                PlayerEntity effectSource = snapshotPlayer != null ? snapshotPlayer : player;
                boolean isSnapshot = snapshotPlayer != null;

                List<StatusEffectInstance> effects;
                if (snapshotPlayer == null && !displayNameOverride.equals(localPlayerName)) {
                    effects = LiveEffectStore.get(displayNameOverride);
                } else {
                    effects = effectSource.getStatusEffects().stream().toList();
                }

                if (!effects.isEmpty()) {
                    var spriteManager = MinecraftClient.getInstance().getStatusEffectSpriteManager();
    
                    int x = mouseX + 12;
                    int y = mouseY;
                    int iconSize = 18;
                    int padding = 4;
                    int lineHeight = 20;
    
                    int maxTextWidth = 0;
                    for (var effect : effects) {
                        var statusEffect = effect.getEffectType().value();
                        String name = Text.translatable(statusEffect.getTranslationKey()).getString();
                        int amp = effect.getAmplifier() + 1;
                        String duration = PotionEffectRenderer.formatDuration(effect.getDuration(), isSnapshot);
                        String fullText = name + " " + amp + " (" + duration + ")";
                        maxTextWidth = Math.max(maxTextWidth, textRenderer.getWidth(fullText));
                    }
    
                    int boxWidth = 22 + maxTextWidth + padding * 2;
                    int boxHeight = effects.size() * lineHeight + padding * 2;
                    int screenWidth = this.width;
                    int screenHeight = this.height;

                    if (x + boxWidth > screenWidth) {
                        x = screenWidth - boxWidth - 4;
                    }

                    if (y + boxHeight > screenHeight) {
                        y = screenHeight - boxHeight - 4;
                    }
                    context.fillGradient(
                            x - padding,
                            y - padding,
                            x - padding + boxWidth,
                            y - padding + boxHeight,
                            0xF0101010,
                            0xF0101010
                    );
    
                    for (var effect : effects) {
                        var statusEffectEntry = effect.getEffectType();
                        var statusEffect = statusEffectEntry.value();
                        var sprite = spriteManager.getSprite(statusEffectEntry);
    
                        String name = Text.translatable(statusEffect.getTranslationKey()).getString();
                        int amp = effect.getAmplifier() + 1;
                        String duration = PotionEffectRenderer.formatDuration(effect.getDuration(), isSnapshot);
    
                        if (sprite != null) {
                            RenderSystem.setShaderTexture(0, sprite.getAtlasId());
                            context.drawSprite(x, y + 1, 0, iconSize, iconSize, sprite);
                        }
    
                        context.drawText(
                                textRenderer,
                                Text.literal(name + " " + amp + " (" + duration + ")"),
                                x + 22,
                                y + 6,
                                0xFFFFFF,
                                false
                        );
    
                        y += lineHeight;
                    }
                } else {
                    context.drawTooltip(textRenderer, Text.translatable("gui.showmeyourbuild.no_active_effects"), mouseX, mouseY);
                }
            }
            wasHoveringPotions = hoveringPotions;
        }
    
        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (AccessorySlotRenderer.mouseClicked((int) mouseX, (int) mouseY)) return true;
    
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
            int extraHeight = (accessoryRows > 1) ? 25 : (accessoryRows == 1 ? 15 : 0);
            int baseHeight = 152;
            int textureHeight = baseHeight + extraHeight;
            int verticalOffset = -textureHeight / 2;

            if (ModCompat.PUFFISH_LOADED) {
                int skillBtnX = centerX - 120 + 5;
                int skillBtnY = centerY + verticalOffset + 20;
    
                if (button == 0 &&
                        mouseX >= skillBtnX && mouseX < skillBtnX + 16 &&
                        mouseY >= skillBtnY && mouseY < skillBtnY + 16) {
                    MinecraftClient.getInstance().getSoundManager().play(
                            PositionedSoundInstance.master(Showmeyourbuild.SWORD_UNSHEATH, 1.0f, 1.0f)
                    );
                    if (snapshot != null) {
                        var preloadedCategories = CategoryCache.getAll();
                        var maybeFirst = preloadedCategories.keySet().stream().findFirst();
                        ReadOnlySkillsScreen.open(preloadedCategories.values().stream().toList(), maybeFirst);
                    } else {
                        ClientPlayNetworking.send(new OpenSkillsPayload(displayNameOverride));
                    }
                    return true;
                }
            }

            int statsBtnX = centerX + 120 - 21;
            int statsBtnY = centerY + verticalOffset + 20;

            if (button == 0 &&
                    mouseX >= statsBtnX && mouseX < statsBtnX + 16 &&
                    mouseY >= statsBtnY && mouseY < statsBtnY + 16) {
                MinecraftClient.getInstance().getSoundManager().play(
                        PositionedSoundInstance.master(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f)
                );
                boolean switchingToStats = this.currentView == ViewMode.BUILD;
                this.currentView = switchingToStats ? ViewMode.STATS : ViewMode.BUILD;
                if (switchingToStats) statsToggleTime = System.currentTimeMillis();
                return true;
            }

            if (currentView == ViewMode.STATS) {
                StatsViewRenderer.handleClick((int) mouseX, (int) mouseY, centerX, centerY);
    
                return true;
            }
            int bonusBtnX = centerX - 120 + 5;
            int bonusBtnY = centerY - textureHeight / 2 + 48;
            if (mouseX >= bonusBtnX && mouseX < bonusBtnX + 16 &&
                    mouseY >= bonusBtnY && mouseY < bonusBtnY + 16) {
                showBonusPanel = !showBonusPanel;
                MinecraftClient.getInstance().getSoundManager().play(
                        PositionedSoundInstance.master(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 0.8f, 1.2f)
                );
                return true;
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
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (currentView == ViewMode.STATS) {
                StatsViewRenderer.handleScroll(verticalAmount);
                return true;
            }
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
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
