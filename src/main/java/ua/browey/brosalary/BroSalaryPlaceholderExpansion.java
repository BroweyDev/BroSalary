package ua.browey.brosalary;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class BroSalaryPlaceholderExpansion extends PlaceholderExpansion {

    private final BroSalary plugin;

    public BroSalaryPlaceholderExpansion(BroSalary plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "brosalary";
    }

    @Override
    public String getAuthor() {
        return "BroweyDev";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        switch (identifier) {
            case "group":
                return PlayerUtils.getPlayerGroup(player);

            case "salary":
                String group = PlayerUtils.getPlayerGroup(player);
                if (group == null) return "Unknown";
                double amount = plugin.getConfig().getDouble("groups." + group + ".amount");
                return String.valueOf(amount);

            case "cooldown":
                long remainingTime = plugin.getPlayerRemainingCooldown(player);
                if (remainingTime > 0) {
                    return String.valueOf(remainingTime);
                } else {
                    return plugin.parseColors(plugin.getConfig().getString("messages.no_cooldown"));
                }

            case "payment_system":
                String paymentGroup = PlayerUtils.getPlayerGroup(player);
                if (paymentGroup == null) return "Unknown";
                return plugin.getConfig().getString("groups." + paymentGroup + ".payment_system");

            default:
                return null;
        }
    }
}
