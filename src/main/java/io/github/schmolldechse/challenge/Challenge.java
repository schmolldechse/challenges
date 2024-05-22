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
import java.util.Map;

public abstract class Challenge implements Listener {

    public abstract ItemStack getItemStack();
    public abstract Component getDisplayName();
    public abstract List<Component> getDescription();

    /**
     * Saves challenge data which can be applied after a restart again
     * @return Map<String, Object>
     */
    public abstract Map<String, Object> save();

    /**
     * Appends saved challenge data
     */
    public void append(Map<String, Object> data) { }

    private final String identifierName;

    protected boolean active = false;

    protected final TimerHandler timerHandler;
    protected final ChallengeHandler challengeHandler;

    private final Plugin plugin;

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
        String timeFormatted = this.timerHandler.format(duration);

        Bukkit.broadcast(Component.text("Die Challenge wurde geschafft", NamedTextColor.GREEN));
        Bukkit.broadcast(Component.empty());
        Bukkit.broadcast(Component.text("Zeit: ", NamedTextColor.GREEN).append(Component.text(timeFormatted).decoration(TextDecoration.ITALIC, true)));
    }

    protected void fail() {
        if (this.timerHandler.isPaused()) return;

        this.timerHandler.pause();
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.getGameMode() != GameMode.SURVIVAL) return;

            player.setHealth(0.0D);
            player.setGameMode(GameMode.SPECTATOR);
        });

        Duration duration = Duration.ofSeconds(this.timerHandler.time);
        String timeFormatted = this.timerHandler.format(duration);

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

    public void onActivate() { }

    public void onDeactivate() { }

    public void onPause() { }

    public void onResume() { }

    public boolean isActive() { return active; }

    public String getIdentifierName() { return identifierName; }
}
