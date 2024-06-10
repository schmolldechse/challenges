package io.github.schmolldechse.challenge.map.player;

import com.google.inject.Inject;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import io.github.schmolldechse.challenge.Challenge;
import io.github.schmolldechse.challenge.Identification;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class OneDiesSetting extends Challenge implements Listener {

    @Inject
    public OneDiesSetting() {
        super("setting_onedies");
    }

    @Override
    public Identification challengeIdentification() {
        return Identification.PLAYER;
    }

    @Override
    public ItemStack getItemStack() {
        return ItemBuilder.from(Material.SKELETON_SKULL)
                .name(this.getDisplayName())
                .lore(this.getDescription())
                .pdc(persistentDataContainer -> persistentDataContainer.set(this.key, PersistentDataType.STRING, this.getIdentifierName()))
                .build();
    }

    @Override
    public Component getDisplayName() {
        return Component.text("Einer stirbt", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public List<Component> getDescription() {
        return Arrays.asList(
                Component.empty(),
                Component.text("Sobald ein Spieler stirbt, ist", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.text("die Challenge fehlgeschlagen", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                this.activationComponent()
        );
    }

    @Override
    public void onActivate() {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public void onDeactivate() {
        PlayerDeathEvent.getHandlerList().unregister(this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(PlayerDeathEvent event) {
        if (!this.active) return;
        if (this.plugin.timerHandler.isPaused()) return;

        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;

        event.deathMessage(
                Component.text(event.getPlayer().getName(), NamedTextColor.GREEN)
                        .append(Component.text(" ist gestorben!", NamedTextColor.RED))
        );

        this.fail();
    }
}
