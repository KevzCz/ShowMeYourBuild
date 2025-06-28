package net.pixeldreamstudios.showmeyourbuild.network.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static net.pixeldreamstudios.showmeyourbuild.Showmeyourbuild.MOD_ID;

public record OpenSkillsPayload(String targetName) implements CustomPayload {
    public static final Id<OpenSkillsPayload> ID =
            new Id<>(Identifier.of(MOD_ID, "open_skills"));

    public static final PacketCodec<PacketByteBuf, OpenSkillsPayload> CODEC =
            PacketCodec.of(OpenSkillsPayload::write, OpenSkillsPayload::read);

    @Override
    public Id<OpenSkillsPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeString(targetName);
    }

    public static OpenSkillsPayload read(PacketByteBuf buf) {
        return new OpenSkillsPayload(buf.readString());
    }
}

