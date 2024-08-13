package ua.browey.brosalary;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SalaryManager {

    public static void giveSalary(BroSalary plugin, Player player) {
        FileConfiguration config = plugin.getConfig();
        String group = PlayerUtils.getPlayerGroup(player);

        if (group == null) {
            player.sendMessage(plugin.parseColors(config.getString("messages.no_group")));
            return;
        }

        long cooldownTime = config.getLong("groups." + group + ".cooldown");
        long lastSalaryTime = plugin.getDataConfig().getLong(player.getUniqueId().toString(), 0L);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastSalaryTime < cooldownTime * 1000) {
            long remainingTime = cooldownTime - (currentTime - lastSalaryTime) / 1000;
            player.sendMessage(plugin.parseColors(config.getString("messages.on_cooldown").replace("%time%", TimeUtils.formatTime(remainingTime, plugin.getTimeConfig()))));
            player.playSound(player.getLocation(), Sound.valueOf(config.getString("groups." + group + ".cooldown_sound")), 1, 1);
            return;
        }

        if (!config.contains("groups." + group + ".payment_system")) {
            player.sendMessage(plugin.parseColors(config.getString("messages.no_payment_system")));
            return;
        }

        String paymentSystem = config.getString("groups." + group + ".payment_system");
        double amount = config.getDouble("groups." + group + ".amount");
        String salaryReceivedMessage = config.getString("groups." + group + ".salary_received_message");

        boolean paymentSuccess = false;
        if (paymentSystem.equalsIgnoreCase("Vault") && plugin.getEconomy() != null) {
            plugin.getEconomy().depositPlayer(player, amount);
            player.sendMessage(plugin.parseColors(salaryReceivedMessage.replace("%amount%", String.valueOf(amount))));
            player.playSound(player.getLocation(), Sound.valueOf(config.getString("groups." + group + ".success_sound")), 1, 1);
            paymentSuccess = true;
        } else if (paymentSystem.equalsIgnoreCase("PlayerPoints") && plugin.getPlayerPointsAPI() != null) {
            plugin.getPlayerPointsAPI().give(player.getUniqueId(), (int) amount);
            player.sendMessage(plugin.parseColors(salaryReceivedMessage.replace("%amount%", String.valueOf(amount))));
            player.playSound(player.getLocation(), Sound.valueOf(config.getString("groups." + group + ".success_sound")), 1, 1);
            paymentSuccess = true;
        } else if (config.contains("groups." + group + ".command")) {
            String command = config.getString("groups." + group + ".command")
                    .replace("{player}", player.getName())
                    .replace("{amount}", String.valueOf(amount));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            player.sendMessage(plugin.parseColors(salaryReceivedMessage.replace("%amount%", String.valueOf(amount))));
            player.playSound(player.getLocation(), Sound.valueOf(config.getString("groups." + group + ".success_sound")), 1, 1);
            paymentSuccess = true;
        } else {
            player.sendMessage(plugin.parseColors(config.getString("messages.no_payment_system")));
        }

        if (paymentSuccess) {
            plugin.getDataConfig().set(player.getUniqueId().toString(), currentTime);
            plugin.saveDataFile();
        }
    }

    public static void resetCooldown(BroSalary plugin, Player player) {
        plugin.getDataConfig().set(player.getUniqueId().toString(), 0L);
        plugin.saveDataFile();
    }
}