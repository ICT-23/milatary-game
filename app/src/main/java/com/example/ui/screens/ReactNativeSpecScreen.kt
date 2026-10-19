package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoolSteel
import com.example.ui.theme.DeepGraphite
import com.example.ui.theme.DeepGraphiteBorder
import com.example.ui.theme.DeepGraphiteElevated
import com.example.ui.theme.RadarCyan
import com.example.ui.theme.SignalAmber
import com.example.ui.theme.WeatheredIvory

@Composable
fun ReactNativeSpecScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepGraphite)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .testTag("react_native_spec_screen")
    ) {
        // TOP BAR
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepGraphiteElevated)
                    .border(1.dp, DeepGraphiteBorder, RoundedCornerShape(8.dp))
                    .testTag("spec_back_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = WeatheredIvory
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "REACT NATIVE ARCHITECTURE & EXPORT SPEC",
                    style = MaterialTheme.typography.titleMedium,
                    color = WeatheredIvory,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "CROSS-FRAMEWORK IMPLEMENTATION BLUEPRINT",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = SignalAmber
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SPECIFICATION CONTENT
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Environment Reality Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = RadarCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ENVIRONMENT DISCOVERY & RUNTIME VERIFICATION",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = RadarCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This AI Studio workspace is a native Android (Kotlin / Jetpack Compose / Gradle) build container. The live simulation you play in this app runs natively on Android's hardware-accelerated Compose Canvas. To fulfill your request for React Native compatibility without misrepresenting the container's capabilities, this specification maps every subsystem 1-to-1 to React Native + Skia.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WeatheredIvory,
                        lineHeight = 18.sp
                    )
                }
            }

            // 1-to-1 Architecture Mapping
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1:1 ARCHITECTURE MAPPING",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SignalAmber
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• Rendering Engine:\n  Android: Jetpack Compose Canvas (DrawScope)\n  React Native: @shopify/react-native-skia Canvas\n\n• Virtual Analog Joystick:\n  Android: pointerInput + detectDragGestures\n  React Native: react-native-gesture-handler (PanGestureHandler)\n\n• Local Persistence:\n  Android: Room Database (SQLite)\n  React Native: @op-engineering/op-sqlite or react-native-mmkv\n\n• Procedural Sound:\n  Android: AudioTrack PCM sine synthesis\n  React Native: expo-av or react-native-sound\n\n• State Management:\n  Android: ViewModel + MutableStateFlow\n  React Native: Zustand / React Context with useFrameLoop",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = CoolSteel,
                        lineHeight = 18.sp
                    )
                }
            }

            // TypeScript Interface Definition
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = SignalAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CORE TYPESCRIPT INTERFACES (src/game/types.ts)",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = SignalAmber
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = """
export interface Vector2 {
  x: number;
  y: number;
}

export interface TacticalOperative {
  callsign: string;
  rankTitle: string;
  position: Vector2;
  facingAngleRad: number;
  health: number;
  morale: number;
}

export interface TacticalObjective {
  id: string;
  title: string;
  detail: string;
  targetPosition: Vector2 | null;
  isCompleted: boolean;
}

export interface StoryChoiceOutcome {
  choiceId: 'SAFEGUARD_CIVILIANS' | 'INTERCEPT_DATA';
  vossTrustDelta: number;
  idrisRespectDelta: number;
  kadeRivalryDelta: number;
}
                        """.trimIndent(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = WeatheredIvory,
                        lineHeight = 15.sp
                    )
                }
            }

            // Export Steps
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepGraphiteElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REACT NATIVE STANDALONE SETUP INSTRUCTIONS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SignalAmber
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. npx create-expo-app Emberwatch --template blank-typescript\n2. npx expo install @shopify/react-native-skia react-native-gesture-handler zustand\n3. Copy the vector assets and game engine loop from GameEngine.kt to engine.ts\n4. Run 'npx expo run:android' to build and run on device.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = CoolSteel,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
