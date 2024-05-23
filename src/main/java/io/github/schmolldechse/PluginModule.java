package io.github.schmolldechse;

import com.google.inject.AbstractModule;
import io.github.schmolldechse.challenge.ChallengeHandler;
import io.github.schmolldechse.config.save.SaveConfigHandler;
import io.github.schmolldechse.setting.SettingHandler;
import io.github.schmolldechse.timer.TimerHandler;

public class PluginModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TimerHandler.class).asEagerSingleton();
        bind(ChallengeHandler.class).asEagerSingleton();
        bind(SettingHandler.class).asEagerSingleton();
        bind(SaveConfigHandler.class).asEagerSingleton();
    }
}
