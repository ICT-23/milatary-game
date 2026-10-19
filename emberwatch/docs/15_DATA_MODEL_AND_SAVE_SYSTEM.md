# 15 — Data Model and Save System

**Purpose:** Authoritative schemas, save safety and migration. **Status:** Proposed · **Last reviewed:** 2026-10-18
Static content is separate from player progress. Mission shape: [05](05_CAMPAIGN_AND_MISSION_DESIGN.md). Numbers: [07](07_PLAYER_PROGRESSION_AND_ECONOMY.md).

## 1. Separation
| Kind | Location | Mutable at runtime |
|---|---|---|
| Static content | `/src/content/*.json` bundled | No |
| Player save | App-private storage `saves/` | Yes |
| Settings | `settings.json` app-private | Yes |

## 2. SaveFile schema (v1)
```ts
type SaveFile = {
  schemaVersion: 1;
  meta: { slotId: string; createdAt: string; updatedAt: string; playSeconds: number; appVersion: string; checksum: string };
  profile: { name: string; appearance: { body: number; hair: number; skin: number }; difficulty: 'cadet'|'standard'|'veteran' };
  progress: {
    xp: number; rank: 'R1'|'R2'|'R3'|'R4'|'R5'|'R6';
    meritPoints: number; requisitionCredits: number;
    skills: Record<string, number>;
    completed: Record<string /*missionId*/, { medal: 'none'|'bronze'|'silver'|'gold'; bestTime?: number; optionalDone: string[] }>;
    unlocked: { missions: string[]; items: string[]; codex: string[]; outfits: string[] };
    equipment: { primary: string; sidearm: string; gadgets: string[]; outfit: string };
  };
  story: { flags: Record<string, boolean|string|number>; trust: { voss: number; relay: number; fathom: number; bram: number; corin: number }; choices: { id: string; value: string; missionId: string }[]; ending?: 'open_beacon'|'quiet_watch'|'held_line' };
  session?: MissionSnapshot; // only for in-progress mission checkpoint
};
type MissionSnapshot = { missionId: string; checkpointId: string; objectiveStates: Record<string,string>; player: { hp: number; pos: [number,number]; ammo: Record<string,number> }; squad: unknown; rngSeed: number; elapsed: number };
type Settings = { audio: { master:number; music:number; sfx:number; voice:number; muted:boolean }; graphics: { profile:'auto'|'low'|'medium'|'high'; fps:30|60 }; access: { subtitleSize:'s'|'m'|'l'; colorMode:'default'|'deut'|'prot'|'trit'; haptics:boolean; aimAssist:number; leftHanded:boolean; reducedMotion:boolean }; controls: { sprintToggle:boolean } };
```

## 3. Static content examples
```json
{ "id": "CH-02", "name": "Mara Voss", "callsign": "Anvil", "role": "instructor",
  "trustStart": 40, "abilities": ["order_hold"], "portraits": ["voss_neutral","voss_determined"] }
```
```json
{ "id": "carbine_c9", "slot": "primary", "name": "C-9 Ridge Carbine",
  "stats": { "damage": 10, "rpm": 520, "spread": 4, "mag": 30 }, "unlock": "start", "cost": 0 }
```

## 4. Safe write procedure (SAVE-001)
1. Serialise → compute SHA-256 checksum.
2. Write to `slot.tmp`.
3. Flush; read back and verify checksum.
4. Move current `slot.json` → `slot.bak` (keep last good).
5. Rename `slot.tmp` → `slot.json` (atomic rename on same volume).
6. Remove nothing else until verified.
Autosave triggers: checkpoint, mission end, app background, settings change (settings file only). Writes happen off the sim tick.

## 5. Recovery
| Situation | Behaviour |
|---|---|
| `slot.json` missing but `.tmp` valid | Promote tmp |
| Checksum mismatch | Load `.bak`; show recovery notice |
| Both corrupt | Offer "Start new" and "Export broken file"; never silently delete |
| Interrupted mid-mission | Resume from latest checkpoint snapshot |

## 6. Migration
Each schema bump adds `migrations/vN_to_vN+1.ts` pure functions; run sequentially at load; keep backup of pre-migration file; unknown newer version → refuse with message ("Update the app"). Tests with fixture saves for every version (SAVE-005).

## 7. Deletion and privacy
"Delete all data" in Settings removes saves, settings, logs. No personal data stored beyond player-chosen name (local only).

## 8. Acceptance
| ID | Criterion |
|---|---|
| SAVE-001 | 0 corrupt/lost saves across 200 forced-kill tests (EXP-05) |
| SAVE-002 | Resume returns player to last checkpoint with identical objective/flag state |
| SAVE-003 | Corrupt save triggers recovery flow |
| SAVE-004 | Delete-all leaves no residual files |
| SAVE-005 | Migration tests pass for all fixture versions |
