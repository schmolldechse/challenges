package io.github.schmolldechse.team;

import io.github.schmolldechse.config.document.Document;

public interface Extension {

    String name();

    Document save();

    static <T extends Extension> T fromDocument(Document document, Class<T> clazz) {
        throw new UnsupportedOperationException("fromDocument not implemented for " + clazz.getName());
    }
}
