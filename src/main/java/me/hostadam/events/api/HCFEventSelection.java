package me.hostadam.events.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import me.hostadam.events.api.util.Cuboid;
import me.hostadam.events.impl.koth.KothEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class HCFEventSelection {

    private static Map<UUID, HCFEventSelection> SELECTIONS = new HashMap<>();

    private UUID uuid;
    private KothEvent kothEvent;
    private Location locationOne, locationTwo;

    public HCFEventSelection(UUID uuid, KothEvent kothEvent) {
        this.uuid = uuid;
        this.kothEvent = kothEvent;
        this.locationOne = null;
        this.locationTwo = null;

        SELECTIONS.put(uuid, this);
    }

    public void clickBlock(Player player, Action action, Block block) {
        if(action.isLeftClick()) {
            locationOne = block.getLocation();
        } else if(action.isRightClick()) {
            locationTwo = block.getLocation();
        }

        player.sendMessage("§aSet corner " + (action == Action.RIGHT_CLICK_BLOCK ? "two" : "one") + " §afor " + kothEvent.getDisplayName() + "§a.");
    }

    public void confirm(Player player) {
        Cuboid cuboid = new Cuboid(this.locationOne, this.locationTwo);
        kothEvent.setZone(cuboid);
        player.sendMessage("§aUpdated the cap zone for §l" + kothEvent.getDisplayName() + "§a.");

        SELECTIONS.remove(player.getUniqueId());
    }

    public void cancel(Player player) {
        SELECTIONS.remove(player.getUniqueId());
    }

    public static void delete(Player player) {
        SELECTIONS.remove(player.getUniqueId());
    }

    public static HCFEventSelection getByPlayer(Player player) {
        return SELECTIONS.get(player.getUniqueId());
    }
}
