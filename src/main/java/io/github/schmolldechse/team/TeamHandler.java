package io.github.schmolldechse.team;

import com.google.inject.Inject;
import io.github.schmolldechse.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class TeamHandler {

    private final Plugin plugin;

    private final List<Team> registeredTeams = new ArrayList<>();

    @Inject
    public TeamHandler() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
    }

    public void register(Team team) {
        this.registeredTeams.add(team);
    }

    public void unregister(Team team) {
        this.registeredTeams.remove(team);
    }

    public Team team(String name) {
        return this.registeredTeams.stream()
                .filter(team -> team.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public Team team(Player player) {
        return this.registeredTeams.stream()
                .filter(team -> team.getUuids().contains(player.getUniqueId()))
                .findFirst()
                .orElse(null);
    }

    public boolean exists(String name) {
        return this.registeredTeams.stream()
                .anyMatch(team -> team.getName().equals(name));
    }

    public boolean inTeam(Player player) {
        return this.registeredTeams.stream()
                .anyMatch(team -> team.getUuids().contains(player.getUniqueId()));
    }

    public List<Team> getRegisteredTeams() {
        return registeredTeams;
    }
}
