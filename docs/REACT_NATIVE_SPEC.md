# OPERATION: EMBERWATCH — REACT NATIVE SPECIFICATION & PORTING GUIDE

## 1. Technical Architecture & Stack
This document provides the implementation specification for deploying *Operation: Emberwatch* in an Expo / React Native environment.

### Core Dependencies
- `@shopify/react-native-skia`: 2.5D top-down hardware-accelerated rendering.
- `react-native-gesture-handler`: High-frequency touch event processing for the Virtual Analog Joystick.
- `react-native-reanimated`: 60 FPS animation ticker and smooth camera translation.
- `zustand`: Lightweight game state management.
- `@op-engineering/op-sqlite` or `react-native-mmkv`: Fast local data persistence for campaign profile and story flags.
- `expo-av`: Tactical audio feedback.

## 2. Directory Structure
```
emberwatch-rn/
├── App.tsx
├── src/
│   ├── components/
│   │   ├── VirtualJoystick.tsx
│   │   ├── TacticalHud.tsx
│   │   ├── DialogueBox.tsx
│   │   └── DecisionModal.tsx
│   ├── game/
│   │   ├── engine.ts
│   │   ├── entities.ts
│   │   ├── math.ts
│   │   └── missions.ts
│   ├── screens/
│   │   ├── TitleScreen.tsx
│   │   ├── BriefingScreen.tsx
│   │   ├── MissionScreen.tsx
│   │   ├── DebriefScreen.tsx
│   │   └── RosterScreen.tsx
│   ├── store/
│   │   └── gameStore.ts
│   └── theme/
│       └── colors.ts
```

## 3. Skia 2.5D Canvas Porting Pattern
The Jetpack Compose Canvas implementation in `MissionCanvasView.kt` maps directly to Skia components:

```tsx
import { Canvas, Rect, Circle, Line, Path, Skia } from "@shopify/react-native-skia";

export const MissionCanvas = ({ engine, camX, camY }: Props) => {
  return (
    <Canvas style={{ flex: 1 }}>
      {/* 1. Terrain Grid */}
      <Rect x={-camX} y={-camY} width={1200} height={900} color="#D6C29A" />
      
      {/* 2. Obstacles & Structures */}
      {engine.obstacles.map((obs, idx) => (
        <Rect
          key={idx}
          x={obs.left - camX}
          y={obs.top - camY}
          width={obs.right - obs.left}
          height={obs.bottom - obs.top}
          color="#354A40"
        />
      ))}

      {/* 3. Player Character */}
      <Circle
        cx={engine.playerPos.x - camX}
        cy={engine.playerPos.y - camY}
        r={18}
        color="#E7A83E"
      />
    </Canvas>
  );
};
```

## 4. Setup Commands
```bash
# Initialize project
npx create-expo-app Emberwatch --template blank-typescript
cd Emberwatch

# Install game libraries
npx expo install @shopify/react-native-skia react-native-gesture-handler react-native-reanimated zustand

# Run on Android physical device or emulator
npx expo run:android
```
