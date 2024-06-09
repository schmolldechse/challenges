package io.github.schmolldechse.challenge.map.challenge.randomizer.modules;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import io.github.schmolldechse.challenge.map.challenge.randomizer.RandomizerChallenge;
import io.github.schmolldechse.challenge.module.Module;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RandomEntityDrops extends Module<RandomizerChallenge> implements Listener {

    private Map<EntityType, LootTable> entitiesRandomizerMap = new HashMap<>();
    private final List<EntityType> filteredEntityTypes;
    private final List<LootTable> filteredLootTables;

    public RandomEntityDrops(RandomizerChallenge challenge) {
        super(challenge, "randomizer_entity_drops");

        this.filteredEntityTypes = Stream.of(EntityType.values())
                .filter(EntityType::isSpawnable)
                .filter(EntityType::isAlive)
                .filter(entityType -> !this.challenge.excludedEntities.contains(entityType))
                .collect(Collectors.toList());

        this.filteredLootTables = Stream.of(LootTables.values())
                .map(LootTables::getLootTable)
                .filter(lootTable -> lootTable.getKey().getKey().startsWith("entities/"))
                .collect(Collectors.toList());

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public ItemStack getItemStack() {
        return ItemBuilder.from(Material.GUNPOWDER)
                .name(this.getDisplayName())
                .lore(this.getDescription())
                .pdc(persistentDataContainer -> persistentDataContainer.set(this.key, PersistentDataType.STRING, this.getIdentifierName()))
                .build();
    }

    @Override
    public Component getDisplayName() {
        return Component.text("Drops aus Entities", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.text("Beinhaltet " + this.filteredLootTables.size() + " Drops", NamedTextColor.GRAY),
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
    public void toggle() {
        super.toggle();

        if (!this.active) return;

        Collections.shuffle(this.filteredEntityTypes);
        Collections.shuffle(this.filteredLootTables);

        this.entitiesRandomizerMap = IntStream.range(0, this.filteredEntityTypes.size())
                .boxed()
                .collect(Collectors.toMap(this.filteredEntityTypes::get, this.filteredLootTables::get));

        this.plugin.getLogger().info("Shuffled " + this.getIdentifierName() + " with " + this.entitiesRandomizerMap.size() + " entities");
    }

    @Override
    public Map<String, Object> save() {
        Map<String, Object> data = new HashMap<>();

        List<List<String>> entitiesSerialized = this.entitiesRandomizerMap.entrySet().stream()
                .map(entry -> Arrays.asList(entry.getKey().name(), entry.getValue().getKey().getKey()))
                .toList();
        data.put("map", entitiesSerialized);

        return data;
    }

    @Override
    public void append(Map<String, Object> data) {
        if (data.containsKey("map")) {
            List<List<String>> entitiesSerialized = (List<List<String>>) data.get("map");
            this.entitiesRandomizerMap = entitiesSerialized.stream()
                    .collect(Collectors.toMap(
                            entry -> EntityType.valueOf(entry.get(0)),
                            entry -> Bukkit.getLootTable(NamespacedKey.fromString(entry.get(1)))
                    ));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(EntityDeathEvent event) {
        if (!this.challenge.isActive()) return;
        if (this.plugin.timerHandler.isPaused()) return;

        if (!this.active) return;

        if (!this.entitiesRandomizerMap.containsKey(event.getEntityType())) return;
        LootTable lootTable = this.entitiesRandomizerMap.get(event.getEntityType());
        LootContext.Builder builder = new LootContext.Builder(event.getEntity().getLocation())
                .lootedEntity(event.getEntity());

        if (event.getEntity().getKiller() != null) builder.killer(event.getEntity().getKiller());

        event.getDrops().clear();

        Collection<ItemStack> loot = lootTable.populateLoot(ThreadLocalRandom.current(), builder.build());
        event.getDrops().addAll(loot);
    }
}
