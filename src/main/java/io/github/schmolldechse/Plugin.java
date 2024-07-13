package io.github.schmolldechse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.github.schmolldechse.challenge.ChallengeHandler;
import io.github.schmolldechse.challenge.map.challenge.forcebattle.commands.ResultCommand;
import io.github.schmolldechse.challenge.map.challenge.forcebattle.commands.SkipCommand;
import io.github.schmolldechse.commands.ResetCommand;
import io.github.schmolldechse.commands.SetupCommand;
import io.github.schmolldechse.commands.TimerCommand;
import io.github.schmolldechse.config.save.SaveConfigHandler;
import io.github.schmolldechse.inventory.*;
import io.github.schmolldechse.listener.PlayerJoinListener;
import io.github.schmolldechse.listener.PlayerMoveListener;
import io.github.schmolldechse.listener.PlayerResourcePackStatusListener;
import io.github.schmolldechse.team.TeamCommand;
import io.github.schmolldechse.team.TeamHandler;
import io.github.schmolldechse.timer.TimerHandler;
import io.github.schmolldechse.world.WorldHandler;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class Plugin extends JavaPlugin {

    @Inject public TimerHandler timerHandler;
    @Inject public ChallengeHandler challengeHandler;
    @Inject public SaveConfigHandler saveConfigHandler;
    @Inject public TeamHandler teamHandler;

    public boolean MOVEMENT_ALLOWED = true;

    // TODO: fetch hash from server defined in config
    public boolean RESOURCEPACK_ENABLED;
    public String RESOURCEPACK_URL = "https://voldechse.wtf/challenges.zip";
    public String RESOURCEPACK_HASH;

    public boolean DELETE_EXECUTED = false;

    // inventories
    public SetupInventory setupInventory;
    public ConditionInventory conditionInventory;
    public ChallengeInventory challengeInventory;
    public PlayerInventory playerInventory;
    public WorldInventory worldInventory;

    public final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void onLoad() {
        if (!this.getDataFolder().exists()) this.getDataFolder().mkdir();

        this.createConfig();
        this.readConfig();

        new WorldHandler().purge();
    }

    @Override
    public void onEnable() {
        Injector injector = Guice.createInjector(new PluginModule());
        injector.injectMembers(this);

        final LifecycleEventManager<org.bukkit.plugin.Plugin> lifecycleManager = this.getLifecycleManager();
        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();

            new ResetCommand(commands);
            new SetupCommand(commands);
            new TimerCommand(commands);
            new TeamCommand(commands);
            new SkipCommand(commands);
            new ResultCommand(commands);
        });

        new PlayerMoveListener();
        new PlayerJoinListener();
        new PlayerResourcePackStatusListener();

        this.setupInventory = new SetupInventory();
        this.conditionInventory = new ConditionInventory();
        this.challengeInventory = new ChallengeInventory();
        this.playerInventory = new PlayerInventory();
        this.worldInventory = new WorldInventory();

        this.DELETE_EXECUTED = false;

        this.saveConfigHandler.readAppend();
    }

    @Override
    public void onDisable() {
        if (!this.DELETE_EXECUTED && this.saveConfigHandler != null) this.saveConfigHandler.save();

        if (this.timerHandler != null) this.timerHandler.shutdown();
        if (this.challengeHandler != null) this.challengeHandler.deactivate();
    }

    private void readConfig() {
        File configFile = new File(this.getDataFolder(), "config.json");
        if (!configFile.exists()) return;

        try {
            String content = new String(Files.readAllBytes(Paths.get(configFile.toURI())));
            JSONObject jsonObject = new JSONObject(content);

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
}