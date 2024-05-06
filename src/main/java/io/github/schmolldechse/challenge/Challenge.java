package io.github.schmolldechse.challenge;

import io.github.schmolldechse.timer.TimerHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.Listener;

import java.util.List;

public abstract class Challenge implements Listener {

    private final Material material;
    private final Component displayName;
    private final String identifierName;
    private final List<Component> description;

    protected boolean active = false;

    protected final TimerHandler timerHandler;
    protected final ChallengeHandler challengeHandler;

    public Challenge(
            Material material,
            Component displayName,
            String identifierName,
            List<Component> description,
            TimerHandler timerHandler,
            ChallengeHandler challengeHandler
    ) {
        this.material = material;
        this.displayName = displayName;
        this.identifierName = identifierName;
        this.description = description;

        this.timerHandler = timerHandler;
        this.challengeHandler = challengeHandler;
    }

    protected void fail() {
        if (this.timerHandler.isPaused()) return;

        this.timerHandler.pause();
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.setHealth(0.0D);
            player.setGameMode(GameMode.SPECTATOR);
        });
    }

    public boolean isActive() {
        return active;
    }

    public Material getMaterial() {
        return material;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public String getIdentifierName() {
        return identifierName;
    }

    public List<Component> getDescription() {
        return description;
    }
}
