package net.pixeldreamstudios.showmeyourbuild.client.renderer.stats;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class AttributeOverrideRegistry {

    public record AttributeOverride(
            Identifier icon,
            String label,
            String tooltip,
            boolean hidden
    ) {}

    public record GroupOverride(
            Identifier icon,
            String label,
            String tooltip,
            boolean hidden
    ) {}

    private static final Map<Identifier, AttributeOverride> attributeOverrides = new HashMap<>();
    private static final Map<String, GroupOverride> groupOverrides = new HashMap<>();

    public static void registerAttributeOverride(Identifier attributeId, AttributeOverride override) {
        attributeOverrides.put(attributeId, override);
    }

    public static void registerGroupOverride(String namespace, GroupOverride override) {
        groupOverrides.put(namespace, override);
    }

    public static AttributeOverride getAttributeOverride(Identifier attributeId) {
        return attributeOverrides.get(attributeId);
    }

    public static GroupOverride getGroupOverride(String namespace) {
        return groupOverrides.get(namespace);
    }

    public static boolean isAttributeHidden(Identifier attributeId) {
        AttributeOverride o = attributeOverrides.get(attributeId);
        return o != null && o.hidden;
    }

    public static boolean isGroupHidden(String namespace) {
        GroupOverride o = groupOverrides.get(namespace);
        return o != null && o.hidden;
    }
}
