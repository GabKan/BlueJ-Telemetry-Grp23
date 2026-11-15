package org.comp3607.json;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.comp3607.api.EventSource;
import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;

public class GuiJsonBuilder {

    public Map<String, Object> build(EventSource source) {
        List<TelemetryEvent> events = source.getEvents();

        int editorsOpened = 0;
        int editorsClosed = 0;
        int classesCreated = 0;
        int classesDeleted = 0;
        int classesModified = 0;

        for (TelemetryEvent event : events) {
            switch (event.getType()) {
                case EDITOR_OPEN:
                    editorsOpened++;
                    break;
                case EDITOR_CLOSE:
                    editorsClosed++;
                    break;
                case CLASS_CREATED:
                    classesCreated++;
                    break;
                case CLASS_DELETED:
                    classesDeleted++;
                    break;
                case CLASS_MODIFIED:
                    classesModified++;
                    break;
                default:
                    break;
            }
        }

        Map<String, Object> gui = new LinkedHashMap<>();
        gui.put("editors_opened", editorsOpened);
        gui.put("editors_closed", editorsClosed);
        gui.put("classes_created", classesCreated);
        gui.put("classes_deleted", classesDeleted);
        gui.put("classes_modified", classesModified);
        return gui;
    }
}
