#!/usr/bin/env bash
# Runs gephi-plugins-tests' SPI contract tests (see pom.xml in this folder) against this
# checkout's plugin modules, one Maven run per NBM module so each failure names a single plugin.
#
# Usage: .github/contract-tests/run.sh [modules/<Folder> ...]
#
# With no arguments, every module in the root pom.xml's <modules> list is tested. Otherwise only
# NBM modules under the given folders are; folders outside the reactor (e.g. a disabled plugin)
# are ignored. Modules must already be installed in the local Maven repository (mvn install).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
HARNESS="$ROOT/.github/contract-tests/pom.xml"
GEPHI_VERSION=$(sed -nE 's#.*<gephi\.version>(.*)</gephi\.version>.*#\1#p' "$ROOT/pom.xml" | head -1)

# "<packaging> <groupId>:<artifactId>:<version> <basedir>" for every project in the reactor
PROJECTS=$(cd "$ROOT" && mvn -B -q -ntp org.codehaus.mojo:exec-maven-plugin:3.5.0:exec \
  -Dexec.executable=echo \
  '-Dexec.args=${project.packaging} ${project.groupId}:${project.artifactId}:${project.version} ${project.basedir}')

TARGETS=()
while read -r packaging gav basedir; do
  [ "$packaging" = nbm ] || continue
  if [ $# -eq 0 ]; then
    TARGETS+=("$gav")
    continue
  fi
  for folder in "$@"; do
    case "$basedir/" in
      */"${folder%/}"/*) TARGETS+=("$gav"); break ;;
    esac
  done
done <<< "$PROJECTS"

if [ ${#TARGETS[@]} -eq 0 ]; then
  echo "No plugin modules to test."
  exit 0
fi

FAILED=()
for gav in "${TARGETS[@]}"; do
  IFS=: read -r group artifact version <<< "$gav"
  echo "::group::Contract tests: $gav"
  if mvn -B -ntp -f "$HARNESS" test -Dgephi.version="$GEPHI_VERSION" \
      -Dplugin.groupId="$group" -Dplugin.artifactId="$artifact" -Dplugin.version="$version"; then
    echo "::endgroup::"
  else
    echo "::endgroup::"
    echo "::error::SPI contract tests failed for $gav"
    FAILED+=("$gav")
  fi
done

if [ ${#FAILED[@]} -gt 0 ]; then
  echo "SPI contract tests failed for: ${FAILED[*]}"
  exit 1
fi
echo "SPI contract tests passed for: ${TARGETS[*]}"
