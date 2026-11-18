package org.comp3607.bluej;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import org.comp3607.collector.*;

import bluej.extensions2.*;
import bluej.extensions2.event.*;
import bluej.extensions2.event.InvocationFinishedEvent.EventType;

public class BlueJApiAdapter implements
    CompileListener, InvocationFinishedListener, ClassListener {
    private final BlueJ bluej;
    private final CompileEventListener compileListener;
    private final ExecutionEventListener executionListener;
    private final ClassEventListener classListener;
    private final InteractionEventListener interactionListener;
    private final long sessionStartMs;
    private final List<Map<String, Object>> errors = new ArrayList<>();
    private final List<String> knownClassNames = new ArrayList<>();
    private final List<String> compilingClassNames = new ArrayList<>();
    private File projectDirectory;

    public BlueJApiAdapter(
        BlueJ bluej,
        CompileEventListener compileListener,
        ExecutionEventListener executionListener,
        ClassEventListener classListener,
        InteractionEventListener interactionListener,
        long sessionStartMs
    ) {
        this.bluej = bluej;
        this.compileListener = compileListener;
        this.executionListener = executionListener;
        this.classListener = classListener;
        this.interactionListener = interactionListener;
        this.sessionStartMs = sessionStartMs;
    }

    public void registerEventListeners() {
        bluej.addCompileListener(this);
        bluej.addInvocationFinishedListener(this);
        bluej.addClassListener(this);
    }

    public void unregisterEventListeners() {
        bluej.removeCompileListener(this);
        bluej.removeInvocationFinishedListener(this);
        bluej.removeClassListener(this);
    }

    /** Records the project that is currently open when the extension starts. */
    public void rememberOpenProjectDirectory() {
        if (bluej == null)
            return;

        try {
            BPackage currentPackage = bluej.getCurrentPackage();
            if (currentPackage != null) {
                projectDirectory = currentPackage.getProject().getDir();
            } else {
                BProject[] projects = bluej.getOpenProjects();
                if (projects.length > 0) {
                    projectDirectory = projects[0].getDir();
                }
            }
            rememberExistingClasses(projectDirectory);
        } catch (ProjectNotOpenException exception) {
            System.err.println(
                "BlueJ Telemetry could not find the open project directory: "
                    + exception.getMessage()
            );
        }
    }

    void rememberExistingClasses(File directory) {
        if (directory == null)
            return;

        try (Stream<Path> files = Files.walk(directory.toPath())) {
            files.filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(fileName -> fileName.endsWith(".java"))
                .map(fileName -> fileName.substring(0, fileName.length() - 5))
                .filter(className -> !knownClassNames.contains(className))
                .forEach(knownClassNames::add);
        } catch (IOException exception) {
            System.err.println(
                "BlueJ Telemetry could not scan existing project classes: "
                    + exception.getMessage()
            );
        } catch (UncheckedIOException exception) {
            System.err.println(
                "BlueJ Telemetry could not finish scanning existing project classes: "
                    + exception.getMessage()
            );
        }
    }

    // BlueJ invocation callback, called after a method or object interaction finishes.
    @Override
    public void invocationFinished(InvocationFinishedEvent event) {
        rememberExecutionProjectDirectory(event);
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
        rememberClassProjectDirectory(event);
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
        rememberClassProjectDirectory(event);
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

    private void rememberClassProjectDirectory(ClassEvent event) {
        if (bluej == null)
            return;

        try {
            BPackage classPackage = event.getPackage();
            if (classPackage != null) {
                projectDirectory = classPackage.getProject().getDir();
            }
        } catch (ProjectNotOpenException exception) {
            System.err.println(
                "BlueJ Telemetry could not find the class event project directory: "
                    + exception.getMessage()
            );
        }
    }

    private void rememberExecutionProjectDirectory(InvocationFinishedEvent event) {
        if (bluej == null)
            return;

        try {
            BPackage invocationPackage = event.getPackage();
            if (invocationPackage != null) {
                projectDirectory = invocationPackage.getProject().getDir();
            }
        } catch (ProjectNotOpenException exception) {
            System.err.println(
                "BlueJ Telemetry could not find the execution project directory: "
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
        rememberProjectDirectory(files);

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

    private void rememberProjectDirectory(File[] files) {
        if (files == null || files.length == 0)
            return;

        File directory = files[0].getAbsoluteFile().getParentFile();
        while (directory != null) {
            File projectSettings = new File(directory, "package.bluej");

            if (projectSettings.isFile()) {
                projectDirectory = directory;
                return;
            }

            directory = directory.getParentFile();
        }
    }

    public File getProjectDirectory() {
        return projectDirectory;
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
