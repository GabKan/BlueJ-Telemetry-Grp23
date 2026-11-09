package org.comp3607.bluej;

import java.io.File;
import java.util.*;

import org.comp3607.collector.*;

import bluej.extensions2.*;
import bluej.extensions2.event.*;
import bluej.extensions2.event.InvocationFinishedEvent.EventType;

public class BlueJApiAdapter implements
    BlueJAdapter, CompileListener, InvocationFinishedListener, ClassListener {
    private final BlueJ bluej;
    private final CompileEventListener compileListener;
    private final ExecutionEventListener executionListener;
    private final ClassEventListener classListener;
    private final InteractionEventListener interactionListener;
    private final FileLoggingEventSink eventSink;
    private final long sessionStartMs;
    private final List<Map<String, Object>> errors = new ArrayList<>();
    private final List<String> knownClassNames = new ArrayList<>();
    private final List<String> compilingClassNames = new ArrayList<>();

    public BlueJApiAdapter(
        BlueJ bluej,
        CompileEventListener compileListener,
        ExecutionEventListener executionListener,
        ClassEventListener classListener,
        InteractionEventListener interactionListener,
        FileLoggingEventSink eventSink,
        long sessionStartMs
    ) {
        this.bluej = bluej;
        this.compileListener = compileListener;
        this.executionListener = executionListener;
        this.classListener = classListener;
        this.interactionListener = interactionListener;
        this.eventSink = eventSink;
        this.sessionStartMs = sessionStartMs;
    }

    @Override
    public void registerEventListeners() {
        bluej.addCompileListener(this);
        bluej.addInvocationFinishedListener(this);
        bluej.addClassListener(this);
    }

    @Override
    public void unregisterEventListeners() {
        bluej.removeCompileListener(this);
        bluej.removeInvocationFinishedListener(this);
        bluej.removeClassListener(this);
    }

    // BlueJ invocation callback, called after a method or object interaction finishes.
    @Override
    public void invocationFinished(InvocationFinishedEvent event) {
        setExecutionLogFile(event);
        String exception = event.getEventType() == EventType.EXCEPTION_EXIT
            ? EventType.EXCEPTION_EXIT.name()
            : null;
        long timestampMs = System.currentTimeMillis();
        interactionListener.onActivity(
            "EXECUTION", timestampMs, timestampMs - sessionStartMs
        );
        executionListener.onExecution(
            event.getClassName(),
            exception,
            timestampMs,
            timestampMs - sessionStartMs
        );
    }

    @Override
    public void classRemoved(ClassEvent event) {
        setClassLogFile(event);
        long timestampMs = System.currentTimeMillis();
        String className = event.getBClass().getName();
        knownClassNames.remove(className);
        interactionListener.onActivity(
            "CLASS_DELETED", timestampMs, timestampMs - sessionStartMs
        );
        classListener.onClassDeleted(
            className,
            timestampMs,
            timestampMs - sessionStartMs
        );
    }

    @Override
    public void classStateChanged(ClassEvent event) {
        // This callback reports compilation state, not source modifications.
    }

    @Override
    public void classNameChanged(ClassEvent event) {
        setClassLogFile(event);
        long timestampMs = System.currentTimeMillis();
        String className = event.getBClass().getName();
        knownClassNames.remove(event.getOldName());
        if (!knownClassNames.contains(className)) {
            knownClassNames.add(className);
        }
        interactionListener.onActivity(
            "CLASS_RENAMED", timestampMs, timestampMs - sessionStartMs
        );
        classListener.onClassRenamed(
            className,
            event.getOldName(),
            timestampMs,
            timestampMs - sessionStartMs
        );
    }

    private void setClassLogFile(ClassEvent event) {
        if (bluej == null)
            return;

        try {
            BPackage classPackage = event.getPackage();
            if (classPackage != null) {
                File projectDirectory = classPackage.getProject().getDir();
                eventSink.setLogFile(new File(projectDirectory, "bluej-telemetry.log"));
            }
        } catch (ProjectNotOpenException exception) {
            System.err.println(
                "BlueJ Telemetry could not find the class event project: "
                    + exception.getMessage()
            );
        }
    }

    private void setExecutionLogFile(InvocationFinishedEvent event) {
        if (bluej == null)
            return;

        try {
            BPackage invocationPackage = event.getPackage();
            if (invocationPackage != null) {
                File projectDirectory = invocationPackage.getProject().getDir();
                eventSink.setLogFile(new File(projectDirectory, "bluej-telemetry.log"));
            }
        } catch (ProjectNotOpenException exception) {
            System.err.println(
                "BlueJ Telemetry could not find the execution project: "
                    + exception.getMessage()
            );
        }
    }

    // BlueJ CompileListener callback.
    @Override
    public void compileStarted(CompileEvent event) {
        errors.clear();
        compilingClassNames.clear();
        long timestampMs = System.currentTimeMillis();
        File[] files = event.getFiles();
        eventSink.setLogFile(findTelemetryLogFile(files));

        if (files == null || files.length == 0) {
            interactionListener.onCompileStarted(
                new ArrayList<>(), timestampMs, timestampMs - sessionStartMs
            );
            recordCompileStart("unknown", timestampMs);
            return;
        }

        for (File file : files) {
            String className = getClassName(file);
            if (!compilingClassNames.contains(className)) {
                compilingClassNames.add(className);
            }
            recordCompileStart(className, timestampMs);
        }
        interactionListener.onCompileStarted(
            new ArrayList<>(compilingClassNames), timestampMs, timestampMs - sessionStartMs
        );
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
        compileListener.onCompileStarted(className, timestampMs, timestampMs - sessionStartMs);
    }

    private void recordCompileFinish(boolean success) {
        long timestampMs = System.currentTimeMillis();
        compileListener.onCompileFinished(
            success, errors, timestampMs, timestampMs - sessionStartMs
        );
        interactionListener.onCompileFinished(timestampMs);
        if (success) {
            for (String className : compilingClassNames) {
                if (!knownClassNames.contains(className)) {
                    knownClassNames.add(className);
                    classListener.onClassCreated(
                        className, timestampMs, timestampMs - sessionStartMs
                    );
                }
            }
        }
        compilingClassNames.clear();
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
