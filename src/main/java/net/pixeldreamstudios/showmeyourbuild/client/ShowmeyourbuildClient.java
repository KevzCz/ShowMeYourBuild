package net.pixeldreamstudios.showmeyourbuild.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.pixeldreamstudios.showmeyourbuild.Showmeyourbuild;
import net.pixeldreamstudios.showmeyourbuild.client.command.ShowBuildCommand;
import net.pixeldreamstudios.showmeyourbuild.network.BuildDataSerializer;
import net.pixeldreamstudios.showmeyourbuild.network.payload.RequestSendBuildPayload;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;

import net.minecraft.client.MinecraftClient;
import net.pixeldreamstudios.showmeyourbuild.client.gui.BuildViewScreen;

public class ShowmeyourbuildClient implements ClientModInitializer {
    private static KeyBinding openBuildScreenKey;
    private static KeyBinding sendBuildKey;
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
