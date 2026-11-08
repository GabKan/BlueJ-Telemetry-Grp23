package org.comp3607.collector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;

import org.comp3607.model.EventType;
import org.comp3607.model.TelemetryEvent;
import org.junit.jupiter.api.Test;

class TelemetryEventFactoryTest {
    @Test
    void createsACompileStartEvent() {
        TelemetryEventFactory factory = new TelemetryEventFactory();
        Map<String, Object> data = Map.of("success", true);

        TelemetryEvent event = factory.createCompileEvent(
            EventType.COMPILE_START, 100L, 10L, data
        );

        assertEquals(EventType.COMPILE_START, event.getType());
        assertEquals(data, event.getData());
    }

    @Test
    void createsACompileFinishEvent() {
        TelemetryEventFactory factory = new TelemetryEventFactory();

        TelemetryEvent event = factory.createCompileEvent(
            EventType.COMPILE_FINISH, 200L, 110L, Map.of("success", false)
        );

        assertEquals(EventType.COMPILE_FINISH, event.getType());
        assertEquals(false, event.getData().get("success"));
    }

    @Test
    void doesNotAllowOtherEventTypes() {
        TelemetryEventFactory factory = new TelemetryEventFactory();
        Map<String, Object> data = new HashMap<>();

        try {
            factory.createCompileEvent(EventType.EXECUTION, 100L, 10L, data);
            fail("Expected an IllegalArgumentException for an execution event");
        } catch (IllegalArgumentException expectedException) {
            assertEquals(
                "Not a compilation event type: EXECUTION",
                expectedException.getMessage()
            );
        }
    }
}
