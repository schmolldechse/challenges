package io.github.schmolldechse.inventory;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.challenge.Challenge;
import io.github.schmolldechse.challenge.Identification;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldInventory {

    private final Plugin plugin;
    private final PaginatedGui gui;

    private final NamespacedKey key;

    public WorldInventory() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.key = new NamespacedKey(this.plugin, "identifier");

        this.gui = Gui.paginated()
                .title(Component.text("Welt Einstellungen", NamedTextColor.RED).decoration(TextDecoration.BOLD, true))
                .rows(6)
                .pageSize(45)
                .disableAllInteractions()
                .create();

        this.gui.setDefaultClickAction(event -> {
            if (event.getCurrentItem() == null) return;
            if (!event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(this.key)) return;

            String identifier = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(this.key, PersistentDataType.STRING);
            Challenge challenge = this.plugin.challengeHandler.getChallenge(identifier);
            if (challenge == null) return;

            if (event.isRightClick()) {
                challenge.openSettings((Player) event.getWhoClicked());
            } else if (event.isLeftClick()) {
                this.plugin.challengeHandler.toggle(identifier);
                this.updateGuiItems();
            }
        });

        this.gui.setItem(6, 1, ItemBuilder.from(Material.MANGROVE_DOOR)
                .name(Component.text("Zurück", NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
                .asGuiItem((event) -> {
                    this.plugin.setupInventory.getInventory().open(event.getWhoClicked());
                }));

        this.gui.setItem(6, 3, ItemBuilder.from(Material.PAPER)
                .name(Component.text("vorherige Seite", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
                .asGuiItem((event) -> {
                    this.gui.previous();
                }));

        this.gui.setItem(6, 7, ItemBuilder.from(Material.PAPER)
                .name(Component.text("nächste Seite", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
                .asGuiItem((event) -> {
                    this.gui.next();
                }));

        this.gui.setOpenGuiAction(event -> {
            this.updateGuiItems();
        });
    }

    public void updateGuiItems() {
        this.gui.clearPageItems();

        this.plugin.challengeHandler.registeredChallenges.entrySet().stream()
                .filter(entry -> entry.getValue().challengeIdentification() == Identification.WORLD)
                .forEach(entry -> {
                    Challenge challenge = entry.getValue();

                    ItemBuilder itemBuilder = ItemBuilder.from(challenge.getItemStack());
                    if (challenge.isActive()) itemBuilder.glow(challenge.isActive());

                    this.gui.addItem(itemBuilder.asGuiItem());
                });

        this.gui.update();
    }

    public PaginatedGui getInventory() {
        return gui;
    }
}
