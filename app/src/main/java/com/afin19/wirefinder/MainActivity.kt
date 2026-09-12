package com.afin19.wirefinder

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.afin19.wirefinder.i18n.AppLanguage
import com.afin19.wirefinder.i18n.LocalAppLanguage
import com.afin19.wirefinder.i18n.LocalAppStrings
import com.afin19.wirefinder.i18n.LocaleManager
import com.afin19.wirefinder.i18n.getStrings
import com.afin19.wirefinder.sensor.MagneticSensorProcessor
import com.afin19.wirefinder.ui.CalibrationOverlay
import com.afin19.wirefinder.ui.InstructionScreen
import com.afin19.wirefinder.ui.LanguageSelectionDialog
import com.afin19.wirefinder.ui.ScannerScreen
import com.afin19.wirefinder.ui.SignalChartScreen
import com.afin19.wirefinder.ui.theme.DarkBackground
import com.afin19.wirefinder.ui.theme.WireFinderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()

        setContent {
            WireFinderTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    color = DarkBackground
                ) {
                    WireFinderApp()
                }
            }
        }
    }
}

enum class Screen {
    INSTRUCTIONS,
    SCANNER,
    CHART
}

@Composable
fun WireFinderApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val processor = remember { MagneticSensorProcessor(context, scope) }
    val state by processor.state.collectAsState()

    var currentLanguage by remember {
        mutableStateOf(LocaleManager.getSavedLanguage(context))
    }
    val currentStrings = remember(currentLanguage) {
        getStrings(currentLanguage)
    }

    var showLanguageDialog by remember {
        mutableStateOf(LocaleManager.isFirstLaunch(context))
    }
    val isFirstLaunch = remember {
        LocaleManager.isFirstLaunch(context)
    }

    var currentScreen by remember { mutableStateOf(Screen.INSTRUCTIONS) }
    val lifecycleOwner = LocalLifecycleOwner.current

    // Handle system back navigation gracefully
    BackHandler(enabled = currentScreen != Screen.INSTRUCTIONS) {
        when (currentScreen) {
            Screen.CHART -> currentScreen = Screen.SCANNER
            Screen.SCANNER -> currentScreen = Screen.INSTRUCTIONS
            else -> {}
        }
    }

    DisposableEffect(lifecycleOwner, currentScreen) {
        val isScanningActive = currentScreen == Screen.SCANNER || currentScreen == Screen.CHART
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (isScanningActive) {
                        processor.start()
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    processor.stop()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        if (isScanningActive) {
            processor.start()
        } else {
            processor.stop()
        }

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            processor.stop()
        }
    }

    CompositionLocalProvider(
        LocalAppLanguage provides currentLanguage,
        LocalAppStrings provides currentStrings
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    Screen.INSTRUCTIONS -> {
                        InstructionScreen(
                            onStartScanning = {
                                currentScreen = Screen.SCANNER
                            },
                            onOpenLanguageSelection = {
                                showLanguageDialog = true
                            }
                        )
                    }

                    Screen.SCANNER -> {
                        ScannerScreen(
                            state = state,
                            onOpenInstructions = {
                                currentScreen = Screen.INSTRUCTIONS
                            },
                            onOpenChart = {
                                currentScreen = Screen.CHART
                            },
                            onStartCalibration = {
                                processor.startCalibration()
                            },
                            onResetPeak = {
                                processor.resetPeak()
                            },
                            onSensitivityChanged = { value ->
                                processor.setSensitivity(value)
                            },
                            onToggleHaptic = { enabled ->
                                processor.setHapticEnabled(enabled)
                            },
                            onToggleSound = { enabled ->
                                processor.setSoundEnabled(enabled)
                            },
                            onToggleSimulation = {
                                processor.toggleSimulation()
                            },
                            onOpenLanguageSelection = {
                                showLanguageDialog = true
                            }
                        )
                    }

                    Screen.CHART -> {
                        SignalChartScreen(
                            state = state,
                            onBack = {
                                currentScreen = Screen.SCANNER
                            },
                            onStartCalibration = {
                                processor.startCalibration()
                            },
                            onResetPeak = {
                                processor.resetPeak()
                            },
                            onToggleSound = { enabled ->
                                processor.setSoundEnabled(enabled)
                            }
                        )
                    }
                }
            }

            // Overlay for Calibration progress
            if (state.isCalibrating || state.calibrationProgress >= 1.0f) {
                CalibrationOverlay(
                    isCalibrating = state.isCalibrating || state.calibrationProgress >= 1.0f,
                    progress = state.calibrationProgress,
                    noiseFloor = state.noiseFloor,
                    onDismiss = {
                        processor.dismissCalibration()
                    }
                )
            }

            // Language Selection Dialog (First launch or manual toggle)
            if (showLanguageDialog) {
                LanguageSelectionDialog(
                    currentLanguage = currentLanguage,
                    isFirstLaunch = isFirstLaunch,
                    onLanguageSelected = { selectedLang ->
                        currentLanguage = selectedLang
                        LocaleManager.saveLanguage(context, selectedLang)
                        showLanguageDialog = false
                    },
                    onDismiss = {
                        showLanguageDialog = false
                    }
                )
            }
        }
    }
}
