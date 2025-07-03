package net.pixeldreamstudios.showmeyourbuild.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.showmeyourbuild.client.BuildDataStore;
import net.pixeldreamstudios.showmeyourbuild.client.gui.ReadOnlySkillsScreen;
import net.pixeldreamstudios.showmeyourbuild.network.payload.SendBuildSnapshotPayload;
import net.pixeldreamstudios.showmeyourbuild.network.payload.SendSkillTreeSnapshotPayload;

import java.util.Optional;

public class ClientNetwork {
    public static void register() {
        PayloadTypeRegistry.playS2C().register(SendBuildSnapshotPayload.ID, SendBuildSnapshotPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(SendSkillTreeSnapshotPayload.ID, SendSkillTreeSnapshotPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(SendSkillTreeSnapshotPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                String playerName = payload.playerName();
                NbtCompound data = payload.skillData();
                var categories = net.pixeldreamstudios.showmeyourbuild.client.SkillTreeSnapshotLoader.load(data);
                Optional<Identifier> maybeFirstId = categories.keySet().stream().findFirst();

                ReadOnlySkillsScreen.open(
                        categories.values().stream().toList(),
                        maybeFirstId
                );
            });
        });




        ClientPlayNetworking.registerGlobalReceiver(SendBuildSnapshotPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                String snapshotId = payload.snapshotId();
                String name = payload.playerName();
                NbtCompound data = payload.data();
                BuildDataStore.save(snapshotId, data);

                Text message = Text.literal("")
                        .append(Text.literal("["+name + "'s Build] ")
                                .styled(style -> style.withClickEvent(
                                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/internal_show_build " + snapshotId)
                                ))
                        );
                MinecraftClient.getInstance().player.sendMessage(message, false);

                BuildDataStore.save(name, data);
            });
        });

    }
}
