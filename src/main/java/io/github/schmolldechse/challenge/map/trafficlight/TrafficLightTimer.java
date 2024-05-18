package io.github.schmolldechse.challenge.map.trafficlight;

import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TrafficLightTimer {

    private ScheduledExecutorService timerService;
    private final BossBar bossBar;

    public long remainingTime;

    public TrafficLightStatus status = TrafficLightStatus.DISABLED;
    public TrafficLightStatus lastStatus = TrafficLightStatus.DISABLED;
    private final TrafficLightComponents components = new TrafficLightComponents();

    public TrafficLightTimer(BossBar bossBar) {
        this.bossBar = bossBar;
    }

    public void resume() {
        if (this.timerService == null || this.timerService.isShutdown()) this.timerService = Executors.newSingleThreadScheduledExecutor();

        /**
         * Append last light
         */
        if (this.lastStatus != TrafficLightStatus.DISABLED) {
            this.status = this.lastStatus;
            this.lastStatus = null;
            this.appendLight();
        }

        this.timerService.scheduleAtFixedRate(() -> {
            this.remainingTime--;
            if (this.remainingTime <= 0) this.transition();
        }, 0, 1, TimeUnit.SECONDS);
    }

    public void pause() {
        if (this.timerService != null) this.timerService.shutdownNow();

        /**
         * Set traffic light to disabled
         */
        this.lastStatus = this.status;
        this.status = TrafficLightStatus.DISABLED;
        if (this.bossBar != null) this.bossBar.name(this.components.trafficLight);
    }

    public void transition() {
        switch (this.status) {
            case GREEN: // green -> yellow
                this.status = TrafficLightStatus.YELLOW;
                this.remainingTime = calculate(1, 4); // 1 - 4 sec
                break;
            case YELLOW: // yellow -> red
                this.status = TrafficLightStatus.RED;
                this.remainingTime = 10; // 10 sec
                break;
            case RED, DISABLED: // red -> green
                this.status = TrafficLightStatus.GREEN;
                this.remainingTime = calculate(150, 480); // 2:30 - 8 min
                break;
        }

        this.appendLight();
    }

    private void appendLight() {
        switch (this.status) {
            case RED:
                this.bossBar.name(this.components.trafficLight.append(this.components.spaceNegative251).append(this.components.redLight));
                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1f, 0.2f));
                break;
            case YELLOW:
                this.bossBar.name(this.components.trafficLight.append(this.components.spaceNegative251).append(this.components.yellowLight));
                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1f, 0.1f));
                break;
            case GREEN:
                this.bossBar.name(this.components.trafficLight.append(this.components.spaceNegative251).append(this.components.greenLight));
                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 0.5f));
                break;
        }
    }

    private long calculate(int min, int max) {
        return (long) (Math.random() * (max - min + 1) + min);
    }
}