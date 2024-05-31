package io.github.schmolldechse.challenge.map.challenge.trafficlight;

import com.google.inject.Inject;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.challenge.Challenge;
import io.github.schmolldechse.challenge.Identification;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrafficLightChallenge extends Challenge {

    private final Plugin plugin;

    public BossBar bossBar;

    public final TrafficLightComponents components = new TrafficLightComponents();
    private TrafficLightTimer timer;
    public TrafficLightStatus status;

    private final TrafficLightInventory settingsInventory;

    @Inject
    public TrafficLightChallenge() {
        super("challenge_trafficlight");

        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.settingsInventory = new TrafficLightInventory(this);
    }

    @Override
    public Identification challengeIdentification() {
        return Identification.CHALLENGE;
    }

    @Override
    public void openSettings(Player player) {
        this.settingsInventory.getInventory().open(player);
    }

    @Override
    public ItemStack getItemStack() {
        Component activated = this.active
                ? Component.text("Aktiviert", NamedTextColor.GREEN)
                : Component.text("Deaktiviert", NamedTextColor.RED);

        return ItemBuilder.from(Material.GREEN_CONCRETE)
                .name(this.getDisplayName())
                .lore(Arrays.asList(
                        Component.empty(),
                        Component.text("Aufgepasst, die Ampel spielt verrückt! Sei achtsam,", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                        Component.text("denn sobald sie auf gelb springt, solltest du", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                        Component.text("lieber stehen bleiben!", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        activated,
                        Component.empty(),
                        Component.text("[Rechtsklick]", NamedTextColor.BLUE).decoration(TextDecoration.ITALIC, true)
                                .append(Component.text(" zum Bearbeiten", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false))
                ))
                .pdc(persistentDataContainer -> persistentDataContainer.set(this.key, PersistentDataType.STRING, this.getIdentifierName()))
                .build();
    }

    @Override
    public Component getDisplayName() {
        return Component.text("Ampel Challenge", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public List<Component> getDescription() {
        return Arrays.asList(
                Component.empty(),
                Component.text("Ziel: ", NamedTextColor.WHITE).append(Component.text("Enderdrachen besiegen", NamedTextColor.YELLOW)).decoration(TextDecoration.ITALIC, true),
                Component.text("Aufgepasst! Grünes Licht bedeutet Bewegung,", NamedTextColor.WHITE),
                Component.text("aber bei Rot ist Stillstand geboten.", NamedTextColor.WHITE)
        );
    }

    @Override
    public void onActivate() {
        this.bossBar = BossBar.bossBar(
                Component.empty(),
                0,
                BossBar.Color.WHITE, // white bossbar removes minecraft's bossbar layout in resource pack
                BossBar.Overlay.PROGRESS
        );

        this.status = TrafficLightStatus.GREEN;

        this.timer = new TrafficLightTimer(this);
        this.timer.remainingTime = this.timer.calculate(
                this.status.minDuration,
                this.status.maxDuration
        );

        this.bossBar.name(this.components.trafficLight);

        Bukkit.getServer().audiences().forEach(audience -> this.bossBar.addViewer(audience));
    }

    @Override
    public void onDeactivate() {
        this.timer.pause(); // shutdown trafficlight timer

        if (this.bossBar == null) return;
        this.bossBar.name(this.components.trafficLight);

        Bukkit.getServer().audiences().forEach(audience -> audience.hideBossBar(this.bossBar));
        this.bossBar = null;
    }

    @Override
    public void onPause() {
        this.timer.pause();
    }

    @Override
    public void onResume() {
        this.timer.resume();
    }

    @Override
    public Map<String, Object> save() {
        Map<String, Object> data = new HashMap<>();

        data.put("status", this.status);
        data.put("remainingTime", this.timer.remainingTime);

        return data;
    }

    @Override
    public void append(Map<String, Object> data) {
        this.status = TrafficLightStatus.valueOf((String) data.get("status"));
        this.timer.remainingTime = ((Number) data.get("remainingTime")).longValue();
    }

    @EventHandler
    public void execute(PlayerJoinEvent event) {
        if (!this.active) return;
        if (this.bossBar == null) return;

        event.getPlayer().showBossBar(this.bossBar);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(EntityDeathEvent event) {
        if (!this.active) return;
        if (this.plugin.timerHandler.isPaused()) return;

        if (!(event.getEntity() instanceof EnderDragon)) return;

        this.success();
        this.timer.pause(); // shutdown trafficlight timer
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(PlayerMoveEvent event) {
        if (!this.active) return;
        if (this.plugin.timerHandler.isPaused()) return;

        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
        if (this.status != TrafficLightStatus.RED) return;

        Location from = event.getFrom();
        Location to = event.getTo();

        double x = Math.floor(from.getX());
        double z = Math.floor(from.getZ());

        if (Math.floor(to.getX()) != x || Math.floor(to.getZ()) != z) {
            Bukkit.broadcast(
                    Component.text(event.getPlayer().getName(), NamedTextColor.GREEN)
                            .append(Component.text(" hat die Ampel nicht beachtet", NamedTextColor.RED))
            );
            this.fail();
            this.timer.pause();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(PlayerDeathEvent event) {
        if (!this.active) return;
        if (this.plugin.timerHandler.isPaused()) return;

        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;

        event.deathMessage(
                Component.text(event.getPlayer().getName(), NamedTextColor.GREEN)
                        .append(Component.text(" ist gestorben!", NamedTextColor.RED))
        );

        this.fail();
        this.timer.pause();
    }
}