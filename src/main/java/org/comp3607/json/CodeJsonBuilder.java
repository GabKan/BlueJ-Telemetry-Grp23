package org.comp3607.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.comp3607.api.EventSource;
import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;

public class CodeJsonBuilder {

    public Map<String, Object> build(EventSource source) {
        List<TelemetryEvent> events = source.getEvents();

        int created = 0;
        int deleted = 0;
        int modified = 0;

        for (TelemetryEvent event : events) {
            switch (event.getType()) {
                case CLASS_CREATED:
                    created++;
                    break;
                case CLASS_DELETED:
                    deleted++;
                    break;
                case CLASS_MODIFIED:
                    modified++;
                    break;
                default:
                    break;
            }
        }

        Map<String, Object> code = new LinkedHashMap<>();
        code.put("classes_created", created);
        code.put("classes_deleted", deleted);
        code.put("classes_modified", modified);
        return code;
    }
}
