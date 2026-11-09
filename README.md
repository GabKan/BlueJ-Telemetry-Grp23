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

Open a BlueJ project, compile a class, and invoke one of its methods to check that the extension is working. The file `bluej-telemetry.log` should appear in that BlueJ project's root folder.

The extension records one `EXECUTION` event after a method invocation, including
the class name and whether it exited because of a runtime exception. It also
records class deletions, renames, and inferred creation on a class's first
successful compile in the current session. Compile events are used as a proxy
for editing activity; keyboard input and typing cadence are not captured.
Periods of at least one minute between observed activity callbacks are
recorded as estimated idle time.

## Linux setup

From the repository folder, build the extension:

```bash
mvn package
```

After the build succeeds, close BlueJ and copy the new JAR into the user
extensions folder:

```bash
cp target/bluej-telemetry-plugin-1.0.0.jar ~/.bluej/extensions2/
```

Restart BlueJ to load the updated JAR. BlueJ 6 uses `~/.bluej/extensions2/`;
older BlueJ versions may use `~/.bluej/extensions/`.

## What the files and folders are

| File or folder | What it is and where it belongs |
| --- | --- |
| `pom.xml` | Maven build configuration. It is in the repository root, beside this README. It sets the Java version, plugin artifact name/version, and dependencies. |
| `lib-repo/` | Local Maven repository containing the BlueJ extension API required to compile the plugin. |
| `target/bluej-telemetry-plugin-1.0.0.jar` | The built extension JAR. Maven generates it in the repository's `target` folder when `mvn package` succeeds. Copy this JAR to BlueJ's user extensions folder; do not copy the whole `target` folder. |
| `extensions2/` | BlueJ 5 and later's per-user extension folder. On Windows this is `%USERPROFILE%\bluej\extensions2\`; on Linux it is `~/.bluej/extensions2/`. Create it if it does not exist. The extension JAR goes here. Check your BlueJ version's documentation if its extension folder differs. |
| `package.bluej` | A BlueJ project settings/marker file created by BlueJ in each project. Keep it in the project; the extension searches upward from compiled source files to find the project root. |
| `bluej-telemetry.log` | A human-readable event log created by the extension in the BlueJ project root the first time a telemetry event is recorded. It is not created by the Maven build. |

## Build and test

### Requirements

- JDK 21 or newer for builds and tests that load the BlueJ 6 API. The plugin
  source is still compiled for Java 17.
- Apache Maven 3.9 or newer
- BlueJ 6.0.1
- VS Code users: install the "Extension Pack for Java"

Run `mvn -v` and check that Maven uses the JDK you expect.

### Build

The BlueJ extension API dependency is stored in the `lib-repo` folder of this
repository, and `pom.xml` reads it from there. Keep this folder when cloning or
sharing the project; Maven needs it to compile the plugin.

```bash
git clone https://github.com/GabKan/BlueJ-Telemetry-Grp23.git
cd BlueJ-Telemetry-Grp23
mvn package
```

The plugin jar is created at `target/bluej-telemetry-plugin-1.0.0.jar`.

### Troubleshooting

| Problem | Cause | Fix |
|---|---|---|
| VS Code shows "Could not read maven project" | The Java extension kept an old error | Run **Java: Clean Java Language Server Workspace**, restart, then right-click `pom.xml` and select **Update Project** |
| Tests fail with `UnsupportedClassVersionError` for a `bluej.extensions2` class | The test runner is using Java 17, but BlueJ 6 API classes require Java 21 | Select Java 21 or newer for the Maven and VS Code test runtimes. BlueJ's bundled JDK is under its installation folder in `jdk`. |
| Execution entries do not appear in the project log | BlueJ may still be running an older installed plugin JAR | Copy the newly built JAR from `target` into the user `extensions2` folder, fully close BlueJ, and restart it before testing. |
| Maven cannot resolve the BlueJ API dependency | The `lib-repo` folder is missing from your clone | Pull the latest repository changes so `lib-repo` is restored. |