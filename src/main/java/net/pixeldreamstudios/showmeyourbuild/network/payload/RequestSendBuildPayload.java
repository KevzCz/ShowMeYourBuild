package net.pixeldreamstudios.showmeyourbuild.network.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static net.pixeldreamstudios.showmeyourbuild.Showmeyourbuild.MOD_ID;

public record RequestSendBuildPayload() implements CustomPayload {
    public static final Id<RequestSendBuildPayload> ID =
            new Id<>(Identifier.of(MOD_ID, "request_send_build"));

    public static final PacketCodec<PacketByteBuf, RequestSendBuildPayload> CODEC =
            PacketCodec.of(RequestSendBuildPayload::write, buf -> new RequestSendBuildPayload());

    @Override
    public Id<RequestSendBuildPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {}
}
