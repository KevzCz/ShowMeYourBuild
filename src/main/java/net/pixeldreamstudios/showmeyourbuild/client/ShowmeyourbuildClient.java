package net.pixeldreamstudios.showmeyourbuild.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.pixeldreamstudios.showmeyourbuild.client.command.ShowBuildCommand;
import net.pixeldreamstudios.showmeyourbuild.client.data.AttributeOverrideLoader;
import net.pixeldreamstudios.showmeyourbuild.client.gui.BuildViewScreen;
import net.pixeldreamstudios.showmeyourbuild.network.ClientNetwork;
import net.pixeldreamstudios.showmeyourbuild.network.payload.RequestSendBuildPayload;
import org.lwjgl.glfw.GLFW;
@Environment(EnvType.CLIENT)
public class ShowmeyourbuildClient implements ClientModInitializer {
    private static KeyBinding openBuildScreenKey;
    private static KeyBinding sendBuildKey;
    private static KeyBinding InspectKey;

    private boolean InspectKeyPressed = false;
    private long lastSendTime = 0;
    private static final long SEND_COOLDOWN_MS = 10_000;

    @Override
    public void onInitializeClient() {
        ClientNetwork.register();
        SpyglassHighlighter.init();
        openBuildScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.showmeyourbuild.open_build_screen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.showmeyourbuild"
        ));
        sendBuildKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.showmeyourbuild.send_build",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                "category.showmeyourbuild"
        ));
        InspectKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.showmeyourbuild.inspect_build",
                InputUtil.Type.MOUSE,
                GLFW.GLFW_KEY_V,
                "category.showmeyourbuild"
        ));

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(new AttributeOverrideLoader());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.crosshairTarget == null) return;

            boolean isPressed = InspectKey.isPressed();

            if (isPressed && !InspectKeyPressed) {
                boolean usingSpyglass = client.player.isUsingItem() &&
                        client.player.getActiveItem().getItem() == net.minecraft.item.Items.SPYGLASS;

                if (usingSpyglass) {
                    Entity target = SpyglassHighlighter.getCurrentTarget();
                    if (target instanceof net.minecraft.entity.player.PlayerEntity otherPlayer) {
                        client.setScreen(new BuildViewScreen(otherPlayer));
                        InspectKeyPressed = isPressed;
                        return;
                    }
                }

                if (client.crosshairTarget.getType() == net.minecraft.util.hit.HitResult.Type.ENTITY) {
                    var entityHit = ((net.minecraft.util.hit.EntityHitResult) client.crosshairTarget).getEntity();
                    if (entityHit instanceof net.minecraft.entity.player.PlayerEntity otherPlayer) {
                        client.setScreen(new BuildViewScreen(otherPlayer));
                    }
                }
            }


            InspectKeyPressed = isPressed;
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openBuildScreenKey.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new BuildViewScreen(client.player));
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (sendBuildKey.wasPressed()) {
                long now = System.currentTimeMillis();

                if (now - lastSendTime < SEND_COOLDOWN_MS) {
                    long secondsLeft = (SEND_COOLDOWN_MS - (now - lastSendTime)) / 1000;
                    client.player.sendMessage(Text.literal("§cPlease wait " + secondsLeft + "s before sending again."), true);
                } else {
                    lastSendTime = now;
                    if (client.player != null) {
                        ClientPlayNetworking.send(new RequestSendBuildPayload());
                        client.player.sendMessage(Text.literal("§aBuild sent to all players."), true);
                    }
                }
            }
        });

        ShowBuildCommand.register();
    }
}
