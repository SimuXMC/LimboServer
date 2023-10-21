package com.github.simuxmc.limboserver.util;

import com.github.simuxmc.limboserver.settings.SettingsContainer;

import java.io.File;
import java.net.URISyntaxException;

public class FileUtil {

	public static File getServerDirectory() throws URISyntaxException {
		File jarFile = new File(SettingsContainer.class.getProtectionDomain().getCodeSource().getLocation()
				.toURI());
		return new File(jarFile.getParentFile().getAbsolutePath());
	}

}
