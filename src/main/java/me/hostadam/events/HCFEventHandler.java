package me.hostadam.events;

import lombok.Getter;
import me.hostadam.events.api.HCFEvent;
import me.hostadam.events.api.HCFEventType;
import me.hostadam.events.impl.dtc.DTCEvent;
import me.hostadam.events.impl.koth.KothEvent;

import java.util.*;
import java.util.stream.Collectors;

public class HCFEventHandler {

	private final EventPlugin plugin;
	private final Map<String, HCFEvent> eventMap;

	@Getter
	private final Set<HCFEvent> activeEvents = new HashSet<>();
	
	public HCFEventHandler(EventPlugin eventPlugin) {
		this.plugin = eventPlugin;
		this.eventMap = new TreeMap<>(
				String.CASE_INSENSITIVE_ORDER);

		this.load();
	}

	public void onDisable() {
		this.save();
	}

	public void createEvent(HCFEvent event) {
		this.eventMap.put(event.getName(), event);
	}

	public void removeEvent(HCFEvent event) {
		this.eventMap.remove(event.getName());

		if(plugin.getConfig().contains("events." + event.getName())) {
			plugin.getConfig().set("events." + event.getName(), "");
			plugin.saveConfig();
		}
	}

	public void startEvent(HCFEvent event, boolean callSuper) {
		if(callSuper) event.start();
		this.activeEvents.add(event);
	}

	public void stopEvent(HCFEvent event, boolean callSuper) {
		if(callSuper) event.stop();
		this.activeEvents.remove(event);
	}
	
	public HCFEvent getEvent(String name) {
		return this.eventMap.get(name);
	}

	public Collection<HCFEvent> getEvents() {
		return this.eventMap.values();
	}

	public void save() {
		for(String string : plugin.getConfig().getConfigurationSection("events").getKeys(false)) {
			plugin.getConfig().set(string, null);
		}

		for(HCFEvent event : this.eventMap.values()) {
			plugin.getConfig().set("events." + event.getName(), event.serialize());
		}

		plugin.saveConfig();
	}

	public void load() {
		for(String eventName : plugin.getConfig().getConfigurationSection("events").getKeys(false)) {
			try {
				final Map<String, Object> data = plugin.getConfig().getConfigurationSection("events." + eventName).getValues(false);
				final HCFEventType type = HCFEventType.valueOf(String.valueOf(data.get("eventType")));

				switch(type) {
					case KOTH:
						KothEvent kothEvent = new KothEvent(data);
						this.eventMap.put(kothEvent.getName(), kothEvent);
						break;
					case DTC:
						DTCEvent dtcEvent = new DTCEvent(data);
						this.eventMap.put(dtcEvent.getName(), dtcEvent);
						break;
				}
			} catch(Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	public List<HCFEvent> getEvents(HCFEventType type) {
		return this.getEvents().stream()
				.filter(hcfEvent -> hcfEvent.getEventType() == type)
				.collect(Collectors.toList()
		);
	}

	public List<HCFEvent> getActiveEvents(HCFEventType type) {
		return this.getActiveEvents().stream()
				.filter(hcfEvent -> hcfEvent.getEventType() == type)
				.collect(Collectors.toList()
		);
	}

	public Set<HCFEvent> getActiveEvents() {
		this.activeEvents.removeIf(hcfEvent -> !hcfEvent.isActive());
		return this.activeEvents;
	}
}