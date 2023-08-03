package me.hostadam.events.impl.dtc;

import lombok.Getter;
import lombok.Setter;
import me.hostadam.events.api.HCFEvent;
import me.hostadam.events.api.HCFEventType;
import me.hostadam.events.api.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class DTCEvent extends HCFEvent implements ConfigurationSerializable {

	private Location location;
	private int remainingPoints, maxPoints;
	private long lastBreakTime = 0;
	
	public DTCEvent(String name, Location location) {
		super(name, HCFEventType.DTC);

		this.maxPoints = getEventPlugin().getConfig().getInt("events.dtc.defaultPoints");
		this.remainingPoints = maxPoints;
		this.location = location;
	}

	public DTCEvent(Map<String, Object> map) {
		super(map);

		this.location = LocationUtil.fromString((String) map.get("location"));
		this.maxPoints = (int) map.get("maxPoints");
		this.remainingPoints = maxPoints;
	}

	@Override
	public boolean isActive() {
		return this.getStartTime() != 0L;
	}

	@Override
	public void start() {
		if(this.isActive() || this.location == null) {
			return;
		}

		this.location.getBlock().setType(Material.OBSIDIAN);
		this.remainingPoints = this.maxPoints;
		this.setStartTime(System.currentTimeMillis());
		this.startTickTask(3);

		final String[] message = {
				" ",
				this.getEventType().getPrefix(),
				this.getEventType().getDefaultColor() + this.getName() + " §7has started.",
				" "
		};

		for(Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(message);
		}
	}

	@Override
	public void tick() {
		if(!this.isActive()) {
			this.cancelTickTask();
		} else if(this.shouldRegenerate()) {
			this.remainingPoints++;

			if(this.remainingPoints % 10 == 0) {
				Bukkit.broadcastMessage(this.getEventType().getSmallPrefix() + " " + this.getColor() + this.getName() + " §7is §aregenerating§7. §7§f(" + this.remainingPoints + "/" + this.maxPoints + "§7)");
			}
		}
	}

	@Override
	public void stop() {
		if(!this.isActive()) {
			return;
		}

		this.setStartTime(0);
		this.setLastBreakTime(0);
		this.setRemainingPoints(this.maxPoints);

		Bukkit.broadcastMessage(" ");
		Bukkit.broadcastMessage(this.getEventType().getPrefix());
		Bukkit.broadcastMessage(this.getColor() + this.getName() + " §7has been cancelled.");
		Bukkit.broadcastMessage(" ");
	}

	@Override
	public void capture(Player winner) {
		Bukkit.broadcastMessage(" ");
		Bukkit.broadcastMessage(this.getEventType().getPrefix());
		Bukkit.broadcastMessage(this.getColor() + this.getName() + " §7has been §cdestroyed§7 by §f" + winner.getName() + "§7.");
		Bukkit.broadcastMessage(" ");

		getEventPlugin().getEventHandler().stopEvent(this, false);

		this.cancelTickTask();
		this.setStartTime(0);
		this.setLastBreakTime(0);
		this.setRemainingPoints(this.maxPoints);
	}

	public boolean shouldRegenerate() {
		return this.remainingPoints < this.maxPoints
				&& (this.lastBreakTime > 0 && (System.currentTimeMillis() - this.lastBreakTime > TimeUnit.SECONDS.toMillis(30)));
	}

	public void handleBreak(BlockBreakEvent event) {
		if(!this.isActive()) {
			return;
		}

		event.setCancelled(true);

		this.remainingPoints--;
		this.lastBreakTime = System.currentTimeMillis();

		event.getBlock().setType(Material.AIR);
		Bukkit.getScheduler().runTaskLater(getEventPlugin(), () -> event.getBlock().setType(Material.OBSIDIAN), 2);

		if(this.remainingPoints <= 0) {
			this.capture(event.getPlayer());
		} else if(this.remainingPoints % 10 == 0 || remainingPoints <= 5) {
			Bukkit.broadcastMessage(this.getEventType().getSmallPrefix() + " " + this.getColor() + this.getName() + " §7is being §cdestroyed§7. §7§f(" + this.remainingPoints + "/" + this.maxPoints + "§7)");
		}
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();

		map.put("location", LocationUtil.toString(this.location));
		map.put("maxPoints", this.maxPoints);

		return map;
	}
}