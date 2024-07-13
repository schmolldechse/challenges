package io.github.schmolldechse.challenge.map.challenge.forcebattle.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.challenge.map.challenge.forcebattle.ForcebattleChallenge;
import io.github.schmolldechse.challenge.map.challenge.forcebattle.ForcebattleTask;
import io.github.schmolldechse.challenge.map.challenge.forcebattle.team.ForcebattleExtension;
import io.github.schmolldechse.team.Team;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class SkipCommand {

    // /skip <player>

    private final Plugin plugin;

    public SkipCommand(Commands commands) {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);

        final LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("skip")
                .then(
                        Commands.argument("player", ArgumentTypes.player())
                                .executes((source) -> {
                                    Player player = source.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(source.getSource()).getFirst();

                                    if (!this.plugin.teamHandler.inTeam(player)) {
                                        source.getSource().getSender().sendMessage(Component.text("(!) Player is not in a team", NamedTextColor.RED));
                                        return 0;
                                    }

                                    Team team = this.plugin.teamHandler.team(player);

                                    ForcebattleExtension extension = team.getExtension(ForcebattleExtension.class).orElse(null);
                                    if (extension == null) {
                                        source.getSource().getSender().sendMessage(Component.text("(!) Players team is not in a forcebattle", NamedTextColor.RED));
                                        return 0;
                                    }

                                    if (extension.getCurrentTask() == null) {
                                        source.getSource().getSender().sendMessage(Component.text("(!) Players team has no current task", NamedTextColor.RED));
                                        return 0;
                                    }

                                    player.getInventory().addItem(extension.getCurrentTask().toItemStack());

                                    ForcebattleTask task = extension.getCurrentTask();
                                    task.setFinishedData(new ForcebattleTask.FinishedData(
                                            true,
                                            this.plugin.timerHandler.elapsed,
                                            player.getName()
                                    ));

                                    ForcebattleChallenge challenge = (ForcebattleChallenge) this.plugin.challengeHandler.getChallenge("challenge_forcebattle");
                                    challenge.nextTask(team, task);

                                    source.getSource().getSender().sendMessage(Component.text("Skipped task for " + player.getName(), NamedTextColor.GREEN));
                                    return 1;
                                })
                );
        commands.register(this.plugin.getPluginMeta(), builder.build(), "Skips a current task for a player", List.of());
    }

}
