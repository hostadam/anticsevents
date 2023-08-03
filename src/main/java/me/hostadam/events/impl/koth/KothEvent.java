package me.hostadam.events.impl.koth;

import lombok.Getter;
import lombok.Setter;
import me.hostadam.events.api.HCFEvent;
import me.hostadam.events.api.HCFEventType;
import me.hostadam.events.api.util.Cuboid;
import me.hostadam.events.api.util.TimeUtil;
import me.hostadam.events.interfaces.Deathable;
import me.hostadam.events.interfaces.Movable;
import me.hostadam.events.interfaces.Quittable;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class KothEvent extends HCFEvent implements ConfigurationSerializable, Movable, Deathable, Quittable {

	private Player capper = null;
	private Cuboid zone;
	private long captureStartTime = 0,
			captureDelay = 0,
			captureTime;

	public KothEvent(String name, Cuboid cuboid) {
		super(name, HCFEventType.KOTH);

		this.zone = cuboid;
		this.captureTime = TimeUnit.MINUTES.toMillis(5);
	}

	public KothEvent(Map<String, Object> map) {
		super(map);

		this.zone = String.valueOf(map.get("zone")).isEmpty() ? null : new Cuboid(String.valueOf(map.get("zone")));
		this.captureTime = Long.valueOf((int) map.get("captureTime"));
	}
	
	@Override
	public boolean isActive() {
		return this.getStartTime() != 0;
	}

	@Override
	public void start() {
		if(this.isActive() || this.zone == null) {
			return;
		}

		this.setStartTime(System.currentTimeMillis());
		this.startTickTask(1);
		Bukkit.broadcastMessage(" ");
		Bukkit.broadcastMessage(this.getEventType().getPrefix());
		Bukkit.broadcastMessage(this.getColor() + this.getName() + " §7has started.");
		Bukkit.broadcastMessage(" ");
	}

	@Override
	public void tick() {
		if(this.isBeingCaptured() && this.capper != null && this.getRemaining() < 500) {
			this.capture(this.capper);
		}
	}

	@Override
	public void stop() {
		if(!this.isActive()) {
			return;
		}

		if(this.getCapper() != null) {
			this.knock();
			this.setCapper(null);
		}

		this.cancelTickTask();
		this.setCaptureStartTime(0L);
		this.setCaptureDelay(0);
		this.setStartTime(0);
		Bukkit.broadcastMessage(" ");
		Bukkit.broadcastMessage(this.getEventType().getPrefix());
		Bukkit.broadcastMessage(this.getColor() + this.getName() + " §7has been cancelled.");
		Bukkit.broadcastMessage(" ");
	}

	@Override
	public void capture(Player winner) {
		Bukkit.broadcastMessage(" ");
		Bukkit.broadcastMessage(this.getEventType().getPrefix());
		Bukkit.broadcastMessage(this.getColor() + this.getName() + " §7has been §acaptured§7 by §f" + winner.getName() + "§7.");
		Bukkit.broadcastMessage(" ");

		getEventPlugin().getEventHandler().stopEvent(this, false);

		this.cancelTickTask();
		this.setCaptureStartTime(0L);
		this.setCaptureDelay(0);
		this.setStartTime(0);
		this.setCapper(null);
	}

	public boolean isBeingCaptured() {
		return this.captureStartTime != 0;
	}

	public long getRemaining() {
		if(!this.isBeingCaptured()) {
			return this.captureTime;
		}

		long remainingTime = (this.captureStartTime + this.captureTime) - System.currentTimeMillis();

		/**
		 * This resolves the issue where the scoreboard would display the KOTH time as negative.
		 */
		return (remainingTime < 0 ? 0 : remainingTime);
	}

	public void knock() {
		if(getRemaining() < (this.captureTime - 30000)) {
			Bukkit.broadcastMessage(" ");
			Bukkit.broadcastMessage(this.getEventType().getPrefix());
			Bukkit.broadcastMessage(this.getColor() + this.getName() + " §7has been §cknocked§7. (§f" + TimeUtil.formatSimple(this.getRemaining()) + "§7)");
			Bukkit.broadcastMessage(" ");
		}

		this.setCapper(null);
		this.setCaptureStartTime(0L);
		this.setCaptureDelay(System.currentTimeMillis() + 1000);
	}

	@Override
	public void handleMove(PlayerMoveEvent event) {
		if(!this.isActive() || this.zone == null) {
			return;
		}

		final Player player = event.getPlayer();
		final boolean fromWithin = this.zone.hasLocationInside(event.getFrom()),
				toWithin = this.zone.hasLocationInside(event.getTo());

		if(this.capper != null && this.capper.getUniqueId() == player.getUniqueId() && fromWithin && !toWithin) {
			this.knock();
		} else if(this.capper == null && !fromWithin && toWithin && !player.isDead()){
			if(player.getGameMode() != GameMode.SURVIVAL) {
				return;
			}

			this.capper = player;
			this.captureStartTime = System.currentTimeMillis();

			if(System.currentTimeMillis() > this.captureDelay) {
				Bukkit.broadcastMessage(this.getEventType().getSmallPrefix() + " " + this.getColor() + this.getName() + " §7is being §acaptured§7. (§f" + TimeUtil.formatSimple(this.getRemaining()) + "§7)");
				this.captureDelay = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
			}

			player.sendMessage("§aYou are now controlling " + this.getColor() + this.getName() + "§a.");
		}
	}

	@Override
	public void handleDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();

		if(this.capper != null && this.capper.getUniqueId().equals(player.getUniqueId())) {
			this.knock();
		}
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = super.serialize();

		map.put("zone", (this.zone == null ? "" : this.zone.toString()));
		map.put("captureTime", this.captureTime);

		return map;
	}

	@Override
	public void handleQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		if(this.capper != null && this.capper.getUniqueId().equals(player.getUniqueId())) {
			this.knock();
		}
	}
}