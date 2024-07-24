package io.github.schmolldechse.challenge.map.challenge.forcebattle;

import com.google.gson.JsonArray;
import io.github.schmolldechse.challenge.Challenge;
import io.github.schmolldechse.challenge.Identification;
import io.github.schmolldechse.challenge.map.challenge.forcebattle.modules.ForcebattleAdvancements;
import io.github.schmolldechse.challenge.map.challenge.forcebattle.modules.ForcebattleEntities;
import io.github.schmolldechse.challenge.map.challenge.forcebattle.modules.ForcebattleItems;
import io.github.schmolldechse.challenge.map.challenge.forcebattle.team.ForcebattleExtension;
import io.github.schmolldechse.challenge.module.Module;
import io.github.schmolldechse.config.document.Document;
import io.github.schmolldechse.misc.item.ItemBuilder;
import io.github.schmolldechse.team.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ForcebattleChallenge extends Challenge {

    private final Random random = new Random();

    private final ForcebattleInventory forcebattleInventory;

    public final List<ForcebattleTask> tasks = new ArrayList<>();

    public int SKIPS_PER_TEAM = 3;
    private final ItemStack skipItem = ItemBuilder.from(Material.BARRIER)
            .name(Component.text("Skip", NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, true))
            .persistentDataContainer(persistentDataContainer -> persistentDataContainer.set(this.key, PersistentDataType.STRING, "skip"))
            .build();

    private ScheduledExecutorService timerService;
    // used for gradient
    private double offset = 0.0D;

    private final NamespacedKey itemDisplayKey = new NamespacedKey("challenge", "forcebattle_itemdisplay");

    public ForcebattleChallenge() {
        super("challenge_forcebattle");

        this.moduleRegistry.register(new ForcebattleAdvancements(this));
        this.moduleRegistry.register(new ForcebattleEntities(this));
        this.moduleRegistry.register(new ForcebattleItems(this));

        this.forcebattleInventory = new ForcebattleInventory(this);
    }

    @Override
    public Identification challengeIdentification() {
        return Identification.CHALLENGE;
    }

    @Override
    public ItemStack getItemStack() {
        return ItemBuilder.from(Material.DIAMOND_SWORD)
                .name(this.getDisplayName())
                .lore(this.getDescription())
                .persistentDataContainer(persistentDataContainer -> persistentDataContainer.set(this.key, PersistentDataType.STRING, this.getIdentifierName()))
                .build();
    }

    @Override
    public Component getDisplayName() {
        return Component.text("Force Battle", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public List<Component> getDescription() {
        return Arrays.asList(
                Component.empty(),
                Component.text("Jage allerlei Items, Achievments oder Tiere", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                this.activationComponent()
        );
    }

    @Override
    public void openSettings(Player player) {
        this.forcebattleInventory.getInventory().open(player);
    }

    @Override
    public void onActivate() {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        Bukkit.broadcast(Component.text("(!) Make sure to add tasks, teams and set the timers time in order to start " + this.getIdentifierName() + "!", NamedTextColor.YELLOW));
    }

    @Override
    public void onDeactivate() {
        PlayerInteractEvent.getHandlerList().unregister(this);
        PlayerJoinEvent.getHandlerList().unregister(this);
        PlayerQuitEvent.getHandlerList().unregister(this);
        PlayerDeathEvent.getHandlerList().unregister(this);
        PlayerRespawnEvent.getHandlerList().unregister(this);

        this.plugin.teamHandler.getRegisteredTeams().forEach(team -> {
            ForcebattleExtension extension = team.getExtension(ForcebattleExtension.class).orElse(null);
            if (extension == null) return;

            team.getUuids().forEach(uuid -> {
                Player player = this.plugin.getServer().getPlayer(uuid);
                if (player == null) return;

                player.hideBossBar(extension.getBossBar());
                this.removeItemDisplay(team);
            });
        });

        if (this.timerService != null) this.timerService.shutdownNow();

        super.onDeactivate();
    }

    @Override
    public void onResume() {
        if (this.plugin.teamHandler.getRegisteredTeams().isEmpty()) {
            Bukkit.broadcast(Component.text("(!) Due to missing teams, " + this.getIdentifierName() + " will be deactivated. Make sure to create teams in order to start the challenge!", NamedTextColor.RED));
            this.setActive(false);
            this.onDeactivate();
            return;
        }

        this.skipItem.setAmount(this.SKIPS_PER_TEAM);
        this.plugin.timerHandler.setReverse(true);

        this.plugin.teamHandler.getRegisteredTeams().forEach(team -> {
            Player leader = Bukkit.getPlayer(team.getUuids().getFirst());
            if (team.getExtension(ForcebattleExtension.class).isPresent()) {
                team.sendMessage(Component.text("Nächste Aufgabe: ", NamedTextColor.GRAY)
                    .append(team.getExtension(ForcebattleExtension.class).get().getCurrentTask().component())
                );

                if (leader != null)
                    this.plugin.scoreboardHandler.scoreboardTeam(leader)
                            .suffix(Component.text(" [", NamedTextColor.GRAY)
                                    .append(team.getExtension(ForcebattleExtension.class).get().getCurrentTask().component())
                                    .append(Component.text("]", NamedTextColor.GRAY))
                            );
                return;
            }

            ForcebattleExtension extension = new ForcebattleExtension(
                    List.of(),
                    this.tasks.get(this.random.nextInt(this.tasks.size())),
                    this.SKIPS_PER_TEAM
            );
            team.addExtension(ForcebattleExtension.class, extension);

            if (leader != null) {
                this.plugin.scoreboardHandler.scoreboardTeam(leader)
                        .suffix(Component.text("[", NamedTextColor.GRAY)
                                .append(extension.getCurrentTask().component())
                                .append(Component.text("]", NamedTextColor.GRAY))
                        );
                leader.getInventory().addItem(this.skipItem);
            }

            team.getUuids().forEach(uuid -> {
                Player player = this.plugin.getServer().getPlayer(uuid);
                if (player == null) return;

                player.showBossBar(extension.getBossBar());

                if (player.getPersistentDataContainer().has(this.itemDisplayKey)) return;
                this.spawnItemDisplay(player, extension);
            });

            team.sendMessage(Component.text("Nächste Aufgabe: ", NamedTextColor.GRAY)
                    .append(extension.getCurrentTask().component())
            );
        });
        this.plugin.scoreboardHandler.playerList();

        if (this.timerService == null || this.timerService.isShutdown())
            this.timerService = Executors.newSingleThreadScheduledExecutor();
        this.timerService.scheduleAtFixedRate(() -> {
            offset += 0.05D;
            if (offset > 1.0) offset -= 2.0D;

            this.plugin.teamHandler.getRegisteredTeams().forEach(team -> {
                ForcebattleExtension extension = team.getExtension(ForcebattleExtension.class).orElse(null);
                if (extension == null) return;

                String text = extension.getCurrentTask() == null ? "Aufgaben erledigt" : "<lang:" + extension.getCurrentTask().translatable() + ">";

                @NotNull Component display = MiniMessage.miniMessage().deserialize("<gradient:#707CF4:#F658CF:" + offset + "><b>" + text);
                extension.getBossBar().name(display);
            });
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public Document save() {
        Document document = new Document();

        this.moduleRegistry.getModules().stream()
                .filter(Module::isActive)
                .forEach(module -> document.append(module.getIdentifierName(), module.save()));

        document.append("tasks", this.tasks);

        document.append("SKIPS_PER_TEAM", this.SKIPS_PER_TEAM);

        return document;
    }

    @Override
    public void append(Document document) {
        document.keys().forEach(key -> {
            Module<? extends Challenge> module = this.moduleRegistry.module(key);
            if (module == null) return;

            module.setActive(true);
            module.activate();
            module.append(document.getDocument(key));
        });

        if (document.contains("tasks")) {
            JsonArray array = document.getArray("tasks");
            array.forEach(element -> {
                ForcebattleTask task = this.plugin.GSON.fromJson(element, ForcebattleTask.class);
                this.tasks.add(task);
            });
        }

        if (document.contains("SKIPS_PER_TEAM")) this.SKIPS_PER_TEAM = document.getInt("SKIPS_PER_TEAM");
    }

    public void nextTask(Team team, ForcebattleTask currentTask) {
        Player leader = Bukkit.getPlayer(team.getUuids().getFirst());

        ForcebattleExtension extension = team.getExtension(ForcebattleExtension.class).orElse(null);
        if (extension == null) {
            team.sendMessage(Component.text("(!) Your team is not registered for the forcebattle challenge!", NamedTextColor.RED));
            return;
        }

        List<ForcebattleTask> completedTaks = extension.getCompletedTasks();
        completedTaks.add(currentTask);
        extension.setCompletedTasks(completedTaks);

        // get a next task for the team
        ForcebattleTask nextTask = this.task(true, team);
        if (nextTask == null) {
            extension.setCurrentTask(null);

            Component component = Component.text("Dein Team hat alle Aufgaben erledigt!", NamedTextColor.YELLOW);
            team.sendMessage(component);

            this.removeItemDisplay(team);

            if (leader != null) {
                this.plugin.scoreboardHandler.scoreboardTeam(leader).suffix(Component.empty());
                this.plugin.scoreboardHandler.playerList();
            }

            this.plugin.getLogger().warning("No more tasks available for team " + team.getName());
            return;
        }

        extension.setCurrentTask(nextTask);
        this.updateItemDisplay(team);

        Component text = Component.text("Aufgabe ", NamedTextColor.GRAY)
                .append(currentTask.component())
                .append(Component.text(" geschafft", NamedTextColor.GREEN))
                .appendNewline()
                .append(Component.text("Nächste Aufgabe: ", NamedTextColor.GRAY))
                .append(nextTask.component());
        team.sendMessage(text);

        this.plugin.scoreboardHandler.scoreboardTeam(leader)
                .suffix(Component.text(" [", NamedTextColor.GRAY)
                        .append(nextTask.component())
                        .append(Component.text("]", NamedTextColor.GRAY))
                );

        this.moduleRegistry.getModules().stream()
                .filter(Module::isActive)
                .forEach(module -> ((ForcebattleModule) module).onTaskComplete(team, nextTask));
    }

    private ForcebattleTask task(boolean shuffle, Team team) {
        ForcebattleExtension extension = team.getExtension(ForcebattleExtension.class).orElse(null);
        if (extension == null) {
            team.sendMessage(Component.text("(!) Your team is not registered for the forcebattle challenge", NamedTextColor.RED));
            return null;
        }

        List<NamespacedKey> completedTaskKeys = extension.getCompletedTasks().stream()
                .map(ForcebattleTask::getKey)
                .toList();

        List<ForcebattleTask> availableTasks = this.tasks.stream()
                .filter(task -> !completedTaskKeys.contains(task.getKey()))
                .toList();

        if (availableTasks.isEmpty()) return null;

        if (shuffle) return availableTasks.get(this.random.nextInt(availableTasks.size())).clone();
        else return availableTasks.getFirst().clone();
    }

    @EventHandler
    public void execute(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.removeItemDisplay(player);
    }

    @EventHandler
    public void execute(PlayerInteractEvent event) {
        if (!this.active) return;
        if (this.plugin.timerHandler.isPaused()) return;

        ItemStack itemStack = event.getItem();
        if (itemStack == null) return;
        if (!itemStack.getItemMeta().getPersistentDataContainer().has(this.key)) return;

        String identifier = itemStack.getItemMeta().getPersistentDataContainer().get(this.key, PersistentDataType.STRING);
        if (identifier == null || !identifier.equalsIgnoreCase("skip")) return;

        Team team = this.plugin.teamHandler.team(event.getPlayer());
        if (team == null) return;

        ForcebattleExtension extension = team.getExtension(ForcebattleExtension.class).orElse(null);
        if (extension == null) {
            event.getPlayer().sendMessage(Component.text("(!) Your team is not registered for the forcebattle challenge!", NamedTextColor.RED));
            return;
        }

        ForcebattleTask currentTask = extension.getCurrentTask();
        if (currentTask == null) return;

        event.getPlayer().getInventory().addItem(extension.getCurrentTask().toItemStack());

        currentTask.setFinishedData(new ForcebattleTask.FinishedData(
                true,
                this.plugin.timerHandler.elapsed,
                event.getPlayer().getName()
        ));
        this.nextTask(team, currentTask);

        itemStack.setAmount(itemStack.getAmount() - 1);
        event.setUseInteractedBlock(Event.Result.DENY);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void execute(PlayerJoinEvent event) {
        if (this.plugin.timerHandler.isPaused()) return;

        Player player = event.getPlayer();

        if (!this.plugin.teamHandler.inTeam(player)) return;
        Team team = this.plugin.teamHandler.team(player);

        Optional<ForcebattleExtension> extension = team.getExtension(ForcebattleExtension.class);
        if (extension.isEmpty()) return;
        player.showBossBar(extension.get().getBossBar());

        if (player.getPersistentDataContainer().has(this.itemDisplayKey)) return;
        this.spawnItemDisplay(player, extension.get());
    }

    @EventHandler
    public void execute(PlayerDeathEvent event) {
        if (this.plugin.timerHandler.isPaused()) return;

        Player player = event.getEntity();

        if (!this.plugin.teamHandler.inTeam(player)) return;
        Team team = this.plugin.teamHandler.team(player);

        Optional<ForcebattleExtension> extension = team.getExtension(ForcebattleExtension.class);
        if (extension.isEmpty()) return;

        this.removeItemDisplay(player);
    }

    @EventHandler
    public void execute(PlayerRespawnEvent event) {
        if (this.plugin.timerHandler.isPaused()) return;

        Player player = event.getPlayer();

        if (!this.plugin.teamHandler.inTeam(player)) return;
        Team team = this.plugin.teamHandler.team(player);

        Optional<ForcebattleExtension> extension = team.getExtension(ForcebattleExtension.class);
        if (extension.isEmpty()) return;

        if (player.getPersistentDataContainer().has(this.itemDisplayKey)) return;
        this.spawnItemDisplay(player, extension.get());
    }

    private void spawnItemDisplay(Player player, ForcebattleExtension extension) {
        Location clone = player.getLocation();
        clone.setYaw(0f);
        clone.setPitch(0f);

        ArmorStand armorStand = player.getWorld().spawn(clone, ArmorStand.class, stand -> {
            stand.setInvisible(true);
            stand.setBasePlate(false);
            stand.setGravity(false);
            stand.setInvulnerable(true);
            stand.setCollidable(false);
        });
        player.addPassenger(armorStand);

        player.getWorld().spawn(clone, ItemDisplay.class, itemDisplay -> {
            Transformation transformation = itemDisplay.getTransformation();
            transformation.getScale().set(.5f);
            itemDisplay.setTransformation(transformation);

            itemDisplay.setViewRange(0.2f);

            itemDisplay.setItemStack(extension.getCurrentTask().toItemStack());
            itemDisplay.setInvulnerable(true);
            itemDisplay.setGravity(false);
            //itemDisplay.setBillboard(Display.Billboard.CENTER);

            armorStand.addPassenger(itemDisplay);
        });

        player.getPersistentDataContainer().set(this.itemDisplayKey, PersistentDataType.INTEGER, armorStand.getEntityId());
    }

    private void updateItemDisplay(Team team) {
        ForcebattleExtension extension = team.getExtension(ForcebattleExtension.class).orElse(null);
        if (extension == null) return;

        team.getUuids().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;

            if (!player.getPersistentDataContainer().has(this.itemDisplayKey)) return;
            int entityId = player.getPersistentDataContainer().get(this.itemDisplayKey, PersistentDataType.INTEGER);

            player.getWorld().getEntitiesByClass(ArmorStand.class).stream()
                    .filter(armorStand -> armorStand.getEntityId() == entityId)
                    .findFirst()
                    .ifPresent(armorStand -> {
                        if (armorStand.getPassengers().getFirst().getType() != EntityType.ITEM_DISPLAY) return;
                        ItemDisplay itemDisplay = (ItemDisplay) armorStand.getPassengers().getFirst();
                        itemDisplay.setItemStack(extension.getCurrentTask().toItemStack());
                    });
        });
    }

    private void removeItemDisplay(Player player) {
        if (!player.getPersistentDataContainer().has(this.itemDisplayKey)) return;
        int entityId = player.getPersistentDataContainer().get(this.itemDisplayKey, PersistentDataType.INTEGER);

        player.getWorld().getEntitiesByClass(ArmorStand.class).stream()
                .filter(armorStand -> armorStand.getEntityId() == entityId)
                .findFirst()
                .ifPresent(entity -> {
                    entity.getPassengers().forEach(Entity::remove);
                    entity.remove();
                });
        player.getPersistentDataContainer().remove(this.itemDisplayKey);
    }

    private void removeItemDisplay(Team team) {
        ForcebattleExtension extension = team.getExtension(ForcebattleExtension.class).orElse(null);
        if (extension == null) return;

        team.getUuids().forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;

            this.removeItemDisplay(player);
        });
    }
}
