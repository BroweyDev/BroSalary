package ua.browey.brosalary;

import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.md_5.bungee.api.ChatColor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BroSalary extends JavaPlugin implements TabCompleter, Listener {

    private Economy economy;
    private PlayerPointsAPI playerPointsAPI;
    private File dataFile;
    private FileConfiguration dataConfig;
    private FileConfiguration timeConfig;
    private FileConfiguration guiConfig;

    @Override
    public void onEnable() {
        setupPlugin();
        new MetricsHandler(this);
        new BroSalaryPlaceholderExpansion(this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new InventoryEventHandler(this), this);
    }

    @Override
    public void onDisable() {
        saveDataFile();
    }

    public void setupPlugin() {
        saveDefaultConfig();
        reloadConfig();
        loadTimeConfig();
        loadGuiConfig();
        setupEconomy();
        setupPlayerPoints();
        loadDataFile();
        getCommand("salary").setTabCompleter(this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new BroSalaryPlaceholderExpansion(this).register();
        }
    }
    public long getPlayerRemainingCooldown(Player player) {
        String group = PlayerUtils.getPlayerGroup(player);
        if (group == null) return 0;

        long lastSalaryTime = getDataConfig().getLong(player.getUniqueId().toString(), 0L);
        long cooldownTime = getConfig().getLong("groups." + group + ".cooldown") * 1000;
        long remainingTime = (lastSalaryTime + cooldownTime - System.currentTimeMillis()) / 1000;

        return Math.max(0, remainingTime);
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null) {
                economy = rsp.getProvider();
            }
        }
    }

    private void setupPlayerPoints() {
        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") != null) {
            playerPointsAPI = PlayerPoints.getInstance().getAPI();
        }
    }

    private void loadDataFile() {
        dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            saveResource("data.yml", false);
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void loadTimeConfig() {
        File timeFile = new File(getDataFolder(), "time.yml");
        if (!timeFile.exists()) {
            saveResource("time.yml", false);
        }
        timeConfig = YamlConfiguration.loadConfiguration(timeFile);
    }

    private void loadGuiConfig() {
        File guiFile = new File(getDataFolder(), "gui.yml");
        if (!guiFile.exists()) {
            saveResource("gui.yml", false);
        }
        guiConfig = YamlConfiguration.loadConfiguration(guiFile);
    }

    public void saveDataFile() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return CommandHandler.handleCommand(this, sender, command, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return CommandHandler.handleTabComplete(sender, command, args);
    }

    public FileConfiguration getDataConfig() {
        return dataConfig;
    }

    public Economy getEconomy() {
        return economy;
    }

    public PlayerPointsAPI getPlayerPointsAPI() {
        return playerPointsAPI;
    }

    public FileConfiguration getTimeConfig() {
        return timeConfig;
    }

    public FileConfiguration getGuiConfig() {
        return guiConfig;
    }

    public String parseColors(String text) {
        if (text == null) {
            return null;
        }

        text = ChatColor.translateAlternateColorCodes('&', text);

        Pattern pattern = Pattern.compile("&#([a-fA-F0-9]{6})");
        Matcher matcher = pattern.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String hexColor = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hexColor).toString());
        }
        matcher.appendTail(buffer);

        return buffer.toString();
    }
}