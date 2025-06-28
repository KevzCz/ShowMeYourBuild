package net.pixeldreamstudios.showmeyourbuild.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.Category;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.api.Experience;

import java.util.Optional;

public class SkillTreeDataSerializer {
    public static NbtCompound serialize(PlayerEntity player) {
        NbtCompound root = new NbtCompound();


        SkillsAPI.streamUnlockedCategories((ServerPlayerEntity) player)
                .forEach(category -> {
                    NbtCompound categoryNbt = new NbtCompound();
                    String catId = category.getId().toString();

                    int spent = category.getSpentPoints((ServerPlayerEntity) player);
                    int earned = category.getPointsTotal((ServerPlayerEntity) player);

                    categoryNbt.putInt("spent", spent);
                    categoryNbt.putInt("earned", earned);


                    Optional<Experience> experienceOpt = category.getExperience();
                    if (experienceOpt.isPresent()) {
                        Experience xp = experienceOpt.get();
                        categoryNbt.putInt("level", xp.getLevel((ServerPlayerEntity) player));
                        categoryNbt.putInt("xp", xp.getCurrent((ServerPlayerEntity) player));
                        categoryNbt.putInt("xpRequired", xp.getRequired(0));
                    } else {
                        categoryNbt.putInt("level", -1);
                        categoryNbt.putInt("xp", 0);
                        categoryNbt.putInt("xpRequired", 1);
                    }


                    NbtCompound skillsNbt = new NbtCompound();
                    category.streamSkills().forEach(skill -> {
                        skillsNbt.putString(skill.getId(), skill.getState((ServerPlayerEntity) player).name());
                    });
                    categoryNbt.put("skills", skillsNbt);

                    root.put(catId, categoryNbt);
                });

        root.putString("Name", player.getName().getString());
        return root;
    }
}
