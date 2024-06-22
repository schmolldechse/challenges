package io.github.schmolldechse.team;

import io.github.schmolldechse.config.document.Document;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Team {

    private String name;
    private final List<UUID> uuids;
    private Document document;

    public Team(String name) {
        this.name = name;

        this.uuids = new ArrayList<>();
        this.document = new Document();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UUID> getUuids() {
        return uuids;
    }

    public boolean isMember(UUID uuid) {
        return this.uuids.contains(uuid);
    }

    public void addMember(UUID uuid) {
        this.uuids.add(uuid);
    }

    public void removeMember(UUID uuid) {
        this.uuids.remove(uuid);
    }

    public void sendMessage(Component component) {
        this.uuids.forEach(uuid -> {
            Optional.ofNullable(Bukkit.getPlayer(uuid))
                    .ifPresent(player -> player.sendMessage(component));
        });
    }

    public Document save() {
        return new Document("name", this.name)
                .append("uuids", this.uuids)
                .append("document", this.document);
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return document;
    }
}
