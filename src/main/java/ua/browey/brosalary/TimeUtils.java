package ua.browey.brosalary;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class TimeUtils {

    public static String formatTime(long seconds, FileConfiguration timeConfig) {
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        StringBuilder timeString = new StringBuilder();
        if (days > 0) {
            timeString.append(days).append(" ").append(getTimeUnit(days, timeConfig.getStringList("days"))).append(" ");
        }
        if (hours > 0) {
            timeString.append(hours).append(" ").append(getTimeUnit(hours, timeConfig.getStringList("hours"))).append(" ");
        }
        if (minutes > 0) {
            timeString.append(minutes).append(" ").append(getTimeUnit(minutes, timeConfig.getStringList("minutes"))).append(" ");
        }
        if (seconds > 0) {
            timeString.append(seconds).append(" ").append(getTimeUnit(seconds, timeConfig.getStringList("seconds"))).append(" ");
        }

        return timeString.toString().trim();
    }

    private static String getTimeUnit(long amount, List<String> units) {
        if (amount == 1) {
            return units.get(0);
        } else if (amount > 1 && amount < 5) {
            return units.get(1);
        } else {
            return units.get(2);
        }
    }
}