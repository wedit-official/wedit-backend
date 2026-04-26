#!/usr/bin/env bash

set -euo pipefail

TEST_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

assert_file_exists() {
  [[ -f "$1" ]] || fail "expected file to exist: $1"
}

assert_dir_exists() {
  [[ -d "$1" ]] || fail "expected directory to exist: $1"
}

assert_contains() {
  local needle="$1"
  local haystack="$2"
  grep -Fq -- "${needle}" "${haystack}" || fail "expected '${needle}' in ${haystack}"
}

assert_output_contains() {
  local needle="$1"
  local output="$2"
  grep -Fq -- "${needle}" <<<"${output}" || fail "expected '${needle}' in output"
}

assert_command_fails() {
  if "$@"; then
    fail "expected command to fail: $*"
  fi
}

setup_sandbox_repo() {
  local sandbox="$1"
  mkdir -p "${sandbox}"
  git init "${sandbox}" >/dev/null 2>&1
  git -C "${sandbox}" config user.email "test@example.com"
  git -C "${sandbox}" config user.name "Test User"
  printf '# sandbox\n' > "${sandbox}/README.md"
  git -C "${sandbox}" add README.md
  git -C "${sandbox}" commit -m "init" >/dev/null 2>&1
  git -C "${sandbox}" branch -M main
  mkdir -p "${sandbox}/docs/exec-plans/active" "${sandbox}/.codex/task-state" "${sandbox}/src/main/java" "${sandbox}/src/test/java"
}
