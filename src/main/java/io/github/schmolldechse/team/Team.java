package io.github.schmolldechse.team;

import io.github.schmolldechse.config.document.Document;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Team {

    private String name;
    private final List<UUID> uuids;

    private final Map<Class<?>, Object> extensions;

    public Team(String name) {
        this.name = name;
        this.uuids = new ArrayList<>();
        this.extensions = new HashMap<>();
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

    public <T> void addExtension(@NotNull Class<?> type, @NotNull T extension) {
        this.extensions.put(type, extension);
    }

    public <T> Optional<T> getExtension(@NotNull Class<T> type) {
        return Optional.ofNullable(type.cast(this.extensions.get(type)));
    }

    public void removeExtension(@NotNull Class<?> type) {
        this.extensions.remove(type);
    }

    public void sendMessage(Component component) {
        this.uuids.forEach(uuid -> {
            Optional.ofNullable(Bukkit.getPlayer(uuid))
                    .ifPresent(player -> player.sendMessage(component));
        });
    }

    public Document save() {
        Document document = new Document("name", this.name)
                .append("uuids", this.uuids);

        List<Document> extensionList = new ArrayList<>();
        this.extensions.forEach((type, extensionObject) -> {
            if (!(extensionObject instanceof Extension)) return;

            Extension extension = (Extension) extensionObject;
            Document extensionDocument = new Document("name", extension.name())
                    .append("data", extension.save());
            extensionList.add(extensionDocument);
        });
        document.append("extensions", extensionList);

        return document;
    }

    public Component members() {
        List<String> names = this.uuids.stream()
                .map(Bukkit::getOfflinePlayer)
                .map(OfflinePlayer::getName)
                .toList();

        return Component.text(String.join(", ", names), NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public String toString() {
        return "Team{" +
                "name='" + this.name + '\'' +
                ", uuids=" + this.uuids +
                ", extensions=" + this.extensions +
                '}';
    }
}
