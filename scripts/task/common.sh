#!/usr/bin/env bash

set -euo pipefail

fail() {
  echo "Error: $*" >&2
  exit 1
}

repo_root() {
  if [[ -n "${STRICT_REPO_ROOT:-}" ]]; then
    (cd "${STRICT_REPO_ROOT}" && pwd)
  else
    git rev-parse --show-toplevel
  fi
}

validate_slug() {
  local slug="$1"
  [[ "${slug}" =~ ^[a-z0-9][a-z0-9-]*$ ]] || fail "slug must match ^[a-z0-9][a-z0-9-]*$: ${slug}"
}

plan_dir() {
  if [[ -n "${STRICT_PLAN_DIR:-}" ]]; then
    mkdir -p "${STRICT_PLAN_DIR}"
    (cd "${STRICT_PLAN_DIR}" && pwd)
  else
    local repo
    repo="$(repo_root)"
    mkdir -p "${repo}/docs/exec-plans/active"
    (cd "${repo}/docs/exec-plans/active" && pwd)
  fi
}

task_state_dir() {
  if [[ -n "${STRICT_TASK_STATE_DIR:-}" ]]; then
    mkdir -p "${STRICT_TASK_STATE_DIR}"
    (cd "${STRICT_TASK_STATE_DIR}" && pwd)
  else
    local repo
    repo="$(repo_root)"
    mkdir -p "${repo}/.codex/task-state"
    (cd "${repo}/.codex/task-state" && pwd)
  fi
}

worktree_base_dir() {
  if [[ -n "${STRICT_WORKTREE_BASE_DIR:-}" ]]; then
    mkdir -p "${STRICT_WORKTREE_BASE_DIR}"
    (cd "${STRICT_WORKTREE_BASE_DIR}" && pwd)
  else
    local repo parent
    repo="$(repo_root)"
    parent="$(dirname "${repo}")"
    mkdir -p "${parent}/wedit-backend-worktrees"
    (cd "${parent}/wedit-backend-worktrees" && pwd)
  fi
}

log_base_dir() {
  if [[ -n "${STRICT_LOG_BASE_DIR:-}" ]]; then
    mkdir -p "${STRICT_LOG_BASE_DIR}"
    (cd "${STRICT_LOG_BASE_DIR}" && pwd)
  else
    local repo parent
    repo="$(repo_root)"
    parent="$(dirname "${repo}")"
    mkdir -p "${parent}/wedit-backend-logs"
    (cd "${parent}/wedit-backend-logs" && pwd)
  fi
}

ports_registry() {
  if [[ -n "${STRICT_PORT_REGISTRY:-}" ]]; then
    mkdir -p "$(dirname "${STRICT_PORT_REGISTRY}")"
    printf '%s\n' "${STRICT_PORT_REGISTRY}"
  else
    printf '%s/ports.tsv\n' "$(task_state_dir)"
  fi
}

task_env_path() {
  local slug="$1"
  printf '%s/%s.env\n' "$(task_state_dir)" "${slug}"
}

default_feature_branch() {
  local slug="$1"
  printf 'codex/%s\n' "${slug}"
}

expected_worktree_path() {
  local slug="$1"
  printf '%s/%s\n' "$(worktree_base_dir)" "${slug}"
}

expected_log_dir() {
  local slug="$1"
  printf '%s/%s\n' "$(log_base_dir)" "${slug}"
}

today_stamp() {
  date '+%Y%m%d'
}

find_existing_plan_for_slug() {
  local slug="$1"
  find "$(plan_dir)" -maxdepth 1 -type f -name "*-${slug}.md" | head -n 1 || true
}

load_task_env() {
  local slug="$1"
  local env_file

  TASK_SLUG="${slug}"
  TASK_TITLE="${slug}"
  BASE_BRANCH="develop"
  FEATURE_BRANCH="$(default_feature_branch "${slug}")"
  EXEC_PLAN=""
  WORKTREE="$(expected_worktree_path "${slug}")"
  PORT=""
  LOG_DIR="$(expected_log_dir "${slug}")"
  ISSUE_NUMBER=""
  ISSUE_URL=""
  ISSUE_TITLE="${TASK_TITLE}"
  LAST_VERIFY_COMMAND=""
  LAST_VERIFY_STATUS=""
  LAST_VERIFY_AT=""

  env_file="$(task_env_path "${slug}")"
  if [[ -f "${env_file}" ]]; then
    # shellcheck disable=SC1090
    source "${env_file}"
  fi
}

write_task_env() {
  local slug="$1"
  local env_file
  env_file="$(task_env_path "${slug}")"

  mkdir -p "$(dirname "${env_file}")"
  {
    printf 'TASK_SLUG=%q\n' "${TASK_SLUG}"
    printf 'TASK_TITLE=%q\n' "${TASK_TITLE}"
    printf 'BASE_BRANCH=%q\n' "${BASE_BRANCH}"
    printf 'FEATURE_BRANCH=%q\n' "${FEATURE_BRANCH}"
    printf 'EXEC_PLAN=%q\n' "${EXEC_PLAN}"
    printf 'WORKTREE=%q\n' "${WORKTREE}"
    printf 'PORT=%q\n' "${PORT}"
    printf 'LOG_DIR=%q\n' "${LOG_DIR}"
    printf 'ISSUE_NUMBER=%q\n' "${ISSUE_NUMBER}"
    printf 'ISSUE_URL=%q\n' "${ISSUE_URL}"
    printf 'ISSUE_TITLE=%q\n' "${ISSUE_TITLE}"
    printf 'LAST_VERIFY_COMMAND=%q\n' "${LAST_VERIFY_COMMAND}"
    printf 'LAST_VERIFY_STATUS=%q\n' "${LAST_VERIFY_STATUS}"
    printf 'LAST_VERIFY_AT=%q\n' "${LAST_VERIFY_AT}"
  } > "${env_file}"
}

replace_port_registry_entry() {
  local slug="$1"
  local port="$2"
  local registry tmp_file
  registry="$(ports_registry)"
  tmp_file="$(mktemp)"

  mkdir -p "$(dirname "${registry}")"
  touch "${registry}"

  awk -F '\t' -v slug="${slug}" '$1 != slug { print $0 }' "${registry}" > "${tmp_file}"
  printf '%s\t%s\n' "${slug}" "${port}" >> "${tmp_file}"
  mv "${tmp_file}" "${registry}"
}

port_for_slug_from_registry() {
  local slug="$1"
  local registry
  registry="$(ports_registry)"
  [[ -f "${registry}" ]] || return 1

  awk -F '\t' -v slug="${slug}" '$1 == slug { print $2; exit }' "${registry}"
}

port_reserved_by_other_slug() {
  local slug="$1"
  local port="$2"
  local registry
  registry="$(ports_registry)"
  [[ -f "${registry}" ]] || return 1

  awk -F '\t' -v slug="${slug}" -v port="${port}" '$1 != slug && $2 == port { found=1 } END { exit found ? 0 : 1 }' "${registry}"
}

port_listening() {
  local port="$1"
  if command -v lsof >/dev/null 2>&1; then
    lsof -iTCP:"${port}" -sTCP:LISTEN -n -P >/dev/null 2>&1
  else
    return 1
  fi
}

plan_section_filled() {
  local plan_file="$1"
  local heading="$2"

  awk -v target="## ${heading}" '
    $0 == target {
      in_section = 1
      next
    }
    in_section && /^## / {
      exit found ? 0 : 1
    }
    in_section {
      if ($0 ~ /^[[:space:]]*$/) {
        next
      }
      if ($0 ~ /^[[:space:]]*(TBD|TODO)[[:space:]]*$/) {
        next
      }
      if ($0 ~ /^[[:space:]]*-\s*(TBD|TODO)?[[:space:]]*$/) {
        next
      }
      found = 1
      exit 0
    }
    END {
      if (found) {
        exit 0
      }
      exit 1
    }
  ' "${plan_file}"
}

plan_has_checked_required_read() {
  local plan_file="$1"
  local doc_path="$2"

  grep -Eq "^[[:space:]]*-[[:space:]]*\\[x\\][[:space:]]+${doc_path//\//\\/}[[:space:]]*$" "${plan_file}"
}

plan_has_checked_related_doc() {
  local plan_file="$1"

  awk '
    /^## Related Docs$/ {
      in_section = 1
      next
    }
    in_section && /^## / {
      exit found ? 0 : 1
    }
    in_section && $0 ~ /^[[:space:]]*-[[:space:]]*\[x\][[:space:]]+docs\/[^[:space:]]+/ {
      found = 1
      exit 0
    }
    END {
      if (found) {
        exit 0
      }
      exit 1
    }
  ' "${plan_file}"
}

plan_doc_notes_filled() {
  local plan_file="$1"
  plan_section_filled "${plan_file}" "Doc Notes"
}

plan_first_nonempty_bullet() {
  local plan_file="$1"
  local heading="$2"

  awk -v target="## ${heading}" '
    $0 == target {
      in_section = 1
      next
    }
    in_section && /^## / {
      exit
    }
    in_section && $0 ~ /^[[:space:]]*-[[:space:]]+/ {
      line = $0
      sub(/^[[:space:]]*-[[:space:]]+/, "", line)
      if (line !~ /^(TBD|TODO)[[:space:]]*$/ && line !~ /^[[:space:]]*$/) {
        print line
        exit
      }
    }
  ' "${plan_file}"
}

plan_is_complete() {
  local plan_file="$1"
  [[ -f "${plan_file}" ]] || return 1

  plan_section_filled "${plan_file}" "Goal" &&
    plan_section_filled "${plan_file}" "Approach" &&
    plan_section_filled "${plan_file}" "Step Plan" &&
    plan_section_filled "${plan_file}" "Done Criteria" &&
    plan_has_checked_required_read "${plan_file}" "AGENTS.md" &&
    plan_has_checked_required_read "${plan_file}" "ARCHITECTURE.md" &&
    plan_has_checked_required_read "${plan_file}" "docs/index.md" &&
    plan_has_checked_related_doc "${plan_file}" &&
    plan_doc_notes_filled "${plan_file}"
}
