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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RandomChestLootTables extends Module<RandomizerChallenge> implements Listener {

    private final Random random = new Random();

    private Map<LootTable, LootTable> lootTableRandomizerMap = new HashMap<>();
    private final List<LootTables> filteredLootTables;

    public RandomChestLootTables(RandomizerChallenge challenge) {
        super(challenge, "randomizer_chestloottables");

        this.filteredLootTables = Stream.of(LootTables.values())
                .filter(lootTable -> lootTable.getKey().getKey().startsWith("chests/"))
                .filter(lootTable -> !this.challenge.excludedLootTables.contains(lootTable))
                .collect(Collectors.toList());

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public ItemStack getItemStack() {
        return ItemBuilder.from(Material.CHEST)
                .name(this.getDisplayName())
                .lore(this.getDescription())
                .pdc(persistentDataContainer -> persistentDataContainer.set(this.key, PersistentDataType.STRING, this.getIdentifierName()))
                .build();
    }

    @Override
    public Component getDisplayName() {
        return Component.text("Loot aus Kisten", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.text("Beinhaltet " + this.lootTableRandomizerMap.size() + " Drops", NamedTextColor.GRAY),
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

        List<LootTables> copy = new ArrayList<>(this.filteredLootTables);

        this.lootTableRandomizerMap = this.filteredLootTables.stream()
                .collect(Collectors.toMap(
                        LootTables::getLootTable,
                        lootTable -> {
                            LootTables randomLootTable = copy.get(this.random.nextInt(copy.size()));
                            copy.remove(randomLootTable);
                            return randomLootTable.getLootTable();
                        }
                ));

        this.plugin.getLogger().info("Shuffled " + this.getIdentifierName() + " with " + this.lootTableRandomizerMap.size() + " loot tables");
    }

    @Override
    public Map<String, Object> save() {
        Map<String, Object> data = new HashMap<>();

        /**
         * getKey() & getValue() each returns {@link org.bukkit.NamespacedKey}, and their getKey() returns a String
         */
        List<List<String>> blocksSerialized = this.lootTableRandomizerMap.entrySet().stream()
                .map(entry -> Arrays.asList(entry.getKey().getKey().getKey(), entry.getValue().getKey().getKey()))
                .toList();
        data.put("map", blocksSerialized);
        data.put("active", this.active);

        return data;
    }

    @Override
    public void append(Map<String, Object> data) {
        if (data.containsKey("active")) this.active = (boolean) data.get("active");

        if (data.containsKey("map")) {
            List<List<String>> serialized = (List<List<String>>) data.get("map");
            this.lootTableRandomizerMap = serialized.stream()
                    .collect(Collectors.toMap(
                            entry -> Bukkit.getLootTable(NamespacedKey.fromString(entry.get(0))),
                            entry -> Bukkit.getLootTable(NamespacedKey.fromString(entry.get(1)))
                    ));
        }
    }

    @EventHandler
    public void execute(LootGenerateEvent event) {
        if (!this.challenge.isActive()) return;
        if (this.plugin.timerHandler.isPaused()) return;

        if (!this.active) return;

        if (!this.lootTableRandomizerMap.containsKey(event.getLootTable())) return;
        LootTable lootTable = this.lootTableRandomizerMap.get(event.getLootTable());
        LootContext.Builder builder = new LootContext.Builder(event.getLootContext().getLocation());

        Collection<ItemStack> items = lootTable.populateLoot(ThreadLocalRandom.current(), builder.build());
        event.setLoot(items);
    }
}
