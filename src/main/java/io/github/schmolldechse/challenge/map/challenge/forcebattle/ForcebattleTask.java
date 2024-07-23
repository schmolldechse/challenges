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
                if (Material.matchMaterial(this.getKey().getKey()).isBlock())
                    translatableKey = "block.minecraft." + this.getKey().getKey();
                else if (Material.matchMaterial(this.getKey().getKey()).isItem())
                    translatableKey = "item.minecraft." + this.getKey().getKey();
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

        return component;
    }

    @Override
    public String toString() {
        return "ForcebattleTask{" +
                "key=" + key +
                ", taskType=" + taskType +
                ", finishedData=" + finishedData +
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
