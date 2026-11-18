# BlueJ Telemetry Plugin

This project builds a BlueJ extension that records compilation, execution,
class, and inferred user-interaction events. The instructions below put
Windows first and Linux instructions follow.

## Windows setup

### Requirements

- BlueJ installed on the computer.
- A Java 21 or newer JDK and Maven available in a terminal. BlueJ 6.0.1 and its
  extension API require Java 21 at runtime.

### Build and install

From the repository folder, build and test the extension:

```powershell
mvn package
```

After the build succeeds, close BlueJ and copy the new JAR into its user
extensions folder:

```powershell
Copy-Item -Force "target\bluej-telemetry-plugin-1.0.0.jar" "$env:USERPROFILE\bluej\extensions2\"
```

Restart BlueJ to load the updated JAR. BlueJ 5 and later use
`%USERPROFILE%\bluej\extensions2` on Windows. Older BlueJ versions may use a
different extension folder. If the folder does not exist, create it once
before copying the JAR.

Open a BlueJ project, compile a class, and invoke one of its methods. When the
BlueJ session ends, the extension saves raw session telemetry in `.telemetry/`,
then creates an analysis JSON report in `json_reports/` and a PDF report in
`pdf_reports/`, all under the project directory where it last observed
telemetry.

The extension records one `EXECUTION` event after a method invocation, including
the class name and whether it exited because of a runtime exception. It also
records class deletions, renames, and inferred creations by comparing
successful compiles with a source-file baseline. The baseline is scanned at
startup when a project is already open, or when the project is first discovered
at compile time. Compile events are used as a proxy for editing activity; exact
keystrokes and typing cadence are not captured. Periods of at least one minute
between observed activity callbacks are recorded as estimated idle time.

## Editor typing telemetry

Visible Java editors are sampled about every 250 ms. Text-length changes are
classified as typing (1 to 20 characters), paste (over 20), or deletion; a
no-change pause becomes idle after 2 seconds. Only text lengths and timestamps
are recorded, not source text or keystrokes. This activity is saved in the raw
`.telemetry/` JSON and is not included in the existing reports.

## What the files and folders are

| File or folder | What it is and where it belongs |
| --- | --- |
| `pom.xml` | Maven build configuration. It is in the repository root, beside this README. It sets the Java version, plugin artifact name/version, and dependencies. |
| `lib-repo/` | Local Maven repository containing the BlueJ extension API required to compile the plugin. |
| `target/bluej-telemetry-plugin-1.0.0.jar` | The built extension JAR. Maven generates it in the repository's `target` folder when `mvn package` succeeds. Copy this JAR to BlueJ's user extensions folder; do not copy the whole `target` folder. |
| `extensions2/` | BlueJ 5 and later's per-user extension folder. On Windows this is `%USERPROFILE%\bluej\extensions2\`; on Linux it is `~/.bluej/extensions2/`. Create it if it does not exist. The extension JAR goes here. Check your BlueJ version's documentation if its extension folder differs. |
| `package.bluej` | A BlueJ project settings/marker file created by BlueJ in each project. Keep it in the project; the extension searches upward from compiled source files to find the project root. |
| `.telemetry/` | Raw session telemetry JSON used as the input for report generation. |
| `json_reports/` | Analysis JSON reports generated from the saved telemetry at session end. |
| `pdf_reports/` | PDF reports generated from the saved telemetry at session end. |
| `telemetry_<session-id>.json` | Session metadata and all raw events, written inside `.telemetry/`. |
| `report_<session-id>.json` | Calculated session summary, compilation statistics, errors, activity time, and class timeline. |
| `report_<session-id>.pdf` | Human-readable report generated from the same saved session telemetry. |