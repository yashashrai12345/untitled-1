package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Arrows", appName)
  }

  @Test
  fun testMeterProgress_SessionIdChangesOnNewLevelOrRestart() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = com.example.data.persistence.GamePreferences.getInstance(context)
    val engine = com.example.game.engine.GameEngine(
      preferences = prefs,
      soundManager = com.example.audio.SoundManager.getInstance(prefs),
      hapticManager = com.example.haptics.HapticManager.getInstance(context, prefs)
    )

    val initialSessionId = engine.state.value.sessionId
    org.junit.Assert.assertTrue(initialSessionId > 0L)

    // Restart level
    engine.restartLevel()
    val restartedSessionId = engine.state.value.sessionId
    org.junit.Assert.assertTrue("SessionId must change on restart to reset meter", restartedSessionId != initialSessionId)
    assertEquals(0.0f, engine.state.value.progressFraction, 0.001f)

    // Load next level
    engine.loadLevel(2)
    val nextLevelSessionId = engine.state.value.sessionId
    org.junit.Assert.assertTrue("SessionId must change on loadLevel to reset meter", nextLevelSessionId != restartedSessionId)
    assertEquals(0.0f, engine.state.value.progressFraction, 0.001f)
  }
}
