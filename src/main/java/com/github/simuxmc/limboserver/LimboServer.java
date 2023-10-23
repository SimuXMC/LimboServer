package com.github.simuxmc.limboserver;

import com.github.simuxmc.limboserver.commands.StatsCommand;
import com.github.simuxmc.limboserver.settings.Settings;
import com.github.simuxmc.limboserver.settings.SettingsContainer;
import com.github.simuxmc.limboserver.util.FileUtil;
import me.nullicorn.nedit.NBTReader;
import me.nullicorn.nedit.type.NBTCompound;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.*;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.bungee.BungeeCordProxy;
import net.minestom.server.extras.velocity.VelocityProxy;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.network.packet.client.ClientPacket;
import net.minestom.server.network.packet.client.play.ClientChatMessagePacket;
import net.minestom.server.network.packet.client.play.ClientInteractEntityPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerBlockPlacementPacket;
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.world.DimensionType;
import net.minestom.server.world.DimensionTypeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

public class LimboServer {

	@SuppressWarnings("unchecked")
	private static final Class<? extends ClientPacket>[] ignorePackets = new Class[]{ClientChatMessagePacket.class,
			ClientPlayerBlockPlacementPacket.class, ClientInteractEntityPacket.class, ClientPlayerDiggingPacket.class};

	public static void main(String[] args) throws IOException, URISyntaxException {
		// TODO make plugins...? (can change settings, spawn holograms, spawn npcs, make commands, interact with the bungee messaging channel)
		Logger logger = LoggerFactory.getLogger(LimboServer.class);
		logger.info("Starting limbo server...");
		MinecraftServer minecraftServer = MinecraftServer.init();
		logger.info("Getting settings...");
		Settings settings = SettingsContainer.getInstance().getSettings();
		System.setProperty("minestom.chunk-view-distance", String.valueOf(settings.getViewDistance()));
		System.setProperty("minestom.tps", String.valueOf(settings.getTickRate()));
		logger.info("Creating end dimension...");
		DimensionTypeManager manager = MinecraftServer.getDimensionTypeManager();
		DimensionType end = DimensionType.builder(NamespaceID.from("limbo:end"))
				.ambientLight(1)
				.bedSafe(false)
				.ceilingEnabled(true)
				.skylightEnabled(true)
				.effects("minecraft:the_end")
				.raidCapable(false)
				.piglinSafe(false)
				.natural(false)
				.ultrawarm(false)
				.build();
		manager.addDimension(end);
		logger.info("Loading the world...");
		InstanceManager instanceManager = MinecraftServer.getInstanceManager();
		InstanceContainer instanceContainer = instanceManager.createInstanceContainer(end);
		instanceContainer.setTime(0);
		instanceContainer.setTimeRate(0);
		String worldName = settings.getWorldName();
		File spawnWorld = new File(FileUtil.getServerDirectory(), worldName + "/");
		Pos spawnPosition = new Pos(0, 42, 0);
		if (!worldName.isBlank() && spawnWorld.exists()) {
			logger.info("World found, using it instead of default.");
			instanceContainer.setChunkLoader(new AnvilLoader(worldName));
			File datFile = new File(spawnWorld, "level.dat");
			if (datFile.exists()) {
				NBTCompound compound = NBTReader.readFile(datFile).getCompound("Data");
				int x = compound.getInt("SpawnX", 0);
				int y = compound.getInt("SpawnY", 42);
				int z = compound.getInt("SpawnZ", 0);
				spawnPosition = new Pos(x, y, z);
			}
		} else {
			logger.info("No world found, generating default world.");
			instanceContainer.setGenerator(unit ->
					unit.modifier().fillHeight(0, 40, Block.END_STONE));
		}
		logger.info("Registering events...");
		GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
		Pos finalSpawnPosition = spawnPosition; // required by ide
		GameMode defaultGameMode = GameMode.valueOf(settings.getDefaultGamemode().toUpperCase(Locale.ENGLISH));
		globalEventHandler.addListener(PlayerLoginEvent.class, event -> {
			final Player player = event.getPlayer();
			player.setGameMode(defaultGameMode);
			player.setInvulnerable(true);
			event.setSpawningInstance(instanceContainer);
			player.setRespawnPoint(finalSpawnPosition);
		});
		globalEventHandler.addListener(PlayerPacketEvent.class, event -> {
			ClientPacket packet = event.getPacket();
			for (Class<? extends ClientPacket> clazz : ignorePackets) {
				if (clazz.isInstance(packet)) {
					event.setCancelled(true);
					return;
				}
			}
		});
		/*globalEventHandler.addListener(PlayerBlockBreakEvent.class, event -> event.setCancelled(true));
		globalEventHandler.addListener(PlayerBlockPlaceEvent.class, event -> event.setCancelled(true));
		globalEventHandler.addListener(PlayerBlockInteractEvent.class, event -> event.setCancelled(true));
		globalEventHandler.addListener(PlayerChatEvent.class, event -> event.setCancelled(true));*/
		globalEventHandler.addListener(PlayerSkinInitEvent.class, event -> {
			Player player = event.getPlayer();
			PlayerSkin skin = PlayerSkin.fromUuid(player.getUsername());
			player.setSkin(skin);
		});
		globalEventHandler.addListener(ServerListPingEvent.class, event -> {
			ResponseData data = new ResponseData();
			data.setVersion(settings.getServerBrand() + " " + MinecraftServer.VERSION_NAME);
			int onlinePlayerCount = event.getResponseData().getOnline();
			int maxPlayerSetting = settings.getMaxPlayers();
			data.setMaxPlayer(maxPlayerSetting == -1 ? onlinePlayerCount+1 : maxPlayerSetting);
			data.setDescription(MiniMessage.miniMessage().deserialize(settings.getMotd()));
			event.setResponseData(data);
		});
		globalEventHandler.addListener(AsyncPlayerPreLoginEvent.class, event -> {
			Player player = event.getPlayer();
			int playerCount = instanceContainer.getPlayers().size();
			int maxPlayers = settings.getMaxPlayers();
			if (maxPlayers != -1 && playerCount > maxPlayers) {
				player.kick("Server is full");
			}
			else {
				logger.info("[Connection] " + player.getUsername() + " (" + (playerCount+1) + " players online)");
			}
		});
		if (!settings.isTablist()) {
			globalEventHandler.addListener(PlayerPacketOutEvent.class, event -> {
				if (event.getPacket() instanceof PlayerInfoUpdatePacket packet &&
						!packet.entries().get(0).username().equalsIgnoreCase(event.getPlayer().getUsername())) {

					event.setCancelled(true);
				}
			});
		}
		logger.info("Registering commands...");
		CommandManager commandManager = MinecraftServer.getCommandManager();
		commandManager.register(new StatsCommand(instanceContainer));
		logger.info("Attempting to initialize authentication...");
		String authType = settings.getAuthType();
		String authToken = settings.getAuthToken();
		switch (authType.toUpperCase(Locale.ENGLISH)) {
			case "DEFAULT" -> {
				MojangAuth.init();
				logger.info("Mojang authentication has been enabled.");
			}
			case "BUNGEECORD" -> {
				if (authToken.isBlank()) {
					logger.warn("No auth token provided for BungeeGuard.");
				}
				else if (!BungeeCordProxy.isValidBungeeGuardToken(authToken)) {
					logger.error("Provided BungeeGuard token is invalid.");
				}
				else {
					BungeeCordProxy.setBungeeGuardTokens(Set.of(authToken));
				}
				BungeeCordProxy.enable();
				logger.info("BungeeCord support has been enabled.");
			}
			case "VELOCITY" -> {
				if (authToken.isBlank()) {
					logger.error("Can't enable velocity support due to no authentication token.");
				}
				else {
					VelocityProxy.enable(authToken);
					logger.info("Velocity support has been enabled.");
				}
			}
			default -> {
				logger.warn("Invalid/no authentication type provided, running on nothing?");
				logger.warn("List of valid options: DEFAULT, BUNGEECORD, VELOCITY");
			}
		}
		String address = settings.getAddress();
		int port = settings.getPort();
		minecraftServer.start(address, port);
		logger.info("Listening on port '" + port + "' for address '" + address + "'.");
	}

}
