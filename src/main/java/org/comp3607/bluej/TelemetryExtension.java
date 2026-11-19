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

/**
 * BlueJ extension entry point that collects session telemetry and exports
 * telemetry data and reports when BlueJ closes the extension.
 */
public class TelemetryExtension extends Extension {
    /** Wall-clock time, in milliseconds since the epoch, when this extension started. */
    private long sessionStartMs;
    /** Identifier shared by the telemetry events and reports for this extension session. */
    private String sessionId;
    /** In-memory event source populated by the registered telemetry listeners. */
    private BufferedEventSink eventSource;
    /** Adapter that connects BlueJ callbacks to the telemetry listeners. */
    private BlueJApiAdapter adapter;
    /** Tracker that samples editor activity and remembers the active project directory. */
    private BlueJEditorActivityTracker editorActivityTracker;

    /**
     * Writes the JSON and PDF reports for a completed telemetry session.
     *
     * @param source source of the session events and metadata
     * @param projectDirectory directory in which report folders are created
     * @param sessionId identifier used to name the JSON report
     * @param editorActivity optional editor-activity data to include in the reports
     * @throws IOException if writing the JSON report fails
     */
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
        new JsonReportGenerator().export(source, editorActivity, jsonReport);

        new PdfReportGenerator().exportQuietly(
            source, editorActivity, projectDirectory.resolve("pdf_reports")
        );
    }

    /**
     * Initializes session state, registers BlueJ event listeners, and starts
     * editor-activity tracking.
     *
     * @param bluej BlueJ application instance that owns this extension
     */
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

    /**
     * Stops tracking, updates session metadata, and saves the telemetry and
     * generated reports to the observed BlueJ project directory.
     */
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

    /**
     * Indicates whether this extension can run with the installed BlueJ API.
     *
     * @return {@code true} when the BlueJ extension API major version is at least 3
     */
    @Override
    public boolean isCompatible() {
        return getExtensionsAPIVersionMajor() >= 3;
    }

    /**
     * Returns the display name of this extension.
     *
     * @return extension name
     */
    @Override
    public String getName() {
        return "BlueJ Telemetry";
    }

    /**
     * Returns the version reported to BlueJ.
     *
     * @return extension version
     */
    @Override
    public String getVersion() {
        return "1.0";
    }

    /**
     * Returns the description shown by BlueJ for this extension.
     *
     * @return extension description
     */
    @Override
    public String getDescription() {
        return "Records compilation and execution events for a BlueJ session.";
    }
}