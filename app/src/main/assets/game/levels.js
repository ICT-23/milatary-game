// OPERATION: EMBERWATCH 3D — Mission & Environment Library
// Seven chapter definitions (objectives, hostiles, dialogue) plus the
// procedural Three.js environment builders used by game.js.
// Loaded after three.min.js and before game.js.

(function () {
  'use strict';

  // ---------------------------------------------------------------- THEMES
  var THEMES = {
    coastal: {
      sky: 0x20262C, fog: 0x20262C, fogDensity: 0.014,
      ground: 0xD6C29A, concrete: 0x4C5862, dark: 0x2A3138, olive: 0x667353,
      water: 0x1B3542, hemiSky: 0x81919E, hemiGround: 0x354A40,
      sun: 0xFFE8D0, hemiIntensity: 0.7, sunIntensity: 1.1,
      bounds: { minX: -105, maxX: 105, minZ: -100, maxZ: 80 }
    },
    vault: {
      sky: 0x0B0E11, fog: 0x0B0E11, fogDensity: 0.036,
      ground: 0x2E353C, concrete: 0x3A434B, dark: 0x181D22, olive: 0x4A5546,
      water: 0x14252C, hemiSky: 0x54646F, hemiGround: 0x0A0D10,
      sun: 0x9FB4C4, hemiIntensity: 0.55, sunIntensity: 0.55,
      bounds: { minX: -44, maxX: 44, minZ: -44, maxZ: 44 }
    },
    marsh: {
      sky: 0x1A2320, fog: 0x1A2320, fogDensity: 0.032,
      ground: 0x505C46, concrete: 0x5A625A, dark: 0x232B26, olive: 0x53614A,
      water: 0x223A36, hemiSky: 0x7C9184, hemiGround: 0x1D2620,
      sun: 0xCFE3D0, hemiIntensity: 0.75, sunIntensity: 0.8,
      bounds: { minX: -58, maxX: 58, minZ: -46, maxZ: 46 }
    },
    highway: {
      sky: 0x2B3037, fog: 0x2B3037, fogDensity: 0.018,
      ground: 0x3A3F45, concrete: 0x565E64, dark: 0x242A30, olive: 0x5C6650,
      water: 0x25333D, hemiSky: 0x93A3B0, hemiGround: 0x272D33,
      sun: 0xFFD9B0, hemiIntensity: 0.7, sunIntensity: 1.0,
      bounds: { minX: -60, maxX: 60, minZ: -46, maxZ: 46 }
    },
    mine: {
      sky: 0x120E0C, fog: 0x120E0C, fogDensity: 0.042,
      ground: 0x3A322B, concrete: 0x4A4038, dark: 0x1C1714, olive: 0x4E4A38,
      water: 0x1E2622, hemiSky: 0x6B5C4E, hemiGround: 0x100C0A,
      sun: 0xFFC98A, hemiIntensity: 0.5, sunIntensity: 0.45,
      bounds: { minX: -52, maxX: 48, minZ: -44, maxZ: 44 }
    },
    fort: {
      sky: 0x232A32, fog: 0x232A32, fogDensity: 0.016,
      ground: 0x555D61, concrete: 0x62696D, dark: 0x2A3036, olive: 0x5D6851,
      water: 0x2A3A44, hemiSky: 0x8FA0AC, hemiGround: 0x272D33,
      sun: 0xFFE4C0, hemiIntensity: 0.75, sunIntensity: 1.0,
      bounds: { minX: -44, maxX: 44, minZ: -46, maxZ: 44 }
    },
    platform: {
      sky: 0x0E1820, fog: 0x0E1820, fogDensity: 0.024,
      ground: 0x33424C, concrete: 0x3E4E58, dark: 0x1B242A, olive: 0x4E5A4A,
      water: 0x0F2028, hemiSky: 0x6C8798, hemiGround: 0x0A1216,
      sun: 0xBFD8E8, hemiIntensity: 0.6, sunIntensity: 0.7,
      bounds: { minX: -42, maxX: 42, minZ: -42, maxZ: 42 }
    }
  };

  // -------------------------------------------------------------- MISSIONS
  // Objective kinds: talk | destroy | scan | interact | choice | defend | reach
  //   auto: true  -> completes on arrival, no interact button.
  var MISSIONS = {
    M1_SIGNAL_IN_DUST: {
      id: 'M1_SIGNAL_IN_DUST',
      chapter: 1,
      title: 'SIGNAL IN THE DUST',
      sector: 'SECTOR 4 // COASTAL RELAY OUTPOST',
      theme: 'coastal',
      spawn: [0, 0, -32],
      parSeconds: 210,
      reserveAmmo: 120,
      supplies: [
        { type: 'ammo', x: -6, z: -20 },
        { type: 'medkit', x: 8, z: 12 },
        { type: 'ammo', x: 16, z: 26 },
        { type: 'medkit', x: -18, z: 36 }
      ],
      startDialogue: {
        speaker: 'CAPTAIN MARA VOSS',
        text: 'Spectre-7, this is Voss. Every telemetry node across Sector 4 went dark twelve minutes ago. Link up with Tomas at the forward post and find out what hit us. Keep your eyes on the sky - recon drones are already circling.'
      },
      drones: [
        { x: -8, y: 5.5, z: 18, hp: 3, squad: 'tower', patrol: 5 },
        { x: 10, y: 6.0, z: 28, hp: 3, squad: 'tower', patrol: 5 },
        { x: 0, y: 7.0, z: 36, hp: 3, squad: 'tower', patrol: 4 }
      ],
      objectives: [
        {
          kind: 'talk', title: 'REPORT TO TOMAS VALE',
          detail: 'Cross the perimeter gauntlet to the forward recon liaison.',
          pos: [0, 0, -8], radius: 6, label: 'TALK [TOMAS VALE]',
          dialogue: {
            speaker: 'TOMAS VALE (RECON)',
            text: 'Spectre! Good to see you upright. Relay Tower Alpha took a foreign pulse burst. Three autonomous scout drones are holding the tower perimeter. Clear them, then scan the dish before the sabotage data degrades.'
          }
        },
        {
          kind: 'destroy', title: 'NEUTRALIZE SURVEILLANCE DRONES',
          detail: 'Destroy the rogue telemetry scouts circling Relay Tower Alpha.',
          pos: [0, 0, 25], squad: 'tower', required: 3
        },
        {
          kind: 'scan', title: 'SCAN DAMAGED RELAY ARRAY',
          detail: 'Use the field scanner on the sabotaged transmitter dish.',
          pos: [0, 0, 25], radius: 8, label: 'SCAN [RELAY TOWER]',
          dialogue: {
            speaker: 'CAPTAIN MARA VOSS',
            text: 'Telemetry confirmed - this was not a fault. Someone spliced the tower by hand. The auxiliary generator southeast is overheating. Get there before it vents into the civilian shelter!'
          }
        },
        {
          kind: 'choice', title: 'RESOLVE GENERATOR CRISIS',
          detail: 'Reach the generator vault and settle the emergency override.',
          pos: [24, 0, 20], radius: 7, label: 'OVERRIDE [GENERATOR]'
        },
        {
          kind: 'reach', title: 'EXTRACT AT LZ FALCON',
          detail: 'Reach the coastal extraction pad and signal the chopper.',
          pos: [-25, 0, 45], radius: 7, label: 'SIGNAL [EXTRACTION LZ]'
        }
      ],
      choice: {
        title: 'CRITICAL EMERGENCY OVERRIDE',
        description: 'Auxiliary generator is venting coolant into the civilian shelter while saboteurs wipe stolen cipher drives.',
        options: [
          {
            id: 'SAFEGUARD_CIVILIANS', label: 'SAFEGUARD CIVILIANS', tone: 'civilians',
            summary: 'Vent pressure away from 40 trapped families. The stolen ciphers escape, but no lives are lost.',
            scoreBonus: 800,
            dialogue: {
              speaker: 'IDRIS KANE (MEDIC)',
              text: 'Bless you, Spectre! You diverted the superheated coolant away from the fallout shelter. All forty families are safe. Now proceed to LZ Falcon for extraction!'
            }
          },
          {
            id: 'INTERCEPT_DATA', label: 'INTERCEPT CIPHER DATA', tone: 'intel',
            summary: 'Overclock the receiver to capture enemy frequency codes. The bunker sustains heat damage.',
            scoreBonus: 1000,
            dialogue: {
              speaker: 'ROWAN KADE (RIVAL)',
              text: 'Ruthless work, Spectre! You captured the saboteurs\' encrypted frequency codes before they wiped the server. High command will commend this. Head to LZ Falcon!'
            }
          }
        ]
      },
      extraction: { pos: [-25, 0.5, 45], smoke: true }
    },

    M2_BROKEN_RELAY: {
      id: 'M2_BROKEN_RELAY',
      chapter: 2,
      title: 'BROKEN RELAY',
      sector: 'SECTOR 5 // SUBTERRANEAN CABLE VAULTS',
      theme: 'vault',
      spawn: [-34, 0, -34],
      parSeconds: 300,
      reserveAmmo: 150,
      supplies: [
        { type: 'ammo', x: -30, z: -6 },
        { type: 'medkit', x: -8, z: 20 },
        { type: 'ammo', x: 8, z: 8 },
        { type: 'medkit', x: 10, z: -24 }
      ],
      startDialogue: {
        speaker: 'LEENA SURI (ENGINEER)',
        text: 'Spectre, Leena. The blackout signatures trace below the old garrison. I am at the cable vault service hatch. Bring a rifle - the conduits are rigged and someone left drones on rack patrol.'
      },
      drones: [
        { x: -6, y: 4.5, z: -18, hp: 4, squad: 'splice', patrol: 6, alertRadius: 26 },
        { x: 18, y: 5.0, z: 8, hp: 3, squad: 'splice', patrol: 6, alertRadius: 26 },
        { x: 6, y: 4.2, z: -32, hp: 3, squad: 'splice', patrol: 5, alertRadius: 26 },
        { x: -24, y: 4.6, z: 20, hp: 3, squad: 'rack', patrol: 8, alertRadius: 24, damage: 7 },
        { kind: 'sentinel', x: -6, z: -8, squad: 'guard' }
      ],
      objectives: [
        {
          kind: 'talk', title: 'REPORT TO LEENA SURI',
          detail: 'Find the engineer at the vault service hatch.',
          pos: [-30, 0, -28], radius: 6, label: 'TALK [LEENA SURI]',
          dialogue: {
            speaker: 'LEENA SURI (ENGINEER)',
            text: 'Three splice nodes are feeding a decoy loop into the uplink. Cut them, then reconnect the main terminal on the far wall. Drones flock to gunfire down here, so move quiet and hit hard.'
          }
        },
        {
          kind: 'destroy', title: 'CLEAR THE SPLICE GUARD',
          detail: 'Destroy the three drones guarding the splice nodes.',
          pos: [0, 0, -8], squad: 'splice', required: 3
        },
        {
          kind: 'interact', title: 'CUT SPLICE NODE ALPHA',
          detail: 'Sever the west conduit splice.',
          pos: [-22, 0, 18], radius: 6, label: 'CUT [NODE ALPHA]',
          dialogue: {
            speaker: 'LEENA SURI (ENGINEER)',
            text: 'Alpha is dead. The loop is degrading - keep cutting. Beta sits behind the rack trench to the east.'
          }
        },
        {
          kind: 'interact', title: 'CUT SPLICE NODE BETA',
          detail: 'Sever the east conduit splice.',
          pos: [16, 0, -6], radius: 6, label: 'CUT [NODE BETA]',
          dialogue: {
            speaker: 'LEENA SURI (ENGINEER)',
            text: 'Beta down. Last one is north, tucked between the coolant pipes. Grab my optical splicing kit on the way out - we will need it at the terminal.'
          }
        },
        {
          kind: 'interact', title: 'CUT SPLICE NODE GAMMA',
          detail: 'Sever the north conduit splice.',
          pos: [24, 0, 22], radius: 6, label: 'CUT [NODE GAMMA]',
          dialogue: {
            speaker: 'TOMAS VALE (RECON)',
            text: 'That is the decoy loop dead. Spectre, I am seeing uplink power returning on my scope. Finish at the main terminal and get to the service elevator.'
          }
        },
        {
          kind: 'interact', title: 'RECONNECT MAIN UPLINK',
          detail: 'Restore the uplink terminal on the far wall.',
          pos: [30, 0, -30], radius: 7, label: 'RECONNECT [UPLINK]',
          dialogue: {
            speaker: 'CAPTAIN MARA VOSS',
            text: 'Uplink is green - we can hear the coast again. Excellent work, Spectre. Service elevator is south-west. Exfil is standing by.'
          }
        },
        {
          kind: 'reach', title: 'RETURN TO SERVICE ELEVATOR',
          detail: 'Board the elevator for surface extraction.',
          pos: [-34, 0, 32], radius: 6, label: 'BOARD [SERVICE ELEVATOR]'
        }
      ],
      extraction: { pos: [-34, 0.5, 32], smoke: true }
    },

    M3_SILENT_CROSSING: {
      id: 'M3_SILENT_CROSSING',
      chapter: 3,
      title: 'SILENT CROSSING',
      sector: 'OAKHAVEN ESTUARY // TIDAL SALT FLATS',
      theme: 'marsh',
      spawn: [-40, 0, -36],
      parSeconds: 330,
      reserveAmmo: 150,
      supplies: [
        { type: 'ammo', x: -14, z: -14 },
        { type: 'medkit', x: 6, z: -6 },
        { type: 'ammo', x: 20, z: 12 },
        { type: 'medkit', x: -20, z: 18 }
      ],
      startDialogue: {
        speaker: 'TOMAS VALE (RECON)',
        text: 'Highway bridge is locked down by a hostile recon patrol, so we cross the salt flats instead. Two sensor beacons watch the causeway. Kill the drones, drop the beacons, and meet me at the rally point.'
      },
      drones: [
        { x: -12, y: 5.2, z: -12, hp: 4, squad: 'patrol', patrol: 7 },
        { x: 10, y: 5.6, z: 4, hp: 4, squad: 'patrol', patrol: 7 },
        { x: 32, y: 6.2, z: 18, hp: 4, squad: 'patrol', patrol: 8 },
        { kind: 'sentinel', x: 12, z: 6, squad: 'guard' },
        { kind: 'walker', x: -14, z: 6, squad: 'guard' }
      ],
      objectives: [
        {
          kind: 'reach', title: 'MOVE ONTO THE SALT FLATS',
          detail: 'Leave the treeline and follow the causeway north.',
          pos: [-30, 0, -26], radius: 6, auto: true,
          dialogue: {
            speaker: 'TOMAS VALE (RECON)',
            text: 'Causeway is clear enough, but you are in the open now. Watch for the drone patrol sweep before you push the middle flats.'
          }
        },
        {
          kind: 'destroy', title: 'SILENCE THE PATROL',
          detail: 'Destroy all three patrol drones before they call in your position.',
          pos: [10, 0, 4], squad: 'patrol', required: 3
        },
        {
          kind: 'interact', title: 'DISABLE SENSOR BEACON ONE',
          detail: 'Kill the south beacon before it reports your crossing.',
          pos: [28, 0, -4], radius: 6, label: 'DISABLE [BEACON ONE]',
          dialogue: {
            speaker: 'LEENA SURI (ENGINEER)',
            text: 'One beacon down, telemetry is blind on the south bank. There is a second node just north of the causeway bend.'
          }
        },
        {
          kind: 'interact', title: 'DISABLE SENSOR BEACON TWO',
          detail: 'Kill the north beacon and open the crossing.',
          pos: [-2, 0, 24], radius: 6, label: 'DISABLE [BEACON TWO]',
          dialogue: {
            speaker: 'CAPTAIN MARA VOSS',
            text: 'Beacon net is dark. Nobody saw you cross. Rally point is west of your position - move before the tide turns.'
          }
        },
        {
          kind: 'reach', title: 'REACH ESTUARY RALLY POINT',
          detail: 'Meet the squad at the western rally marker.',
          pos: [-34, 0, 34], radius: 7, label: 'SIGNAL [RALLY POINT]'
        }
      ],
      extraction: { pos: [-34, 0.5, 34], smoke: true }
    },

    M4_THE_LONG_RETURN: {
      id: 'M4_THE_LONG_RETURN',
      chapter: 4,
      title: 'THE LONG RETURN',
      sector: 'NORTH RIDGE HIGHWAY // EVACUATION ESCORT',
      theme: 'highway',
      spawn: [-52, 0, -30],
      parSeconds: 360,
      reserveAmmo: 180,
      supplies: [
        { type: 'ammo', x: -30, z: -16 },
        { type: 'medkit', x: -8, z: 8 },
        { type: 'ammo', x: 18, z: 14 },
        { type: 'medkit', x: 34, z: -6 }
      ],
      startDialogue: {
        speaker: 'IDRIS KANE (MEDIC)',
        text: 'Spectre, thank the ridge you are here. Civilian transports are stacked behind me with wounded aboard. Interceptor probes are tearing up the trestle bridge - clear them and get this convoy rolling.'
      },
      drones: [
        { x: -20, y: 6.0, z: -14, hp: 4, squad: 'interceptors', patrol: 8, damage: 8 },
        { x: 0, y: 6.2, z: 2, hp: 4, squad: 'interceptors', patrol: 8, damage: 8 },
        { x: 24, y: 6.4, z: -4, hp: 4, squad: 'interceptors', patrol: 8, damage: 8 },
        { x: 36, y: 7.0, z: 14, hp: 5, squad: 'interceptors', patrol: 6, damage: 10 },
        { kind: 'sentinel', x: 20, z: -6, squad: 'guard' },
        { kind: 'walker', x: -22, z: 6, squad: 'guard' }
      ],
      objectives: [
        {
          kind: 'talk', title: 'LINK UP WITH MEDIC KANE',
          detail: 'Report to Idris at the convoy staging point.',
          pos: [-44, 0, -24], radius: 6, label: 'TALK [IDRIS KANE]',
          dialogue: {
            speaker: 'IDRIS KANE (MEDIC)',
            text: 'Four interceptor probes are cycling over the bridge. If one of them tags a transport, the blast takes the casualties with it. Clear the sky, then sweep the deck for charges.'
          }
        },
        {
          kind: 'destroy', title: 'CLEAR THE INTERCEPTOR PROBES',
          detail: 'Destroy all four interceptor probes before they hit the convoy.',
          pos: [8, 0, 0], squad: 'interceptors', required: 4
        },
        {
          kind: 'interact', title: 'SWEEP THE TRESTLE BRIDGE',
          detail: 'Clear the demo charges rigged under the bridge deck.',
          pos: [4, 0, 20], radius: 7, label: 'SWEEP [TRESTLE BRIDGE]',
          dialogue: {
            speaker: 'LEENA SURI (ENGINEER)',
            text: 'Charges disarmed - the bridge will hold one convoy. Lead the transports across and get them behind the Fort Vane gate.'
          }
        },
        {
          kind: 'reach', title: 'ESCORT CONVOY TO FORT VANE',
          detail: 'Move ahead of the transports to the fort gate.',
          pos: [48, 0, -24], radius: 7, label: 'ESCORT [FORT VANE GATE]'
        }
      ],
      extraction: { pos: [48, 0.5, -24], smoke: true }
    },

    M5_FAULT_LINES: {
      id: 'M5_FAULT_LINES',
      chapter: 5,
      title: 'FAULT LINES',
      sector: 'MINING DISTRICT 9 // ABANDONED SHAFTS',
      theme: 'mine',
      spawn: [-40, 0, 36],
      parSeconds: 390,
      reserveAmmo: 180,
      supplies: [
        { type: 'ammo', x: -18, z: 18 },
        { type: 'medkit', x: -4, z: -8 },
        { type: 'ammo', x: 16, z: -18 },
        { type: 'medkit', x: 0, z: -34 }
      ],
      startDialogue: {
        speaker: 'CAPTAIN MARA VOSS',
        text: 'The detonators that killed Sector 4 came from our own reserve. Clearance codes were issued inside the district. District 9 is sealed - go in, access Shaft 3, and find out who signed for those charges.'
      },
      drones: [
        { x: -16, y: 5.0, z: 18, hp: 4, squad: 'security', patrol: 7, damage: 9 },
        { x: 10, y: 5.2, z: 6, hp: 4, squad: 'security', patrol: 7, damage: 9 },
        { x: 26, y: 5.4, z: -12, hp: 4, squad: 'security', patrol: 6, damage: 9 },
        { x: -6, y: 6.0, z: -24, hp: 5, squad: 'security', patrol: 6, damage: 11 },
        { kind: 'sentinel', x: -16, z: -10, squad: 'guard' },
        { kind: 'walker', x: 16, z: -4, squad: 'guard' }
      ],
      objectives: [
        {
          kind: 'reach', title: 'ENTER MINING DISTRICT 9',
          detail: 'Advance through the sealed district gate.',
          pos: [-30, 0, 26], radius: 6, auto: true,
          dialogue: {
            speaker: 'ROWAN KADE (RIVAL)',
            text: 'Spectre. Funny meeting you down here. Command sent me to secure the terminal, but the security grid is not accepting my codes. Clear the drones and maybe I will let you read the screen.'
          }
        },
        {
          kind: 'destroy', title: 'BREAK THE SECURITY GRID',
          detail: 'Destroy the four mine security drones holding the shaft.',
          pos: [4, 0, 0], squad: 'security', required: 4
        },
        {
          kind: 'interact', title: 'ACCESS SHAFT 3 TERMINAL',
          detail: 'Pull the clearance ledger from the encrypted terminal.',
          pos: [28, 0, -28], radius: 7, label: 'ACCESS [SHAFT 3 TERMINAL]',
          dialogue: {
            speaker: 'LEENA SURI (ENGINEER)',
            text: 'Ledger is open. Those charges were signed out on Rowan Kade\'s authorization code. He is standing right behind you, Spectre. Your call.'
          }
        },
        {
          kind: 'choice', title: 'CONFRONT ROWAN KADE',
          detail: 'Decide how to handle the operative who signed for the charges.',
          pos: [10, 0, -32], radius: 6, label: 'CONFRONT [ROWAN KADE]'
        },
        {
          kind: 'reach', title: 'WITHDRAW TO EXTRACTION',
          detail: 'Leave District 9 before the shaft seals.',
          pos: [-40, 0, 32], radius: 6, label: 'WITHDRAW [EXTRACTION]'
        }
      ],
      choice: {
        title: 'CONFRONTATION // ROWAN KADE',
        description: 'Kade has not drawn on you. His code signed out the detonators, but he claims he was ordered to move the crates and never saw the manifest.',
        options: [
          {
            id: 'DETAIN_KADE', label: 'DETAIN KADE', tone: 'civilians',
            summary: 'Place him under arrest and hand the ledger to Voss. Command gets a clean chain of evidence.',
            scoreBonus: 1000,
            dialogue: {
              speaker: 'CAPTAIN MARA VOSS',
              text: 'Kade is in restraints and the ledger is in my hands. That is how a soldier handles a compromised operative, Spectre. Command will have questions, but the record is clean.'
            }
          },
          {
            id: 'COVER_KADE', label: 'COVER FOR KADE', tone: 'intel',
            summary: 'Bury his name in the report and keep him in the field as an asset. He owes you now.',
            scoreBonus: 800,
            dialogue: {
              speaker: 'ROWAN KADE (RIVAL)',
              text: 'You buried my name in that report. I do not forget debts, Spectre. When this goes loud, you will have someone in the room who owes you.'
            }
          }
        ]
      },
      extraction: { pos: [-40, 0.5, 32], smoke: true }
    },

    M6_EMBER_PROTOCOL: {
      id: 'M6_EMBER_PROTOCOL',
      chapter: 6,
      title: 'EMBER PROTOCOL',
      sector: 'FORT VANE // CENTRAL SIGNAL TOWER',
      theme: 'fort',
      spawn: [0, 0, -40],
      parSeconds: 420,
      reserveAmmo: 210,
      supplies: [
        { type: 'ammo', x: -14, z: -22 },
        { type: 'medkit', x: 12, z: -18 },
        { type: 'ammo', x: -16, z: 6 },
        { type: 'medkit', x: 18, z: 8 },
        { type: 'ammo', x: -8, z: 18 }
      ],
      startDialogue: {
        speaker: 'CAPTAIN MARA VOSS',
        text: 'This is the one, Spectre. They are coming for the central transmitter before the ceasefire committee convenes. Three assault waves. Hold the courtyard and keep that tower breathing.'
      },
      drones: [
        { kind: 'sentinel', x: -22, z: 20, squad: 'guard' },
        { kind: 'sentinel', x: 26, z: 14, squad: 'guard' }
      ],
      objectives: [
        {
          kind: 'talk', title: 'REPORT TO VOSS AT THE TOWER',
          detail: 'Take your position at the central transmitter.',
          pos: [0, 0, -28], radius: 6, label: 'REPORT [CAPT. VOSS]',
          dialogue: {
            speaker: 'CAPTAIN MARA VOSS',
            text: 'Defensive ring is set. First wave is already on approach from the north - they will focus the transmitter. If the manifold takes a hit we lose coolant, so keep them off the tower.'
          }
        },
        {
          kind: 'defend', title: 'HOLD THE PERIMETER',
          detail: 'Survive three assault waves aimed at the central transmitter.',
          pos: [0, 0, 10], squad: 'fort',
          waves: [
            [
              { x: -16, y: 6, z: -10, hp: 4, damage: 8, alertRadius: 40 },
              { x: 16, y: 6, z: -10, hp: 4, damage: 8, alertRadius: 40 }
            ],
            [
              { x: -24, y: 6, z: 6, hp: 4, damage: 9, alertRadius: 44 },
              { x: 0, y: 7, z: 12, hp: 5, damage: 9, alertRadius: 44 },
              { x: 24, y: 6, z: 6, hp: 4, damage: 9, alertRadius: 44 }
            ],
            [
              { x: -14, y: 6, z: 22, hp: 5, damage: 10, alertRadius: 46 },
              { x: 14, y: 6, z: 22, hp: 5, damage: 10, alertRadius: 46 },
              { x: 0, y: 8, z: 28, hp: 6, damage: 12, alertRadius: 46 },
              { kind: 'walker', x: 0, z: 18, alertRadius: 48 }
            ]
          ]
        },
        {
          kind: 'interact', title: 'RESTORE COOLANT MANIFOLD',
          detail: 'Patch the damaged cooling manifold on the west wall.',
          pos: [-18, 0, 14], radius: 6, label: 'PATCH [COOLANT MANIFOLD]',
          dialogue: {
            speaker: 'LEENA SURI (ENGINEER)',
            text: 'Manifold is holding. The transmitter can take the load now. Signal the ceasefire committee before the next wave forms.'
          }
        },
        {
          kind: 'reach', title: 'SIGNAL THE CEASEFIRE COMMITTEE',
          detail: 'Transmit the all-clear from the north gate.',
          pos: [0, 0, -38], radius: 7, label: 'TRANSMIT [CEASEFIRE]'
        }
      ],
      extraction: { pos: [0, 0.5, -38], smoke: true }
    },

    M7_LAST_SIGNAL: {
      id: 'M7_LAST_SIGNAL',
      chapter: 7,
      title: 'LAST SIGNAL',
      sector: 'OFFSHORE PLATFORM CHIMERA // TRANSMISSION DECK',
      theme: 'platform',
      spawn: [-28, 0, 30],
      parSeconds: 480,
      reserveAmmo: 240,
      supplies: [
        { type: 'ammo', x: -16, z: 12 },
        { type: 'medkit', x: 0, z: -2 },
        { type: 'ammo', x: 14, z: -18 },
        { type: 'medkit', x: -6, z: 20 }
      ],
      startDialogue: {
        speaker: 'CAPTAIN MARA VOSS',
        text: 'This is it. Platform Chimera is set to fire a false-flag EMP and blame the republic. The architect is aboard. Neutralize the deck, reach the master console, and send the truth before this rig goes into the sea.'
      },
      drones: [
        { x: -10, y: 6, z: 10, hp: 5, squad: 'elite', patrol: 7, damage: 10, fireRate: 1.9 },
        { x: 8, y: 6, z: 14, hp: 5, squad: 'elite', patrol: 7, damage: 10, fireRate: 1.9 },
        { x: 20, y: 6, z: -4, hp: 6, squad: 'elite', patrol: 6, damage: 11, fireRate: 1.8 },
        { x: -2, y: 7, z: -14, hp: 5, squad: 'elite', patrol: 6, damage: 10, fireRate: 1.9 },
        { x: 24, y: 7, z: 22, hp: 6, squad: 'elite', patrol: 5, damage: 11, fireRate: 1.7 },
        { kind: 'sentinel', x: -22, z: -2, squad: 'guard', alertRadius: 24 },
        { kind: 'sentinel', x: 2, z: 26, squad: 'guard', alertRadius: 24 },
        { kind: 'walker', x: 14, z: 2, squad: 'guard' }
      ],
      objectives: [
        {
          kind: 'reach', title: 'BOARD THE TRANSMISSION DECK',
          detail: 'Push through the container yard onto the deck.',
          pos: [-22, 0, 26], radius: 6, auto: true,
          dialogue: {
            speaker: 'TOMAS VALE (RECON)',
            text: 'Deck is crawling with elite guard drones. They are faster than anything we have faced. Keep moving, do not let them box you in.'
          }
        },
        {
          kind: 'destroy', title: 'BREAK THE ELITE GUARD',
          detail: 'Destroy all five elite guard drones on the transmission deck.',
          pos: [8, 0, 6], squad: 'elite', required: 5
        },
        {
          kind: 'interact', title: 'ACCESS MASTER CONSOLE',
          detail: 'Reach the master console and override the uplink.',
          pos: [24, 0, -13], radius: 7, label: 'ACCESS [MASTER CONSOLE]',
          dialogue: {
            speaker: 'THE ARCHITECT',
            text: 'You are standing in the control room of the only truth this republic understands, Spectre. The EMP is already armed. You can broadcast my confession, or you can burn it all and be a hero.'
          }
        },
        {
          kind: 'choice', title: 'THE ARCHITECT\'S OFFER',
          detail: 'Choose what Asterra hears tonight.',
          pos: [32, 0, -16], radius: 7, label: 'DECIDE [BROADCAST]'
        },
        {
          kind: 'destroy', title: 'APPREHEND THE ARCHITECT',
          detail: 'The architect is sealed in a command frame on the deck. Break it before the scuttle charges trip.',
          pos: [12, 0, -14], squad: 'architect', required: 1,
          spawn: [
            { kind: 'boss', x: 12, z: -14, label: 'THE ARCHITECT // COMMAND FRAME' }
          ]
        },
        {
          kind: 'reach', title: 'EVACUATE BEFORE SCUTTLE',
          detail: 'Reach the helipad before the platform floods.',
          pos: [-28, 0, 30], radius: 7, label: 'SIGNAL [HELIPAD EVAC]'
        }
      ],
      choice: {
        title: 'THE LAST TRANSMISSION',
        description: 'The architect\'s confession is loaded on the array. The scuttle charges are counting down either way.',
        options: [
          {
            id: 'BROADCAST_TRUTH', label: 'BROADCAST THE TRUTH', tone: 'civilians',
            summary: 'Send the full confession coast to coast. The republic sees everything, including our own failures.',
            scoreBonus: 1200,
            dialogue: {
              speaker: 'CAPTAIN MARA VOSS',
              text: 'It is going out on every band. Half the coast is listening to a dead man confess. History will argue about tonight for a hundred years, Spectre. Get off that platform.'
            }
          },
          {
            id: 'DESTROY_ARRAY', label: 'DESTROY THE ARRAY', tone: 'intel',
            summary: 'Burn the transmission rig with the architect aboard. No EMP, no confession, no witnesses.',
            scoreBonus: 1000,
            dialogue: {
              speaker: 'ROWAN KADE (RIVAL)',
              text: 'Array is scrap and the architect went down with it. The EMP can never fire. Some truths are too heavy to hand to the public, Spectre - but command will know what you did.'
            }
          }
        ]
      },
      extraction: { pos: [-28, 0.5, 30], smoke: true }
    },

    M8_ASHES_OF_CHIMERA: {
      id: 'M8_ASHES_OF_CHIMERA',
      chapter: 8,
      title: 'ASHES OF CHIMERA',
      sector: 'OFFSHORE PLATFORM CHIMERA // LOWER DECKS',
      theme: 'platform',
      spawn: [-28, 0, 30],
      parSeconds: 300,
      reserveAmmo: 210,
      supplies: [
        { type: 'ammo', x: -16, z: 12 },
        { type: 'medkit', x: 2, z: 10 },
        { type: 'ammo', x: 14, z: -18 },
        { type: 'medkit', x: -22, z: 26 }
      ],
      startDialogue: {
        speaker: 'LEENA SURI (ENGINEER)',
        text: 'Deck is already listing eight degrees and the scuttle charges are burning. The flight recorder on the lower deck clears Emberwatch by itself. Get it, and get off before this rig becomes a reef.'
      },
      drones: [
        { x: -10, y: 6, z: 10, hp: 4, squad: 'pursuit', patrol: 7 },
        { x: 8, y: 6, z: 14, hp: 4, squad: 'pursuit', patrol: 7 },
        { x: 20, y: 6, z: -4, hp: 5, squad: 'pursuit', patrol: 6 },
        { x: -2, y: 7, z: -14, hp: 4, squad: 'pursuit', patrol: 6 },
        { kind: 'sentinel', x: -22, z: -2, squad: 'lockdown', alertRadius: 26 },
        { kind: 'walker', x: 14, z: 2, squad: 'lockdown' }
      ],
      objectives: [
        {
          kind: 'reach', title: 'REACH THE LOWER DECK',
          detail: 'Cross the container yard before the deck floods.',
          pos: [-22, 0, 26], radius: 6, auto: true,
          dialogue: {
            speaker: 'TOMAS VALE (RECON)',
            text: 'The architect left a retention squad behind. They are not trying to win, Spectre - they are trying to buy the scuttle time. Do not let them slow you down.'
          }
        },
        {
          kind: 'destroy', title: 'SILENCE THE PURSUIT',
          detail: 'Destroy the four retention drones covering the stairwell.',
          pos: [8, 0, 6], squad: 'pursuit', required: 4
        },
        {
          kind: 'scan', title: 'RECOVER THE FLIGHT RECORDER',
          detail: 'Pull the recorder from the sensor mast before it burns.',
          pos: [24, 0, -13], radius: 7, label: 'SCAN [FLIGHT RECORDER]',
          dialogue: {
            speaker: 'LEENA SURI (ENGINEER)',
            text: 'Recorder is intact. Two hours of deck telemetry, every order the architect gave, and the falsified EMP authorisation on top of it. This is the whole case in one box.'
          }
        },
        {
          kind: 'destroy', title: 'BREAK THE LOCKDOWN',
          detail: 'Destroy the lockdown emplacement and the armoured biped holding the deck.',
          pos: [-6, 0, -8], squad: 'lockdown', required: 2
        },
        {
          kind: 'choice', title: 'THE RECORDER',
          detail: 'Decide what happens to the evidence.',
          pos: [32, 0, -16], radius: 7, label: 'DECIDE [RECORDER]'
        },
        {
          kind: 'reach', title: 'EXTRACT BEFORE SCUTTLE',
          detail: 'Reach the helipad before the platform goes under.',
          pos: [-28, 0, 30], radius: 7, label: 'SIGNAL [HELIPAD EVAC]'
        }
      ],
      choice: {
        title: 'WHAT THE RECORDER PROVES',
        description: 'The recorder is the only copy. The rig will take it to the bottom with it if it stays aboard.',
        options: [
          {
            id: 'BROADCAST_TRUTH', label: 'TRANSMIT FROM THE DECK', tone: 'civilians',
            summary: 'Push the recorder to every coastal relay before the rig floods. The truth becomes public and unstoppable.',
            scoreBonus: 1100,
            dialogue: {
              speaker: 'CAPTAIN MARA VOSS',
              text: 'Uplink held for forty seconds and it was enough. The recorder is on nine relays and still copying. Whatever happens to Chimera tonight, the truth is already ashore.'
            }
          },
          {
            id: 'DESTROY_ARRAY', label: 'CARRY IT OUT BY HAND', tone: 'intel',
            summary: 'Keep the recorder sealed and deliver it to the tribunal yourself. Slower, safer, entirely under your control.',
            scoreBonus: 950,
            dialogue: {
              speaker: 'ROWAN KADE (RIVAL)',
              text: 'Good soldier. Evidence in a sealed case, presented to a court, not to a mob. Command can build a hundred cases on this box - as long as nobody drops it.'
            }
          }
        ]
      },
      extraction: { pos: [-28, 0.5, 30], smoke: true }
    },

    M9_THE_QUIET_COAST: {
      id: 'M9_THE_QUIET_COAST',
      chapter: 9,
      title: 'THE QUIET COAST',
      sector: 'OAKHAVEN ESTUARY // SURVIVING CELLS',
      theme: 'marsh',
      spawn: [-40, 0, -36],
      parSeconds: 360,
      reserveAmmo: 210,
      supplies: [
        { type: 'ammo', x: -14, z: -14 },
        { type: 'medkit', x: 6, z: -6 },
        { type: 'ammo', x: 20, z: 12 },
        { type: 'medkit', x: -20, z: 18 }
      ],
      startDialogue: {
        speaker: 'TOMAS VALE (RECON)',
        text: 'Two cells got off Chimera before it sank and they are dug into the estuary hides. They have the names of everyone who signed the EMP order. We take the hides, we take the network with them.'
      },
      drones: [
        { x: -12, y: 5.2, z: -12, hp: 4, squad: 'north', patrol: 7 },
        { x: 10, y: 5.6, z: 4, hp: 4, squad: 'north', patrol: 7 },
        { x: 32, y: 6.2, z: 18, hp: 4, squad: 'north', patrol: 8 },
        { x: 22, y: 6.0, z: -10, hp: 4, squad: 'north', patrol: 7 },
        { x: -26, y: 5.8, z: 6, hp: 4, squad: 'south', patrol: 7 },
        { x: -8, y: 6.4, z: 20, hp: 4, squad: 'south', patrol: 7 },
        { x: 4, y: 5.6, z: -24, hp: 4, squad: 'south', patrol: 6 },
        { x: 30, y: 6.6, z: -2, hp: 5, squad: 'south', patrol: 6 },
        { kind: 'sentinel', x: 12, z: 6, squad: 'north', alertRadius: 26 },
        { kind: 'walker', x: -14, z: 6, squad: 'south' }
      ],
      objectives: [
        {
          kind: 'talk', title: 'LINK UP WITH TOMAS',
          detail: 'Report to the estuary observation post.',
          pos: [-36, 0, -30], radius: 6, label: 'TALK [TOMAS VALE]',
          dialogue: {
            speaker: 'TOMAS VALE (RECON)',
            text: 'North hide is the radio cell, south hide is the muscle. Take the radio cell first and they cannot call the other one in. Both hides are covered by a turret, so use the reeds.'
          }
        },
        {
          kind: 'destroy', title: 'CLEAR THE NORTH HIDE',
          detail: 'Destroy the radio cell and its sentinel turret.',
          pos: [10, 0, 4], squad: 'north', required: 5
        },
        {
          kind: 'destroy', title: 'CLEAR THE SOUTH HIDE',
          detail: 'Destroy the south cell and the armoured biped guarding it.',
          pos: [-12, 0, -12], squad: 'south', required: 5
        },
        {
          kind: 'defend', title: 'HOLD THE BRIDGE',
          detail: 'Survive three counter-attacks on the causeway bridge.',
          pos: [-2, 0, 24], squad: 'coast',
          waves: [
            [
              { x: -20, y: 6, z: 10, hp: 4, damage: 9, alertRadius: 44 },
              { x: 18, y: 6, z: 12, hp: 4, damage: 9, alertRadius: 44 }
            ],
            [
              { x: -24, y: 6, z: -6, hp: 4, damage: 10, alertRadius: 46 },
              { x: 0, y: 7, z: 6, hp: 5, damage: 10, alertRadius: 46 },
              { x: 22, y: 6, z: -4, hp: 4, damage: 10, alertRadius: 46 }
            ],
            [
              { x: -10, y: 6, z: 20, hp: 5, damage: 11, alertRadius: 48 },
              { x: 12, y: 6, z: 22, hp: 5, damage: 11, alertRadius: 48 },
              { kind: 'walker', x: -14, z: 6, alertRadius: 48 }
            ]
          ]
        },
        {
          kind: 'choice', title: 'THE CELLS ARE BROKEN',
          detail: 'Decide the fate of the survivors.',
          pos: [20, 0, 12], radius: 7, label: 'DECIDE [SURVIVORS]'
        },
        {
          kind: 'reach', title: 'EXTRACT FROM THE ESTUARY',
          detail: 'Reach the rally point west of the causeway.',
          pos: [-34, 0, 34], radius: 7, label: 'SIGNAL [RALLY POINT]'
        }
      ],
      choice: {
        title: 'TERMS OF SURRENDER',
        description: 'Eleven survivors, one recorder, and a tribunal that will want names.',
        options: [
          {
            id: 'TAKE_PRISONERS', label: 'TAKE THEM PRISONERS', tone: 'civilians',
            summary: 'Bind the survivors and hand them to the tribunal. Slower, riskier, and it keeps Emberwatch on the right side of the law.',
            scoreBonus: 1000,
            dialogue: {
              speaker: 'IDRIS KANE (MEDIC)',
              text: 'Eleven sets of restraints and one working radio. They will talk before the tribunal sits, and this time they will be swearing it under oath instead of over a comms link.'
            }
          },
          {
            id: 'BURN_HIDE', label: 'BURN THE HIDE', tone: 'intel',
            summary: 'Put the hide and everything in it to the torch. No survivors to testify and no names to leak.',
            scoreBonus: 900,
            dialogue: {
              speaker: 'ROWAN KADE (RIVAL)',
              text: 'The hide is ash and the network died with it. Clean, quiet, and nothing anyone can pull out of the mud later. Command will not ask how.'
            }
          }
        ]
      },
      extraction: { pos: [-34, 0.5, 34], smoke: true }
    },

    M10_EMBERWATCH: {
      id: 'M10_EMBERWATCH',
      chapter: 10,
      title: 'EMBERWATCH',
      sector: 'FORT VANE // ACADEMY PARADE GROUND',
      theme: 'fort',
      spawn: [0, 0, -40],
      parSeconds: 540,
      reserveAmmo: 240,
      supplies: [
        { type: 'ammo', x: -14, z: -22 },
        { type: 'medkit', x: 12, z: -18 },
        { type: 'ammo', x: -16, z: 6 },
        { type: 'medkit', x: 18, z: 8 },
        { type: 'ammo', x: -8, z: 18 }
      ],
      startDialogue: {
        speaker: 'CAPTAIN MARA VOSS',
        text: 'They are not here for the transmitter any more. They are here for the academy. Cadets are still inside those barracks, Spectre, so the parade ground holds or we lose everything we were ever defending.'
      },
      drones: [
        { x: -18, y: 6, z: -10, hp: 5, squad: 'assault', patrol: 7, damage: 10 },
        { x: 16, y: 6, z: -10, hp: 5, squad: 'assault', patrol: 7, damage: 10 },
        { x: -4, y: 7, z: 4, hp: 5, squad: 'assault', patrol: 6, damage: 10 },
        { x: 24, y: 6, z: 6, hp: 6, squad: 'assault', patrol: 6, damage: 11 },
        { x: -26, y: 7, z: 14, hp: 5, squad: 'assault', patrol: 5, damage: 11 },
        { kind: 'sentinel', x: -22, z: 20, squad: 'siege', alertRadius: 28 },
        { kind: 'sentinel', x: 26, z: 14, squad: 'siege', alertRadius: 28 },
        { kind: 'sentinel', x: 20, z: -22, squad: 'siege', alertRadius: 26 },
        { kind: 'walker', x: 12, z: 22, squad: 'siege' },
        { kind: 'walker', x: -12, z: -24, squad: 'siege' }
      ],
      objectives: [
        {
          kind: 'destroy', title: 'REPEL THE FIRST ASSAULT',
          detail: 'Break the assault group before it reaches the barracks.',
          pos: [0, 0, -8], squad: 'assault', required: 5
        },
        {
          kind: 'defend', title: 'HOLD THE PARADE GROUND',
          detail: 'Survive three waves breaking through the north gate.',
          pos: [0, 0, 10], squad: 'academy',
          waves: [
            [
              { x: -16, y: 6, z: -6, hp: 4, damage: 9, alertRadius: 44 },
              { x: 16, y: 6, z: -6, hp: 4, damage: 9, alertRadius: 44 }
            ],
            [
              { x: -24, y: 6, z: 8, hp: 4, damage: 10, alertRadius: 46 },
              { x: 0, y: 7, z: 14, hp: 5, damage: 10, alertRadius: 46 },
              { x: 24, y: 6, z: 8, hp: 4, damage: 10, alertRadius: 46 }
            ],
            [
              { x: -14, y: 6, z: 24, hp: 5, damage: 11, alertRadius: 48 },
              { x: 14, y: 6, z: 24, hp: 5, damage: 11, alertRadius: 48 },
              { x: 0, y: 8, z: 30, hp: 6, damage: 12, alertRadius: 48 },
              { kind: 'walker', x: 0, z: 18, alertRadius: 48 }
            ]
          ]
        },
        {
          kind: 'destroy', title: 'BREAK THE SIEGE',
          detail: 'Destroy the emplacements and the two bipeds shelling the parade ground.',
          pos: [0, 0, 16], squad: 'siege', required: 5
        },
        {
          kind: 'destroy', title: 'DESTROY THE WARDEN FRAME',
          detail: 'The Warden command frame has landed on the parade ground. Break it before the barracks come down.',
          pos: [0, 0, 22], squad: 'warden', required: 1,
          spawn: [
            { kind: 'boss', x: 0, z: 22, label: 'THE WARDEN // COMMAND FRAME' }
          ]
        },
        {
          kind: 'choice', title: 'THE LAST ORDER',
          detail: 'Decide what Emberwatch becomes after tonight.',
          pos: [-18, 0, 14], radius: 7, label: 'DECIDE [FINAL ORDER]'
        },
        {
          kind: 'reach', title: 'STAND DOWN',
          detail: 'Signal the all-clear from the north gate.',
          pos: [0, 0, -38], radius: 7, label: 'TRANSMIT [ALL CLEAR]'
        }
      ],
      choice: {
        title: 'WHAT EMBERWATCH STANDS FOR',
        description: 'The academy survived. The record of what it did is the only thing left to settle.',
        options: [
          {
            id: 'BROADCAST_TRUTH', label: 'BROADCAST IT LIVE', tone: 'civilians',
            summary: 'Open the academy archive to the public: every order, every failure, every cadet who held the line.',
            scoreBonus: 1300,
            dialogue: {
              speaker: 'CAPTAIN MARA VOSS',
              text: 'Every relay on the coast is carrying it. No more ghosts, no more sealed files. Whatever Emberwatch is now, it belongs to the people who are watching.'
            }
          },
          {
            id: 'SEAL_ARCHIVE', label: 'SEAL THE ARCHIVE', tone: 'intel',
            summary: 'Lock the archive for fifty years and let the academy rebuild its name without the weight of the record.',
            scoreBonus: 1150,
            dialogue: {
              speaker: 'ROWAN KADE (RIVAL)',
              text: 'Archive sealed, cadets safe, and the name stays clean. Somewhere down the line someone will open that door and hate us for it. That is a problem for a quieter century.'
            }
          }
        ]
      },
      extraction: { pos: [0, 0.5, -38], smoke: true }
    }
  };

  // -------------------------------------------------------- ENVIRONMENT FABRIC
  function buildEnvironment(themeName, scene) {
    var theme = THEMES[themeName] || THEMES.coastal;
    var colliders = [];
    var rotators = [];
    var beacons = [];
    var extras = { targets: [] };

    scene.background = new THREE.Color(theme.sky);
    scene.fog = new THREE.FogExp2(theme.fog, theme.fogDensity);

    // Gradient sky dome. Built on a 2x256 canvas from the theme colour so it
    // ships offline and costs one draw call: zenith darker, warm band at the
    // horizon where the sun sits.
    var skyDome = null;
    if (typeof document !== 'undefined' && document.createElement) {
      var r = (theme.sky >> 16) & 255;
      var g = (theme.sky >> 8) & 255;
      var b = theme.sky & 255;
      var shade = function (m) {
        return 'rgb(' + Math.round(r * m) + ',' + Math.round(g * m) + ',' + Math.round(b * m) + ')';
      };
      var canvas = document.createElement('canvas');
      canvas.width = 2;
      canvas.height = 256;
      var ctx = canvas.getContext('2d');
      var grad = ctx.createLinearGradient(0, 0, 0, 256);
      grad.addColorStop(0, shade(0.42));
      grad.addColorStop(0.58, shade(1));
      grad.addColorStop(0.88, shade(1.35));
      grad.addColorStop(1, shade(0.85));
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, 2, 256);
      var skyTexture = new THREE.CanvasTexture(canvas);
      skyTexture.minFilter = THREE.LinearFilter;
      skyDome = new THREE.Mesh(
        new THREE.SphereGeometry(420, 24, 16),
        new THREE.MeshBasicMaterial({
          map: skyTexture, side: THREE.BackSide, fog: false, depthWrite: false
        })
      );
      scene.add(skyDome);
    }

    var hemi = new THREE.HemisphereLight(theme.hemiSky, theme.hemiGround, theme.hemiIntensity);
    scene.add(hemi);

    var sun = new THREE.DirectionalLight(theme.sun, theme.sunIntensity);
    sun.position.set(40, 60, 30);
    sun.castShadow = true;
    sun.shadow.mapSize.width = 2048;
    sun.shadow.mapSize.height = 2048;
    sun.shadow.camera.near = 0.5;
    sun.shadow.camera.far = 220;
    // Fit the shadow frustum to this theme so a 200 m map still gets a crisp
    // shadow under the operative instead of a 1024 map spread over the world.
    var reach = Math.max(
      55,
      Math.abs(theme.bounds.minX), Math.abs(theme.bounds.maxX),
      Math.abs(theme.bounds.minZ), Math.abs(theme.bounds.maxZ)
    );
    sun.shadow.camera.left = -reach;
    sun.shadow.camera.right = reach;
    sun.shadow.camera.top = reach;
    sun.shadow.camera.bottom = -reach;
    sun.shadow.bias = -0.0006;
    scene.add(sun);
    scene.add(sun.target);

    var M = {
      ground: new THREE.MeshLambertMaterial({ color: theme.ground }),
      concrete: new THREE.MeshLambertMaterial({ color: theme.concrete }),
      dark: new THREE.MeshStandardMaterial({ color: theme.dark, roughness: 0.6, metalness: 0.28 }),
      olive: new THREE.MeshStandardMaterial({ color: theme.olive, roughness: 0.75 }),
      metal: new THREE.MeshStandardMaterial({ color: 0x81919E, metalness: 0.8, roughness: 0.32 }),
      trench: new THREE.MeshLambertMaterial({ color: 0x14181C }),
      water: new THREE.MeshStandardMaterial({ color: theme.water, roughness: 0.2, metalness: 0.7 }),
      rust: new THREE.MeshStandardMaterial({ color: 0x7A4A32, roughness: 0.9 }),
      amber: new THREE.MeshBasicMaterial({ color: 0xE7A83E }),
      red: new THREE.MeshBasicMaterial({ color: 0xC84C4C }),
      cyan: new THREE.MeshBasicMaterial({ color: 0x38D39F }),
      glass: new THREE.MeshBasicMaterial({ color: 0x6FA8B8, transparent: true, opacity: 0.45 })
    };

    // y is the base of the box; collide adds a padded AABB to the movement field.
    function addBox(w, h, d, x, y, z, mat, opts) {
      opts = opts || {};
      var mesh = new THREE.Mesh(new THREE.BoxGeometry(w, h, d), mat);
      mesh.position.set(x, y + h / 2, z);
      mesh.castShadow = opts.cast !== false;
      mesh.receiveShadow = true;
      scene.add(mesh);
      if (opts.collide) {
        var pad = opts.pad === undefined ? 0.5 : opts.pad;
        colliders.push({
          minX: x - w / 2 - pad, maxX: x + w / 2 + pad,
          minZ: z - d / 2 - pad, maxZ: z + d / 2 + pad
        });
      }
      return mesh;
    }

    function addCyl(rTop, rBottom, h, x, y, z, mat, seg) {
      var mesh = new THREE.Mesh(new THREE.CylinderGeometry(rTop, rBottom, h, seg || 10), mat);
      mesh.position.set(x, y + h / 2, z);
      mesh.castShadow = true;
      mesh.receiveShadow = true;
      scene.add(mesh);
      return mesh;
    }

    function addGround(color, size) {
      var mat = new THREE.MeshLambertMaterial({ color: color === undefined ? theme.ground : color });
      var ground = new THREE.Mesh(new THREE.PlaneGeometry(size || 260, size || 260), mat);
      ground.rotation.x = -Math.PI / 2;
      ground.receiveShadow = true;
      scene.add(ground);
      return ground;
    }

    function addWater(size) {
      var ocean = new THREE.Mesh(new THREE.PlaneGeometry(size || 300, size || 300), M.water);
      ocean.rotation.x = -Math.PI / 2;
      ocean.position.y = -0.6;
      scene.add(ocean);
      return ocean;
    }

    function addRing(radius, x, z, mat) {
      var ring = new THREE.Mesh(new THREE.RingGeometry(radius - 0.7, radius, 36), mat);
      ring.rotation.x = -Math.PI / 2;
      ring.position.set(x, 0.09, z);
      scene.add(ring);
      return ring;
    }

    function addLamp(x, y, z, mat, size) {
      var lamp = new THREE.Mesh(new THREE.BoxGeometry(size || 4, 0.22, 0.4), mat);
      lamp.position.set(x, y, z);
      scene.add(lamp);
      return lamp;
    }

    function addPointLight(color, intensity, distance, x, y, z) {
      var light = new THREE.PointLight(color, intensity, distance);
      light.position.set(x, y, z);
      scene.add(light);
      return light;
    }

    function addRangeTargets() {
      for (var i = 0; i < 5; i++) {
        var group = new THREE.Group();
        var board = new THREE.Mesh(new THREE.BoxGeometry(1.6, 2.8, 0.2), M.red);
        board.position.y = 1.4;
        group.add(board);
        group.position.set(-18 + i * 8, 0, -45);
        scene.add(group);
        extras.targets.push(group);
      }
    }

    switch (themeName) {
      case 'vault': {
        addGround();
        // Outer retaining walls
        addBox(96, 11, 3, 0, 0, -46, M.dark, { collide: true, pad: 0.4 });
        addBox(96, 11, 3, 0, 0, 46, M.dark, { collide: true, pad: 0.4 });
        addBox(3, 11, 96, -46, 0, 0, M.dark, { collide: true, pad: 0.4 });
        addBox(3, 11, 96, 46, 0, 0, M.dark, { collide: true, pad: 0.4 });

        // Concrete support pillars and wall stubs (wide gaps keep the vault traversable)
        var pillars = [[-16, -8], [16, -8], [-16, 14], [16, 14], [0, 30], [0, -34]];
        for (var p = 0; p < pillars.length; p++) {
          addBox(3, 9, 3, pillars[p][0], 0, pillars[p][1], M.concrete, { collide: true, pad: 0.4 });
        }
        addBox(14, 8, 2, -34, 0, 4, M.dark, { collide: true, pad: 0.4 });
        addBox(12, 8, 2, 34, 0, 4, M.dark, { collide: true, pad: 0.4 });
        addBox(12, 8, 2, 8, 0, -20, M.dark, { collide: true, pad: 0.4 });
        addBox(12, 8, 2, -8, 0, 22, M.dark, { collide: true, pad: 0.4 });

        // Cable trenches (walkable)
        addBox(4, 0.14, 74, -6, 0.02, -2, M.trench, { collide: false, cast: false });
        addBox(4, 0.14, 60, 22, 0.02, 8, M.trench, { collide: false, cast: false });

        // Server racks with cyan status strips
        for (var r = 0; r < 4; r++) {
          addBox(3, 4, 2, -38 + r * 4, 0, 6, M.dark, { collide: true, pad: 0.4 });
          addLamp(-38 + r * 4, 3.2, 7.2, M.cyan, 2.2);
        }

        // Ceiling lamp fixtures
        for (var lx = -36; lx <= 36; lx += 12) {
          addLamp(lx, 8.6, -18, M.cyan, 5);
          addLamp(lx, 8.6, 18, M.cyan, 5);
        }

        // Splice node terminals
        addBox(2.4, 2.2, 2.4, -22, 0, 18, M.metal, { collide: true, pad: 0.4 });
        addLamp(-22, 2.6, 18, M.amber, 2);
        addBox(2.4, 2.2, 2.4, 16, 0, -6, M.metal, { collide: true, pad: 0.4 });
        addLamp(16, 2.6, -6, M.amber, 2);
        addBox(2.4, 2.2, 2.4, 24, 0, 22, M.metal, { collide: true, pad: 0.4 });
        addLamp(24, 2.6, 22, M.amber, 2);

        // Main uplink pylon
        addBox(7, 3, 7, 30, 0, -30, M.concrete, { collide: true, pad: 0.4 });
        addCyl(1.1, 1.6, 10, 30, 3, -30, M.metal, 8);
        var dish = new THREE.Mesh(new THREE.CylinderGeometry(2.6, 0.4, 1, 14, 1, true), M.metal);
        dish.position.set(30, 13, -30);
        dish.rotation.x = Math.PI / 4;
        scene.add(dish);
        rotators.push({ mesh: dish, axis: 'y', speed: 0.5 });

        // Service elevator
        addRing(4.2, -34, 32, M.cyan);
        addBox(9, 0.25, 9, -34, 0.02, 32, M.concrete, { collide: false, cast: false });

        beacons.push(addPointLight(0x38D39F, 0.9, 26, 30, 8, -30));
        beacons.push(addPointLight(0xE7A83E, 0.6, 22, -22, 5, 18));
        break;
      }

      case 'marsh': {
        addGround();
        // Shallow water pools
        var pools = [[-26, 6, 26, 14], [8, -22, 22, 12], [34, 20, 18, 16], [-6, 30, 20, 10]];
        for (var pw = 0; pw < pools.length; pw++) {
          var pool = new THREE.Mesh(new THREE.PlaneGeometry(pools[pw][2], pools[pw][3]), M.water);
          pool.rotation.x = -Math.PI / 2;
          pool.position.set(pools[pw][0], 0.03, pools[pw][1]);
          scene.add(pool);
        }

        // Causeway
        addBox(12, 0.16, 96, 0, 0.02, 0, M.concrete, { collide: false, cast: false });

        // Reeds and driftwood
        for (var i = 0; i < 34; i++) {
          var rx = (Math.sin(i * 12.9898) * 43758.5453) % 1;
          var rz = (Math.sin(i * 78.233) * 12543.1234) % 1;
          var px = rx * 100;
          var pz = rz * 80 - 12;
          if (Math.abs(px) < 9) continue;
          addBox(0.24, 2.2 + (i % 3) * 0.4, 0.24, px, 0, pz, M.olive, { collide: false, cast: false });
        }

        // Sensor beacon masts
        var masts = [[28, -4], [-2, 24], [-30, -20]];
        for (var b = 0; b < masts.length; b++) {
          addBox(1.2, 1, 1.2, masts[b][0], 0, masts[b][1], M.concrete, { collide: true, pad: 0.4 });
          addCyl(0.22, 0.3, 8, masts[b][0], 1, masts[b][1], M.metal, 6);
          addBox(1.6, 0.9, 1.6, masts[b][0], 8.6, masts[b][1], M.red, { collide: false });
          beacons.push(addPointLight(0xC84C4C, 0.8, 20, masts[b][0], 9.4, masts[b][1]));
        }

        // Sunken barge cover
        addBox(16, 3, 7, -40, 0, 8, M.rust, { collide: true, pad: 0.6 });
        addBox(10, 2.4, 6, 40, 0, -8, M.rust, { collide: true, pad: 0.6 });

        // Rally point canopy
        addBox(7, 0.4, 7, -34, 3, 34, M.olive, { collide: false });
        addBox(0.4, 3, 0.4, -37, 0, 31, M.metal, { collide: true, pad: 0.3 });
        addBox(0.4, 3, 0.4, -31, 0, 31, M.metal, { collide: true, pad: 0.3 });
        addBox(0.4, 3, 0.4, -37, 0, 37, M.metal, { collide: true, pad: 0.3 });
        addBox(0.4, 3, 0.4, -31, 0, 37, M.metal, { collide: true, pad: 0.3 });
        addRing(4.4, -34, 34, M.cyan);
        break;
      }

      case 'highway': {
        addGround();
        // Highway deck
        addBox(26, 0.18, 130, 0, 0.02, 0, M.dark, { collide: false, cast: false });
        for (var i = -56; i <= 56; i += 10) {
          addBox(0.5, 0.05, 4, 0, 0.06, i, M.concrete, { collide: false, cast: false });
        }

        // Trestle bridge rails (walk between them)
        addBox(1.4, 3.4, 40, -13, 0, 6, M.concrete, { collide: true, pad: 0.3 });
        addBox(1.4, 3.4, 40, 13, 0, 6, M.concrete, { collide: true, pad: 0.3 });
        for (var g = -14; g <= 26; g += 8) {
          addBox(26, 1.1, 1.1, 0, 3.4, g, M.metal, { collide: false });
        }
        addBox(1.6, 10, 1.6, -13, 0, -14, M.concrete, { collide: true, pad: 0.3 });
        addBox(1.6, 10, 1.6, 13, 0, -14, M.concrete, { collide: true, pad: 0.3 });
        addBox(1.6, 10, 1.6, -13, 0, 26, M.concrete, { collide: true, pad: 0.3 });
        addBox(1.6, 10, 1.6, 13, 0, 26, M.concrete, { collide: true, pad: 0.3 });

        // Convoy trucks
        var trucks = [[-40, -14, 0], [-40, 6, 0], [-40, 24, 0]];
        for (var t = 0; t < trucks.length; t++) {
          addBox(6, 3.2, 11, trucks[t][0], 0, trucks[t][1], M.olive, { collide: true, pad: 0.6 });
          addBox(5, 1.4, 3, trucks[t][0], 3.2, trucks[t][1] - 3, M.dark, { collide: false });
          addBox(1.6, 0.6, 1.6, trucks[t][0] - 2, 0.2, trucks[t][1] + 4, M.cyan, { collide: false });
        }

        // Wrecks and debris
        addBox(7, 2.4, 4, 18, 0, -22, M.rust, { collide: true, pad: 0.6 });
        addBox(5, 2.0, 3, -8, 0, -30, M.rust, { collide: true, pad: 0.6 });
        addBox(3, 1.2, 3, 30, 0, 30, M.concrete, { collide: true, pad: 0.4 });
        // Fort Vane gate (open through the middle so the convoy can pass)
        addBox(10, 4, 1.2, 50, 0, -34, M.concrete, { collide: true, pad: 0.4 });
        addBox(10, 4, 1.2, 50, 0, -14, M.concrete, { collide: true, pad: 0.4 });
        addBox(16, 1.4, 1.6, 50, 4, -24, M.rust, { collide: false });
        beacons.push(addPointLight(0xE7A83E, 0.8, 24, 0, 6, 6));
        break;
      }

      case 'mine': {
        addGround();
        // Rail lines
        addBox(0.4, 0.12, 84, -2.4, 0.02, 0, M.metal, { collide: false, cast: false });
        addBox(0.4, 0.12, 84, 2.4, 0.02, 0, M.metal, { collide: false, cast: false });
        for (var s = -40; s <= 40; s += 5) {
          addBox(6, 0.08, 0.5, 0, 0.03, s, M.rust, { collide: false, cast: false });
        }

        // Shaft portal frames
        var frames = [[-20, -6], [4, -6], [28, -6], [-32, 20], [0, 20], [28, 20]];
        for (var f = 0; f < frames.length; f++) {
          addBox(1.4, 7, 1.4, frames[f][0] - 4, 0, frames[f][1], M.rust, { collide: true, pad: 0.4 });
          addBox(1.4, 7, 1.4, frames[f][0] + 4, 0, frames[f][1], M.rust, { collide: true, pad: 0.4 });
          addBox(9.6, 1, 1.6, frames[f][0], 7, frames[f][1], M.rust, { collide: false });
        }

        // Ore carts
        var carts = [[-8, 12], [10, -14], [-24, -24], [20, 6]];
        for (var c = 0; c < carts.length; c++) {
          addBox(3.4, 2.2, 4.4, carts[c][0], 0, carts[c][1], M.rust, { collide: true, pad: 0.5 });
          addBox(3.8, 0.5, 0.8, carts[c][0], 2.2, carts[c][1], M.dark, { collide: false });
        }

        // Rock spoil heaps
        addBox(12, 4, 10, -42, 0, -8, M.dark, { collide: true, pad: 0.6 });
        addBox(10, 3.4, 12, 40, 0, 34, M.dark, { collide: true, pad: 0.6 });

        // Shaft 3 terminal bunker
        addBox(8, 4, 8, 28, 0, -28, M.concrete, { collide: true, pad: 0.5 });
        addBox(6, 3, 0.4, 28, 1, -23.8, M.glass, { collide: false, cast: false });
        addLamp(28, 4.4, -24, M.amber, 4);

        // Floodlights
        var lamps = [[-34, -34], [34, 8], [-16, 34]];
        for (var lg = 0; lg < lamps.length; lg++) {
          addCyl(0.2, 0.3, 9, lamps[lg][0], 0, lamps[lg][1], M.metal, 6);
          beacons.push(addPointLight(0xFFC98A, 1.1, 30, lamps[lg][0], 9.4, lamps[lg][1]));
        }
        break;
      }

      case 'fort': {
        addGround();
        // Courtyard perimeter
        addBox(88, 8, 2.4, 0, 0, -46, M.concrete, { collide: true, pad: 0.4 });
        addBox(88, 8, 2.4, 0, 0, 44, M.concrete, { collide: true, pad: 0.4 });
        addBox(2.4, 8, 88, -44, 0, 0, M.concrete, { collide: true, pad: 0.4 });
        addBox(2.4, 8, 88, 44, 0, 0, M.concrete, { collide: true, pad: 0.4 });

        // Central transmitter
        addBox(12, 2.6, 12, 0, 0, 10, M.concrete, { collide: true, pad: 0.5 });
        addCyl(1.4, 2.2, 16, 0, 2.6, 10, M.metal, 10);
        var fortDish = new THREE.Mesh(new THREE.CylinderGeometry(4.4, 0.6, 1.2, 18, 1, true), M.metal);
        fortDish.position.set(0, 19, 10);
        fortDish.rotation.x = Math.PI / 4;
        scene.add(fortDish);
        rotators.push({ mesh: fortDish, axis: 'y', speed: 0.55 });
        beacons.push(addPointLight(0xE7A83E, 1.6, 40, 0, 20, 10));

        // Defense walls / sandbag ring
        addBox(20, 2, 3, -22, 0, 0, M.concrete, { collide: true, pad: 0.5 });
        addBox(20, 2, 3, 22, 0, 0, M.concrete, { collide: true, pad: 0.5 });
        addBox(3, 2, 18, -32, 0, 16, M.concrete, { collide: true, pad: 0.5 });
        addBox(3, 2, 18, 32, 0, 16, M.concrete, { collide: true, pad: 0.5 });

        // Coolant manifold (west wall)
        addBox(6, 3.2, 5, -28, 0, 14, M.dark, { collide: true, pad: 0.5 });
        addCyl(0.4, 0.4, 9, -24, 2.6, 14, M.metal, 8);
        addLamp(-28, 3.6, 14, M.cyan, 4);

        // Searchlight towers
        var towers = [[-36, -34], [36, -34], [-36, 34], [36, 34]];
        for (var tw = 0; tw < towers.length; tw++) {
          addCyl(0.5, 0.8, 12, towers[tw][0], 0, towers[tw][1], M.metal, 6);
          addBox(2.4, 1.6, 2.4, towers[tw][0], 12, towers[tw][1], M.dark, { collide: false });
          beacons.push(addPointLight(0xC84C4C, 1.0, 30, towers[tw][0], 13.6, towers[tw][1]));
        }

        // Supply stacks
        addBox(7, 3, 7, -34, 0, 6, M.olive, { collide: true, pad: 0.5 });
        addBox(7, 3, 7, 34, 0, 2, M.olive, { collide: true, pad: 0.5 });
        addRing(4.4, 0, -38, M.cyan);
        break;
      }

      case 'platform': {
        addWater(320);
        var deck = new THREE.Mesh(new THREE.BoxGeometry(92, 1.2, 92), M.concrete);
        deck.position.y = -0.6;
        deck.receiveShadow = true;
        scene.add(deck);

        // Railings (posts only, so the deck stays walkable)
        for (var e = -42; e <= 42; e += 6) {
          addBox(0.4, 1.5, 0.4, e, 0, -42, M.metal, { collide: true, pad: 0.2 });
          addBox(0.4, 1.5, 0.4, e, 0, 42, M.metal, { collide: true, pad: 0.2 });
          addBox(0.4, 1.5, 0.4, -42, 0, e, M.metal, { collide: true, pad: 0.2 });
          addBox(0.4, 1.5, 0.4, 42, 0, e, M.metal, { collide: true, pad: 0.2 });
        }

        // Transmission array
        addBox(14, 3, 14, 24, 0, -24, M.dark, { collide: true, pad: 0.5 });
        addCyl(1.6, 3.2, 18, 24, 3, -24, M.metal, 12);
        var arrayDish = new THREE.Mesh(new THREE.CylinderGeometry(6.5, 1, 1.6, 20, 1, true), M.metal);
        arrayDish.position.set(24, 22, -24);
        arrayDish.rotation.x = Math.PI / 4;
        scene.add(arrayDish);
        rotators.push({ mesh: arrayDish, axis: 'y', speed: 0.35 });
        beacons.push(addPointLight(0x38D39F, 1.6, 44, 24, 24, -24));

        // Container yard
        var containers = [[-16, 16, 0], [-16, 6, 0], [10, 20, 1], [-6, 26, 0], [14, -8, 1]];
        for (var ct = 0; ct < containers.length; ct++) {
          addBox(9, 3.2, 4.4, containers[ct][0], 0, containers[ct][1], containers[ct][2] ? M.rust : M.olive, { collide: true, pad: 0.6 });
        }

        // Crane frame
        addBox(1.6, 16, 1.6, -34, 0, -10, M.metal, { collide: true, pad: 0.4 });
        addBox(1.6, 16, 1.6, -18, 0, -10, M.metal, { collide: true, pad: 0.4 });
        addBox(18, 1.4, 1.6, -26, 16, -10, M.metal, { collide: false });

        // Helipad
        addRing(6.5, -28, 30, M.cyan);
        addBox(16, 0.2, 16, -28, 0.02, 30, M.dark, { collide: false, cast: false });
        for (var hp = -34; hp <= -22; hp += 4) {
          addLamp(hp, 1.2, 22.4, M.cyan, 1.6);
        }
        break;
      }

      default: {
        // Coastal relay outpost (Chapter 1)
        addGround();
        addWater(320);

        // Patrol road
        addBox(14, 0.16, 190, 0, 0.02, 0, M.concrete, { collide: false, cast: false });

        // Command outpost, generator vault, civilian shelter
        addBox(20, 8, 16, -22, 0, -30, M.olive, { collide: true, pad: 0.8 });
        addBox(14, 6, 12, 24, 0, 12, M.dark, { collide: true, pad: 0.8 });
        addBox(16, 7, 20, 32, 0, 45, M.olive, { collide: true, pad: 0.8 });

        // Roof dishes
        addCyl(1.6, 2.4, 1, -22, 8, -30, M.metal, 12);
        addCyl(1.2, 2.0, 1, 24, 6, 12, M.metal, 12);

        // Perimeter barricades
        for (var i = -30; i <= 30; i += 12) {
          if (Math.abs(i) > 6) {
            addBox(8, 2.5, 3, i, 0, -12, M.concrete, { collide: true, pad: 0.5 });
          }
        }

        // Relay Tower Alpha
        addCyl(0.5, 1.2, 16, 0, 0, 25, M.metal, 4);
        var dish = new THREE.Mesh(new THREE.CylinderGeometry(3.5, 0.4, 1.2, 16, 1, true), M.metal);
        dish.position.set(0, 17, 25);
        dish.rotation.x = Math.PI / 4;
        dish.castShadow = true;
        scene.add(dish);
        rotators.push({ mesh: dish, axis: 'y', speed: 0.6 });
        colliders.push({ minX: -4, maxX: 4, minZ: 21, maxZ: 29 });
        beacons.push(addPointLight(0xC84C4C, 2.0, 35, 0, 17, 25));

        // Supply crates
        for (var c = 0; c < 12; c++) {
          var cx = (c % 2 === 0 ? -12 : 14) + Math.sin(c) * 6;
          var cz = -25 + c * 5;
          addBox(3, 3, 3, cx, 0, cz, M.olive, { collide: true, pad: 0.4 });
        }

        // Ballistic range targets
        addRangeTargets();

        // LZ Falcon pad
        addRing(6.2, -25, 45, M.cyan);
        addBox(13, 0.2, 13, -25, 0.02, 45, M.concrete, { collide: false, cast: false });
        break;
      }
    }

    return {
      colliders: colliders,
      rotators: rotators,
      beacons: beacons,
      materials: M,
      extras: extras,
      theme: theme,
      sun: sun,
      skyDome: skyDome,
      bounds: theme.bounds
    };
  }

  window.EMBERWATCH_LEVELS = {
    THEMES: THEMES,
    MISSIONS: MISSIONS,
    buildEnvironment: buildEnvironment
  };
})();
