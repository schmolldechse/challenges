package io.github.schmolldechse.commands;

import dev.jorel.commandapi.CommandTree;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.world.WorldHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class ResetCommand {

    // /reset

    private final Plugin plugin;

    private final WorldHandler worldHandler;

    public ResetCommand(WorldHandler worldHandler) {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);

        this.worldHandler = worldHandler;
    }

    public void registerCommand() {
        new CommandTree("reset")
                .executes((sender, args) -> {
                    this.prepare();
                })
                .register();
    }
}
