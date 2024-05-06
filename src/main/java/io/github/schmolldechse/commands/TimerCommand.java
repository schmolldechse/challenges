package io.github.schmolldechse.commands;

import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import io.github.schmolldechse.timer.TimerHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.time.Duration;

public class TimerCommand {

    // /timer pause
    // /timer resume
    // /timer time <time>

    private final TimerHandler timerHandler;

    public TimerCommand(TimerHandler timerHandler) {
        this.timerHandler = timerHandler;
    }

    public void registerCommand() {
        new CommandTree("timer")
                .withPermission(CommandPermission.OP)
                .then(new LiteralArgument("pause")
                        .executes((sender, args) -> {
                            if (this.timerHandler.isPaused()) {
                                sender.sendMessage(Component.text("(!) Timer already paused", NamedTextColor.RED));
                                return;
                            }
                            this.timerHandler.pause();
                        }))
                .then(new LiteralArgument("resume")
                        .executes((sender, args) -> {
                            if (!this.timerHandler.isPaused()) {
                                sender.sendMessage(Component.text("(!) Timer already running", NamedTextColor.RED));
                                return;
                            }
                            this.timerHandler.start();
                        }))
                .then(new LiteralArgument("time")
                        .then(new GreedyStringArgument("time")
                                .executesPlayer((sender, args) -> {
                                    String time = (String) args.get("time");

                                    this.timerHandler.update((int) convert(time));
                                    if (this.timerHandler.isPaused()) this.timerHandler.start();
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