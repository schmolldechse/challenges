package io.github.schmolldechse.challenge.map.challenge.randomizer.modules;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import io.github.schmolldechse.challenge.map.challenge.randomizer.RandomizerChallenge;
import io.github.schmolldechse.challenge.module.Module;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RandomEntities extends Module<RandomizerChallenge> implements Listener {

    private final Random random = new Random();

    private Map<EntityType, EntityType> entitiesRandomizerMap = new HashMap<>();
    private final List<EntityType> filteredEntityTypes;

    public RandomEntities(RandomizerChallenge challenge) {
        super(challenge, "randomizer_entities");

        this.filteredEntityTypes = Stream.of(EntityType.values())
                .filter(EntityType::isSpawnable)
                .filter(EntityType::isAlive)
                .filter(entityType -> !this.challenge.excludedEntities.contains(entityType))
                .filter(entityType -> entityType != EntityType.ENDER_DRAGON)
                .filter(entityType -> entityType != EntityType.WITHER)
                .toList();

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @Override
    public ItemStack getItemStack() {
        return ItemBuilder.from(Material.CREEPER_HEAD)
                .name(this.getDisplayName())
                .lore(this.getDescription())
                .pdc(persistentDataContainer -> persistentDataContainer.set(this.key, PersistentDataType.STRING, this.getIdentifierName()))
                .build();
    }

    @Override
    public Component getDisplayName() {
        return Component.text("Zufällige Entities", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.text("Achtung ", NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, true).decoration(TextDecoration.BOLD, true)
                        .append(Component.text("das ist SEHR unbalanced", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false)
                                .decoration(TextDecoration.BOLD, false)
                        ),
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
    public void toggle() {
        super.toggle();

        if (!this.active) return;

        List<EntityType> copy = new ArrayList<>(this.filteredEntityTypes);

        this.entitiesRandomizerMap = this.filteredEntityTypes.stream()
                .collect(Collectors.toMap(
                        entityType -> entityType,
                        entityType -> {
                            EntityType randomEntity = copy.get(this.random.nextInt(copy.size()));
                            copy.remove(randomEntity);
                            return randomEntity;
                        }
                ));

        this.plugin.getLogger().info("Shuffled " + this.getIdentifierName() + " with " + this.entitiesRandomizerMap.size() + " entities");
    }

    @Override
    public Map<String, Object> save() {
        Map<String, Object> data = new HashMap<>();

        List<List<String>> entitiesSerialized = this.entitiesRandomizerMap.entrySet().stream()
                .map(entry -> Arrays.asList(entry.getKey().name(), entry.getValue().name()))
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
                            entry -> EntityType.valueOf(entry.get(1))
                    ));
        }
    }

    private int entityFloodCount = 0;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(CreatureSpawnEvent event) {
        if (!this.challenge.isActive()) return;
        if (this.plugin.timerHandler.isPaused()) return;

        if (!this.active) return;

        // blocks entities spawned by plugins, so they will not end in a loop
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) return;

        if (!this.entitiesRandomizerMap.containsKey(event.getEntityType())) return;
        EntityType randomEntityType = this.entitiesRandomizerMap.get(event.getEntityType());

        // prevent ocean being flooded by entities which causes lag
        Biome biome = event.getLocation().getWorld().getBiome(event.getLocation().getBlockX(), event.getLocation().getBlockY(), event.getLocation().getBlockZ());
        if (biome == Biome.OCEAN
                || biome == Biome.FROZEN_OCEAN
                || biome == Biome.DEEP_OCEAN
                || biome == Biome.WARM_OCEAN
                || biome == Biome.LUKEWARM_OCEAN
                || biome == Biome.COLD_OCEAN
                || biome == Biome.DEEP_LUKEWARM_OCEAN
                || biome == Biome.DEEP_COLD_OCEAN
                || biome == Biome.DEEP_FROZEN_OCEAN
                || biome == Biome.RIVER
                || biome == Biome.FROZEN_RIVER) {
            this.entityFloodCount++;
            if (this.entityFloodCount % 3 != 0) return;
            this.entityFloodCount = 0;
        }

        event.getLocation().getWorld().spawnEntity(event.getLocation(), randomEntityType, CreatureSpawnEvent.SpawnReason.CUSTOM);
        event.setCancelled(true);
    }
}
