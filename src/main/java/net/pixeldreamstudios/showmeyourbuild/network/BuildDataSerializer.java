package net.pixeldreamstudios.showmeyourbuild.network;

import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryOps;
import net.pixeldreamstudios.attributepanel.api.AttributePanelAPI;
import net.pixeldreamstudios.showmeyourbuild.util.ModCompat;

public class BuildDataSerializer {
    public static NbtCompound serialize(PlayerEntity player) {
        RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, player.getRegistryManager());
        NbtCompound root = new NbtCompound();

        NbtList armorList = new NbtList();
        for (ItemStack stack : player.getInventory().armor) {
            ItemStack.CODEC.encodeStart(ops, stack).result().ifPresent(armorList::add);
        }
        root.put("Armor", armorList);

        ItemStack.CODEC.encodeStart(ops, player.getMainHandStack()).result().ifPresent(nbt -> root.put("MainHand", nbt));
        ItemStack.CODEC.encodeStart(ops, player.getOffHandStack()).result().ifPresent(nbt -> root.put("OffHand", nbt));

        root.putString("Name", player.getName().getString());
        if (ModCompat.TRINKETS_LOADED) {
            TrinketsApi.getTrinketComponent(player).ifPresent(component -> {
                NbtCompound trinketNbt = new NbtCompound();
                var wrapperLookup = player.getRegistryManager();
                component.writeToNbt(trinketNbt, wrapperLookup);
                root.put("Trinkets", trinketNbt);
            });
        }
        if (ModCompat.ATTRIBUTE_PANEL_LOADED) {
            NbtCompound attributes = AttributePanelAPI.getAttributeSnapshot(player);
            root.put("Attributes", attributes);
//            System.out.print(attributes);
        }


        NbtList potionList = new NbtList();
        for (var effectInstance : player.getStatusEffects()) {
            NbtCompound effectNbt = new NbtCompound();
            effectNbt.putString("Id", Registries.STATUS_EFFECT.getId(effectInstance.getEffectType().value()).toString());
            effectNbt.putInt("Amplifier", effectInstance.getAmplifier());
            effectNbt.putInt("Duration", effectInstance.getDuration());
            effectNbt.putBoolean("Ambient", effectInstance.isAmbient());
            effectNbt.putBoolean("ShowParticles", effectInstance.shouldShowParticles());
            potionList.add(effectNbt);
        }

        root.put("PotionEffects", potionList);



        return root;
    }
}
