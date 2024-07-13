package io.github.schmolldechse.challenge.map.challenge.forcebattle.team;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.util.GuiFiller;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import io.github.schmolldechse.challenge.map.challenge.forcebattle.ForcebattleTask;
import io.github.schmolldechse.config.document.Document;
import io.github.schmolldechse.team.Extension;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ForcebattleExtension implements Extension {

    private static final Gson GSON = new Gson();

    private List<ForcebattleTask> completedTasks;
    @Nullable
    private ForcebattleTask currentTask;
    private int skipsLeft;
    private final BossBar bossBar;
    private final PaginatedGui paginatedGui;

    public ForcebattleExtension(List<ForcebattleTask> completedTasks, @NotNull ForcebattleTask currentTask, int skipsLeft) {
        this.completedTasks = completedTasks;
        this.currentTask = currentTask;
        this.skipsLeft = skipsLeft;

        this.bossBar = BossBar.bossBar(
                Component.empty(),
                0f,
                BossBar.Color.WHITE,
                BossBar.Overlay.PROGRESS
        );

        this.paginatedGui = Gui.paginated()
                .title(Component.text("Geschaffte Aufgaben", NamedTextColor.DARK_GRAY))
                .rows(6)
                .disableItemDrop()
                .disableItemPlace()
                .disableItemSwap()
                .disableItemTake()
                .create();

        this.paginatedGui.getFiller().fillTop(ItemBuilder.from(Material.WHITE_STAINED_GLASS_PANE).asGuiItem());
        this.paginatedGui.getFiller().fillSide(GuiFiller.Side.BOTH, List.of(
                        ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE).asGuiItem()
                )
        );
    }

    public List<ForcebattleTask> getCompletedTasks() {
        return new ArrayList<>(this.completedTasks);
    }

    public void setCompletedTasks(List<ForcebattleTask> completedTasks) {
        this.completedTasks = completedTasks;
    }

    public @Nullable ForcebattleTask getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(@Nullable ForcebattleTask currentTask) {
        this.currentTask = currentTask;
    }

    public int getSkipsLeft() {
        return skipsLeft;
    }

    public void setSkipsLeft(int skipsLeft) {
        this.skipsLeft = skipsLeft;
    }

    public BossBar getBossBar() {
        return bossBar;
    }

    public PaginatedGui getPaginatedGui() {
        return paginatedGui;
    }

    @Override
    public String name() {
        return "extension_forcebattle";
    }

    @Override
    public Document save() {
        return new Document()
                .append("completedTasks", this.completedTasks)
                .append("currentTask", this.currentTask)
                .append("skipsLeft", this.skipsLeft);
    }

    public static ForcebattleExtension fromDocument(Document document) {
        JsonArray completedTasks = document.getArray("completedTasks");

        List<ForcebattleTask> tasks = new ArrayList<>();
        completedTasks.forEach(taskElement -> {
            ForcebattleTask task = GSON.fromJson(taskElement, ForcebattleTask.class);
            tasks.add(task);
        });

        return new ForcebattleExtension(
                tasks,
                GSON.fromJson(document.getElement("currentTask").getAsJsonObject(), ForcebattleTask.class),
                document.getInt("skipsLeft")
        );
    }

    @Override
    public String toString() {
        return "ForcebattleExtension{" +
                "completedTasks=" + this.completedTasks +
                ", currentTask=" + this.currentTask +
                ", skipsLeft=" + this.skipsLeft +
                '}';
    }
}
