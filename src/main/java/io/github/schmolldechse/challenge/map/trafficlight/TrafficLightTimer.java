package io.github.schmolldechse.challenge.map.trafficlight;

import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TrafficLightTimer {

    private ScheduledExecutorService timerService;
    private long remainingTime = -1, pauseStartTime;

    public TrafficLightStatus status;
    private final TrafficLightComponents components = new TrafficLightComponents();

    public BossBar bossBar;

    public TrafficLightTimer() {
        this.timerService = Executors.newSingleThreadScheduledExecutor();
    }

    public void schedule(Runnable command, long delay, TimeUnit unit, TrafficLightStatus newStatus) {
        this.timerService.schedule(command, delay, unit);

        this.status = newStatus;
        this.remainingTime = unit.toSeconds(delay);
        this.pauseStartTime = System.currentTimeMillis();
    }

    public void shutdown() {
        if (this.timerService == null) return;

        this.timerService.shutdownNow();
        this.timerService = null;

        this.status = TrafficLightStatus.DISABLED;
    }

    public void pause() {
        if (this.timerService != null && !this.timerService.isShutdown()) this.timerService.shutdownNow();

        long currentTime = System.currentTimeMillis();
        this.remainingTime -= (currentTime - this.pauseStartTime) / 1000;
    }

    public void resume() {
        if (this.remainingTime < 0) return;

        this.timerService = Executors.newSingleThreadScheduledExecutor();

        Runnable nextPhaseRunnable = getNextPhaseRunnable();
        if (nextPhaseRunnable != null) {
            this.timerService.schedule(nextPhaseRunnable, this.remainingTime, TimeUnit.SECONDS);
            this.pauseStartTime = System.currentTimeMillis();
        }
    }

    private Runnable getNextPhaseRunnable() {
        switch (this.status) {
            case RED:
                return this::changeToGreen;
            case YELLOW:
                return this::changeToRed;
            default: // GREEN or DISABLED
                return this::changeToYellow;
        }
    }

    public void changeToRed() {
        if (this.timerService == null) this.timerService = Executors.newSingleThreadScheduledExecutor();

        this.status = TrafficLightStatus.RED;
        this.bossBar.name(this.components.trafficLight.append(this.components.spaceNegative251).append(this.components.redLight));

        Bukkit.getOnlinePlayers().forEach(player -> player.playSound(this.components.red));

        this.schedule(this::changeToGreen, 10, TimeUnit.SECONDS, TrafficLightStatus.GREEN);
    }

    public void changeToYellow() {
        if (this.timerService == null) this.timerService = Executors.newSingleThreadScheduledExecutor();

        this.status = TrafficLightStatus.YELLOW;
        this.bossBar.name(this.components.trafficLight.append(this.components.spaceNegative251).append(this.components.yellowLight));

        Bukkit.getOnlinePlayers().forEach(player -> player.playSound(this.components.yellow));

        Random random = new Random();
        int delay = random.nextInt(2) + 3; // 3 - 5 sec

        this.schedule(this::changeToRed, delay, TimeUnit.SECONDS, TrafficLightStatus.RED);
    }

    public void changeToGreen() {
        if (this.timerService == null) this.timerService = Executors.newSingleThreadScheduledExecutor();

        this.status = TrafficLightStatus.GREEN;
        this.bossBar.name(this.components.trafficLight.append(this.components.spaceNegative251).append(this.components.greenLight));

        Bukkit.getOnlinePlayers().forEach(player -> player.playSound(this.components.green));

        Random random = new Random();
        int delay = random.nextInt(331) + 150; // random seconds between 150 (2:30min) - 480 (8min)

        this.schedule(this::changeToYellow, delay, TimeUnit.SECONDS, TrafficLightStatus.YELLOW);
    }

    public TrafficLightStatus getStatus() {
        return status;
    }
}