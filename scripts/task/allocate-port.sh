#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./common.sh
source "${SCRIPT_DIR}/common.sh"

slug="${1:-}"
[[ -n "${slug}" ]] || fail "usage: allocate-port.sh <slug>"

validate_slug "${slug}"
load_task_env "${slug}"

existing_port="$(port_for_slug_from_registry "${slug}" || true)"
if [[ -n "${existing_port}" ]]; then
  PORT="${existing_port}"
  write_task_env "${slug}"
  printf '%s\n' "${PORT}"
  exit 0
fi

for port in $(seq 18080 18199); do
  if port_reserved_by_other_slug "${slug}" "${port}"; then
    continue
  fi
  if port_listening "${port}"; then
    continue
  fi

  PORT="${port}"
  replace_port_registry_entry "${slug}" "${port}"
  write_task_env "${slug}"
  printf '%s\n' "${PORT}"
  exit 0
done

fail "no available port found in range 18080-18199"
