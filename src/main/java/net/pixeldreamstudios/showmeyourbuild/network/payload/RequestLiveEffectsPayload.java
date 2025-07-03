package net.pixeldreamstudios.showmeyourbuild.network.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static net.pixeldreamstudios.showmeyourbuild.Showmeyourbuild.MOD_ID;
public record RequestLiveEffectsPayload(String targetName) implements CustomPayload {
    public static final Id<RequestLiveEffectsPayload> ID = new Id<>(Identifier.of(MOD_ID, "request_live_effects"));

    public static final PacketCodec<PacketByteBuf, RequestLiveEffectsPayload> CODEC =
            PacketCodec.of(RequestLiveEffectsPayload::write, RequestLiveEffectsPayload::read);

    public void write(PacketByteBuf buf) {
        buf.writeString(targetName);
    }

    public static RequestLiveEffectsPayload read(PacketByteBuf buf) {
        return new RequestLiveEffectsPayload(buf.readString());
    }

    @Override
    public Id<RequestLiveEffectsPayload> getId() {
        return ID;
    }
}
