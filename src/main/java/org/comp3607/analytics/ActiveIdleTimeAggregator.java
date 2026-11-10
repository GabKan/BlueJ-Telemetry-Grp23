package org.comp3607.analytics;

import java.util.*;
import org.comp3607.model.TelemetryEvent;

// Splits the session into active time and idle time using the gaps between events

public class ActiveIdleTimeAggregator {

    public static final String ACTIVE_KEY = "active";
    public static final String IDLE_KEY = "idle";

// A gap with out any activity for 60000 milliseconds counts as idle.
    public static final long IDLE_LIMIT_MS = 60000;

// Returns a map with the keys active and idle

    public Map<String, Long> calculate(List<TelemetryEvent> events) {
        long activeTime = 0;
        long idleTime = 0;

        for (int i = 1; i < events.size(); i++) {
            long before = events.get(i - 1).getTimestampMs();
            long after = events.get(i).getTimestampMs();
            long gap = after - before;

            if (gap > IDLE_LIMIT_MS) {
                idleTime = idleTime + gap;
            } else {
                activeTime = activeTime + gap;
            }
        }

        Map<String, Long> result = new HashMap<String, Long>();
        result.put(ACTIVE_KEY, activeTime);
        result.put(IDLE_KEY, idleTime);
        return result;
    }
}