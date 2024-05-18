package io.github.schmolldechse.challenge.map.trafficlight;

import com.google.inject.Inject;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.challenge.Challenge;
import io.github.schmolldechse.challenge.ChallengeHandler;
import io.github.schmolldechse.timer.TimerHandler;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.EnderDragon;
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

    public TrafficLightStatus status = TrafficLightStatus.DISABLED;
    public TrafficLightStatus lastStatus = TrafficLightStatus.DISABLED;

    private boolean started = false;

    @Inject
    public TrafficLightChallenge(TimerHandler timerHandler, ChallengeHandler challengeHandler) {
        super(
                "c_trafficlight",
                timerHandler,
                challengeHandler
        );

        this.plugin = JavaPlugin.getPlugin(Plugin.class);
    }

    @Override
    public ItemStack getItemStack() {
        return ItemBuilder.from(Material.DIRT)
                .name(this.getDisplayName())
                .lore(Arrays.asList(
                        Component.text("Stirbt jemand, gilt die Challenge als fehlgeschlagen!", NamedTextColor.WHITE)
                ))
                .pdc(persistentDataContainer -> {
                    NamespacedKey key = new NamespacedKey(this.plugin, "identifier");
                    persistentDataContainer.set(key, PersistentDataType.STRING, this.getIdentifierName());
                })
                .build();
    }

    @Override
    public Component getDisplayName() {
        return Component.text("Ampel Challenge", NamedTextColor.RED);
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
                BossBar.Color.WHITE,
                BossBar.Overlay.PROGRESS
        );

        this.timer = new TrafficLightTimer(this);
        this.bossBar.name(this.components.trafficLight);

        Bukkit.getServer().audiences().forEach(audience -> this.bossBar.addViewer(audience));
    }

    @Override
    public void onDeactivate() {
        this.timer.pause(); // shutdown timer
        this.started = false;

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
        if (!this.started) {
            this.started = true;
            this.timer.transition();
        }

        this.timer.resume();
    }

    @Override
    public Map<String, Object> save() {
        Map<String, Object> data = new HashMap<>();

        data.put("started", this.started);
        data.put("status", this.status);
        data.put("remainingTime", this.timer.remainingTime);

        return data;
    }

    @Override
    public void append(Map<String, Object> data) {
        this.started = (boolean) data.get("started");
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
        if (this.timerHandler.isPaused()) return;

        if (!(event.getEntity() instanceof EnderDragon)) return;

        this.success();
        this.timer.pause();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(PlayerMoveEvent event) {
        if (!this.active) return;
        if (this.timerHandler.isPaused()) return;

        if (this.status == TrafficLightStatus.RED
                && event.getPlayer().getGameMode() != GameMode.SPECTATOR) {
            Location from = event.getFrom();
            Location to = event.getTo();

            double x = Math.floor(from.getX());
            double z = Math.floor(from.getZ());

            if (Math.floor(to.getX()) != x || Math.floor(to.getZ()) != z) {
                Bukkit.broadcast(Component.text(event.getPlayer().getName(), NamedTextColor.GREEN).append(Component.text(" hat die Ampel nicht beachtet", NamedTextColor.RED)));
                this.fail();
                this.timer.pause();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(PlayerDeathEvent event) {
        if (!this.active) return;
        event.deathMessage(null);

        if (this.timerHandler.isPaused()) return;

        Bukkit.broadcast(Component.text(event.getPlayer().getName(), NamedTextColor.GREEN).append(Component.text(" ist gestorben!", NamedTextColor.RED)));
        this.fail();
        this.timer.pause();
    }
}
