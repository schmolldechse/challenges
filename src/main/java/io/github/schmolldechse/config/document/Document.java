package io.github.schmolldechse.config.document;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Document {

    private final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Document.class, new DocumentTypeAdapter())
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    protected JsonObject jsonObject;

    public Document() {
        this.jsonObject = new JsonObject();
    }

    public Document(@NotNull JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public Document(@NotNull Document document) {
        this.jsonObject = document.jsonObject.deepCopy();
    }

    public Document(@NotNull String key, @Nullable String value) {
        this.jsonObject = new JsonObject();
        this.append(key, value);
    }

    public Document(@NotNull String key, @Nullable Object value) {
        this.jsonObject = new JsonObject();
        this.append(key, value);
    }

    public Document(@NotNull String key, @Nullable Number value) {
        this.jsonObject = new JsonObject();
        this.append(key, value);
    }

    public boolean contains(@NotNull String key) {
        return this.jsonObject.has(key);
    }

    public Document append(@NotNull String key, @Nullable String value) {
        if (value == null) return this;
        this.jsonObject.addProperty(key, value);
        return this;
    }

    public Document append(@NotNull String key, @Nullable Number value) {
        if (value == null) return this;
        this.jsonObject.addProperty(key, value);
        return this;
    }

    public Document append(@NotNull String key, @Nullable Object value) {
        if (value == null) return this;
        this.jsonObject.add(key, this.GSON.toJsonTree(value));
        return this;
    }

    public Document append(@NotNull String key, @Nullable List<?> value) {
        if (value == null) return this;
        this.jsonObject.add(key, this.GSON.toJsonTree(value));
        return this;
    }

    public Document append(@NotNull String key, @Nullable Map<?, ?>  value) {
        if (value == null) return this;
        this.jsonObject.add(key, this.GSON.toJsonTree(value));
        return this;
    }

    public Document append(@NotNull String key, @Nullable Document value) {
        if (value == null) return this;
        this.jsonObject.add(key, value.jsonObject);
        return this;
    }

    public JsonArray getArray(@NotNull String key) {
        if (!this.jsonObject.has(key)) return null;
        return this.jsonObject.get(key).getAsJsonArray();
    }

    public JsonElement getElement(@NotNull String key) {
        if (!this.jsonObject.has(key)) return null;
        return this.jsonObject.get(key);
    }

    public String getString(@NotNull String key) {
        if (!this.jsonObject.has(key)) return null;
        return this.jsonObject.get(key).getAsString();
    }

    public int getInt(@NotNull String key) {
        if (!this.jsonObject.has(key)) return 0;
        return this.jsonObject.get(key).getAsInt();
    }

    public long getLong(@NotNull String key) {
        if (!this.jsonObject.has(key)) return 0;
        return this.jsonObject.get(key).getAsLong();
    }

    public double getDouble(@NotNull String key) {
        if (!this.jsonObject.has(key)) return 0;
        return this.jsonObject.get(key).getAsDouble();
    }

    public float getFloat(@NotNull String key) {
        if (!this.jsonObject.has(key)) return 0;
        return this.jsonObject.get(key).getAsFloat();
    }

    public short getShort(@NotNull String key) {
        if (!this.jsonObject.has(key)) return 0;
        return this.jsonObject.get(key).getAsShort();
    }

    public boolean getBoolean(@NotNull String key) {
        if (!this.jsonObject.has(key)) return false;
        return this.jsonObject.get(key).getAsBoolean();
    }

    public Document getDocument(@NotNull String key) {
        if (!this.jsonObject.has(key)) return null;
        return new Document(this.jsonObject.getAsJsonObject(key));
    }

    public Document remove(@NotNull String key) {
        this.jsonObject.remove(key);
        return this;
    }

    public Set<String> keys() {
        return this.jsonObject.entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Document clear() {
        this.keys().forEach(this::remove);
        return this;
    }

    public String toJson() {
        return this.GSON.toJson(this.jsonObject);
    }

    public static Document load(@NotNull Path path) {
        try (InputStreamReader inputStreamReader = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            JsonObject object = JsonParser.parseReader(bufferedReader).getAsJsonObject();
            return new Document(object);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to load document from path: " + path, exception);
        }
    }

    public boolean save(@NotNull Path path) {
        try {
            if (!Files.exists(path)) Files.createFile(path);

            try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(Files.newOutputStream(path), StandardCharsets.UTF_8)) {
                this.GSON.toJson(this.jsonObject, outputStreamWriter);
                return true;
            }
        } catch (IOException exception) {
            throw new RuntimeException("Failed to save document to path: " + path, exception);
        }
    }
}
