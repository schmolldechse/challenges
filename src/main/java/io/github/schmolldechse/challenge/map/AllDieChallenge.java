package io.github.schmolldechse.challenge.map;

import com.google.inject.Inject;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.challenge.Challenge;
import io.github.schmolldechse.challenge.ChallengeHandler;
import io.github.schmolldechse.timer.TimerHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class AllDieChallenge extends Challenge {

    private final Plugin plugin;

    @Inject
    public AllDieChallenge(TimerHandler timerHandler, ChallengeHandler challengeHandler) {
        super(
                Material.DIRT,
                Component.text("Einer stirbt, Alle sterben", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false),
                "c_alldie",
                Arrays.asList(
                        Component.empty(),
                        Component.text("Die Challenge endet, sobald einer stirbt", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false),
                        Component.text("- ", NamedTextColor.DARK_GRAY).append(Component.text("kein Zeitlimit", NamedTextColor.YELLOW)).decoration(TextDecoration.ITALIC, false)),
                timerHandler,
                challengeHandler
        );

        this.plugin = JavaPlugin.getPlugin(Plugin.class);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void execute(PlayerDeathEvent event) {
        if (!this.active) return;
        event.deathMessage(null);

        if (this.timerHandler.isPaused()) return;

        Bukkit.broadcast(Component.text(event.getPlayer().getName(), NamedTextColor.GREEN).append(Component.text(" ist gestorben!", NamedTextColor.RED)));
        this.fail();
    }
}
