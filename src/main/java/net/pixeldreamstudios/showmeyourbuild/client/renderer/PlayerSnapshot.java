package net.pixeldreamstudios.showmeyourbuild.client.renderer;

import com.mojang.authlib.GameProfile;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
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
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerSnapshot {

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
            }
        }

        if (data.contains("MainHand", NbtElement.COMPOUND_TYPE)) {
            mainHand = ItemStack.CODEC.parse(ops, data.get("MainHand")).result().orElse(ItemStack.EMPTY);

        }
        if (data.contains("OffHand", NbtElement.COMPOUND_TYPE)) {
            offHand = ItemStack.CODEC.parse(ops, data.get("OffHand")).result().orElse(ItemStack.EMPTY);

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
        if (data.contains("PotionEffects", NbtElement.LIST_TYPE)) {
            NbtList potionList = data.getList("PotionEffects", NbtElement.COMPOUND_TYPE);

            for (int i = 0; i < potionList.size(); i++) {
                NbtCompound tag = potionList.getCompound(i);
                Identifier id = Identifier.tryParse(tag.getString("Id"));

                if (id != null && net.minecraft.registry.Registries.STATUS_EFFECT.containsId(id)) {
                    var key = net.minecraft.registry.RegistryKey.of(
                            net.minecraft.registry.Registries.STATUS_EFFECT.getKey(),
                            id
                    );

                    fakePlayer.addStatusEffect(new StatusEffectInstance(
                            net.minecraft.registry.Registries.STATUS_EFFECT.entryOf(key),
                            tag.getInt("Duration"),
                            tag.getInt("Amplifier"),
                            tag.getBoolean("Ambient"),
                            tag.getBoolean("ShowParticles")
                    ));
                }
            }
        }


        if (data.contains("Trinkets", NbtElement.COMPOUND_TYPE)) {
            NbtCompound trinketNbt = data.getCompound("Trinkets");


            for (String group : trinketNbt.getKeys()) {
                NbtCompound groupCompound = trinketNbt.getCompound(group);
                for (String slot : groupCompound.getKeys()) {
                    NbtCompound slotData = groupCompound.getCompound(slot);
                    if (!slotData.contains("Items", NbtElement.LIST_TYPE)) continue;

                    NbtList items = slotData.getList("Items", NbtElement.COMPOUND_TYPE);

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
                            continue;
                        }
                        trinketsOnlyNbt.put(group, trinketNbt.get(group));
                    }

                    component.readFromNbt(trinketsOnlyNbt, registryLookup);
                    });
            }

        }




        for (int i = 0; i < 4; i++) {
            fakePlayer.getInventory().armor.set(i, armorStacks[i]);
            }

        fakePlayer.equipStack(EquipmentSlot.HEAD, armorStacks[3]);
        fakePlayer.equipStack(EquipmentSlot.CHEST, armorStacks[2]);
        fakePlayer.equipStack(EquipmentSlot.LEGS, armorStacks[1]);
        fakePlayer.equipStack(EquipmentSlot.FEET, armorStacks[0]);
        fakePlayer.setStackInHand(Hand.MAIN_HAND, mainHand);
        fakePlayer.setStackInHand(Hand.OFF_HAND, offHand);


        return new SnapshotData(fakePlayer, armorStacks, mainHand, offHand, accessories);

    }
}
