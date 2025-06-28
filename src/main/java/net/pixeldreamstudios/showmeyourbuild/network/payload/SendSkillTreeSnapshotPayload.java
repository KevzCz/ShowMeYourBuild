package net.pixeldreamstudios.showmeyourbuild.network.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.nbt.NbtCompound;

import static net.pixeldreamstudios.showmeyourbuild.Showmeyourbuild.MOD_ID;

public record SendSkillTreeSnapshotPayload(String playerName, NbtCompound skillData) implements CustomPayload {
    public static final Id<SendSkillTreeSnapshotPayload> ID =
            new Id<>(Identifier.of(MOD_ID, "send_skill_tree_snapshot"));

    public static final PacketCodec<PacketByteBuf, SendSkillTreeSnapshotPayload> CODEC =
            PacketCodec.of(SendSkillTreeSnapshotPayload::write, SendSkillTreeSnapshotPayload::read);

    @Override
    public Id<SendSkillTreeSnapshotPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeString(playerName);
        buf.writeNbt(skillData);
    }

    public static SendSkillTreeSnapshotPayload read(PacketByteBuf buf) {
        return new SendSkillTreeSnapshotPayload(buf.readString(), buf.readNbt());
    }
}
