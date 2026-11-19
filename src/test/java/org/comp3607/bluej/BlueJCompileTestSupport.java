package org.comp3607.bluej;

import java.io.File;
import java.nio.file.Path;

import bluej.extensions2.event.CompileEvent;

final class BlueJCompileTestSupport {
    static void startCompilation(BlueJApiAdapter adapter, Path sourceFile) {
        startCompilation(adapter, sourceFile, true);
    }

    static void startBlueJGeneratedCompilation(BlueJApiAdapter adapter, Path sourceFile) {
        startCompilation(adapter, sourceFile, false);
    }

    private static void startCompilation(
        BlueJApiAdapter adapter,
        Path sourceFile,
        boolean userGenerated
    ) {
        CompileEvent startEvent = compileEvent(
            CompileEvent.EventType.COMPILE_START_EVENT, sourceFile, userGenerated
        );
        adapter.compileStarted(startEvent);
    }

    static void finishSuccessfulCompilation(BlueJApiAdapter adapter, Path sourceFile) {
        CompileEvent finishEvent = compileEvent(
            CompileEvent.EventType.COMPILE_DONE_EVENT, sourceFile, true
        );
        adapter.compileSucceeded(finishEvent);
    }

    static void finishFailedCompilation(BlueJApiAdapter adapter, Path sourceFile) {
        CompileEvent finishEvent = compileEvent(
            CompileEvent.EventType.COMPILE_FAILED_EVENT, sourceFile, true
        );
        adapter.compileFailed(finishEvent);
    }

    static void finishBlueJGeneratedFailedCompilation(
        BlueJApiAdapter adapter,
        Path sourceFile
    ) {
        CompileEvent finishEvent = compileEvent(
            CompileEvent.EventType.COMPILE_FAILED_EVENT, sourceFile, false
        );
        adapter.compileFailed(finishEvent);
    }

    static void recordCompileError(BlueJApiAdapter adapter, Path sourceFile) {
        CompileEvent errorEvent = compileEvent(
            CompileEvent.EventType.COMPILE_ERROR_EVENT, sourceFile, true
        );
        errorEvent.setErrorLineNumber(BlueJApiAdapterTestSupport.COMPILE_ERROR_LINE);
        errorEvent.setErrorMessage(BlueJApiAdapterTestSupport.COMPILE_ERROR_MESSAGE);
        adapter.compileError(errorEvent);
    }

    static void recordBlueJGeneratedCompileError(BlueJApiAdapter adapter, Path sourceFile) {
        CompileEvent errorEvent = compileEvent(
            CompileEvent.EventType.COMPILE_ERROR_EVENT, sourceFile, false
        );
        errorEvent.setErrorLineNumber(BlueJApiAdapterTestSupport.COMPILE_ERROR_LINE);
        errorEvent.setErrorMessage(BlueJApiAdapterTestSupport.COMPILE_ERROR_MESSAGE);
        adapter.compileError(errorEvent);
    }

    private static CompileEvent compileEvent(
        CompileEvent.EventType type,
        Path sourceFile,
        boolean userGenerated
    ) {
        return new CompileEvent(type, userGenerated, new File[] { sourceFile.toFile() });
    }
}
