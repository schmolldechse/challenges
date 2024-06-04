package io.github.schmolldechse.challenge.map.challenge.randomizer;

import com.google.inject.Inject;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import io.github.schmolldechse.challenge.Challenge;
import io.github.schmolldechse.challenge.Identification;
import io.github.schmolldechse.challenge.map.challenge.randomizer.modules.RandomBlockDrops;
import io.github.schmolldechse.challenge.map.challenge.randomizer.modules.RandomCrafting;
import io.github.schmolldechse.challenge.map.challenge.randomizer.modules.RandomEntities;
import io.github.schmolldechse.challenge.map.challenge.randomizer.modules.RandomEntityDrops;
import io.github.schmolldechse.challenge.module.Module;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class RandomizerChallenge extends Challenge implements Listener {

    //TODO: remove list in 1.21 again
    public final List<Material> excludedMaterials = List.of(
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

    // TODO: remove BREEZE in 1.21
    public final List<EntityType> excludedEntities = List.of(
            EntityType.DROPPED_ITEM, EntityType.EXPERIENCE_ORB, EntityType.AREA_EFFECT_CLOUD, EntityType.EGG,
            EntityType.LEASH_HITCH, EntityType.PAINTING, EntityType.ARROW, EntityType.SNOWBALL, EntityType.FIREBALL,
            EntityType.SMALL_FIREBALL, EntityType.ENDER_PEARL, EntityType.ENDER_SIGNAL, EntityType.SPLASH_POTION,
            EntityType.THROWN_EXP_BOTTLE, EntityType.ITEM_FRAME, EntityType.WITHER_SKULL, EntityType.PRIMED_TNT,
            EntityType.FALLING_BLOCK, EntityType.FIREWORK, EntityType.SPECTRAL_ARROW, EntityType.DRAGON_FIREBALL,
            EntityType.EVOKER_FANGS, EntityType.LLAMA_SPIT, EntityType.GLOW_ITEM_FRAME, EntityType.MARKER,
            EntityType.BLOCK_DISPLAY, EntityType.INTERACTION, EntityType.ITEM_DISPLAY, EntityType.TEXT_DISPLAY,
            EntityType.BREEZE, EntityType.WIND_CHARGE, EntityType.FISHING_HOOK, EntityType.LIGHTNING,
            EntityType.PLAYER, EntityType.UNKNOWN, EntityType.MINECART, EntityType.MINECART_CHEST,
            EntityType.MINECART_FURNACE, EntityType.MINECART_TNT, EntityType.MINECART_HOPPER, EntityType.MINECART_MOB_SPAWNER
    );

    private final RandomizerInventory randomizerInventory;

    @Inject
    public RandomizerChallenge() {
        super("challenge_randomizer");

        this.moduleRegistry.register(new RandomBlockDrops(this));
        this.moduleRegistry.register(new RandomCrafting(this));
        this.moduleRegistry.register(new RandomEntities(this));
        this.moduleRegistry.register(new RandomEntityDrops(this));

        this.randomizerInventory = new RandomizerInventory(this);
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

        this.moduleRegistry.getModules().stream()
                .filter(Module::isActive)
                .forEach(module -> data.put(module.getIdentifierName(), module.save()));

        return data;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void append(Map<String, Object> data) {
        data.forEach((identifierName, value) -> {
            Module<? extends Challenge> module = this.moduleRegistry.module(identifierName);
            if (module == null)
                throw new IllegalArgumentException("Module with identifier " + identifierName + " does not exist");

            module.append((Map<String, Object>) value);
        });
    }
}
