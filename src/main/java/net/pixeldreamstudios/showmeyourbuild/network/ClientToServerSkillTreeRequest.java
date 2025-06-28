package net.pixeldreamstudios.showmeyourbuild.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.pixeldreamstudios.showmeyourbuild.network.payload.RequestSkillTreePayload;

public class ClientToServerSkillTreeRequest {
    public static void send(String targetName) {
        ClientPlayNetworking.send(new RequestSkillTreePayload(targetName));
    }
}
