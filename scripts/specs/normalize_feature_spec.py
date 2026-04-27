#!/usr/bin/env python3

from __future__ import annotations

import argparse
import csv
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable


REPO_ROOT = Path(__file__).resolve().parents[2]
DEFAULT_RAW_SPEC = REPO_ROOT / "docs/specs/sources/2026-wedit-feature-spec.csv"
DEFAULT_OUTPUT = REPO_ROOT / "docs/specs/generated/2026-wedit-backend-features.csv"

OUTPUT_FIELDS = [
    "feature_id",
    "backend_scope",
    "capability",
    "domain_owner",
    "source_main",
    "source_sub",
    "item_name",
    "summary",
    "priority",
    "backend_difficulty",
    "source_file",
    "source_row",
]


@dataclass(frozen=True)
class FeatureRule:
    feature_id: str
    backend_scope: str
    capability: str
    domain_owner: str


def rule(feature_id: str, backend_scope: str, capability: str, domain_owner: str) -> FeatureRule:
    return FeatureRule(
        feature_id=feature_id,
        backend_scope=backend_scope,
        capability=capability,
        domain_owner=domain_owner,
    )


FEATURE_RULES = {
    ("1.로그인", "1.1 로그인", "로그인"): rule("auth-google-login", "backend_owned", "authentication", "member"),
    ("1.로그인", "1.2 회원가입", "회원가입"): rule("auth-signup-wedding-date", "backend_owned", "authentication", "member"),
    ("2.내비게이션", "2.1 홈", "홈 버튼"): rule("nav-home-button", "out_of_scope_for_backend", "navigation", "frontend"),
    ("2.내비게이션", "2.2 청첩장", "청첩장 버튼"): rule("nav-invitation-button", "out_of_scope_for_backend", "navigation", "frontend"),
    ("2.내비게이션", "2.3 투어일기", "투어일기 버튼"): rule("nav-tour-journal-button", "out_of_scope_for_backend", "navigation", "frontend"),
    ("2.내비게이션", "2.4 마이", "마이 버튼"): rule("nav-my-page-button", "out_of_scope_for_backend", "navigation", "frontend"),
    ("3.홈", "3.1 견적서", "견적서 버튼"): rule("home-quote-entry-button", "out_of_scope_for_backend", "navigation", "frontend"),
    ("3.홈", "3.1 견적서", "견적서 카드"): rule("home-quote-card-summary", "backend_shared", "quote-home", "quote"),
    ("3.홈", "3.1 견적서", "3.1.1 견적서 창 : 웨딩홀 리스트 +선택버튼"): rule("home-quote-weddinghall-list", "backend_owned", "quote-home", "quote"),
    ("3.홈", "3.1 견적서", "3.1.2 견적서 창 : 스튜디오 리스트  + 선택버튼"): rule("home-quote-studio-list", "backend_owned", "quote-home", "quote"),
    ("3.홈", "3.1 견적서", "3.1.3 견적서 창 : 드레스샵 리스트+ 선택버튼"): rule("home-quote-dress-list", "backend_owned", "quote-home", "quote"),
    ("3.홈", "3.1 견적서", "3.1.4 견적서 창 : 메이크업샵 리스트+ 선택버튼"): rule("home-quote-makeup-list", "backend_owned", "quote-home", "quote"),
    ("3.홈", "3.1 견적서", "3.1.5 견적서 창 : 마감"): rule("home-quote-soldout-status", "backend_owned", "quote-home", "quote"),
    ("3.홈", "3.1 견적서", "3.1.6 견적서 창 : 총합"): rule("home-quote-total", "backend_owned", "quote-home", "quote"),
    ("3.홈", "3.2 추천", "에디터 추천 게시물"): rule("home-recommendation-list", "backend_owned", "vendor-discovery", "vendor"),
    ("3.홈", "3.2 추천", "에디터 추천게시물 클릭"): rule("home-recommendation-detail-navigation", "out_of_scope_for_backend", "navigation", "frontend"),
    ("3.홈", "3.3 검색", "카테고리별 개별 검색 공간 이동"): rule("home-search-hub-navigation", "out_of_scope_for_backend", "navigation", "frontend"),
    ("3.홈", "3.3 검색", "3.3.1 웨딩홀 검색"): rule("home-search-weddinghall", "backend_owned", "vendor-discovery", "vendor"),
    ("3.홈", "3.3 검색", "3.3.2스튜디오 검색"): rule("home-search-studio", "backend_owned", "vendor-discovery", "vendor"),
    ("3.홈", "3.3 검색", "3.3.3 드레스샵 검색"): rule("home-search-dress", "backend_owned", "vendor-discovery", "vendor"),
    ("3.홈", "3.3 검색", "3.3.4 메이크업샵 검색"): rule("home-search-makeup", "backend_owned", "vendor-discovery", "vendor"),
    ("4.투어일지", "4.1 드레스 투어", "기록한 리스트들 표시"): rule("tour-journal-list-display", "backend_owned", "tour-journal", "tour-journal"),
    ("4.투어일지", "4.1 드레스 투어", "드레스 삭제"): rule("tour-journal-delete", "backend_owned", "tour-journal", "tour-journal"),
    ("4.투어일지", "4.1 드레스 투어", "> 버튼 누르면 5.2로"): rule("tour-journal-sketch-navigation", "out_of_scope_for_backend", "navigation", "frontend"),
    ("4.투어일지", "4.2 드레스 스케치", "5.2.1 기본 몸 이미지"): rule("dress-sketch-base-body", "out_of_scope_for_backend", "dress-sketch", "frontend"),
    ("4.투어일지", "4.2 드레스 스케치", "5.2.2 라인(A라인/벨라인/슬림라인 )"): rule("dress-sketch-line-style", "out_of_scope_for_backend", "dress-sketch", "frontend"),
    ("4.투어일지", "4.2 드레스 스케치", "5.2.3 넥라인(라운드/스퀘어/브이/오픈숄더/일자/하트/홀터)"): rule("dress-sketch-neckline", "out_of_scope_for_backend", "dress-sketch", "frontend"),
    ("4.투어일지", "4.2 드레스 스케치", "5.2.4 소재(비즈/실크/쉬폰/레이스)"): rule("dress-sketch-material", "out_of_scope_for_backend", "dress-sketch", "frontend"),
    ("5.마이", "5.1 프로필 화면", "프로필 정보 표시"): rule("profile-basic-info", "backend_owned", "profile", "member"),
    ("5.마이", "5.1 프로필 화면", "디데이"): rule("profile-d-day", "backend_owned", "profile", "member"),
    ("5.마이", "5.4 청첩장", "청첩장 제작 버튼"): rule("invitation-create-entry", "backend_shared", "invitation", "invitation"),
    ("5.마이", "5.4 청첩장", "미리보기 버튼"): rule("invitation-preview", "backend_shared", "invitation", "invitation"),
    ("5.마이", "5.4 청첩장", "테마"): rule("invitation-theme", "backend_owned", "invitation", "invitation"),
    ("5.마이", "5.4 청첩장", "기본정보"): rule("invitation-basic-info", "backend_owned", "invitation", "invitation"),
    ("5.마이", "5.4 청첩장", "인사말"): rule("invitation-greeting", "backend_owned", "invitation", "invitation"),
    ("5.마이", "5.4 청첩장", "예식일시"): rule("invitation-ceremony-datetime", "backend_owned", "invitation", "invitation"),
    ("5.마이", "5.4 청첩장", "예식장소"): rule("invitation-venue", "backend_owned", "invitation", "invitation"),
    ("5.마이", "5.4 청첩장", "교통수단"): rule("invitation-transportation", "backend_owned", "invitation", "invitation"),
    ("5.마이", "5.4 청첩장", "갤러리"): rule("invitation-gallery", "backend_owned", "invitation", "invitation"),
    ("5.마이", "5.4 청첩장", "엔딩 사진/문구(잠금!)"): rule("invitation-ending-locked-content", "backend_owned", "invitation", "invitation"),
    ("5.마이", "5.4 청첩장", "계좌번호"): rule("invitation-bank-account", "backend_owned", "invitation", "invitation"),
    ("5.마이", "5.4 청첩장", "참석여부"): rule("invitation-rsvp", "backend_owned", "invitation", "invitation"),
    ("5.마이", "5.4 청첩장", "공유하기"): rule("invitation-share", "backend_shared", "invitation", "invitation"),
    ("5.마이", "5.4 청첩장", "배경음악/파티룸(잠금!)"): rule("invitation-bgm-partyroom-locked-content", "backend_owned", "invitation", "invitation"),
    ("6.업체페이지-가격보기", "6.1 가격보기", "원하는 대행업체→옵션 클릭(기본→추가)"): rule("vendor-pricing-option-selection", "backend_owned", "vendor-pricing", "vendor"),
    ("6.업체페이지-가격보기", "6.3 견적서 넣기 버튼", "견적서담기"): rule("vendor-quote-add", "backend_owned", "vendor-pricing", "quote"),
    ("6.업체페이지-가격보기", "6.4 달력에 추가 버튼", "달력에 일정추가"): rule("vendor-calendar-add", "backend_shared", "vendor-pricing", "external-integration"),
    ("7.디테일", "7.1 메인사진", ""): rule("vendor-detail-main-photo", "backend_owned", "vendor-detail", "vendor"),
    ("7.디테일", "7.2 업체명 및 소개글", "업체측에서 가져온 데이터"): rule("vendor-detail-summary", "backend_owned", "vendor-detail", "vendor"),
    ("7.디테일", "7.4 업체측 상세컷 및 설명", "상세컷 및 설명 (위치포함)"): rule("vendor-detail-gallery-and-location", "backend_owned", "vendor-detail", "vendor"),
}


def clean_text(value: str | None) -> str:
    if value is None:
        return ""
    value = value.strip()
    if value in {"‘’", "''"}:
        return ""
    return value


def load_raw_rows(raw_path: Path) -> list[dict[str, str]]:
    rows: list[dict[str, str]] = []
    current_main = ""
    current_sub = ""

    with raw_path.open(newline="", encoding="utf-8-sig") as handle:
        reader = csv.DictReader(handle)
        for source_row, row in enumerate(reader, start=2):
            maybe_main = clean_text(row.get("세부 기능 (1)"))
            maybe_sub = clean_text(row.get("세부적인 구분"))

            if maybe_main:
                current_main = maybe_main
            if maybe_sub:
                current_sub = maybe_sub

            if not current_main or not current_sub:
                raise ValueError(f"Spec row {source_row} is missing section context.")

            item_name = clean_text(row.get("구분"))
            if item_name is None:
                item_name = ""

            rows.append(
                {
                    "source_main": current_main,
                    "source_sub": current_sub,
                    "item_name": item_name,
                    "summary": clean_text(row.get("부가설명")),
                    "priority": clean_text(row.get("우선순위")),
                    "backend_difficulty": clean_text(row.get("백엔드 난이도")),
                    "source_row": str(source_row),
                }
            )

    return rows


def build_feature_rows(raw_rows: Iterable[dict[str, str]], raw_path: Path) -> list[dict[str, str]]:
    feature_rows: list[dict[str, str]] = []
    try:
        raw_path_display = raw_path.resolve().relative_to(REPO_ROOT).as_posix()
    except ValueError:
        raw_path_display = raw_path.as_posix()

    for raw_row in raw_rows:
        key = (
            raw_row["source_main"],
            raw_row["source_sub"],
            raw_row["item_name"],
        )
        feature_rule = FEATURE_RULES.get(key)
        if feature_rule is None:
            raise KeyError(f"Unmapped feature row at source row {raw_row['source_row']}: {key}")

        feature_rows.append(
            {
                "feature_id": feature_rule.feature_id,
                "backend_scope": feature_rule.backend_scope,
                "capability": feature_rule.capability,
                "domain_owner": feature_rule.domain_owner,
                "source_main": raw_row["source_main"],
                "source_sub": raw_row["source_sub"],
                "item_name": raw_row["item_name"],
                "summary": raw_row["summary"] or raw_row["item_name"],
                "priority": raw_row["priority"],
                "backend_difficulty": raw_row["backend_difficulty"],
                "source_file": raw_path_display,
                "source_row": raw_row["source_row"],
            }
        )

    return feature_rows


def write_csv(rows: Iterable[dict[str, str]], output_path: Path) -> None:
    output_path.parent.mkdir(parents=True, exist_ok=True)
    with output_path.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=OUTPUT_FIELDS)
        writer.writeheader()
        writer.writerows(rows)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Normalize the 2026 Wedit feature-spec export into canonical backend feature rows."
    )
    parser.add_argument("--raw", type=Path, default=DEFAULT_RAW_SPEC, help="Path to the primary raw CSV export.")
    parser.add_argument(
        "--output",
        type=Path,
        default=DEFAULT_OUTPUT,
        help="Path to write the normalized feature CSV.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    raw_rows = load_raw_rows(args.raw)
    feature_rows = build_feature_rows(raw_rows, args.raw)
    write_csv(feature_rows, args.output)
    print(f"Wrote {len(feature_rows)} normalized rows to {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
