package io.github.schmolldechse.challenge.map.challenge.forcebattle.modules;

import io.github.schmolldechse.challenge.map.challenge.forcebattle.ForcebattleChallenge;
import io.github.schmolldechse.challenge.map.challenge.forcebattle.ForcebattleModule;
import io.github.schmolldechse.challenge.map.challenge.forcebattle.ForcebattleTask;
import io.github.schmolldechse.challenge.map.challenge.forcebattle.team.ForcebattleExtension;
import io.github.schmolldechse.misc.item.ItemBuilder;
import io.github.schmolldechse.team.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.stream.Stream;

public class ForcebattleEntities extends ForcebattleModule implements Listener {

    private final List<EntityType> filteredEntityTypes;

    public ForcebattleEntities(ForcebattleChallenge challenge) {
        super(challenge, "forcebattle_entities");

        this.filteredEntityTypes = Stream.of(EntityType.values())
                .filter(EntityType::isSpawnable)
                .filter(EntityType::isAlive)
                .filter(entityType -> !this.challenge.excludedEntities.contains(entityType))
                .filter(entityType -> entityType != EntityType.ARMOR_STAND)
                .filter(entityType -> entityType != EntityType.GIANT) // giants can't spawn
                .toList();
    }

    @Override
    public ItemStack getItemStack() {
        return ItemBuilder.from(Material.CREEPER_HEAD)
                .name(this.getDisplayName())
                .lore(this.getDescription())
                .persistentDataContainer(persistentDataContainer -> persistentDataContainer.set(this.key, PersistentDataType.STRING, this.getIdentifierName()))
                .build();
    }

    @Override
    public Component getDisplayName() {
        return Component.text("Entities", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.text("Beinhaltet " + this.filteredEntityTypes.size() + " Entities", NamedTextColor.GRAY),
                Component.empty(),
                Component.text("[Klick]", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, true)
                        .append(Component.text(" zum (De-) Aktivieren", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)),
                Component.empty(),
                this.active
                        ? Component.text("Aktiviert", NamedTextColor.GREEN)
                        : Component.text("Deaktiviert", NamedTextColor.RED)
        );
    }

    @Override
    public void activate() {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public void deactivate() {
        EntityDeathEvent.getHandlerList().unregister(this);
    }

    @Override
    public void toggle() {
        super.toggle();

        if (!this.active) {
            this.challenge.tasks.removeIf(task -> task.getTaskType() == ForcebattleTask.TaskType.ENTITY);
            return;
        }

        this.filteredEntityTypes.stream()
                .map(entity -> new ForcebattleTask(
                        entity.getKey(),
                        ForcebattleTask.TaskType.ENTITY,
                        null
                ))
                .forEach(this.challenge.tasks::add);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(EntityDeathEvent event) {
        if (!this.challenge.isActive()) return;
        if (this.plugin.timerHandler.isPaused()) return;

        if (!this.active) return;

        if (event.getEntity().getKiller() == null) return;

        Team team = this.plugin.teamHandler.team(event.getEntity().getKiller());
        if (team == null) return;

        ForcebattleExtension extension = team.getExtension(ForcebattleExtension.class).orElse(null);
        if (extension == null) {
            event.getEntity().getKiller().sendMessage(Component.text("(!) Your team is not registered for the forcebattle challenge!", NamedTextColor.RED));
            return;
        }

        ForcebattleTask currentTask = extension.getCurrentTask();
        if (currentTask == null) return;
        if (currentTask.getTaskType() != ForcebattleTask.TaskType.ENTITY) return;

        if (!event.getEntityType().getKey().equals(currentTask.getKey())) return;

        currentTask.setFinishedData(new ForcebattleTask.FinishedData(
                false,
                this.plugin.timerHandler.elapsed,
                event.getEntity().getKiller().getName()
        ));

        this.challenge.nextTask(team, currentTask);
    }
}