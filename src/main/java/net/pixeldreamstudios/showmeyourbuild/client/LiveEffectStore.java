package net.pixeldreamstudios.showmeyourbuild.client;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.*;

public class LiveEffectStore {
    private static final Map<String, List<StatusEffectInstance>> liveEffects = new HashMap<>();

    public static void save(String playerName, NbtCompound data) {
        List<StatusEffectInstance> list = new ArrayList<>();
        NbtList potions = data.getList("PotionEffects", NbtCompound.COMPOUND_TYPE);

        for (int i = 0; i < potions.size(); i++) {
            NbtCompound tag = potions.getCompound(i);
            Identifier id = Identifier.tryParse(tag.getString("Id"));

            if (id != null && Registries.STATUS_EFFECT.containsId(id)) {
                RegistryKey<?> key = RegistryKey.of(Registries.STATUS_EFFECT.getKey(), id);
                RegistryEntry<?> entry = Registries.STATUS_EFFECT.getEntry((RegistryKey<net.minecraft.entity.effect.StatusEffect>) key).orElse(null);

                if (entry != null) {
                    list.add(new StatusEffectInstance(
                            (RegistryEntry<net.minecraft.entity.effect.StatusEffect>) entry,
                            tag.getInt("Duration"),
                            tag.getInt("Amplifier"),
                            tag.getBoolean("Ambient"),
                            tag.getBoolean("ShowParticles")
                    ));
                }
            }
        }

        liveEffects.put(playerName, list);
    }
    private static final Map<UUID, List<StatusEffectInstance>> LIVE_EFFECTS = new HashMap<>();

    public static void update(UUID uuid, List<StatusEffectInstance> effects) {
        LIVE_EFFECTS.put(uuid, effects);
    }

    public static List<StatusEffectInstance> getEffectsFor(UUID uuid) {
        return LIVE_EFFECTS.getOrDefault(uuid, List.of());
    }
    public static List<StatusEffectInstance> get(String playerName) {
        return liveEffects.getOrDefault(playerName, List.of());
    }
}
