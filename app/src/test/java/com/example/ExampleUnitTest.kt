package com.example

import com.example.game.GameEngine
import com.example.game.Vector2
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testVectorMath() {
        val v1 = Vector2(3f, 4f)
        assertEquals(5f, v1.length(), 0.001f)

        val normalized = v1.normalized()
        assertEquals(0.6f, normalized.x, 0.001f)
        assertEquals(0.8f, normalized.y, 0.001f)

        val v2 = Vector2(0f, 0f)
        assertEquals(5f, v1.distanceTo(v2), 0.001f)
    }

    @Test
    fun testGameEngineInitialization() {
        val engine = GameEngine()
        assertEquals(100, engine.playerHealth)
        assertEquals(100, engine.playerMorale)
        assertEquals(0, engine.currentObjectiveIndex)
        assertNotNull(engine.objectives)
        assertTrue(engine.objectives.isNotEmpty())
    }

    @Test
    fun testObjectiveAdvancement() {
        val engine = GameEngine()
        val initialIdx = engine.currentObjectiveIndex
        engine.advanceObjective()
        assertEquals(initialIdx + 1, engine.currentObjectiveIndex)

        // Relay target should now be enabled
        val relayTarget = engine.interactiveTargets.find { it.id == "TARGET_RELAY" }
        assertNotNull(relayTarget)
        assertTrue(relayTarget!!.isEnabled)
    }

    @Test
    fun testScannerTriggerAndCooldown() {
        val engine = GameEngine()
        assertTrue(engine.triggerScanner())
        assertTrue(engine.activeScannerCooldown > 0f)

        // Second trigger should be rejected while on cooldown
        assertFalse(engine.triggerScanner())
    }
}
