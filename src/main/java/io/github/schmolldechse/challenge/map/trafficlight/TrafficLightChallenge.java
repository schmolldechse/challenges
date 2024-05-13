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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;

public class TrafficLightChallenge extends Challenge {

    private final Plugin plugin;

    private BossBar bossBar;

    private final TrafficLightComponents components = new TrafficLightComponents();
    private TrafficLightTimer trafficLightTimer = new TrafficLightTimer();

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
                this.components.trafficLight,
                0,
                BossBar.Color.WHITE,
                BossBar.Overlay.PROGRESS
        );

        Bukkit.getServer().audiences().forEach(audience -> this.bossBar.addViewer(audience));

        this.trafficLightTimer.bossBar = this.bossBar;
        this.trafficLightTimer.status = TrafficLightStatus.DISABLED;
    }

    @Override
    public void onDeactivate() {
        this.trafficLightTimer.shutdown();
        this.trafficLightTimer.status = TrafficLightStatus.DISABLED;

        if (this.bossBar == null) return;
        this.bossBar.name(this.components.trafficLight);

        Bukkit.getServer().audiences().forEach(audience -> audience.hideBossBar(this.bossBar));
        this.bossBar = null;
    }

    @Override
    public void onPause() {
        this.trafficLightTimer.pause();
    }

    @Override
    public void onResume() {
        if (this.trafficLightTimer.status == TrafficLightStatus.DISABLED) this.trafficLightTimer.changeToGreen();
        else this.trafficLightTimer.resume();
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
    }
}
