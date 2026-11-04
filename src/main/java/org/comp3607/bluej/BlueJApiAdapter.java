package org.comp3607.bluej;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.comp3607.api.EventSource;
import org.comp3607.collector.CompileEventListener;
import org.comp3607.model.TelemetryEvent;

import bluej.extensions2.BlueJ;
import bluej.extensions2.event.CompileEvent;
import bluej.extensions2.event.CompileListener;

public class BlueJApiAdapter implements BlueJAdapter, CompileListener {
    private final BlueJ bluej;
    private final CompileEventListener telemetryListener;
    private final EventSource eventSource;
    private final long sessionStartMs;
    private final List<String> errors = new ArrayList<>();
    private File telemetryLogFile;

    public BlueJApiAdapter(
        BlueJ bluej,
        CompileEventListener telemetryListener,
        EventSource eventSource,
        long sessionStartMs
    ) {
        this.bluej = bluej;
        this.telemetryListener = telemetryListener;
        this.eventSource = eventSource;
        this.sessionStartMs = sessionStartMs;
    }

    @Override
    public void registerEventListeners() {
        bluej.addCompileListener(this);
    }

    @Override
    public void unregisterEventListeners() {
        bluej.removeCompileListener(this);
    }

    @Override
    public void compileStarted(CompileEvent event) {
        errors.clear();
        long timestampMs = System.currentTimeMillis();
        File[] files = event.getFiles();
        telemetryLogFile = findTelemetryLogFile(files);

        if (files == null || files.length == 0) {
            recordCompileStart("unknown", timestampMs);
            return;
        }

        for (File file : files) {
            recordCompileStart(getClassName(file), timestampMs);
        }
    }

    @Override
    public void compileError(CompileEvent event) {
        errors.add("Line " + event.getErrorLineNumber() + ": " + event.getErrorMessage());
    }

    @Override
    public void compileWarning(CompileEvent event) {
    }

    @Override
    public void compileSucceeded(CompileEvent event) {
        recordCompileFinish(true);
    }

    @Override
    public void compileFailed(CompileEvent event) {
        recordCompileFinish(false);
    }

    private void recordCompileStart(String className, long timestampMs) {
        telemetryListener.onCompileStarted(className, timestampMs, timestampMs - sessionStartMs);
        writeLatestEventToLog();
    }

    private void recordCompileFinish(boolean success) {
        long timestampMs = System.currentTimeMillis();
        telemetryListener.onCompileFinished(
            success, errors, timestampMs, timestampMs - sessionStartMs
        );
        writeLatestEventToLog();
    }

    private File findTelemetryLogFile(File[] files) {
        if (files == null || files.length == 0)
            return null;

        File directory = files[0].getAbsoluteFile().getParentFile();
        while (directory != null) {
            File projectSettings = new File(directory, "package.bluej");

            if (projectSettings.isFile())
                return new File(directory, "bluej-telemetry.log");

            directory = directory.getParentFile();
        }
        return null;
    }

    private void writeLatestEventToLog() {
        if (telemetryLogFile == null) {
            System.out.println("BlueJ Telemetry could not find the BlueJ project folder for its log.");
            return;
        }

        List<TelemetryEvent> events = eventSource.snapshot();
        if (events.isEmpty())
            return;

        TelemetryEvent event = events.get(events.size() - 1);
        try (FileWriter output = new FileWriter(telemetryLogFile, true)) {
            output.write("type=" + event.getType()
                    + " timestampMs=" + event.getTimestampMs()
                    + " sessionOffsetMs=" + event.getSessionOffsetMs()
                    + " data=" + event.getData());
            output.write(System.lineSeparator());
        } catch (IOException exception) {
            System.out.println("BlueJ Telemetry could not write its log: " + exception.getMessage());
        }
    }

    private String getClassName(File file) {
        String fileName = file.getName();
        int extensionPosition = fileName.lastIndexOf('.');
        if (extensionPosition > 0) {
            return fileName.substring(0, extensionPosition);
        }
        return fileName;
    }
}
