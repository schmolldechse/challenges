package io.github.schmolldechse.challenge.map.player;

import com.google.inject.Inject;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.challenge.Challenge;
import io.github.schmolldechse.challenge.Identification;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OneDiesSetting extends Challenge {

    private final Plugin plugin;

    @Inject
    public OneDiesSetting() {
        super("setting_onedies");

        this.plugin = JavaPlugin.getPlugin(Plugin.class);
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
        Component activated = this.active
                ? Component.text("Aktiviert", NamedTextColor.GREEN)
                : Component.text("Deaktiviert", NamedTextColor.RED);

        return Arrays.asList(
                Component.empty(),
                Component.text("Sobald ein Spieler stirbt, ist", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.text("die Challenge fehlgeschlagen", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                activated
        );
    }

    @Override
    public Map<String, Object> save() {
        return Map.of();
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
