package io.github.schmolldechse.misc.scoreboard;

import com.google.inject.Inject;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.misc.scoreboard.listener.PlayerJoinListener;
import io.github.schmolldechse.misc.scoreboard.listener.PlayerQuitListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ScoreboardHandler implements Listener {

    private final Plugin plugin;

    @Inject
    public ScoreboardHandler() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);

        new PlayerJoinListener();
        new PlayerQuitListener();
    }

    public void clear() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.getScoreboard().getTeams().forEach(Team::unregister);
        });
    }

    public Team scoreboardTeam(Player player) {
        return player.getScoreboard().getEntityTeam(player);
    }

    public void playerList() {
        Bukkit.getOnlinePlayers().forEach(this::playerList);
    }

    public void playerList(Player player) {
        Scoreboard scoreboard = player.getScoreboard();

        this.plugin.teamHandler.getRegisteredTeams().forEach(team -> {
            Team scoreboardTeam = scoreboard.getTeam(team.getName());
            if (scoreboardTeam == null) scoreboardTeam = scoreboard.registerNewTeam(team.getName());

            NamedTextColor color = team.getUuids().contains(player.getUniqueId()) ? NamedTextColor.GREEN : NamedTextColor.YELLOW;

            scoreboardTeam.prefix(Component.text("[#" + team.getName() + "] ", color));
            scoreboardTeam.color(NamedTextColor.WHITE); // players name color

            Team finalScoreboardTeam = scoreboardTeam;
            team.getUuids().forEach(uuid -> {
                Player teamPlayer = Bukkit.getPlayer(uuid);
                if (teamPlayer != null) finalScoreboardTeam.addEntity(teamPlayer);
            });
        });
    }
}
