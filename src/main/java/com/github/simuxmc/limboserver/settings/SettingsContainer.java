package com.github.simuxmc.limboserver.settings;

import com.github.simuxmc.limboserver.util.FileUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

public class SettingsContainer {

	private static final Logger LOGGER = LoggerFactory.getLogger(SettingsContainer.class);

	private static SettingsContainer instance;

	private final Settings settings;

	private SettingsContainer(Settings settings) {
		if (instance != null) {
			throw new RuntimeException("Reflection is not supported here.");
		}
		this.settings = settings;
	}

	public static SettingsContainer getInstance() throws IOException, URISyntaxException {
		if (instance != null) {
			return instance;
		}
		File settingsFile = new File(FileUtil.getServerDirectory(), "settings.json");
		Gson gson = new Gson();
		Settings settings;
		if (!settingsFile.exists()) {
			LOGGER.info("Settings file not found.");
			settings = generateSettings(settingsFile, gson);
		}
		else {
			LOGGER.info("Settings file found.");
			settings = gson.fromJson(new FileReader(settingsFile), Settings.class);
			if (settings == null) {
				LOGGER.error("Existing settings file was invalid.");
				settings = generateSettings(settingsFile, gson);
			}
		}
		instance = new SettingsContainer(settings);
		return instance;
	}

	private static Settings generateSettings(File settingsFile, Gson gson) throws IOException {
		LOGGER.info("Generating settings file...");
		File settingsParentFile = settingsFile.getParentFile();
		if (!settingsParentFile.exists()) {
			settingsParentFile.mkdirs();
		}
		settingsFile.createNewFile();
		Settings settings = new Settings("Limbo", "world", "creative", 3,
				5, "0.0.0.0", 25565, -1, "A Limbo Server", false,
				"default", "");
		FileWriter writer = new FileWriter(settingsFile);
		gson.toJson(settings, writer);
		writer.close();
		return settings;
	}

	public Settings getSettings() {
		return settings;
	}

}
