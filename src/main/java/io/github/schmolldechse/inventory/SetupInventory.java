package io.github.schmolldechse.inventory;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import io.github.schmolldechse.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

public class SetupInventory {

    private final Plugin plugin;
    private final Gui gui;

    public SetupInventory() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);

        this.gui = Gui.gui()
                .title(Component.text("Setup", NamedTextColor.BLUE).decoration(TextDecoration.BOLD, true))
                .rows(6)
                .disableAllInteractions()
                .create();

        this.gui.setItem(2, 5, ItemBuilder.from(Material.COMPARATOR)
                .name(Component.text("Bedingungen", NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
                .asGuiItem((event -> {
                    this.plugin.conditionInventory.getInventory().open(event.getWhoClicked());
                })));

        this.gui.setItem(3, 2, ItemBuilder.from(Material.BOOK)
                .name(Component.text("Challenges", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, true))
                .asGuiItem((event) -> {
                    this.plugin.challengeInventory.getInventory().open(event.getWhoClicked());
                }));

        this.gui.setItem(3, 8, ItemBuilder.from(Material.PLAYER_HEAD)
                .name(Component.text("Spieler", NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
                .asGuiItem((event) -> {
                    this.plugin.playerInventory.getInventory().open(event.getWhoClicked());
                }));

        this.gui.setItem(5, 3, ItemBuilder.from(Material.GRASS_BLOCK)
                .name(Component.text("Welt", NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
                .asGuiItem((event) -> {
                    this.plugin.worldInventory.getInventory().open(event.getWhoClicked());
                }));

        //TODO: item
        this.gui.setItem(5, 7, ItemBuilder.from(Material.CLOCK)
                .name(Component.text("Timer", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
                .lore(Component.text("bald", NamedTextColor.RED))
                .asGuiItem((event) -> {

                }));
    }

    public Gui getInventory() {
        return gui;
    }
}