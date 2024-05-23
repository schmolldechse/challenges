package io.github.schmolldechse.commands;

import dev.jorel.commandapi.CommandTree;
import io.github.schmolldechse.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class SetupCommand {

    // /setup

    private final Plugin plugin;

    public SetupCommand() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
    }

    public void registerCommand() {
        new CommandTree("setup")
                .executesPlayer((sender, args) -> {
                    this.plugin.setupInventory.getInventory().open(sender);
                })
                .register();
    }
}
