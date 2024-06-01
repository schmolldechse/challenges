package io.github.schmolldechse.challenge.map.challenge.randomizer;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import io.github.schmolldechse.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class RandomizerInventory {

    private final Plugin plugin;

    private final RandomizerChallenge challenge;

    private final Gui gui;

    public RandomizerInventory(RandomizerChallenge challenge) {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.challenge = challenge;

        this.gui = Gui.gui()
                .title(Component.text("Einstellungen", NamedTextColor.BLUE).decoration(TextDecoration.BOLD, true))
                .rows(3)
                .disableAllInteractions()
                .create();

        this.gui.setItem(3, 1, ItemBuilder.from(Material.MANGROVE_DOOR)
                .name(Component.text("Zurück", NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
                .asGuiItem((event) -> this.plugin.challengeInventory.getInventory().open(event.getWhoClicked())));

        this.statusItems();

        this.gui.setCloseGuiAction(event -> this.challenge.shuffle(false));
    }

    private void statusItems() {
        this.gui.setItem(2, 2, ItemBuilder.from(Material.GRASS_BLOCK)
                .name(Component.text("Blöcke Randomizer", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
                .lore(Arrays.asList(
                        Component.text("Beinhaltet " + this.challenge.blocksRandomizerMap.size() + " Blöcke", NamedTextColor.GRAY),
                        Component.empty(),
                                Component.text("[Klick]", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, true)
                                        .append(Component.text(" zum (De-) Aktivieren", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)),
                        Component.empty(),
                        this.challenge.BLOCKS_RANDOMIZED
                                ? Component.text("Aktiviert", NamedTextColor.GREEN)
                                : Component.text("Deaktiviert", NamedTextColor.RED))
                )
                .asGuiItem(event -> {
                    this.challenge.BLOCKS_RANDOMIZED = !this.challenge.BLOCKS_RANDOMIZED;

                    this.statusItems();
                    this.gui.update();
                }));

        this.gui.setItem(2, 5, ItemBuilder.from(Material.CREEPER_HEAD)
                .name(Component.text("Entities Randomizer", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
                .lore(Arrays.asList(
                        Component.text("Beinhaltet " + this.challenge.entitiesRandomizerMap.size() + " Entities", NamedTextColor.GRAY),
                        Component.empty(),
                        Component.text("[Klick]", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, true)
                                .append(Component.text(" zum (De-) Aktivieren", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)),
                        Component.empty(),
                        this.challenge.ENTITIES_RANDOMIZED
                                ? Component.text("Aktiviert", NamedTextColor.GREEN)
                                : Component.text("Deaktiviert", NamedTextColor.RED))
                )
                .asGuiItem(event -> {
                    this.challenge.ENTITIES_RANDOMIZED = !this.challenge.ENTITIES_RANDOMIZED;

                    this.statusItems();
                    this.gui.update();
                }));

        this.gui.setItem(2, 8, ItemBuilder.from(Material.CRAFTING_TABLE)
                .name(Component.text("Craftingrezepte Randomizer", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
                .lore(Arrays.asList(
                        Component.text("Beinhaltet " + this.challenge.craftingRandomizerMap.size() + " Crafting Rezepte", NamedTextColor.GRAY),
                        Component.empty(),
                        Component.text("[Klick]", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, true)
                                .append(Component.text(" zum (De-) Aktivieren", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)),
                        Component.empty(),
                        this.challenge.CRAFTING_RANDOMIZED
                                ? Component.text("Aktiviert", NamedTextColor.GREEN)
                                : Component.text("Deaktiviert", NamedTextColor.RED))
                )
                .asGuiItem(event -> {
                    this.challenge.CRAFTING_RANDOMIZED = !this.challenge.CRAFTING_RANDOMIZED;

                    this.statusItems();
                    this.gui.update();
                }));
    }

    public Gui getInventory() {
        return gui;
    }
}