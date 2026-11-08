package org.comp3607.bluej;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.comp3607.collector.CompileEventListener;
import org.comp3607.collector.FileLoggingEventSink;

import bluej.extensions2.BlueJ;
import bluej.extensions2.event.CompileEvent;
import bluej.extensions2.event.CompileListener;

public class BlueJApiAdapter implements BlueJAdapter, CompileListener {
    private final BlueJ bluej;
    private final CompileEventListener telemetryListener;
    private final FileLoggingEventSink eventSink;
    private final long sessionStartMs;
    private final List<Map<String, Object>> errors = new ArrayList<>();

    public BlueJApiAdapter(
        BlueJ bluej,
        CompileEventListener telemetryListener,
        FileLoggingEventSink eventSink,
        long sessionStartMs
    ) {
        this.bluej = bluej;
        this.telemetryListener = telemetryListener;
        this.eventSink = eventSink;
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

    // BlueJ CompileListener callback.
    @Override
    public void compileStarted(CompileEvent event) {
        errors.clear();
        long timestampMs = System.currentTimeMillis();
        File[] files = event.getFiles();
        eventSink.setLogFile(findTelemetryLogFile(files));

        if (files == null || files.length == 0) {
            recordCompileStart("unknown", timestampMs);
            return;
        }

        for (File file : files) {
            recordCompileStart(getClassName(file), timestampMs);
        }
    }

    // BlueJ CompileListener callback.
    @Override
    public void compileError(CompileEvent event) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", null);
        error.put("message", event.getErrorMessage());
        error.put("lineNumber", event.getErrorLineNumber());
        errors.add(error);
    }

    // BlueJ CompileListener callback.
    @Override
    public void compileWarning(CompileEvent event) {
    }

    // BlueJ CompileListener callback.
    @Override
    public void compileSucceeded(CompileEvent event) {
        recordCompileFinish(true);
    }

    // BlueJ CompileListener callback.
    @Override
    public void compileFailed(CompileEvent event) {
        recordCompileFinish(false);
    }

    private void recordCompileStart(String className, long timestampMs) {
        telemetryListener.onCompileStarted(className, timestampMs, timestampMs - sessionStartMs);
    }

    private void recordCompileFinish(boolean success) {
        long timestampMs = System.currentTimeMillis();
        telemetryListener.onCompileFinished(
            success, errors, timestampMs, timestampMs - sessionStartMs
        );
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

    private String getClassName(File file) {
        String fileName = file.getName();
        int extensionPosition = fileName.lastIndexOf('.');
        if (extensionPosition > 0) {
            return fileName.substring(0, extensionPosition);
        }
        return fileName;
    }
}
