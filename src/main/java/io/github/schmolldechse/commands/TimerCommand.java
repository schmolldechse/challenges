package io.github.schmolldechse.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.schmolldechse.Plugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.List;

public class TimerCommand {

    // /timer pause
    // /timer resume
    // /timer time <time>

    private final Plugin plugin;

    public TimerCommand(Commands commmands) {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.register(commmands);
    }

    private void register(Commands commands) {
        final LiteralArgumentBuilder<CommandSourceStack> timerBuilder = Commands.literal("timer")
                .then(
                        Commands.literal("pause")
                                .executes((source) -> {
                                    if (this.plugin.timerHandler.isPaused()) {
                                        source.getSource().getExecutor().sendMessage(Component.text("(!) Timer already paused", NamedTextColor.RED));
                                        return 0;
                                    }
                                    this.plugin.timerHandler.pause();
                                    this.plugin.MOVEMENT_ALLOWED = false;
                                    return 1;
                                })
                )
                .then(
                        Commands.literal("resume")
                                .executes((source) -> {
                                    if (!this.plugin.timerHandler.isPaused()) {
                                        source.getSource().getExecutor().sendMessage(Component.text("(!) Timer already running", NamedTextColor.RED));
                                        return 0;
                                    }
                                    this.plugin.timerHandler.start();
                                    this.plugin.MOVEMENT_ALLOWED = true;
                                    return 1;
                                })
                )
                .then(
                        Commands.literal("time")
                                .then(
                                        Commands.argument("time", StringArgumentType.greedyString())
                                                .executes((source) -> {
                                                            String time = StringArgumentType.getString(source, "time");
                                                            if (time == null) return 0;

                                                            int converted = (int) convert(time);
                                                            this.plugin.timerHandler.update(converted);
                                                            Component component = Component.text(this.plugin.timerHandler.format(Duration.ofSeconds(converted)));

                                                            source.getSource().getExecutor().sendMessage(Component.text("Timer set to ", NamedTextColor.GREEN).append(component));
                                                            return 1;
                                                        }
                                                )
                                )
                );
        commands.register(this.plugin.getPluginMeta(), timerBuilder.build(), "Modifies the timer", List.of());
    }

    private long convert(String time) {
        String[] units = time.split(" ");
        Duration duration = Duration.ZERO;

        for (String unit : units) {
            String type = unit.substring(unit.length() - 1);
            String valueString = unit.substring(0, unit.length() - 1);

            if (!valueString.matches("\\d+")) continue;

            long value = Long.parseLong(valueString);
            duration = switch (type) {
                case "w", "week" -> duration.plusDays(value * 7);
                case "d", "days" -> duration.plusDays(value);
                case "h", "hours" -> duration.plusHours(value);
                case "m", "min", "minutes" -> duration.plusMinutes(value);
                case "s", "sec" -> duration.plusSeconds(value);
                default -> duration.plusSeconds(0);
            };
        }

        return duration.getSeconds();
    }
}