#!/usr/bin/env bash
# Runs gephi-plugins-tests' SPI contract tests (see pom.xml in this folder) against this
# checkout's plugin modules, one Maven run per NBM module so each failure names a single plugin.
#
# Usage: .github/contract-tests/run.sh [modules/<Folder> ...]
#
# With no arguments, every module in the root pom.xml's <modules> list is tested. Otherwise only
# NBM modules under the given folders are; a folder outside the reactor (e.g. a disabled plugin)
# is skipped with a warning. Checks that are only advisory (e.g. a layout adding nodes) don't fail
# the run: they are reported as warning annotations, and in the job summary when running on GitHub
# Actions. Modules must be installed in the local Maven repository first
# (mvn install): a module that is missing there, or was rebuilt since, fails instead of testing a
# stale jar.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
HARNESS_DIR="$ROOT/.github/contract-tests"
GEPHI_VERSION=$(sed -nE 's#.*<gephi\.version>(.*)</gephi\.version>.*#\1#p' "$ROOT/pom.xml" | head -1)
if [ -z "$GEPHI_VERSION" ]; then
  echo "::error::Could not read <gephi.version> from $ROOT/pom.xml"
  exit 1
fi

# One "packaging|groupId|artifactId|version|basedir|built jar|local repository" line per project
if ! PROJECTS=$(cd "$ROOT" && mvn -B -q -ntp org.codehaus.mojo:exec-maven-plugin:3.5.0:exec \
    -Dexec.executable=echo \
    '-Dexec.args=${project.packaging}|${project.groupId}|${project.artifactId}|${project.version}|${project.basedir}|${project.build.directory}/${project.build.finalName}.jar|${settings.localRepository}'); then
  echo "$PROJECTS"
  echo "::error::Could not list the reactor's modules, see the Maven output above"
  exit 1
fi

FOLDERS=()
for arg in "$@"; do
  folder="${arg#"$ROOT"/}"
  folder="${folder#./}"
  FOLDERS+=("${folder%/}")
done

in_folder() {
  case "$1/" in
    */"$2"/*) return 0 ;;
  esac
  return 1
}

TARGETS=()
while IFS='|' read -r packaging group artifact version basedir jar repo; do
  [ "$packaging" = nbm ] || continue
  if [ ${#FOLDERS[@]} -eq 0 ]; then
    TARGETS+=("$group|$artifact|$version|$jar|$repo")
    continue
  fi
  for folder in "${FOLDERS[@]}"; do
    if in_folder "$basedir" "$folder"; then
      TARGETS+=("$group|$artifact|$version|$jar|$repo")
      break
    fi
  done
done <<< "$PROJECTS"

for folder in ${FOLDERS[@]+"${FOLDERS[@]}"}; do
  matched=false
  while IFS='|' read -r packaging _ _ _ basedir _; do
    if [ "$packaging" = nbm ] && in_folder "$basedir" "$folder"; then
      matched=true
      break
    fi
  done <<< "$PROJECTS"
  if [ "$matched" = false ]; then
    echo "::warning::$folder has no plugin module in the root pom.xml's <modules> list; skipping"
  fi
done

if [ ${#TARGETS[@]} -eq 0 ]; then
  echo "No plugin modules to test."
  exit 0
fi

# Advisory test warnings, one "plugin<TAB>check<TAB>message" entry each
WARNINGS_FILE="$HARNESS_DIR/target/contract-warnings.tsv"
WARNINGS=()

# Workflow command data can't contain %, the warnings are single lines already
annotation_escape() {
  printf '%s' "$1" | sed -e 's/%/%25/g'
}

# Plugin names and messages come from the plugin's code, so don't let them format the summary
markdown_escape() {
  printf '%s' "$1" | sed -e 's/&/\&amp;/g' -e 's/</\&lt;/g' -e 's/>/\&gt;/g' -e 's/|/\\|/g' -e 's/`/\\`/g'
}

collect_warnings() {
  [ -f "$WARNINGS_FILE" ] || return 0
  local check message
  while IFS=$'\t' read -r check message; do
    WARNINGS+=("$1"$'\t'"$check"$'\t'"$message")
    echo "::warning title=SPI contract test warning::$(annotation_escape "$1: $check: $message")"
  done < "$WARNINGS_FILE"
}

# The jars of the modules that depend on the one being tested, installed in the local repository. A plugin split in
# modules keeps parts of it (e.g. its user interface) in modules that depend on the main one, and the tests look
# those up too.
dependent_jars() {
  local packaging group artifact version basedir repo jar
  while IFS='|' read -r packaging group artifact version basedir _ repo; do
    [ "$packaging" = nbm ] || continue
    [ "$artifact" != "$1" ] || continue
    grep -q "<artifactId>$1</artifactId>" "$basedir/pom.xml" || continue
    jar="$repo/${group//.//}/$artifact/$version/$artifact-$version.jar"
    [ -f "$jar" ] && echo "$jar"
  done <<< "$PROJECTS"
  return 0
}

PASSED=()
FAILED=()
for target in "${TARGETS[@]}"; do
  IFS='|' read -r group artifact version jar repo <<< "$target"
  gav="$group:$artifact:$version"
  installed="$repo/${group//.//}/$artifact/$version/$artifact-$version.jar"
  if [ ! -f "$installed" ]; then
    echo "::error::$gav isn't installed in $repo, run mvn install first"
    FAILED+=("$gav")
    continue
  fi
  if [ -f "$jar" ] && ! cmp -s "$jar" "$installed"; then
    echo "::error::$gav was rebuilt since it was installed, run mvn install again"
    FAILED+=("$gav")
    continue
  fi

  EXTRA_ARGS=()
  EXTRA_JARS=$(dependent_jars "$artifact" | paste -sd, -)
  if [ -n "$EXTRA_JARS" ]; then
    EXTRA_ARGS=(-Dmaven.test.additionalClasspath="$EXTRA_JARS")
  fi

  rm -rf "$HARNESS_DIR/target"
  echo "::group::Contract tests: $gav"
  if mvn -B -ntp -f "$HARNESS_DIR/pom.xml" test -Dgephi.version="$GEPHI_VERSION" \
      -Dplugin.groupId="$group" -Dplugin.artifactId="$artifact" -Dplugin.version="$version" \
      ${EXTRA_ARGS[@]+"${EXTRA_ARGS[@]}"}; then
    echo "::endgroup::"
  else
    echo "::endgroup::"
    echo "::error::SPI contract tests failed for $gav"
    collect_warnings "$gav"
    FAILED+=("$gav")
    continue
  fi
  collect_warnings "$gav"
  PASSED+=("$gav")
done

if [ ${#WARNINGS[@]} -gt 0 ] && [ -n "${GITHUB_STEP_SUMMARY:-}" ]; then
  {
    echo "### SPI contract test warnings"
    echo
    echo "These checks don't fail the build, but are worth a look in review."
    echo
    echo "| Plugin | Check | Warning |"
    echo "| --- | --- | --- |"
    for warning in "${WARNINGS[@]}"; do
      IFS=$'\t' read -r gav check message <<< "$warning"
      echo "| $(markdown_escape "$gav") | $(markdown_escape "$check") | $(markdown_escape "$message") |"
    done
  } >> "$GITHUB_STEP_SUMMARY"
fi

if [ ${#PASSED[@]} -gt 0 ]; then
  echo "SPI contract tests passed for: ${PASSED[*]}"
fi
if [ ${#FAILED[@]} -gt 0 ]; then
  echo "SPI contract tests failed for: ${FAILED[*]}"
  exit 1
fi
