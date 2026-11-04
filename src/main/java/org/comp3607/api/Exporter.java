package org.comp3607.api;

import java.io.IOException;
import java.nio.file.Path;

public interface Exporter {
    void export(EventSource source, Path target) throws IOException;
}
