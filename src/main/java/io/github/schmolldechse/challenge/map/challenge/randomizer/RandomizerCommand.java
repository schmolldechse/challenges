package io.github.schmolldechse.challenge.map.challenge.randomizer;

import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RandomizerCommand {

    // /randomizer BLOCK <Material> :: Outputs which material the given block will be replaced with
    // /randomizer ENTITY <EntityType> :: Outputs which entity type the given entity will be replaced with
    // /randomizer CRAFTING <Material> :: Outputs which material the given crafting recipe will be replaced with

    private final RandomizerChallenge challenge;

    public RandomizerCommand(RandomizerChallenge challenge) {
        this.challenge = challenge;
    }

    public void registerCommand() {
        new CommandTree("randomizer")
                .then(new LiteralArgument("block")
                        .then(new BlockStateArgument("block")
                                .executes((sender, args) -> {
                                    BlockData blockData = (BlockData) args.get("block");
                                    if (blockData == null) {
                                        sender.sendMessage(Component.text("(!) " + args.get("block") + " does not exist in minecraft", NamedTextColor.RED));
                                        return;
                                    }
                                    if (!blockData.getMaterial().isBlock()) {
                                        sender.sendMessage(Component.text("(!) " + blockData.getMaterial() + " is not a block", NamedTextColor.RED));
                                        return;
                                    }

                                    if (!this.challenge.blocksRandomizerMap.containsValue(blockData.getMaterial())) {
                                        sender.sendMessage(Component.text("(!) " + blockData.getMaterial() + " will not be replaced", NamedTextColor.RED));
                                        return;
                                    }

                                    sender.sendMessage("Block " + blockData.getMaterial().name() + " will be replaced with " + this.challenge.blocksRandomizerMap.get(blockData.getMaterial()));
                                })))
                .then(new LiteralArgument("entity")
                        .then(new EntityTypeArgument("entity")
                                .executes((sender, args) -> {
                                    EntityType entityType = (EntityType) args.get("entity");
                                    if (entityType == null) {
                                        sender.sendMessage(Component.text("(!) " + args.get("entity") + " does not exist in minecraft", NamedTextColor.RED));
                                        return;
                                    }
                                    if (!this.challenge.entitiesRandomizerMap.containsValue(entityType)) {
                                        sender.sendMessage(Component.text("(!) " + entityType + " will not be replaced", NamedTextColor.RED));
                                        return;
                                    }

                                    sender.sendMessage("Entity " + entityType.name() + " will be replaced with " + this.challenge.entitiesRandomizerMap.get(entityType));
                                })))
                .then(new LiteralArgument("crafting")
                        .then(new ItemStackPredicateArgument("item")
                                .executes((sender, args) -> {
                                    @SuppressWarnings("unchecked")
                                    Predicate<ItemStack> predicate = (Predicate<ItemStack>) args.get("item");

                                    Optional<Material> matched = Stream.of(Material.values())
                                            .filter(material -> predicate.test(new ItemStack(material)))
                                            .filter(Material::isItem)
                                            .findFirst();

                                    if (matched.isEmpty()) {
                                        sender.sendMessage(Component.text("(!) " + args.get("item") + " does not exist in minecraft", NamedTextColor.RED));
                                        return;
                                    }

                                    Material original = matched.get();
                                    if (!this.challenge.craftingRandomizerMap.containsKey(original)) {
                                        sender.sendMessage(Component.text("(!) Crafting recipe for " + original.name() + " will not be replaced", NamedTextColor.RED));
                                        return;
                                    }

                                    Material randomized = this.challenge.craftingRandomizerMap.get(original);
                                    sender.sendMessage(Component.text("Crafting recipe for " + original.name() + " will be replaced with " + randomized));
                                })))
                .register();
    }
}
