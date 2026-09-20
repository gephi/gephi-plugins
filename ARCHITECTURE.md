# gephi-plugins Architecture

## What this repository is

`gephi-plugins` is not a plugin, and (on the branch you're most likely looking at) it is not a
collection of plugins either. It is the scaffold and Maven build harness that contributors fork to
develop and submit plugins for [Gephi](https://gephi.org), the graph visualization platform built on
the Apache NetBeans Platform.

```text
Your plugin module(s)         (modules/<PluginName>, added by you or by `generate`)
        │  implements a Gephi SPI, declares Maven deps on Gephi/NetBeans API modules
        ▼
Gephi                          (downloaded as a build dependency; see root pom.xml)
        │
NetBeans Platform              (module system, Lookup, `.nbm` packaging)
        │
       Java
```

A plugin is nothing more than another NetBeans module, built the same way Gephi's own modules are
built, that Gephi discovers at startup because it's on the classpath. This repository exists to make
that build set up correctly (manifest, packaging, dependency versions) without contributors needing
to hand-roll it.

## Repository / branch model

This is the part that isn't obvious from a single checkout: the same GitHub repository serves three
different purposes on three different branches. Confusion here (e.g. "why is `<modules>` empty?",
"where is `modules/pom.xml`?") almost always traces back to not knowing which branch does what.

| Branch | Purpose |
|---|---|
| `master` | The template. `pom.xml`'s `<modules>` list starts empty. Contributors fork this branch and add their own plugin(s) to it locally — this is what `mvn org.gephi:gephi-maven-plugin:generate` does. Most day-to-day plugin development happens against a `master`-based fork. |
| `master-forge` | Where plugin submissions land. It accumulates every community plugin in one big multi-module build, and is what generates the plugin listing at gephi.org. PRs submitting or updating a plugin target this branch, not `master` (see README's "Submit a plugin"). |
| `parent-pom` | Hosts `modules/pom.xml`, the actual `gephi-plugin-parent` Maven artifact (see below). Pushing to this branch triggers `release-pom.yml`, which deploys `org.gephi:gephi-plugin-parent` to Maven Central. |

`build.yml` (the main CI workflow) runs on every branch push *except* `master-forge`, `master`,
`parent-pom`, and `gh-pages` — i.e. on the feature/topic branches contributors actually push to.
`test-generation.yml` runs on `master` and is an integration test for the scaffold itself: it runs
`generate`, `package`, and the `release` profile's `build-metadata`/`create-autoupdate` goals against
a throwaway fixture plugin, so a `gephi-maven-plugin` version bump in `pom.xml` is caught before real
plugin repos pick it up.

## How Gephi can be extended

Gephi's extensibility model is what determines what a plugin *is*. This section summarizes the
mechanism; see the [core Gephi ARCHITECTURE.md](https://github.com/gephi/gephi/blob/master/ARCHITECTURE.md)
for the full treatment (API/SPI design philosophy, controllers/models, Lookup internals).

Gephi separates **APIs** (functionality a module offers to others, e.g. `ProjectController`) from
**SPIs** (Service Provider Interfaces — extension points meant to be implemented by core modules
*and* plugins alike, e.g. `Importer`, `Layout`, `Statistics`). A plugin always extends an SPI; it
never needs to modify Gephi core to add functionality.

| SPI | Extension point |
|---|---|
| Import SPI | File, database, and wizard importers |
| Layout SPI | Layout algorithms |
| Statistics SPI | Metrics and other graph algorithms |
| Tools SPI | Tools in the visualization toolbar |
| Export SPI | File exporters for graphs and graphics |
| Filters SPI | Filters |
| Preview SPI | Preview builders and renderers |
| Generator SPI | Graph generators |
| Data Laboratory SPI | Data Laboratory manipulators |
| Appearance SPI | Transformers for ranking and partitioning nodes/edges |
| Project SPI | Persistence providers for `.gephi` project files |
| Visualization SPI | Renderers for the newer `VisualizationEngine` module (work in progress) |

Implementations are discovered at runtime through **Lookup**, NetBeans's service-registry mechanism,
not through any Gephi-specific plugin registry:

```java
@ServiceProvider(service = Layout.class)
public class MyLayout implements Layout {
}
```

Annotating a class this way is what makes it show up in Gephi's layout list, exporter list, filter
list, etc. — implementing the interface alone is not enough; without `@ServiceProvider`, `Lookup`
will not find it. This is also the whole mechanism: there's no separate "plugin API" beyond the SPI
you're implementing and this annotation.

## Anatomy of a plugin module

`mvn org.gephi:gephi-maven-plugin:generate` produces this layout under `modules/<PluginName>/`:

```text
modules/<PluginName>/
├── src/
│   └── main/
│       ├── java/            # SPI implementation(s), e.g. org.foo.myplugin.MyLayout
│       ├── resources/       # Bundle.properties, icons
│       └── nbm/
│           └── manifest.mf  # OpenIDE-Module-* branding/description/category entries
└── pom.xml                  # parent = org.gephi:gephi-plugin-parent, packaging = nbm
```

The generated `pom.xml` sets `<parent>` to `org.gephi:gephi-plugin-parent` (see below) and
configures `nbm-maven-plugin` with the plugin's author/license and a `<publicPackages>` list —
only packages listed there are visible to other modules/plugins, mirroring the public-package
convention used throughout core Gephi. `manifest.mf` carries the branding shown in Gephi's Plugin
Manager (`OpenIDE-Module-Name`, `-Short-Description`, `-Long-Description`, `-Display-Category`), or
alternatively an `OpenIDE-Module-Localizing-Bundle` pointer into `Bundle.properties` when the text is
too long for the manifest format.

A plugin can also be a **suite**: several modules in the same top-level folder that split API,
implementation, and UI concerns (the same four-role convention — `XxxAPI` / `XxxPlugin` /
`XxxPluginUI` / `DesktopXxx` — used across core Gephi's own modules). This is only worth doing for
plugins complex enough to need a shared API surface between multiple sub-modules; a single module is
enough for the vast majority of plugins.

## Dependency management: `gephi-plugin-parent`

Every plugin's `pom.xml` inherits from `org.gephi:gephi-plugin-parent` — an artifact built from this
repo's `parent-pom` branch, published to Maven Central, and versioned alongside Gephi itself (e.g.
`0.11.3`). It supplies:

- Java/compiler settings (JDK 17 target).
- A `<dependencyManagement>` entry for every Gephi and NetBeans Platform module a plugin might
  depend on (`graph-api`, `layout-api`, `org-openide-util-lookup`, etc.), so a plugin's own `pom.xml`
  can declare a dependency without a `<version>` and get the right one for the Gephi version it
  targets.

This is why the root `pom.xml` on `master` and `master-forge` looks different from
`modules/pom.xml` on `parent-pom`: the former is the reactor POM that aggregates whichever plugin
modules exist in this checkout (`<packaging>pom</packaging>`, lists `<modules>`); the latter is the
plugin parent POM those modules inherit build configuration and dependency versions from.

## The `gephi-maven-plugin` build lifecycle

[`gephi-maven-plugin`](https://github.com/gephi/gephi-maven-plugin) is the Maven plugin that drives
everything above. Its goals, bound in this repo's root `pom.xml` or invoked directly:

| Goal | When it runs | What it does |
|---|---|---|
| `generate` | Invoked manually | Interactively scaffolds a new plugin module and adds it to `pom.xml`'s `<modules>` |
| `validate` | Bound to the `validate` phase (every `mvn package`) | Checks every listed module's manifest/pom configuration is well-formed; fails the build with a specific reason if not |
| `run` | Invoked manually | Downloads/builds the matching Gephi distribution and launches it with the current modules installed, for manual testing |
| `build-metadata` / `create-autoupdate` | Bound to the `package` phase under the `release` profile | Generates the autoupdate site (`updates.xml` + `.nbm` files) published to `metadataUrl` — this is what powers Gephi's in-app Plugin Manager and the gephi.org plugin listing |
| `migrate` | Invoked manually | Helps update an existing plugin's configuration when the target Gephi version changes |

## Root files

```text
.github/workflows/   # build.yml (feature branches), test-generation.yml (master), release-pom.yml (parent-pom)
modules/             # one folder per plugin/suite; empty <modules> list on master until you add one
plugins/             # static assets (images) used by the gephi.org plugin listing, not source code
pom.xml              # reactor POM for this checkout's plugin modules — NOT the plugin parent POM
nbactions.xml        # NetBeans IDE run/debug actions (`mvn package org.gephi:gephi-maven-plugin:run`)
```
