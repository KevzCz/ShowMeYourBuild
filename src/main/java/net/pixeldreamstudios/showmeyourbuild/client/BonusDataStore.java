package net.pixeldreamstudios.showmeyourbuild.client;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

public class BonusDataStore {
    public static record BonusEntry(
            Identifier attributeId,
            double base,
            double finalValue,
            List<String> flatSources,
            List<String> baseMultSources,
            List<String> totalMultSources) {}

    private static final List<BonusEntry> currentBonuses = new ArrayList<>();
    private static final Set<Identifier> EXCLUDED_ATTRIBUTES = Set.of(
            Identifier.of("spell_engine", "damage_taken")
    );
    public static void loadFromNbt(NbtCompound nbt) {
        currentBonuses.clear();

        for (String key : nbt.getKeys()) {
            Identifier id = Identifier.tryParse(key);
            if (id == null || EXCLUDED_ATTRIBUTES.contains(id)) continue;

            NbtCompound attrData = nbt.getCompound(key);
            double base = attrData.getDouble("Base");
            double finalVal = attrData.getDouble("Final");

            if (Double.isNaN(finalVal) || Math.abs(finalVal - base) < 0.0001) continue;

            List<String> flatSources = new ArrayList<>();
            if (attrData.contains("Flat")) {
                for (var element : attrData.getList("Flat", 10)) {
                    NbtCompound flat = (NbtCompound) element;
                    String source = flat.getString("Source");
                    double val = flat.getDouble("Value");
                    flatSources.add(String.format("%s: %+,.2f", source, val));
                }
            }

            List<String> baseMultSources = new ArrayList<>();
            if (attrData.contains("BaseMult")) {
                for (var element : attrData.getList("BaseMult", 10)) {
                    NbtCompound mod = (NbtCompound) element;
                    String source = mod.getString("Source");
                    double val = mod.getDouble("Value");
                    baseMultSources.add(String.format("%s: %+d%% Base", source, (int)(val * 100)));
                }
            }

            List<String> totalMultSources = new ArrayList<>();
            if (attrData.contains("TotalMult")) {
                for (var element : attrData.getList("TotalMult", 10)) {
                    NbtCompound mod = (NbtCompound) element;
                    String source = mod.getString("Source");
                    double val = mod.getDouble("Value");
                    totalMultSources.add(String.format("%s: %+d%% Total", source, (int)(val * 100)));
                }
            }

            currentBonuses.add(new BonusEntry(
                    Identifier.tryParse(key),
                    base,
                    finalVal,
                    flatSources,
                    baseMultSources,
                    totalMultSources
            ));
        }

        // Sort for consistent order
        currentBonuses.sort(Comparator.comparing(e -> e.attributeId.toString()));
    }

    public static List<BonusEntry> getBonuses() {
        return currentBonuses;
    }

    public static Map<Text, Double> getCondensedBonusesFrom(NbtCompound nbt) {
        List<BonusEntry> bonuses = new ArrayList<>();
        Set<Identifier> excluded = EXCLUDED_ATTRIBUTES;

        for (String key : nbt.getKeys()) {
            Identifier id = Identifier.tryParse(key);
            if (id == null || excluded.contains(id)) continue;

            NbtCompound attrData = nbt.getCompound(key);
            double base = attrData.getDouble("Base");
            double finalVal = attrData.getDouble("Final");

            if (Double.isNaN(finalVal) || Math.abs(finalVal - base) < 0.001) continue;

            double delta = finalVal - base;

            Text label = Registries.ATTRIBUTE.containsId(id)
                    ? Text.translatable(Registries.ATTRIBUTE.get(id).getTranslationKey())
                    : Text.literal(formatName(id.getPath()));

            boolean merged = false;
            for (var existingKey : bonuses) {
                if (existingKey.attributeId.equals(id)) {
                    merged = true;
                    break;
                }
            }

            if (!merged) {
                bonuses.add(new BonusEntry(id, base, finalVal, List.of(), List.of(), List.of()));
            }
        }

        // === Condense
        Map<Text, Double> condensed = new LinkedHashMap<>();
        for (var entry : bonuses) {
            Identifier id = entry.attributeId();
            double delta = entry.finalValue() - entry.base();

            Text label = Registries.ATTRIBUTE.containsId(id)
                    ? Text.translatable(Registries.ATTRIBUTE.get(id).getTranslationKey())
                    : Text.literal(formatName(id.getPath()));

            boolean merged = false;
            for (Text key : condensed.keySet()) {
                if (key.getString().equals(label.getString())) {
                    condensed.put(key, condensed.get(key) + delta);
                    merged = true;
                    break;
                }
            }

            if (!merged) {
                condensed.put(label, delta);
            }
        }

        return condensed;
    }


    private static String formatName(String path) {
        String[] words = path.split("_");
        for (int i = 0; i < words.length; i++) {
            if (words[i].length() > 0) {
                words[i] = Character.toUpperCase(words[i].charAt(0)) + words[i].substring(1);
            }
        }
        return String.join(" ", words);
    }
}
