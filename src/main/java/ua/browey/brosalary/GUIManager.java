package ua.browey.brosalary;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GUIManager {

    public static void openSalaryGUI(BroSalary plugin, Player player) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration guiConfig = plugin.getGuiConfig();

        int guiSize = guiConfig.getInt("gui.size", 54);
        String guiTitle = plugin.parseColors(guiConfig.getString("gui.title", "Salary"));

        Inventory gui = Bukkit.createInventory(null, guiSize, guiTitle);

        long lastSalaryTime = plugin.getDataConfig().getLong(player.getUniqueId().toString(), 0L);
        long currentTime = System.currentTimeMillis();
        String group = PlayerUtils.getPlayerGroup(player);
        long cooldownTime = config.getLong("groups." + group + ".cooldown") * 1000;
        long remainingTime = (lastSalaryTime + cooldownTime - currentTime) / 1000;

        ItemStack item;
        if (remainingTime > 0) {
            item = createGuiItem(
                    Material.valueOf(guiConfig.getString("items.onCooldown.material", "BARRIER")),
                    plugin.parseColors(guiConfig.getString("items.onCooldown.name", "Зарплата")),
                    plugin.parseColors(guiConfig.getString("items.onCooldown.lore", "КД: %time%")).replace("%time%", TimeUtils.formatTime(remainingTime, plugin.getTimeConfig())),
                    guiConfig.getInt("items.onCooldown.customModelData", 0),
                    guiConfig.getBoolean("items.onCooldown.enchanted", false)
            );
        } else {
            item = createGuiItem(
                    Material.valueOf(guiConfig.getString("items.canReceive.material", "ENDER_EYE")),
                    plugin.parseColors(guiConfig.getString("items.canReceive.name", "Заплата")),
                    plugin.parseColors(guiConfig.getString("items.canReceive.lore", "ЗП можно получить")),
                    guiConfig.getInt("items.canReceive.customModelData", 0),
                    guiConfig.getBoolean("items.canReceive.enchanted", false)
            );
        }

        int slot = guiConfig.getInt("items.slot", 22);
        gui.setItem(slot, item);

        if (guiConfig.contains("other_items")) {
            for (Map<?, ?> itemMap : guiConfig.getMapList("other_items")) {
                ItemStack otherItem = createOtherGuiItem(plugin, itemMap);
                if (otherItem != null) {
                    Object slotObj = itemMap.get("slot");
                    if (slotObj instanceof Integer) {
                        gui.setItem((Integer) slotObj, otherItem);
                    } else if (slotObj instanceof List) {
                        for (Object slotValue : (List<?>) slotObj) {
                            if (slotValue instanceof Integer) {
                                gui.setItem((Integer) slotValue, otherItem);
                            }
                        }
                    }
                }
            }
        }

        player.openInventory(gui);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.getOpenInventory().getTopInventory().equals(gui)) {
                    updateSalaryGUI(plugin, player, gui);
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private static ItemStack createOtherGuiItem(BroSalary plugin, Map<?, ?> itemMap) {
        String material = (String) itemMap.get("material");
        if (material == null) {
            return null;
        }

        ItemStack item = new ItemStack(Material.valueOf(material));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String name = (String) itemMap.get("name");
            if (name != null) {
                meta.setDisplayName(plugin.parseColors(name));
            }

            List<String> lore = (List<String>) itemMap.get("lore");
            if (lore != null) {
                meta.setLore(lore.stream().map(plugin::parseColors).collect(Collectors.toList()));
            }

            Integer customModelData = (Integer) itemMap.get("customModelData");
            if (customModelData != null) {
                meta.setCustomModelData(customModelData);
            }

            Boolean enchanted = (Boolean) itemMap.get("enchanted");
            if (enchanted != null && enchanted) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.ARROW_INFINITE, 1, true);
                Boolean hideEnchantment = (Boolean) itemMap.get("hideEnchantment");
                if (hideEnchantment != null && hideEnchantment) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            }

            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createGuiItem(Material material, String name, String lore, int customModelData, boolean enchanted) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> loreList = new ArrayList<>();
            loreList.add(lore);
            meta.setLore(loreList);
            if (customModelData != 0) {
                meta.setCustomModelData(customModelData);
            }
            if (enchanted) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.ARROW_INFINITE, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private static void updateSalaryGUI(BroSalary plugin, Player player, Inventory gui) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration guiConfig = plugin.getGuiConfig();

        long lastSalaryTime = plugin.getDataConfig().getLong(player.getUniqueId().toString(), 0L);
        long currentTime = System.currentTimeMillis();
        String group = PlayerUtils.getPlayerGroup(player);
        long cooldownTime = config.getLong("groups." + group + ".cooldown") * 1000;
        long remainingTime = (lastSalaryTime + cooldownTime - currentTime) / 1000;

        ItemStack item;
        if (remainingTime > 0) {
            item = createGuiItem(
                    Material.valueOf(guiConfig.getString("items.onCooldown.material", "BARRIER")),
                    plugin.parseColors(guiConfig.getString("items.onCooldown.name", "Зарплата")),
                    plugin.parseColors(guiConfig.getString("items.onCooldown.lore", "КД: %time%")).replace("%time%", TimeUtils.formatTime(remainingTime, plugin.getTimeConfig())),
                    guiConfig.getInt("items.onCooldown.customModelData", 0),
                    guiConfig.getBoolean("items.onCooldown.enchanted", false)
            );
        } else {
            item = createGuiItem(
                    Material.valueOf(guiConfig.getString("items.canReceive.material", "ENDER_EYE")),
                    plugin.parseColors(guiConfig.getString("items.canReceive.name", "Заплата")),
                    plugin.parseColors(guiConfig.getString("items.canReceive.lore", "ЗП можно получить")),
                    guiConfig.getInt("items.canReceive.customModelData", 0),
                    guiConfig.getBoolean("items.canReceive.enchanted", false)
            );
        }

        int slot = guiConfig.getInt("items.slot", 22);
        gui.setItem(slot, item);
    }
}