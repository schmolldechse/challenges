package io.github.schmolldechse.challenge.map.world;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import io.github.schmolldechse.challenge.Challenge;
import io.github.schmolldechse.challenge.Identification;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class HardcoreSetting extends Challenge implements Listener {

    public HardcoreSetting() {
        super("setting_hardcore");

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public Identification challengeIdentification() {
        return Identification.WORLD;
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
        return Component.text("Hardcore", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public List<Component> getDescription() {
        return Arrays.asList(
                Component.empty(),
                Component.text("Regeneration von Herzen ist nur", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.text("noch durch Goldene Äpfel möglich", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                this.activationComponent()
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(EntityRegainHealthEvent event) {
        if (!this.active) return;
        if (this.plugin.timerHandler.isPaused()) return;

        if (!(event.getEntity() instanceof Player player)) return;
        if (player.getGameMode() != GameMode.SURVIVAL) return;

        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED) return;
        event.setCancelled(true);
    }
}
