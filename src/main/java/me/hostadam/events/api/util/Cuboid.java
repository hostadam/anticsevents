package me.hostadam.events.api.util;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Data
public class Cuboid {

    private World world;
    private int minX, maxX, minY, maxY, minZ, maxZ;

    public Cuboid(Location cornerOne, Location cornerTwo) {
        if(!cornerOne.getWorld().getName().equals(cornerTwo.getWorld().getName())) {
            return;
        }

        this.world = cornerOne.getWorld();
        this.minX = Math.min(cornerOne.getBlockX(), cornerTwo.getBlockX());
        this.maxX = Math.max(cornerOne.getBlockX(), cornerTwo.getBlockX());
        this.minY = Math.min(cornerOne.getBlockY(), cornerTwo.getBlockY());
        this.maxY = Math.max(cornerOne.getBlockY(), cornerTwo.getBlockY());
        this.minZ = Math.min(cornerOne.getBlockZ(), cornerTwo.getBlockZ());
        this.maxZ = Math.max(cornerOne.getBlockZ(), cornerTwo.getBlockZ());
    }

    public Cuboid(String string) {
        String[] split = string.split(",");

        this.world = Bukkit.getWorld(split[0]);
        this.minX = Integer.valueOf(split[1]);
        this.maxX = Integer.valueOf(split[2]);
        this.minY = Integer.valueOf(split[3]);
        this.maxY = Integer.valueOf(split[4]);
        this.minZ = Integer.valueOf(split[5]);
        this.maxZ = Integer.valueOf(split[6]);
    }

    public String toString() {
        return this.world.getName() + "," + this.minX + "," + this.maxX + "," + this.minY + "," + this.maxY + "," + this.minZ + "," + this.maxZ;
    }

    public Location getCenter() {
        int x1 = maxX + 1;
        int z1 = maxZ + 1;

        return new Location(getWorld(), minX + (x1 - minX) / 2.0, 64, minZ + (z1 - minZ) / 2.0);
    }

    public Location getCenterWithY() {
        int x1 = maxX + 1;
        int z1 = maxZ + 1;
        int y1 = maxY + 1;

        return new Location(getWorld(), minX + (x1 - minX) / 2.0, minY + (y1 - minY) / 2.0, minZ + (z1 - minZ) / 2.0);
    }

    public boolean hasPlayerInside(Player player){
        return hasLocationInside(player.getLocation());
    }

    public boolean hasLocationInside(Location location ){
        return world.getName().equals(location.getWorld().getName())
                && location.getBlockX() >= minX && location.getBlockX() <= maxX
                && location.getBlockY() >= minY && location.getBlockY() <= maxY
                && location.getBlockZ() >= minZ && location.getBlockZ() <= maxZ;
    }

    public List<Location> getEdges() {
        final List<Location> edges = new ArrayList<>();

        final World world = getWorld();
        final Location one = world.getBlockAt(minX, 64, minZ).getLocation(),
                two = world.getBlockAt(maxX, 64, maxZ).getLocation();

        for(int x = one.getBlockX(); x <= two.getBlockX(); x++) {
            edges.add(new Location(world, x, world.getHighestBlockYAt(x, one.getBlockZ()), one.getBlockZ()));
            edges.add(new Location(world, x, world.getHighestBlockYAt(x, two.getBlockZ()), two.getBlockZ()));
        }

        for(int z = one.getBlockZ(); z <= two.getBlockZ(); z++) {
            edges.add(new Location(world, one.getBlockX(), world.getHighestBlockYAt(one.getBlockX(), z), z));
            edges.add(new Location(world, two.getBlockX(), world.getHighestBlockYAt(two.getBlockX(), z), z));
        }

        return edges;
    }

    public List<Location> getAllLocations() {
        final List<Location> list = new ArrayList<>();

        for(int x = minX; x <= maxX; x++) {
            for(int y = minY; y <= maxY; y++) {
                for(int z = minZ; z <= maxZ; z++) {
                    list.add(new Location(world, x, y, z));
                }
            }
        }
        return list;
    }
}