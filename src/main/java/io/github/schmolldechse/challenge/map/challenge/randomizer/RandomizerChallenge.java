package io.github.schmolldechse.challenge.map.challenge.randomizer;

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
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RandomizerChallenge extends Challenge {

    private final Plugin plugin;

    private final Random random = new Random();

    public boolean BLOCKS_RANDOMIZED = false,
            ENTITIES_RANDOMIZED = false,
            CRAFTING_RANDOMIZED = false;
    // Origin -> Random
    public Map<Material, Material> blocksRandomizerMap = new HashMap<>();
    public Map<Material, Material> craftingRandomizerMap = new HashMap<>();
    public Map<EntityType, EntityType> entitiesRandomizerMap = new HashMap<>();

    //TODO: remove in 1.21 again
    private final List<Material> excludedMaterials = List.of(
            Material.TUFF_SLAB, Material.TUFF_STAIRS, Material.TUFF_WALL, Material.CHISELED_TUFF,
            Material.POLISHED_TUFF, Material.POLISHED_TUFF_SLAB, Material.POLISHED_TUFF_STAIRS,
            Material.POLISHED_TUFF_WALL, Material.TUFF_BRICKS, Material.TUFF_BRICK_SLAB,
            Material.TUFF_BRICK_STAIRS, Material.TUFF_BRICK_WALL, Material.CHISELED_TUFF_BRICKS,
            Material.CHISELED_COPPER, Material.EXPOSED_CHISELED_COPPER, Material.WEATHERED_CHISELED_COPPER,
            Material.OXIDIZED_CHISELED_COPPER, Material.WAXED_CHISELED_COPPER, Material.WAXED_EXPOSED_CHISELED_COPPER,
            Material.WAXED_WEATHERED_CHISELED_COPPER, Material.WAXED_OXIDIZED_CHISELED_COPPER, Material.COPPER_DOOR,
            Material.EXPOSED_COPPER_DOOR, Material.WEATHERED_COPPER_DOOR, Material.OXIDIZED_COPPER_DOOR,
            Material.WAXED_COPPER_DOOR, Material.WAXED_EXPOSED_COPPER_DOOR, Material.WAXED_WEATHERED_COPPER_DOOR,
            Material.WAXED_OXIDIZED_COPPER_DOOR, Material.COPPER_TRAPDOOR, Material.EXPOSED_COPPER_TRAPDOOR,
            Material.WEATHERED_COPPER_TRAPDOOR, Material.OXIDIZED_COPPER_TRAPDOOR, Material.WAXED_COPPER_TRAPDOOR,
            Material.WAXED_EXPOSED_COPPER_TRAPDOOR, Material.WAXED_WEATHERED_COPPER_TRAPDOOR, Material.WAXED_OXIDIZED_COPPER_TRAPDOOR,
            Material.BUNDLE, Material.CRAFTER, Material.BREEZE_SPAWN_EGG, Material.COPPER_GRATE,
            Material.EXPOSED_COPPER_GRATE, Material.WEATHERED_COPPER_GRATE, Material.OXIDIZED_COPPER_GRATE,
            Material.WAXED_COPPER_GRATE, Material.WAXED_EXPOSED_COPPER_GRATE, Material.WAXED_WEATHERED_COPPER_GRATE,
            Material.WAXED_OXIDIZED_COPPER_GRATE, Material.COPPER_BULB, Material.EXPOSED_COPPER_BULB,
            Material.WEATHERED_COPPER_BULB, Material.OXIDIZED_COPPER_BULB, Material.WAXED_COPPER_BULB,
            Material.WAXED_EXPOSED_COPPER_BULB, Material.WAXED_WEATHERED_COPPER_BULB, Material.WAXED_OXIDIZED_COPPER_BULB,
            Material.TRIAL_SPAWNER, Material.TRIAL_KEY, Material.AIR
    );

    private final List<EntityType> filteredEntityTypes = Stream.of(EntityType.values())
            .filter(EntityType::isSpawnable)
            .filter(EntityType::isAlive)
            .filter(entityType -> entityType != EntityType.DROPPED_ITEM)
            .filter(entityType -> entityType != EntityType.EXPERIENCE_ORB)
            .filter(entityType -> entityType != EntityType.AREA_EFFECT_CLOUD)
            .filter(entityType -> entityType != EntityType.EGG)
            .filter(entityType -> entityType != EntityType.LEASH_HITCH)
            .filter(entityType -> entityType != EntityType.PAINTING)
            .filter(entityType -> entityType != EntityType.ARROW)
            .filter(entityType -> entityType != EntityType.SNOWBALL)
            .filter(entityType -> entityType != EntityType.FIREBALL)
            .filter(entityType -> entityType != EntityType.SMALL_FIREBALL)
            .filter(entityType -> entityType != EntityType.ENDER_PEARL)
            .filter(entityType -> entityType != EntityType.ENDER_SIGNAL)
            .filter(entityType -> entityType != EntityType.SPLASH_POTION)
            .filter(entityType -> entityType != EntityType.THROWN_EXP_BOTTLE)
            .filter(entityType -> entityType != EntityType.ITEM_FRAME)
            .filter(entityType -> entityType != EntityType.WITHER_SKULL)
            .filter(entityType -> entityType != EntityType.PRIMED_TNT)
            .filter(entityType -> entityType != EntityType.FALLING_BLOCK)
            .filter(entityType -> entityType != EntityType.FIREWORK)
            .filter(entityType -> entityType != EntityType.SPECTRAL_ARROW)
            .filter(entityType -> entityType != EntityType.DRAGON_FIREBALL)
            .filter(entityType -> entityType != EntityType.EVOKER_FANGS)
            .filter(entityType -> entityType != EntityType.LLAMA_SPIT)
            .filter(entityType -> entityType != EntityType.GLOW_ITEM_FRAME)
            .filter(entityType -> entityType != EntityType.MARKER)
            .filter(entityType -> entityType != EntityType.BLOCK_DISPLAY)
            .filter(entityType -> entityType != EntityType.INTERACTION)
            .filter(entityType -> entityType != EntityType.ITEM_DISPLAY)
            .filter(entityType -> entityType != EntityType.TEXT_DISPLAY)
            .filter(entityType -> entityType != EntityType.BREEZE) //TODO: mob will be added in 1.21
            .filter(entityType -> entityType != EntityType.WIND_CHARGE)
            .filter(entityType -> entityType != EntityType.FISHING_HOOK)
            .filter(entityType -> entityType != EntityType.LIGHTNING)
            .filter(entityType -> entityType != EntityType.PLAYER)
            .filter(entityType -> entityType != EntityType.UNKNOWN)
            .filter(entityType -> entityType != EntityType.ENDER_DRAGON)
            .filter(entityType -> entityType != EntityType.MINECART)
            .filter(entityType -> entityType != EntityType.MINECART_CHEST)
            .filter(entityType -> entityType != EntityType.MINECART_FURNACE)
            .filter(entityType -> entityType != EntityType.MINECART_TNT)
            .filter(entityType -> entityType != EntityType.MINECART_HOPPER)
            .filter(entityType -> entityType != EntityType.MINECART_MOB_SPAWNER)
            .filter(entityType -> entityType != EntityType.WITHER)
            .toList();

    private final List<Material> filteredItemList = Stream.of(Material.values())
            .filter(Material::isItem)
            .filter(material -> !this.excludedMaterials.contains(material))
            .filter(material -> material != Material.AIR)
            .toList();

    private final List<Material> filteredItemBlockList = Stream.of(Material.values())
            .filter(Material::isItem)
            .filter(material -> !this.excludedMaterials.contains(material))
            .filter(Material::isBlock)
            .filter(material -> material != Material.AIR)
            .toList();

    private final RandomizerInventory randomizerInventory;

    @Inject
    public RandomizerChallenge() {
        super("challenge_randomizer");

        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.shuffle(true);

        this.randomizerInventory = new RandomizerInventory(this);
        new RandomizerCommand(this).registerCommand();
    }

    @Override
    public Identification challengeIdentification() {
        return Identification.CHALLENGE;
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
        return Component.text("Randomizer", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public List<Component> getDescription() {
        Component activated = this.active
                ? Component.text("Aktiviert", NamedTextColor.GREEN)
                : Component.text("Deaktiviert", NamedTextColor.RED);

        return Arrays.asList(
                Component.empty(),
                Component.text("Irgendwie kommt hier nicht das richtige raus?", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.text("Hier ist dein Glück gefragt! Abgebaute Blöcke,", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.text("Entities und Craften lassen nun etwas völlig", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.text("erscheinen, als gewohnt", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                activated,
                Component.empty(),
                Component.text("[Rechtsklick]", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, true)
                        .append(Component.text(" zum Bearbeiten", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
        );
    }

    @Override
    public void openSettings(Player player) {
        this.randomizerInventory.getInventory().open(player);
    }

    @Override
    public Map<String, Object> save() {
        Map<String, Object> data = new HashMap<>();

        List<List<String>> blocksSerialized = this.blocksRandomizerMap.entrySet().stream()
                .map(entry -> Arrays.asList(entry.getKey().name(), entry.getValue().name()))
                .toList();
        data.put("blocksRandomizerMap", blocksSerialized);
        data.put("BLOCKS_RANDOMIZED", this.BLOCKS_RANDOMIZED);

        List<List<String>> craftingSerialized = this.craftingRandomizerMap.entrySet().stream()
                .map(entry -> Arrays.asList(entry.getKey().name(), entry.getValue().name()))
                .toList();
        data.put("craftingRandomizerMap", craftingSerialized);
        data.put("CRAFTING_RANDOMIZED", this.CRAFTING_RANDOMIZED);

        List<List<String>> entitiesSerialized = this.entitiesRandomizerMap.entrySet().stream()
                .map(entry -> Arrays.asList(entry.getKey().name(), entry.getValue().name()))
                .toList();
        data.put("entitiesRandomizerMap", entitiesSerialized);
        data.put("ENTITIES_RANDOMIZED", this.ENTITIES_RANDOMIZED);

        return data;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void append(Map<String, Object> data) {
        List<List<String>> blocksSerialized = (List<List<String>>) data.get("blocksRandomizerMap");
        this.blocksRandomizerMap = blocksSerialized.stream()
                .collect(Collectors.toMap(
                        pair -> Material.valueOf(pair.get(0)),
                        pair -> Material.valueOf(pair.get(1))
                ));
        this.BLOCKS_RANDOMIZED = (boolean) data.get("BLOCKS_RANDOMIZED");

        List<List<String>> entitiesSerialized = (List<List<String>>) data.get("entitiesRandomizerMap");
        this.entitiesRandomizerMap = entitiesSerialized.stream()
                .collect(Collectors.toMap(
                        pair -> EntityType.valueOf(pair.get(0)),
                        pair -> EntityType.valueOf(pair.get(1))
                ));
        this.ENTITIES_RANDOMIZED = (boolean) data.get("ENTITIES_RANDOMIZED");

        List<List<String>> craftingSerialized = (List<List<String>>) data.get("craftingRandomizerMap");
        this.craftingRandomizerMap = craftingSerialized.stream()
                .collect(Collectors.toMap(
                        pair -> Material.valueOf(pair.get(0)),
                        pair -> Material.valueOf(pair.get(1))
                ));
        this.CRAFTING_RANDOMIZED = (boolean) data.get("CRAFTING_RANDOMIZED");
    }

    protected void shuffle(boolean force) {
        if (force || this.BLOCKS_RANDOMIZED) {
            List<Material> copy = new ArrayList<>(this.filteredItemBlockList);

            this.blocksRandomizerMap = this.filteredItemBlockList.stream()
                    .collect(Collectors.toMap(
                            material -> material,
                            material -> {
                                Material randomMaterial = copy.get(this.random.nextInt(copy.size()));
                                copy.remove(randomMaterial);
                                return randomMaterial;
                            }
                    ));

            this.plugin.getLogger().info("Randomized " + this.blocksRandomizerMap.size() + " blocks");
        }

        if (force || this.ENTITIES_RANDOMIZED) {
            List<EntityType> copy = new ArrayList<>(this.filteredEntityTypes);

            this.entitiesRandomizerMap = this.filteredEntityTypes.stream()
                    .collect(Collectors.toMap(
                            entityType -> entityType,
                            entityType -> {
                                EntityType randomEntityType = copy.get(this.random.nextInt(copy.size()));
                                copy.remove(randomEntityType);
                                return randomEntityType;
                            }
                    ));

            this.plugin.getLogger().info("Randomized " + this.entitiesRandomizerMap.size() + " entities");
        }

        if (force || this.CRAFTING_RANDOMIZED) {
            List<Material> copy = new ArrayList<>(this.filteredItemList);

            this.craftingRandomizerMap = this.filteredItemList.stream()
                    .collect(Collectors.toMap(
                            material -> material,
                            material -> {
                                Material randomMaterial = copy.get(this.random.nextInt(copy.size()));
                                copy.remove(randomMaterial);
                                return randomMaterial;
                            }
                    ));

            this.plugin.getLogger().info("Randomized " + this.craftingRandomizerMap.size() + " crafting recipes");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(BlockBreakEvent event) {
        if (!this.active) return;
        if (this.plugin.timerHandler.isPaused()) return;

        if (!this.BLOCKS_RANDOMIZED) return;
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;

        Material randomMaterial = this.blocksRandomizerMap.get(event.getBlock().getType());
        if (randomMaterial == null) return;

        event.setDropItems(false);

        event.getBlock().getDrops().forEach(drop -> {
            if (drop.getAmount() < 1) return;
            ItemStack newItem = new ItemStack(randomMaterial, drop.getAmount());
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), newItem);
        });
    }

    private int oceanEntityCount = 0;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(CreatureSpawnEvent event) {
        if (!this.active) return;
        if (this.plugin.timerHandler.isPaused()) return;

        if (!this.ENTITIES_RANDOMIZED) return;

        // Blocks entities that are spawned by plugins
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
            this.oceanEntityCount++;
            if (this.oceanEntityCount % 3 != 0) return;
            this.oceanEntityCount = 0;
        }

        event.getLocation().getWorld().spawnEntity(event.getLocation(), randomEntityType, CreatureSpawnEvent.SpawnReason.CUSTOM);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(CraftItemEvent event) {
        if (!this.active) return;
        if (this.plugin.timerHandler.isPaused()) return;

        if (!this.CRAFTING_RANDOMIZED) return;

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (player.getGameMode() != GameMode.SURVIVAL) return;

        event.setCurrentItem(new ItemStack(this.craftingRandomizerMap.getOrDefault(event.getCurrentItem().getType(), Material.AIR)));
    }
}
