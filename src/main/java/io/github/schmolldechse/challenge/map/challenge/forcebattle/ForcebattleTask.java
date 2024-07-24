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

    private @Nullable FinishedData finishedData;

    /**
     * Constructs a new ForcebattleTask
     *
     * @param key               The key representing the task
     * @param taskType          The type of the task
     * @param finishedData      {@link FinishedData} representing the finished state of the task
     */
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
        return switch (this.getTaskType()) {
            case ADVANCEMENT -> "advancements." + this.getKey().getKey().replace("/", ".") + ".title";
            case ENTITY -> "entity.minecraft." + this.getKey().getKey();
            case ITEM -> {
                Material material = Material.matchMaterial(this.getKey().getKey());
                if (material.isBlock()) yield "block.minecraft." + this.getKey().getKey();
                else if (material.isItem()) yield "item.minecraft." + this.getKey().getKey();
                else yield "NaN";
            }
            default -> "NaN";
        };
    }

    public Component component() {
        Component component = Component.empty().append(Component.translatable(this.translatable(), NamedTextColor.GOLD));

        if (this.getTaskType() == ForcebattleTask.TaskType.ADVANCEMENT)
            component = component.hoverEvent(HoverEvent.showText(Component.translatable(this.translatable().replace(".title", ".description"))));

        if (this.getTaskType() == TaskType.ITEM) {
            if (this.isDisc() || this.isBannerPattern())
                component = component.append(Component.space())
                        .append(Component.text("(", NamedTextColor.GOLD))
                        .append(Component.translatable(this.translatable() + ".desc", NamedTextColor.GOLD))
                        .append(Component.text(")", NamedTextColor.GOLD));
            else if (this.isTemplate())
                component = component.append(Component.space())
                        .append(Component.text("(", NamedTextColor.GOLD))
                        .append(Component.text(this.templateName(), NamedTextColor.GOLD))
                        .append(Component.text(")", NamedTextColor.GOLD));
        }

        return component;
    }

    public boolean isDisc() {
        return Material.matchMaterial(this.getKey().getKey()).name().contains("DISC");
    }

    public boolean isTemplate() {
        return Material.matchMaterial(this.getKey().getKey()).name().endsWith("TEMPLATE");
    }

    public boolean isBannerPattern() {
        return Material.matchMaterial(this.getKey().getKey()).name().endsWith("BANNER_PATTERN");
    }

    public String templateName() {
        return switch (Material.matchMaterial(this.getKey().getKey())) {
            case NETHERITE_UPGRADE_SMITHING_TEMPLATE -> "Netherite Upgrade";
            case SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE -> "Sentry Armor Trim";
            case DUNE_ARMOR_TRIM_SMITHING_TEMPLATE -> "Dune Armor Trim";
            case COAST_ARMOR_TRIM_SMITHING_TEMPLATE -> "Coast Armor Trim";
            case WILD_ARMOR_TRIM_SMITHING_TEMPLATE -> "Wild Armor Trim";
            case WARD_ARMOR_TRIM_SMITHING_TEMPLATE -> "Ward Armor Trim";
            case EYE_ARMOR_TRIM_SMITHING_TEMPLATE -> "Eye Armor Trim";
            case VEX_ARMOR_TRIM_SMITHING_TEMPLATE -> "Vex Armor Trim";
            case TIDE_ARMOR_TRIM_SMITHING_TEMPLATE -> "Tide Armor Trim";
            case SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE -> "Snout Armor Trim";
            case RIB_ARMOR_TRIM_SMITHING_TEMPLATE -> "Rib Armor Trim";
            case SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE -> "Spire Armor Trim";
            case WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE -> "Wayfinder Armor Trim";
            case SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE -> "Shaper Armor Trim";
            case SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE -> "Silence Armor Trim";
            case RAISER_ARMOR_TRIM_SMITHING_TEMPLATE -> "Raiser Armor Trim";
            case HOST_ARMOR_TRIM_SMITHING_TEMPLATE -> "Host Armor Trim";
            case FLOW_ARMOR_TRIM_SMITHING_TEMPLATE -> "Flow Armor Trim";
            case BOLT_ARMOR_TRIM_SMITHING_TEMPLATE -> "Bolt Armor Trim";
            default -> "NaN";
        };
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
            cloned.finishedData = this.finishedData != null ? this.finishedData.clone() : null;
            return cloned;
        } catch (CloneNotSupportedException exception) {
            throw new AssertionError(exception);
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
