package net.pixeldreamstudios.showmeyourbuild.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.client.config.ClientCategoryConfig;
import net.puffish.skillsmod.client.data.ClientCategoryData;

import java.util.HashMap;
import java.util.Map;
@Environment(EnvType.CLIENT)
public class FakeClientCategoryFactory {
    public static ClientCategoryData create(ClientCategoryConfig config, Map<String, Skill.State> states, int spent, int earned, int level, int xp, int xpRequired) {
        ClientCategoryData data = new ClientCategoryData(
                config,
                new HashMap<>(states),
                spent,
                earned,
                level,
                xp,
                xpRequired
        );

        for (Map.Entry<String, Skill.State> entry : states.entrySet()) {
            String id = entry.getKey();
            Skill.State state = entry.getValue();
            if (state == Skill.State.UNLOCKED) {
                data.unlock(id);
            } else {
                data.lock(id);
            }
        }

        return data;
    }
}

