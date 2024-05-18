package io.github.schmolldechse.listener;

import io.github.schmolldechse.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerJoinListener implements Listener {

    private final Plugin plugin;

    public PlayerJoinListener() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler
    public void execute(PlayerJoinEvent event) {
        if (!this.plugin.RESOURCEPACK_ENABLED) return;

        event.getPlayer().setResourcePack(
                this.plugin.RESOURCEPACK_URL,
                this.plugin.RESOURCEPACK_HASH,
                true,
                Component.text("Damit du alle Grafiken ordnungsgemäß sehen kannst, musst du das Resource Pack akzeptieren", NamedTextColor.RED).decoration(TextDecoration.BOLD, true)
        );
    }
}
