package com.example.ui.screens

import android.annotation.SuppressLint
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import com.example.game.settings.QualityPreset

/**
 * Hosts the Three.js chapter engine inside a WebView.
 *
 * Every chapter is driven by the same `index.html`, the mission is chosen with the
 * `?mission=` query parameter (mirrored into [getPlatformConfig] so the engine can
 * read tactical settings before first paint), and results come back through
 * [onMissionFinished] once the extraction is confirmed.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Game3DView(
    missionId: String,
    sensitivity: Float,
    sfxVolume: Float,
    hapticsEnabled: Boolean,
    highContrast: Boolean,
    qualityPreset: QualityPreset,
    masterVolume: Float,
    dialogueVolume: Float,
    subtitlesEnabled: Boolean,
    pauseSignal: Int,
    onMissionFinished: (score: Int, choice: String, timeSec: Int, healthPct: Int) -> Unit,
    onAbortMission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // The bridge object outlives recomposition, so route calls through holders.
    val finishCallback = rememberUpdatedState(onMissionFinished)
    val abortCallback = rememberUpdatedState(onAbortMission)
    val configHolder = rememberUpdatedState(
        Game3DConfig(
            missionId = missionId,
            sensitivity = sensitivity,
            sfxVolume = sfxVolume,
            haptics = hapticsEnabled,
            highContrast = highContrast,
            quality = qualityPreset,
            masterVolume = masterVolume,
            dialogueVolume = dialogueVolume,
            subtitles = subtitlesEnabled
        )
    )

    val bridge = remember {
        object {
            @JavascriptInterface
            fun triggerHaptic(ms: Long) {
                if (!configHolder.value.hapticsEnabled) return
                val duration = ms.coerceAtLeast(10)
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val manager = context.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                        manager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator?.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator?.vibrate(duration)
                        }
                    }
                } catch (_: Exception) {
                }
            }

            @JavascriptInterface
            fun getPlatformConfig(): String = configHolder.value.toJson()

            @JavascriptInterface
            fun onMissionComplete(score: Int, choice: String, timeSec: Int, healthPct: Int) {
                finishCallback.value(score, choice, timeSec, healthPct)
            }

            @JavascriptInterface
            fun onAbortMission() {
                abortCallback.value()
            }
        }
    }

    var webView by remember { mutableStateOf<WebView?>(null) }

    // Remember where the counter started so a brand-new run is never paused by a
    // signal that was raised in an earlier mission.
    val pauseBaseline = remember { pauseSignal }

    // Back press / native pause requests are forwarded into the engine.
    LaunchedEffect(pauseSignal, webView) {
        if (pauseSignal > pauseBaseline) {
            webView?.evaluateJavascript("window.emberwatchPause && window.emberwatchPause();", null)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF111417)).testTag("game_3d_view")) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(0xFF111417.toInt())
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            view?.evaluateJavascript("window.emberwatchResume && window.emberwatchResume();", null)
                        }
                    }
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        mediaPlaybackRequiresUserGesture = false
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        cacheMode = WebSettings.LOAD_NO_CACHE
                        setSupportZoom(false)
                        builtInZoomControls = false
                        displayZoomControls = false
                    }
                    addJavascriptInterface(bridge, "AndroidBridge")
                    loadUrl("file:///android_asset/game/index.html?mission=$missionId")
                    webView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.apply {
                removeJavascriptInterface("AndroidBridge")
                loadUrl("about:blank")
                destroy()
            }
            webView = null
        }
    }
}

/** Immutable snapshot of the settings handed to the running engine. */
private data class Game3DConfig(
    val missionId: String,
    val sensitivity: Float,
    val sfxVolume: Float,
    val haptics: Boolean,
    val highContrast: Boolean,
    val quality: QualityPreset,
    val masterVolume: Float,
    val dialogueVolume: Float,
    val subtitles: Boolean
) {
    fun toJson(): String = buildString {
        append('{')
        append("\"missionId\":\"").append(missionId.replace("\"", "")).append("\",")
        append("\"sensitivity\":").append(sensitivity.coerceIn(0.2f, 3f)).append(',')
        // Section 15.2: the engine mixes its effects against the master bus only.
        append("\"sfxVolume\":").append((sfxVolume * masterVolume).coerceIn(0f, 1f)).append(',')
        append("\"haptics\":").append(haptics).append(',')
        append("\"highContrast\":").append(highContrast).append(',')
        // Section 21.3: sent as concrete numbers so the engine never re-tunes a preset.
        append("\"quality\":\"").append(quality.id).append("\",")
        append("\"resolutionScale\":").append(quality.resolutionScale).append(',')
        append("\"shadowDistance\":").append(quality.shadowDistanceMeters).append(',')
        append("\"textureTier\":").append(quality.textureTier).append(',')
        append("\"particleBudget\":").append(quality.particleBudget).append(',')
        append("\"foliageDensity\":").append(quality.foliageDensity).append(',')
        append("\"postProcessingTier\":").append(quality.postProcessingTier).append(',')
        append("\"aiBudget\":").append(quality.aiBudget).append(',')
        append("\"masterVolume\":").append(masterVolume.coerceIn(0f, 1f)).append(',')
        append("\"dialogueVolume\":").append(dialogueVolume.coerceIn(0f, 1f)).append(',')
        append("\"subtitles\":").append(subtitles)
        append('}')
    }
}
