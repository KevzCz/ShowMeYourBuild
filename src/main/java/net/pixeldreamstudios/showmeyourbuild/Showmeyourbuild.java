package net.pixeldreamstudios.showmeyourbuild;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.showmeyourbuild.network.ServerNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Showmeyourbuild implements ModInitializer {
	public static final String MOD_ID = "showmeyourbuild";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier SWORD_UNSHEATH_ID = Identifier.of("showmeyourbuild", "sword_unsheath");
	public static final SoundEvent SWORD_UNSHEATH = SoundEvent.of(SWORD_UNSHEATH_ID);

	@Override
	public void onInitialize() {

		ServerNetwork.register();

		Registry.register(Registries.SOUND_EVENT, SWORD_UNSHEATH_ID, SWORD_UNSHEATH);
	}
}