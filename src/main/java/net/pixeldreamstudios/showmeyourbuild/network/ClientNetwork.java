package net.pixeldreamstudios.showmeyourbuild.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.showmeyourbuild.client.BuildDataStore;
import net.pixeldreamstudios.showmeyourbuild.client.gui.ReadOnlySkillsScreen;
import net.pixeldreamstudios.showmeyourbuild.network.payload.SendBuildSnapshotPayload;
import net.pixeldreamstudios.showmeyourbuild.network.payload.SendLiveEffectsPayload;
import net.pixeldreamstudios.showmeyourbuild.network.payload.SendSkillTreeSnapshotPayload;

import java.util.Optional;
@Environment(EnvType.CLIENT)
public class ClientNetwork {
    public static void register() {
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

                // Save under snapshot ID and name
                BuildDataStore.save(snapshotId, data);
                BuildDataStore.save(name, data);

                // Create clickable chat message
                Text message = Text.literal("")
                        .append(Text.literal("[" + name + "'s Build]")
                                .styled(style -> style
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/internal_show_build " + snapshotId))
                                        .withColor(0x00AAD4)
                                        .withUnderline(true)
                                        .withHoverEvent(new net.minecraft.text.HoverEvent(
                                                net.minecraft.text.HoverEvent.Action.SHOW_TEXT,
                                                Text.literal("Click to view " + name + "'s Build")
                                        ))
                                )
                        );

                MinecraftClient.getInstance().player.sendMessage(message, false);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(SendLiveEffectsPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                String playerName = payload.playerName();
                NbtCompound effectData = payload.effectData();

                net.pixeldreamstudios.showmeyourbuild.client.LiveEffectStore.save(playerName, effectData);
            });
        });

    }
}
