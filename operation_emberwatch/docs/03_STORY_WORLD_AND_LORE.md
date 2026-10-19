# 03 — Story, World and Lore

**Purpose:** Single authority for the fictional setting and campaign facts.  
**Status:** Proposed  
**Last reviewed:** 2026-10-19

## Premise

The Republic of Veyra is a fictional maritime nation shaped by steep green headlands, slate quarries and small fishing towns. Its eastern shore relies on a chain of civil-warning beacons and evacuation relays called **Emberwatch**. During the severe storm known as **The Long Rain**, several relays went dark. A civilian evacuation convoy was diverted, a coastal neighborhood was flooded, and two rival groups began blaming one another. The government describes the outage as sabotage; local communities believe warnings were delayed before the storm reached land.

Recruit **Mara Venn**, a skilled repair apprentice from the affected province, enters Meridian Defence Academy after helping residents move to higher ground. She wants to prevent another family from being left without a warning. She is not a chosen-one super-soldier: her distinctive strength is noticing practical failures and listening to people others dismiss.

The campaign begins as an academy assessment interrupted by a real emergency request. The squad is sent to assist with a relay recovery under a temporary ceasefire. Their mission reveals that a key Emberwatch maintenance log was marked destroyed before the storm. Evidence points not to a single mastermind, but to a chain of bad incentives, rushed decisions and intentional concealment by a few people.

## World and timeline

### Geography

- **Veyra:** coastal republic with three fictional regions: the capital district of Lumen Bay, the upland farming belt of Alder March and the storm-exposed eastern coast of Greywater Reach.
- **Meridian Defence Academy:** inland training complex built around a decommissioned flood-control station; includes parade yard, classroom, obstacle field, simulator hall and operations room.
- **Greywater Reach:** weathered sea walls, fishing terraces, salt grass, cliff roads and isolated relay cabins. Art direction is stylized grounded realism, not a representation of any real nation's bases.
- **North Relay Station:** a compact switchyard and beacon platform with a shelter below the control room. The MVP's main vertical-slice location.

### Timeline (all events fictional)

| Period | Event | Present-day consequence |
|---|---|---|
| 18 years before game | Veyra builds the first Emberwatch relays after a devastating coastal surge. | Warning infrastructure is a symbol of shared public safety. |
| 7 years before game | Budgets are centralized; local maintenance teams lose some authority. | Small failures take longer to report and fix. |
| 14 months before game | A pilot network upgrade is paused after inconsistent test results. | Some logs are split between paper archives and digital records. |
| Day 0: The Long Rain | A severe storm overwhelms two districts; three relays go dark. | Trust in public communications fractures. |
| Day 9 | A temporary ceasefire opens aid corridors, but armed groups still control road junctions. | Field missions are tense and time-sensitive. |
| Game opening | Mara begins her academy field assessment. | Her team must choose whether to follow a narrow written order or protect an at-risk shelter. |

## Factions

### Veyra Civil Response Service (VCRS)
A civilian-led organization responsible for warning systems, shelters and evacuation logistics. It includes capable responders, overstretched staff and leaders who disagree about what to disclose. The service is not inherently corrupt; its records are essential to resolving the mystery.

### Meridian Defence Corps (MDC)
A fictional uniformed defense force tasked with protecting relief corridors and restoring infrastructure during the crisis. Academy doctrine teaches protection and accountability, but some senior staff worry that exposing institutional failure will collapse public confidence.

### Greywater Mutual Front (GMF)
A loose coalition formed from coastal volunteers and former local security workers after warnings failed. It controls several road checkpoints and has a genuine protective mission, but some cells have become coercive and exploit the crisis. Its leader, Soren Vale, is a former civil engineer, not a cartoon villain.

### The Relay Committee
A cross-agency project group responsible for the Emberwatch upgrade. Members pursued different goals: reliability, cost control, political stability or career survival. The central secret concerns a deliberate alteration to one incident timeline, not a world-spanning conspiracy.

## The mystery

The mission record for North Relay was stamped “destroyed at 04:10,” but a paper service ticket places a technician at the relay at 05:02. That discrepancy establishes that someone edited a timeline. Further evidence shows that a manual failover was available, but its authorization was withheld while officials debated whether the unverified route could risk sending civilians into another flooded area. One commander later concealed that delay to prevent panic and preserve the ceasefire. A GMF unit separately used the outage to seize a relay component, believing the central government had abandoned Greywater.

The reveal is morally mixed: officials made a consequential mistake and then hid it; the mutual front used the failure for leverage; frontline responders worked with incomplete information. Mara's task is not to prove every person guilty or clear every institution. It is to restore warning capability, safeguard the incident record and persuade the people who still disagree to cooperate on the immediate rescue.

## Campaign arc

1. **Recruit to responsible teammate:** Mara learns that following directions is not the same as understanding their purpose.
2. **Skill to judgment:** Training skills become useful during a real coastal emergency.
3. **Suspicion to evidence:** the mismatch in records turns rumors into a verifiable, limited claim.
4. **Rivalry to cooperation:** Mara and rival cadet Ivo Senn discover that each has a blind spot.
5. **Revenge to accountability:** Soren Vale must choose between public exposure that endangers an aid corridor and controlled handover of the relay logs.
6. **Local resolution:** Emberwatch's North and East relays are restored, a protected log copy reaches an independent review panel, and evacuation routes reopen. A final note hints that other records require review, but the core local crisis is resolved.

## Themes and narrative rules

- Courage is acting despite uncertainty, not seeking danger.
- Responsibility requires admitting errors and repairing harm.
- Trust is built through consistent follow-through.
- Civilian protection is a core value and a mission constraint.
- Antagonists can be accountable without being inhuman.
- No real-world nation, active conflict, insignia, weapon program or political organization is mapped one-to-one onto Veyra.

## Canonical facts to preserve

- The event is called **The Long Rain**.
- The warning system is **Emberwatch**.
- The school is **Meridian Defence Academy**.
- The protagonist is **Mara Venn**.
- The rival is **Ivo Senn**.
- The Greywater Mutual Front's senior leader is **Soren Vale**.
- The crisis is resolved locally by the MVP ending. A sequel is not required to understand the story or receive emotional closure.

## Choices and outcome matrix

| Choice key | Mission | Decision | Immediate result | Later result |
|---|---|---|---|---|
| `CHOICE_01_SHELTER` | M01 | Help a shelter team during the assessment or complete the timed course first. | Course score differs; shelter lead reacts. | Campaign flag changes one debrief line; does not alter campaign access. |
| `CHOICE_02_RECORD` | M03 | Share a partial log with the squad or wait for authentication. | Trust reaction vs. caution line. | M04 dialogue changes; log is still recovered in either path. |
| `CHOICE_03_HANDOVER` | M05 | Give the full log directly to the panel with a public statement, or secure a witnessed sealed handover first. | Different staging and squad reactions. | Epilogue emphasis changes; both end with a credible investigation and relays restored. |

## Source of truth and changes

Do not add new factions or renumber missions casually. Story changes require updates to [characters](04_CHARACTER_AND_FACTION_BIBLE.md), [missions](05_CAMPAIGN_AND_MISSION_DESIGN.md), [save flags](15_DATA_MODEL_AND_SAVE_SYSTEM.md), [localization keys](16_ASSET_PIPELINE_AND_FILE_CONVENTIONS.md) and [tests](20_TESTING_AND_QUALITY_ASSURANCE.md).
