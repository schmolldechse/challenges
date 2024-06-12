package io.github.schmolldechse.config.save;

import com.google.gson.JsonArray;
import com.google.inject.Inject;
import io.github.schmolldechse.Plugin;
import io.github.schmolldechse.challenge.Challenge;
import io.github.schmolldechse.config.document.Document;
import io.github.schmolldechse.team.Team;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;

public class SaveConfigHandler {

    private final Plugin plugin;

    private final File path;

    @Inject
    public SaveConfigHandler() {
        this.plugin = JavaPlugin.getPlugin(Plugin.class);
        this.path = new File(this.plugin.getDataFolder(), "save.cache");
    }

    public void readAppend() {
        if (!this.path.exists()) return;
        Document document = Document.load(this.path.toPath());

        if (document.contains("timer")) {
            Document timer = document.getDocument("timer");
            this.plugin.timerHandler.time = timer.getInt("time");
            this.plugin.timerHandler.reverse = timer.getBoolean("reverse");
        }

        if (document.contains("teams")) {
            JsonArray teams = document.getArray("teams");

            teams.forEach(element -> {
                Document teamDocument = new Document(element.getAsJsonObject());

                Team team = new Team(teamDocument.getString("name"));
                team.setDocument(teamDocument.getDocument("document"));
                teamDocument.getArray("uuids").forEach(uuidElement -> team.addMember(UUID.fromString(uuidElement.getAsString())));

                this.plugin.teamHandler.register(team);
            });
        }

        if (document.contains("challenges")) {
            JsonArray challenges = document.getArray("challenges");

            challenges.forEach(element -> {
                Document challengeDocument = new Document(element.getAsJsonObject());
                String identifierName = challengeDocument.keys().iterator().next();

                Challenge challenge = this.plugin.challengeHandler.getChallenge(identifierName);
                if (challenge == null) return;

                challenge.setActive(true);
                challenge.onActivate();
                challenge.append(challengeDocument.getDocument(identifierName));
            });
        }

        this.path.delete();
    }

    public void save() {
        Document document = new Document();

        document.append("timer", new Document()
                .append("time", this.plugin.timerHandler.time)
                .append("reverse", this.plugin.timerHandler.reverse)
        );

        document.append("teams", this.plugin.teamHandler.getRegisteredTeams().stream()
                .map(Team::save)
                .toList()
        );

        document.append("challenges", this.plugin.challengeHandler.registeredChallenges.entrySet().stream()
                .filter(entry -> entry.getValue().isActive())
                .map(entry -> new Document().append(entry.getKey(), entry.getValue().save()))
                .toList());

        document.save(this.path.toPath());
    }
}
