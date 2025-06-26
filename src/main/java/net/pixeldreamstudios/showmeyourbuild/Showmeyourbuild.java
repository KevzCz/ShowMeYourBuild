package net.pixeldreamstudios.showmeyourbuild;

import net.fabricmc.api.ModInitializer;

import net.pixeldreamstudios.showmeyourbuild.network.ClientNetwork;
import net.pixeldreamstudios.showmeyourbuild.network.ServerNetwork;
import org.apache.logging.log4j.core.jmx.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Showmeyourbuild implements ModInitializer {
	public static final String MOD_ID = "showmeyourbuild";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ClientNetwork.register();
		ServerNetwork.register();
		LOGGER.info("Hello Fabric world!");
	}
}