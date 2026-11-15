package org.comp3607.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.comp3607.api.EventSource;

public class TelemetryJsonSerialiser {

    public String serialise(EventSource source) {
        Map<String, Object> json = new HashMap<>();
        json.putAll(new SessionJsonBuilder().build(source));
        json.putAll(new EventsJsonBuilder().build(source));

        StringBuilder output = new StringBuilder();
        appendValue(output, json, 0);
        return output.toString();
    }

    private void appendValue(StringBuilder output, Object value, int indentation) {
        if (value == null) {
            output.append("null");
        } else if (value instanceof String) {
            appendString(output, (String) value);
        } else if (value instanceof Number || value instanceof Boolean) {
            output.append(value);
        } else if (value instanceof Map<?, ?>) {
            appendObject(output, (Map<?, ?>) value, indentation);
        } else if (value instanceof List<?>) {
            appendArray(output, (List<?>) value, indentation);
        } else {
            throw new IllegalArgumentException(
                "Unsupported value in telemetry JSON: " + value.getClass().getName()
            );
        }
    }

    private void appendObject(StringBuilder output, Map<?, ?> values, int indentation) {
        output.append('{');
        if (!values.isEmpty()) {
            output.append('\n');
            int index = 0;
            for (Map.Entry<?, ?> entry : values.entrySet()) {
                appendIndentation(output, indentation + 1);
                appendString(output, String.valueOf(entry.getKey()));
                output.append(": ");
                appendValue(output, entry.getValue(), indentation + 1);
                if (index < values.size() - 1) {
                    output.append(',');
                }
                output.append('\n');
                index++;
            }
            appendIndentation(output, indentation);
        }
        output.append('}');
    }

    private void appendArray(StringBuilder output, List<?> values, int indentation) {
        output.append('[');
        if (!values.isEmpty()) {
            output.append('\n');
            for (int index = 0; index < values.size(); index++) {
                appendIndentation(output, indentation + 1);
                appendValue(output, values.get(index), indentation + 1);
                if (index < values.size() - 1) {
                    output.append(',');
                }
                output.append('\n');
            }
            appendIndentation(output, indentation);
        }
        output.append(']');
    }

    private void appendString(StringBuilder output, String value) {
        output.append('"');
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (character == '"') {
                output.append("\\\"");
            } else if (character == '\\') {
                output.append("\\\\");
            } else if (character == '\b') {
                output.append("\\b");
            } else if (character == '\f') {
                output.append("\\f");
            } else if (character == '\n') {
                output.append("\\n");
            } else if (character == '\r') {
                output.append("\\r");
            } else if (character == '\t') {
                output.append("\\t");
            } else if (character < 0x20) {
                output.append(String.format("\\u%04x", (int) character));
            } else {
                output.append(character);
            }
        }
        output.append('"');
    }

    private void appendIndentation(StringBuilder output, int indentation) {
        for (int index = 0; index < indentation; index++) {
            output.append("  ");
        }
    }
}
