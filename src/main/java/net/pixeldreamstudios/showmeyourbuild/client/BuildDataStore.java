package net.pixeldreamstudios.showmeyourbuild.client;

import net.minecraft.nbt.NbtCompound;

import java.util.HashMap;
import java.util.Map;

public class BuildDataStore {
    private static final Map<String, NbtCompound> snapshots = new HashMap<>();

    public static void save(String snapshotId, NbtCompound data) {
        snapshots.put(snapshotId, data);
    }

    public static NbtCompound get(String snapshotId) {
        return snapshots.get(snapshotId);
    }

}
