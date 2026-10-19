# 11 — Audio and Cinematic Direction

**Purpose:** Define the soundscape, mixing priorities and achievable cinematic plan.  
**Status:** Proposed  
**Last reviewed:** 2026-10-19

## Audio identity

The score should feel intimate and resilient rather than bombastic. Use low strings, muted percussion, breathy synth texture and a few distinctive metallic/wooden tones inspired by warning relays. Avoid recognizably imitative film/game themes, copyrighted samples, extracted reference-game sounds or music generated from prompts that request an artist's exact style.

## Audio families

| Family | MVP content | Priority |
|---|---|---|
| Music | title motif, academy theme, field tension loop, rescue cue, final resolution | High |
| Environment | wind, rain, distant water, academy PA, shelter murmur, relay hum | High |
| Movement | footsteps by surface, cloth movement, ladder/vault if used | Medium-high |
| Interaction | gate latch, switch, repair completion, evidence pickup, UI confirm/error | High |
| Combat | fictional shot layers, impacts, reload/empty cues, enemy alerts | Medium-high |
| Dialogue | short instructor lines, squad callouts, choice responses, ending scene | High for story; can ship text-first if voice budget is unavailable |
| UI | navigation, select, back, reward, locked/disabled, low health | Medium |
| Accessibility | subtitle timing, distinct non-color cues, optional haptics | High |

## Mixing hierarchy

1. Critical dialogue and objective cues.
2. Player action and immediate danger.
3. Companion/civilian signals.
4. Mission ambience.
5. Music and decorative texture.

When ducking is used, ease music down under dialogue rather than abruptly muting it. Audio sliders are independent for Master, Music, Ambience, Dialogue and SFX. Master mute does not permanently overwrite submix preferences. Device audio focus loss pauses or ducks appropriately and restores safely.

## Dialogue rules

- The subtitle text is always available for spoken story lines.
- Captions for gameplay-critical environmental sound can be enabled separately, e.g. `[relay alarm pulses]` or `[steps on metal walkway]`.
- Each line has a stable key, speaker, text, optional VO asset, timing estimate, emotion tag and localization status.
- Voice direction prioritizes intelligibility on a phone speaker; not every enemy needs a bark.
- Keep lines short enough to read during normal play. Important evidence is not delivered only through audio.
- The MVP may use text dialogue plus a small number of voiced anchors if full cast recording is not funded. The narrative must not refer to unheard off-screen voice events as if the player heard them.

## Music structure

- **Title:** motif introduced by a repeated three-note relay tone over a soft low register.
- **Academy:** steady, lighter percussion; not triumphant military brass.
- **Greywater exploration:** sparse pulse and bowed texture, leaving space for rain and footsteps.
- **Encounter:** short tension stem layered over ambience; avoid loud musical stingers for every hit.
- **Repair / rescue success:** motif resolves with a warm chord, not an exaggerated victory fanfare.
- **Ending:** theme returns with a new harmony that implies shared responsibility.

Loop points and stem transitions must be tested on device for clicks, timing drift and abrupt volume changes. Avoid dynamic mixing logic that depends on unstable frame timing.

## Cinematic plan for MVP

| ID | Moment | Target duration | Production method |
|---|---|---:|---|
| CIN-01 | Mara sees academy route board and storm warning | 15–25 sec | Real-time camera track, static crowd dressing. |
| CIN-02 | North Relay signal relights | 8–12 sec | Gameplay control briefly locks; player can skip after first watch. |
| CIN-03 | Evidence handover and ending | 30–45 sec | Real-time staging, 4–5 speaking characters maximum; alternate dialogue delivered through line data. |

No pre-rendered movie is required for MVP. Cutscenes must not hide a state transition or count a timer. When skipped, all objective, relationship, save and reward events must be applied exactly once.

## Audio naming and format

Use IDs like `AUD_SFX_RelaySwitch_On_v01`, `AUD_VO_Mara_M03_RecordChoice_A_v01`, `AUD_MUS_M05_Climax_StemA_v01`. Source stems and proof of license remain outside runtime package if unnecessary. Runtime format and compression should follow engine-supported codecs and be measured on target hardware. Dialogue/music may be streamed if memory tests show value; small UI/SFX cues can be loaded for responsive playback.

## Validation checklist

- Every key interaction communicates state even with audio muted.
- No high-frequency repeated sound becomes painful in a 10-minute loop.
- Subtitle line start/end matches speech; captions don't overlap objective alerts.
- Independent sliders work from settings and pause menu.
- Music transitions are tested after retry and checkpoint load.
- Interruptions (incoming call, notification, headset removal) do not leave audio muted or doubled.
- All music and SFX licenses are recorded before release.

## Related documents

[UI accessibility](08_UI_UX_AND_SCREEN_SPECIFICATIONS.md) · [Asset pipeline](16_ASSET_PIPELINE_AND_FILE_CONVENTIONS.md) · [QA](20_TESTING_AND_QUALITY_ASSURANCE.md)
