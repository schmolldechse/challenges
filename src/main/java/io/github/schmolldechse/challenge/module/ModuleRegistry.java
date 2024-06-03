package io.github.schmolldechse.challenge.module;

import io.github.schmolldechse.challenge.Challenge;

import java.util.ArrayList;
import java.util.List;

public class ModuleRegistry {

    private final List<Module<? extends Challenge>> modules = new ArrayList<>();

    public void register(Module<? extends Challenge> module) {
        this.modules.add(module);
    }

    public Module<? extends Challenge> module(String identifierName) {
        return this.modules.stream()
                .filter(module -> module.getIdentifierName().equals(identifierName))
                .findFirst()
                .orElse(null);
    }

    public List<Module<? extends Challenge>> getModules() {
        return new ArrayList<>(this.modules);
    }
}
