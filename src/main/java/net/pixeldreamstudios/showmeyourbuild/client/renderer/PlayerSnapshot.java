package net.pixeldreamstudios.showmeyourbuild.client.renderer;

import com.mojang.authlib.GameProfile;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.List;
import java.util.UUID;

public class PlayerSnapshot {

    public record SnapshotData(OtherClientPlayerEntity player, ItemStack[] armor, ItemStack mainHand, ItemStack offHand) {}

    public static SnapshotData fromNbt(NbtCompound data, String displayName, PlayerEntity fallbackPlayer) {
        MinecraftClient client = MinecraftClient.getInstance();
        var world = client.world;
        var session = client.getSession();

        UUID uuid = MinecraftClient.getInstance()
                .getSocialInteractionsManager()
                .getUuid(displayName);

        GameProfile profile = uuid != null
                ? new GameProfile(uuid, displayName)
                : new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + displayName).getBytes()), displayName);

        System.out.println("[Snapshot] Creating fake player for: " + displayName);

        ItemStack[] armorStacks = new ItemStack[4];
        ItemStack mainHand = ItemStack.EMPTY;
        ItemStack offHand = ItemStack.EMPTY;

        RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, client.getNetworkHandler().getRegistryManager());

        // Load armor
        NbtList armorList = data.getList("Armor", NbtElement.COMPOUND_TYPE);

// Default to EMPTY if index is missing
        armorStacks[0] = armorList.size() > 3
                ? ItemStack.CODEC.parse(ops, armorList.get(3)).result().orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY; // Boots

        armorStacks[3] = armorList.size() > 2
                ? ItemStack.CODEC.parse(ops, armorList.get(2)).result().orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY; // Leggings

        armorStacks[2] = armorList.size() > 1
                ? ItemStack.CODEC.parse(ops, armorList.get(1)).result().orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY; // Chestplate (or Elytra)

        armorStacks[1] = armorList.size() > 0
                ? ItemStack.CODEC.parse(ops, armorList.get(0)).result().orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY; // Helmet


        // Load hand items
        if (data.contains("MainHand", NbtElement.COMPOUND_TYPE)) {
            mainHand = ItemStack.CODEC.parse(ops, data.get("MainHand")).result().orElse(ItemStack.EMPTY);
            System.out.println("[Snapshot] MainHand = " + mainHand);
        }

        if (data.contains("OffHand", NbtElement.COMPOUND_TYPE)) {
            offHand = ItemStack.CODEC.parse(ops, data.get("OffHand")).result().orElse(ItemStack.EMPTY);
            System.out.println("[Snapshot] OffHand = " + offHand);
        }

        // Final copy for lambda capture
        ItemStack finalMainHand = mainHand;
        ItemStack finalOffHand = offHand;

        OtherClientPlayerEntity fakePlayer = new OtherClientPlayerEntity(world, profile) {


            @Override
            public boolean isPartVisible(net.minecraft.entity.player.PlayerModelPart part) {
                return true;
            }
            @Override
            public boolean shouldRenderName() {
                return false;
            }
            @Override
            public Text getName() {
                return Text.empty();
            }
        };

        fakePlayer.copyPositionAndRotation(fallbackPlayer);
        fakePlayer.setPose(fallbackPlayer.getPose());
        System.out.println("[Snapshot] Applied position/pose from fallback player.");

        // Copy armor to vanilla inventory
        for (int i = 0; i < 4; i++) {
            fakePlayer.getInventory().armor.set(i, armorStacks[i]);
            System.out.println("[Snapshot] Inventory armor slot " + i + " = " + armorStacks[i]);
        }

        // Equip armor for rendering
        fakePlayer.equipStack(EquipmentSlot.HEAD, armorStacks[3]);
        fakePlayer.equipStack(EquipmentSlot.CHEST, armorStacks[2]);
        fakePlayer.equipStack(EquipmentSlot.LEGS, armorStacks[1]);
        fakePlayer.equipStack(EquipmentSlot.FEET, armorStacks[0]);
        System.out.println("[Snapshot] Called equipStack() for all armor slots.");

        // Hands
        fakePlayer.setStackInHand(Hand.MAIN_HAND, mainHand);
        fakePlayer.setStackInHand(Hand.OFF_HAND, offHand);
        System.out.println("[Snapshot] Set hand items in fake player inventory.");

        // Load trinkets if present
        if (data.contains("Trinkets", NbtElement.COMPOUND_TYPE)) {
            NbtCompound trinketNbt = data.getCompound("Trinkets");
            TrinketsApi.getTrinketComponent(fakePlayer).ifPresent(component -> {
                var registryLookup = client.getNetworkHandler().getRegistryManager();
                component.readFromNbt(trinketNbt, registryLookup);
                System.out.println("[Snapshot] Loaded trinket data into snapshot player.");
            });
        }

        System.out.println("[Snapshot] Snapshot player ready.");
        return new SnapshotData(fakePlayer, armorStacks, mainHand, offHand);
    }
}
