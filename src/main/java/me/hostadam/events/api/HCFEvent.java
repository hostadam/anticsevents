package me.hostadam.events.api;

import lombok.Data;
import me.hostadam.events.EventPlugin;
import me.hostadam.events.task.HCFEventTask;
import org.bukkit.ChatColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@Data
public abstract class HCFEvent implements ConfigurationSerializable {

	private final EventPlugin eventPlugin = EventPlugin.getInstance();
	private final String name;
	private final HCFEventType eventType;

	private ChatColor color;
	private long startTime = 0;
	private HCFEventTask tickTask;

	public HCFEvent(String name, HCFEventType type) {
		this.name = name;
		this.eventType = type;
		this.color = type.getDefaultColor();
	}

	public HCFEvent(Map<String, Object> map) {
		this.name = (String) map.get("name");
		this.eventType = HCFEventType.valueOf((String) map.get("eventType"));
		this.color = ChatColor.valueOf((String) map.get("color"));
	}

	public String getDisplayName() {
		return this.color + this.name;
	}

	public void startTickTask(int seconds) {
		this.tickTask = new HCFEventTask(this);
		this.tickTask.runTaskTimer(this.eventPlugin, seconds * 20L,seconds * 20L);
	}

	public void cancelTickTask() {
		if(this.tickTask == null) {
			return;
		}

		this.tickTask.cancel();
		this.tickTask = null;
	}

	public long getRunTime() {
		return System.currentTimeMillis() - startTime;
	}
	
	public abstract boolean isActive();
	public abstract void start();
	public abstract void tick();
	public abstract void stop();
	public abstract void capture(Player winner);

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();

		map.put("name", this.name);
		map.put("eventType", this.eventType.name());
		map.put("color", this.color.name());

		return map;
	}
}