package me.hostadam.events.task;

import lombok.AllArgsConstructor;
import me.hostadam.events.api.HCFEvent;
import org.bukkit.scheduler.BukkitRunnable;

@AllArgsConstructor
public class HCFEventTask extends BukkitRunnable {

    private final HCFEvent event;

    @Override
    public void run() {
        this.event.tick();
    }
}
