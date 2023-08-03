package me.hostadam.events.command;

import com.google.common.primitives.Ints;
import me.hostadam.events.EventPlugin;
import me.hostadam.events.HCFEventHandler;
import me.hostadam.events.api.HCFEvent;
import me.hostadam.events.api.HCFEventSelection;
import me.hostadam.events.api.HCFEventType;
import me.hostadam.events.api.util.TimeUtil;
import me.hostadam.events.impl.dtc.DTCEvent;
import me.hostadam.events.impl.koth.KothEvent;
import me.hostadam.events.listener.HCFEventListener;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class EventCommand extends Command {

    private final EventPlugin eventPlugin = EventPlugin.getInstance();

    public EventCommand() {
        super("event", "Event-related commands", "/event", Arrays.asList("dtc", "koth"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, String s, @NotNull String[] args) {
        if(args.length == 0) {
            sender.sendMessage("§6§lEvent Usage");
            sender.sendMessage("§7<> (required) [] (optional)");
            sender.sendMessage(" ");
            sender.sendMessage("§e/event list §7- list all events");
            sender.sendMessage("§e/event delete <name> §7- delete an event");
            sender.sendMessage("§e/event start <name> §7- start an event");
            sender.sendMessage("§e/event stop <name> §7- stop an events");
            sender.sendMessage("§e/event tp <name> §7- teleport to an event");
            sender.sendMessage("§e/event setlocation <dtc> §7- set the location of a dtc");
            sender.sendMessage("§e/event claimfor <koth> §7- get a selection wand to claim capzone");
            sender.sendMessage("§e/event create <name> <type> §7- create an event");
            sender.sendMessage("§e/event setcolor <name> <color> §7- set the color of an event");
            sender.sendMessage("§e/event setcaptime <koth> <time> §7- set the captime of a koth");
            sender.sendMessage("§e/event setpoints <dtc> <points> §7- set the points to win a dtc");
            return true;
        }

        final HCFEventHandler eventHandler = this.eventPlugin.getEventHandler();

        switch(args[0].toLowerCase()) {
            case "list":
                if(eventHandler.getEvents().isEmpty()) {
                    sender.sendMessage("§cNo events found.");
                    return true;
                }

                sender.sendMessage(" ");
                for(HCFEvent event : eventHandler.getEvents()) {
                    if(!(sender instanceof Player player)) {
                        sender.sendMessage("§8\u25A0 " + event.getColor() + event.getName() + "§7 (" + event.getEventType().getPrefix() + "§7): " + (event.isActive() ? "§aActive" : "§cNot Active"));
                    } else {
                        TextComponent textComponent = new TextComponent("§8\u25A0 " + event.getColor() + event.getName() + "§7 (" + event.getEventType().getPrefix() + "§7): " + (event.isActive() ? "§aActive" : "§cNot Active"));
                        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§aClick to teleport.").create()));
                        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event tp " + event.getName()));
                        player.sendMessage(textComponent);
                    }
                }

                sender.sendMessage(" ");
                return true;
            case "delete":
                if(!sender.hasPermission("hcfevent.command.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }

                if(args.length < 2) {
                    sender.sendMessage("§cInvalid usage: /event delete <name>");
                    return true;
                }

                HCFEvent hcfEvent = eventHandler.getEvent(args[1]);
                if(hcfEvent == null) {
                    sender.sendMessage("§cThis event does not exist.");
                    return true;
                }

                if(hcfEvent.isActive()) {
                    eventHandler.stopEvent(hcfEvent, true);
                }

                eventHandler.removeEvent(hcfEvent);
                sender.sendMessage("§aDeleted " + hcfEvent.getName() + "§a.");
                return true;
            case "start":
                if(!sender.hasPermission("hcfevent.command.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }

                if(args.length < 2) {
                    sender.sendMessage("§cInvalid usage: /event start <name>");
                    return true;
                }

                hcfEvent = eventHandler.getEvent(args[1]);
                if(hcfEvent == null) {
                    sender.sendMessage("§cInvalid event.");
                    return true;
                }

                if(hcfEvent.isActive()) {
                    sender.sendMessage("§cThis event is already active.");
                    return true;
                }

                eventHandler.startEvent(hcfEvent, true);
                return true;
            case "stop":
            case "cancel":
                if(!sender.hasPermission("hcfevent.command.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }

                if(args.length < 2) {
                    sender.sendMessage("§cInvalid usage: /event stop <name>");
                    return true;
                }

                hcfEvent = eventHandler.getEvent(args[1]);
                if(hcfEvent == null) {
                    sender.sendMessage("§cInvalid event.");
                    return true;
                }

                if(!hcfEvent.isActive()) {
                    sender.sendMessage("§cThis event is not active.");
                    return true;
                }

                eventHandler.stopEvent(hcfEvent, true);
                return true;
            case "tp":
            case "teleport":
                if(!sender.hasPermission("hcfevent.command.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }

                if(args.length < 2) {
                    sender.sendMessage("§cInvalid usage: /event tp <name>");
                    return true;
                }

                if(!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can do this.");
                    return true;
                }

                hcfEvent = eventHandler.getEvent(args[1]);
                if(hcfEvent instanceof KothEvent kothEvent) {
                    if(kothEvent.getZone() == null) {
                        sender.sendMessage("§c" + kothEvent.getName() + " does not have a capzone set.");
                        return true;
                    }

                    player.teleport(kothEvent.getZone().getCenter());
                } else if(hcfEvent instanceof DTCEvent dtcEvent) {
                    if(dtcEvent.getLocation() == null) {
                        sender.sendMessage("§c" + dtcEvent.getName() + " does not have a location set.");
                        return true;
                    }

                    player.teleport(dtcEvent.getLocation());
                }

                return true;
            case "setlocation":
                if(!sender.hasPermission("hcfevent.command.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }

                hcfEvent = eventHandler.getEvent(args[1]);
                if(hcfEvent == null || !(hcfEvent instanceof DTCEvent dtcEvent)) {
                    sender.sendMessage("§cInvalid event.");
                    return true;
                }

                if(!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can do this.");
                    return true;
                }

                Block block = player.getTargetBlock(null, 5);
                if(block == null || block.getType() != Material.OBSIDIAN) {
                    sender.sendMessage("§cYou are not looking at a block within 5 blocks.");
                    return true;
                }

                dtcEvent.setLocation(block.getLocation());
                sender.sendMessage("§aUpdated the location for " + dtcEvent.getColor() + dtcEvent.getName() + " §ato §f" + block.getLocation().getBlockX() + ", " + block.getLocation().getBlockZ() + "§a.");
                return true;
            case "claimfor":
                if(!sender.hasPermission("hcfevent.command.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }

                hcfEvent = eventHandler.getEvent(args[1]);
                if(hcfEvent == null || !(hcfEvent instanceof KothEvent kothEvent)) {
                    sender.sendMessage("§cInvalid event.");
                    return true;
                }

                if(!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can do this.");
                    return true;
                }

                new HCFEventSelection(player.getUniqueId(), kothEvent);
                player.getInventory().addItem(HCFEventListener.SELECTION_WAND);
                player.sendMessage("§aStarted capzone selection for §f" + hcfEvent.getDisplayName() + "§e.");
                player.sendMessage("§aLeft click to select point one, right click to select point two, middle + left click to confirm.");
                return true;
            case "create":
                if(!sender.hasPermission("hcfevent.command.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }

                if(args.length < 3) {
                    sender.sendMessage("§cInvalid usage: /event create <name> <type>");
                    return true;
                }

                if(eventHandler.getEvent(args[1]) != null) {
                    sender.sendMessage("§cThis event already exists.");
                    return true;
                }

                HCFEventType type;
                try {
                    type = HCFEventType.valueOf(args[2].toUpperCase());
                } catch(Exception exception) {
                    sender.sendMessage("§cInvalid event type.");
                    return true;
                }

                HCFEvent event;

                switch(type) {
                    case KOTH:
                        event = new KothEvent(args[1], null);
                        break;
                    case DTC:
                        event = new DTCEvent(args[1], null);
                        break;
                    default:
                        return true;
                }

                eventHandler.createEvent(event);
                sender.sendMessage("§eCreated a new Event.");
                return true;
            case "setcolor":
                if(!sender.hasPermission("hcfevent.command.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }

                if(args.length < 3) {
                    sender.sendMessage("§cInvalid usage: /event setcolor <name> <color>");
                    return true;
                }

                hcfEvent = eventHandler.getEvent(args[1]);
                if(hcfEvent == null) {
                    sender.sendMessage("§cInvalid event.");
                    return true;
                }

                ChatColor color;

                try {
                    color = ChatColor.valueOf(args[2]);
                } catch(Exception exception) {
                    sender.sendMessage("§cInvalid color.");
                    return true;
                }

                hcfEvent.setColor(color);
                sender.sendMessage("§eYou have set the color for " + hcfEvent.getName() + " §eto " + color + color.name() + "§e.");
                return true;
            case "setcaptime":
                if(!sender.hasPermission("hcfevent.command.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }

                if(args.length < 3) {
                    sender.sendMessage("§cInvalid usage: /event setcaptime <koth> <time>");
                    return true;
                }

                hcfEvent = eventHandler.getEvent(args[1]);
                if(hcfEvent == null || !(hcfEvent instanceof KothEvent kothEvent)) {
                    sender.sendMessage("§cInvalid event.");
                    return true;
                }

                long duration = TimeUtil.parse(args[2]);
                if(duration == -1) {
                    sender.sendMessage("§cInvalid time.");
                    return true;
                }

                kothEvent.setCaptureTime(duration);
                sender.sendMessage("§eSet captime to §f" + TimeUtil.formatSimple(kothEvent.getCaptureTime()) + "§e.");

                if(kothEvent.isBeingCaptured() && kothEvent.getRemaining() > kothEvent.getCaptureTime()) {
                    kothEvent.setCaptureStartTime(System.currentTimeMillis());
                }

                return true;
            case "setpoints":
                if(!sender.hasPermission("hcfevent.command.admin")) {
                    sender.sendMessage("§cNo permission.");
                    return true;
                }

                if(args.length < 3) {
                    sender.sendMessage("§cInvalid usage: /event setpoints <dtc> <points>");
                    return true;
                }

                hcfEvent = eventHandler.getEvent(args[1]);
                if(hcfEvent == null || !(hcfEvent instanceof DTCEvent dtcEvent)) {
                    sender.sendMessage("§cInvalid event.");
                    return true;
                }

                if(Ints.tryParse(args[2]) == null) {
                    sender.sendMessage("§cInvalid number.");
                    return true;
                }

                int points = Integer.parseInt(args[2]);
                if(points <= 0) {
                    sender.sendMessage("§cMust be higher than 0.");
                    return true;
                }

                dtcEvent.setMaxPoints(points);

                if(dtcEvent.getRemainingPoints() > points) {
                    dtcEvent.setRemainingPoints(points);
                }

                sender.sendMessage("§eSet max points to §b" + points + "§e.");
                return true;
            default:
                sender.sendMessage("§cInvalid usage.");
                return true;
        }
    }
}
