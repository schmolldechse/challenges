package io.github.schmolldechse.challenge.map.condition;

import io.github.schmolldechse.challenge.Challenge;
import io.github.schmolldechse.challenge.Identification;
import io.github.schmolldechse.misc.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class EnderDragonCondition extends Challenge implements Listener {

    public EnderDragonCondition() {
        super("condition_enderdragon");
    }

    @Override
    public Identification challengeIdentification() {
        return Identification.CONDITION;
    }

    @Override
    public ItemStack getItemStack() {
        return ItemBuilder.from(Material.END_STONE)
                .name(this.getDisplayName())
                .lore(this.getDescription())
                .persistentDataContainer(persistentDataContainer -> persistentDataContainer.set(this.key, PersistentDataType.STRING, this.getIdentifierName()))
                .build();
    }

    @Override
    public Component getDisplayName() {
        return Component.text("Ender Drache", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public List<Component> getDescription() {
        return Arrays.asList(
                Component.empty(),
                Component.text("Die Challenge gilt als geschlagen,", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.text("wenn der Ender Drache geschlagen wird", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
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
        EntityDeathEvent.getHandlerList().unregister(this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(EntityDeathEvent event) {
        if (!this.active) return;
        if (this.plugin.timerHandler.isPaused()) return;

        if (!(event.getEntity() instanceof EnderDragon)) return;

        this.success();
    }
}
