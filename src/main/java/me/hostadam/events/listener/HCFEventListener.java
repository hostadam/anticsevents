package me.hostadam.events.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.hostadam.events.EventPlugin;
import me.hostadam.events.api.HCFEvent;
import me.hostadam.events.api.HCFEventSelection;
import me.hostadam.events.api.HCFEventType;
import me.hostadam.events.impl.dtc.DTCEvent;
import me.hostadam.events.interfaces.Deathable;
import me.hostadam.events.interfaces.Movable;
import me.hostadam.events.interfaces.Quittable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class HCFEventListener implements Listener {

    public static final ItemStack SELECTION_WAND = new ItemStack(Material.GOLDEN_AXE);
    private final EventPlugin plugin = EventPlugin.getInstance();

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        HCFEventSelection selection = HCFEventSelection.getByPlayer(player);
        if(!event.hasItem() || selection == null || !event.getItem().isSimilar(SELECTION_WAND)) {
            return;
        }

        event.setCancelled(true);

        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            selection.cancel(player);
        } else if (player.isSneaking() && event.getAction() == Action.LEFT_CLICK_AIR) {
            if (selection.getLocationOne() == null || selection.getLocationTwo() == null) {
                player.sendMessage("§cYou must set both corners.");
                return;
            }

            selection.confirm(player);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            selection.clickBlock(player, event.getAction(), event.getClickedBlock());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        HCFEventSelection selection = HCFEventSelection.getByPlayer(player);

        if (selection != null) {
            if (player.getInventory().contains(SELECTION_WAND)) {
                player.getInventory().removeItem(SELECTION_WAND);
            }

            HCFEventSelection.delete(player);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (HCFEventSelection.getByPlayer(player) != null && event.getItemDrop().getItemStack().isSimilar(SELECTION_WAND)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        if(from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
            return;
        }

        for(HCFEvent hcfEvent : plugin.getEventHandler().getActiveEvents()) {
            if(!(hcfEvent instanceof Movable movable)) {
                continue;
            }

            movable.handleMove(event);
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        for(HCFEvent hcfEvent : plugin.getEventHandler().getActiveEvents()) {
            if(!(hcfEvent instanceof Movable movable)) {
                continue;
            }

            movable.handleMove(event);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        for(HCFEvent hcfEvent : plugin.getEventHandler().getActiveEvents()) {
            if(!(hcfEvent instanceof Deathable deathable)) {
                continue;
            }

            deathable.handleDeath(event);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        for(HCFEvent hcfEvent : plugin.getEventHandler().getActiveEvents()) {
            if(!(hcfEvent instanceof Quittable quittable)) {
                continue;
            }

            quittable.handleQuit(event);
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        final Block block = event.getBlock();
        final List<HCFEvent> events = plugin.getEventHandler().getActiveEvents(HCFEventType.DTC);

        if(events.isEmpty()) {
            return;
        }

        for(HCFEvent hcfEvent : events) {
            DTCEvent dtcEvent = (DTCEvent) hcfEvent;
            if(block.getType() != Material.OBSIDIAN
                    || !validateLocations(dtcEvent.getLocation(), block.getLocation())) {
                continue;
            }

            dtcEvent.handleBreak(event);
        }
    }

    private boolean validateLocations(Location location, Location other) {
        if(other == null || location == null) {
            return false;
        }

        return location.getWorld().getName().equals(other.getWorld().getName())
                && location.getBlockX() == other.getBlockX() && location.getBlockY() == other.getBlockY() && location.getBlockZ() == other.getBlockZ();
    }

    static {
        SELECTION_WAND.editMeta(meta -> {
            meta.setDisplayName("§6Selection Wand");
        });
    }
}
