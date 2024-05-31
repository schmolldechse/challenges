package io.github.schmolldechse.challenge.map.world;

import com.google.inject.Inject;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.challenge.Challenge;
import io.github.schmolldechse.challenge.Identification;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CraftingtableSetting extends Challenge {

    private final Plugin plugin;

    @Inject
    public CraftingtableSetting() {
        super("setting_nocraftingtable");

        this.plugin = JavaPlugin.getPlugin(Plugin.class);
    }

    @Override
    public Identification challengeIdentification() {
        return Identification.WORLD;
    }

    @Override
    public ItemStack getItemStack() {
        return ItemBuilder.from(Material.CRAFTING_TABLE)
                .name(this.getDisplayName())
                .lore(this.getDescription())
                .pdc(persistentDataContainer -> persistentDataContainer.set(this.key, PersistentDataType.STRING, this.getIdentifierName()))
                .build();
    }

    @Override
    public Component getDisplayName() {
        return Component.text("Ohne Werkbank", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public List<Component> getDescription() {
        Component activated = this.active
                ? Component.text("Aktiviert", NamedTextColor.GREEN)
                : Component.text("Deaktiviert", NamedTextColor.RED);

        return Arrays.asList(
                Component.empty(),
                Component.text("Verwendest oder stellst du dir eine", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.text("Werkbank her, ist die Challenge gescheitert", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                activated
        );
    }

    @Override
    public Map<String, Object> save() {
        return Map.of();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void execute(CraftItemEvent event) {
        if (!this.active) return;
        if (this.plugin.timerHandler.isPaused()) return;

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (player.getGameMode() != GameMode.SURVIVAL) return;

        if (event.getInventory().getType() == InventoryType.WORKBENCH) {
            Bukkit.broadcast(
                    Component.text(player.getName(), NamedTextColor.GREEN)
                            .append(Component.text(" hat eine Werkbank verwendet", NamedTextColor.RED))
            );
            this.fail();
            return;
        }

        if (event.getCurrentItem().getType() != Material.CRAFTING_TABLE) return;

        Bukkit.broadcast(Component.text(player.getName(), NamedTextColor.GREEN).append(Component.text(" hat eine Werkbank hergestellt", NamedTextColor.RED)));
        this.fail();
    }
}
