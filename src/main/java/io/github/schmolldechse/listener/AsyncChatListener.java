package io.github.schmolldechse.listener;

import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.team.Team;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class AsyncChatListener implements Listener {

    private final Plugin plugin;

    public AsyncChatListener() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void execute(AsyncChatEvent event) {
        Player player = event.getPlayer();

        Team team = this.plugin.teamHandler.team(player);
        if (team == null) {
            event.renderer((source, sourceDisplayName, message, viewer) -> sourceDisplayName
                    .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                    .append(message));
            return;
        }

        event.renderer((source, sourceDisplayName, message, viewer) -> {
            return MiniMessage.miniMessage().deserialize("<yellow>[#" + team.getName() + "]</yellow> <reset>" + source.getName() + "</reset> <dark_gray>|</dark_gray> <reset>" + PlainTextComponentSerializer.plainText().serialize(message));
        });
    }
}
