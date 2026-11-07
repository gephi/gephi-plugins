#!/usr/bin/env bash
# Runs gephi-plugins-tests' SPI contract tests (see pom.xml in this folder) against this
# checkout's plugin modules, one Maven run per NBM module so each failure names a single plugin.
#
# Usage: .github/contract-tests/run.sh [modules/<Folder> ...]
#
# With no arguments, every module in the root pom.xml's <modules> list is tested. Otherwise only
# NBM modules under the given folders are; a folder outside the reactor (e.g. a disabled plugin)
# is skipped with a warning. Modules must be installed in the local Maven repository first
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

  rm -rf "$HARNESS_DIR/target"
  echo "::group::Contract tests: $gav"
  if mvn -B -ntp -f "$HARNESS_DIR/pom.xml" test -Dgephi.version="$GEPHI_VERSION" \
      -Dplugin.groupId="$group" -Dplugin.artifactId="$artifact" -Dplugin.version="$version"; then
    echo "::endgroup::"
  else
    echo "::endgroup::"
    echo "::error::SPI contract tests failed for $gav"
    FAILED+=("$gav")
    continue
  fi
  PASSED+=("$gav")
done

if [ ${#PASSED[@]} -gt 0 ]; then
  echo "SPI contract tests passed for: ${PASSED[*]}"
fi
if [ ${#FAILED[@]} -gt 0 ]; then
  echo "SPI contract tests failed for: ${FAILED[*]}"
  exit 1
fi
