package net.pixeldreamstudios.showmeyourbuild.client;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.showmeyourbuild.network.CategoryCache;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.client.config.ClientCategoryConfig;
import net.puffish.skillsmod.client.data.ClientCategoryData;
import net.pixeldreamstudios.showmeyourbuild.network.FakeClientCategoryFactory;

import java.util.HashMap;
import java.util.Map;

public class SkillTreeSnapshotLoader {
    public static Map<Identifier, ClientCategoryData> load(NbtCompound root) {
        Map<Identifier, ClientCategoryData> result = new HashMap<>();

        for (String key : root.getKeys()) {
            if (key.equals("Name")) continue;

            Identifier categoryId = Identifier.tryParse(key);
            if (categoryId == null) continue;

            ClientCategoryData data = CategoryCache.get(categoryId);
            if (data == null) continue;

            ClientCategoryConfig config = data.getConfig();
            if (config == null) continue;

            NbtCompound categoryNbt = root.getCompound(key);
            int spent = categoryNbt.getInt("spent");
            int earned = categoryNbt.getInt("earned");
            int level = categoryNbt.getInt("level");
            int xp = categoryNbt.getInt("xp");
            int xpRequired = categoryNbt.getInt("xpRequired");

            NbtCompound skillsNbt = categoryNbt.getCompound("skills");
            Map<String, Skill.State> stateMap = new HashMap<>();

            for (String skillId : skillsNbt.getKeys()) {
                try {
                    Skill.State state = Skill.State.valueOf(skillsNbt.getString(skillId));
                    stateMap.put(skillId, state);
                } catch (IllegalArgumentException ignored) {
                    // skip invalid
                }
            }
            System.out.println("[Debug] SnapshotLoader input keys: " + root.getKeys());
            System.out.println("[Debug] Parsing skill data for: " + categoryId);
            ClientCategoryData fake = FakeClientCategoryFactory.create(config, stateMap, spent, earned, level, xp, xpRequired);
            result.put(categoryId, fake);
        }

        return result;
    }
}
