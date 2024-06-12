package io.github.schmolldechse.config.document;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.UncheckedIOException;

public class DocumentTypeAdapter extends TypeAdapter<Document> {

    private final Gson GSON = new Gson();

    @Override
    public void write(JsonWriter out, Document value) throws IOException {
        out.beginObject();

        value.jsonObject.entrySet().forEach(entry -> {
            try {
                out.name(entry.getKey());
                this.GSON.toJson(entry.getValue(), out);
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        });

        out.endObject();
    }

    @Override
    public Document read(JsonReader in) throws IOException {
        Document document = new Document();

        in.beginObject();

        while (in.hasNext()) {
            String key = in.nextName();
            JsonElement jsonElement = this.GSON.fromJson(in, JsonElement.class);
            document.jsonObject.add(key, jsonElement);
        }

        in.endObject();
        return document;
    }
}
