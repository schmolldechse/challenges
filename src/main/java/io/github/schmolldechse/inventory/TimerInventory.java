package io.github.schmolldechse.inventory;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import io.github.schmolldechse.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

public class TimerInventory {

    private final Plugin plugin;
    private final Gui gui;

    public TimerInventory() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);

        this.gui = Gui.gui()
                .title(Component.text("Timer", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true))
                .rows(3)
                .disableAllInteractions()
                .create();

        this.gui.setItem(2, 2, ItemBuilder.from(Material.CLOCK)
                .name(Component.text("Zeit: ", NamedTextColor.GRAY)
                        .append(Component.text(this.plugin.timerHandler.format(Duration.ZERO)))
                        .decoration(TextDecoration.ITALIC, true))
                .asGuiItem((event) -> {

                }));

        this.gui.setItem(2, 5, ItemBuilder.from(Material.COMPARATOR)
                .name(Component.text("Einstellungen", NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
                .asGuiItem((event) -> {

                }));

        this.gui.setItem(2, 8, ItemBuilder.from(Material.CLOCK)
                .name(Component.text("Timer", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, true))
                .asGuiItem((event) -> {

                }));
    }

    public Gui getInventory() {
        return gui;
    }
}