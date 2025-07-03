package net.pixeldreamstudios.showmeyourbuild.client.renderer.stats;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.*;

public class AttributeGroupFactory {

    private static final Set<String> excludedNamespaces = new HashSet<>();
    private static final Set<Identifier> excludedAttributes = new HashSet<>();

    public static void excludeModNamespace(String namespace) {
        excludedNamespaces.add(namespace);
    }

    public static void excludeAttribute(Identifier attributeId) {
        excludedAttributes.add(attributeId);
    }

    public static List<StatGroup> buildAttributeGroups(NbtCompound attributesNbt) {
        Map<String, StatGroup> namespaceGroups = new HashMap<>();

        for (String key : attributesNbt.getKeys()) {
            if (key.startsWith("effect:")) continue;

            Identifier id = Identifier.tryParse(key);
            if (id == null || excludedNamespaces.contains(id.getNamespace()) || excludedAttributes.contains(id)) continue;
            if (AttributeOverrideRegistry.isAttributeHidden(id)) continue;


            NbtCompound attrNbt = attributesNbt.getCompound(key);
            if (!attrNbt.contains("Final")) continue;

            double finalVal = attrNbt.getDouble("Final");
            if (Double.isNaN(finalVal)) continue;

            var attribute = net.minecraft.registry.Registries.ATTRIBUTE.get(id);
            if (attribute == null) continue;
            AttributeOverrideRegistry.AttributeOverride override = AttributeOverrideRegistry.getAttributeOverride(id);

            String label = override != null && override.label() != null
                    ? override.label()
                    : net.minecraft.text.Text.translatable(attribute.getTranslationKey()).getString();

            String tooltip = override != null && override.tooltip() != null
                    ? override.tooltip()
                    : net.minecraft.text.Text.translatable(attribute.getTranslationKey()).getString();

            Identifier icon = override != null ? override.icon() : null;

            String namespace = id.getNamespace();
            if (AttributeOverrideRegistry.isGroupHidden(namespace)) continue;

            AttributeOverrideRegistry.GroupOverride groupOverride = AttributeOverrideRegistry.getGroupOverride(namespace);

            String groupLabel = groupOverride != null && groupOverride.label() != null
                    ? groupOverride.label()
                    : capitalize(namespace);

            Identifier groupIcon = groupOverride != null ? groupOverride.icon() : null;
            String groupTooltip = groupOverride != null && groupOverride.tooltip() != null
                    ? groupOverride.tooltip()
                    : groupLabel + " Attributes";

            StatGroup group = namespaceGroups.computeIfAbsent(groupLabel, lbl ->
                    new StatGroup(groupIcon, groupLabel, null, false, groupTooltip, -1) // temp column
            );



            String value = formatValue(finalVal);

            group.addChild(new StatRow(icon, label, value, tooltip, true, group.column, finalVal - attrNbt.getDouble("Base")));

        }
        for (StatGroup group : namespaceGroups.values()) {
            group.getChildren().sort(Comparator.comparingDouble(r -> -Math.abs(r.getDelta())));
        }

        List<StatGroup> result = new ArrayList<>(namespaceGroups.values());
        result.sort(Comparator.comparing(group -> group.tooltip));
        return result;
    }



    private static String formatValue(double val) {
        if (Math.floor(val) == val) {
            return String.format("%.0f", val);
        } else {
            return String.format("%.2f", val);
        }
    }

    private static String capitalize(String namespace) {
        if (namespace == null || namespace.isEmpty()) return "";
        return Character.toUpperCase(namespace.charAt(0)) + namespace.substring(1);
    }
}
