# Zbuntu Terminal Architecture

## Overview

Zbuntu Terminal is a native Android terminal emulator that integrates C++ for low-level PTY (pseudo-terminal) operations with Java for the Android UI layer. It uses the Termux bootstrap package to provide a full Linux-like environment on Android devices.

## Component Diagram

```
┌─────────────────────────────────────────────────────┐
│                   Android UI Layer                   │
│  ┌──────────────────────────────────────────────┐  │
│  │           MainActivity (Activity)             │  │
│  │  - UI Controller                              │  │
│  │  - Event Handler                              │  │
│  └──────────────────────────────────────────────┘  │
│                        │                             │
│  ┌──────────────────────────────────────────────┐  │
│  │          TerminalView (Custom View)           │  │
│  │  - Text Rendering                             │  │
│  │  - Input Handling                             │  │
│  │  - Display Management                         │  │
│  └──────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
                         │
┌─────────────────────────────────────────────────────┐
│                  Service Layer                       │
│  ┌──────────────────────────────────────────────┐  │
│  │        TermuxService (Background Service)     │  │
│  │  - Session Management                         │  │
│  │  - Lifecycle Control                          │  │
│  └──────────────────────────────────────────────┘  │
│                        │                             │
│  ┌──────────────────────────────────────────────┐  │
│  │          BootstrapManager (Java)              │  │
│  │  - Download Bootstrap Packages                │  │
│  │  - Extract and Setup                          │  │
│  │  - Environment Configuration                  │  │
│  └──────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
                         │
┌─────────────────────────────────────────────────────┐
│               Terminal Session Layer                 │
│  ┌──────────────────────────────────────────────┐  │
│  │        TerminalSession (Java)                 │  │
│  │  - Subprocess Management                      │  │
│  │  - I/O Stream Handling                        │  │
│  │  - Reader Thread                              │  │
│  └──────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
                         │
                    JNI Bridge
                         │
┌─────────────────────────────────────────────────────┐
│                Native Layer (C/C++)                  │
│  ┌──────────────────────────────────────────────┐  │
│  │              JNI Functions                    │  │
│  │  - createSubprocess()                         │  │
│  │  - waitFor()                                  │  │
│  │  - close()                                    │  │
│  └──────────────────────────────────────────────┘  │
│                        │                             │
│  ┌──────────────────────────────────────────────┐  │
│  │           PTY Implementation (C)              │  │
│  │  - open("/dev/ptmx")                          │  │
│  │  - grantpt/unlockpt                           │  │
│  │  - fork/exec                                  │  │
│  │  - Terminal Setup                             │  │
│  └──────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
                         │
┌─────────────────────────────────────────────────────┐
│                 Linux Kernel Layer                   │
│  - /dev/ptmx (PTY Master)                           │
│  - /dev/pts/* (PTY Slaves)                          │
│  - Process Management                                │
│  - File System                                       │
└─────────────────────────────────────────────────────┘
```

## Data Flow

### Session Creation

1. **User Action**: User launches app or requests new session
2. **MainActivity**: Binds to TermuxService
3. **BootstrapManager**: Checks if bootstrap is installed
4. **Bootstrap Installation** (if needed):
   - Detect device architecture
   - Download bootstrap-{arch}.zip
   - Extract to /data/data/com.zbuntu.terminal/files/usr
   - Set permissions
5. **Session Start**:
   - TermuxService.createSession()
   - TerminalSession constructed with shell path, env vars
   - TerminalSession.start() called with terminal dimensions
6. **JNI Call**:
   - JNI.createSubprocess() invoked
   - Native termux.c:create_subprocess() executed
7. **PTY Creation**:
   - Open /dev/ptmx
   - Grant and unlock PTY
   - Fork child process
   - Child: exec shell (bash/sh)
   - Parent: return PTY master FD
8. **I/O Setup**:
   - FileInputStream/FileOutputStream created from FD
   - Reader thread started
9. **Active Session**:
   - User input → TerminalView → TerminalSession.write() → PTY
   - Shell output → PTY → Reader thread → processOutput() → TerminalView

### Input Processing

```
User Keyboard Input
       ↓
TerminalView.onKeyDown() / onTextInput()
       ↓
mapKeyEvent() [converts to terminal codes]
       ↓
TerminalSession.write(bytes)
       ↓
FileOutputStream.write()
       ↓
PTY Master FD
       ↓
Shell Process (stdin)
```

### Output Processing

```
Shell Process (stdout/stderr)
       ↓
PTY Slave
       ↓
PTY Master FD
       ↓
FileInputStream.read() [in reader thread]
       ↓
TerminalSession.processOutput()
       ↓
TerminalSessionClient.onTextChanged()
       ↓
MainActivity (UI thread)
       ↓
TerminalView.appendText()
       ↓
View.invalidate() → onDraw()
       ↓
Display on Screen
```

## File System Structure

```
/data/data/com.zbuntu.terminal/files/
├── home/                          # User home directory ($HOME)
│   ├── .hushlogin                # Suppress login messages
│   └── [user files]
│
└── usr/                           # System directory ($PREFIX)
    ├── bin/                       # Executables
    │   ├── sh                    # Shell
    │   ├── bash                  # Bash shell
    │   ├── ls, cat, etc.         # Core utilities
    │   └── [other binaries]
    │
    ├── lib/                       # Shared libraries
    │   ├── libc.so
    │   └── [other libraries]
    │
    ├── etc/                       # Configuration files
    │   ├── profile
    │   └── [other configs]
    │
    ├── share/                     # Shared data
    ├── libexec/                   # Helper programs
    ├── tmp/                       # Temporary files
    └── var/                       # Variable data
```

## Threading Model

### Main Thread (UI Thread)
- Runs Android activities and views
- Handles UI events
- Updates TerminalView display
- **Must not** perform blocking I/O

### Reader Thread
- Created per TerminalSession
- Continuously reads from PTY master FD
- Blocks on read() until data available
- Processes output and notifies client
- Runs until session ends

### Bootstrap Installation Thread
- Created when installing bootstrap
- Downloads zip file over network
- Extracts files to storage
- Updates progress on UI thread

### Service Thread
- Runs TermuxService
- Manages session lifecycle
- Can be called from any thread (synchronized)

## JNI Interface

### Native Methods (Java → C)

```java
// Create subprocess with PTY
int createSubprocess(
    String cmd,           // Command to execute
    String cwd,           // Working directory
    String[] args,        // Command arguments
    String[] envVars,     // Environment variables
    int[] processId,      // Output: process ID
    int rows,            // Terminal rows
    int columns          // Terminal columns
) → int fd              // Returns PTY master FD
```

```java
// Wait for process completion
int waitFor(int pid) → int exitCode
```

```java
// Close file descriptor
void close(int fd)
```

### Native Implementation (C)

Key functions in termux.c:
- `create_subprocess()`: Core PTY and process creation
- Uses POSIX APIs: open, grantpt, unlockpt, ptsname, fork, setsid, execvp
- Sets terminal size with ioctl(TIOCSWINSZ)
- Redirects stdin/stdout/stderr to PTY slave

## Environment Variables

Set by BootstrapManager for each session:

```bash
TERM=xterm-256color              # Terminal type
HOME=/data/.../files/home        # User home
PATH=/data/.../files/usr/bin     # Executable path
PREFIX=/data/.../files/usr       # Prefix for packages
TMPDIR=/data/.../files/usr/tmp   # Temp directory
SHELL=/data/.../files/usr/bin/sh # Default shell
LANG=en_US.UTF-8                 # Locale
```

## Bootstrap Package

The bootstrap package contains:
- **bin/**: Core utilities (sh, bash, ls, cat, etc.)
- **lib/**: Required libraries (libc, etc.)
- **etc/**: Configuration files
- **APT tools**: pkg, apt, dpkg for package management

Downloaded from: `https://github.com/termux/termux-packages/releases/`

Architectures supported:
- aarch64 (ARM 64-bit)
- arm (ARM 32-bit)
- x86_64 (Intel/AMD 64-bit)
- i686 (Intel/AMD 32-bit)

## Build System

### Gradle Configuration
- `build.gradle`: Root project setup
- `app/build.gradle`: App module with NDK configuration
- Specifies CMake version, ABI filters, STL type

### CMake Build
- `CMakeLists.txt`: Defines native library
- Compiles C and C++ sources
- Links Android libraries (log, android)
- Outputs `libtermux-native.so`

### Build Process
1. Gradle syncs project
2. CMake generates native build files
3. Native code compiled for each ABI
4. Java code compiled to DEX
5. Resources processed and packaged
6. APK assembled and signed

## Security Considerations

### Permissions
- Internet: For bootstrap download
- Storage: For file access (if needed)
- No root required: All operations in app sandbox

### Sandboxing
- App runs in Android app sandbox
- File system isolated to /data/data/com.zbuntu.terminal/
- No access to other apps' data
- Network access controlled by Android

### Process Isolation
- Each terminal session is a separate process
- Child processes inherit app UID
- Cannot access system files without permission

## Extension Points

### Custom Commands
Add custom native commands by:
1. Implementing in C/C++
2. Compiling to binary
3. Placing in usr/bin/
4. Making executable

### Package Management
Use APT within terminal:
```bash
pkg update
pkg install <package>
```

### Plugins
Could extend with:
- Termux:API style plugins
- Intent-based communication
- Shared preferences for config

## Performance Optimization

### Efficient Rendering
- Only redraw changed regions
- Use hardware acceleration
- Optimize font rendering

### I/O Buffering
- Buffer terminal output
- Batch UI updates
- Use appropriate buffer sizes (4KB)

### Memory Management
- Limit scrollback buffer (1000 lines)
- Release resources on session end
- Clean up native handles

## Error Handling

### PTY Creation Failures
- Check /dev/ptmx accessibility
- Handle permission errors
- Retry with fallback options

### Bootstrap Download Failures
- Network error handling
- Corrupted file detection
- Retry mechanism
- Offline mode support

### Session Crashes
- Detect process termination
- Clean up resources
- Notify user
- Allow restart

## Future Enhancements

### Terminal Emulation
- Full VT100 support
- Xterm extensions
- ANSI color codes (256-color, true color)
- Mouse support

### Features
- Multiple tabs/sessions
- Split screen
- Terminal customization (fonts, colors)
- Keyboard shortcuts
- Text selection and copy/paste

### Integration
- File manager integration
- Share target support
- Notification updates
- Background execution

## References

- Linux PTY: https://man7.org/linux/man-pages/man7/pty.7.html
- Android NDK: https://developer.android.com/ndk
- Termux: https://github.com/termux/termux-app
- VT100: https://vt100.net/
