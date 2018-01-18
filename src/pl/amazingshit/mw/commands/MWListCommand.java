package pl.amazingshit.mw.commands;

import java.util.List;

import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;

import pl.amazingshit.mw.managers.ConfigWorld;
import pl.amazingshit.mw.managers.WorldManager;
import pl.amazingshit.mw.util.PInfo;

public class MWListCommand {

	public static void handle(CommandSender sender, String alias, PInfo s) {
		List<String> send = ConfigWorld.getWorldStringList();
		sender.sendMessage("World list:");
		for (String world: send) {
			if (s.isPlayer()) {
				String color = "�a";
				WorldManager wm = new WorldManager(world);
				if (wm.dimension() == Environment.NETHER) {
					color = "�c";
				}
				sender.sendMessage(" - " + color + world);
			}
			else {
				sender.sendMessage(" - " + world);
			}
		}
	}
}