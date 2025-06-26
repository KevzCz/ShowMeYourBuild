package net.pixeldreamstudios.showmeyourbuild.util;

import net.fabricmc.loader.api.FabricLoader;

public class ModCompat {
    public static final boolean TRINKETS_LOADED = FabricLoader.getInstance().isModLoaded("trinkets");
}
