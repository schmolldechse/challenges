package io.github.schmolldechse.misc;

import io.github.schmolldechse.challenge.Challenge;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;

public class Reflection {

    public static Set<Class<? extends Challenge>> findChallengeClasses(String packageName) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(packageName)
                .addScanners(new SubTypesScanner(false)));

        return reflections.getSubTypesOf(Challenge.class);
    }
}
