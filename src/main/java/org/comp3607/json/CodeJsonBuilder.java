package org.comp3607.json;

import java.util.HashMap;
import java.util.Map;

import org.comp3607.api.EventSource;
import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;

public class CodeJsonBuilder {

    public Map<String, Object> build(EventSource source) {
        int created = 0;
        int deleted = 0;
        int modified = 0;

        for (TelemetryEvent event : source.snapshot()) {
            if (event.getType() == EventType.CLASS_CREATED) {
                created++;
            } else if (event.getType() == EventType.CLASS_DELETED) {
                deleted++;
            } else if (event.getType() == EventType.CLASS_MODIFIED) {
                modified++;
            }
        }

        Map<String, Object> code = new HashMap<>();
        code.put("classes_created", created);
        code.put("classes_deleted", deleted);
        code.put("classes_modified", modified);
        return code;
    }
}
