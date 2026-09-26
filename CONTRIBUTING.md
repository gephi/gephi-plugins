# Contributing to gephi-plugins

This covers code-quality expectations for plugin code and, since most activity in this repository is
reviewing plugin submissions from third parties, a checklist for reviewing those PRs. For how to set
up, build, and submit a plugin in the first place, see `README.md`. For how the repository and
Gephi's extension mechanism fit together, see `ARCHITECTURE.md`.

## Code quality

- Write code, comments, commit messages, and PR descriptions in English.
- Match the existing style of the file being edited; don't reformat unrelated code in the same PR.
- Remove debug leftovers before submitting: ad-hoc debug output (see "Logging" below for what to use
  instead), commented-out code blocks, placeholder text left over from the `generate` template (e.g.
  "Plugin catch-phrase", "Insert dependencies here").
- Plugins pick their own license (the `generate` goal defaults to Apache 2.0) — don't carry over
  core Gephi's CDDL/GPL dual-license header into plugin code.

## Gephi platform conventions

Core Gephi has established conventions for cross-cutting concerns. Plugins should follow them
instead of introducing their own — this keeps a plugin's runtime behavior and log/UI output
consistent with core Gephi and every other installed plugin.

### Logging

Declare a per-class logger field and use it — this is the pattern throughout core Gephi, not SLF4J
or Log4j:

```java
private static final Logger LOGGER = Logger.getLogger(ProjectControllerImpl.class.getName());
```

(real example: `modules/ProjectAPI/.../ProjectControllerImpl.java`). Scope the logger to your own
class, never a shared or hardcoded name: the fully-qualified class name
(`org.<yourorg>.<yourplugin>.SomeClass`) is what makes a log line traceable back to your plugin
specifically, as opposed to an `org.gephi.*` core line or another plugin's output.

Use `SEVERE`/`WARNING`/`INFO`/`FINE` levels appropriately; don't invent your own scale. When logging
a caught exception, pass it as the log call's `Throwable` argument so the stack trace is preserved,
e.g.:

```java
Logger.getLogger(SQLiteDriver.class.getName()).log(Level.SEVERE, null, ex);
// or, with a message:
Logger.getLogger("").log(Level.SEVERE, "Error while setting value for property '" + getName() + "'", e);
```

(real examples: `modules/DBDrivers/.../SQLiteDriver.java`, `modules/FiltersAPI/.../FilterProperty.java`).
Don't use `exception.printStackTrace()` — it bypasses the logger, so the message never respects the
user's configured log level or destination. Log messages are developer-facing, plain Java strings —
don't route them through `Bundle.properties`/`NbBundle`.

### Localization

No hardcoded English UI strings (labels, tooltips, dialog text, user-facing error messages) in Java
code. Put the string in a `Bundle.properties` file in the same package as the class
(`src/main/resources/<your/package/path>/Bundle.properties`) and reference it with
`NbBundle.getMessage(YourClass.class, "key")`:

```properties
# org/gephi/datalab/api/Bundle.properties
DataLaboratoryHelper.ui.okButton.text=Ok
SettingsPanel.title={0}
```
```java
NbBundle.getMessage(DataLaboratoryHelper.class, "SettingsPanel.title", ui.getDisplayName());
```

The real key convention observed throughout core Gephi (320 `Bundle.properties` files, 477
`NbBundle.getMessage` call sites) is `ClassName.member.property`, e.g.
`DataLaboratoryHelper.ui.okButton.text` for a field/button named `okButton`'s `text`. For
parameterized text, use `{0}`, `{1}`, ... `MessageFormat` placeholders in the property value and
pass the extra arguments to `NbBundle.getMessage(Class, String, Object...)` — as in the
`SettingsPanel.title={0}` example above.

If you build UI with the NetBeans GUI builder (a `.form` file next to the `.java` file), it
generates `ResourceString` entries in the `.form` XML itself (with a `bundle=".../Bundle.properties"`
and `key="..."` pair per field) and writes the corresponding `NbBundle.getMessage(...)` call into
the generated code for you — you still need the key/value present in `Bundle.properties`, but you
don't hand-write the `NbBundle.getMessage` call for form fields.

### Preferences

Persist a **per-user, cross-session, cross-project** setting (e.g. "always export in millimeters")
with `NbPreferences.forModule(YourClass.class)`:

```java
boolean defaultMM = NbPreferences.forModule(UIExporterPDF.class).getBoolean("Default_Millimeter", false);
millimeter = NbPreferences.forModule(UIExporterPDF.class).getBoolean("Millimeter", defaultMM);
// ...
NbPreferences.forModule(UIExporterPDF.class).putBoolean("Millimeter", millimeter);
```

(real example: `modules/PreviewExportUI/.../UIExporterPDFPanel.java`; `AbstractExporterSettings` in
the same module wraps the same pattern for `getInt`/`putInt`/`getFloat`/`putFloat`). Don't build a
plugin-invented properties file, serialization scheme, or static field for this.

`NbPreferences` is backed by the NetBeans user directory and is global to the user's Gephi
installation — it is the wrong place for anything scoped to a single project or workspace (layout
parameters, filter configuration, anything that should be saved and reopened *with* the project).
That belongs in a Project SPI persistence provider that serializes into the `.gephi` file instead —
see `ARCHITECTURE.md`'s Project SPI row.

### Library dependencies and version collisions

Before adding a library directly, check whether Gephi already wraps it in one of its `*Wrapper`
modules and depend on that Maven artifact instead:

- `core-library-wrapper` (`org.gephi:core-library-wrapper`) — e.g. `gson`, `commons-math3`,
  `jfreechart`, `trove4j`, `commons-csv`, `commons-codec`, `commons-compress`.
- `org.gephi:ui-library-wrapper` — the Flamingo ribbon UI library.
- `org.gephi:batik-wrapper` — Batik/SVG rendering.

```xml
<dependency>
    <groupId>org.gephi</groupId>
    <artifactId>core-library-wrapper</artifactId>
</dependency>
```

Two copies of the same library on the classpath (one via the wrapper, one direct) is wasteful and
can behave inconsistently — depend on the wrapper's Maven coordinate above instead of the raw
library's.

If a plugin needs library X, and X transitively pulls in a different version of a library Y that
Gephi already provides (directly or via a wrapper), don't let both versions coexist on the
classpath — exclude X's transitive dependency on Y. `batik-wrapper`'s own `pom.xml` does exactly
this for `batik-transcoder`:

```xml
<dependency>
    <groupId>org.apache.xmlgraphics</groupId>
    <artifactId>batik-transcoder</artifactId>
    <version>${gephi.batik.version}</version>
    <exclusions>
        <exclusion>
            <groupId>xml-apis</groupId>
            <artifactId>xml-apis</artifactId>
        </exclusion>
        <exclusion>
            <groupId>commons-logging</groupId>
            <artifactId>commons-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

Only reach for `<exclusions>` when there's no wrapper module to depend on instead of the raw
library. Before submitting, run `mvn dependency:tree` in your plugin's module (add
`-Dincludes=<groupId>:<artifactId>` to filter to one suspect library) to check whether a dependency
you added is pulling in a second, different version of something Gephi already provides.

### Icons

Use SVG, not PNG/GIF, for toolbar, menu, and branding icons. Load one the same way core Gephi does, via
`ImageUtilities.loadImageIcon(...)` from an SPI's `getIcon()`:

```java
@Override
public Icon getIcon() {
    return ImageUtilities.loadImageIcon("DataLaboratoryPlugin/settle.svg", false);
}
```

The path argument is `"<YourModuleCodeNameBase>/<relative/path>.svg"`, resolved as a classpath resource — for your own
plugin, put the file at `src/main/resources/<YourModuleCodeNameBase>/icon.svg` in that same module.
Use the mandated pixel size: `viewBox="0 0 32 32"`.

### Suite folder/package naming

When a plugin has multiple modules, keep folder and package names consistent across the suite. For
example, a suite named `MyPlugin` from an org `com.foo`, mirroring `ARCHITECTURE.md`'s
API/SPI/implementation/UI package-role table, would look like:

```text
modules/
├── MyPluginAPI/     com.foo.myplugin.api      (public API interfaces)
│                    com.foo.myplugin.spi      (SPI interfaces, if the suite defines its own)
│                    com.foo.myplugin          (API implementation)
└── MyPluginUI/      com.foo.myplugin.ui       (Swing panels/wizards implementing the UI)
```

Don't mix naming schemes within one suite — e.g. `MyPluginCore` next to `myplugin.ui` next to
`MyPluginModuleThree` — pick one convention (ideally the one above) and apply it to every module
folder and every module's base package.

### Test utilities

For graph fixtures in unit tests, depend on GraphAPI's (`org.gephi:graph-api`) test-jar and reuse
`org.gephi.graph.GraphGenerator`'s static factory methods rather than hand-building a `GraphModel`:

```java
GraphModel model = GraphGenerator.generateCompleteUndirectedGraph(10);
// also available: generateNullUndirectedGraph(int), generateCyclicDirectedGraph(int),
// generatePathUndirectedGraph(int), generateStarUndirectedGraph(int), and directed variants
```

It's the only class GraphAPI's `pom.xml` explicitly includes in its test-jar build (its own
comment: "Only this class is intended to be re-used other modules" — other GraphAPI test classes are
excluded from the jar). Declare the dependency as:

```xml
<dependency>
    <groupId>org.gephi</groupId>
    <artifactId>graph-api</artifactId>
    <scope>test</scope>
    <type>test-jar</type>
</dependency>
```

(real consumer example: `modules/AppearanceAPI/pom.xml`). For import fixtures, depend the same way
on `org.gephi:io-importer-api`'s test-jar (unlike GraphAPI's, it isn't filtered to one class) and
use:

```java
GraphModel model = GraphImporter.importGraph(new File("path/to/test.gexf"));
// or, to load a fixture bundled as a classpath resource next to a test class:
GraphModel model = GraphImporter.importGraph(MyPluginTest.class, "fixture.gexf");
```

to build a `GraphModel` from a test file through the real import pipeline (real consumer example:
`modules/StatisticsPlugin/pom.xml`, depending on `io-importer-api` with `<type>test-jar</type>`),
instead of hand-rolling either one.

### Comments

Keep code comments short and about the current code — what invariant holds, what a non-obvious
value means — not the history of why it got that way or what alternative was rejected. That
belongs in the commit message or PR description, not the source, where it rots as the code
evolves. For example:

```java
// Bad: explains history/rationale, will be stale the moment someone changes this again
// We used to retry 3 times here but that caused timeouts in slow environments, so now it's 1.
int maxRetries = 1;

// Good: states a fact about the current code
// Must stay <= server-side rate limit of 1 req/s; see the ImporterUI cap for the same constant.
int maxRetries = 1;
```

## Reviewing a plugin submission PR

Third-party plugin PRs are usually incomplete in the same handful of ways. Work through this list
before approving; when something's missing, name the specific item rather than asking generically
for "more polish."

### Build actually passes

- `mvn clean package` succeeds from the repository root, including the `gephi-maven-plugin:validate`
  goal that runs automatically at the `validate` phase — don't take a green PR description at face
  value, run it.
- If the PR adds a suite (multiple modules in one plugin folder), every sub-module folder is added to
  the root `pom.xml`'s `<modules>` list, not just the first one.
- The PR touches only its own plugin's folder plus its own line in root `pom.xml`'s `<modules>` list
  — changes to another plugin's code, to `gephi-plugin-parent`-managed versions, or to unrelated
  workflow/config files are a red flag in a plugin-submission PR.

### `pom.xml` configuration

- `<parent>` is `org.gephi:gephi-plugin-parent` with a `<version>` matching this repo's
  `gephi.version` property — not a hardcoded fork or an unrelated parent.
- Dependencies on Gephi/NetBeans modules are declared **without** an explicit `<version>` — the
  parent's `dependencyManagement` should supply it. An explicit version there usually means the
  author copy-pasted from an old example instead of relying on the parent, and can silently drift
  from the Gephi version this repo targets.
- `<publicPackages>` lists only packages meant for other modules to consume, not implementation
  packages — an empty list, or a list that includes an obvious `impl`/internal package, is worth
  flagging either way (the former may be intentional, the latter usually isn't).
- For an **update** to an existing plugin, the `<version>` was actually bumped from the previous
  release — this is easy to miss and the autoupdate site keys off it.
- `author`, and `licenseName`/`licenseFile` (or an equivalent license declaration), are filled in on
  the `nbm-maven-plugin` configuration — not left as generated placeholders.

### Manifest / branding

- `src/main/nbm/manifest.mf` (or the `Bundle.properties` it points to via
  `OpenIDE-Module-Localizing-Bundle`) has real values for module name, short description, long
  description, and category — not leftover template placeholders.
- The declared category (`Layout`, `Export`, `Import`, `Data Laboratory`, `Filter`, `Generator`,
  `Metric`, `Preview`, `Tool`, `Appearance`) actually matches the SPI the plugin implements (see
  next section) — a layout algorithm categorized as "Tool" will be hard for users to find.

### SPI implementation correctness

- The class implementing the plugin's functionality is annotated `@ServiceProvider(service = ...)`
  for the correct SPI interface. Without the annotation, Gephi's `Lookup` will not discover it and
  the plugin will silently do nothing at runtime — this is a common, easy-to-miss failure mode that
  the build will not catch. See `ARCHITECTURE.md`'s "How Gephi can be extended" section for the SPI
  list.
- In a suite, UI-only code (Swing panels, wizards) lives in the UI/plugin-UI module, not mixed into
  the same module as the core SPI implementation.
- If the plugin implements a cancellable long-running operation (a `Generator`, `Importer`,
  `Statistics`, or other `LongTask`), check that `cancel()` actually stops the work instead of being
  a stub that always returns `false` — a no-op cancel passes `mvn package` cleanly and only shows up
  as an unresponsive UI at runtime.
- Any code that reads or writes the graph inside such a task must release its lock
  (`Graph.readUnlock()`/`writeUnlock()`) on every exit path, including the cancellation path — a leak
  here breaks graph consistency for the whole session, not just this plugin's feature (see
  `ARCHITECTURE.md`'s locking model).

### Platform conventions

See "Gephi platform conventions" above for the reasoning; on review, check that the plugin:

- Logs via `java.util.logging` with a per-class logger — not `System.out`, not SLF4J/Log4j.
- Has no hardcoded English UI strings — routed through `Bundle.properties` + `NbBundle.getMessage`.
- Persists settings via `NbPreferences.forModule(...)`, not a homemade scheme.
- Depends on a `*Wrapper` module instead of re-adding a library Gephi already wraps, and uses
  `<exclusions>` rather than silently duplicating a library at a different version — `mvn
  dependency:tree` on the plugin's module is the fast way to check.
- Uses SVG icons loaded via `ImageUtilities.loadImageIcon(...)`, not PNG/GIF.
- Uses consistent folder/package naming across suite modules, if any.
- Reuses `GraphGenerator`/`GraphImporter` from GraphAPI/ImportAPI's test-jars in tests rather than
  reinventing fixture construction.
- Has comments that state facts about the current code, not rationale or history.

### Licensing and attribution

- A license file is actually included and matches what's declared in `pom.xml` (e.g. a plugin
  declaring GPLv3 ships a `gpl-3.0.txt` or equivalent, not just the pom field).
- Any bundled third-party library, native binary, or copy-pasted code is disclosed and its license is
  compatible with the plugin's declared license — don't assume silence means it's fine.

### File hygiene

- No build artifacts committed (`target/`, `*.class`, stray `*.jar` outside a deliberately vendored
  native dependency, or a built `*.nbm` file — the packaged plugin binary the build produces, not
  something to submit as source).
- No IDE metadata committed (`.idea/`, `.classpath`, `.project`, `*.iml`) unless the existing plugin
  already tracks it as a convention.
- No secrets, tokens, or credentials anywhere in the diff.

### Tests

- Unit tests are included where the plugin's logic is non-trivial (e.g. algorithm implementations),
  using the `org-netbeans-modules-nbjunit` dependency per `README.md`'s testing section.
- Tests are not disabled or skipped (`@Ignore`, `-DskipTests` baked into the module's own `pom.xml`)
  to force a green build.

### PR completeness

- `.github/pull_request_template.md`'s checkboxes reflect what was actually done, not left checked
  by default.
- The "What is the purpose of this plugin?" and "How to test your plugin in Gephi?" sections are
  filled in with real, reproducible content — not left as the template's empty numbered list.
- The PR targets the `master-forge` branch for a plugin submission or update, not `master` (see
  README's "Submit a plugin"). A PR against `master` touching only `modules/` is itself worth
  questioning.

## Maintaining a plugin after approval

Once merged into `master-forge`, a plugin's contributor keeps their fork as the source of truth for
future updates — the flow is identical to the initial submission (see README's "Update a plugin"):
sync the fork with `master`, commit there, and open a new PR against `master-forge`. Don't grant a
contributor write access to this repository, or a branch of it, by default; a normal PR is the
standard path for every update, not just the first one.

### When a plugin's fork stops being a reliable source of truth

Some plugin maintainers go unresponsive, or their fork disappears. When that blocks an update the
plugin genuinely needs (e.g. following a new Gephi release) and there's no fork-based PR to wait on,
a maintainer can adopt the plugin onto its own branch instead. Decide this case by case — it's not
triggered by a fixed inactivity window, only by a real block a fork-based PR can't route around.

- Cut a branch named after the plugin (e.g. `geolayout-plugin`) from its current state in
  `master-forge`.
- Grant write access to that branch only, via a branch protection rule scoped to the name pattern —
  to whoever is adopting maintenance (a `gephi-plugins` maintainer, or a new vetted community
  adopter). Never grant blanket write access to the whole repository for this.
- Changes still land on `master-forge` only through a PR from that branch, the same as a fork would
  — never a direct push. `master-forge` is a merge/build target, not something to develop against
  directly, and `build.yml` (the main CI workflow) explicitly excludes `master-forge` from its
  triggers, so a direct push there skips CI entirely.
- Never fork `master-forge` itself for this, or anything else — it aggregates every plugin in one
  multi-module build, so a fork of it drags in every other plugin along with the one being adopted.

The same applies to maintainer-driven bulk updates across many plugins at once (e.g. a Gephi version
bump): push to a short-lived branch and open a PR into `master-forge` rather than committing to it
directly, so the change still gets a CI run before landing.

### Documenting a plugin in master-forge's `<modules>` list

`master-forge`'s root `pom.xml` is the source of truth for which plugins are currently built and
published, so its `<modules>` list is also the only place that records where each plugin's code
actually comes from. Precede every `<module>` entry with three one-line comments, in this order:

```xml
<!-- name: <the plugin's own display name> -->
<!-- origin: fork (<github-owner>/<repo>) -->
<!-- status: active -->
<module>modules/<Folder></module>
```

- **name** is what the plugin calls itself — `OpenIDE-Module-Name` in `manifest.mf`, or the
  `Bundle.properties` key it points to via `OpenIDE-Module-Localizing-Bundle` — not the `modules/`
  folder name, which is often close but not guaranteed to match.
- **origin** is `fork (<owner>/<repo>)` for the normal case: a contributor's fork stays the source of
  truth for future updates (see "Maintaining a plugin after approval" above). Use
  `branch (<branch-name>)` when a maintainer has adopted the plugin onto its own branch in this
  repository instead (per "When a plugin's fork stops being a reliable source of truth" above). If
  neither can be determined — e.g. history predating PR-linked commits — use
  `unknown (git author: <name>)` rather than guessing.
- **status** is `active` for a plugin currently building, or `disabled — <reason>` when the
  `<module>` line itself is commented out. Keep the reason short but specific enough that a future
  maintainer knows what needs fixing before re-enabling it (e.g. a dependency that no longer
  resolves, or an API the plugin needs to migrate off of) — not just that something's wrong.

A disabled entry comments out all four lines:

```xml
<!-- name: Linkfluence Plugin -->
<!-- origin: fork (eduramiba/gephi-plugins) -->
<!-- status: disabled — needs to migrate away from Joda Time -->
<!-- <module>modules/LinkfluencePlugin</module> -->
```

**Suites** (a plugin split across multiple `modules/` folders — API/Impl/UI, or a bundled dependency
like the streaming plugin's `JettyWrapper`) share one origin and one status, so don't repeat the same
three comments once per folder. Precede the whole run of `<module>` lines with a single block
instead, using the suite's main module for `name` — the one module in the group whose `manifest.mf`
does *not* set `AutoUpdate-Show-In-Client: false`. That flag is how the other modules (API, Impl,
UI, or a bundled library) mark themselves as implementation details hidden from Gephi's plugin
manager, so its absence is what identifies the module the suite is actually known as:

```xml
<!-- name: Graph Streaming -->
<!-- origin: fork (panisson/gephi-plugins) -->
<!-- status: active -->
<module>modules/GraphStreaming</module>
<module>modules/DesktopStreaming</module>
<module>modules/StreamingAPI</module>
<module>modules/StreamingImpl</module>
<module>modules/JettyWrapper</module>
<module>modules/StreamingServer</module>
```

Only group modules that share **both** the same origin and the same status — two folders from the
same fork owner but a different PR are two separate plugin submissions, not a suite, and keep their
own three-comment block each. If one module in an otherwise-grouped suite is later disabled while its
siblings stay active, split it back out into its own block rather than forcing a mismatched status
into the shared one.

Update these comments whenever a plugin's status or origin changes — disabling or re-enabling it,
or adopting an unresponsive contributor's plugin onto a branch — don't leave them describing a
stale state.
