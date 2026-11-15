package org.comp3607.bluej;

import java.io.IOException;
import java.nio.file.Path;

import org.comp3607.collector.*;
import org.comp3607.export.ExportFileNamer;
import org.comp3607.export.JsonFileExporter;
import org.comp3607.json.TelemetryJsonReader;
import org.comp3607.json.TelemetryJsonSerialiser;
import org.comp3607.model.SessionMetadata;
import org.comp3607.report.JsonReportGenerator;
import org.comp3607.report.PdfReportGenerator;

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
                Path projectDirectory = adapter.getProjectDirectory().toPath();
                Path telemetryFile = projectDirectory.resolve(".telemetry")
                    .resolve(new ExportFileNamer().nameFor(eventSource.metadata()));
                try {
                    JsonFileExporter jsonExporter = new JsonFileExporter(
                        new TelemetryJsonSerialiser(),
                        new ExportFileNamer(),
                        projectDirectory.resolve(".telemetry")
                    );
                    jsonExporter.export(eventSource, telemetryFile);
                } catch (IOException exception) {
                    System.err.println(
                        "BlueJ Telemetry could not save session telemetry: "
                            + exception.getMessage()
                    );
                    return;
                }

                BufferedEventSink savedSource;
                try {
                    savedSource = new TelemetryJsonReader().read(telemetryFile);
                } catch (IOException exception) {
                    System.err.println(
                        "BlueJ Telemetry could not read session telemetry for reports: "
                            + exception.getMessage()
                    );
                    return;
                }

                Path jsonReport = projectDirectory.resolve("json_reports")
                    .resolve(JsonReportGenerator.reportFileName(sessionId));
                try {
                    new JsonReportGenerator().export(savedSource, jsonReport);
                } catch (IOException exception) {
                    System.err.println(
                        "BlueJ Telemetry could not create the JSON analysis report: "
                            + exception.getMessage()
                    );
                }

                Path pdfReport = projectDirectory.resolve("pdf_reports")
                    .resolve(PdfReportGenerator.reportFileName(sessionId));
                try {
                    new PdfReportGenerator().export(savedSource, pdfReport);
                } catch (IOException exception) {
                    System.err.println(
                        "BlueJ Telemetry could not create the PDF report: "
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
