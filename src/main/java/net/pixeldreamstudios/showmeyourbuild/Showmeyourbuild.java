package net.pixeldreamstudios.showmeyourbuild;

import net.fabricmc.api.ModInitializer;
import net.pixeldreamstudios.showmeyourbuild.network.ServerNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Showmeyourbuild implements ModInitializer {
	public static final String MOD_ID = "showmeyourbuild";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ServerNetwork.register();
	}
}