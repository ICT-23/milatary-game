# 11 — Audio and Cinematic Direction

**Purpose:** Authoritative audio identity, mix rules and cinematic plan. **Status:** Proposed · **Last reviewed:** 2026-10-18
Naming/format rules: [16](16_ASSET_PIPELINE_AND_FILE_CONVENTIONS.md). No copyrighted music, film audio or extracted game audio may be used (see [24](24_REFERENCE_ANALYSIS_AND_ORIGINALITY.md)).

## 1. Musical identity
"Quiet resolve under weather." Core instruments: low strings, felt piano, hand drum, a distinctive **bowed hammered-dulcimer** timbre as the Emberwatch motif (4-note rising figure). Academy = disciplined, warm; Reach = sparse, tense; Summit = full theme, brass swell. All music original/commissioned or licensed with written terms.

## 2. Music layering (stems per cue)
| Layer | Trigger |
|---|---|
| Ambient bed | Always |
| Pulse | Alert state or timer < 30 s |
| Action | Combat |
| Motif | Objective complete, discovery |
Transition rules: crossfade 1.5 s on beat grid where possible; stingers duck music 6 dB for 1.2 s.

## 3. Sound design catalogue
| Group | Examples | Notes |
|---|---|---|
| Ambience | Academy yard, forest night, dam wind, flood roar, interior hum | Looped, 60–90 s |
| Footsteps | Gravel, stone, wood, mud, water, metal | 4 variations each, pitch ±5% |
| Weapons | Carbine, SMG, DMR, pistol, reload, dry-fire | Short, mid-range-focused for phones |
| Impacts | Body-neutral thuds, cover hits, water splash | Non-graphic |
| UI | Tap, back, confirm, error, rank-up, medal | ≤ 400 ms |
| Beacon | Hum, step success, ignition swell | Signature sound |
| Cues | Detected "!", suspicious "?", discovery | Distinct shapes in frequency |

## 4. Dialogue
MVP: key lines voiced (prologue, callsign scene, log discovery, Keth parley, final broadcast ≈ 150 lines, estimate); remainder text + short vocal barks. Subtitles always available; speaker name coloured + label (not colour-only).

## 5. Mix and priority (high → low)
1. Dialogue 2. Alert cues 3. UI confirmations 4. Player weapon 5. Ally/enemy weapons 6. Footsteps 7. Ambience 8. Music.
Voice ducks music −8 dB and ambience −4 dB. Max concurrent voices: 16 (Low 12). Headroom −3 dBFS peak; loudness target −16 LUFS (music), −18 (SFX bus), (`Needs Validation`).

## 6. Player controls
Master, Music, SFX, Voice sliders; Mute all; Subtitles on/off/size; "Mono audio" toggle; respects system mute and audio focus (pause on call).

## 7. Formats
| Type | Format | Notes |
|---|---|---|
| Music | OGG Vorbis or AAC, 128 kbps stereo | Streamed |
| SFX | OGG 96 kbps mono, 44.1 kHz | Preloaded groups |
| Voice | OGG 64 kbps mono | Streamed per scene |
Budget: total audio ≤ 120 MB in APK/AAB base (estimate), rest via Play Asset Delivery if needed.

## 8. Cinematic plan (MVP, 5 moments; see [05](05_CAMPAIGN_AND_MISSION_DESIGN.md) §7)
| # | Moment | Method | Length |
|---|---|---|---|
| 1 | P-01 opening: beacon fails to light | Animated stills + in-engine | 30 s |
| 2 | M-104 callsign "Kindle" | In-engine letterbox | 20 s |
| 3 | M-202 keeper's log | Still + voiceover | 40 s |
| 4 | M-301 Keth encounter | In-engine | 45 s |
| 5 | M-401 final broadcast | In-engine montage | 45 s |
Rules: all skippable after first view (hold to skip); never gate gameplay > 45 s; environmental storytelling via props (keeper's cot, burned logbooks, children's drawings in the Tessel's Ford shelter).

## 9. Acceptance
| ID | Criterion |
|---|---|
| AUD-001 | Independent volume sliders persist |
| AUD-002 | Audio pauses/resumes correctly on call, background, headset unplug |
| AUD-003 | No clipping in mixed scenes (spectrum check) |
| AUD-004 | Latency from fire press to sound ≤ 100 ms (EXP-04) |
