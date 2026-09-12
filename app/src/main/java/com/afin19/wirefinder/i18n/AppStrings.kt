package com.afin19.wirefinder.i18n

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    RUSSIAN("ru", "Russian", "Русский"),
    UKRAINIAN("uk", "Ukrainian", "Українська")
}

class Strings(
    // App header & general
    val appName: String,
    val appSubtitle: String,

    // Instruction screen
    val instPowerTitle: String,
    val instPowerDesc: String,
    val instMoveTitle: String,
    val instMoveDesc: String,
    val instCalibTitle: String,
    val instCalibDesc: String,
    val instDepthTitle: String,
    val instDepthDesc: String,
    val safetyTitle: String,
    val safetyDesc: String,
    val understandStart: String,

    // Scanner top bar & status
    val simMode: String,
    val sensorFormat: (Int) -> String,
    val chartTooltip: String,
    val hapticOnTooltip: String,
    val hapticOffTooltip: String,
    val soundOnTooltip: String,
    val soundOffTooltip: String,
    val instructionsTooltip: String,
    val languageTooltip: String,
    val noSensorNotice: String,

    // Meter & level
    val meterWireHere: String,
    val meterStrongSignal: String,
    val meterApproaching: String,
    val meterBackground: String,
    val searchingWire: String,
    val wireDetected: String,
    val strongWireSignal: String,
    val signalMax: String,
    val peakLabel: String,
    val deltaBLabel: String,

    // History chart
    val historyChartTitle: String,
    val historySecondsAgo: String,
    val historyNow: String,

    // Controls
    val openChartButton: String,
    val calibrateButton: String,
    val resetPeakButton: String,
    val sensitivityLabel: String,

    // Diagnostics / Physics
    val physicsTitle: String,
    val showDetails: String,
    val hideDetails: String,
    val rawMagnitudeLabel: String,
    val baselineLabel: String,
    val deltaBAmplitudeLabel: String,
    val noiseFloorLabel: String,
    val samplingRateLabel: String,
    val turnOnSim: String,
    val turnOffSim: String,

    // Calibration Overlay
    val calibratingTitle: String,
    val calibratedTitle: String,
    val calibratingHint: String,
    val calibratedHint: String,
    val backgroundNoiseLabel: (String) -> String,
    val readyToSearch: String,
    val secondsLeft: (Int) -> String,

    // Chart Screen
    val chartTitle: String,
    val chartFormula: String,
    val backToScanner: String,
    val hzUnit: String,
    val metricDeltaCurrent: String,
    val metricSignalSpan: String,
    val metricIndicator: String,
    val envelopeTitle: String,
    val envelopeSubtitle: String,
    val baselineGridText: String,

    // Language Dialog
    val chooseLanguageTitle: String,
    val chooseLanguageDesc: String,
    val confirmLanguage: String
)

val EnglishStrings = Strings(
    appName = "Wire Finder",
    appSubtitle = "Live Wire & AC Magnetic Field Detector",

    instPowerTitle = "Line must be under electrical load",
    instPowerDesc = "Turn on a light, kettle, heater, or iron on the circuit you are testing. The 50/60 Hz current generates an alternating magnetic field that the sensor can register.",
    instMoveTitle = "Move phone slowly against the wall",
    instMoveDesc = "Hold the back of the phone firmly against the wall and glide slowly (1–2 cm/sec). The peak in signal corresponds directly to the wire's location.",
    instCalibTitle = "Calibrate on a clear wall first",
    instCalibDesc = "Place the phone on a clear spot of wall with no wiring for 2–3 seconds and press 'Calibrate' to zero out ambient geomagnetic background noise.",
    instDepthTitle = "Detection depth: 1.5–3 cm",
    instDepthDesc = "Designed for standard plaster, drywall, and wood partitions. Deeply buried conduits in heavy reinforced concrete may shield the signal.",
    safetyTitle = "Safety Advisory & Warning",
    safetyDesc = "This application is an auxiliary inspection tool and does not replace certified professional wall scanners. Always observe strict safety protocols before drilling or cutting into walls!",
    understandStart = "Got it, Start Scanning",

    simMode = "Test Mode (Simulation)",
    sensorFormat = { rate -> "$rate Hz • Magnetometer" },
    chartTooltip = "Signal chart",
    hapticOnTooltip = "Vibration ON",
    hapticOffTooltip = "Vibration OFF",
    soundOnTooltip = "Sound ON",
    soundOffTooltip = "Sound OFF",
    instructionsTooltip = "Instructions",
    languageTooltip = "Change Language",
    noSensorNotice = "Hardware magnetometer not detected on this device. AC 50 Hz simulation has been activated for demonstration.",

    meterWireHere = "WIRE DETECTED HERE!",
    meterStrongSignal = "STRONG SIGNAL",
    meterApproaching = "APPROACHING WIRE",
    meterBackground = "BACKGROUND LEVEL",
    searchingWire = "Searching for wire...",
    wireDetected = "Wire detected",
    strongWireSignal = "Strong wire signal!",
    signalMax = "MAX SIGNAL",
    peakLabel = "PEAK",
    deltaBLabel = "ΔB signal:",

    historyChartTitle = "Signal Dynamic (wall sweep trajectory)",
    historySecondsAgo = "-8 sec",
    historyNow = "Now",

    openChartButton = "Open Signal Chart ΔB(t)",
    calibrateButton = "Calibrate",
    resetPeakButton = "Reset Peak",
    sensitivityLabel = "Sensitivity:",

    physicsTitle = "Sensor Telemetry & Physics",
    showDetails = "Details",
    hideDetails = "Hide",
    rawMagnitudeLabel = "Total vector |B| (Earth + field)",
    baselineLabel = "Slow trend baseline",
    deltaBAmplitudeLabel = "Oscillation amplitude ΔB (peak-to-peak)",
    noiseFloorLabel = "Noise floor",
    samplingRateLabel = "Magnetometer sampling rate",
    turnOnSim = "Enable simulation",
    turnOffSim = "Disable simulation",

    calibratingTitle = "Calibrating...",
    calibratedTitle = "Calibration Complete",
    calibratingHint = "Slowly slide phone over a clear wall section, pressing the back flat against the surface.",
    calibratedHint = "Sensor baseline noise floor measured and locked.",
    backgroundNoiseLabel = { noise -> "Noise floor: $noise µT" },
    readyToSearch = "Ready to Search",
    secondsLeft = { sec -> "Time left: $sec s" },

    chartTitle = "Signal Chart",
    chartFormula = "ΔB(t) = |B| − baseline (~1 sec)",
    backToScanner = "Back to scanner",
    hzUnit = "Hz",
    metricDeltaCurrent = "ΔB(t) CURRENT",
    metricSignalSpan = "SPAN (SIGNAL)",
    metricIndicator = "LEVEL",
    envelopeTitle = "Envelope signal(t)",
    envelopeSubtitle = "Smoothed peak-to-peak amplitude",
    baselineGridText = " 0.00 µT (baseline)",

    chooseLanguageTitle = "Choose Language",
    chooseLanguageDesc = "Select your preferred language for Wire Finder. You can change this at any time in the settings.",
    confirmLanguage = "Continue"
)

val RussianStrings = Strings(
    appName = "Wire Finder",
    appSubtitle = "Поиск скрытой проводки под нагрузкой",

    instPowerTitle = "Линия должна быть под нагрузкой",
    instPowerDesc = "Включите свет, обогреватель, утюг или другой электроприбор на проверяемой линии. Ток 50 Гц создает переменное магнитное поле, которое улавливает сенсор.",
    instMoveTitle = "Ведите телефон медленно и плотно к стене",
    instMoveDesc = "Прижимайте заднюю панель телефона к стене и перемещайте плавно (1–2 см/сек). Положение локального максимума сигнала соответствует положению провода.",
    instCalibTitle = "Обязательная калибровка на пустой стене",
    instCalibDesc = "Перед началом поиска приложите телефон к заведомо пустому участку стены на 2–3 секунды и нажмите «Калибровка» для измерения фонового шума сенсора.",
    instDepthTitle = "Глубина обнаружения: 1.5–3 см",
    instDepthDesc = "Метод рассчитан на проводку под типичной штукатуркой или гипсокартоном. Глубоко заложенную проводку в толстом железобетоне метод не обнаруживает.",
    safetyTitle = "Внимание и техника безопасности",
    safetyDesc = "Приложение является вспомогательным инструментом и не заменяет сертифицированный профессиональный детектор. Всегда соблюдайте технику безопасности при сверлении и штроблении стен!",
    understandStart = "Понятно, начать",

    simMode = "Тест-режим (симуляция)",
    sensorFormat = { rate -> "$rate Гц • Магнитометр" },
    chartTooltip = "График сигнала",
    hapticOnTooltip = "Вибрация вкл",
    hapticOffTooltip = "Вибрация выкл",
    soundOnTooltip = "Звук вкл",
    soundOffTooltip = "Звук выкл",
    instructionsTooltip = "Инструкция",
    languageTooltip = "Выбор языка",
    noSensorNotice = "Физический компас не обнаружен на устройстве. Включена симуляция 50 Гц для демонстрации.",

    meterWireHere = "ПРОВОДКА ЗДЕСЬ!",
    meterStrongSignal = "СИЛЬНЫЙ СИГНАЛ",
    meterApproaching = "ПРИБЛИЖЕНИЕ",
    meterBackground = "ФОНОВЫЙ УРОВЕНЬ",
    searchingWire = "Поиск проводки...",
    wireDetected = "Обнаружен провод",
    strongWireSignal = "Сильный сигнал провода!",
    signalMax = "МАКСИМУМ",
    peakLabel = "ПИК",
    deltaBLabel = "Сигнал ΔB:",

    historyChartTitle = "Динамика сигнала (траектория вдоль стены)",
    historySecondsAgo = "-8 сек",
    historyNow = "Сейчас",

    openChartButton = "Открыть график сигнала ΔB(t)",
    calibrateButton = "Калибровка",
    resetPeakButton = "Сброс пика",
    sensitivityLabel = "Чувствительность:",

    physicsTitle = "Данные сенсора и физика",
    showDetails = "Подробнее",
    hideDetails = "Скрыть",
    rawMagnitudeLabel = "Полный вектор |B| (Земля + поле)",
    baselineLabel = "Медленный тренд baseline",
    deltaBAmplitudeLabel = "Амплитуда колебаний ΔB (peak-to-peak)",
    noiseFloorLabel = "Базовый уровень шума (noise floor)",
    samplingRateLabel = "Частота опроса магнитометра",
    turnOnSim = "Включить симуляцию",
    turnOffSim = "Выключить симуляцию",

    calibratingTitle = "Идёт калибровка...",
    calibratedTitle = "Калибровка выполнена",
    calibratingHint = "Плавно водите телефоном по заведомо пустому участку стены, плотно прижимая заднюю крышку.",
    calibratedHint = "Базовый шум сенсора измерен и зафиксирован.",
    backgroundNoiseLabel = { noise -> "Фоновый шум: $noise µT" },
    readyToSearch = "Готово к поиску",
    secondsLeft = { sec -> "Осталось: $sec сек" },

    chartTitle = "График сигнала",
    chartFormula = "ΔB(t) = |B| − baseline (~1 сек)",
    backToScanner = "Назад к сканеру",
    hzUnit = "Гц",
    metricDeltaCurrent = "ΔB(t) ТЕКУЩЕЕ",
    metricSignalSpan = "РАЗМАХ (SIGNAL)",
    metricIndicator = "ИНДИКАТОР",
    envelopeTitle = "Огибающая signal(t)",
    envelopeSubtitle = "Сглаженный размах колебаний",
    baselineGridText = " 0.00 µT (baseline)",

    chooseLanguageTitle = "Выберите язык",
    chooseLanguageDesc = "Выберите язык приложения Wire Finder. Вы сможете изменить его в любой момент.",
    confirmLanguage = "Продолжить"
)

val UkrainianStrings = Strings(
    appName = "Wire Finder",
    appSubtitle = "Пошук прихованої проводки під навантаженням",

    instPowerTitle = "Лінія має бути під навантаженням",
    instPowerDesc = "Увімкніть світло, чайник, обігрівач або праску на лінії, що перевіряється. Струм 50 Гц створює змінне магнітне поле, яке вловлює сенсор.",
    instMoveTitle = "Ведіть телефон повільно і щільно до стіни",
    instMoveDesc = "Притискайте задню панель телефону до стіни та переміщуйте плавно (1–2 см/сек). Положення локального максимуму сигналу відповідає положенню дроту.",
    instCalibTitle = "Обов'язкове калібрування на порожній стіні",
    instCalibDesc = "Перед початком пошуку прикладіть телефон до порожньої ділянки стіни без проводів на 2–3 секунди та натисніть «Калібрування» для вимірювання фонового шуму.",
    instDepthTitle = "Глибина виявлення: 1.5–3 см",
    instDepthDesc = "Метод розрахований на проводку під типовою штукатуркою або гіпсокартоном. Глибоко закладену проводку в товстому залізобетоні метод не виявляє.",
    safetyTitle = "Увага та техніка безпеки",
    safetyDesc = "Додаток є допоміжним інструментом і не замінює сертифікований професійний детектор. Завжди дотримуйтесь правил безпеки під час свердління та штроблення стін!",
    understandStart = "Зрозуміло, почати",

    simMode = "Тест-режим (симуляція)",
    sensorFormat = { rate -> "$rate Гц • Магнітометр" },
    chartTooltip = "Графік сигналу",
    hapticOnTooltip = "Вібрація увімк",
    hapticOffTooltip = "Вібрація вимк",
    soundOnTooltip = "Звук увімк",
    soundOffTooltip = "Звук вимк",
    instructionsTooltip = "Інструкція",
    languageTooltip = "Вибір мови",
    noSensorNotice = "Фізичний компас не знайдено на пристрої. Увімкнено симуляцію 50 Гц для демонстрації.",

    meterWireHere = "ПРОВОДКА ТУТ!",
    meterStrongSignal = "СИЛЬНИЙ СИГНАЛ",
    meterApproaching = "НАБЛИЖЕННЯ",
    meterBackground = "ФОНОВИЙ РІВЕНЬ",
    searchingWire = "Пошук проводки...",
    wireDetected = "Знайдено провід",
    strongWireSignal = "Сильний сигнал дроту!",
    signalMax = "МАКСИМУМ",
    peakLabel = "ПІК",
    deltaBLabel = "Сигнал ΔB:",

    historyChartTitle = "Динаміка сигналу (траєкторія вздовж стіни)",
    historySecondsAgo = "-8 сек",
    historyNow = "Зараз",

    openChartButton = "Відкрити графік сигналу ΔB(t)",
    calibrateButton = "Калібрування",
    resetPeakButton = "Скидання піку",
    sensitivityLabel = "Чутливість:",

    physicsTitle = "Дані сенсора та фізика",
    showDetails = "Детальніше",
    hideDetails = "Приховати",
    rawMagnitudeLabel = "Повний вектор |B| (Земля + поле)",
    baselineLabel = "Повільний тренд baseline",
    deltaBAmplitudeLabel = "Амплітуда коливань ΔB (peak-to-peak)",
    noiseFloorLabel = "Базовий рівень шуму (noise floor)",
    samplingRateLabel = "Частота опитування магнітометра",
    turnOnSim = "Увімкнути симуляцію",
    turnOffSim = "Вимкнути симуляцію",

    calibratingTitle = "Йде калібрування...",
    calibratedTitle = "Калібрування виконано",
    calibratingHint = "Плавно водіть телефоном по вільній від проводки ділянці стіни, щільно притискаючи задню кришку.",
    calibratedHint = "Базовий шум сенсора виміряно та зафіксовано.",
    backgroundNoiseLabel = { noise -> "Фоновий шум: $noise µT" },
    readyToSearch = "Готово до пошуку",
    secondsLeft = { sec -> "Залишилося: $sec сек" },

    chartTitle = "Графік сигналу",
    chartFormula = "ΔB(t) = |B| − baseline (~1 сек)",
    backToScanner = "Назад до сканера",
    hzUnit = "Гц",
    metricDeltaCurrent = "ΔB(t) ПОТОЧНЕ",
    metricSignalSpan = "РОЗМАХ (SIGNAL)",
    metricIndicator = "ІНДИКАТОР",
    envelopeTitle = "Обвідна signal(t)",
    envelopeSubtitle = "Згладжений розмах коливань",
    baselineGridText = " 0.00 µT (baseline)",

    chooseLanguageTitle = "Оберіть мову",
    chooseLanguageDesc = "Оберіть мову додатка Wire Finder. Ви зможете змінити її в будь-який момент.",
    confirmLanguage = "Продовжити"
)

fun getStrings(language: AppLanguage): Strings = when (language) {
    AppLanguage.ENGLISH -> EnglishStrings
    AppLanguage.RUSSIAN -> RussianStrings
    AppLanguage.UKRAINIAN -> UkrainianStrings
}

val LocalAppStrings = staticCompositionLocalOf { EnglishStrings }
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }

object LocaleManager {
    private const val PREFS_NAME = "wire_finder_settings"
    private const val KEY_LANGUAGE = "selected_language"
    private const val KEY_LANGUAGE_SELECTED_ONCE = "language_selected_once"

    fun isFirstLaunch(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return !prefs.getBoolean(KEY_LANGUAGE_SELECTED_ONCE, false)
    }

    fun getSavedLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.code) ?: AppLanguage.ENGLISH.code
        return AppLanguage.entries.firstOrNull { it.code == code } ?: AppLanguage.ENGLISH
    }

    fun saveLanguage(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LANGUAGE, language.code)
            .putBoolean(KEY_LANGUAGE_SELECTED_ONCE, true)
            .apply()
    }
}
