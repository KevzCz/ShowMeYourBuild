package net.pixeldreamstudios.showmeyourbuild.client.renderer;

import com.mojang.authlib.GameProfile;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerSnapshot {
    public static boolean debug_message = false;
    public record SnapshotData(
            OtherClientPlayerEntity player,
            ItemStack[] armor,
            ItemStack mainHand,
            ItemStack offHand,
            List<ItemStack> accessories
    ) {}

    public static SnapshotData fromNbt(NbtCompound data, String displayName, PlayerEntity fallbackPlayer) {
        MinecraftClient client = MinecraftClient.getInstance();
        var world = client.world;

        UUID uuid = client.getSocialInteractionsManager().getUuid(displayName);
        GameProfile profile = uuid != null
                ? new GameProfile(uuid, displayName)
                : new GameProfile(UUID.nameUUIDFromBytes(("OfflinePlayer:" + displayName).getBytes()), displayName);

        if (debug_message) System.out.println("[Snapshot] Creating fake player for: " + displayName);


        ItemStack[] armorStacks = new ItemStack[4];
        ItemStack mainHand = ItemStack.EMPTY;
        ItemStack offHand = ItemStack.EMPTY;

        RegistryOps<NbtElement> ops = RegistryOps.of(NbtOps.INSTANCE, client.getNetworkHandler().getRegistryManager());

        NbtList armorList = data.getList("Armor", NbtElement.COMPOUND_TYPE);


        for (int i = 0; i < 4; i++) {
            armorStacks[i] = ItemStack.EMPTY;
        }

        for (int i = 0; i < armorList.size(); i++) {
            NbtCompound itemNbt = armorList.getCompound(i);
            ItemStack stack = ItemStack.CODEC.parse(ops, itemNbt).result().orElse(ItemStack.EMPTY);
            if (stack.isEmpty()) continue;

            int slot = -1;

            if (itemNbt.contains("Slot", NbtElement.BYTE_TYPE)) {
                slot = itemNbt.getByte("Slot");
            } else if (stack.getItem() instanceof net.minecraft.item.ArmorItem armorItem) {
                EquipmentSlot detectedSlot = armorItem.getSlotType();
                switch (detectedSlot) {
                    case HEAD -> slot = 3;
                    case CHEST -> slot = 2;
                    case LEGS -> slot = 1;
                    case FEET -> slot = 0;
                }
            }

            if (slot >= 0 && slot < 4) {
                armorStacks[slot] = stack;
                if (debug_message) System.out.println("[Snapshot] Loaded armor slot " + slot + " = " + stack);
            } else if (debug_message) {
                System.out.println("[Snapshot] Could not determine armor slot for: " + stack);
            }
        }

        if (data.contains("MainHand", NbtElement.COMPOUND_TYPE)) {
            mainHand = ItemStack.CODEC.parse(ops, data.get("MainHand")).result().orElse(ItemStack.EMPTY);
            if (debug_message) System.out.println("[Snapshot] MainHand = " + mainHand);
        }
        if (data.contains("OffHand", NbtElement.COMPOUND_TYPE)) {
            offHand = ItemStack.CODEC.parse(ops, data.get("OffHand")).result().orElse(ItemStack.EMPTY);
            if (debug_message) System.out.println("[Snapshot] OffHand = " + offHand);
        }


        OtherClientPlayerEntity fakePlayer = new OtherClientPlayerEntity(world, profile) {
            @Override public boolean isPartVisible(PlayerModelPart part) { return true; }
            @Override public boolean shouldRenderName() { return false; }
            @Override public Text getName() { return Text.empty(); }
        };

        fakePlayer.copyPositionAndRotation(fallbackPlayer);
        fakePlayer.setPose(fallbackPlayer.getPose());


        boolean trinketsLoaded = FabricLoader.getInstance().isModLoaded("trinkets");
        List<ItemStack> accessories = new ArrayList<>();

        if (data.contains("Trinkets", NbtElement.COMPOUND_TYPE)) {
            NbtCompound trinketNbt = data.getCompound("Trinkets");
            if (debug_message) System.out.println("[Snapshot] Raw trinket NBT: " + trinketNbt);


            if (debug_message) System.out.println("[Snapshot] Parsed Trinket Slots:");
            for (String group : trinketNbt.getKeys()) {
                NbtCompound groupCompound = trinketNbt.getCompound(group);
                for (String slot : groupCompound.getKeys()) {
                    NbtCompound slotData = groupCompound.getCompound(slot);
                    if (!slotData.contains("Items", NbtElement.LIST_TYPE)) continue;

                    NbtList items = slotData.getList("Items", NbtElement.COMPOUND_TYPE);
                    if (debug_message) System.out.println("  - Group: '" + group + "', Slot: '" + slot + "', Item count: " + items.size());
                }
            }

            for (String group : trinketNbt.getKeys()) {
                NbtCompound groupCompound = trinketNbt.getCompound(group);
                for (String slot : groupCompound.getKeys()) {
                    NbtCompound slotData = groupCompound.getCompound(slot);
                    if (!slotData.contains("Items", NbtElement.LIST_TYPE)) continue;

                    NbtList items = slotData.getList("Items", NbtElement.COMPOUND_TYPE);
                    for (int i = 0; i < items.size(); i++) {
                        NbtCompound stackNbt = items.getCompound(i);
                        if (!stackNbt.contains("id")) continue;

                        ItemStack stack = ItemStack.CODEC.parse(ops, stackNbt).result().orElse(ItemStack.EMPTY);
                        if (!stack.isEmpty()) {
                            accessories.add(stack);
                            if (debug_message) System.out.println("  [Snapshot Accessories] " + group + "/" + slot + ": " + stack);
                        }
                    }
                }
            }

            if (trinketsLoaded) {
                TrinketsApi.getTrinketComponent(fakePlayer).ifPresent(component -> {
                    var registryLookup = client.getNetworkHandler().getRegistryManager();

                    NbtCompound trinketsOnlyNbt = new NbtCompound();
                    for (String group : trinketNbt.getKeys()) {
                        if (group.startsWith("accessories") || group.equals("data_written_by_accessories")) {
                            if (debug_message) System.out.println("[Snapshot] Skipping accessories group: " + group);
                            continue;
                        }
                        trinketsOnlyNbt.put(group, trinketNbt.get(group));
                    }

                    component.readFromNbt(trinketsOnlyNbt, registryLookup);
                    if (debug_message) System.out.println("[Snapshot] Synced trinkets component from filtered NBT");
                });
            }

        }
        else {
            if (debug_message) System.out.println("[Snapshot] No trinket data found in NBT.");
        }

        if (debug_message) System.out.println("[Snapshot] Applied position/pose from fallback player.");


        for (int i = 0; i < 4; i++) {
            fakePlayer.getInventory().armor.set(i, armorStacks[i]);
            if (debug_message) System.out.println("[Snapshot] Inventory armor slot " + i + " = " + armorStacks[i]);
        }

        fakePlayer.equipStack(EquipmentSlot.HEAD, armorStacks[3]);
        fakePlayer.equipStack(EquipmentSlot.CHEST, armorStacks[2]);
        fakePlayer.equipStack(EquipmentSlot.LEGS, armorStacks[1]);
        fakePlayer.equipStack(EquipmentSlot.FEET, armorStacks[0]);
        fakePlayer.setStackInHand(Hand.MAIN_HAND, mainHand);
        fakePlayer.setStackInHand(Hand.OFF_HAND, offHand);

        if (debug_message) System.out.println("[Snapshot] Snapshot player ready.");
        if (debug_message) System.out.println("[Snapshot] Returning " + accessories.size() + " accessories in snapshot.");

        return new SnapshotData(fakePlayer, armorStacks, mainHand, offHand, accessories);

    }
}
