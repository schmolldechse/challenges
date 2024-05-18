package io.github.schmolldechse.listener;

import io.github.schmolldechse.Plugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerResourcePackStatusListener implements Listener {

    private final Plugin plugin;

    public PlayerResourcePackStatusListener() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler
    public void execute(PlayerResourcePackStatusEvent event) {
        if (!this.plugin.RESOURCEPACK_ENABLED) return;
        if (event.getStatus() != PlayerResourcePackStatusEvent.Status.DECLINED) return;

        event.getPlayer().kick(Component.text(
                "Bitte akzeptiere das Resource Pack, damit du die Grafiken korrekt sehen kannst",
                NamedTextColor.RED
        ));
    }
}
