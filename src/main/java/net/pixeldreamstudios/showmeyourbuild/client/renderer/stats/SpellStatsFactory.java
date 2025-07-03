package net.pixeldreamstudios.showmeyourbuild.client.renderer.stats;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.*;

public class SpellStatsFactory {

    private static final Identifier GROUP_ICON = Identifier.of("spell_power", "textures/mob_effect/generic.png");

    private static final Map<String, Identifier> ICONS = new HashMap<>();

    static {
        ICONS.put("spell_power", GROUP_ICON);
        ICONS.put("spell_power:fire", Identifier.of("spell_power", "textures/mob_effect/fire.png"));
        ICONS.put("spell_power:frost", Identifier.of("spell_power", "textures/mob_effect/frost.png"));
        ICONS.put("spell_power:arcane", Identifier.of("spell_power", "textures/mob_effect/arcane.png"));
        ICONS.put("spell_power:soul", Identifier.of("spell_power", "textures/mob_effect/soul.png"));
        ICONS.put("spell_power:lightning", Identifier.of("spell_power", "textures/mob_effect/lightning.png"));
        ICONS.put("spell_power:critical_chance", Identifier.of("spell_power", "textures/mob_effect/critical_chance.png"));
        ICONS.put("spell_power:critical_damage", Identifier.of("spell_power", "textures/mob_effect/critical_damage.png"));
        ICONS.put("spell_power:haste", Identifier.of("spell_power", "textures/mob_effect/haste.png"));
    }

    private static final Set<String> PERCENT_KEYS = Set.of(
            "spell_power:haste",
            "spell_power:critical_chance",
            "spell_power:critical_damage"
    );

    public static StatGroup createFromAttributes(NbtCompound attributesNbt) {
        StatGroup group = new StatGroup(
                GROUP_ICON,
                "Spell Power",
                "",
                false,
                "Spell Power",
                -1
        );

        for (String key : attributesNbt.getKeys()) {
            if (!key.startsWith("spell_power:")) continue;

            double finalVal = getFinal(attributesNbt, key);
            if (Double.isNaN(finalVal)) continue;

            double baseVal = getBase(attributesNbt, key);
            double delta = finalVal - baseVal;

            String tooltip = getTooltipLabel(key);
            Identifier icon = ICONS.getOrDefault(key, GROUP_ICON);
            String valueStr = formatValue(key, finalVal);

            // Pass delta for sorting + coloring
            group.addChild(new StatRow(icon, "", valueStr, tooltip, true, 0, delta));
        }

        // Bring changed attributes to top (highest delta magnitude first)
        group.getChildren().sort(Comparator.<StatRow>comparingDouble(r -> -Math.abs(r.getDelta())));

        return group;
    }

    private static double getFinal(NbtCompound nbt, String key) {
        if (!nbt.contains(key)) return Double.NaN;
        NbtCompound compound = nbt.getCompound(key);
        return compound.contains("Final") ? compound.getDouble("Final") : Double.NaN;
    }

    private static double getBase(NbtCompound nbt, String key) {
        if (!nbt.contains(key)) return 0.0;
        NbtCompound compound = nbt.getCompound(key);
        return compound.contains("Base") ? compound.getDouble("Base") : 0.0;
    }

    private static String formatValue(String key, double val) {
        if (PERCENT_KEYS.contains(key)) {
            return "%+.0f%%".formatted((val - 100.0)); // 100% is baseline
        } else {
            return val == (int) val ? "%+.0f".formatted(val) : "%+.1f".formatted(val);
        }
    }

    private static String getTooltipLabel(String attrId) {
        String key = attrId.contains(":") ? attrId.split(":")[1] : attrId;
        return capitalize(key.replace('_', ' '));
    }

    private static String capitalize(String input) {
        String[] words = input.split(" ");
        StringBuilder result = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                result.append(Character.toUpperCase(w.charAt(0)))
                        .append(w.substring(1)).append(" ");
            }
        }
        return result.toString().trim();
    }

    public static void registerIcon(String attributeId, Identifier icon) {
        ICONS.put(attributeId, icon);
    }
}
