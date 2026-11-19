package org.comp3607.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.comp3607.api.EventSource;
import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;

/**
 * Builds the code section.
 */
public class CodeJsonBuilder implements SectionBuilder {

    @Override
    public String sectionName() {
        return "code";
    }

    /**
     * Class create, delete and modify counts, plus the class names seen.
     */
    @Override
    public Map<String, Object> build(EventSource source) {
        List<TelemetryEvent> events = source.snapshot();

        int classesCreated = 0;
        int classesDeleted = 0;
        int classesModified = 0;
        List<String> classNames = new ArrayList<>();

        for (TelemetryEvent event : events) {
            EventType type = event.getType();
            if (type == EventType.CLASS_CREATED) {
                classesCreated++;
            } else if (type == EventType.CLASS_DELETED) {
                classesDeleted++;
            } else if (type == EventType.CLASS_MODIFIED) {
                classesModified++;
            } else {
                continue;
            }

            Object name = event.getData().get("class_name");
            if (name != null) {
                String text = String.valueOf(name);
                if (!classNames.contains(text)) {
                    classNames.add(text);
                }
            }
        }

        Map<String, Object> code = new HashMap<>();
        code.put("classes_created", classesCreated);
        code.put("classes_deleted", classesDeleted);
        code.put("classes_modified", classesModified);
        code.put("class_names", classNames);

        return code;
    }
}
