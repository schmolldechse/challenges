package io.github.schmolldechse.challenge.map.challenge.forcebattle.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.challenge.map.challenge.forcebattle.ForcebattleTask;
import io.github.schmolldechse.challenge.map.challenge.forcebattle.team.ForcebattleExtension;
import io.github.schmolldechse.misc.item.ItemBuilder;
import io.github.schmolldechse.team.Team;
import io.github.schmolldechse.team.argument.TeamArgument;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class ResultCommand {

    // /result start
    // /result evaluate
    // /result team <team>

    private final Plugin plugin;

    private Queue<Team> sortedTeams;
    private int currentRank;

    public ResultCommand(Commands commands) {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);

        final LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("result")
                .then(
                        Commands.literal("start")
                                .executes((source) -> {
                                    if (!this.plugin.timerHandler.isPaused()) {
                                        source.getSource().getSender().sendMessage(Component.text("(!) Pause the timer first", NamedTextColor.RED));
                                        return 0;
                                    }

                                    this.sortedTeams = this.plugin.teamHandler.getRegisteredTeams().stream()
                                            .filter(team -> team.getExtension(ForcebattleExtension.class).isPresent())
                                            .collect(Collectors.toCollection(() -> new PriorityQueue<>(Comparator.comparingInt(team ->
                                                    team.getExtension(ForcebattleExtension.class)
                                                            .map(forcebattleExtension -> forcebattleExtension.getCompletedTasks().size())
                                                            .orElse(0)
                                            ))));
                                    this.currentRank = this.sortedTeams.size();

                                    this.evaluateNext(source.getSource());
                                    return 1;
                                })
                )
                .then(
                        Commands.literal("evaluate")
                                .requires(source -> source.getSender() instanceof Player player && player.hasPermission("challenge.forcebattle.command.result.evaluate"))
                                .executes((source) -> {
                                    this.evaluateNext(source.getSource());
                                    return 1;
                                })
                )
                .then(
                        Commands.literal("team")
                                .then(
                                        Commands.argument("team", new TeamArgument(this.plugin))
                                                .requires(source -> source.getSender() instanceof Player)
                                                .executes((source) -> {
                                                    Team team = source.getArgument("team", Team.class);
                                                    ForcebattleExtension extension = team.getExtension(ForcebattleExtension.class).orElse(null);
                                                    if (extension == null) {
                                                        source.getSource().getSender().sendMessage(Component.text("(!) This team is not registered for the forcebattle challenge", NamedTextColor.RED));
                                                        return 0;
                                                    }

                                                    extension.getPaginatedGui().open((Player) source.getSource().getSender());
                                                    return 1;
                                                })
                                )
                );
        commands.register(this.plugin.getPluginMeta(), builder.build(), "Results of the forcebattle challenge", List.of());
    }

    private void evaluateNext(CommandSourceStack source) {
        if (this.sortedTeams.isEmpty()) {
            source.getSender().sendMessage(Component.text("(!) No more teams to evaluate", NamedTextColor.RED));
            return;
        }

        Team team = this.sortedTeams.poll();
        ForcebattleExtension extension = team.getExtension(ForcebattleExtension.class).get();
        //if (extension == null) return;

        Bukkit.getOnlinePlayers().forEach(player -> extension.getPaginatedGui().open(player));

        new BukkitRunnable() {
            private final Queue<ForcebattleTask> tasksQueue;

            {
                this.tasksQueue = new LinkedList<>(extension.getCompletedTasks().stream()
                        .filter(task -> task.getFinishedData() != null)
                        .sorted(Comparator.comparingInt(task -> task.getFinishedData().getFinishedAfter()))
                        .toList());
            }

            @Override
            public void run() {
                if (tasksQueue.isEmpty()) {
                    if (extension.getPaginatedGui().getPageItems().size() % 35 > 0) {
                        extension.getPaginatedGui().setItem(4, 1, ItemBuilder.from(Material.RED_STAINED_GLASS_PANE)
                                .name(Component.text("Vorherige Seite", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
                                .asGuiItem((event -> extension.getPaginatedGui().previous())));

                        extension.getPaginatedGui().setItem(4, 9, ItemBuilder.from(Material.GREEN_STAINED_GLASS_PANE)
                                .name(Component.text("Nächste Seite", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false))
                                .asGuiItem((event -> extension.getPaginatedGui().next())));

                        extension.getPaginatedGui().update();
                    }

                    final Component title = Component.text(currentRank, rank(currentRank)).decoration(TextDecoration.ITALIC, false)
                            .append(Component.text(". ", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
                            .append(team.members()).decoration(TextDecoration.ITALIC, false);
                    final Component subtitle = Component.text(extension.getCompletedTasks().size() + " Aufgaben geschafft", NamedTextColor.GOLD);

                    Bukkit.broadcast(title
                            .append(Component.text(" - ", NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false))
                            .append(Component.text(extension.getCompletedTasks().size() + " ", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
                            .append(Component.text("[Übersicht]", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false).clickEvent(ClickEvent.runCommand("/result team " + team.getName())))
                    );

                    if (!sortedTeams.isEmpty())
                        source.getSender().sendMessage(Component.text("[Nächstes Team]", NamedTextColor.GREEN)
                                .decoration(TextDecoration.ITALIC, false)
                                .clickEvent(ClickEvent.runCommand("/result evaluate"))
                        );

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            extension.getPaginatedGui().close(player);

                            player.showTitle(Title.title(title, subtitle, Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))));
                        });
                    }, 30L);

                    currentRank--;

                    this.cancel();
                    return;
                }

                ForcebattleTask task = tasksQueue.poll();

                extension.getPaginatedGui().addItem(ItemBuilder.from(task.toItemStack())
                        .lore(lore -> {
                            lore.add(Component.empty());
                            lore.add(Component.text(plugin.timerHandler.format(Duration.ofSeconds(task.getFinishedData().getFinishedAfter())), NamedTextColor.GOLD));
                            if (task.getFinishedData().isSkipped())
                                lore.add(Component.text("[Skipped]", NamedTextColor.RED));
                        })
                        .asGuiItem()
                );

                extension.getPaginatedGui().update();
                extension.getPaginatedGui().next();

                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 0.1f));
            }
        }.runTaskTimer(this.plugin, 0, 10L);
    }

    private NamedTextColor rank(int rank) {
        return switch (rank) {
            case 1 -> NamedTextColor.GOLD;
            case 2 -> NamedTextColor.GRAY;
            case 3 -> NamedTextColor.DARK_GRAY;
            default -> NamedTextColor.WHITE;
        };
    }
}
