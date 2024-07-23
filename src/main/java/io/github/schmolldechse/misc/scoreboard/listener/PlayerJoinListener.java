package io.github.schmolldechse.misc.scoreboard.listener;

import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.team.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerJoinListener implements Listener {

    private final Plugin plugin;

    public PlayerJoinListener() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void execute(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Team team = this.plugin.teamHandler.team(player);
        if (team == null) return;

        this.plugin.scoreboardHandler.playerList();
    }
}
