package net.pixeldreamstudios.showmeyourbuild.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.pixeldreamstudios.showmeyourbuild.client.command.ShowBuildCommand;
import net.pixeldreamstudios.showmeyourbuild.client.gui.BuildViewScreen;
import net.pixeldreamstudios.showmeyourbuild.network.payload.RequestSendBuildPayload;
import org.lwjgl.glfw.GLFW;

public class ShowmeyourbuildClient implements ClientModInitializer {
    private static KeyBinding openBuildScreenKey;
    private static KeyBinding sendBuildKey;
    private static KeyBinding middleClickInspectKey;
    private boolean wasMiddleKeyPressed = false;
    @Override
    public void onInitializeClient() {
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
        middleClickInspectKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.showmeyourbuild.inspect_build",
                InputUtil.Type.MOUSE,
                GLFW.GLFW_MOUSE_BUTTON_MIDDLE,
                "category.showmeyourbuild"
        ));


        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.crosshairTarget == null) return;

            boolean isPressed = middleClickInspectKey.isPressed();

            if (isPressed && !wasMiddleKeyPressed) {
                if (client.crosshairTarget.getType() == net.minecraft.util.hit.HitResult.Type.ENTITY) {
                    var entityHit = ((net.minecraft.util.hit.EntityHitResult) client.crosshairTarget).getEntity();
                    if (entityHit instanceof net.minecraft.entity.player.PlayerEntity otherPlayer) {
                        client.setScreen(new BuildViewScreen(otherPlayer));
                        }
                }
            }

            wasMiddleKeyPressed = isPressed;
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
                if (client.player != null) {
                    ClientPlayNetworking.send(new RequestSendBuildPayload());
                }
            }
        });
        ShowBuildCommand.register();


    }
}
