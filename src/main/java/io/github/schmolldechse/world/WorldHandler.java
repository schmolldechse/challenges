package io.github.schmolldechse.world;

import com.google.inject.Inject;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.timer.TimerHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class WorldHandler {

    private final TimerHandler timerHandler;

    private final Plugin plugin;

    @Inject
    public WorldHandler(TimerHandler timerHandler) {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.timerHandler = timerHandler;
    }

    public void prepare() {
        if (this.timerHandler != null && !this.timerHandler.isPaused()) this.timerHandler.pause();

        Component message = Component.text("Die Welt wird zurückgesetzt", NamedTextColor.RED).decoration(TextDecoration.ITALIC, true);
        Bukkit.getOnlinePlayers().forEach(player -> player.kick(message, PlayerKickEvent.Cause.PLUGIN));

        Bukkit.getWorlds().forEach(world -> {
            world.setAutoSave(false);

            Bukkit.unloadWorld(world, false);
            delete(world.getWorldFolder());
        });

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> Bukkit.spigot().restart(), 20L);
    }

    private boolean delete(File path) {
        if (!path.exists()) return false;

        File[] files = path.listFiles();
        for (File file : files) {
            if (file.isDirectory()) delete(file);
            else file.delete();
        }

        return path.delete();
    }
}