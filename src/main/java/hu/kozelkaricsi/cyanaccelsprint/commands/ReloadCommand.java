package hu.kozelkaricsi.cyanaccelsprint.commands;

import hu.kozelkaricsi.cyanaccelsprint.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    private final Main plugin;

    public ReloadCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.reloadConfig();
        plugin.config = plugin.getConfig();
        plugin.sendTranslation(sender, "message-config-reloaded", "Configuration reloaded.");

        return true;
    }
}
