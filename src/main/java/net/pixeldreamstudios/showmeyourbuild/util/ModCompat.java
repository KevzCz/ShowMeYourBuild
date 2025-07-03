package net.pixeldreamstudios.showmeyourbuild.util;

import net.fabricmc.loader.api.FabricLoader;

public class ModCompat {
    public static final boolean TRINKETS_LOADED = FabricLoader.getInstance().isModLoaded("trinkets");
    public static final boolean PUFFISH_LOADED = FabricLoader.getInstance().isModLoaded("puffish_skills");
    public static final boolean ATTRIBUTE_PANEL_LOADED = FabricLoader.getInstance().isModLoaded("kevs-attributes-panel");

    public static final boolean SPELLPOWER_LOADED = FabricLoader.getInstance().isModLoaded("spell_power");

}
