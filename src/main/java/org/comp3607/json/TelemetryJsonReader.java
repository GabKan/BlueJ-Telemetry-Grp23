package org.comp3607.json;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.comp3607.collector.BufferedEventSink;
import org.comp3607.model.EventType;
import org.comp3607.model.SessionMetadata;
import org.comp3607.model.TelemetryEvent;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class TelemetryJsonReader {

    private static final Type DATA_MAP_TYPE = new TypeToken<Map<String, Object>>() { }.getType();

    public BufferedEventSink read(Path file) throws IOException {
        JsonObject root;
        try {
            JsonElement parsedJson = JsonParser.parseString(
                Files.readString(file, StandardCharsets.UTF_8)
            );
            if (!parsedJson.isJsonObject()) {
                throw new IOException("Telemetry JSON must contain a JSON object.");
            }
            root = parsedJson.getAsJsonObject();
        } catch (JsonParseException exception) {
            throw new IOException("Could not parse telemetry JSON: " + file, exception);
        }

        JsonObject session = objectField(root, "session");
        String sessionId = stringField(session, "sessionId");
        String studentId = nullableStringField(session, "studentId");
        long startMs = longField(session, "startMs");
        long endMs = longField(session, "endMs");

        BufferedEventSink source = new BufferedEventSink(
            new SessionMetadata(sessionId, studentId, startMs, endMs)
        );
        JsonArray events = arrayField(root, "events");
        Gson gson = new Gson();

        for (JsonElement element : events) {
            if (!element.isJsonObject()) {
                throw new IOException("Each telemetry event must be a JSON object.");
            }
            JsonObject event = element.getAsJsonObject();
            EventType type = eventType(event);
            long timestampMs = longField(event, "timestampMs");
            long sessionOffsetMs = longField(event, "sessionOffsetMs");
            JsonObject eventData = objectField(event, "data");
            Map<String, Object> data = gson.fromJson(eventData, DATA_MAP_TYPE);
            source.accept(new TelemetryEvent(type, timestampMs, sessionOffsetMs, data));
        }

        return source;
    }

    private JsonObject objectField(JsonObject object, String name) throws IOException {
        JsonElement value = object.get(name);
        if (value == null || !value.isJsonObject()) {
            throw new IOException("Missing or invalid JSON object field: " + name);
        }
        return value.getAsJsonObject();
    }

    private JsonArray arrayField(JsonObject object, String name) throws IOException {
        JsonElement value = object.get(name);
        if (value == null || !value.isJsonArray()) {
            throw new IOException("Missing or invalid JSON array field: " + name);
        }
        return value.getAsJsonArray();
    }

    private String stringField(JsonObject object, String name) throws IOException {
        JsonElement value = object.get(name);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IOException("Missing or invalid JSON string field: " + name);
        }
        return value.getAsString();
    }

    private String nullableStringField(JsonObject object, String name) throws IOException {
        JsonElement value = object.get(name);
        if (value == null || value.isJsonNull()) {
            return null;
        }
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IOException("Invalid JSON string field: " + name);
        }
        return value.getAsString();
    }

    private long longField(JsonObject object, String name) throws IOException {
        JsonElement value = object.get(name);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new IOException("Missing or invalid JSON number field: " + name);
        }
        try {
            return value.getAsLong();
        } catch (NumberFormatException exception) {
            throw new IOException("Invalid JSON number field: " + name, exception);
        }
    }

    private EventType eventType(JsonObject event) throws IOException {
        String name = stringField(event, "type");
        try {
            return EventType.valueOf(name);
        } catch (IllegalArgumentException exception) {
            throw new IOException("Unknown telemetry event type: " + name, exception);
        }
    }
}
