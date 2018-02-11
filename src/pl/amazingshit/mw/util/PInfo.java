package pl.amazingshit.mw.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.nijikokun.bukkit.Permissions.Permissions;
/**
 * Gets information about command sender.
 */
public class PInfo {

	private CommandSender cs;

	public PInfo(CommandSender c) {
		this.cs = c;
	}

	public Boolean isPlayer() {
		return (cs instanceof Player);
	}

	public Boolean isAuthorized(String permission) {
		if (this.isPlayer()) {
			Player p = (Player)cs;
			if (mw.permissionsEnabled) {
				return Permissions.Security.has(p, permission) || Permissions.Security.has(p, "multiworld.*");
			}
			if (p.isOp()) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return true;
		}
	}

	public String getName() {
		if (isPlayer()) {
			return ((Player)cs).getName();
		}
		else {
			return "Console";
		}
	}
}
