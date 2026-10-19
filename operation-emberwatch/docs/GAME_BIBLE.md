# OPERATION: EMBERWATCH — Game Bible

## Product north star

**Make the player feel responsible for people, not merely successful at tasks.** Operation: Emberwatch is a fictional single-player military adventure with academy training, environmental exploration, controlled squad interactions, consequential decisions and a complete first-campaign mystery.

- **Platform:** Android-first; portrait mobile interface.
- **Current implementation:** React Native + TypeScript + Expo, interactive top-down 2.5D-style map, SVG environmental art and local persistence.
- **Play style:** Objective-led exploration, readable movement and proximity interactions. Combat is not required in the first playable mission.
- **Audience:** Teen and adult players who enjoy character-driven military stories, training challenges, team relationships and mystery.
- **Non-goals for MVP:** Multiplayer, accounts, cloud saves, AI calls during gameplay, elaborate firearm simulation, heavy 3D rendering or live-service infrastructure.

## Original world: Asterra

Asterra is a fictional coastal nation rebuilding after years of regional tension. Its ceasefire is held together by fragile agreements and shared access to coastal settlements, emergency relays and supply corridors. The emergency communications network was designed to keep local communities connected during storms and evacuations. It is not invulnerable: local relay stations have different maintenance histories, and the institutions responsible for them do not always share information.

When a blackout isolates several settlements, an academy field exercise is suspended. The first signal log contains a timestamp from before the announced emergency. That small inconsistency becomes the first thread of a deliberate effort to destabilize the ceasefire.

The visual world should feel lived-in rather than militarized for spectacle: weathered concrete, coastal scrub, relay huts, worn service roads, utility markers, hand-painted safety signs and settlements that look worth protecting.

## Core cast and arcs

### Player recruit

The player chooses a callsign and one of three visual kits: Ember (balanced), Ranger (scout styling) or Tech (engineer styling). The recruit is capable but inexperienced. Their defining dilemma is knowing when to follow an order, when to ask for clarification and when responsibility requires independent action. The first mission's decision is intentionally uncomfortable rather than a simple good-versus-evil test.

### Captain Mara Voss — mentor

Disciplined, perceptive and reserved. Voss believes preparation saves lives. An earlier evacuation failed after she continued following the written plan when field conditions had changed. She has mistaken control for responsibility ever since. Her arc is learning that discipline should support judgment, not replace it.

### Idris Kane — medic

Calm, direct and empathetic. Idris notices the people who disappear behind a tactical summary. He challenges decisions that place civilian convenience below mission efficiency. His arc is to pair compassion with a practical understanding that preserving evidence can also protect future lives.

### Leena Suri — engineer

Analytical, curious and candid about uncertainty. She repairs field equipment and interprets communications traces. Leena knows the network has design weaknesses but initially assumes technical evidence will speak for itself. She learns that evidence must be communicated clearly and protected from political manipulation.

### Tomas Vale — reconnaissance specialist

Patient, observant and cautious. Tomas identifies alternative routes and small environmental inconsistencies. He hesitates to report information before he can verify it. His arc is learning to share uncertainty early so the squad can reason together.

### Rowan Kade — rival recruit

Talented, ambitious and quick to equate hesitation with failure. Rowan wants to be trusted with responsibility and fears being overlooked. Player decisions determine whether the rivalry becomes earned respect or a brittle competition. Rowan is not secretly evil; their confidence is strongest when the situation is simple and weakest when different kinds of responsibility collide.

### Antagonist: Sera Dain — former director of the Coastal Signal Directorate

Dain helped design Asterra's emergency relay system. During a devastating storm, fragmented responsibility and a failed handoff left her brother's settlement disconnected until help arrived too late. The official inquiry blamed local maintenance and moved on. Dain concluded that the ceasefire governments only fund resilience after a public crisis and that distributed local control allows officials to hide failures.

Dain now engineers a sequence of controlled communications disruptions and forges route markers to make a centralized emergency network appear indispensable. Her stated goal—ending the patchwork that leaves remote settlements at risk—is understandable. Her method deliberately exposes ordinary people to danger and manufactures evidence. She is accountable for the harm she causes, even if the problem she began trying to solve is real.

Her identity is revealed gradually through relay configuration signatures, a procurement trail, testimony from the settlements and a message that exposes both her motives and her deception. Avoid a single exposition monologue.

## First campaign: seven planned missions

1. **First Light** — academy orientation and movement training; establish the recruit's place in the squad. (A short prologue/training vignette that can be folded into Mission 01 for the MVP.)
2. **Signal in the Dust** — the playable MVP: meet Voss, restore the field relay, recover a suspicious signal trace, choose between immediate civilian support and securing extra evidence, then reach the rally point.
3. **Broken Relay** — compare logs from Kestrel Relay Station. The protagonist learns the anomaly was manually introduced, not caused by weather alone.
4. **Silent Crossing** — navigate a damaged causeway. Tomas's observations and Leena's environmental solution offer different safe routes; the player's earlier trust/intel values alter the available callout.
5. **The Long Return** — assist civilians through a disrupted evacuation corridor. Squad coordination, limited medical supplies and safe-route choices matter more than eliminating enemies.
6. **Fault Lines** — compare contradictory testimony. Voss's earlier failure becomes relevant, and Rowan's response depends on whether the player previously valued people, evidence or both.
7. **Ember Protocol** — combine the recovered log, squad trust and technical evidence to protect a regional relay network. The player exposes Dain's manufactured crisis while preserving proof of the real systemic failures she exploited.
8. **Last Signal** — campaign climax. The squad restores a community-controlled emergency path, transmits authenticated evidence to independent reviewers and confronts Dain. Dain is stopped, but her testimony forces the authorities to address the neglected infrastructure. The central mystery and protagonist's first leadership arc resolve in this campaign.

Production plan: build and polish Mission 01 before adding additional interactive levels. The current UI lists seven mission cards after folding the training prologue into the opening sequence; missions 02 onward are roadmap entries only.

## First mission beat sheet

1. **Academy range:** Player moves with the virtual joystick and reaches Captain Voss. The mentor establishes the squad's standards.
2. **Radio site:** The blackout is confirmed as unusual. Leena restores terminal power; the interaction teaches contextual actions.
3. **Signal log:** A timestamp predates the official alert. The player learns what the clue suggests and what it cannot yet prove.
4. **Dune-path call:** Civilians need help while an additional copy of the trace may be lost. Both options carry a cost.
5. **Western rally point:** The player reaches extraction, receives score/decision feedback and saves campaign flags locally.

## Choice and consequence contract

- **Evacuate civilians first:** +2 squad trust, no bonus intelligence from the decision itself, debrief emphasizes that no one was abandoned.
- **Secure more signal intelligence first:** −1 squad trust and +1 intel, debrief shows Idris's disappointment and the squad's questions.
- Both choices complete the mission. Never lock the player out of the primary campaign ending based on a single early decision.
- Store flags as structured local data; the mission should never depend on a live AI service.

## Art direction

- **Mood:** restrained coastal thriller, late-morning haze, quiet tension and human-scale stakes.
- **Primary colors:** deep blue-green `#0A1215`, dark slate `#111E22`, field green `#283A35`, signal lime `#C5E56A`, muted teal `#7ABDB4`, warning amber `#E79A59`, warm evidence-paper `#D0B88A`.
- **Avoid:** generic sci-fi neon, excessive orange/blue blockbuster grading, blood-heavy imagery, realistic insignia copied from real units, glossy gradients, asset-store-looking UI and cluttered HUDs.
- **Environment:** use layered terrain, contour routes, relay huts, field markers, coastal edges and sparse trees. Objectives should use distinct silhouettes and high-contrast markers.
- **Character language:** readable tiny silhouettes, differentiated gear shape and palette, restrained pose language. Do not use real-world unit patches or identifiable real persons.
- **UI:** editorial field dossier combined with tactical map. Compact uppercase labels, high-contrast body copy, generous tap zones, clear objective progression and persistent pause access.
- **Animation targets:** small marker pulse, restrained radio-signal rings, control feedback and short screen transitions. Keep motion optional or low-amplitude on lower-end phones.
- **Audio direction for a later phase:** wind and coastal ambience, soft equipment clicks, radio static, restrained score, distinct dialogue cues; provide mute/volume controls before adding sound.

## Mobile interaction standards

- Virtual joystick on the lower-left and contextual actions on the right, with enough spacing to avoid accidental taps.
- Proximity threshold for interaction, with readable distance feedback when out of range.
- Scan action identifies the active target and distance.
- Objective HUD displays one current action, objective count and progress segments.
- Pause preserves the current in-session field position and exposes control sensitivity.
- Scrollable dossier screens, portrait layout, safe-area support and clear contrast.
- No mission-completing action should trigger just by opening a screen; each step requires an explicit player action.

## Technical architecture

- `App.tsx`: screen flow, mission state machine, dialogue progression, choices and UI.
- `src/components/WorldMap.tsx`: vector environment, player marker and contextual objectives.
- `src/components/ControlPad.tsx`: PanResponder joystick and action controls.
- `src/data/missions.ts`: data-driven campaign roadmap/objective content.
- `src/lib/storage.ts`: resilient AsyncStorage save read/write/reset.
- `src/types/game.ts`: shared screen, player, interactable and save types.
- `src/theme.ts`: central color tokens.

Future mission data should declare objective IDs, prerequisites, target positions, dialogue events, story flags and completion rewards rather than adding one-off screen logic for each mission. Keep player/save state versioned for migrations.

## Definition of done for the MVP

- New player can create a callsign, choose a visual kit, read the briefing and enter the map.
- Joystick movement feels consistent; player stays within bounds and cannot pass through blocked structures.
- Every marker is reachable and can be interacted with only in range.
- Mission sequence cannot skip relay restoration or clue collection.
- Both civilian choices complete and produce the correct trust/intel/debrief changes.
- Pause and restart work; sensitivity affects movement speed.
- Save data survives process restart, malformed save data falls back safely, and reset clears it.
- A real Android build runs on a small and a large phone without cut-off controls or unreadable text.
- No claim of release readiness until device testing, Android App Bundle generation, content rating/store listing and Play Console checks are complete.
