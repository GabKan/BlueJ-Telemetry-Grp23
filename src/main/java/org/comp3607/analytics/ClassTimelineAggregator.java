package org.comp3607.analytics;

import java.util.ArrayList;
import java.util.List;
import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;

// Creates a timeline of class created, modified and deleted events

public class ClassTimelineAggregator {

    public List<String> calculate(List<TelemetryEvent> events) {
        List<String> timeline = new ArrayList<String>();

        for (TelemetryEvent event : events) {
            String action = actionFor(event.getType());
            if (action != null) {
                String className = String.valueOf(event.getData().get("className"));
                String time = TimeFormatter.TimeFormat(event.getSessionOffsetMs());
                timeline.add(time + " - " + action + " " + className);
            }
        }
        return timeline;
    }

    // Returns the different actions for the different class events or return null if not a class event

    private String actionFor(EventType type) {
        if (type == EventType.CLASS_CREATED) {
            return "Created";
        }
        if (type == EventType.CLASS_MODIFIED) {
            return "Modified";
        }
        if (type == EventType.CLASS_DELETED) {
            return "Deleted";
        }
        return null;
    }
}