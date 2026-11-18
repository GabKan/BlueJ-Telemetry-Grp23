package org.comp3607.report;

import org.comp3607.analytics.EditorActivity;
import org.comp3607.analytics.ReportData;
import org.comp3607.analytics.TimeFormatter;

//  Builds the text of the editor activity table 

public class EditorActivityTableBuilder {

    private static final long MILLIS_PER_MINUTE = 60000;
    private static final double MILLIS_PER_SECOND = 1000.0;

    public String build(ReportData reportData) {
        EditorActivity editor = reportData.getEditorActivity();
        if (!editor.isAvailable()) {
            return "Editor activity|No data recorded";
        }

        String pasteWord = " pastes";
        if (editor.getPasteCount() == 1) {
            pasteWord = " paste";
        }
        String speedText = String.format("%.0f", editor.getTypingSpeed());

        String table = "";
        table = table + "Typing|" + timeText(editor.getTypingMs()) + ", "+ editor.getTypingChars() + " chars\n";
        table = table + "Deleting|" + timeText(editor.getDeletionMs()) + ", "+ editor.getDeletionChars() + " chars\n";
        table = table + "Pasting|" + timeText(editor.getPasteMs()) + ", "+ editor.getPasteCount() + pasteWord + "\n";
        table = table + "Active|" + timeText(editor.getActiveMs()) + "\n";
        table = table + "Idle|" + timeText(editor.getIdleMs()) + ", longest pause "+ timeText(editor.getLongestIdleMs()) + "\n";
        table = table + "Edits|" + editor.getEditCount() + " edits, typing speed "+ speedText + " chars/min";
        return table;
    }

    private String timeText(long millis) {
        if (millis < MILLIS_PER_MINUTE) {
            return String.format("%.1f s", millis / MILLIS_PER_SECOND);
        }
        return TimeFormatter.TimeFormat(millis);
    }
}