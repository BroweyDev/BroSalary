package ua.browey.brosalary;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class MetricsHandler {

    private Metrics metrics;

    public MetricsHandler(JavaPlugin plugin) {
        int pluginId = 22985;
        try {
            metrics = new Metrics(plugin, pluginId);
            plugin.getLogger().info("bStats успешно инициализирован!");
        } catch (Exception e) {
            plugin.getLogger().severe("Ошибка при инициализации bStats: " + e.getMessage());
        }
    }

    public Metrics getMetrics() {
        return metrics;
    }
}
