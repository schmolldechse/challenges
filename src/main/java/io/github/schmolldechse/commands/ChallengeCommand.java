package io.github.schmolldechse.commands;

import dev.jorel.commandapi.CommandTree;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.challenge.ChallengeHandler;
import io.github.schmolldechse.timer.TimerHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ChallengeCommand {

    // /setup

    private final Plugin plugin;

    private final TimerHandler timerHandler;
    private final ChallengeHandler challengeHandler;

    private final PaginatedGui gui;

    public ChallengeCommand(TimerHandler timerHandler, ChallengeHandler challengeHandler) {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);

        this.timerHandler = timerHandler;
        this.challengeHandler = challengeHandler;

        this.gui = Gui.paginated()
                .title(Component.text("Challenges"))
                .rows(6)
                .pageSize(45)
                .disableAllInteractions()
                .create();

        this.gui.setDefaultClickAction(event -> {
            if (event.getCurrentItem() == null) return;

            NamespacedKey key = new NamespacedKey(this.plugin, "identifier");
            if (!event.getCurrentItem().getItemMeta().getPersistentDataContainer().has(key)) return;

            String identifier = event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
            this.challengeHandler.toggle(identifier);
            this.updateGuiItems();
        });

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
    }

    public void registerCommand() {
        new CommandTree("challenge")
                .executesPlayer((sender, args) -> {
                    this.updateGuiItems();
                    this.gui.open(sender);
                })
                .register();
    }

    private void updateGuiItems() {
        this.gui.clearPageItems();

        this.challengeHandler.registeredChallenges.forEach((identifier, challenge) -> {
            ItemBuilder itemBuilder = ItemBuilder.from(challenge.getItemStack());
            if (challenge.isActive()) itemBuilder.glow(challenge.isActive());

            this.gui.addItem(itemBuilder.asGuiItem());
        });

        this.gui.update();
    }
}
