# EXEC_PLAN: [feat] 업체 상세 CRUD API

- Task slug: `vendor-detail-crud-api`
- Base branch: `main`
- Feature branch: `codex/vendor-detail-crud-api`
- Worktree: `/Users/hyunwoo/Desktop/Project/Wedit/wedit-backend-worktrees/vendor-detail-crud-api`
- Port: `18080`
- Log dir: `/Users/hyunwoo/Desktop/Project/Wedit/wedit-backend-worktrees/wedit-backend-logs/vendor-detail-crud-api`
- Status: `verified`

## Required Reads
- [x] AGENTS.md
- [x] ARCHITECTURE.md
- [x] docs/index.md

## Related Docs
- [x] docs/architecture/backend-map.md
- [x] docs/testing/codex-harness.md
- [x] docs/specs/2026-wedit-backend-capabilities.md
- [x] docs/specs/2026-wedit-backend-coverage-matrix.md
- [x] docs/exec-plans/active/20260425-vendor-crud.md

## Related Feature IDs
- [x] vendor-detail-main-photo
- [x] vendor-detail-summary
- [x] vendor-detail-gallery-and-location

## Doc Notes
- Public API additions must keep the `controller -> service -> repository -> entity` dependency direction; controllers must not access repositories directly.
- Tests must run under the `test` profile without real MySQL, OAuth providers, or external network calls.
- The product spec wants vendor detail to expose summary, main photo, gallery, and location as one read model. This task extends that into full CRUD for the vendor detail resource.
- Existing `20260425-vendor-crud` plan establishes soft deletion via `isActive`; this task should preserve that policy for delete operations.
- `scripts/task/init-task.sh vendor-detail-crud-api "[feat] 업체 상세 CRUD API"` created the worktree branch, but the root checkout changed to `main` before helper scripts completed. Port/log/EXEC_PLAN were completed from the generated worktree.

## Goal
- Provide complete vendor detail CRUD API coverage for creating, reading, listing, updating, and soft-deleting vendor detail records, including representative photo, gallery/location data, and subtype-specific summary fields where the current domain model supports them.

## Approach
- Inspect current vendor/product entities, repositories, response wrappers, validation style, and existing tests before editing.
- Add controller/service/dto layers under `api/vendor`, keeping repository access in the service.
- Use `VendorCategory` to route subtype create/update behavior and expose a stable read DTO containing core summary, subtype payload, main media, gallery, and location fields.
- Keep delete as a soft delete using the existing active flag, and keep inactive records out of list/read contracts unless explicitly needed for tests.
- Add MockMvc/JPA-backed tests for normal CRUD, not-found/inactive edge cases, and request validation failures.
- Update the spec coverage matrix evidence for the three vendor detail feature rows after tests are in place.

## Step Plan
- Inspect `api/vendor`, `api/product`, common response/exception types, security test setup, and existing vendor tests.
- Define request/response DTOs for vendor detail create/update/read/list contracts.
- Implement service logic for category-specific entity creation/update and media ordering/main-photo selection.
- Add REST endpoints for create, list, detail read, update, and delete.
- Add integration tests covering successful CRUD, validation errors, and inactive/not-found behavior.
- Update docs/specs coverage matrix and run `./gradlew check build --no-daemon`.

## Done Criteria
- Vendor detail CRUD endpoints are implemented through controller/service/repository/entity layers.
- Read responses include summary, category, subtype details, representative media, gallery media, and location/map fields where available.
- Delete performs soft deletion and inactive vendors no longer appear in normal list/detail reads.
- Tests cover normal behavior, edge cases, and input validation.
- `./gradlew check build --no-daemon` passes before commit.

## Verification
- [x] `./gradlew test --tests com.wedit.backend.api.vendor.VendorDetailCrudIntegrationTest --no-daemon`
- [x] `python3 scripts/specs/verify_feature_harness.py`
- [x] `./gradlew check build --no-daemon`
