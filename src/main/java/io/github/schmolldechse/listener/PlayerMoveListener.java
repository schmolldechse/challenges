package io.github.schmolldechse.listener;

import io.github.schmolldechse.Plugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerMoveListener implements Listener {

    private final Plugin plugin;

    public PlayerMoveListener() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler
    public void execute(PlayerMoveEvent event) {
        if (!this.plugin.timerHandler.isPaused()) return;
        if (this.plugin.MOVEMENT_ALLOWED) return;

        Player player = event.getPlayer();
        if (player.isOp()) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        double x = Math.floor(from.getX());
        double z = Math.floor(from.getZ());
        if (Math.floor(to.getX()) != x || Math.floor(to.getZ()) != z) {
            x += .5;
            z += .5;

            player.teleport(new Location(from.getWorld(), x, from.getY(), z, from.getYaw(), from.getPitch()));
            player.teleport(player);
        }
    }
}
