package me.hostadam.events.api.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class LocationUtil {

    public static Location fromString(String string) {
        if(string == null || string.isEmpty()) {
            return null;
        }

        String[] split = string.split(",");
        return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]));
    }

    public static String toString(Location location) {
        if(location == null) {
            return "";
        }

        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ() + "," + location.getYaw() + "," + location.getPitch();
    }
}
