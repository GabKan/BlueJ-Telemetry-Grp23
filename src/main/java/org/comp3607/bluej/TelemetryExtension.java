package org.comp3607.bluej;

import org.comp3607.collector.BufferedEventSink;
import org.comp3607.collector.CompileEventListener;
import org.comp3607.collector.FileLoggingEventSink;
import org.comp3607.collector.TelemetryEventFactory;
import org.comp3607.model.SessionMetadata;

import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;

public class TelemetryExtension extends Extension {
    private long sessionStartMs;
    private String sessionId;
    private BufferedEventSink eventSource;
    private FileLoggingEventSink eventSink;
    private BlueJApiAdapter adapter;

    // BlueJ Extension API lifecycle hook.
    @Override
    public void startup(BlueJ bluej) {
        sessionStartMs = System.currentTimeMillis();
        sessionId = "session-" + sessionStartMs;

        SessionMetadata metadata = new SessionMetadata(sessionId, "unknown", sessionStartMs, 0L);
        eventSource = new BufferedEventSink(metadata);
        eventSink = new FileLoggingEventSink(eventSource);

        CompileEventListener compileListener = new CompileEventListener(
            eventSink, new TelemetryEventFactory()
        );

        adapter = new BlueJApiAdapter(bluej, compileListener, eventSink, sessionStartMs);
        adapter.registerEventListeners();

        System.out.println("BlueJ Telemetry extension started");
    }

    // BlueJ Extension API lifecycle hook.
    @Override
    public void terminate() {
        if (adapter != null)
            adapter.unregisterEventListeners();

        if (eventSource != null) {
            long sessionEndMs = System.currentTimeMillis();
            eventSource.updateMetadata(
                new SessionMetadata(
                    sessionId, "unknown", sessionStartMs, sessionEndMs
                )
            );
        }

        System.out.println("BlueJ Telemetry extension stopped");
    }

    // BlueJ Extension API compatibility hook.
    @Override
    public boolean isCompatible() {
        return getExtensionsAPIVersionMajor() >= 3;
    }

    // BlueJ Extension API metadata hook.
    @Override
    public String getName() {
        return "BlueJ Telemetry";
    }

    // BlueJ Extension API metadata hook.
    @Override
    public String getVersion() {
        return "1.0";
    }

    // BlueJ Extension API metadata hook.
    @Override
    public String getDescription() {
        return "Records basic compilation events for a BlueJ session.";
    }
}
