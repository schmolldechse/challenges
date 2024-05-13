package io.github.schmolldechse.challenge;

import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.timer.TimerHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.List;

public abstract class Challenge implements Listener {

    public abstract ItemStack getItemStack();
    public abstract Component getDisplayName();
    public abstract List<Component> getDescription();

    private final String identifierName;

    protected boolean active = false;

    protected final TimerHandler timerHandler;
    protected final ChallengeHandler challengeHandler;

    private Plugin plugin;

    public Challenge(
            String identifierName,
            TimerHandler timerHandler,
            ChallengeHandler challengeHandler
    ) {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);

        this.identifierName = identifierName;

        this.timerHandler = timerHandler;
        this.challengeHandler = challengeHandler;
    }

    protected void success() {
        if (this.timerHandler.isPaused()) return;

        this.timerHandler.pause();

        Duration duration = Duration.ofSeconds(this.timerHandler.time);
        String timeFormatted = this.timerHandler.formatWholeDuration(duration);

        Bukkit.broadcast(Component.text("Die Challenge wurde geschafft", NamedTextColor.GREEN));
        Bukkit.broadcast(Component.empty());
        Bukkit.broadcast(Component.text("Zeit: ", NamedTextColor.GREEN).append(Component.text(timeFormatted).decoration(TextDecoration.ITALIC, true)));
    }

    protected void fail() {
        if (this.timerHandler.isPaused()) return;

        this.timerHandler.pause();
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.setHealth(0.0D);
            player.setGameMode(GameMode.SPECTATOR);
        });

        Duration duration = Duration.ofSeconds(this.timerHandler.time);
        String timeFormatted = this.timerHandler.formatWholeDuration(duration);

        Bukkit.broadcast(Component.text("Die Challenge ist fehlgeschlagen", NamedTextColor.RED));
        Bukkit.broadcast(Component.empty());
        Bukkit.broadcast(Component.text("Zeit: ", NamedTextColor.RED).append(Component.text(timeFormatted).decoration(TextDecoration.ITALIC, true)));
    }

    public void toggle() {
        if (this.challengeHandler == null) throw new IllegalStateException("ChallengeHandler is not initialized");

        this.active = !this.active;

        if (this.active) this.onActivate();
        else this.onDeactivate();

        String displayNameSerialized = PlainTextComponentSerializer.plainText().serialize(this.getDisplayName());
        Component descriptionComponent = this.getDisplayName();

        for (Component line : this.getDescription()) {
            descriptionComponent = descriptionComponent.append(line).append(Component.newline());
        }

        Bukkit.broadcast(Component
                .text(displayNameSerialized + (this.active ? " aktiviert" : " deaktiviert"), (this.active ? NamedTextColor.GREEN : NamedTextColor.RED))
                .hoverEvent(HoverEvent.showText(descriptionComponent))
        );
    }

    public void onActivate() {
        this.plugin.getLogger().info(this.identifierName + " activated");
    }

    public void onDeactivate() {
        this.plugin.getLogger().info(this.identifierName + " deactivated");
    }

    public void onPause() {
        this.plugin.getLogger().info(this.identifierName + " paused due to timer being paused");
    }

    public void onResume() {
        this.plugin.getLogger().info(this.identifierName + " resumed due to timer being resumed");
    }

    public boolean isActive() {
        return active;
    }

    public String getIdentifierName() {
        return identifierName;
    }
}
