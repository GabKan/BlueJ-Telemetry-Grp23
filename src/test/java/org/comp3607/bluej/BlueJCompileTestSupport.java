package org.comp3607.bluej;

import java.io.File;
import java.nio.file.Path;

import bluej.extensions2.event.CompileEvent;

final class BlueJCompileTestSupport {
    static void startCompilation(BlueJApiAdapter adapter, Path sourceFile) {
        CompileEvent startEvent = compileEvent(
            CompileEvent.EventType.COMPILE_START_EVENT, sourceFile
        );
        adapter.compileStarted(startEvent);
    }

    static void finishSuccessfulCompilation(BlueJApiAdapter adapter, Path sourceFile) {
        CompileEvent finishEvent = compileEvent(
            CompileEvent.EventType.COMPILE_DONE_EVENT, sourceFile
        );
        adapter.compileSucceeded(finishEvent);
    }

    static void finishFailedCompilation(BlueJApiAdapter adapter, Path sourceFile) {
        CompileEvent finishEvent = compileEvent(
            CompileEvent.EventType.COMPILE_FAILED_EVENT, sourceFile
        );
        adapter.compileFailed(finishEvent);
    }

    static void recordCompileError(BlueJApiAdapter adapter, Path sourceFile) {
        CompileEvent errorEvent = compileEvent(
            CompileEvent.EventType.COMPILE_ERROR_EVENT, sourceFile
        );
        errorEvent.setErrorLineNumber(BlueJApiAdapterTestSupport.COMPILE_ERROR_LINE);
        errorEvent.setErrorMessage(BlueJApiAdapterTestSupport.COMPILE_ERROR_MESSAGE);
        adapter.compileError(errorEvent);
    }

    private static CompileEvent compileEvent(CompileEvent.EventType type, Path sourceFile) {
        return new CompileEvent(type, new File[] { sourceFile.toFile() });
    }
}
