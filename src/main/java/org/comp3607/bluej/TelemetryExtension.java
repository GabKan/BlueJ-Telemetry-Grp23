package org.comp3607.bluej;

import java.io.IOException;
import java.nio.file.Path;

import org.comp3607.collector.*;
import org.comp3607.export.ExportFileNamer;
import org.comp3607.export.JsonFileExporter;
import org.comp3607.json.TelemetryJsonSerialiser;
import org.comp3607.model.SessionMetadata;

import bluej.extensions2.*;

public class TelemetryExtension extends Extension {
    private long sessionStartMs;
    private String sessionId;
    private BufferedEventSink eventSource;
    private BlueJApiAdapter adapter;

    // BlueJ Extension API lifecycle hook.
    @Override
    public void startup(BlueJ bluej) {
        sessionStartMs = System.currentTimeMillis();
        sessionId = "session-" + sessionStartMs;

        SessionMetadata metadata = new SessionMetadata(sessionId, "unknown", sessionStartMs, 0L);
        eventSource = new BufferedEventSink(metadata);

        TelemetryEventFactory eventFactory = new TelemetryEventFactory();
        CompileEventListener compileListener = new CompileEventListener(
            eventSource, eventFactory
        );
        ExecutionEventListener executionListener = new ExecutionEventListener(
            eventSource, eventFactory
        );
        ClassEventListener classListener = new ClassEventListener(
            eventSource, eventFactory
        );
        InteractionEventListener interactionListener = new InteractionEventListener(
            eventSource, eventFactory, sessionStartMs
        );

        adapter = new BlueJApiAdapter(
            bluej,
            compileListener,
            executionListener,
            classListener,
            interactionListener,
            sessionStartMs
        );
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

            if (adapter != null && adapter.getProjectDirectory() != null) {
                Path telemetryDirectory = adapter.getProjectDirectory().toPath()
                    .resolve(".telemetry");
                JsonFileExporter exporter = new JsonFileExporter(
                    new TelemetryJsonSerialiser(),
                    new ExportFileNamer(),
                    telemetryDirectory
                );
                try {
                    exporter.export(eventSource);
                } catch (IOException exception) {
                    System.err.println(
                        "BlueJ Telemetry could not save the session JSON: "
                            + exception.getMessage()
                    );
                }
            } else {
                System.err.println(
                    "BlueJ Telemetry could not save the session JSON because no project "
                        + "directory was observed."
                );
            }
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
        return "Records compilation and execution events for a BlueJ session.";
    }
}
