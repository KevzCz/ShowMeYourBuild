package net.pixeldreamstudios.showmeyourbuild.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.pixeldreamstudios.showmeyourbuild.network.payload.RequestSendBuildPayload;
import net.pixeldreamstudios.showmeyourbuild.network.payload.SendBuildSnapshotPayload;

public class ServerNetwork {
    public static void register() {
        PayloadTypeRegistry.playC2S().register(RequestSendBuildPayload.ID, RequestSendBuildPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestSendBuildPayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.player().server.execute(() -> {
                NbtCompound data = BuildDataSerializer.serialize(player);

                for (ServerPlayerEntity p : player.server.getPlayerManager().getPlayerList()) {
                    String snapshotId = java.util.UUID.randomUUID().toString();
                    ServerPlayNetworking.send(p, new SendBuildSnapshotPayload(snapshotId, player.getName().getString(), data));

                }
            });
        });
    }
}
