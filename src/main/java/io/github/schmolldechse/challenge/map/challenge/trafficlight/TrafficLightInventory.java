package io.github.schmolldechse.challenge.map.challenge.trafficlight;

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.misc.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.Arrays;

public class TrafficLightInventory {

    private final Plugin plugin;

    private final TrafficLightChallenge challenge;

    private final Gui gui;

    public TrafficLightInventory(TrafficLightChallenge challenge) {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.challenge = challenge;

        this.gui = Gui.gui()
                .title(Component.text("Einstellungen", NamedTextColor.BLUE).decoration(TextDecoration.BOLD, true))
                .rows(5)
                .disableAllInteractions()
                .create();

        this.gui.setItem(5, 1, ItemBuilder.from(Material.MANGROVE_DOOR)
                .name(Component.text("Zurück", NamedTextColor.RED).decoration(TextDecoration.ITALIC, true))
                .asGuiItem((event) -> {
                    this.plugin.challengeInventory.getInventory().open(event.getWhoClicked());
                }));

        this.buttons();

        this.gui.setOpenGuiAction(event -> {
            this.statusItems();
            this.gui.update();
        });
    }

    private void statusItems() {
        this.gui.setItem(2, 5, ItemBuilder.from(Material.RED_CONCRETE)
                .name(Component.text("Zeit des Rotlichts", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
                .lore(Arrays.asList(
                        Component.empty(),
                        Component.text("min.", NamedTextColor.GRAY)
                                .append(Component.text(" \uD83E\uDC12 ", NamedTextColor.DARK_GRAY))
                                .append(Component.text(this.plugin.timerHandler.format(Duration.ofSeconds(TrafficLightStatus.RED.minDuration)), NamedTextColor.AQUA)),
                        Component.text("max.", NamedTextColor.GRAY)
                                .append(Component.text(" \uD83E\uDC12 ", NamedTextColor.DARK_GRAY))
                                .append(Component.text(this.plugin.timerHandler.format(Duration.ofSeconds(TrafficLightStatus.RED.maxDuration)), NamedTextColor.AQUA)),
                        Component.empty())
                )
                .asGuiItem());

        this.gui.setItem(3, 5, ItemBuilder.from(Material.YELLOW_CONCRETE)
                .name(Component.text("Zeit des Gelblichts", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
                .lore(Arrays.asList(
                        Component.empty(),
                        Component.text("min.", NamedTextColor.GRAY)
                                .append(Component.text(" \uD83E\uDC12 ", NamedTextColor.DARK_GRAY))
                                .append(Component.text(this.plugin.timerHandler.format(Duration.ofSeconds(TrafficLightStatus.YELLOW.minDuration)), NamedTextColor.AQUA)),
                        Component.text("max.", NamedTextColor.GRAY)
                                .append(Component.text(" \uD83E\uDC12 ", NamedTextColor.DARK_GRAY))
                                .append(Component.text(this.plugin.timerHandler.format(Duration.ofSeconds(TrafficLightStatus.YELLOW.maxDuration)), NamedTextColor.AQUA)),
                        Component.empty())
                )
                .asGuiItem());

        this.gui.setItem(4, 5, ItemBuilder.from(Material.GREEN_CONCRETE)
                .name(Component.text("Zeit des Grünlichts", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false))
                .lore(Arrays.asList(
                        Component.empty(),
                        Component.text("min.", NamedTextColor.GRAY)
                                .append(Component.text(" \uD83E\uDC12 ", NamedTextColor.DARK_GRAY))
                                .append(Component.text(this.plugin.timerHandler.format(Duration.ofSeconds(TrafficLightStatus.GREEN.minDuration)), NamedTextColor.AQUA)),
                        Component.text("max.", NamedTextColor.GRAY)
                                .append(Component.text(" \uD83E\uDC12 ", NamedTextColor.DARK_GRAY))
                                .append(Component.text(this.plugin.timerHandler.format(Duration.ofSeconds(TrafficLightStatus.GREEN.maxDuration)), NamedTextColor.AQUA)),
                        Component.empty())
                )
                .asGuiItem());
    }

    private void buttons() {
        createButtonRow(2, TrafficLightStatus.RED);
        createButtonRow(3, TrafficLightStatus.YELLOW);
        createButtonRow(4, TrafficLightStatus.GREEN);
    }

    private void createButtonRow(int row, TrafficLightStatus status) {
        this.gui.setItem(row, 2, createButton(Material.DARK_OAK_BUTTON, "- 1m", -60, status));
        this.gui.setItem(row, 3, createButton(Material.DARK_OAK_BUTTON, "- 10s", -10, status));
        this.gui.setItem(row, 4, createButton(Material.DARK_OAK_BUTTON, "- 1s", -1, status));
        this.gui.setItem(row, 6, createButton(Material.STONE_BUTTON, "+ 1s", 1, status));
        this.gui.setItem(row, 7, createButton(Material.STONE_BUTTON, "+ 10s", 10, status));
        this.gui.setItem(row, 8, createButton(Material.STONE_BUTTON, "+ 1m", 60, status));
    }

    private GuiItem createButton(Material material, String name, int timeChange, TrafficLightStatus status) {
        return ItemBuilder.from(material)
                .name(Component.text(name, NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
                .lore(Arrays.asList(
                        Component.empty(),
                        Component.text("[Linksklick]", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, true)
                                .append(Component.text(" um die min. Zeit zu " + (name.startsWith("-") ? "verringern" : "vergrößern"), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)),
                        Component.text("[Rechtsklick]", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, true)
                                .append(Component.text(" um die max. Zeit zu " + (name.startsWith("-") ? "verringern" : "vergrößern"), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)),
                        Component.empty()
                ))
                .asGuiItem(event -> {
                    if (event.isLeftClick()) {
                        status.minDuration = Math.max(0, status.minDuration + timeChange);
                        status.maxDuration = Math.max(status.minDuration, status.maxDuration);
                    } else status.maxDuration = Math.max(status.minDuration, status.maxDuration + timeChange);

                    this.statusItems();
                    this.gui.update();
                });
    }

    public Gui getInventory() {
        return gui;
    }
}
