package net.pixeldreamstudios.showmeyourbuild.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.pixeldreamstudios.showmeyourbuild.util.ModCompat;

public class BuildDataSerializer {
    public static NbtCompound serialize(PlayerEntity player) {
        RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, player.getRegistryManager());
        NbtCompound root = new NbtCompound();

        // Armor
        NbtList armorList = new NbtList();
        for (ItemStack stack : player.getInventory().armor) {
            ItemStack.CODEC.encodeStart(ops, stack).result().ifPresent(armorList::add);
        }
        root.put("Armor", armorList);

        // Hands
        ItemStack.CODEC.encodeStart(ops, player.getMainHandStack()).result().ifPresent(nbt -> root.put("MainHand", nbt));
        ItemStack.CODEC.encodeStart(ops, player.getOffHandStack()).result().ifPresent(nbt -> root.put("OffHand", nbt));

        // Name
        root.putString("Name", player.getName().getString());
        if (ModCompat.TRINKETS_LOADED) {
            TrinketsApi.getTrinketComponent(player).ifPresent(component -> {
                NbtCompound trinketNbt = new NbtCompound();
                var wrapperLookup = player.getRegistryManager();
                component.writeToNbt(trinketNbt, wrapperLookup); // ✅ CORRECT type

                root.put("Trinkets", trinketNbt);
            });
        }


        return root;
    }
}
