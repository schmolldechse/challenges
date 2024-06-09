package io.github.schmolldechse.listener;

import io.github.schmolldechse.Plugin;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;

public class PlayerJoinListener implements Listener {

    private final Plugin plugin;

    private final ResourcePackInfo PACK_INFO;

    public PlayerJoinListener() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);

        this.PACK_INFO = ResourcePackInfo.resourcePackInfo()
                .uri(URI.create(this.plugin.RESOURCEPACK_URL))
                .hash(this.plugin.RESOURCEPACK_HASH)
                .build();

        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    @EventHandler
    public void execute(PlayerJoinEvent event) {
        if (!this.plugin.RESOURCEPACK_ENABLED) return;

        final ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                .packs(this.PACK_INFO)
                .prompt(Component.text("Bitte akzeptiere das Resource Pack, um alle Grafiken zu sehen"))
                .required(true)
                .build();
        event.getPlayer().sendResourcePacks(request);
    }
}
