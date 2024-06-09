package io.github.schmolldechse.team;

import java.util.*;

public class Team {

    private String name;
    private final List<UUID> uuids;
    private Map<String, Object> data;

    public Team(String name) {
        this.name = name;

        this.uuids = new ArrayList<>();
        this.data = new HashMap<>();
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

    public Map<String, Object> save() {
        Map<String, Object> data = new HashMap<>();

        data.put("name", this.name);
        data.put("uuids", this.uuids);
        data.put("data", this.data);

        return data;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
