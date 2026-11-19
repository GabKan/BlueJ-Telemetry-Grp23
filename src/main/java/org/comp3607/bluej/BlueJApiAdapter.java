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

/**
 * Bridges BlueJ compile, invocation, and class events to the telemetry
 * listeners, while tracking the current project and its known classes.
 */
public class BlueJApiAdapter implements
    CompileListener, InvocationFinishedListener, ClassListener {
    /** BlueJ application used to register listeners and resolve project information. */
    private final BlueJ bluej;
    /** Listener that converts compile callbacks into telemetry events. */
    private final CompileEventListener compileListener;
    /** Listener that converts completed invocations into telemetry events. */
    private final ExecutionEventListener executionListener;
    /** Listener that records class creation, removal, and rename events. */
    private final ClassEventListener classListener;
    /** Listener that records activity associated with compile and class callbacks. */
    private final InteractionEventListener interactionListener;
    /** Session start time used to calculate offsets for emitted events. */
    private final long sessionStartMs;
    /** Compile errors collected between compile start and completion callbacks. */
    private final List<Map<String, Object>> errors = new ArrayList<>();
    /** Class names known to exist in the active project. */
    private final List<String> knownClassNames = new ArrayList<>();
    /** Class names included in the compile currently in progress. */
    private final List<String> compilingClassNames = new ArrayList<>();
    /** Normalized directory whose Java files were last used to seed the class baseline. */
    private Path classBaselineDirectory;
    /** Most recently identified BlueJ project directory. */
    private File projectDirectory;

    /**
     * Creates an adapter that forwards BlueJ events to the supplied telemetry
     * listeners.
     *
     * @param bluej BlueJ application instance
     * @param compileListener destination for compilation events
     * @param executionListener destination for execution events
     * @param classListener destination for class lifecycle events
     * @param interactionListener destination for interaction activity
     * @param sessionStartMs session start time in milliseconds since the epoch
     */
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

    /**
     * Registers this adapter for compile, invocation, and class events from BlueJ.
     */
    public void registerEventListeners() {
        bluej.addCompileListener(this);
        bluej.addInvocationFinishedListener(this);
        bluej.addClassListener(this);
    }

    /**
     * Removes this adapter from the BlueJ event listener registrations.
     */
    public void unregisterEventListeners() {
        bluej.removeCompileListener(this);
        bluej.removeInvocationFinishedListener(this);
        bluej.removeClassListener(this);
    }

    /**
     * Records the currently open project directory and seeds the known-class
     * baseline from its Java source files.
     */
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

    /**
     * Adds Java source-file names under the supplied directory to the known
     * class baseline. A new baseline is scanned only when the normalized
     * directory differs from the previously scanned one.
     *
     * @param directory project directory to scan; {@code null} is ignored
     */
    void rememberExistingClasses(File directory) {
        if (directory == null)
            return;

        Path baselineDirectory = directory.toPath().toAbsolutePath().normalize();
        if (baselineDirectory.equals(classBaselineDirectory))
            return;

        try (Stream<Path> files = Files.walk(baselineDirectory)) {
            files.filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(fileName -> fileName.endsWith(".java"))
                .map(fileName -> fileName.substring(0, fileName.length() - 5))
                .filter(className -> !knownClassNames.contains(className))
                .forEach(knownClassNames::add);
            classBaselineDirectory = baselineDirectory;
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

    /**
     * Handles a completed BlueJ invocation by recording execution and
     * corresponding interaction activity.
     *
     * @param event BlueJ event describing the completed invocation
     */
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

    /**
     * Handles removal of a class from the project and removes it from the
     * known-class baseline.
     *
     * @param event BlueJ event describing the removed class
     */
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

    /**
     * Receives class compilation-state changes. These callbacks are not treated
     * as source edits, so this implementation intentionally records no event.
     *
     * @param event BlueJ event describing the class state change
     */
    @Override
    public void classStateChanged(ClassEvent event) {
        // This callback reports compilation state, not source modifications.
    }

    /**
     * Handles a class rename, updating the known-class baseline and emitting
     * class and interaction events.
     *
     * @param event BlueJ event describing the renamed class
     */
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

    /**
     * Updates the remembered project directory from a class event when its
     * package and project are available.
     *
     * @param event class event from which to resolve the project directory
     */
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

    /**
     * Updates the remembered project directory from a completed invocation
     * when its package and project are available.
     *
     * @param event invocation event from which to resolve the project directory
     */
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

    /**
     * Starts bookkeeping for a BlueJ compilation, recording its source class
     * names and start activity.
     *
     * @param event BlueJ event containing the files being compiled
     */
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

    /**
     * Adds a compiler error to the collection for the active compilation.
     *
     * @param event BlueJ event containing the compiler message and line number
     */
    @Override
    public void compileError(CompileEvent event) {
        Map<String, Object> error = new HashMap<>();
        error.put("errorCode", null);
        error.put("message", event.getErrorMessage());
        error.put("lineNumber", event.getErrorLineNumber());
        errors.add(error);
    }

    /**
     * Receives compiler warnings. Warnings are currently not included in
     * telemetry.
     *
     * @param event BlueJ warning event
     */
    @Override
    public void compileWarning(CompileEvent event) {
    }

    /**
     * Completes compile bookkeeping after BlueJ reports a successful compile.
     *
     * @param event BlueJ completion event
     */
    @Override
    public void compileSucceeded(CompileEvent event) {
        recordCompileFinish(true);
    }

    /**
     * Completes compile bookkeeping after BlueJ reports a failed compile.
     *
     * @param event BlueJ completion event
     */
    @Override
    public void compileFailed(CompileEvent event) {
        recordCompileFinish(false);
    }

    /**
     * Forwards a compile-start event with both its wall-clock time and offset
     * from the session start.
     *
     * @param className name of the class being compiled
     * @param timestampMs event time in milliseconds since the epoch
     */
    private void recordCompileStart(String className, long timestampMs) {
        compileListener.onCompileStarted(className, timestampMs, timestampMs - sessionStartMs);
    }

    /**
     * Emits the compile result and interaction activity, and adds newly
     * compiled classes to the baseline when compilation succeeds.
     *
     * @param success {@code true} if the compilation succeeded
     */
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

    /**
     * Finds the BlueJ project root containing the supplied source files by
     * searching upward for a {@code package.bluej} marker.
     *
     * @param files source files associated with the compile; {@code null} or
     *              an empty array is ignored
     */
    private void rememberProjectDirectory(File[] files) {
        if (files == null || files.length == 0)
            return;

        File directory = files[0].getAbsoluteFile().getParentFile();
        while (directory != null) {
            File projectSettings = new File(directory, "package.bluej");

            if (projectSettings.isFile()) {
                projectDirectory = directory;
                rememberExistingClasses(directory);
                return;
            }

            directory = directory.getParentFile();
        }
    }

    /**
     * Returns the most recently identified project directory.
     *
     * @return project directory, or {@code null} if none has been identified
     */
    public File getProjectDirectory() {
        return projectDirectory;
    }

    /**
     * Extracts the source file's base name without its final extension.
     *
     * @param file source file whose class name is required
     * @return file name without its final extension, or the full name if it
     *         has no extension
     */
    private String getClassName(File file) {
        String fileName = file.getName();
        int extensionPosition = fileName.lastIndexOf('.');
        if (extensionPosition > 0) {
            return fileName.substring(0, extensionPosition);
        }
        return fileName;
    }
}
