# Zbuntu Terminal - Native Android Terminal Emulator with C++

A native Android terminal emulator application built with C++ that provides a Linux-like terminal environment using the Termux bootstrap package.

## Features

- **Native C++ Implementation**: Uses JNI for PTY (pseudo-terminal) management
- **Termux Bootstrap Integration**: Automatically downloads and installs Termux packages
- **Terminal Emulation**: Basic VT100 terminal emulation
- **Multiple Architecture Support**: arm, arm64, x86, x86_64
- **Full Linux Environment**: Access to shell, coreutils, and package management via APT

## Architecture

### Components

1. **Native Layer (C++)**
   - `termux.c` - PTY creation and subprocess management
   - `termux_jni.cpp` - JNI bridge functions
   - Uses `/dev/ptmx` for pseudo-terminal allocation
   - Handles fork/exec for shell processes

2. **Java Layer**
   - `BootstrapManager` - Downloads and installs Termux bootstrap packages
   - `TerminalSession` - Manages terminal sessions and I/O
   - `TerminalView` - Custom view for rendering terminal output
   - `TermuxService` - Background service for session management
   - `MainActivity` - Main UI controller

3. **File System Structure**
   ```
   /data/data/com.zbuntu.terminal/files/
   ├── home/           # User home directory
   └── usr/            # Termux system files
       ├── bin/        # Executables
       ├── lib/        # Libraries
       ├── etc/        # Configuration
       └── tmp/        # Temporary files
   ```

## Building

### Prerequisites

- Android Studio 2024.1 or later
- Android SDK 34
- Android NDK (installed via SDK Manager)
- CMake 3.22.1 or later
- Java 11 or later

### Build Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/thertxnetwork/zbuntu.git
   cd zbuntu
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Install NDK and CMake**
   - Go to `Tools > SDK Manager > SDK Tools`
   - Check `NDK (Side by side)` and `CMake`
   - Click Apply to install

4. **Build the project**
   - Click `Build > Make Project` or press `Ctrl+F9`
   - Wait for Gradle sync and native build to complete

5. **Run on device/emulator**
   - Connect an Android device or start an emulator
   - Click `Run > Run 'app'` or press `Shift+F10`

### Command Line Build

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

The APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`

## Gradle Configuration

Key configuration files:

- `build.gradle` - Root project configuration
- `app/build.gradle` - App module configuration with NDK settings
- `app/src/main/cpp/CMakeLists.txt` - Native build configuration

## Permissions

The app requires the following permissions:
- `INTERNET` - For downloading bootstrap packages
- `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` - For file access
- `WAKE_LOCK` - To keep terminal sessions active

## Bootstrap Installation

On first launch, the app automatically:
1. Detects device architecture (arm, arm64, x86, x86_64)
2. Downloads appropriate bootstrap package from Termux releases
3. Extracts files to app private storage
4. Sets up directory structure and permissions
5. Starts terminal session with bash/sh shell

## Usage

1. Launch the app
2. Wait for bootstrap installation (first launch only)
3. Terminal session starts automatically
4. Use on-screen keyboard or physical keyboard for input
5. Supports basic terminal commands and features

### Key Mappings

- Enter → `\r` (carriage return)
- Backspace → `\b` (backspace)
- Arrow keys → VT100 escape sequences
- Tab → `\t` (tab character)

## Development

### Project Structure

```
zbuntu/
├── app/
│   ├── src/main/
│   │   ├── cpp/                    # Native C++ code
│   │   │   ├── CMakeLists.txt
│   │   │   ├── termux.c
│   │   │   └── termux_jni.cpp
│   │   ├── java/com/zbuntu/terminal/
│   │   │   ├── MainActivity.java
│   │   │   ├── BootstrapManager.java
│   │   │   ├── pty/
│   │   │   │   ├── JNI.java
│   │   │   │   └── TerminalSession.java
│   │   │   ├── view/
│   │   │   │   └── TerminalView.java
│   │   │   └── service/
│   │   │       └── TermuxService.java
│   │   ├── res/                    # Resources
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── README.md
```

### Key Classes

- **JNI**: Native method declarations
- **TerminalSession**: Manages PTY and subprocess lifecycle
- **BootstrapManager**: Handles Termux package installation
- **TerminalView**: Custom view for terminal rendering
- **TermuxService**: Background service managing sessions
- **MainActivity**: UI controller and event handler

## Technical Details

### PTY Implementation

The app creates a pseudo-terminal using:
1. `open("/dev/ptmx")` - Open master PTY
2. `grantpt()` / `unlockpt()` - Grant access to slave
3. `ptsname()` - Get slave device name
4. `fork()` - Create child process
5. `setsid()` - Create new session
6. `dup2()` - Redirect stdin/stdout/stderr to PTY
7. `execvp()` - Execute shell

### Terminal Emulation

Basic features implemented:
- Text rendering with monospace font
- Scrollback buffer (1000 lines)
- Input handling via InputConnection
- Key event mapping to terminal codes

For full VT100 emulation, consider integrating libraries like Termux's terminal-emulator.

## Future Enhancements

- [ ] Full VT100/xterm emulation
- [ ] Text selection and copy/paste
- [ ] Multiple terminal sessions/tabs
- [ ] Configurable colors and fonts
- [ ] Hardware keyboard support improvements
- [ ] Notification for background sessions
- [ ] Integration with Termux:API for Android features

## License

This project is created for educational purposes. The Termux bootstrap packages are licensed under their respective licenses.

## References

- [Termux Official Repository](https://github.com/termux/termux-app)
- [Android NDK Documentation](https://developer.android.com/ndk)
- [PTY Manual](https://man7.org/linux/man-pages/man7/pty.7.html)
- [VT100 Terminal Codes](https://vt100.net/docs/vt100-ug/)

## Support

For issues and questions:
- Create an issue on GitHub
- Check Termux documentation for package-related questions

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

---

**Note**: This is an educational project demonstrating native Android development with C++, JNI, and terminal emulation. For production use, consider the official Termux app or other established terminal emulators.