package io.github.schmolldechse.challenge.map.challenge.forcebattle;

import io.github.schmolldechse.challenge.module.Module;
import io.github.schmolldechse.team.Team;

public abstract class ForcebattleModule extends Module<ForcebattleChallenge> {

    public ForcebattleModule(ForcebattleChallenge challenge, String identifierName) {
        super(challenge, identifierName);
    }

    public void onTaskComplete(Team team, ForcebattleTask newTask) { }
}
