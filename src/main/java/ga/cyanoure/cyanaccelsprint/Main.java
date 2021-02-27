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
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class Main extends JavaPlugin implements Listener, CommandExecutor{
	public FileConfiguration config;
	
	class SprintUser {
		public Player Player;
		public double SprintSince;
		public int PotionStrength = 0;
	}
	
	List<SprintUser> SprintList;
	
	private void pluginMessage(String msg) {
		String prefix = this.config.getString("prefix");
		getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&',prefix+msg));
	}

	private void LoadConfig() {
		this.saveDefaultConfig();
		//this.getConfig().addDefault("", "");
		//this.getConfig().options().copyDefaults(true);
		this.config = this.getConfig();
	}
	
	private void ReloadConfig() {
		this.reloadConfig();
		this.config = this.getConfig();
	}
	
	@Override
	public void onEnable() {
		LoadConfig();
		SprintList = new ArrayList<SprintUser>();
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
	
	/*@EventHandler
	public void onSprint(PlayerToggleSprintEvent event) {
		Player p = event.getPlayer();
		
		// Az ellen�rz�s ford�tva m�k�dik, mert ha nem sprintelt, akkor sprintelni fog, ha sprintel, akkor nem fog.
		if(!p.isSprinting()) {
			SprintUser newItem = new SprintUser();
			newItem.Player = p;
			newItem.SprintSince = System.currentTimeMillis();
			for(int i = 0; i < SprintList.size(); i++) {
				if(SprintList.get(i).Player.getName() == p.getName()) {
					pluginMessage("&cSprint beragad�s hiba kik�sz�b�lve: A j�t�kos m�r a list�ban van, ez�rt nem lett �jra hozz�adva.");
					return;
				}
			}
			SprintList.add(newItem);
		}else {
			for(int i = 0; i < SprintList.size(); i++) {
				if(SprintList.get(i).Player.getName() == p.getName()) {
					SprintList.remove(i);
					p.removePotionEffect(PotionEffectType.SPEED);
					break;
				}
			}
		}
	}*/
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		for(int i = 0; i < SprintList.size(); i++) {
			if(SprintList.get(i).Player.getName().equals(p.getName())) {
				SprintList.remove(i);
				p.removePotionEffect(PotionEffectType.SPEED);
				break;
			}
		}
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if(p.isSprinting() && !isInList(p)) {
			if(config.getString("sprint-permission") == null || p.hasPermission(config.getString("sprint-permission"))) {
				if(config.getStringList("world-list").contains(p.getWorld().getName()) == (config.getString("world-list-type").equals("whitelist"))) {
					addToList(p);
				}
			}
		}else if(!p.isSprinting() && isInList(p)) {
			removeFromList(p);
			p.removePotionEffect(PotionEffectType.SPEED);
		}
		if(p.isSprinting()) {
			if(isInList(p)) {
				for (int i = 0; i < SprintList.size(); i++) {
					if (SprintList.get(i).Player.getName().equals(p.getName())) {
						double SprintSince = SprintList.get(i).SprintSince;
						double TimeNow = System.currentTimeMillis();
						int ElteltIdo = (int) ((TimeNow - SprintSince) / 1000);
						int speedLevel = 0;
						Object[] SprintDurations = this.config.getConfigurationSection("sprint").getKeys(false).toArray();
						for (int j = 0; j < SprintDurations.length; j++) {
							int ido = Integer.parseInt(SprintDurations[j].toString());
							if (ElteltIdo >= ido) {
								speedLevel = this.config.getInt("sprint." + String.valueOf(ido));
							}
						}
						if (speedLevel > 0 && SprintList.get(i).PotionStrength != speedLevel) {
							p.removePotionEffect(PotionEffectType.SPEED);
							p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, speedLevel));
						}
						SprintList.get(i).PotionStrength = speedLevel;
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
				p.sendMessage(ChatColor.translateAlternateColorCodes('&',this.config.getString("prefix")+"&cYou don't have permission for that!"));
				return true;
			}
		}
		
		this.reloadConfig();
		this.config = this.getConfig();
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&',this.config.getString("prefix")+"&2Configuration reloaded."));
		
		return true;
	}
	
	private boolean isInList(Player p) {
		return SprintList.stream().anyMatch(item -> item.Player.getUniqueId().toString().equalsIgnoreCase(p.getUniqueId().toString()));
	}
	
	private void addToList(Player p) {
		if(!isInList(p)) {
			SprintUser newItem = new SprintUser();
			newItem.Player = p;
			newItem.SprintSince = System.currentTimeMillis();
			SprintList.add(newItem);
		}
	}
	
	private void removeFromList(Player p) {
		while(isInList(p)) {
			for(int i = 0; i < SprintList.size(); i++) {
				if(SprintList.get(i).Player.getUniqueId().toString().equalsIgnoreCase(p.getUniqueId().toString())) {
					SprintList.remove(i);
					break;
				}
			}
		}
	}
}
