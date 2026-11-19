package org.comp3607.bluej;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import bluej.extensions2.BClass;
import bluej.extensions2.BPackage;
import bluej.extensions2.BProject;
import bluej.extensions2.BlueJ;
import bluej.extensions2.PackageNotFoundException;
import bluej.extensions2.ProjectNotOpenException;
import bluej.extensions2.editor.JavaEditor;

/**
 * Samples visible BlueJ Java editors and summarizes text-length changes as
 * typing, paste, deletion, and idle activity. It records lengths and timing,
 * not source text or keystrokes.
 */
public class BlueJEditorActivityTracker {

    private static final long SAMPLE_MS = 250;
    private static final long IDLE_GAP_MS = 2000;
    private static final long FX_TIMEOUT_MS = 1000;
    private static final int PASTE_CHARS = 20;
    private static final int MAX_SEGMENTS = 5000;

    private final BlueJ bluej;
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, Sample> previousSamples = new HashMap<>();
    private final Map<String, Segment> openSegments = new HashMap<>();
    private final List<Map<String, Object>> segments = new ArrayList<>();
    private final Map<String, Long> counts = new HashMap<>();
    private final Map<String, Long> durations = new HashMap<>();
    private final Map<String, Long> characters = new HashMap<>();
    private volatile File projectDirectory;
    private long observedMs;
    private long longestIdleMs;
    private long editCount;
    private boolean segmentsTruncated;
    private boolean stopped;

    /**
     * Creates a tracker for the supplied BlueJ application.
     *
     * @param bluej BlueJ application whose open projects and editors are sampled
     */
    public BlueJEditorActivityTracker(BlueJ bluej) {
        this.bluej = bluej;
        counts.put("typing", 0L);
        counts.put("paste", 0L);
        counts.put("deletion", 0L);
        counts.put("idle", 0L);
        durations.putAll(counts);
        characters.putAll(counts);
    }

    /**
     * Starts periodic sampling of visible Java editors.
     */
    public void start() {
        timer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                pollEditors();
            }
        }, 0, SAMPLE_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops sampling and closes any activity segments that are still open.
     * Repeated calls after the first have no effect.
     */
    public synchronized void stop() {
        if (stopped) {
            return;
        }
        stopped = true;
        timer.shutdownNow();
        closeAllSegments();
    }

    /**
     * Returns the most recently observed BlueJ project directory.
     *
     * @return project directory, or {@code null} if none has been observed
     */
    public synchronized File getProjectDirectory() {
        return projectDirectory;
    }

    /**
     * Returns a snapshot of the editor-activity segments and aggregate totals.
     *
     * @return map containing sampling metadata, segments, and summary totals
     */
    public synchronized Map<String, Object> getEditorActivity() {
        Map<String, Object> activity = new HashMap<>();
        activity.put("pollIntervalMs", SAMPLE_MS);
        activity.put("pasteThresholdChars", PASTE_CHARS);
        activity.put("gapToleranceMs", IDLE_GAP_MS);
        activity.put("segmentsTruncated", segmentsTruncated);

        List<Map<String, Object>> savedSegments = new ArrayList<>();
        for (Map<String, Object> segment : segments) {
            savedSegments.add(new HashMap<>(segment));
        }
        activity.put("segments", savedSegments);

        Map<String, Object> summary = new HashMap<>();
        summary.put("observedMs", observedMs);
        summary.put("typing", totals("typing", false));
        summary.put("paste", totals("paste", false));
        summary.put("deletion", totals("deletion", false));
        summary.put("idle", totals("idle", true));
        summary.put("editCount", editCount);
        activity.put("summary", summary);
        return activity;
    }

    private void pollEditors() {
        try {
            List<Sample> samples = readEditors();
            if (samples == null) {
                return;
            }
            updateSamples(samples);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (Exception exception) {
            System.err.println("BlueJ Telemetry could not sample editors: "
                + exception.getMessage());
        }
    }

    private List<Sample> readEditors() throws Exception {
        Class<?> platform = Class.forName("javafx.application.Platform");
        Method isFxThread = platform.getMethod("isFxApplicationThread");
        if ((Boolean) isFxThread.invoke(null)) {
            return readOpenEditors();
        }

        FutureTask<List<Sample>> task = new FutureTask<>(
            new Callable<List<Sample>>() {
                @Override
                public List<Sample> call() {
                    return readOpenEditors();
                }
            }
        );
        Method runLater = platform.getMethod("runLater", Runnable.class);
        runLater.invoke(null, task);
        try {
            return task.get(FX_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException exception) {
            task.cancel(false);
            return null;
        } catch (InterruptedException exception) {
            task.cancel(false);
            throw exception;
        }
    }

    private List<Sample> readOpenEditors() {
        List<Sample> samples = new ArrayList<>();
        long now = System.currentTimeMillis();
        try {
            for (BProject project : bluej.getOpenProjects()) {
                projectDirectory = project.getDir();
                for (BPackage currentPackage : project.getPackages()) {
                    for (BClass currentClass : currentPackage.getClasses()) {
                        JavaEditor editor = currentClass.getJavaEditorIfOpen();
                        if (editor != null && editor.isVisible()) {
                            samples.add(new Sample(
                                currentClass.getName(),
                                editor.getTextLength(),
                                now
                            ));
                        }
                    }
                }
            }
        } catch (ProjectNotOpenException | PackageNotFoundException exception) {
            System.err.println("BlueJ Telemetry could not inspect an open project: "
                + exception.getMessage());
        }
        return samples;
    }

    private synchronized void updateSamples(List<Sample> samples) {
        if (stopped) {
            return;
        }
        Map<String, Boolean> seen = new HashMap<>();
        for (Sample sample : samples) {
            seen.put(sample.className, true);
            recordSample(sample.className, sample.length, sample.time);
        }

        List<String> oldNames = new ArrayList<>(previousSamples.keySet());
        for (String className : oldNames) {
            if (!seen.containsKey(className)) {
                closeSegment(className);
                previousSamples.remove(className);
            }
        }
    }

    synchronized void recordSample(String className, int length, long time) {
        Sample previous = previousSamples.get(className);
        if (previous != null && time > previous.time) {
            int change = length - previous.length;
            String label = "idle";
            if (change > PASTE_CHARS) {
                label = "paste";
            } else if (change > 0) {
                label = "typing";
            } else if (change < 0) {
                label = "deletion";
            }
            addInterval(className, label, previous.time, time - previous.time, change);
        }
        previousSamples.put(className, new Sample(className, length, time));
    }

    private void addInterval(
        String className,
        String label,
        long startMs,
        long durationMs,
        int change
    ) {
        Segment current = openSegments.get(className);
        if ("idle".equals(label)) {
            if (current == null || "idle".equals(current.label)) {
                if (current == null) {
                    current = new Segment(className, "idle", startMs);
                    openSegments.put(className, current);
                }
                current.durationMs += durationMs;
            } else {
                current.pendingIdleMs += durationMs;
                if (current.pendingIdleMs > IDLE_GAP_MS) {
                    long idleStart = current.startMs + current.durationMs;
                    long idleDuration = current.pendingIdleMs;
                    current.pendingIdleMs = 0;
                    closeSegment(className);
                    Segment idle = new Segment(className, "idle", idleStart);
                    idle.durationMs = idleDuration;
                    openSegments.put(className, idle);
                }
            }
            return;
        }

        editCount++;
        if (current != null && !"idle".equals(current.label)) {
            current.durationMs += current.pendingIdleMs;
            current.pendingIdleMs = 0;
            if (current.label.equals(label)) {
                current.durationMs += durationMs;
                current.chars += change;
                return;
            }
        }
        closeSegment(className);
        Segment next = new Segment(className, label, startMs);
        next.durationMs = durationMs;
        next.chars = change;
        openSegments.put(className, next);
    }

    private void closeSegment(String className) {
        Segment segment = openSegments.remove(className);
        if (segment == null) {
            return;
        }
        segment.durationMs += segment.pendingIdleMs;
        segment.pendingIdleMs = 0;
        addToSummary(segment);

        if (segments.size() < MAX_SEGMENTS) {
            Map<String, Object> saved = new HashMap<>();
            saved.put("class", segment.className);
            saved.put("label", segment.label);
            saved.put("startMs", segment.startMs);
            saved.put("durationMs", segment.durationMs);
            saved.put("chars", segment.chars);
            segments.add(saved);
        } else {
            segmentsTruncated = true;
        }
    }

    private void closeAllSegments() {
        List<String> classNames = new ArrayList<>(openSegments.keySet());
        for (String className : classNames) {
            closeSegment(className);
        }
    }

    private void addToSummary(Segment segment) {
        counts.put(segment.label, counts.get(segment.label) + 1);
        durations.put(segment.label, durations.get(segment.label) + segment.durationMs);
        if ("deletion".equals(segment.label)) {
            characters.put(segment.label,
                characters.get(segment.label) + Math.abs(segment.chars));
        } else {
            characters.put(segment.label, characters.get(segment.label) + segment.chars);
        }
        observedMs += segment.durationMs;
        if ("idle".equals(segment.label)) {
            longestIdleMs = Math.max(longestIdleMs, segment.durationMs);
        }
    }

    private Map<String, Object> totals(String label, boolean includeLongest) {
        Map<String, Object> total = new HashMap<>();
        total.put("count", counts.get(label));
        total.put("totalMs", durations.get(label));
        if (includeLongest) {
            total.put("longestMs", longestIdleMs);
        } else {
            total.put("totalChars", characters.get(label));
        }
        return total;
    }

    private static class Sample {
        private final String className;
        private final int length;
        private final long time;

        private Sample(String className, int length, long time) {
            this.className = className;
            this.length = length;
            this.time = time;
        }
    }

    private static class Segment {
        private final String className;
        private final String label;
        private final long startMs;
        private long durationMs;
        private long pendingIdleMs;
        private int chars;

        private Segment(String className, String label, long startMs) {
            this.className = className;
            this.label = label;
            this.startMs = startMs;
        }
    }
}
