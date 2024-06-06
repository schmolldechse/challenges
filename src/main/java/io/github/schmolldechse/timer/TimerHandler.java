package io.github.schmolldechse.timer;

import com.google.inject.Inject;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.challenge.Challenge;
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
    public boolean reverse;
    public boolean isStarted = false;

    private ScheduledExecutorService timerService;
    private final ScheduledExecutorService actionbarService;

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private double offset = 0.0D;

    private final Plugin plugin;

    @Inject
    public TimerHandler() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);

        this.actionbarService = Executors.newSingleThreadScheduledExecutor();
        this.actionbarService.scheduleAtFixedRate(() -> {
            offset += 0.05D;
            if (offset > 1.0) offset -= 2.0D;

            Duration duration = Duration.ofSeconds(time);
            String timeFormatted = this.format(duration);

            @NotNull Component display = miniMessage.deserialize("<gradient:#707CF4:#F658CF:" + offset + "><b>" + (isPaused() ? "Timer pausiert" : timeFormatted));
            Bukkit.getOnlinePlayers().forEach(player -> player.sendActionBar(display));
        }, 0, 50, TimeUnit.MILLISECONDS); // 2s animation
    }

    public void start() {
        if (this.isStarted) return;

        this.isStarted = true;

        this.plugin.challengeHandler.registeredChallenges.values().stream()
                .filter(Challenge::isActive)
                .forEach(Challenge::onResume);

        this.timerService = Executors.newSingleThreadScheduledExecutor();
        this.timerService.scheduleAtFixedRate(() -> {
            if (this.time <= 0 && this.reverse) this.pause();
            else this.time += this.reverse ? -1 : +1;
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void pause() {
        this.timerService.shutdownNow();
        this.isStarted = false;

        this.plugin.challengeHandler.registeredChallenges.values().stream()
                .filter(Challenge::isActive)
                .forEach(Challenge::onPause);
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

    public String format(Duration duration) {
        long total = duration.toSeconds();

        if (total < 60) {
            return String.format("%02ds", total);
        } else if (total / 60 < 60) {
            long minutes = total / 60;
            long seconds = total % 60;
            return String.format("%02dm %02ds", minutes, seconds);
        } else if (total / 3600 < 24) {
            long hours = total / 3600;
            long minutes = (total % 3600) / 60;
            long seconds = total % 60;
            return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
        } else {
            long days = total / 86400;
            long hours = (total % 86400) / 3600;
            long minutes = (total % 3600) / 60;
            long seconds = total % 60;
            return String.format("%02dd %02dh %02dm %02ds", days, hours, minutes, seconds);
        }
    }
}