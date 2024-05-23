package io.github.schmolldechse.challenge.map.trafficlight.inventory;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.challenge.map.trafficlight.TrafficLightChallenge;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.Arrays;

public class SettingsInventory {

    private final Plugin plugin;

    private final TrafficLightChallenge challenge;

    private final Gui gui;

    public SettingsInventory(TrafficLightChallenge challenge) {
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

        this.gui.setOpenGuiAction(event -> {
            this.updateGuiItems();
        });

        this.gui.setDefaultClickAction(event -> {
            this.updateGuiItems();
        });
    }

    //TODO: fix buttons
    private void setRow(int row, Material material, String name, int minDuration, int maxDuration) {
        // left
        this.gui.setItem(row, 2, ItemBuilder.from(Material.DARK_OAK_BUTTON)
                .name(Component.text("-1 m", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true))
                .asGuiItem((event) -> {
                    switch (material) {
                        case RED_CONCRETE:
                            this.challenge.MIN_RED_DURATION = Math.min(1, this.challenge.MIN_RED_DURATION - 60);
                            break;
                        case YELLOW_CONCRETE:
                            this.challenge.MIN_YELLOW_DURATION = Math.min(1, this.challenge.MIN_YELLOW_DURATION - 60);
                            break;
                        case GREEN_CONCRETE:
                            this.challenge.MIN_GREEN_DURATION = Math.min(1, this.challenge.MIN_GREEN_DURATION - 60);
                            break;
                    }
                }));
        this.gui.setItem(row, 3, ItemBuilder.from(Material.DARK_OAK_BUTTON)
                .name(Component.text("-10 s", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true))
                .asGuiItem((event) -> {
                    switch (material) {
                        case RED_CONCRETE:
                            this.challenge.MIN_RED_DURATION = Math.min(1, this.challenge.MIN_RED_DURATION - 10);
                            break;
                        case YELLOW_CONCRETE:
                            this.challenge.MIN_YELLOW_DURATION = Math.min(1, this.challenge.MIN_YELLOW_DURATION - 10);
                            break;
                        case GREEN_CONCRETE:
                            this.challenge.MIN_GREEN_DURATION = Math.min(1, this.challenge.MIN_GREEN_DURATION - 10);
                            break;
                    }
                }));
        this.gui.setItem(row, 4, ItemBuilder.from(Material.DARK_OAK_BUTTON)
                .name(Component.text("-1 s", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true))
                .asGuiItem((event) -> {
                    switch (material) {
                        case RED_CONCRETE:
                            this.challenge.MIN_RED_DURATION = Math.min(1, this.challenge.MIN_RED_DURATION - 1);
                            break;
                        case YELLOW_CONCRETE:
                            this.challenge.MIN_YELLOW_DURATION = Math.min(1, this.challenge.MIN_YELLOW_DURATION - 1);
                            break;
                        case GREEN_CONCRETE:
                            this.challenge.MIN_GREEN_DURATION = Math.min(1, this.challenge.MIN_GREEN_DURATION - 1);
                            break;
                    }
                }));

        // center
        this.gui.setItem(row, 5, ItemBuilder.from(material)
                .name(Component.text(name, NamedTextColor.WHITE))
                .lore(Arrays.asList(
                        Component.empty(),
                        Component.text("min.", NamedTextColor.GRAY)
                                .append(Component.text(" \uD83E\uDC12 ", NamedTextColor.DARK_GRAY))
                                .append(Component.text(this.plugin.timerHandler.format(Duration.ofSeconds(minDuration)), NamedTextColor.AQUA)),
                        Component.text("max.", NamedTextColor.GRAY)
                                .append(Component.text(" \uD83E\uDC12 ", NamedTextColor.DARK_GRAY))
                                .append(Component.text(this.plugin.timerHandler.format(Duration.ofSeconds(maxDuration)), NamedTextColor.AQUA)),
                        Component.empty())
                )
                .asGuiItem());

        // right
        this.gui.setItem(row, 6, ItemBuilder.from(Material.STONE_BUTTON)
                .name(Component.text("+1 s", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true))
                .asGuiItem((event) -> {
                    switch (material) {
                        case RED_CONCRETE:
                            this.challenge.MIN_RED_DURATION = Math.min(1, this.challenge.MIN_RED_DURATION + 1);
                            break;
                        case YELLOW_CONCRETE:
                            this.challenge.MIN_YELLOW_DURATION = Math.min(1, this.challenge.MIN_YELLOW_DURATION + 1);
                            break;
                        case GREEN_CONCRETE:
                            this.challenge.MIN_GREEN_DURATION = Math.min(1, this.challenge.MIN_GREEN_DURATION + 1);
                            break;
                    }
                }));
        this.gui.setItem(row, 7, ItemBuilder.from(Material.STONE_BUTTON)
                .name(Component.text("+10 s", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true))
                .asGuiItem((event) -> {
                    switch (material) {
                        case RED_CONCRETE:
                            this.challenge.MIN_RED_DURATION = Math.min(1, this.challenge.MIN_RED_DURATION + 10);
                            break;
                        case YELLOW_CONCRETE:
                            this.challenge.MIN_YELLOW_DURATION = Math.min(1, this.challenge.MIN_YELLOW_DURATION + 10);
                            break;
                        case GREEN_CONCRETE:
                            this.challenge.MIN_GREEN_DURATION = Math.min(1, this.challenge.MIN_GREEN_DURATION + 10);
                            break;
                    }
                }));
        this.gui.setItem(row, 8, ItemBuilder.from(Material.STONE_BUTTON)
                .name(Component.text("+1 m", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true))
                .asGuiItem((event) -> {
                    switch (material) {
                        case RED_CONCRETE:
                            this.challenge.MIN_RED_DURATION = Math.min(1, this.challenge.MIN_RED_DURATION + 60);
                            break;
                        case YELLOW_CONCRETE:
                            this.challenge.MIN_YELLOW_DURATION = Math.min(1, this.challenge.MIN_YELLOW_DURATION + 60);
                            break;
                        case GREEN_CONCRETE:
                            this.challenge.MIN_GREEN_DURATION = Math.min(1, this.challenge.MIN_GREEN_DURATION + 60);
                            break;
                    }
                }));
    }

    public void updateGuiItems() {
        this.setRow(2, Material.RED_CONCRETE, "Zeit bis zum Rotlicht", this.challenge.MIN_RED_DURATION, this.challenge.MAX_RED_DURATION);
        this.setRow(3, Material.YELLOW_CONCRETE, "Zeit bis zum Gelblicht", this.challenge.MIN_YELLOW_DURATION, this.challenge.MAX_YELLOW_DURATION);
        this.setRow(4, Material.GREEN_CONCRETE, "Zeit bis zum Grünlicht", this.challenge.MIN_GREEN_DURATION, this.challenge.MAX_GREEN_DURATION);

        this.gui.update();
    }

    public Gui getInventory() {
        return gui;
    }
}
