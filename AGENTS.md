# AGENTS.md

Instructions for AI coding agents (Claude Code, Cursor, Codex, etc.) working in this repository.

## Project overview

`gephi-plugins` is the scaffold and Maven build harness contributors fork to develop and submit
plugins for [Gephi](https://gephi.org), the graph visualization platform. A plugin is a NetBeans
module that implements one of Gephi's extension points (SPIs) — this repo does not contain Gephi
itself or, on `master`, any plugin source by default. See `README.md` for the day-to-day
getting-started flow (create/build/run/submit a plugin) and `ARCHITECTURE.md` for how the repository
(including its unusual three-branch model), the `gephi-maven-plugin` build lifecycle, and Gephi's
SPI/Lookup extension mechanism fit together.

Read `ARCHITECTURE.md` before assuming something is broken: an empty `<modules>` list in `pom.xml`,
or the absence of `modules/pom.xml`, is expected on `master` — see its "Repository / branch model"
section.

## Build and test

Requires JDK 17 and Maven.

- Scaffold a new plugin (interactive prompts): `mvn org.gephi:gephi-maven-plugin:generate`
- Build and validate every plugin currently listed in `pom.xml`: `mvn clean package`
- Build/test a single plugin module: `mvn -pl modules/<ModuleName> clean package`
- Run a single module's unit tests only: `mvn -pl modules/<ModuleName> test`
- Run Gephi with the module(s) installed (build first): `mvn org.gephi:gephi-maven-plugin:run`
- IDE run/debug configs are defined in `nbactions.xml`; the IntelliJ debug setup (remote debugger +
  `-Drun.params.debug` VM option) is documented in `README.md`.

`mvn package` always runs `gephi-maven-plugin:validate` at the `validate` phase — if a build fails
there, the error names the specific manifest/pom problem; fix that rather than working around it.

## Adding or changing a plugin

- One plugin (or one suite of related modules) per folder under `modules/`, added to the root
  `pom.xml`'s `<modules>` list — `generate` does both steps for you; do it manually the same way if
  extending an existing plugin's suite.
- A plugin's `pom.xml` inherits from `org.gephi:gephi-plugin-parent` (published from this repo's
  `parent-pom` branch — not present in a normal `master` checkout). Add dependencies without a
  `<version>`; the parent's `dependencyManagement` supplies the version matching the target Gephi
  release. See `ARCHITECTURE.md`.
- Register SPI implementations with `@ServiceProvider(service = ...)`; see `ARCHITECTURE.md`'s
  "How Gephi can be extended" section for which SPI fits a given feature, and the
  [core Gephi ARCHITECTURE.md](https://github.com/gephi/gephi/blob/master/ARCHITECTURE.md) for the
  full API/SPI/Lookup design.
- Bump the plugin's own `<version>` in its `pom.xml` on every update — the autoupdate site keys off
  it, and reviewers check for it.
- Only list packages meant for other modules to use under `<publicPackages>` in the plugin's
  `pom.xml`.

## Code style

See `CONTRIBUTING.md`'s "Code quality" section rather than duplicating it here.

## PR / commit guidelines

- Plugin submissions (new plugin or update) target the `master-forge` branch, not `master` — see
  README's "Submit a plugin" / "Update a plugin" sections. Changes to the scaffold itself (root
  `pom.xml`, `.github/workflows`, this file, `ARCHITECTURE.md`) target `master`.
- Use `.github/issue_template.md`'s structure when filing or triaging bug reports.
- Keep commits scoped to one plugin or one logical change — a PR touching unrelated plugins in the
  same suite stands out during review.

### Reviewing a third-party plugin PR

Most activity in this repo is reviewing plugin submissions rather than writing plugin code. When
asked to review one, work through `CONTRIBUTING.md`'s "Reviewing a plugin submission PR" checklist
(build, `pom.xml` config, manifest/branding, SPI registration, licensing, file hygiene, tests, PR
template completeness) and report findings against specific, named items from it rather than general
impressions — these PRs reliably have the same handful of holes, and the checklist exists to catch
them without re-deriving them each time.

## Security

- Never commit secrets, API keys, or tokens. `release-pom.yml` publishes to Maven Central using
  repository secrets (GPG key, OSSRH credentials) — never hardcode credentials locally to bypass it.
- This is a public repository — don't add personal or employer-internal tooling references (private
  registries, internal URLs, machine-specific paths) to any committed file.
