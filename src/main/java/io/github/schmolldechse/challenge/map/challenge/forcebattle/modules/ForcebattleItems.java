package io.github.schmolldechse.challenge.map.challenge.forcebattle.modules;

import io.github.schmolldechse.challenge.map.challenge.forcebattle.ForcebattleChallenge;
import io.github.schmolldechse.challenge.map.challenge.forcebattle.ForcebattleModule;
import io.github.schmolldechse.challenge.map.challenge.forcebattle.ForcebattleTask;
import io.github.schmolldechse.challenge.map.challenge.forcebattle.team.ForcebattleExtension;
import io.github.schmolldechse.misc.item.ItemBuilder;
import io.github.schmolldechse.team.Team;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ForcebattleItems extends ForcebattleModule implements Listener {

    private final List<Material> filteredItemList;

    public ForcebattleItems(ForcebattleChallenge challenge) {
        super(challenge, "forcebattle_items");

        this.filteredItemList = Stream.of(Material.values())
                .filter(Material::isItem)
                .filter(material -> !this.challenge.excludedMaterials.contains(material))
                .filter(material -> !material.name().endsWith("_SPAWN_EGG"))
                // Items that are not obtainable in survival 
                .filter(material -> material != Material.DEBUG_STICK)
                .filter(material -> material != Material.WRITTEN_BOOK)
                .filter(material -> material != Material.COMMAND_BLOCK)
                .filter(material -> material != Material.COMMAND_BLOCK_MINECART)
                .filter(material -> material != Material.CHAIN_COMMAND_BLOCK)
                .filter(material -> material != Material.REPEATING_COMMAND_BLOCK)
                .filter(material -> material != Material.STRUCTURE_BLOCK)
                .filter(material -> material != Material.STRUCTURE_VOID)
                .filter(material -> material != Material.BARRIER)
                .filter(material -> material != Material.VAULT)
                .filter(material -> material != Material.TRIAL_SPAWNER)
                .filter(material -> material != Material.SPAWNER)
                .filter(material -> material != Material.BEDROCK)
                .filter(material -> material != Material.LIGHT)
                .filter(material -> material != Material.REINFORCED_DEEPSLATE)
                .filter(material -> material != Material.JIGSAW)
                .filter(material -> material != Material.BUDDING_AMETHYST)
                .filter(material -> material != Material.DRAGON_EGG)
                .filter(material -> material != Material.KNOWLEDGE_BOOK)
                .toList();
    }

    @Override
    public ItemStack getItemStack() {
        return ItemBuilder.from(Material.GRASS_BLOCK)
                .name(this.getDisplayName())
                .lore(this.getDescription())
                .persistentDataContainer(persistentDataContainer -> persistentDataContainer.set(this.key, PersistentDataType.STRING, this.getIdentifierName()))
                .build();
    }

    @Override
    public Component getDisplayName() {
        return Component.text("Items", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.text("Beinhaltet " + this.filteredItemList.size() + " Items", NamedTextColor.GRAY),
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
        PlayerInventorySlotChangeEvent.getHandlerList().unregister(this);
    }

    @Override
    public void toggle() {
        super.toggle();

        if (!this.active) {
            this.challenge.tasks.removeIf(task -> task.getTaskType() == ForcebattleTask.TaskType.ITEM);
            return;
        }

        this.filteredItemList.stream()
                .map(entity -> new ForcebattleTask(
                        entity.getKey(),
                        ForcebattleTask.TaskType.ITEM,
                        null
                ))
                .forEach(this.challenge.tasks::add);
    }

    @Override
    public void onTaskComplete(Team team, ForcebattleTask task) {
        if (!this.challenge.isActive()) return;
        if (this.plugin.timerHandler.isPaused()) return;

        if (!this.active) return;

        if (task.getTaskType() != ForcebattleTask.TaskType.ITEM) return;

        team.getUuids().forEach(uuid -> {
            Player player = this.plugin.getServer().getPlayer(uuid);
            if (player == null) return;

            boolean hasItem = Arrays.stream(player.getInventory().getContents())
                    .anyMatch(item -> item != null && item.getType().getKey().equals(task.getKey()));
            if (!hasItem) return;

            task.setFinishedData(new ForcebattleTask.FinishedData(
                    false,
                    this.plugin.timerHandler.elapsed,
                    player.getName()
            ));
            this.challenge.nextTask(team, task);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(PlayerInventorySlotChangeEvent event) {
        if (!this.challenge.isActive()) return;
        if (this.plugin.timerHandler.isPaused()) return;

        if (!this.active) return;

        Team team = this.plugin.teamHandler.team(event.getPlayer());
        if (team == null) return;

        ForcebattleExtension extension = team.getExtension(ForcebattleExtension.class).orElse(null);
        if (extension == null) {
            event.getPlayer().sendMessage(Component.text("(!) Your team is not registered for the forcebattle challenge!", NamedTextColor.RED));
            return;
        }

        ForcebattleTask currentTask = extension.getCurrentTask();
        if (currentTask == null) return;
        if (currentTask.getTaskType() != ForcebattleTask.TaskType.ITEM) return;

        if (!event.getNewItemStack().getType().getKey().equals(currentTask.getKey())) return;

        currentTask.setFinishedData(new ForcebattleTask.FinishedData(
                false,
                this.plugin.timerHandler.elapsed,
                event.getPlayer().getName()
        ));

        this.challenge.nextTask(team, currentTask);
    }
}