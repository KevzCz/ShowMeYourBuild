package net.pixeldreamstudios.showmeyourbuild.network.payload;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static net.pixeldreamstudios.showmeyourbuild.Showmeyourbuild.MOD_ID;

public record SendLiveEffectsPayload(String playerName, NbtCompound effectData) implements CustomPayload {
    public static final Id<SendLiveEffectsPayload> ID =
            new Id<>(Identifier.of(MOD_ID, "send_live_effects"));

    public static final PacketCodec<PacketByteBuf, SendLiveEffectsPayload> CODEC =
            PacketCodec.of(SendLiveEffectsPayload::write, SendLiveEffectsPayload::read);

    public void write(PacketByteBuf buf) {
        buf.writeString(playerName);
        buf.writeNbt(effectData);
    }

    public static SendLiveEffectsPayload read(PacketByteBuf buf) {
        return new SendLiveEffectsPayload(buf.readString(), buf.readNbt());
    }

    @Override
    public Id<SendLiveEffectsPayload> getId() {
        return ID;
    }
}
