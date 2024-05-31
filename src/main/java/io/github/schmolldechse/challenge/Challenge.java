package io.github.schmolldechse.challenge;

import io.github.schmolldechse.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public abstract class Challenge implements Listener {

    protected final Plugin plugin;

    protected boolean active = false;
    private final String identifierName;

    /**
     * Creates a new challenge
     * @param identifierName unique identifier for the challenge
     */
    public Challenge(final String identifierName) {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.identifierName = identifierName;
    }

    /**
     * ItemStack representation of the setting
     * @return ItemStack
     */
    public abstract ItemStack getItemStack();

    /**
     * Display name of the setting
     * @return display name as a Component
     */
    public abstract Component getDisplayName();

    /**
     * Description of the setting
     * @return description as a list of Components
     */
    public abstract List<Component> getDescription();

    /**
     * Saves challenge data which can be applied after a restart
     * @return map of saved data
     */
    public Map<String, Object> save() { return Map.of(); }

    /**
     * Appends saved data to the challenge
     * @param data data to append
     */
    public void append(Map<String, Object> data) { }

    /**
     * Opens the inventory for the player to change the settings of a challenge
     * @param player Player to open the inventory for
     */
    public void openSettings(Player player) { }

    protected void success() {
        if (this.plugin.timerHandler.isPaused()) return;

        this.plugin.timerHandler.pause();
        this.onPause();

        Duration duration = Duration.ofSeconds(this.plugin.timerHandler.time);
        String timeFormatted = this.plugin.timerHandler.format(duration);

        Bukkit.broadcast(Component.text("Die Challenge wurde geschafft", NamedTextColor.GREEN));
        Bukkit.broadcast(Component.empty());
        Bukkit.broadcast(Component.text("Zeit: ", NamedTextColor.GREEN).append(Component.text(timeFormatted).decoration(TextDecoration.ITALIC, true)));
    }

    protected void fail() {
        if (this.plugin.timerHandler.isPaused()) return;

        this.plugin.timerHandler.pause();
        this.onPause();

        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.getGameMode() != GameMode.SURVIVAL) return;

            player.setHealth(0.0D);
            player.setGameMode(GameMode.SPECTATOR);
        });

        Duration duration = Duration.ofSeconds(this.plugin.timerHandler.time);
        String timeFormatted = this.plugin.timerHandler.format(duration);

        Bukkit.broadcast(Component.text("Die Challenge ist fehlgeschlagen", NamedTextColor.RED));
        Bukkit.broadcast(Component.empty());
        Bukkit.broadcast(Component.text("Zeit: ", NamedTextColor.RED).append(Component.text(timeFormatted).decoration(TextDecoration.ITALIC, true)));
    }

    public void toggle() {
        if (this.plugin.challengeHandler == null) throw new IllegalStateException("ChallengeHandler is not initialized");

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

    public void setActive(boolean active) {
        this.active = active;
    }

    public void onActivate() {

    }

    public void onDeactivate() {

    }

    public void onPause() { }

    public void onResume() { }

    public boolean isActive() { return active; }

    public String getIdentifierName() { return identifierName; }
}
