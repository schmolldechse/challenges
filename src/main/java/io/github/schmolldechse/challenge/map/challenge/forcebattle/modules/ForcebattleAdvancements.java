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
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ForcebattleAdvancements extends ForcebattleModule implements Listener {

    private final List<Advancement> filteredAdvancementList;

    public ForcebattleAdvancements(ForcebattleChallenge challenge) {
        super(challenge, "forcebattle_advancements");

        this.filteredAdvancementList = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(Bukkit.advancementIterator(), Spliterator.ORDERED), false)
                .filter(advancement -> !advancement.getCriteria().isEmpty())
                .filter(advancement -> advancement.getDisplay() != null)
                .filter(advancement -> advancement.getDisplay().doesAnnounceToChat())
                .collect(Collectors.toList());
    }

    @Override
    public ItemStack getItemStack() {
        return ItemBuilder.from(Material.KNOWLEDGE_BOOK)
                .name(this.getDisplayName())
                .lore(this.getDescription())
                .persistentDataContainer(persistentDataContainer -> persistentDataContainer.set(this.key, PersistentDataType.STRING, this.getIdentifierName()))
                .build();
    }

    @Override
    public Component getDisplayName() {
        return Component.text("Advancements", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.text("Beinhaltet " + this.filteredAdvancementList.size() + " Advancements", NamedTextColor.GRAY),
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
        PlayerAdvancementDoneEvent.getHandlerList().unregister(this);
    }

    @Override
    public void toggle() {
        super.toggle();

        if (!this.active) {
            this.challenge.tasks.removeIf(task -> task.getTaskType() == ForcebattleTask.TaskType.ADVANCEMENT);
            return;
        }

        this.filteredAdvancementList.stream()
                .map(advancement -> new ForcebattleTask(
                        advancement.getKey(),
                        ForcebattleTask.TaskType.ADVANCEMENT,
                        null
                ))
                .forEach(this.challenge.tasks::add);
    }

    @Override
    public void onTaskComplete(Team team, ForcebattleTask task) {
        if (!this.challenge.isActive()) return;
        if (this.plugin.timerHandler.isPaused()) return;

        if (!this.active) return;

        if (task.getTaskType() != ForcebattleTask.TaskType.ADVANCEMENT) return;

        team.getUuids().forEach(uuid -> {
            Player player = this.plugin.getServer().getPlayer(uuid);
            if (player == null) return;

            if (!player.getAdvancementProgress(Bukkit.getAdvancement(task.getKey())).isDone()) return;

            task.setFinishedData(new ForcebattleTask.FinishedData(
                    false,
                    this.plugin.timerHandler.elapsed,
                    player.getName()
            ));
            this.challenge.nextTask(team, task);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(PlayerAdvancementDoneEvent event) {
        if (!this.challenge.isActive()) return;
        if (this.plugin.timerHandler.isPaused()) return;

        if (!this.active) return;

        Player player = event.getPlayer();

        Team team = this.plugin.teamHandler.team(player);
        if (team == null) return;

        ForcebattleExtension extension = team.getExtension(ForcebattleExtension.class).orElse(null);
        if (extension == null) {
            player.sendMessage(Component.text("(!) Your team is not registered for the forcebattle challenge!", NamedTextColor.RED));
            return;
        }

        if (player.getGameMode() != GameMode.SURVIVAL) return;

        ForcebattleTask currentTask = extension.getCurrentTask();
        if (currentTask == null) return;
        if (currentTask.getTaskType() != ForcebattleTask.TaskType.ADVANCEMENT) return;

        if (!event.getAdvancement().getKey().equals(currentTask.getKey())) return;

        currentTask.setFinishedData(new ForcebattleTask.FinishedData(
                false,
                this.plugin.timerHandler.elapsed,
                event.getPlayer().getName()
        ));

        this.challenge.nextTask(team, currentTask);
    }
}
