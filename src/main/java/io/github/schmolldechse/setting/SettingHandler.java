package io.github.schmolldechse.setting;

import com.google.inject.Inject;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.misc.Reflection;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.util.*;

public class SettingHandler {

    public final Map<String, Setting> registeredSettings = new HashMap<>();

    private final Plugin plugin;

    @Inject
    public SettingHandler() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);

        this.register();
    }

    private void register() {
        Set<Class<? extends Setting>> classes = Reflection.findClasses("io.github.schmolldechse.setting.map", Setting.class);

        for (Class<? extends Setting> settingClass : classes) {
            try {
                Constructor<? extends Setting> constructor = settingClass.getDeclaredConstructor();
                Setting challenge = constructor.newInstance();

                this.registeredSettings.put(challenge.getIdentifierName(), challenge);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        this.plugin.getLogger().info("Registered " + this.registeredSettings.size() + " setting" + (this.registeredSettings.size() > 1 ? "s" : ""));
    }

    public Setting getSetting(String identifier) {
        return this.registeredSettings.get(identifier);
    }

    public void toggle(String identifier) {
        if (this.plugin == null) throw new IllegalStateException("Plugin is not initialized");

        Setting setting = this.getSetting(identifier);
        if (setting == null) throw new IllegalArgumentException("Setting with identifier " + identifier + " does not exist");
        setting.toggle();
    }

    public void deactivate() {
        this.registeredSettings.entrySet().stream()
                .filter(entry -> entry.getValue().isActive())
                .forEach(entry -> {
                    entry.getValue().active = false;
                    entry.getValue().onDeactivate();
                });
    }
}