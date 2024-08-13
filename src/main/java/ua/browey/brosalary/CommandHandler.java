package ua.browey.brosalary;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler {

    public static boolean handleCommand(BroSalary plugin, CommandSender sender, Command command, String[] args) {
        FileConfiguration config = plugin.getConfig();
        if (command.getName().equalsIgnoreCase("salary")) {
            if (args.length == 0 && sender instanceof Player) {
                Player player = (Player) sender;
                if (!sender.hasPermission("brosalary.use")) {
                    String noPermissionMessage = config.getString("messages.no_permission");
                    if (noPermissionMessage != null) {
                        player.sendMessage(plugin.parseColors(noPermissionMessage));
                    } else {
                        player.sendMessage("У вас нет разрешения на использование этой команды.");
                    }
                    return true;
                }
                SalaryManager.giveSalary(plugin, player);
                return true;
            } else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
                if (!sender.hasPermission("brosalary.reset")) {
                    String noPermissionMessage = config.getString("messages.no_permission");
                    if (noPermissionMessage != null) {
                        sender.sendMessage(plugin.parseColors(noPermissionMessage));
                    } else {
                        sender.sendMessage("У вас нет разрешения на сброс зарплаты.");
                    }
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target != null) {
                    SalaryManager.resetCooldown(plugin, target);
                    String cooldownResetMessage = config.getString("messages.cooldown_reset");
                    if (cooldownResetMessage != null) {
                        sender.sendMessage(plugin.parseColors(cooldownResetMessage.replace("{target}", target.getName())));
                    } else {
                        sender.sendMessage("КД было сброшено на " + target.getName());
                    }
                } else {
                    String playerNotFoundMessage = config.getString("messages.player_not_found");
                    if (playerNotFoundMessage != null) {
                        sender.sendMessage(plugin.parseColors(playerNotFoundMessage));
                    } else {
                        sender.sendMessage("Игрок не найден.");
                    }
                }
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("brosalary.reload")) {
                    String noPermissionMessage = config.getString("messages.no_permission");
                    if (noPermissionMessage != null) {
                        sender.sendMessage(plugin.parseColors(noPermissionMessage));
                    } else {
                        sender.sendMessage("У вас нет разрешения на перезагрузку плагина.");
                    }
                    return true;
                }
                plugin.setupPlugin();
                String pluginReloadedMessage = config.getString("messages.plugin_reloaded");
                if (pluginReloadedMessage != null) {
                    sender.sendMessage(plugin.parseColors(pluginReloadedMessage));
                } else {
                    sender.sendMessage("Плагин перезагружен.");
                }
                return true;
            } else if (args.length == 1 && args[0].equalsIgnoreCase("gui") && sender instanceof Player) {
                if (!sender.hasPermission("brosalary.gui")) {
                    String noPermissionMessage = config.getString("messages.no_permission");
                    if (noPermissionMessage != null) {
                        sender.sendMessage(plugin.parseColors(noPermissionMessage));
                    } else {
                        sender.sendMessage("У вас нет разрешения на открытие графического интерфейса.");
                    }
                    return true;
                }
                GUIManager.openSalaryGUI(plugin, (Player) sender);
                return true;
            }
        }
        return false;
    }

    public static List<String> handleTabComplete(CommandSender sender, Command command, String[] args) {
        List<String> completions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("salary")) {
            if (args.length == 1) {
                if ("reset".startsWith(args[0].toLowerCase()) && sender.hasPermission("brosalary.reset")) {
                    completions.add("reset");
                }
                if ("reload".startsWith(args[0].toLowerCase()) && sender.hasPermission("brosalary.reload")) {
                    completions.add("reload");
                }
                if ("gui".startsWith(args[0].toLowerCase()) && sender.hasPermission("brosalary.gui")) {
                    completions.add("gui");
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("reset") && sender.hasPermission("brosalary.reset")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(player.getName());
                    }
                }
            }
        }
        return completions;
    }
}