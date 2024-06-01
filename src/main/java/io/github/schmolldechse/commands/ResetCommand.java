package io.github.schmolldechse.commands;

import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import io.github.schmolldechse.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ResetCommand {

    /**
     * Deletes all registered world folder and restarts the server
     * When specified, it copies the current {@link io.github.schmolldechse.challenge.ChallengeHandler settings}
     *
     * /reset
     * /reset copy
     */

    private final Plugin plugin;

    public ResetCommand() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
    }

    public void registerCommand() {
        new CommandTree("reset")
                .then(new LiteralArgument("copy")
                        .executes((sender, args) -> {
                            this.prepare();
                            this.executeRestart();
                        }))
                .executes((sender, args) -> {
                    this.prepare();
                    this.plugin.DELETE_EXECUTED = true;
                    this.executeRestart();
                })
                .register();
    }

    private void prepare() {
        if (this.plugin.timerHandler != null && !this.plugin.timerHandler.isPaused()) this.plugin.timerHandler.pause();

        Component message = Component.text("Die Welt wird zurückgesetzt", NamedTextColor.RED).decoration(TextDecoration.ITALIC, true);
        Bukkit.getOnlinePlayers().forEach(player -> player.kick(message, PlayerKickEvent.Cause.PLUGIN));

        File resetCacheFile = new File(this.plugin.getDataFolder(), "reset.cache");
        try {
            List<String> worlds = Bukkit.getWorlds().stream()
                    .map(WorldInfo::getName)
                    .collect(Collectors.toList());

            Files.write(Paths.get(resetCacheFile.toURI()), worlds);

            this.plugin.getLogger().info("Wrote world names to reset cache file");
        } catch (IOException exception) {
            this.plugin.getLogger().severe("Failed to create reset cache file");
        }
    }

    private void executeRestart() {
        switch (this.plugin.RESET_TYPE) {
            case "STOP":
                Bukkit.shutdown();
                break;
            case "RESTART":
                Bukkit.spigot().restart();
                break;
        }
    }
}
