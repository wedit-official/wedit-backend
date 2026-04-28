# Quality Scorecard

| 항목 | 현재 상태 | 메모 |
| --- | --- | --- |
| 문서화 | 낮음 -> 보강 중 | `AGENTS.md`와 `docs/` 허브를 도입함 |
| 도메인/JPA 테스트 | 중간 | 엔티티와 JPA 회귀 테스트가 이미 존재함 |
| 사용자 여정 테스트 | 낮음 -> 보강 중 | `member/auth` 통합 테스트를 추가함 |
| 구조 가드레일 | 없음 -> 도입 | ArchUnit 규칙으로 핵심 계층 의존을 검증함 |
| CI 테스트 게이트 | 없음 -> 도입 | PR 빌드에서 더 이상 `-x test`를 사용하지 않음 |
| Docker 패키징 게이트 | 없음 -> 도입 | PR/push CI와 하네스에서 Docker image build 계약을 검증함 |
| PR 리뷰 게이트 | 없음 -> 도입 | 자동 review activity, requested changes, unresolved thread, CI 상태를 merge 전 검증하고 `finish-pr.sh`로 develop 동기화와 branch/worktree cleanup까지 수행함 |
| 기능명세 추적 | 부분 도입 -> 하네스 편입 | `feature_id`, 정규화 CSV, 커버리지 매트릭스, test evidence를 검증함 |
| Obsidian 오류 원장 | 부분 도입 -> 운영 규칙화 | 재발 가능한 CI/리뷰/하네스 실패를 증상/원인/수정/체크포인트로 남김 |

## 다음 관심사
- OAuth 실제 연동을 위한 별도 하네스
- 문서 린트와 링크 검증
- 품질 점수 자동 갱신
- 기능 구현 시 coverage matrix status/evidence 자동 갱신 보조
