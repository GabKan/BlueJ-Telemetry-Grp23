package org.comp3607.api;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Contract for exporting an event source to a target path.
 */
public interface Exporter {
    /**
     * Exports the supplied session data.
     *
     * @param source source of events and session metadata
     * @param target destination path for the exported output
     * @throws IOException if the output cannot be written
     */
    void export(EventSource source, Path target) throws IOException;
}
