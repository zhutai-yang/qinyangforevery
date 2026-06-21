---
name: athlete-data-import
description: |
  Use when gathering, normalizing, exporting, or importing detailed table-tennis athlete data into the TT system. Trigger examples: "/athlete-data-import", "获取某个运动员数据", "导入运动员数据", "更新运动员资料", "快捷添加运动员数据".
---

# athlete-data-import

## Purpose

Collect public athlete data from verifiable sources, normalize it into the TT athlete import JSON contract, and import or update it through the admin Athlete quick-import entry.

## Source Rules

1. For current ranking, points, event calendar, upcoming matches, or latest results, browse and verify with official or primary sources first.
2. Prefer WTT, ITTF, Olympics, national federation pages, and official event pages. Use secondary sources only to cross-check or fill non-critical bio fields.
3. Record every source in `sources[]` with `name`, `url`, `retrieved_at`, and `note`.
4. If a field cannot be verified, omit it or put it under `extra_profile.unverified_notes`; do not invent values.

## Output Contract

Create a JSON object in this shape:

```json
{
  "athlete": {
    "name": "王楚钦",
    "name_en": "Wang Chuqin",
    "gender": "男",
    "birth_date": "2000-05-11",
    "birth_place": "吉林省吉林市",
    "nationality": "中国",
    "association": "中国 / China",
    "height_cm": 182,
    "dominant_hand": "左手",
    "playing_style": "左手横拍",
    "profile_title": "男单世界第一",
    "profile_summary": "公开可核验摘要，面向官网展示。",
    "hero_image_url": "",
    "social_url": "",
    "major_identity": "奥运冠军、世乒赛冠军"
  },
  "ranking": {
    "current_world_rank": 1,
    "highest_world_rank": 1,
    "ranking_points": 0,
    "ranking_date": "2026-06-18"
  },
  "achievements": [
    {
      "year": 2024,
      "event": "巴黎奥运会",
      "category": "混合双打",
      "result": "金牌",
      "partner_or_team": "孙颖莎",
      "source_url": "https://..."
    }
  ],
  "results": [
    {
      "date": "2026-02-01",
      "event": "WTT 新加坡大满贯",
      "category": "男单",
      "result": "冠军",
      "opponent": "林昀儒",
      "score": "4-0",
      "source_url": "https://..."
    }
  ],
  "upcoming_events": [
    {
      "start_date": "2026-06-25",
      "end_date": "2026-07-05",
      "event": "WTT 大满贯美国站",
      "level": "大满贯",
      "location": "美国",
      "status": "预告",
      "source_url": "https://..."
    }
  ],
  "sources": [
    {
      "name": "World Table Tennis",
      "url": "https://worldtabletennis.com/",
      "retrieved_at": "2026-06-18",
      "note": "排名/赛历核验"
    }
  ],
  "extra_profile": {
    "external_ids": {
      "wtt": "",
      "ittf": ""
    },
    "unverified_notes": []
  }
}
```

For a blank starter, copy `assets/import-template.json` and fill the fields.

## Import Steps

1. Build the JSON contract above.
2. Open Admin -> 明星选手 -> 快捷导入/更新.
3. Paste JSON and click preview. Confirm name, ranking, source count, achievements/results/upcoming counts.
4. Submit import.
5. Verify the athlete row updates in the list and, when featured/published elsewhere, the portal athlete page still renders.

## Mapping

| JSON path | TT storage |
|---|---|
| `athlete.*` | `reg_athlete` structured profile fields |
| `ranking.*` | `reg_athlete.current_world_rank`, `highest_world_rank`, `ranking_points`, `data_collected_at` |
| full JSON | `reg_athlete_import_snapshot.payload_json` |
| `sources[]` | `reg_athlete_data_source`, `reg_athlete.source_urls`, plus full snapshot |
| `achievements[]` | `reg_athlete_achievement` |
| `results[]` | `reg_athlete_result` |
| `upcoming_events[]` | `reg_athlete_upcoming_event`; create managed schedule records separately when the event is operated in TT |

## Completion Output

After using this skill, report:

| field | value |
|---|---|
| result | PASS / FAIL / BLOCKED |
| next_action | CONTINUE / RETRY / ESCALATE |
| change_digest | athlete name, source count, import target, fields updated |
