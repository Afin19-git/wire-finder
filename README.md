# Wire Finder

Wire Finder is an open-source Android application that uses your device's magnetic sensor to detect live electrical wiring in walls. This utility tool helps you locate hidden wires and cables before drilling or cutting into walls.

## Features

- 🧲 **Magnetic Sensor Detection** - Uses high-sampling-rate magnetometer to detect electromagnetic fields
- 📊 **Real-time Signal Visualization** - View signal strength in real time with interactive charts
- 🔊 **Audio Feedback** - Beeper indicates proximity to detected wires
- 🎯 **Calibration Support** - Calibrate sensor readings for accurate detection
- 🌍 **Multi-language Support** - Available in multiple languages
- 🎨 **Design** - Modern, intuitive dark theme interface

## Requirements

- Android 7.0 (API level 24) or higher
- Device with magnetometer sensor
- Vibration capability (optional, for feedback)

## Installation

### From Source

1. Clone the repository:
```bash
git clone https://github.com/afin19/wire-finder.git
cd wire-finder
```

2. Open in Android Studio:
   - Select **File** → **Open**
   - Choose the `wire-finder` directory
   - Let Android Studio import and sync the project

3. Build and run:
   - Connect an Android device or open an emulator
   - Click **Run** → **Run 'app'** or press `Shift + F10`

## Build from Command Line

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

## Permissions Required

- `VIBRATE` - For haptic feedback
- `HIGH_SAMPLING_RATE_SENSORS` - For high-precision magnetometer access

## Project Structure

```
wire-finder/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/afin19/wirefinder/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── sensor/MagneticSensorProcessor.kt
│   │   │   │   ├── ui/                    # Compose UI screens
│   │   │   │   └── i18n/                  # Internationalization
│   │   │   └── res/                       # Resources (strings, drawables, etc.)
│   │   ├── test/                          # Unit tests
│   │   └── androidTest/                   # Instrumented tests
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml                 # Dependency versions
└── README.md
```

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Kotlin Coroutines
- **Database**: Room (for future data persistence)
- **Testing**: JUnit, Robolectric, Roborazzi

## Contributing

Contributions are welcome! Here's how you can help:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the GPL-3.0 License - see the [LICENSE](LICENSE) file for details.

## Disclaimer

This application provides a basic tool for detecting electromagnetic fields. It should not be relied upon as the sole method for electrical safety. Always follow proper safety procedures when working with electrical installations. If you're unsure about electrical wiring, consult a qualified electrician.

## Troubleshooting

### App not detecting wires
- Ensure your device has a magnetometer sensor
- Try calibrating the sensor before scanning
- Move device slowly and steadily across walls
- Some devices may have poor sensor quality

### Build errors
- Make sure you have Android SDK 36 installed
- Run `./gradlew clean` before rebuilding
- Check that your Java version is 11 or higher

## Contact & Support

For issues, feature requests, or questions:
- Open an issue on GitHub
- Check existing issues before creating a new one

---

**Happy scanning! 🔍**
