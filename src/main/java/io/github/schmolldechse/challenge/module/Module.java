package io.github.schmolldechse.challenge.module;

import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.challenge.Challenge;
import io.github.schmolldechse.config.document.Document;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public abstract class Module<T extends Challenge> {

    protected final Plugin plugin;
    protected final NamespacedKey key;
    protected T challenge;

    private final String identifierName;
    protected boolean active = false;

    public Module(T challenge, final String identifierName) {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.key = new NamespacedKey(this.plugin, "identifier");
        this.challenge = challenge;

        this.identifierName = identifierName;
        this.active = false;
    }

    /**
     * ItemStack representation of the module
     * @return {@link ItemStack}
     */
    public abstract ItemStack getItemStack();

    /**
     * Display name of the module
     * @return display name as a {@link Component}
     */
    public abstract Component getDisplayName();

    /**
     * Description of the module
     * @return description as a list of {@link Component}
     */
    public abstract List<Component> getDescription();

    /**
     * Saves module data which can be applied after a restart
     * @return Document of saved data
     */
    public Document save() { return new Document(); }

    /**
     * Appends saved data to the module
     * @param document {@link Document} containing the saved data
     */
    public void append(Document document) { }

    public void toggle() {
        if (this.plugin.challengeHandler == null) throw new IllegalStateException("ChallengeHandler is not initialized");

        this.active = !this.active;

        if (this.active) this.activate();
        else this.deactivate();
    }

    public void activate() { }

    public void deactivate() { }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getIdentifierName() {
        return this.identifierName;
    }
}
