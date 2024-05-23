package io.github.schmolldechse;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.github.schmolldechse.challenge.ChallengeHandler;
import io.github.schmolldechse.commands.ResetCommand;
import io.github.schmolldechse.commands.SetupCommand;
import io.github.schmolldechse.commands.TimerCommand;
import io.github.schmolldechse.config.save.SaveConfigHandler;
import io.github.schmolldechse.inventory.ChallengeInventory;
import io.github.schmolldechse.inventory.SettingInventory;
import io.github.schmolldechse.inventory.SetupInventory;
import io.github.schmolldechse.listener.PlayerJoinListener;
import io.github.schmolldechse.listener.PlayerMoveListener;
import io.github.schmolldechse.listener.PlayerResourcePackStatusListener;
import io.github.schmolldechse.setting.SettingHandler;
import io.github.schmolldechse.timer.TimerHandler;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public final class Plugin extends JavaPlugin {

    @Inject public TimerHandler timerHandler;
    @Inject public ChallengeHandler challengeHandler;
    @Inject public SettingHandler settingHandler;
    @Inject public SaveConfigHandler saveConfigHandler;

    public boolean MOVEMENT_ALLOWED = true;
    public String RESET_TYPE = "RESTART";

    // TODO: fetch hash from server defined in config
    public boolean RESOURCEPACK_ENABLED;
    public String RESOURCEPACK_URL = "https://voldechse.wtf/challenges.zip";
    public String RESOURCEPACK_HASH;

    public boolean RESET_EXECUTED = false;

    // inventories
    public SetupInventory setupInventory;
    public ChallengeInventory challengeInventory;
    public SettingInventory settingInventory;

    @Override
    public void onLoad() {
        // TODO: CommandAPI -> Cloud v2
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(true));

        if (!this.getDataFolder().exists()) this.getDataFolder().mkdir();

        this.createConfig();
        this.readConfig();

        this.purgeWorlds(true);
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();

        Injector injector = Guice.createInjector(new PluginModule());
        injector.injectMembers(this);

        new TimerCommand().registerCommand();
        new SetupCommand().registerCommand();
        new ResetCommand().registerCommand();

        new PlayerMoveListener(this.timerHandler);
        new PlayerJoinListener();
        new PlayerResourcePackStatusListener();

        this.setupInventory = new SetupInventory();
        this.challengeInventory = new ChallengeInventory();
        this.settingInventory = new SettingInventory();

        this.RESET_EXECUTED = false;

        this.saveConfigHandler.readAppend();
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();

        if (!this.RESET_EXECUTED && this.saveConfigHandler != null) this.saveConfigHandler.save();

        if (this.timerHandler != null) this.timerHandler.shutdown();
        if (this.challengeHandler != null) this.challengeHandler.deactivate();
        if (this.settingHandler != null) this.settingHandler.deactivate();

        this.purgeWorlds(false);
    }

    private void readConfig() {
        File configFile = new File(this.getDataFolder(), "config.json");
        if (!configFile.exists()) return;

        try {
            String content = new String(Files.readAllBytes(Paths.get(configFile.toURI())));
            JSONObject jsonObject = new JSONObject(content);

            String resetType = jsonObject.getString("resetType");
            switch (resetType) {
                case "RESTART":
                    this.RESET_TYPE = "RESTART";
                    break;
                case "STOP":
                    this.RESET_TYPE = "STOP";
                    break;
                default:
                    this.getLogger().severe("Found invalid parameter for resetType in config.json: " + resetType);
                    break;
            }

            this.RESOURCEPACK_ENABLED = jsonObject.getJSONObject("resourcepack").getBoolean("enabled");
            if (this.RESOURCEPACK_ENABLED) {
                this.RESOURCEPACK_URL = jsonObject.getJSONObject("resourcepack").getString("url");
                this.RESOURCEPACK_HASH = jsonObject.getJSONObject("resourcepack").getString("hash");
            }
        } catch (IOException e) {
            this.getLogger().severe("Failed to read config.json: " + e.getMessage());
        }
    }

    private void createConfig() {
        File configFile = new File(this.getDataFolder(), "config.json");
        if (configFile.exists()) return;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("resetType", "RESTART");

        JSONObject resourcePack = new JSONObject();
        resourcePack.put("enabled", false);
        resourcePack.put("url", "https://voldechse.wtf/challenges.zip");
        resourcePack.put("hash", "ba14b01c69bb434e259786bdca4c14bb1c00e495");

        jsonObject.put("resourcepack", resourcePack);

        try (FileWriter file = new FileWriter(configFile)) {
            file.write(jsonObject.toString());
            file.flush();
        } catch (IOException e) {
            this.getLogger().severe("Failed to write config.json: " + e.getMessage());
        }
    }

    private void purgeWorlds(boolean deleteCache) {
        File resetCacheFile = new File(this.getDataFolder(), "reset.cache");
        if (!resetCacheFile.exists()) return;

        Bukkit.getWorlds().forEach(world -> Bukkit.unloadWorld(world, false));

        List<String> worlds;
        try {
            worlds = Files.readAllLines(Paths.get(resetCacheFile.toURI()));
        } catch (IOException exception) {
            this.getLogger().severe("Failed to read reset.cache: " + exception.getMessage());
            return;
        }

        File directory = new File(this.getServer().getWorldContainer().getAbsolutePath());
        File[] worldDirectories = directory.listFiles(File::isDirectory);
        if (worldDirectories == null) return;

        Arrays.stream(worldDirectories)
                .filter(worldDirectory -> worlds.contains(worldDirectory.getName()))
                .forEach(worldDirectory -> {
                    try {
                        FileUtils.deleteDirectory(worldDirectory);
                    } catch (IOException e) {
                        this.getLogger().severe("Failed to delete world directory: " + e.getMessage());
                    }
                });

        if (deleteCache) {
            resetCacheFile.delete();

            this.getLogger().info("Deleted reset.cache file");
            this.getLogger().info("RESTARTING SERVER - STARTING NEW WORLD GENERATION ON NEXT STARTUP");

            switch (this.RESET_TYPE) {
                case "STOP":
                    Bukkit.shutdown();
                    break;
                case "RESTART":
                    Bukkit.spigot().restart();
                    break;
            }
        }
    }
}