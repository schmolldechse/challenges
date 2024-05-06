package io.github.schmolldechse;

import com.google.inject.AbstractModule;
import io.github.schmolldechse.challenge.ChallengeHandler;
import io.github.schmolldechse.timer.TimerHandler;
import io.github.schmolldechse.world.WorldHandler;

public class PluginModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(TimerHandler.class).asEagerSingleton();
        bind(ChallengeHandler.class).asEagerSingleton();
        bind(WorldHandler.class).asEagerSingleton();
    }
}
