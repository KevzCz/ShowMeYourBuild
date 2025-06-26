package net.pixeldreamstudios.showmeyourbuild.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.pixeldreamstudios.showmeyourbuild.client.BuildDataStore;
import net.pixeldreamstudios.showmeyourbuild.client.gui.BuildViewScreen;
import net.pixeldreamstudios.showmeyourbuild.network.payload.RequestSendBuildPayload;
import net.pixeldreamstudios.showmeyourbuild.network.payload.SendBuildSnapshotPayload;

public class ClientNetwork {
    public static void register() {
        PayloadTypeRegistry.playS2C().register(SendBuildSnapshotPayload.ID, SendBuildSnapshotPayload.CODEC);



        ClientPlayNetworking.registerGlobalReceiver(SendBuildSnapshotPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                String snapshotId = payload.snapshotId();
                String name = payload.playerName();
                NbtCompound data = payload.data();
                BuildDataStore.save(snapshotId, data);

                Text message = Text.literal(name + "'s Build → ")
                        .append(Text.literal("[Click to View]")
                                .styled(style -> style.withClickEvent(
                                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/internal_show_build " + snapshotId)
                                ))
                        );
                MinecraftClient.getInstance().player.sendMessage(message, false);


                // Save data somewhere accessible for command fallback
                BuildDataStore.save(name, data);
            });
        });
    }
}
