package org.comp3607.collector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

import org.comp3607.api.EventSink;
import org.comp3607.model.*;

public class FileLoggingEventSink implements EventSink {
    private final EventSink eventSink;
    private File logFile;

    public FileLoggingEventSink(EventSink eventSink) {
        this.eventSink = eventSink;
    }

    public void setLogFile(File logFile) {
        this.logFile = logFile;
    }

    @Override
    public void accept(TelemetryEvent event) {
        eventSink.accept(event);

        if (logFile == null) {
            System.err.println("BlueJ Telemetry could not find a project log file.");
            return;
        }

        try {
            Files.writeString(
                logFile.toPath(),
                formatEvent(event) + System.lineSeparator(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
        } catch (IOException exception) {
            System.err.println(
                "BlueJ Telemetry could not write " + logFile + ": " + exception.getMessage()
            );
        }
    }

    private String formatEvent(TelemetryEvent event) {
        Map<String, Object> data = event.getData();
        String prefix = " timestampMs=" + event.getTimestampMs()
            + " sessionOffsetMs=" + event.getSessionOffsetMs();

        if (event.getType() == EventType.COMPILE_START) {
            return "event=COMPILE_START" + prefix
                + " className=" + quote(String.valueOf(data.get("className")));
        }

        if (event.getType() == EventType.COMPILE_FINISH) {
            boolean success = Boolean.TRUE.equals(data.get("success"));
            StringBuilder output = new StringBuilder("event=COMPILE_FINISH")
                .append(prefix)
                .append(" status=").append(success ? "SUCCEEDED" : "FAILED")
                .append(" success=").append(success);

            Object errorsValue = data.get("errors");
            if (errorsValue instanceof List<?> errors) {
                for (Object errorValue : errors) {
                    if (errorValue instanceof Map<?, ?> error) {
                        output.append(System.lineSeparator())
                            .append("  errorCode=").append(error.get("errorCode"))
                            .append(" lineNumber=").append(error.get("lineNumber"))
                            .append(" message=").append(quote(String.valueOf(error.get("message"))));
                    }
                }
            }
            return output.toString();
        }

        if (event.getType() == EventType.EXECUTION) {
            return "event=EXECUTION" + prefix
                + " className=" + quote(String.valueOf(data.get("className")))
                + " exception=" + data.get("exception");
        }

        if (event.getType() == EventType.EDIT_ACTIVITY) {
            Object names = data.get("classNames");
            return "event=EDIT_ACTIVITY" + prefix
                + " activitySource=" + data.get("activitySource")
                + " classNames=" + quote(String.valueOf(names));
        }

        if (event.getType() == EventType.IDLE_PERIOD) {
            return "event=IDLE_PERIOD" + prefix
                + " durationMs=" + data.get("durationMs")
                + " previousActivity=" + data.get("previousActivity")
                + " resumedActivity=" + data.get("resumedActivity");
        }

        if (event.getType() == EventType.CLASS_CREATED) {
            return "event=CLASS_CREATED" + prefix
                + " className=" + quote(String.valueOf(data.get("className")));
        }

        if (event.getType() == EventType.CLASS_DELETED) {
            return "event=CLASS_DELETED" + prefix
                + " className=" + quote(String.valueOf(data.get("className")));
        }

        if (event.getType() == EventType.CLASS_MODIFIED) {
            return "event=CLASS_MODIFIED" + prefix
                + " className=" + quote(String.valueOf(data.get("className")))
                + " oldName=" + quote(String.valueOf(data.get("oldName")));
        }

        return "event=" + event.getType() + prefix + " data=" + data;
    }

    private String quote(String value) {
        return "\"" + value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\t", "\\t") + "\"";
    }
}
