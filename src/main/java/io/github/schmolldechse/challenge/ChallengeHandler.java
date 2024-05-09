package io.github.schmolldechse.challenge;

import com.google.inject.Inject;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.timer.TimerHandler;
import io.github.schmolldechse.misc.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.util.*;

public class ChallengeHandler {

    public final Map<String, Challenge> registeredChallenges = new HashMap<>();

    private final TimerHandler timerHandler;

    private final Plugin plugin;

    @Inject
    public ChallengeHandler(TimerHandler timerHandler) {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.timerHandler = timerHandler;

        this.register();
    }

    private void register() {
        Set<Class<? extends Challenge>> challengeClasses = Reflection.findChallengeClasses("io.github.schmolldechse.challenge.map");

        for (Class<? extends Challenge> challengeClass : challengeClasses) {
            try {
                Constructor<? extends Challenge> constructor = challengeClass.getDeclaredConstructor(TimerHandler.class, ChallengeHandler.class);
                Challenge challenge = constructor.newInstance(this.timerHandler, this);

                this.registeredChallenges.put(challenge.getIdentifierName(), challenge);

                Bukkit.getPluginManager().registerEvents(challenge, this.plugin);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        this.plugin.getLogger().info("Registered " + this.registeredChallenges.size() + " challenge" + (this.registeredChallenges.size() > 1 ? "1" : ""));
    }

    public Challenge getChallenge(String identifier) {
        return this.registeredChallenges.get(identifier);
    }

    public void toggle(String identifier) {
        if (this.plugin == null) throw new IllegalStateException("Plugin is not initialized");

        this.registeredChallenges.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(identifier))
                .forEach(entry -> entry.getValue().active = false);

        this.getChallenge(identifier).active = true;
    }
}