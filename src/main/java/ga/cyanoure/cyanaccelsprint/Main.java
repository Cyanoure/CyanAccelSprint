package ga.cyanoure.cyanaccelsprint;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class Main extends JavaPlugin implements Listener, CommandExecutor{
	public FileConfiguration config;
	
	private static final int POTION_DURATION = 1000000;

	private class SprintUser {
		public Player player;
		public long sprintSince;
		public int potionStrength = 0;

		public SprintUser(Player player, long sprintSince) {
			this.player = player;
			this.sprintSince = sprintSince;
		}
	}

	private List<SprintUser> sprintList;
	
	private void pluginMessage(String msg) {
		String prefix = this.config.getString("prefix", "[CAS] ");
		getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + msg));
	}

	private void LoadConfig() {
		this.saveDefaultConfig();
		this.config = this.getConfig();
	}
	
	@Override
	public void onEnable() {
		LoadConfig();
		sprintList = new ArrayList<>();
		getServer().getPluginManager().registerEvents(this, this);
		this.getCommand("casreload").setExecutor(this);
		
		pluginMessage("&2Plugin and configuration loaded.");
		
		new UpdateChecker(this,79042).getLatestVersion(version -> {
			pluginMessage("Current version: "+this.getDescription().getVersion());
			pluginMessage("Latest version on spigotmc.org: "+version);
			pluginMessage("You can download the latest version at https://www.spigotmc.org/resources/cyanaccelsprint.79042/");
		});;
	}
	
	@Override
	public void onDisable() {
		pluginMessage("&2Plugin disabled.");
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		sprintList.removeIf(user -> user.player.getUniqueId().equals(p.getUniqueId()));
		p.removePotionEffect(PotionEffectType.SPEED);
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if(p.isSprinting() && !isInList(p)) {
			String sprintPermission = config.getString("sprint-permission");
			if (sprintPermission == null || p.hasPermission(sprintPermission)) {
				String worldListType = config.getString("world-list-type", "whitelist");
				List<String> worldList = config.getStringList("world-list");
				if (worldList.contains(p.getWorld().getName()) == worldListType.equals("whitelist")) {
					addToList(p);
				}
			}
		}else if(!p.isSprinting() && isInList(p)) {
			removeFromList(p);
			p.removePotionEffect(PotionEffectType.SPEED);
		}
		if(p.isSprinting()) {
			if(isInList(p)) {
				for (SprintUser user : sprintList) {
					if (user.player.getUniqueId().equals(p.getUniqueId())) {
						long sprintSince = user.sprintSince;
						long timeNow = System.currentTimeMillis();
						int elapsed = (int) ((timeNow - sprintSince) / 1000);
						int speedLevel = 0;
						if (this.config.getConfigurationSection("sprint") != null) {
							for (String key : this.config.getConfigurationSection("sprint").getKeys(false)) {
								int ido = Integer.parseInt(key);
								if (elapsed >= ido) {
									speedLevel = this.config.getInt("sprint." + key);
								}
							}
						}
						if (speedLevel > 0 && user.potionStrength != speedLevel) {
							p.removePotionEffect(PotionEffectType.SPEED);
							p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, POTION_DURATION, speedLevel));
						}
						user.potionStrength = speedLevel;
						break;
					}
				}
			}
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player p = (Player)sender;
			if(!p.hasPermission("cyanaccelsprint.admin")) {
				p.sendMessage(ChatColor.translateAlternateColorCodes('&',this.config.getString("prefix", "[CAS] ") + "&cYou don't have permission for that!"));
				return true;
			}
		}
		
		this.reloadConfig();
		this.config = this.getConfig();
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',this.config.getString("prefix", "[CAS] ") + "&2Configuration reloaded."));
		
		return true;
	}
	
	private boolean isInList(Player p) {
		return sprintList.stream().anyMatch(user -> user.player.getUniqueId().equals(p.getUniqueId()));
	}
	
	private void addToList(Player p) {
		if(!isInList(p)) {
			sprintList.add(new SprintUser(p, System.currentTimeMillis()));
		}
	}
	
	private void removeFromList(Player p) {
		sprintList.removeIf(user -> user.player.getUniqueId().equals(p.getUniqueId()));
	}
}
