# Terminal Emulator + AI

A native Android terminal emulator app built on the Android NDK, providing a Unix-like shell with full package management compatibility. Features a split interface with terminal windows alongside a dedicated environment for downloading and running local AI models.

## Features

- **Native Terminal Emulator**: Built on Android NDK with PTY support
- **Unix-like Shell**: Full shell environment with command history, environment variables, and working directory management
- **Package Management**: Install/remove/list packages (apt/pkg compatible)
- **Split Interface**: Terminal windows + AI model management side by side
- **AI Model Backend**: ExecuTorch and TensorFlow Lite support
- **NPU Driver Bridge**: Hardware acceleration across CPU, GPU, and NPU
- **Thermal Management**: Automatic performance scaling based on device temperature
- **State Saving**: Automatic session preservation at 50% capacity threshold
- **Gotcha AI Integration**: Full compatibility with Gotcha AI assistant
- **Accessibility Services**: Screen reading and device control
- **Gemma Models**: Pre-configured with RAG memory and dynamic skill learning
- **CrewAI Integration**: Multi-agent AI orchestration built into APK

## Architecture

```
app/
├── src/main/
│   ├── java/com/termuxai/terminal/
│   │   ├── TerminalApplication.kt          # App initialization
│   │   ├── ui/
│   │   │   ├── MainActivity.kt            # Main split interface
│   │   │   ├── TerminalActivity.kt        # Terminal window
│   │   │   └── AIModelActivity.kt         # AI model manager
│   │   ├── terminal/
│   │   │   ├── TerminalSession.kt         # Session management
│   │   │   └── TerminalView.kt            # Terminal UI
│   │   ├── ai/
│   │   │   └── AIModelManager.kt          # Model download/management
│   │   ├── accessibility/
│   │   │   └── TerminalAccessibilityService.kt  # Screen reading & control
│   │   ├── services/
│   │   │   ├── StateSavingService.kt      # Auto state preservation
│   │   │   └── BootReceiver.kt            # Boot restoration
│   │   └── utils/
│   │       ├── NativeTerminal.kt          # JNI bridge
│   │       ├── ThermalManager.kt          # Thermal management
│   │       └── GotchaIntegration.kt       # Gotcha AI integration
│   ├── cpp/
│   │   ├── CMakeLists.txt                 # NDK build config
│   │   ├── terminal_jni.cpp               # JNI implementation
│   │   ├── shell_session.cpp              # Shell environment
│   │   ├── command_executor.cpp           # Command execution
│   │   └── pty_manager.cpp                # PTY management
│   └── res/                               # Android resources
├── build.gradle                            # App build config
└── proguard-rules.pro                     # ProGuard rules
```

## Build Instructions

### Prerequisites

1. **Android Studio** (Hedgehog or later) with:
   - Android SDK 34 (API level 34)
   - Android NDK 25.2.9519653 or later
   - CMake 3.22.1 or later

2. **Java Development Kit (JDK) 17**

3. **Gradle 8.2+**

### Building the APK

#### Option 1: Using Android Studio

1. Open the project in Android Studio
2. Sync Gradle files
3. Select `Build > Build Bundle(s) / APK(s) > Build APK(s)`
4. The APK will be saved to `app/build/outputs/apk/debug/app-debug.apk`

#### Option 2: Using Command Line (Gradle)

```bash
# Navigate to project directory
cd /path/to/TerminalEmulator

# Build debug APK
./gradlew assembleDebug

# The APK will be at:
# app/build/outputs/apk/debug/app-debug.apk
```

#### Option 3: Using Termux (on-device build)

```bash
# Install required packages
pkg install openjdk-17 gradle android-ndk cmake -y

# Set up environment
export ANDROID_HOME=/path/to/android/sdk
export NDK_HOME=$ANDROID_HOME/ndk/25.2.9519653

# Build
cd ~/TerminalEmulator
gradle assembleDebug
```

### Installing the APK

#### Automatic Installation (via ADB)

```bash
# Connect device via USB or wireless debugging
adb devices

# Install APK automatically
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Or install to specific device
adb -s <device_id> install -r app/build/outputs/apk/debug/app-debug.apk
```

#### Save to Downloads Folder

```bash
# Copy APK to Downloads folder
cp app/build/outputs/apk/debug/app-debug.apk /storage/emulated/0/Download/TerminalEmulator.apk

# Or for release builds
cp app/build/outputs/apk/release/app-release.apk /storage/emulated/0/Download/TerminalEmulator.apk
```

#### Manual Installation

1. Copy the APK to your device's Downloads folder
2. Open the file manager on your device
3. Navigate to Downloads
4. Tap on `TerminalEmulator.apk`
5. Allow installation from unknown sources if prompted
6. Follow the installation wizard

### Building Release APK

```bash
# Generate signing key (first time only)
keytool -genkey -v -keystore terminal-release.keystore -alias terminal -keyalg RSA -keysize 2048 -validity 10000

# Build release APK
./gradlew assembleRelease

# Sign the APK
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
    -keystore terminal-release.keystore \
    app/build/outputs/apk/release/app-release-unsigned.apk terminal

# Align the APK
zipalign -v 4 \
    app/build/outputs/apk/release/app-release-unsigned.apk \
    app/build/outputs/apk/release/TerminalEmulator.apk

# Copy to Downloads
cp app/build/outputs/apk/release/TerminalEmulator.apk /storage/emulated/0/Download/
```

### Automated Build Script

Save this as `build_and_install.sh`:

```bash
#!/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
OUTPUT_DIR="/storage/emulated/0/Download"

echo "=== Building Terminal Emulator + AI ==="

cd "$PROJECT_DIR"

# Clean previous builds
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Find the APK
APK_PATH=$(find . -name "*.apk" -path "*/debug/*" | head -1)

if [ -z "$APK_PATH" ]; then
    echo "ERROR: APK not found!"
    exit 1
fi

echo "APK built: $APK_PATH"

# Copy to Downloads folder
cp "$APK_PATH" "$OUTPUT_DIR/TerminalEmulator.apk"
echo "APK saved to: $OUTPUT_DIR/TerminalEmulator.apk"

# Try to install via ADB
if command -v adb &> /dev/null; then
    adb devices | grep -q "device$" && {
        echo "Installing via ADB..."
        adb install -r "$OUTPUT_DIR/TerminalEmulator.apk"
        echo "Installation complete!"
    } || {
        echo "No ADB device connected. APK saved to Downloads folder."
        echo "Install manually from: $OUTPUT_DIR/TerminalEmulator.apk"
    }
else
    echo "ADB not available. APK saved to Downloads folder."
    echo "Install manually from: $OUTPUT_DIR/TerminalEmulator.apk"
fi

echo "=== Build Complete ==="
```

## Configuration

### Gotcha AI Integration

The app integrates with Gotcha AI through:
1. Accessibility Service for screen reading
2. Broadcast intents for command execution
3. Shared storage for model files

To enable Gotcha integration:
1. Install both apps on the same device
2. Enable the Terminal Emulator accessibility service in Settings
3. Gotcha will automatically detect and communicate with the terminal

### AI Model Setup

1. Open the AI Model Manager in the app
2. Download desired models (Gemma 2B, Gemma 7B, etc.)
3. Models are stored in `/storage/emulated/0/Android/data/com.termuxai.terminal/files/models/`
4. Models can also be shared with Gotcha AI

### NPU/GPU Acceleration

The app automatically detects and uses available hardware:
- **NPU**: Qualcomm Hexagon, MediaTek APU, Samsung NPU
- **GPU**: Vulkan, OpenCL, OpenGL ES
- **CPU**: ARM NEON, SIMD optimizations

Thermal management automatically scales performance based on device temperature.

### State Saving

The app automatically saves session state when:
- System memory reaches 50% capacity
- Storage reaches 50% capacity
- Device temperature exceeds safe thresholds
- App is backgrounded for extended periods

States are restored automatically on next launch.

## Permissions

The app requires the following permissions:
- `INTERNET`: For model downloads
- `ACCESS_NETWORK_STATE`: Network status
- `WRITE_EXTERNAL_STORAGE`: File management
- `FOREGROUND_SERVICE`: Background AI processing
- `BIND_ACCESSIBILITY_SERVICE`: Screen reading and control
- `SYSTEM_ALERT_WINDOW`: Overlay windows
- `REQUEST_INSTALL_PACKAGES`: APK installation

## Troubleshooting

### Build Errors

1. **NDK not found**: Set `ANDROID_NDK_HOME` environment variable
2. **CMake errors**: Ensure CMake 3.22+ is installed
3. **Gradle sync fails**: Check internet connection and proxy settings

### Runtime Issues

1. **Terminal not working**: Check if `/system/bin/sh` exists
2. **AI models not loading**: Ensure sufficient storage space
3. **Accessibility not working**: Re-enable in Settings > Accessibility
4. **Thermal throttling**: Device may be overheating; reduce workload

## License

MIT License - See LICENSE file for details.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## Credits

- Terminal emulation based on Termux
- AI inference powered by ExecuTorch and TensorFlow Lite
- NDK optimizations for ARM architecture
- Integration with Gotcha AI assistant