#!/usr/bin/env bash

set -euo pipefail

tag="${1:-wedit-backend-harness:local}"
target="${DOCKER_TARGET:-builder}"
required="${STRICT_DOCKER_BUILD_REQUIRED:-${CI:-false}}"
docker_socket="${DOCKER_HOST:-unix:///var/run/docker.sock}"

if [[ "${docker_socket}" == unix://* ]]; then
  docker_socket="${docker_socket#unix://}"
else
  docker_socket="/var/run/docker.sock"
fi

docker_available() {
  command -v docker >/dev/null 2>&1 || return 1

  if command -v python3 >/dev/null 2>&1; then
    python3 - "${docker_socket}" <<'PY'
import socket
import sys

path = sys.argv[1]
client = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
client.settimeout(1)
try:
    client.connect(path)
except OSError:
    sys.exit(1)
finally:
    client.close()
PY
    return $?
  fi

  [[ -S "${docker_socket}" ]]
}

if ! docker_available; then
  if [[ "${required}" == "true" ]]; then
    echo "Docker is required for CI Docker image build but is not available." >&2
    exit 1
  fi

  echo "Skipping Docker image build because Docker is not available locally."
  exit 0
fi

docker build --target "${target}" -t "${tag}" .
