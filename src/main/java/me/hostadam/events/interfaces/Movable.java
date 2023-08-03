package me.hostadam.events.interfaces;

import org.bukkit.event.player.PlayerMoveEvent;

public interface Movable {

    void handleMove(PlayerMoveEvent event);
}
