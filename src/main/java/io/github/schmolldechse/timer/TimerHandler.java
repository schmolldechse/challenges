package io.github.schmolldechse.timer;

import io.github.schmolldechse.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimerHandler {

    public int time;
    private boolean reverse;
    private boolean isStarted = false;

    private ScheduledExecutorService timerService;
    private final ScheduledExecutorService actionbarService;

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private double offset = 0.0D;

    private final Plugin plugin;

    public TimerHandler() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);

        this.actionbarService = Executors.newSingleThreadScheduledExecutor();
        this.actionbarService.scheduleAtFixedRate(() -> {
            offset += 0.05D;
            if (offset > 1.0) offset -= 2.0D;

            Duration duration = Duration.ofSeconds(time);
            String timeFormatted = formatDuration(duration);

            @NotNull Component display = miniMessage.deserialize("<gradient:#707CF4:#F658CF:" + offset + "><b>" + (isPaused() ? "Timer pausiert" : timeFormatted));
            Bukkit.getOnlinePlayers().forEach(player -> player.sendActionBar(display));
        }, 0, 50, TimeUnit.MILLISECONDS); // 2s animation
    }

    public void start() {
        if (this.isStarted) return;

        this.isStarted = true;

        this.plugin.challengeHandler.registeredChallenges.entrySet().stream()
                .filter(entry -> entry.getValue().isActive())
                .forEach(entry -> entry.getValue().onResume());

        this.timerService = Executors.newSingleThreadScheduledExecutor();
        this.timerService.scheduleAtFixedRate(() -> {
            if (this.time <= 0 && this.reverse) this.pause();
            else this.time += this.reverse ? -1 : +1;
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void pause() {
        this.timerService.shutdownNow();
        this.isStarted = false;

        this.plugin.challengeHandler.registeredChallenges.entrySet().stream()
                .filter(entry -> entry.getValue().isActive())
                .forEach(entry -> entry.getValue().onPause());
    }

    public boolean isPaused() {
        return !this.isStarted || this.timerService.isShutdown();
    }

    public void update(int seconds) {
        this.time = seconds;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public void shutdown() {
        if (this.timerService != null && !this.timerService.isShutdown()) this.timerService.shutdown();
        if (this.actionbarService != null && !this.actionbarService.isShutdown()) this.actionbarService.shutdown();
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDaysPart();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        if (days > 0) {
            return String.format("%dd, und %02d:%02d:%02d", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%02d:%02d", minutes, seconds);
        } else {
            return String.format("%02d", seconds);
        }
    }

    public String formatWholeDuration(Duration duration) {
        long days = duration.toDaysPart();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        if (days > 0) {
            return String.format("%dd Tage, %02d Stunden, %02d Minuten und %02d Sekunden", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("%02d Stunden, %02d Minuten und %02d Sekunden", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%02d Minuten und %02d Sekunden", minutes, seconds);
        } else {
            return String.format("%02d Sekunden", seconds);
        }
    }
}