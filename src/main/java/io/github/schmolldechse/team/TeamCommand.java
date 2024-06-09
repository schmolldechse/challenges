package io.github.schmolldechse.team;

import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import io.github.schmolldechse.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class TeamCommand {

    /**
     * /team clear
     * /team -c | -create <name>
     * /team -d | -delete <name>
     * /team <name> add <player>
     * /team <name> remove <player>
     */

    private final Plugin plugin;

    public TeamCommand() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
    }

    public void registerCommand() {
        new CommandTree("team")
                .then(new LiteralArgument("-create", "-c")
                        .then(new StringArgument("name")
                                .executes((sender, args) -> {
                                    String name = (String) args.get("name");
                                    if (name == null) return;

                                    if (this.plugin.teamHandler.exists(name)) {
                                        sender.sendMessage(Component.text("(!) Team with name " + name + " already exists", NamedTextColor.RED));
                                        return;
                                    }

                                    this.plugin.teamHandler.register(new Team(name));
                                    sender.sendMessage(Component.text("Team " + name + " created", NamedTextColor.GREEN));
                                })))
                .then(new LiteralArgument("-delete", "-d")
                        .then(new StringArgument("name")
                                .executes((sender, args) -> {
                                    String name = (String) args.get("name");
                                    if (name == null) return;

                                    if (!this.plugin.teamHandler.exists(name)) {
                                        sender.sendMessage(Component.text("(!) Team with name " + name + " does not exist", NamedTextColor.RED));
                                        return;
                                    }

                                    Team team = this.plugin.teamHandler.team(name);
                                    this.plugin.teamHandler.unregister(team);

                                    sender.sendMessage(Component.text("Team " + name + " deleted", NamedTextColor.GREEN));
                                })))
                .then(teamArgument()
                        .then(new LiteralArgument("add")
                                .then(new PlayerArgument("player")
                                        .executes((sender, args) -> {
                                            Team team = (Team) args.get("name");
                                            Player player = (Player) args.get("player");

                                            if (this.plugin.teamHandler.inTeam(player)) {
                                                sender.sendMessage(Component.text("(!) Player " + player.getName() + " is already in a team", NamedTextColor.RED));
                                                return;
                                            }

                                            team.addMember(player.getUniqueId());
                                            sender.sendMessage(Component.text("Player " + player.getName() + " added to team " + team.getName(), NamedTextColor.GREEN));
                                        })))
                        .then(new LiteralArgument("remove")
                                .then(new PlayerArgument("player")
                                        .executes((sender, args) -> {
                                            Team team = (Team) args.get("name");
                                            Player player = (Player) args.get("player");

                                            if (!this.plugin.teamHandler.inTeam(player)) {
                                                sender.sendMessage(Component.text("(!) Player " + player.getName() + " is not in a team", NamedTextColor.RED));
                                                return;
                                            }

                                            team.removeMember(player.getUniqueId());
                                            sender.sendMessage(Component.text("Player " + player.getName() + " removed from team " + team.getName(), NamedTextColor.GREEN));
                                        }))))
                .then(new LiteralArgument("clear")
                        .executes((sender, args) -> {
                            this.plugin.teamHandler.getRegisteredTeams().clear();
                            sender.sendMessage(Component.text("Cleared team list", NamedTextColor.GREEN));
                        }))
                .register();
    }

    private Argument<Team> teamArgument() {
        return new CustomArgument<Team, String>(new StringArgument("name"), info -> {
            Team team = this.plugin.teamHandler.team(info.input());
            if (team == null) throw CustomArgument.CustomArgumentException.fromMessageBuilder(new CustomArgument.MessageBuilder("Unknown team: ").appendArgInput());
            else return team;
        }).replaceSuggestions(ArgumentSuggestions.strings(info ->
            this.plugin.teamHandler.getRegisteredTeams().stream().map(Team::getName).toArray(String[]::new)
        ));
    }
}
