# Quick Start Guide - Zbuntu Terminal

Get started with Zbuntu Terminal in minutes!

## For Users

### Installing from APK

1. **Download the APK**
   - Get the latest release from GitHub Releases
   - Or build from source (see below)

2. **Enable Unknown Sources**
   - Settings → Security → Unknown Sources (Android 7)
   - Settings → Apps → Special Access → Install Unknown Apps (Android 8+)

3. **Install the APK**
   - Open the downloaded APK file
   - Tap "Install"
   - Wait for installation to complete

4. **Launch the App**
   - Tap "Open" or find "Zbuntu Terminal" in app drawer
   - Grant required permissions if prompted

5. **First Launch**
   - App will download Termux bootstrap (~50-100 MB)
   - Wait for installation to complete (2-5 minutes)
   - Terminal session will start automatically

6. **Start Using**
   ```bash
   # You're now in a Linux shell!
   ls -la
   pwd
   echo "Hello from Android!"
   ```

### Basic Usage

#### Running Commands
```bash
# File operations
ls
cd /data/data/com.zbuntu.terminal/files/home
mkdir myproject
touch file.txt

# Text editing
echo "Hello World" > hello.txt
cat hello.txt

# Package management (after bootstrap)
pkg update
pkg install nano
pkg install git
```

#### Keyboard Shortcuts
- **Enter**: Execute command
- **Backspace**: Delete character
- **Arrow Keys**: Navigate (if available)
- **Tab**: Auto-complete (basic)

#### Tips
- Tap screen to show keyboard
- Use volume keys for special keys (Ctrl, Alt) on some devices
- Swipe to scroll through terminal history
- Long-press for context menu (if available)

## For Developers

### Quick Build and Run

1. **Prerequisites**
   - Android Studio installed
   - Git installed

2. **Clone and Open**
   ```bash
   git clone https://github.com/thertxnetwork/zbuntu.git
   cd zbuntu
   # Open in Android Studio
   ```

3. **Install SDK Components**
   - Android Studio → Tools → SDK Manager
   - Install: NDK, CMake, SDK 34

4. **Build and Run**
   - Click Run button (green triangle)
   - Select device/emulator
   - Wait for build and installation

### Command Line Build

```bash
# Quick build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

## Common Issues

### Bootstrap Download Fails
**Problem**: Network error during bootstrap installation

**Solution**:
- Check internet connection
- Try on WiFi instead of mobile data
- Download bootstrap manually and place in app files
- Restart app

### Terminal Not Starting
**Problem**: Session ends immediately

**Solution**:
- Check bootstrap installation completed
- Verify shell exists: `/data/data/.../files/usr/bin/sh`
- Check logcat for errors:
  ```bash
  adb logcat | grep Zbuntu
  ```

### Keyboard Not Showing
**Problem**: On-screen keyboard doesn't appear

**Solution**:
- Tap the terminal view
- Check keyboard settings
- Try different keyboard app
- Restart app

### Commands Not Found
**Problem**: Shell commands not available

**Solution**:
- Wait for bootstrap to complete fully
- Check PATH variable:
  ```bash
  echo $PATH
  ```
- Reinstall bootstrap (clear app data)

### Build Errors
**Problem**: Android Studio build fails

**Solution**:
- Sync Gradle files
- Install NDK and CMake
- Clear cache: Build → Clean Project
- Invalidate caches: File → Invalidate Caches → Restart

## Next Steps

### For Users
1. Explore Linux commands
2. Install packages with `pkg install`
3. Create projects in home directory
4. Learn shell scripting

### For Developers
1. Read [ARCHITECTURE.md](ARCHITECTURE.md) for code structure
2. Read [BUILD.md](BUILD.md) for detailed build guide
3. Check [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines
4. Explore the codebase

## Resources

### Documentation
- [README.md](README.md) - Project overview
- [ARCHITECTURE.md](ARCHITECTURE.md) - Technical details
- [BUILD.md](BUILD.md) - Build instructions
- [CONTRIBUTING.md](CONTRIBUTING.md) - Contribution guide

### External Links
- [Termux Wiki](https://wiki.termux.com/)
- [Linux Command Reference](https://linux.die.net/)
- [Android NDK Guide](https://developer.android.com/ndk/guides)
- [PTY Documentation](https://man7.org/linux/man-pages/man7/pty.7.html)

### Example Commands

#### Package Management
```bash
# Update package lists
pkg update

# Upgrade installed packages
pkg upgrade

# Install a package
pkg install python
pkg install nodejs
pkg install git

# Search for packages
pkg search editor

# List installed packages
pkg list-installed
```

#### File Operations
```bash
# Navigate
cd ~
cd /sdcard  # If storage permission granted

# Create directories
mkdir -p projects/myapp

# Create files
touch README.md
echo "# My Project" > README.md

# View files
cat README.md
less README.md
nano README.md
```

#### Development
```bash
# Python
pkg install python
python --version
python script.py

# Node.js
pkg install nodejs
node --version
npm --version

# Git
pkg install git
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
git clone https://github.com/user/repo.git
```

## Getting Help

### App Issues
- Check GitHub Issues
- Create new issue with details
- Include logs and device info

### Linux/Shell Help
- Use `man` command: `man ls`
- Use `--help` flag: `ls --help`
- Visit Termux wiki
- Search online tutorials

### Development Help
- Read source code comments
- Check Android documentation
- Ask in GitHub discussions
- Review similar projects

## Support the Project

- ⭐ Star the repository
- 🐛 Report bugs
- 💡 Suggest features
- 🔧 Contribute code
- 📖 Improve documentation
- 🔗 Share with others

---

**Happy Terminal Hacking! 🚀**
