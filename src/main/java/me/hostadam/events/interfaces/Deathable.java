package me.hostadam.events.interfaces;

import org.bukkit.event.entity.PlayerDeathEvent;

public interface Deathable {

    void handleDeath(PlayerDeathEvent event);
}
