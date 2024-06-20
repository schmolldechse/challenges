package io.github.schmolldechse.misc.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemBuilder extends BaseItemBuilder<ItemBuilder> {

    /**
     * Constructor for the ItemBuilder
     * @param itemStack The {@link ItemStack} to build
     */
    ItemBuilder(@NotNull final ItemStack itemStack) {
        super(itemStack);
    }

    /**
     * Create a new ItemBuilder from an {@link ItemStack}
     *
     * @param itemStack The {@link ItemStack} to edit
     * @return The new {@link ItemBuilder}
     */
    @NotNull
    public static ItemBuilder from(@NotNull final ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }

    /**
     * Create a new ItemBuilder from a {@link Material}
     *
     * @param material The {@link Material} to create an item from
     * @return The new {@link ItemBuilder}
     */
    @NotNull
    public static ItemBuilder from(@NotNull final Material material) {
        return new ItemBuilder(new ItemStack(material));
    }
}
