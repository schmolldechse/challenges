package io.github.schmolldechse.challenge.map.player.maxhearts;

import dev.triumphteam.gui.guis.Gui;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.misc.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class MaxHealthInventory {

    private final Plugin plugin;

    private final MaxHealthSetting setting;

    private final Gui gui;

    public MaxHealthInventory(MaxHealthSetting setting) {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.setting = setting;

        this.gui = Gui.gui()
                .title(Component.text("Einstellungen", NamedTextColor.BLUE).decoration(TextDecoration.BOLD, true))
                .rows(3)
                .disableAllInteractions()
                .create();

        this.gui.setItem(3, 1, ItemBuilder.from(Material.MANGROVE_DOOR)
                .name(Component.text("Zurück", NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
                .asGuiItem((event) -> {
                    this.plugin.playerInventory.getInventory().open(event.getWhoClicked());
                }));

        this.gui.setItem(2, 4, ItemBuilder.from(Material.DARK_OAK_BUTTON)
                .name(Component.text("- 0.5", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
                .lore(Arrays.asList(
                        Component.empty(),
                        Component.text("[Linksklick]", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, true)
                                .append(Component.text(" zum Verringern", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
                ))
                .asGuiItem(event -> {
                    if (this.setting.maxHealth <= 1) return;
                    this.setting.maxHealth -= 1.0;
                    this.setting.updateHearts(this.setting.maxHealth);

                    this.update();
                }));

        this.gui.setItem(2, 5, ItemBuilder.from(Material.GOLDEN_APPLE)
                .name(Component.text("Festgelegte Herzen", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
                .lore(lore -> {
                            lore.addAll(this.setting.heartDisplay());
                            lore.add(Component.text("[Klick] ", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, true)
                                    .append(Component.text("zum Zurücksetzen", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
                        }
                )
                .asGuiItem((event -> {
                    this.setting.maxHealth = 20.0;
                    this.setting.updateHearts(this.setting.maxHealth);

                    this.update();
                })));

        this.gui.setItem(2, 6, ItemBuilder.from(Material.DARK_OAK_BUTTON)
                .name(Component.text("+ 0.5", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false))
                .lore(Arrays.asList(
                        Component.empty(),
                        Component.text("[Linksklick]", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, true)
                                .append(Component.text(" zum Vergrößern", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
                ))
                .asGuiItem(event -> {
                    this.setting.maxHealth += 1.0;
                    this.setting.updateHearts(this.setting.maxHealth);

                    this.update();
                }));
    }

    private void update() {
        this.gui.updateItem(2, 5, ItemBuilder.from(Material.GOLDEN_APPLE)
                .name(Component.text("Festgelegte Herzen", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
                .lore(lore -> {
                            lore.addAll(this.setting.heartDisplay());
                            lore.add(Component.empty());
                            lore.add(Component.text("[Klick] ", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, true)
                                    .append(Component.text("zum Zurücksetzen", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)));
                        }
                )
                .asGuiItem((event -> {
                    this.setting.maxHealth = 20.0;
                    this.setting.updateHearts(this.setting.maxHealth);

                    this.update();
                })));
    }

    public Gui getInventory() {
        return gui;
    }
}
