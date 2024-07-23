package io.github.schmolldechse.team;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.team.argument.TeamArgument;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class TeamCommand {

    /**
     * /team clear
     * /team create <name>
     * /team delete <name>
     * /team <name> add <player>
     * /team <name> remove <player>
     */

    private final Plugin plugin;

    public TeamCommand(Commands commands) {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);

        final LiteralArgumentBuilder<CommandSourceStack> teamBuilder = Commands.literal("team")
                .then(
                        Commands.literal("clear")
                                .executes((source) -> {
                                    this.plugin.teamHandler.getRegisteredTeams().clear();
                                    source.getSource().getExecutor().sendMessage(Component.text("All teams cleared", NamedTextColor.GREEN));
                                    return 1;
                                })
                )
                .then(
                        Commands.literal("create")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes((source) -> {
                                            String name = StringArgumentType.getString(source, "name");
                                            if (name == null) return 0;

                                            if (this.plugin.teamHandler.exists(name)) {
                                                source.getSource().getExecutor().sendMessage(Component.text("(!) Team with name " + name + " already exists", NamedTextColor.RED));
                                                return 0;
                                            }

                                            this.plugin.teamHandler.register(new Team(name));
                                            source.getSource().getExecutor().sendMessage(Component.text("Team " + name + " created", NamedTextColor.GREEN));
                                            return 1;
                                        })
                                )
                )
                .then(
                        Commands.literal("delete")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes((source) -> {
                                            String name = StringArgumentType.getString(source, "name");
                                            if (name == null) return 0;

                                            if (!this.plugin.teamHandler.exists(name)) {
                                                source.getSource().getExecutor().sendMessage(Component.text("(!) Team with name " + name + " does not exist", NamedTextColor.RED));
                                                return 0;
                                            }

                                            Team team = this.plugin.teamHandler.team(name);
                                            this.plugin.teamHandler.unregister(team);

                                            source.getSource().getExecutor().sendMessage(Component.text("Team " + name + " deleted", NamedTextColor.GREEN));
                                            return 1;
                                        })
                                )
                )
                .then(
                        Commands.argument("team", new TeamArgument(this.plugin))
                                .then(
                                        Commands.literal("add")
                                                .then(Commands.argument("player", ArgumentTypes.player())
                                                        .executes((source) -> {
                                                            Team team = source.getArgument("team", Team.class);
                                                            Player player = source.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(source.getSource()).getFirst();

                                                            if (this.plugin.teamHandler.inTeam(player))
                                                                this.plugin.teamHandler.team(player).removeMember(player.getUniqueId());

                                                            team.addMember(player.getUniqueId());
                                                            source.getSource().getExecutor().sendMessage(Component.text("Player " + player.getName() + " added to team " + team.getName(), NamedTextColor.GREEN));

                                                            this.plugin.scoreboardHandler.playerList();
                                                            return 1;
                                                        })
                                                )
                                )
                                .then(
                                        Commands.literal("remove")
                                                .then(Commands.argument("player", ArgumentTypes.player())
                                                        .executes((source) -> {
                                                            Team team = source.getArgument("team", Team.class);
                                                            Player player = source.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(source.getSource()).getFirst();

                                                            if (!this.plugin.teamHandler.inTeam(player)) {
                                                                source.getSource().getExecutor().sendMessage(Component.text("(!) Player " + player.getName() + " is not in a team", NamedTextColor.RED));
                                                                return 0;
                                                            }

                                                            team.removeMember(player.getUniqueId());
                                                            source.getSource().getExecutor().sendMessage(Component.text("Player " + player.getName() + " removed from team " + team.getName(), NamedTextColor.GREEN));

                                                            this.plugin.scoreboardHandler.playerList();
                                                            return 1;
                                                        })
                                                )
                                )
                );
        commands.register(this.plugin.getPluginMeta(), teamBuilder.build(), "Command to create teams", List.of());
    }
}
