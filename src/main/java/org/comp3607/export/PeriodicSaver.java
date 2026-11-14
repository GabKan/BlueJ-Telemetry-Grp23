package org.comp3607.export;

import org.comp3607.api.EventSource;

public class PeriodicSaver implements AutoCloseable {

    private final JsonFileExporter exporter;
    private final EventSource source;
    private final long intervalMs;
    private boolean running = true;

    public PeriodicSaver(JsonFileExporter exporter, EventSource source, long intervalMs) {
        this.exporter = exporter;
        this.source = source;
        this.intervalMs = intervalMs;
        new Thread(this::saveLoop).start();
    }
    private void saveLoop() {
        while (running) {
            try {
                exporter.export(source);
            } catch (Exception e) {
            }
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    @Override
    public void close() {
        running = false;
    }
}
