package net.pixeldreamstudios.showmeyourbuild.util;

import net.fabricmc.loader.api.FabricLoader;

public class ModCompat {
    public static final boolean TRINKETS_LOADED = FabricLoader.getInstance().isModLoaded("trinkets");
    public static final boolean PUFFISH_LOADED = FabricLoader.getInstance().isModLoaded("puffish_skills");
}
