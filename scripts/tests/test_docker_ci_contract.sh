#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=./testlib.sh
source "${SCRIPT_DIR}/testlib.sh"

dockerfile="${TEST_ROOT}/Dockerfile"
staging_workflow="${TEST_ROOT}/.github/workflows/CI-stg.yml"
production_workflow="${TEST_ROOT}/.github/workflows/CI-prd.yml"

assert_contains 'RUN ./gradlew bootJar -x test --no-daemon' "${dockerfile}"

if grep -Fq 'RUN ./gradlew build -x test' "${dockerfile}"; then
  fail "Dockerfile must not run full Gradle build; it recursively executes shellTest in image builds."
fi

for workflow in "${staging_workflow}" "${production_workflow}"; do
  assert_contains 'docker build -t wedit-be:${{ env.IMAGE_TAG }} .' "${workflow}"
  assert_contains 'docker tag wedit-be:${{ env.IMAGE_TAG }} ${{ secrets.DOCKERHUB_USERNAME }}/wedit-be:${{ env.IMAGE_TAG }}' "${workflow}"
done
