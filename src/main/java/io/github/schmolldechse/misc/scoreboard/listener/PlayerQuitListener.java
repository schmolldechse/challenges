package io.github.schmolldechse.misc.scoreboard.listener;

import io.github.schmolldechse.Plugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerQuitListener implements Listener {

    private final Plugin plugin;

    public PlayerQuitListener() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler
    public void execute(PlayerQuitEvent event) {
        this.plugin.scoreboardHandler.playerList();
    }
}
