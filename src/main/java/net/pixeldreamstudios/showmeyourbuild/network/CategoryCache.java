package net.pixeldreamstudios.showmeyourbuild.network;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.client.data.ClientCategoryData;

import java.util.HashMap;
import java.util.Map;

public class CategoryCache {
    private static final Map<Identifier, ClientCategoryData> categoryMap = new HashMap<>();

    public static void put(ClientCategoryData data) {
        categoryMap.put(data.getConfig().id(), data);
    }
    public static void clear() {
        categoryMap.clear();
    }

    public static ClientCategoryData get(Identifier id) {
        return categoryMap.get(id);
    }

    public static Map<Identifier, ClientCategoryData> getAll() {
        return Map.copyOf(categoryMap);
    }
}
