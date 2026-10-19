# OPERATION: EMBERWATCH — GAME DESIGN DOCUMENT

## 1. Executive Summary
- **Title**: OPERATION: EMBERWATCH
- **Genre**: Story-driven tactical military simulation & squad progression
- **Target Platform**: Android smartphones & tablets
- **Engine**: Native Android Kotlin + Jetpack Compose 2.5D hardware-accelerated Canvas with companion React Native Skia architecture.
- **Core Loop**: Strategic Briefing → Tactical Top-Down Deployment → Environmental Recon & Scanning → Consequential Story Dilemma → Tactical Extraction → After-Action Report (AAR) & Operative Promotion.

## 2. Core Pillars
1. **Meaningful Story & Asterra Lore**: A grounded military conflict focusing on duty, ethics, and squad relationships rather than generic invincibility.
2. **Tactical 2.5D Spatial Gameplay**: Direct real-time virtual joystick movement, line-of-sight vision cones, dynamic camera tracking, obstacle collision, and radar scanning.
3. **Branching Moral Dilemmas**: Choices with permanent narrative consequences saved into the persistent SQLite database.
4. **Offline-First & Zero Exploitative Monetization**: Fully functioning single-player campaign without online paywalls or forced microtransactions.

## 3. Mission 1: Signal in the Dust
- **Setting**: Sector 4 Coastal Relay Outpost, Cape Oakhaven, Republic of Asterra.
- **Sequence of Objectives**:
  1. *Link up with Recon Specialist*: Meet Tomas Vale behind the sandbag emplacement.
  2. *Scan Damaged Relay Tower Alpha*: Use the Field Scanner to analyze telemetry array sabotage.
  3. *Investigate Overloading Generator*: Inspect auxiliary power manifold overheating near the civilian fallout shelter.
  4. *Resolve Override Dilemma*:
     - **Option A (Safeguard Civilians)**: Vent power into drainage sluice to protect 40 sheltering families. (+Voss trust, +Idris respect).
     - **Option B (Intercept Cipher Data)**: Overclock scanner to capture the enemy saboteur's stolen frequency ciphers. (+Intel, -Rowan rivalry).
  5. *Extract at LZ Falcon*: Deploy extraction flare at the coastal cliff landing zone.
  6. *After-Action Report*: Unlocks Chapter 2 dossier ("Broken Relay") and awards the Cadet Operative rank promotion.
