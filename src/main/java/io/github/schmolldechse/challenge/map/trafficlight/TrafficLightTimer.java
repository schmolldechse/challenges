package io.github.schmolldechse.challenge.map.trafficlight;

import org.bukkit.Bukkit;
import org.bukkit.Sound;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TrafficLightTimer {

    private final TrafficLightChallenge challenge;

    private ScheduledExecutorService timerService;

    public long remainingTime;

    public TrafficLightTimer(TrafficLightChallenge challenge) {
        this.challenge = challenge;
    }

    public void resume() {
        if (this.timerService == null || this.timerService.isShutdown()) this.timerService = Executors.newSingleThreadScheduledExecutor();

        /**
         * Append last light
         */
        if (this.challenge.lastStatus != TrafficLightStatus.DISABLED) {
            this.challenge.status = this.challenge.lastStatus;
            this.challenge.lastStatus = null;
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
        this.challenge.lastStatus = this.challenge.status;
        this.challenge.status = TrafficLightStatus.DISABLED;
        if (this.challenge.bossBar != null) this.challenge.bossBar.name(this.challenge.components.trafficLight);
    }

    public void transition() {
        switch (this.challenge.status) {
            case GREEN: // green -> yellow
                this.challenge.status = TrafficLightStatus.YELLOW;
                this.remainingTime = calculate(1, 4); // 1 - 4 sec until red light
                break;
            case YELLOW: // yellow -> red
                this.challenge.status = TrafficLightStatus.RED;
                this.remainingTime = 10; // 10 sec until green light
                break;
            case RED, DISABLED: // red -> green
                this.challenge.status = TrafficLightStatus.GREEN;
                this.remainingTime = calculate(150, 480); // 2:30 - 8 min until yellow light
                break;
        }

        this.appendLight();
    }

    private void appendLight() {
        switch (this.challenge.status) {
            case RED:
                this.challenge.bossBar.name(
                        this.challenge.components.trafficLight.append(this.challenge.components.spaceNegative251).append(this.challenge.components.redLight)
                );
                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1f, 0.2f));
                break;
            case YELLOW:
                this.challenge.bossBar.name(
                        this.challenge.components.trafficLight.append(this.challenge.components.spaceNegative251).append(this.challenge.components.yellowLight)
                );
                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1f, 0.1f));
                break;
            case GREEN:
                this.challenge.bossBar.name(
                        this.challenge.components.trafficLight.append(this.challenge.components.spaceNegative251).append(this.challenge.components.greenLight)
                );
                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 0.5f));
                break;
        }
    }

    private long calculate(int min, int max) {
        return (long) (Math.random() * (max - min + 1) + min);
    }
}