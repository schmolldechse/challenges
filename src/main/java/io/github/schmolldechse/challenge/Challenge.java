package io.github.schmolldechse.challenge;

import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.challenge.module.Module;
import io.github.schmolldechse.challenge.module.ModuleRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public abstract class Challenge implements Listener {

    protected final Plugin plugin;
    protected final NamespacedKey key;

    protected final ModuleRegistry moduleRegistry;

    private final String identifierName;
    protected boolean active = false;

    /**
     * Creates a new challenge
     * @param identifierName unique identifier for the challenge
     */
    public Challenge(final String identifierName) {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.key = new NamespacedKey(this.plugin, "identifier");
        this.identifierName = identifierName;

        this.moduleRegistry = new ModuleRegistry();
    }

    public abstract Identification challengeIdentification();

    /**
     * ItemStack representation of the challenge
     * @return {@link ItemStack}
     */
    public abstract ItemStack getItemStack();

    /**
     * Display name of the challenge
     * @return display name as a {@link Component}
     */
    public abstract Component getDisplayName();

    /**
     * Description of the challenge
     * @return description as a list of {@link Component}
     */
    public abstract List<Component> getDescription();

    /**
     * Returns a component which represents the activation state of the challenge
     * @return {@link Component} representing the activation state
     */
    protected Component activationComponent() {
        return this.active
                ? Component.text("Aktiviert", NamedTextColor.GREEN)
                : Component.text("Deaktiviert", NamedTextColor.RED);
    }

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
        this.plugin.challengeHandler.registeredChallenges.values().stream()
                .filter(Challenge::isActive)
                .forEach(Challenge::onPause);

        Duration duration = Duration.ofSeconds(this.plugin.timerHandler.time);
        String timeFormatted = this.plugin.timerHandler.format(duration);

        Bukkit.broadcast(Component.text("Die Challenge wurde geschafft", NamedTextColor.GREEN));
        Bukkit.broadcast(Component.empty());
        Bukkit.broadcast(Component.text("Zeit: ", NamedTextColor.GREEN).append(Component.text(timeFormatted).decoration(TextDecoration.ITALIC, true)));
    }

    protected void fail() {
        if (this.plugin.timerHandler.isPaused()) return;

        this.plugin.timerHandler.pause();
        this.plugin.challengeHandler.registeredChallenges.values().stream()
                .filter(Challenge::isActive)
                .forEach(Challenge::onPause);

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
        // resume challenge if timerHandler already started
        if (!this.plugin.timerHandler.isPaused()) this.onResume();
    }

    public void onDeactivate() {
        this.moduleRegistry.getModules().stream()
                .filter(Module::isActive)
                .forEach(Module::deactivate);
    }

    public void onPause() { }

    public void onResume() { }

    public boolean isActive() { return this.active; }

    public String getIdentifierName() { return this.identifierName; }

    public ModuleRegistry getModuleRegistry() {
        return this.moduleRegistry;
    }
}
