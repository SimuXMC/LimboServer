package com.github.simuxmc.limboserver.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.instance.InstanceContainer;

public class StatsCommand extends Command {

	public StatsCommand(InstanceContainer world) {
		super("stats");
		setDefaultExecutor(((sender, context) -> {
			sender.sendMessage("Limbo Server Stats");
			Runtime runtime = Runtime.getRuntime();
			long totalMemory = runtime.totalMemory()/1000000;
			long maxMemory = runtime.maxMemory()/1000000;
			sender.sendMessage("Memory: " + totalMemory + "MB" + "/" + maxMemory + "MB");
			int playerCount = world.getPlayers().size();
			sender.sendMessage("Player Count: " + playerCount);
		}));
	}



}
