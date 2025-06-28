package net.pixeldreamstudios.showmeyourbuild.network.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static net.pixeldreamstudios.showmeyourbuild.Showmeyourbuild.MOD_ID;

public record RequestSkillTreePayload(String targetName) implements CustomPayload {
    public static final Id<RequestSkillTreePayload> ID =
            new Id<>(Identifier.of(MOD_ID, "request_skill_tree"));

    public static final PacketCodec<PacketByteBuf, RequestSkillTreePayload> CODEC =
            PacketCodec.of(RequestSkillTreePayload::write, RequestSkillTreePayload::read);

    @Override
    public Id<RequestSkillTreePayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeString(targetName);
    }

    public static RequestSkillTreePayload read(PacketByteBuf buf) {
        return new RequestSkillTreePayload(buf.readString());
    }
}

