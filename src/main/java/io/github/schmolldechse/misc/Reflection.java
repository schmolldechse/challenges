package io.github.schmolldechse.misc;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;

public class Reflection {

    public static <T> Set<Class<? extends T>> findClasses(String packageName, Class<T> type) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(packageName)
                .addScanners(new SubTypesScanner(false)));

        return reflections.getSubTypesOf(type);
    }
}
