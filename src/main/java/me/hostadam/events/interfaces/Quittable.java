package me.hostadam.events.interfaces;

import org.bukkit.event.player.PlayerQuitEvent;

public interface Quittable {

    void handleQuit(PlayerQuitEvent event);
}
