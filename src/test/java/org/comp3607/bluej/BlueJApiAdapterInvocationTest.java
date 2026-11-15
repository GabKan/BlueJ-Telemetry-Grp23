package org.comp3607.bluej;

import static org.junit.jupiter.api.Assertions.*;

import org.comp3607.collector.*;
import org.comp3607.model.*;
import org.junit.jupiter.api.Test;

import bluej.debugmgr.*;
import bluej.extensions2.event.*;

class BlueJApiAdapterInvocationTest {
    @Test
    void recordsExceptionalInvocation() {
        BufferedEventSink source = BlueJApiAdapterTestSupport.eventSource();
        ExecutionEvent execution = new ExecutionEvent(null, "Application", null);
        execution.setMethodName("main");
        execution.setResult(ExecutionEvent.Result.EXCEPTION_EXIT);
        BlueJApiAdapterTestSupport.adapter(source)
            .invocationFinished(new InvocationFinishedEvent(execution));
        TelemetryEvent event = source.snapshot().get(0);
        assertEquals(EventType.EXECUTION, event.getType());
        assertEquals("EXCEPTION_EXIT", event.getData().get("exception"));
    }

    @Test
    void recordsNormalInvocationWithoutAnException() {
        BufferedEventSink source = BlueJApiAdapterTestSupport.eventSource();
        ExecutionEvent execution = new ExecutionEvent(null, "Application", null);
        execution.setMethodName("main");
        execution.setResult(ExecutionEvent.Result.NORMAL_EXIT);
        BlueJApiAdapterTestSupport.adapter(source)
            .invocationFinished(new InvocationFinishedEvent(execution));
        TelemetryEvent event = source.snapshot().get(0);
        assertEquals(EventType.EXECUTION, event.getType());
        assertEquals("Application", event.getData().get("className"));
        assertNull(event.getData().get("exception"));
    }
}
