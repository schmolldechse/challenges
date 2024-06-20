package io.github.schmolldechse.challenge.map.challenge.randomizer;

import com.google.inject.Inject;
import io.github.schmolldechse.challenge.Challenge;
import io.github.schmolldechse.challenge.Identification;
import io.github.schmolldechse.challenge.map.challenge.randomizer.modules.*;
import io.github.schmolldechse.challenge.module.Module;
import io.github.schmolldechse.config.document.Document;
import io.github.schmolldechse.misc.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class RandomizerChallenge extends Challenge {

    private final RandomizerInventory randomizerInventory;

    @Inject
    public RandomizerChallenge() {
        super("challenge_randomizer");

        this.moduleRegistry.register(new RandomBlockDrops(this));
        this.moduleRegistry.register(new RandomChestLootTables(this));
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
                .persistentDataContainer(persistentDataContainer -> persistentDataContainer.set(this.key, PersistentDataType.STRING, this.getIdentifierName()))
                .build();
    }

    @Override
    public Component getDisplayName() {
        return Component.text("Randomizer", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public List<Component> getDescription() {
        return Arrays.asList(
                Component.empty(),
                Component.text("Irgendwie kommt hier nicht das richtige raus?", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.text("Hier ist dein Glück gefragt! Abgebaute Blöcke,", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.text("Entities und Craften lassen nun etwas völlig", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.text("erscheinen, als gewohnt", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                this.activationComponent(),
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
    public Document save() {
        Document document = new Document();

        this.moduleRegistry.getModules().stream()
                .filter(Module::isActive)
                .forEach(module -> document.append(module.getIdentifierName(), module.save()));

        return document;
    }

    @Override
    public void append(Document document) {
        document.keys().forEach(identifierName -> {
            Module<? extends Challenge> module = this.moduleRegistry.module(identifierName);
            if (module == null)
                throw new IllegalArgumentException("Module with identifier " + identifierName + " does not exist");

            module.setActive(true);
            module.activate();
            module.append(document.getDocument(identifierName));
        });
    }
}
