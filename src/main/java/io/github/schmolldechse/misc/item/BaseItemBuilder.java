package io.github.schmolldechse.misc.item;

import com.google.common.base.Preconditions;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public abstract class BaseItemBuilder<T extends BaseItemBuilder<T>> {

    private static final EnumSet<Material> LEATHER_ARMOR = EnumSet.of(
            Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS
    );

    private ItemStack itemStack;
    private ItemMeta itemMeta;

    protected BaseItemBuilder(@NotNull final ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "ItemStack cannot be null");

        this.itemStack = itemStack;
        this.itemMeta = this.itemStack.hasItemMeta() ? this.itemStack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(this.itemStack.getType());
    }

    /**
     * Sets the display name of the {@link ItemStack} using {@link Component}
     *
     * @param name {@link Component} name of the {@link ItemStack}
     * @return {@link T}
     */
    @NotNull
    public T name(@NotNull final Component name) {
        if (this.itemMeta == null) return (T) this;

        this.itemMeta.displayName(name);
        return (T) this;
    }

    /**
     * Sets the amount of the {@link ItemStack}
     *
     * @param amount amount of the {@link ItemStack}
     */
    @NotNull
    public T amount(final int amount) {
        this.itemStack.setAmount(amount);
        return (T) this;
    }

    /**
     * Sets the lore of the {@link ItemStack}
     *
     * @param lore {@link Component} lore of the {@link ItemStack}
     * @return {@link T}
     */
    @NotNull
    public T lore(@Nullable final Component @NotNull ... lore) {
        return this.lore(Arrays.asList(lore));
    }

    /**
     * Sets the lore of the {@link ItemStack}
     *
     * @param lore {@link List} of {@link Component} lore of the {@link ItemStack}
     * @return {@link T}
     */
    @NotNull
    public T lore(@NotNull final List<@Nullable Component> lore) {
        if (this.itemMeta == null) return (T) this;

        this.itemMeta.lore(lore);
        return (T) this;
    }

    @NotNull
    public T lore(@NotNull final Consumer<List<@Nullable Component>> consumer) {
        List<Component> lore = new ArrayList<>();
        consumer.accept(lore);
        return this.lore(lore);
    }

    /**
     * Enchants the {@link ItemStack} with the specified {@link Enchantment}
     *
     * @param enchantment {@link Enchantment} to enchant the {@link ItemStack} with
     * @return {@link T}
     */
    @NotNull
    public T enchant(@NotNull final Enchantment enchantment) {
        return this.enchant(enchantment, 1, true);
    }

    /**
     * Enchants the {@link ItemStack} with the specified {@link Enchantment} and level
     *
     * @param enchantment {@link Enchantment} to enchant the {@link ItemStack} with
     * @param level level of the enchantment
     * @return {@link T}
     */
    @NotNull
    public T enchant(@NotNull final Enchantment enchantment, final int level) {
        return this.enchant(enchantment, level, true);
    }

    /**
     * Enchants the {@link ItemStack} with the specified {@link Enchantment} and level
     *
     * @param enchantment {@link Enchantment} to enchant the {@link ItemStack} with
     * @param level level of the enchantment
     * @param ignoreRestriction whether to ignore the enchantment level restriction
     * @return {@link T}
     */
    @NotNull
    public T enchant(@NotNull final Enchantment enchantment, final int level, final boolean ignoreRestriction) {
        if (this.itemMeta == null) return (T) this;

        this.itemMeta.addEnchant(enchantment, level, ignoreRestriction);
        return (T) this;
    }

    /**
     * Enchants the {@link ItemStack} with the specified {@link Map} of {@link Enchantment} and level
     *
     * @param enchantments {@link Map} of {@link Enchantment} and level to enchant the {@link ItemStack} with
     * @return {@link T}
     */
    @NotNull
    public T enchant(@NotNull final Map<Enchantment, Integer> enchantments) {
        return this.enchant(enchantments, true);
    }

    /**
     * Enchants the {@link ItemStack} with the specified {@link Map} of {@link Enchantment} and level
     *
     * @param enchantments {@link Map} of {@link Enchantment} and level to enchant the {@link ItemStack} with
     * @param ignoreRestriction whether to ignore the enchantment level restriction
     * @return {@link T}
     */
    @NotNull
    public T enchant(@NotNull final Map<Enchantment, Integer> enchantments, final boolean ignoreRestriction) {
        enchantments.forEach((enchantment, level) -> this.enchant(enchantment, level, ignoreRestriction));
        return (T) this;
    }

    /**
     * Disenchantes a certain {@link Enchantment} from the {@link ItemStack}
     *
     * @param enchantment
     * @return
     */
    @NotNull
    public T disenchant(@NotNull final Enchantment enchantment) {
        if (this.itemMeta == null) return (T) this;

        this.itemMeta.removeEnchant(enchantment);
        return (T) this;
    }

    /**
     * Add an {@link ItemFlag} to the {@link ItemStack}
     *
     * @param flags {@link ItemFlag} to add to the {@link ItemStack}
     * @return {@link T}
     */
    @NotNull
    public T flags(@NotNull final ItemFlag... flags) {
        if (this.itemMeta == null) return (T) this;

        this.itemMeta.addItemFlags(flags);
        return (T) this;
    }

    /**
     * Makes the {@link ItemStack} unbreakable
     * @return {@link T}
     */
    @NotNull
    public T unbreakable() {
        return this.unbreakable(true);
    }

    /**
     * Makes the {@link ItemStack} unbreakable
     *
     * @param unbreakable whether the {@link ItemStack} should be unbreakable
     * @return {@link T}
     */
    @NotNull
    public T unbreakable(final boolean unbreakable) {
        if (this.itemMeta == null) return (T) this;

        this.itemMeta.setUnbreakable(unbreakable);
        return (T) this;
    }

    /**
     * Makes the {@link ItemStack} glow
     * @return {@link T}
     */
    @NotNull
    public T glow() {
        return this.glow(true);
    }

    /**
     * Makes the {@link ItemStack} glow
     *
     * @param glow whether the {@link ItemStack} should glow
     * @return {@link T}
     */
    @NotNull
    public T glow(final boolean glow) {
        if (this.itemMeta == null) return (T) this;

        if (glow) {
            this.itemMeta.addEnchant(Enchantment.LURE, 1, true);
            this.itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            return (T) this;
        }

        this.itemMeta.getEnchants().keySet().forEach(this.itemMeta::removeEnchant);
        return (T) this;
    }

    /**
     * Consumer for applying {@link PersistentDataContainer} to the {@link ItemStack}
     *
     * @param consumer {@link Consumer} with the {@link PersistentDataContainer}
     * @return {@link T}
     */
    @NotNull
    public T persistentDataContainer(@NotNull final Consumer<PersistentDataContainer> consumer) {
        consumer.accept(this.itemMeta.getPersistentDataContainer());
        return (T) this;
    }

    /**
     * Sets the custom model data of the {@link ItemStack}
     *
     * @param modelData custom model data of the {@link ItemStack}
     * @return {@link T}
     */
    @NotNull
    public T model(final int modelData) {
        if (this.itemMeta == null) return (T) this;

        this.itemMeta.setCustomModelData(modelData);
        return (T) this;
    }

    @NotNull
    public T color(@NotNull final Color color) {
        if (!LEATHER_ARMOR.contains(this.itemStack.getType())) return (T) this;

        LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) this.itemMeta;
        leatherArmorMeta.setColor(color);
        this.setItemMeta(leatherArmorMeta);
        return (T) this;
    }

    /**
     * Builds the {@link ItemStack}
     * @return {@link ItemStack}
     */
    @NotNull
    public ItemStack build() {
        this.itemStack.setItemMeta(this.itemMeta);
        return this.itemStack;
    }

    /**
     * Creates a {@link GuiItem} from the {@link ItemStack}
     * @return {@link GuiItem}
     */
    @NotNull
    public GuiItem asGuiItem() {
        return new GuiItem(this.build());
    }

    /**
     * Creates a {@link GuiItem} from the {@link ItemStack} with a {@link GuiAction}
     *
     * @param clickAction {@link GuiAction} to perform when the {@link ItemStack} is clicked
     * @return {@link GuiItem}
     */
    @NotNull
    public GuiItem asGuiItem(@NotNull final GuiAction<InventoryClickEvent> clickAction) {
        return new GuiItem(this.build(), clickAction);
    }

    /**
     * Package private getter for extended builders
     * @return {@link ItemStack}
     */
    @NotNull
    protected ItemStack getItemStack() {
        return this.itemStack;
    }

    /**
     * Package private setter for extended builders
     * @param itemStack {@link ItemStack}
     */
    protected void setItemStack(@NotNull final ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Package private getter for extended builders
     * @return {@link ItemMeta}
     */
    @NotNull
    protected ItemMeta getItemMeta() {
        return this.itemMeta;
    }

    /**
     * Package private setter for extended builders
     * @param itemMeta {@link ItemMeta}
     */
    protected void setItemMeta(@NotNull final ItemMeta itemMeta) {
        this.itemMeta = itemMeta;
    }


}
