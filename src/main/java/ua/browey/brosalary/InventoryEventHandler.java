package ua.browey.brosalary;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryEventHandler implements Listener {

    private final BroSalary plugin;

    public InventoryEventHandler(BroSalary plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(plugin.parseColors(plugin.getGuiConfig().getString("gui.title", "Зарплата")))) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }

            Player player = (Player) event.getWhoClicked();
            int slot = plugin.getGuiConfig().getInt("items.slot", 22);

            if (event.getRawSlot() == slot) {
                long lastSalaryTime = plugin.getDataConfig().getLong(player.getUniqueId().toString(), 0L);
                long currentTime = System.currentTimeMillis();
                String group = PlayerUtils.getPlayerGroup(player);
                long cooldownTime = plugin.getConfig().getLong("groups." + group + ".cooldown") * 1000;
                long remainingTime = (lastSalaryTime + cooldownTime - currentTime) / 1000;

                if (remainingTime > 0) {
                    player.sendMessage(plugin.parseColors(plugin.getConfig().getString("messages.on_cooldown").replace("%time%", TimeUtils.formatTime(remainingTime, plugin.getTimeConfig()))));
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.valueOf(plugin.getConfig().getString("groups." + group + ".cooldown_sound")), 1, 1);
                } else {
                    SalaryManager.giveSalary(plugin, player);
                    player.closeInventory();
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Можно добавить логику при закрытии инвентаря, если необходимо
    }
}