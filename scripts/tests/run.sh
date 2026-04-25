#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Git hooks export repository-scoped env vars such as GIT_DIR and GIT_INDEX_FILE.
# The sandbox tests spin up temporary repositories, so these vars must not leak in.
unset GIT_DIR
unset GIT_WORK_TREE
unset GIT_INDEX_FILE
unset GIT_PREFIX

failures=0

for test_file in "${SCRIPT_DIR}"/test_*.sh; do
  echo "Running $(basename "${test_file}")"
  if bash "${test_file}"; then
    echo "PASS $(basename "${test_file}")"
  else
    echo "FAIL $(basename "${test_file}")" >&2
    failures=$((failures + 1))
  fi
done

if [[ "${failures}" -gt 0 ]]; then
  echo "${failures} shell test(s) failed." >&2
  exit 1
fi
