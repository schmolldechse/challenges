package io.github.schmolldechse.challenge.map.challenge.forcebattle;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.Nullable;

public class ForcebattleTask implements Cloneable {

    /**
     * IMPORTANT:
     * <p>
     * Advancement does not implement the "Translatable" interface, so the key will be the following
     * e.g. "minecraft:story/mine_stone" -> "advancements.story.mine_stone.title"
     * <p>
     * To get the spawnegg of an entity, the key will be the following
     * e.g. "minecraft:zombie" -> "minecraft:zombie_spawn_egg"
     */

    private final NamespacedKey key;
    private final TaskType taskType;

    @Nullable
    private FinishedData finishedData;

    public ForcebattleTask(NamespacedKey key, TaskType taskType, @Nullable FinishedData finishedData) {
        this.key = key;
        this.taskType = taskType;
        this.finishedData = finishedData;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public @Nullable FinishedData getFinishedData() {
        return finishedData;
    }

    public void setFinishedData(@Nullable FinishedData finishedData) {
        this.finishedData = finishedData;
    }

    public ItemStack toItemStack() {
        ItemStack itemStack = new ItemStack(Material.BARRIER);
        switch (this.getTaskType()) {
            case ADVANCEMENT -> {
                Advancement advancement = Bukkit.getAdvancement(this.getKey());
                if (advancement != null && advancement.getDisplay() != null)
                    itemStack = advancement.getDisplay().icon();
            }
            case ENTITY -> {
                Material spawnEgg = Bukkit.getItemFactory().getSpawnEgg(RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE).get(this.getKey()));
                if (spawnEgg != null) itemStack = new ItemStack(spawnEgg);
            }
            case ITEM -> {
                ItemType item = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).get(this.getKey());
                if (item != null) itemStack = item.createItemStack();
            }
        }
        return itemStack;
    }

    public String translatable() {
        String translatableKey = "";
        switch (this.getTaskType()) {
            case ADVANCEMENT -> translatableKey = "advancements." + this.getKey().getKey().replace("/", ".") + ".title";
            case ENTITY -> translatableKey = "entity.minecraft." + this.getKey().getKey();
            case ITEM -> {
                Material material = Material.matchMaterial(this.getKey().getKey());
                if (material.isBlock()) translatableKey = "block.minecraft." + this.getKey().getKey();
                else if (material.isItem()) translatableKey = "item.minecraft." + this.getKey().getKey();
                else translatableKey = "NaN";
            }
            default -> translatableKey = "NaN";
        }
        return translatableKey;
    }

    public Component component() {
        Component component = Component.empty().append(Component.translatable(this.translatable(), NamedTextColor.GOLD));

        if (this.getTaskType() == ForcebattleTask.TaskType.ADVANCEMENT)
            component = component.hoverEvent(HoverEvent.showText(Component.translatable(this.translatable().replace(".title", ".description"))));

        if (this.getTaskType() == TaskType.ITEM) {
            if (this.isDisc(Material.matchMaterial(this.getKey().getKey())) || this.isBannerPattern(Material.matchMaterial(this.getKey().getKey())))
                component = component.append(Component.empty())
                        .append(Component.text("(", NamedTextColor.GOLD))
                        .append(Component.translatable(this.translatable() + ".desc", NamedTextColor.GOLD))
                        .append(Component.text(")", NamedTextColor.GOLD));
            else if (this.isTemplate(Material.matchMaterial(this.getKey().getKey())))
                component = component.append(Component.empty())
                        .append(Component.text("(", NamedTextColor.GOLD))
                        .append(Component.text(this.templateName(Material.matchMaterial(this.getKey().getKey())), NamedTextColor.GOLD))
                        .append(Component.text(")", NamedTextColor.GOLD));
        }

        return component;
    }

    private boolean isDisc(Material material) {
        return material.name().contains("DISC");
    }

    private boolean isTemplate(Material material) {
        return material.name().endsWith("TEMPLATE");
    }

    private boolean isBannerPattern(Material material) {
        return material.name().endsWith("BANNER_PATTERN");
    }

    private String templateName(Material material) {
        String name;
        switch (material) {
            case NETHERITE_UPGRADE_SMITHING_TEMPLATE -> name =  "Netherite Upgrade";
            case SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE -> name = "Sentry Armor Trim";
            case DUNE_ARMOR_TRIM_SMITHING_TEMPLATE -> name = "Dune Armor Trim";
            case COAST_ARMOR_TRIM_SMITHING_TEMPLATE -> name = "Coast Armor Trim";
            case WILD_ARMOR_TRIM_SMITHING_TEMPLATE -> name = "Wild Armor Trim";
            case WARD_ARMOR_TRIM_SMITHING_TEMPLATE -> name = "Ward Armor Trim";
            case EYE_ARMOR_TRIM_SMITHING_TEMPLATE -> name = "Eye Armor Trim";
            case VEX_ARMOR_TRIM_SMITHING_TEMPLATE -> name = "Vex Armor Trim";
            case TIDE_ARMOR_TRIM_SMITHING_TEMPLATE -> name = "Tide Armor Trim";
            case SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE -> name = "Snout Armor Trim";
            case RIB_ARMOR_TRIM_SMITHING_TEMPLATE -> name = "Rib Armor Trim";
            case SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE -> name = "Spire Armor Trim";
            case WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE -> name = "Wayfinder Armor Trim";
            case SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE -> name = "Shaper Armor Trim";
            case SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE -> name = "Silence Armor Trim";
            case RAISER_ARMOR_TRIM_SMITHING_TEMPLATE -> name = "Raiser Armor Trim";
            case HOST_ARMOR_TRIM_SMITHING_TEMPLATE -> name = "Host Armor Trim";
            case FLOW_ARMOR_TRIM_SMITHING_TEMPLATE -> name = "Flow Armor Trim";
            case BOLT_ARMOR_TRIM_SMITHING_TEMPLATE -> name = "Bolt Armor Trim";
            default -> name = "NaN";
        }
        return name;
    }

    @Override
    public String toString() {
        return "ForcebattleTask{" +
                "key=" + this.key +
                ", taskType=" + this.taskType +
                ", finishedData=" + this.finishedData +
                '}';
    }

    @Override
    public ForcebattleTask clone() {
        try {
            ForcebattleTask cloned = (ForcebattleTask) super.clone();
            if (this.finishedData != null)
                cloned.finishedData = new FinishedData(
                        this.finishedData.skipped,
                        this.finishedData.finishedAfter,
                        this.finishedData.finishedFrom
                );

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public static class FinishedData implements Cloneable {

        private boolean skipped;
        private int finishedAfter;
        private String finishedFrom;

        public FinishedData(boolean skipped, int finishedAfter, String finishedFrom) {
            this.skipped = skipped;
            this.finishedAfter = finishedAfter;
            this.finishedFrom = finishedFrom;
        }

        public boolean isSkipped() {
            return skipped;
        }

        public void setSkipped(boolean skipped) {
            this.skipped = skipped;
        }

        public int getFinishedAfter() {
            return finishedAfter;
        }

        public void setFinishedAfter(int finishedAfter) {
            this.finishedAfter = finishedAfter;
        }

        public String getFinishedFrom() {
            return finishedFrom;
        }

        public void setFinishedFrom(String finishedFrom) {
            this.finishedFrom = finishedFrom;
        }

        @Override
        public FinishedData clone() {
            return new FinishedData(this.skipped, this.finishedAfter, this.finishedFrom);
        }
    }

    public enum TaskType {
        ADVANCEMENT,
        ENTITY,
        ITEM
    }
}
