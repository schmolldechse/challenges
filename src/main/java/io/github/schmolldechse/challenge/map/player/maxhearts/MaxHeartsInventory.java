package io.github.schmolldechse.challenge.map.player.maxhearts;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import io.github.schmolldechse.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class MaxHeartsInventory {

    private final Plugin plugin;

    private final MaxHeartsSetting setting;

    private final Gui gui;

    public MaxHeartsInventory(MaxHeartsSetting setting) {
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
                                .append(Component.text(" zum Verringern", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)),
                        Component.empty()
                ))
                .asGuiItem(event -> {
                    if (this.setting.maxHearts <= 0.5) return;
                    this.setting.maxHearts -= 0.5;
                    this.setting.updateHearts(this.setting.maxHearts);

                    this.statusItems();
                }));

        this.gui.setItem(2, 6, ItemBuilder.from(Material.DARK_OAK_BUTTON)
                .name(Component.text("+ 0.5", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false))
                .lore(Arrays.asList(
                        Component.empty(),
                        Component.text("[Linksklick]", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, true)
                                .append(Component.text(" zum Vergrößern", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)),
                        Component.empty()
                ))
                .asGuiItem(event -> {
                    this.setting.maxHearts += 0.5;
                    this.setting.updateHearts(this.setting.maxHearts);

                    this.statusItems();
                    this.gui.update();
                }));

        this.gui.setOpenGuiAction(event -> this.statusItems());
    }

    private void statusItems() {
        this.gui.setItem(2, 5, ItemBuilder.from(Material.GOLDEN_APPLE)
                .name(Component.text("Festgelegte Herzen", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
                .lore(this.setting.heartDisplay())
                .asGuiItem());

        this.gui.update();
    }

    public Gui getInventory() {
        return gui;
    }
}
