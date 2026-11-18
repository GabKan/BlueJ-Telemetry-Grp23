package org.comp3607.bluej;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.comp3607.collector.BufferedEventSink;
import org.comp3607.collector.ClassEventListener;
import org.comp3607.collector.CompileEventListener;
import org.comp3607.collector.ExecutionEventListener;
import org.comp3607.collector.InteractionEventListener;
import org.comp3607.collector.TelemetryEventFactory;
import org.comp3607.export.ExportFileNamer;
import org.comp3607.export.JsonFileExporter;
import org.comp3607.json.TelemetryJsonSerialiser;
import org.comp3607.model.SessionMetadata;
import org.comp3607.report.JsonReportGenerator;
import org.comp3607.report.PdfReportGenerator;

import bluej.extensions2.BlueJ;
import bluej.extensions2.Extension;

public class TelemetryExtension extends Extension {
    private long sessionStartMs;
    private String sessionId;
    private BufferedEventSink eventSource;
    private BlueJApiAdapter adapter;
    private BlueJEditorActivityTracker editorActivityTracker;

    static void generateReportsForSession(
        org.comp3607.api.EventSource source,
        java.nio.file.Path projectDirectory,
        String sessionId
    ) throws IOException {
        generateReportsForSession(source, projectDirectory, sessionId, null);
    }

    static void generateReportsForSession(
        org.comp3607.api.EventSource source,
        java.nio.file.Path projectDirectory,
        String sessionId,
        Map<String, Object> editorActivity
    ) throws IOException {
        if (source == null || projectDirectory == null) {
            return;
        }

        Path jsonReport = projectDirectory.resolve("json_reports")
            .resolve(JsonReportGenerator.reportFileName(sessionId));
        new JsonReportGenerator().export(source, jsonReport);

        new PdfReportGenerator().exportQuietly(
            source, editorActivity, projectDirectory.resolve("pdf_reports")
        );
    }

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
        adapter.rememberOpenProjectDirectory();
        adapter.registerEventListeners();

        editorActivityTracker = new BlueJEditorActivityTracker(bluej);
        editorActivityTracker.start();

        System.out.println("BlueJ Telemetry extension started");
    }

    // BlueJ Extension API lifecycle hook.
    @Override
    public void terminate() {
        if (adapter != null)
            adapter.unregisterEventListeners();

        if (editorActivityTracker != null) {
            editorActivityTracker.stop();
        }

        if (eventSource != null) {
            long sessionEndMs = System.currentTimeMillis();
            eventSource.updateMetadata(
                new SessionMetadata(
                    sessionId, "unknown", sessionStartMs, sessionEndMs
                )
            );

            File observedProjectDirectory = null;
            if (editorActivityTracker != null) {
                observedProjectDirectory = editorActivityTracker.getProjectDirectory();
            }
            if (observedProjectDirectory == null && adapter != null) {
                observedProjectDirectory = adapter.getProjectDirectory();
            }

            if (observedProjectDirectory != null) {
                Path projectDirectory = observedProjectDirectory.toPath();
                Path telemetryFile = projectDirectory.resolve(".telemetry")
                    .resolve(new ExportFileNamer().nameFor(eventSource.metadata()));
                try {
                    JsonFileExporter jsonExporter = new JsonFileExporter(
                        new TelemetryJsonSerialiser(),
                        new ExportFileNamer(),
                        projectDirectory.resolve(".telemetry")
                    );
                    jsonExporter.export(
                        eventSource,
                        telemetryFile,
                        editorActivityTracker == null
                            ? null : editorActivityTracker.getEditorActivity()
                    );
                } catch (IOException exception) {
                    System.err.println(
                        "BlueJ Telemetry could not save session telemetry: "
                            + exception.getMessage()
                    );
                    return;
                }

                Map<String, Object> editorActivity = null;
                if (editorActivityTracker != null) {
                    editorActivity = editorActivityTracker.getEditorActivity();
                }

                try {
                    generateReportsForSession(
                        eventSource, projectDirectory, sessionId, editorActivity
                    );
                } catch (IOException exception) {
                    System.err.println(
                        "BlueJ Telemetry could not create the session reports from the current telemetry: "
                            + exception.getMessage()
                    );
                }
            } else {
                System.err.println(
                    "BlueJ Telemetry could not create reports because no project directory "
                        + "was observed."
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