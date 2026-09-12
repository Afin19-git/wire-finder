package com.afin19.wirefinder

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.afin19.wirefinder.sensor.DeltaPoint
import com.afin19.wirefinder.sensor.WireFinderState
import com.afin19.wirefinder.ui.SignalChartScreen
import com.afin19.wirefinder.ui.theme.WireFinderTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.sin

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class SignalChartScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun signal_chart_screenshot() {
    val now = 100000L
    val samplePoints = (0..100).map { i ->
      val t = now - (100 - i) * 35L
      val wave = (sin(i * 0.3) * 1.5f).toFloat()
      val env = 1.6f
      DeltaPoint(timestampMs = t, delta = wave, envelope = env)
    }

    val state = WireFinderState(
      isScanning = true,
      delta = 0.85f,
      displaySignal = 1.70f,
      level = 48f,
      samplingRateHz = 65,
      deltaHistory = samplePoints
    )

    composeTestRule.setContent {
      WireFinderTheme {
        SignalChartScreen(
          state = state,
          onBack = {},
          onStartCalibration = {},
          onResetPeak = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/signal_chart.png")
  }
}
