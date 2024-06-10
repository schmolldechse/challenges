package io.github.schmolldechse.challenge.map.player.maxhearts;

import com.google.inject.Inject;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import io.github.schmolldechse.challenge.Challenge;
import io.github.schmolldechse.challenge.Identification;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class MaxHealthSetting extends Challenge implements Listener {

    public double maxHealth = 20.0D;

    private final MaxHealthInventory settingsInventory;

    @Inject
    public MaxHealthSetting() {
        super("setting_maxhealth");

        this.settingsInventory = new MaxHealthInventory(this);
    }

    @Override
    public Identification challengeIdentification() {
        return Identification.PLAYER;
    }

    @Override
    public ItemStack getItemStack() {
        return ItemBuilder.from(Material.GOLDEN_APPLE)
                .name(this.getDisplayName())
                .lore(this.getDescription())
                .pdc(persistentDataContainer -> persistentDataContainer.set(this.key, PersistentDataType.STRING, this.getIdentifierName()))
                .build();
    }

    @Override
    public Component getDisplayName() {
        return Component.text("Festgelegte Herzen", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public List<Component> getDescription() {
        return Arrays.asList(
                Component.empty(),
                Component.text("Setze die maximalen Herzen fest,", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.text("die ein Spieler besitzen kann", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                this.activationComponent(),
                Component.empty(),
                Component.text("[Rechtsklick]", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, true)
                        .append(Component.text(" zum Bearbeiten", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
        );
    }

    @Override
    public void onActivate() {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        this.updateHearts(this.maxHealth);
    }

    @Override
    public void onDeactivate() {
        PlayerJoinEvent.getHandlerList().unregister(this);
        this.updateHearts(20.0D);
    }

    @Override
    public Map<String, Object> save() {
        Map<String, Object> data = new HashMap<>();
        data.put("maxHealth", this.maxHealth);
        return data;
    }

    @Override
    public void append(Map<String, Object> data) {
        this.maxHealth = (double) data.get("maxHealth");
        this.updateHearts(this.maxHealth);
    }

    @Override
    public void openSettings(Player player) {
        this.settingsInventory.getInventory().open(player);
    }

    @EventHandler
    public void execute(PlayerJoinEvent event) {
        if (!this.active) return;
        if (this.plugin.timerHandler.isPaused()) return;

        event.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(this.maxHealth);
    }

    public void updateHearts(double hearts) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.getGameMode() != GameMode.SURVIVAL) return;

            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(hearts);
            player.setHealth(hearts);
        });
    }

    public List<Component> heartDisplay() {
        int fullHearts = (int) maxHealth / 2;
        boolean halfHeart = maxHealth % 2 != 0;

        List<Component> components = new ArrayList<>();
        Component line = Component.empty();
        for (int i = 0; i < fullHearts; i++) {
            line = line.append(MiniMessage.miniMessage().deserialize("<color:#FF0000>❤</color>"));
            if ((i + 1) % 10 == 0) {
                components.add(line);
                line = Component.empty();
            }
        }

        if (halfHeart) line = line.append(MiniMessage.miniMessage().deserialize("<color:#FF6666>❤</color>"));
        components.add(line);
        return components;
    }
}