package io.github.schmolldechse;

import com.google.inject.AbstractModule;
import io.github.schmolldechse.challenge.ChallengeHandler;
import io.github.schmolldechse.config.save.SaveConfigHandler;
import io.github.schmolldechse.misc.scoreboard.ScoreboardHandler;
import io.github.schmolldechse.team.TeamHandler;
import io.github.schmolldechse.timer.TimerHandler;

public class PluginModule extends AbstractModule {
    @Override
    protected void configure() {
        this.bind(TeamHandler.class).asEagerSingleton();
        this.bind(TimerHandler.class).asEagerSingleton();
        this.bind(ChallengeHandler.class).asEagerSingleton();
        this.bind(SaveConfigHandler.class).asEagerSingleton();
        this.bind(ScoreboardHandler.class).asEagerSingleton();
    }
}
