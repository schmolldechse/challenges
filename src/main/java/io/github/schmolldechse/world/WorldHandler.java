package io.github.schmolldechse.world;

import io.github.schmolldechse.Plugin;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class WorldHandler {

    private final Plugin plugin;

    private final String[] FOLDERS = { "advancements", "data", "entities", "playerdata", "poi", "region", "stats", "DIM-1", "DIM1" };
    private final String[] FILES = { "level.dat", "level.dat_old", "paper-world.yml", "session.lock" };

    public WorldHandler() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
    }

    public void purge() {
        File file = new File(this.plugin.getDataFolder(), "reset.cache");
        if (!file.exists()) return;

        Bukkit.getWorlds().forEach(world -> Bukkit.unloadWorld(world, false));

        List<String> worlds;
        try {
            worlds = Files.readAllLines(Paths.get(file.toURI()));
        } catch (IOException e) {
            this.plugin.getLogger().severe("Failed to read reset.cache: " + e.getMessage());
            return;
        }

        File directory = new File(this.plugin.getServer().getWorldContainer().getAbsolutePath());
        File[] worldDirectories = directory.listFiles(File::isDirectory);
        if (worldDirectories == null) return;

        Arrays.stream(worldDirectories)
                .filter(worldDirectory -> worlds.contains(worldDirectory.getName()))
                .forEach(worldDirectory -> {
                    Arrays.stream(this.FOLDERS)
                            .forEach(folder -> {
                                File subFolder = new File(worldDirectory, folder);
                                if (!subFolder.exists() || !subFolder.isDirectory()) return;

                                try {
                                    FileUtils.cleanDirectory(subFolder);
                                } catch (IOException e) {
                                    this.plugin.getLogger().severe("Failed to delete contents of folder " + subFolder.getAbsolutePath() + ": " + e.getMessage());
                                }
                            });

                    Arrays.stream(this.FILES).forEach(fileName -> {
                        File subFile = new File(worldDirectory, fileName);
                        if (subFile.exists() && !subFile.delete())
                            this.plugin.getLogger().severe("Failed to delete file: " + subFile.getAbsolutePath());
                    });
                });

        file.delete();
        this.plugin.getLogger().info("Deleted reset.cache file");
    }
}
