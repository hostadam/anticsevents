package me.hostadam.events;

import lombok.Getter;
import me.hostadam.events.command.EventCommand;
import me.hostadam.events.listener.HCFEventListener;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class EventPlugin extends JavaPlugin {

    @Getter
    private static EventPlugin instance;

    @Getter
    private HCFEventHandler eventHandler;

    private final List<EntityType> types = new ArrayList<>();

    @Override
    public void onEnable() {
        instance = this;

        if(!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        saveDefaultConfig();
        this.eventHandler = new HCFEventHandler(this);
        getServer().getPluginManager().registerEvents(new HCFEventListener(), this);

        getServer().getCommandMap().register("event", new EventCommand());
    }

    @EventHandler
    public void onDisable() {
        this.eventHandler.onDisable();
    }
}
