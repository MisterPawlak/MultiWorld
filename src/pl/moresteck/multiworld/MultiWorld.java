package pl.moresteck.multiworld;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import pl.moresteck.bvnpe.BukkitVersion;
import pl.moresteck.multiworld.commands.MCreate;
import pl.moresteck.multiworld.commands.MHelp;
import pl.moresteck.multiworld.commands.MImport;
import pl.moresteck.multiworld.commands.MInfo;
import pl.moresteck.multiworld.commands.MList;
import pl.moresteck.multiworld.commands.MPluginInfo;
import pl.moresteck.multiworld.commands.MSave;
import pl.moresteck.multiworld.commands.MSetSpawn;
import pl.moresteck.multiworld.commands.MTeleport;
import pl.moresteck.multiworld.commands.MUnload;
import pl.moresteck.multiworld.commands.MWho;
import pl.moresteck.multiworld.listeners.listener;
import pl.moresteck.multiworld.world.MWorld;
import pl.moresteck.multiworld.world.MWorldConfig;

public class MultiWorld extends JavaPlugin {
	public static MLogger log = new MLogger("Minecraft");
	public static Server server;
	public static List<MWorld> worlds = new LinkedList<MWorld>();
	public static String version;
	public static MultiWorld instance;

	public void onEnable() {
		BukkitVersion.setupVersion((CraftServer) this.getServer());

		instance = this;
		server = this.getServer();
		version = this.getDescription().getVersion();

		this.load();
		Perm.setPermissions(server.getPluginManager().getPlugin("Permissions"));
	}

	public void load() {
		// Just to not repeat the message.
		// BukkitVersion PE enables on startup, so it loads before MultiWorld.
		if (!server.getPluginManager().isPluginEnabled("BukkitVersion")) {
			log.info(true, "BV: enabled v" + BukkitVersion.plugin_version);
			log.info(true, "BV: This server runs on Minecraft " + (BukkitVersion.getVersion().startsWith("b") ? BukkitVersion.getVersion().substring(1) : "Beta " + BukkitVersion.getVersion()));
			if (BukkitVersion.getVersionId() == 0) {
				log.info("BV: Woah! How did you get this Bukkit version?");
				log.info("BV: If it isn't a recompile, fake or test version, please send it to me on Discord: Moresteck#1688");
			}
		}

		log.info("[MultiWorld] Enabled v" + version);
		log.info("[MultiWorld] Always use version's latest build of Craftbukkit for the best compatibility!");
		log.info("[MultiWorld] Download it here: https://betacraft.ovh/bukkit");
		// No compatibility for b1.1 because it doesn't contain any multi-world code to hook in.
		if (BukkitVersion.getVersionId() == 0) {
			log.info("[MultiWorld] No support for Beta 1.1, disabling...");
			this.setEnabled(false);
			return;
		}
		// Load worlds.
		for (String worldname: MWorldConfig.getWorlds()) {
			MultiWorld.instance.loadWorld(worldname);
		}

		// To control spawn flags & PVP flags.
		if (BukkitVersion.getVersionId() <= 9) {
			if (BukkitVersion.getVersionId() <= 3) {
				if (BukkitVersion.getVersionId() == 1) {
					BukkitVersion.registerEventSafely(MultiWorld.instance, "ENTITY_DAMAGED", (Listener) new listener());
				} else {
					BukkitVersion.registerEventSafely(MultiWorld.instance, "ENTITY_DAMAGE", (Listener) new listener());
				}
			}
			BukkitVersion.registerEventSafely(MultiWorld.instance, "CREATURE_SPAWN", (Listener) new listener());
		}
	}

	public void timer() {
		final Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				t.cancel();
			}
		}, 2);
	}

	public void onDisable() {
		log.info(true, "[MultiWorld] Saving worlds...");
		for (MWorld world: worlds) {
			saveWorld(world.getWorld());
		}
		log.info("[MultiWorld] Disabled.");
	}

	public static boolean isDisabled() {
		return !MultiWorld.instance.isEnabled();
	}

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
		if (args.length > 0) {
			new MHelp(cmd, cs, args).execute();
			new MCreate(cmd, cs, args).execute();
			new MTeleport(cmd, cs, args).execute();
			new MSetSpawn(cmd, cs, args).execute();
			new MImport(cmd, cs, args).execute();
			new MInfo(cmd, cs, args).execute();
			new MPluginInfo(cmd, cs, args).execute();
			new MList(cmd, cs, args).execute();
			new MWho(cmd, cs, args).execute();
			new MUnload(cmd, cs, args).execute();
			new MSave(cmd, cs, args).execute();
		} else if (args.length == 0) {
			new MHelp(cmd, cs, args).execute();
		}
		if (cmd.getName().equalsIgnoreCase("testentity")) {
			World w = server.getWorld("world");
			if (args.length == 0) {
				for (Entity e: w.getEntities()) {
					cs.sendMessage(getName(e));
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("xd")) {
				for (Entity e: w.getEntities()) {
					e.remove();
				}
				return true;
			}
		}
		return true;
	}

	public static void saveWorld(World world) {
		// b1.3+
		if (BukkitVersion.getVersionId() >= 2) {
			world.save();
		} else {
			((CraftWorld)world).getHandle().a(true, null);
		}
		log.info(true, "[MultiWorld] \"" + world.getName() + "\" saved.");
	}

	public static MWorld getWorld(String name) {
		for (MWorld world: worlds) {
			if (world.getName().equalsIgnoreCase(name)) {
				return world;
			}
		}
		return null;
	}

	public static String unloadWorld(MWorld world) {
		if (server.getWorlds().get(0) == world.getWorld()) {
			return "Can't unload the main world!";
		}
		// Evacuate the players.
		for (Player p : MultiWorld.server.getOnlinePlayers()) {
			if (p.getWorld().getName().equals(world.getName())) {
				if (BukkitVersion.getVersionId() >= 2) {
					p.teleport(server.getWorlds().get(0).getSpawnLocation());
				} else {
					p.teleportTo(server.getWorlds().get(0).getSpawnLocation());
				}
			}
		}
		worlds.remove(world);
		MWorldConfig.removeWorld(world.getName());
		if (BukkitVersion.getVersionId() >= 9) {
			server.unloadWorld(world.getName(), true);
		}
		log.info("[MultiWorld] Unloaded world \"" + world.getName() + "\" (Seed: " + world.getSeed() + ")");
		return "Unloaded \"" + world.getName() + "\"";
	}

	protected void loadWorld(String worldname) {
		MWorld world = new MWorld(worldname);
		// If the version is b1.6.6 or higher...
		if (BukkitVersion.getVersionId() >= 9) {
			ChunkGenerator generator = null;
			if (!world.getGenerator().equals("")) {
				String gen;
				String genargs;
				try {
					String[] arr = world.getGenerator().split(":");
					gen = arr[0];
					genargs = arr[1];
				} catch (Exception ex) {
					log.info("[MultiWorld] Generator must be specified as "
							+ ChatColor.GOLD + "GeneratorName" + ChatColor.WHITE + ":"
							+ ChatColor.GOLD + "GeneratorArguments" + ChatColor.WHITE + " in " + worldname);
					return;
				}
				try {
					generator = MultiWorld.server.getPluginManager().getPlugin(gen).
					getDefaultWorldGenerator(worldname, genargs);
				} catch (Exception ex) {
					log.info("[MultiWorld] Wrong generator name or generator arguments in " + worldname);
					return;
				}
			}
			World bworld = null;
			if (BukkitVersion.isVersionHigh()) {
				org.bukkit.WorldCreator creator = new org.bukkit.WorldCreator(worldname);
				creator.environment(world.getEnvironment());
				creator.generator(generator);
				creator.seed(world.getSeed());
				bworld = server.getWorld(worldname) == null ? server.createWorld(creator) : server.getWorld(worldname);
			} else {
				bworld = server.getWorld(worldname) == null ? server.createWorld(worldname, world.getEnvironment(),
						world.getSeed(), generator) : server.getWorld(worldname);
			}

			log.info("[MultiWorld] Loaded world \"" + worldname + "\" (Seed: " + world.getSeed() + ")");
			bworld.setSpawnFlags(world.getAllowMonsters(), world.getAllowAnimals());
			bworld.setPVP(world.getPvP());
		} else {
			World bworld = null;
			// If the version is b1.3 or b1.2_01...
			if (BukkitVersion.getVersionId() <= 2) {
				bworld = server.getWorld(worldname);
				if (bworld == null) {
					bworld = server.createWorld(worldname, world.getEnvironment());
				}
				log.info("[MultiWorld] Loaded world \"" + worldname + "\" (Seed: " + world.getSeed() + ")");
			// Else, the version is between b1.4 and b1.6.5.
			} else {
				bworld = server.getWorld(worldname) == null ? 
						server.createWorld(worldname, world.getEnvironment(),
						world.getSeed()) : server.getWorld(worldname);
				log.info("[MultiWorld] Loaded world \"" + worldname + "\" (Seed: " + world.getSeed() + ")");
				if (BukkitVersion.getVersionId() >= 4) bworld.setPVP(world.getPvP());
			}
		}
		worlds.add(world);
	}

	public static String getName(Entity e) {
		return e.getClass().getSimpleName().substring(5).toUpperCase();
	}
}