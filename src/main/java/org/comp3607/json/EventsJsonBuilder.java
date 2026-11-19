package org.comp3607.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.comp3607.api.EventSource;
import org.comp3607.model.TelemetryEvent;

public class EventsJsonBuilder implements SectionBuilder {

    @Override
    public String sectionName() {
        return "events";
    }

    @Override
    public Map<String, Object> build(EventSource source) {
        List<Map<String, Object>> events = new ArrayList<>();

        for (TelemetryEvent event : source.snapshot()) {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("type", event.getType().name());
            eventData.put("timestampMs", event.getTimestampMs());
            eventData.put("sessionOffsetMs", event.getSessionOffsetMs());
            eventData.put("data", event.getData());
            events.add(eventData);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("events", events);
        return result;
    }
}
