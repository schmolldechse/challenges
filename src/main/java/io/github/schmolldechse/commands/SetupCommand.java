package io.github.schmolldechse.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.schmolldechse.Plugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class SetupCommand {

    // /setup

    private final Plugin plugin;

    public SetupCommand(Commands commands) {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.register(commands);
    }

    private void register(Commands commands) {
        final LiteralArgumentBuilder<CommandSourceStack> setupBuilder = Commands.literal("setup")
                .executes((source) -> {
                    Entity entity = source.getSource().getExecutor();
                    if (!(entity instanceof Player player)) {
                        source.getSource().getExecutor().sendMessage(Component.text("You can not execute this command", NamedTextColor.RED));
                        return 0;
                    }

                    this.plugin.setupInventory.getInventory().open(player);
                    return 1;
                });
        commands.register(this.plugin.getPluginMeta(), setupBuilder.build(), "Reset the worlds & if specified, delete the current settings of challenges", List.of());
    }
}
