#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"

git -C "${REPO_ROOT}" config core.hooksPath .githooks
printf 'Configured core.hooksPath to .githooks in %s\n' "${REPO_ROOT}"
