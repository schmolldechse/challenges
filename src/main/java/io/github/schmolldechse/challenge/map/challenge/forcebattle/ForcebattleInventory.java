package io.github.schmolldechse.challenge.map.challenge.forcebattle;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.challenge.Challenge;
import io.github.schmolldechse.challenge.module.Module;
import io.github.schmolldechse.misc.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ForcebattleInventory {

    private final Plugin plugin;
    private final ForcebattleChallenge challenge;

    private final PaginatedGui gui;

    private final NamespacedKey key;

    public ForcebattleInventory(ForcebattleChallenge challenge) {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.challenge = challenge;
        this.key = new NamespacedKey(this.plugin, "identifier");

        this.gui = Gui.paginated()
                .title(Component.text("Einstellungen", NamedTextColor.BLUE).decoration(TextDecoration.BOLD, true))
                .rows(3)
                .disableAllInteractions()
                .create();

        this.gui.setItem(3, 1, ItemBuilder.from(Material.MANGROVE_DOOR)
                .name(Component.text("Zurück", NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
                .asGuiItem((event) -> this.plugin.challengeInventory.getInventory().open(event.getWhoClicked())));

        this.gui.setDefaultClickAction(event -> {
            if (event.getCurrentItem() == null) return;
            if (!event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(this.key)) return;

            String identifier = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(this.key, PersistentDataType.STRING);
            Module<? extends Challenge> module = this.challenge.getModuleRegistry().module(identifier);
            if (module == null) return;

            module.toggle();
            this.updateGuiItems();
        });

        this.gui.setOpenGuiAction(event -> this.updateGuiItems());
    }

    public void updateGuiItems() {
        this.gui.clearPageItems();

        this.challenge.getModuleRegistry().getModules()
                .forEach(module -> {
                    this.gui.addItem(ItemBuilder.from(module.getItemStack())
                            .glow(module.isActive())
                            .asGuiItem()
                    );
                });

        this.gui.update();
    }

    public PaginatedGui getInventory() {
        return gui;
    }
}