package net.pixeldreamstudios.showmeyourbuild.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.pixeldreamstudios.showmeyourbuild.network.payload.*;
import net.pixeldreamstudios.showmeyourbuild.util.ModCompat;
import net.puffish.skillsmod.api.SkillsAPI;

public class ServerNetwork {
    public static void register() {
        PayloadTypeRegistry.playC2S().register(RequestSendBuildPayload.ID, RequestSendBuildPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(OpenSkillsPayload.ID, OpenSkillsPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RequestSendBuildPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.player().server.execute(() -> {
                NbtCompound data = BuildDataSerializer.serialize(player);
                if (ModCompat.PUFFISH_LOADED)
                {
                    NbtCompound skillTreeData = SkillTreeDataSerializer.serialize(player);
                data.put("Skills", skillTreeData);
                }
                for (ServerPlayerEntity p : player.server.getPlayerManager().getPlayerList()) {
                    String snapshotId = java.util.UUID.randomUUID().toString();
                    ServerPlayNetworking.send(p, new SendBuildSnapshotPayload(snapshotId, player.getName().getString(), data));

                }
            });
        });
        PayloadTypeRegistry.playC2S().register(RequestSkillTreePayload.ID, RequestSkillTreePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RequestSkillTreePayload.ID, (payload, context) -> {
            ServerPlayerEntity requester = context.player();
            String targetName = payload.targetName();

            requester.server.execute(() -> {
                ServerPlayerEntity target = requester.server
                        .getPlayerManager()
                        .getPlayer(targetName);

                if (target != null) {
                    var skillTreeNbt = SkillTreeDataSerializer.serialize(target);
                    var response = new SendSkillTreeSnapshotPayload(target.getName().getString(), skillTreeNbt);
                    ServerPlayNetworking.send(requester, response);
                } else {
                    requester.sendMessage(Text.literal("Could not find player: " + targetName), false);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(OpenSkillsPayload.ID, (payload, context) -> {
            String targetName = payload.targetName();
            ServerPlayerEntity requester = context.player();

            requester.server.execute(() -> {
                ServerPlayerEntity target = requester.server
                        .getPlayerManager()
                        .getPlayer(targetName);

                if (target != null) {
                    var skillTreeNbt = SkillTreeDataSerializer.serialize(target);
                    var response = new SendSkillTreeSnapshotPayload(target.getName().getString(), skillTreeNbt);
                    ServerPlayNetworking.send(requester, response);
                } else {
                    requester.sendMessage(Text.literal("Could not find player: " + targetName), false);
                }
            });
        });
    }
}
