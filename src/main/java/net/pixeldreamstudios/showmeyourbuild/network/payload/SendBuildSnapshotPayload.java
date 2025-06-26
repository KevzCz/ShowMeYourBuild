package net.pixeldreamstudios.showmeyourbuild.network.payload;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static net.pixeldreamstudios.showmeyourbuild.Showmeyourbuild.MOD_ID;

public record SendBuildSnapshotPayload(String snapshotId, String playerName, NbtCompound data) implements CustomPayload {
    public static final Id<SendBuildSnapshotPayload> ID = new Id<>(Identifier.of(MOD_ID, "send_build_snapshot"));

    public static final PacketCodec<PacketByteBuf, SendBuildSnapshotPayload> CODEC =
            PacketCodec.of(SendBuildSnapshotPayload::write, SendBuildSnapshotPayload::read);

    @Override
    public Id<SendBuildSnapshotPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeString(snapshotId);
        buf.writeString(playerName);
        buf.writeNbt(data);
    }

    public static SendBuildSnapshotPayload read(PacketByteBuf buf) {
        return new SendBuildSnapshotPayload(buf.readString(), buf.readString(), buf.readNbt());
    }
}
