package io.github.schmolldechse.commands;

import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import io.github.schmolldechse.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

public class TimerCommand {

    // /timer pause
    // /timer resume
    // /timer time <time>

    private final Plugin plugin;

    public TimerCommand() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
    }

    public void registerCommand() {
        new CommandTree("timer")
                .then(new LiteralArgument("pause")
                        .executes((sender, args) -> {
                            if (this.plugin.timerHandler.isPaused()) {
                                sender.sendMessage(Component.text("(!) Timer already paused", NamedTextColor.RED));
                                return;
                            }
                            this.plugin.timerHandler.pause();
                            this.plugin.MOVEMENT_ALLOWED = false;
                        }))
                .then(new LiteralArgument("resume")
                        .executes((sender, args) -> {
                            if (!this.plugin.timerHandler.isPaused()) {
                                sender.sendMessage(Component.text("(!) Timer already running", NamedTextColor.RED));
                                return;
                            }

                            this.plugin.timerHandler.start();
                            this.plugin.MOVEMENT_ALLOWED = true;
                        }))
                .then(new LiteralArgument("time")
                        .then(new GreedyStringArgument("time")
                                .executesPlayer((sender, args) -> {
                                    String time = (String) args.get("time");
                                    if (time == null) return;

                                    this.plugin.timerHandler.update((int) convert(time));
                                })))
                .register();
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