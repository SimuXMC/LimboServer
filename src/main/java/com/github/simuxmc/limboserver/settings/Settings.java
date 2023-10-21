package com.github.simuxmc.limboserver.settings;

public class Settings {

	private final String serverBrand;
	private final String worldName;
	private final String defaultGamemode;
	private final int viewDistance;
	private final int tickRate;
	private final String address;
	private final int port;
	private final int maxPlayers;
	private final String motd;
	private final boolean tablist;
	private final String authType;
	private final String authToken;

	public Settings(String serverBrand, String worldName, String defaultGamemode, int viewDistance, int tickRate, String address, int port, int maxPlayers, String motd, boolean tablist,
					String authType, String authToken) {
		this.serverBrand = serverBrand;
		this.worldName = worldName;
		this.defaultGamemode = defaultGamemode;
		this.viewDistance = viewDistance;
		this.tickRate = tickRate;
		this.address = address;
		this.port = port;
		this.maxPlayers = maxPlayers;
		this.motd = motd;
		this.tablist = tablist;
		this.authType = authType;
		this.authToken = authToken;
	}

	public String getServerBrand() {
		return serverBrand;
	}

	public String getWorldName() {
		return worldName;
	}

	public String getDefaultGamemode() {
		return defaultGamemode;
	}

	public int getViewDistance() {
		return viewDistance;
	}

	public int getTickRate() {
		return tickRate;
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public String getMotd() {
		return motd;
	}

	public boolean isTablist() {
		return tablist;
	}

	public String getAuthType() {
		return authType;
	}

	public String getAuthToken() {
		return authToken;
	}

	@Override
	public String toString() {
		return "Settings{" +
				"serverBrand='" + serverBrand + '\'' +
				", worldName='" + worldName + '\'' +
				", defaultGamemode='" + defaultGamemode + '\'' +
				", viewDistance=" + viewDistance +
				", tickRate=" + tickRate +
				", address='" + address + '\'' +
				", port=" + port +
				", maxPlayers=" + maxPlayers +
				", motd='" + motd + '\'' +
				", tablist=" + tablist +
				", authType='" + authType + '\'' +
				", authToken='" + authToken + '\'' +
				'}';
	}

}
