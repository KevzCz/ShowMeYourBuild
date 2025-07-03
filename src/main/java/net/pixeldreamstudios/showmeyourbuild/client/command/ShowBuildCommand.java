package net.pixeldreamstudios.showmeyourbuild.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.pixeldreamstudios.showmeyourbuild.client.BuildDataStore;
import net.pixeldreamstudios.showmeyourbuild.client.gui.BuildViewScreen;

public class ShowBuildCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            register(dispatcher);
        });
    }

    private static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                ClientCommandManager.literal("internal_show_build")
                        .then(ClientCommandManager.argument("id", StringArgumentType.word())
                                .executes(ctx -> {
                                    String snapshotId = StringArgumentType.getString(ctx, "id");
                                    NbtCompound data = BuildDataStore.get(snapshotId);

                                    if (data != null) {
                                        MinecraftClient.getInstance().setScreen(new BuildViewScreen(data));
                                    } else {
                                        MinecraftClient.getInstance().player.sendMessage(
                                                Text.literal("No build snapshot found for ID: " + snapshotId),
                                                false
                                        );
                                    }

                                    return 1;
                                })
                        )
        );
    }
}
