package io.github.schmolldechse.challenge;

import com.google.inject.Inject;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.misc.Reflection;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.util.*;

public class ChallengeHandler {

    public final Map<String, Challenge> registeredChallenges = new HashMap<>();

    private final Plugin plugin;

    @Inject
    public ChallengeHandler() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);

        this.register();
    }

    private void register() {
        Set<Class<? extends Challenge>> classes = Reflection.findClasses("io.github.schmolldechse.challenge.map", Challenge.class);

        for (Class<? extends Challenge> challengeClass : classes) {
            try {
                Constructor<? extends Challenge> constructor = challengeClass.getDeclaredConstructor();
                Challenge challenge = constructor.newInstance();

                this.registeredChallenges.put(challenge.getIdentifierName(), challenge);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        this.plugin.getLogger().info("Registered " + this.registeredChallenges.size() + " challenge" + (this.registeredChallenges.size() > 1 ? "s" : ""));
    }

    public Challenge getChallenge(String identifier) {
        return this.registeredChallenges.get(identifier);
    }

    public void toggle(String identifier) {
        if (this.plugin == null) throw new IllegalStateException("Plugin is not initialized");

        Challenge challenge = this.getChallenge(identifier);
        if (challenge == null) throw new IllegalArgumentException("Challenge with identifier " + identifier + " does not exist");
        challenge.toggle();
    }

    public void deactivate() {
        this.registeredChallenges.entrySet().stream()
                .filter(entry -> entry.getValue().isActive())
                .forEach(entry -> {
                    entry.getValue().active = false;
                    entry.getValue().onDeactivate();
                });
    }
}