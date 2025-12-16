# Build Guide for Zbuntu Terminal

This guide provides detailed instructions for building the Zbuntu Terminal application from source.

## Prerequisites

### Required Software

1. **Android Studio** (2024.1 or later)
   - Download from: https://developer.android.com/studio
   - Install with default settings

2. **Android SDK**
   - Installed automatically with Android Studio
   - Minimum API Level: 24 (Android 7.0)
   - Target API Level: 34 (Android 14)

3. **Android NDK** (Native Development Kit)
   - Version: Latest stable
   - Install via SDK Manager in Android Studio

4. **CMake**
   - Version: 3.22.1 or later
   - Install via SDK Manager in Android Studio

5. **Java Development Kit (JDK)**
   - Version: 11 or later
   - OpenJDK or Oracle JDK

### System Requirements

- **OS**: Windows 10/11, macOS 10.14+, or Linux
- **RAM**: 8 GB minimum (16 GB recommended)
- **Disk Space**: 10 GB free space
- **Internet**: Required for downloading dependencies

## Installation Steps

### 1. Install Android Studio

#### Windows
```bash
# Download installer from https://developer.android.com/studio
# Run the installer
# Follow the setup wizard
```

#### macOS
```bash
# Download DMG from https://developer.android.com/studio
# Drag Android Studio to Applications
# Open Android Studio
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt-get update
sudo apt-get install openjdk-11-jdk
# Download tar.gz from https://developer.android.com/studio
tar -xzf android-studio-*.tar.gz
cd android-studio/bin
./studio.sh
```

### 2. Install SDK Components

Open Android Studio → Tools → SDK Manager

#### SDK Platforms Tab
- [x] Android 14.0 (API 34)
- [x] Android 7.0 (API 24)

#### SDK Tools Tab
- [x] Android SDK Build-Tools
- [x] NDK (Side by side)
- [x] CMake
- [x] Android SDK Platform-Tools
- [x] Android Emulator

Click "Apply" to install selected components.

### 3. Clone the Repository

```bash
git clone https://github.com/thertxnetwork/zbuntu.git
cd zbuntu
```

### 4. Open Project in Android Studio

1. Launch Android Studio
2. Click "Open" or "Open an Existing Project"
3. Navigate to the cloned `zbuntu` directory
4. Click "OK"

Android Studio will:
- Sync Gradle files
- Download dependencies
- Index the project
- Configure CMake

Wait for the process to complete (may take several minutes on first run).

## Building the Project

### Method 1: Android Studio GUI

1. **Sync Project**
   - Click: `File → Sync Project with Gradle Files`
   - Or click the elephant icon in the toolbar

2. **Build Project**
   - Click: `Build → Make Project`
   - Or press: `Ctrl+F9` (Windows/Linux) or `Cmd+F9` (macOS)

3. **Build APK**
   - Click: `Build → Build Bundle(s) / APK(s) → Build APK(s)`
   - Wait for build to complete
   - Click "locate" in the notification to find the APK

### Method 2: Command Line (Gradle)

#### Build Debug APK
```bash
cd zbuntu
./gradlew assembleDebug
```

#### Build Release APK
```bash
./gradlew assembleRelease
```

#### Clean Build
```bash
./gradlew clean
./gradlew assembleDebug
```

#### Build All Variants
```bash
./gradlew build
```

### Output Locations

- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release-unsigned.apk`

## Running the Application

### On Emulator

1. **Create AVD (Android Virtual Device)**
   - Click: `Tools → Device Manager`
   - Click: "Create Device"
   - Select device (e.g., Pixel 6)
   - Select system image (API 34, arm64-v8a recommended)
   - Click "Finish"

2. **Run Application**
   - Select emulator from device dropdown
   - Click: `Run → Run 'app'`
   - Or press: `Shift+F10`

### On Physical Device

1. **Enable Developer Options**
   - Settings → About Phone
   - Tap "Build Number" 7 times

2. **Enable USB Debugging**
   - Settings → Developer Options
   - Enable "USB Debugging"

3. **Connect Device**
   - Connect via USB cable
   - Accept debugging authorization on device

4. **Run Application**
   - Select device from device dropdown
   - Click: `Run → Run 'app'`
   - Or press: `Shift+F10`

### Command Line Installation

```bash
# Install debug APK
./gradlew installDebug

# Install and run
./gradlew installDebug
adb shell am start -n com.zbuntu.terminal/.MainActivity
```

## Build Configuration

### Gradle Properties

Edit `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true

# Optional: Enable parallel builds
org.gradle.parallel=true

# Optional: Enable build cache
org.gradle.caching=true
```

### NDK Configuration

Edit `app/build.gradle`:
```groovy
android {
    defaultConfig {
        ndk {
            // Build for specific ABIs to reduce APK size
            abiFilters 'arm64-v8a', 'armeabi-v7a'
        }
    }
}
```

### CMake Options

Edit `app/build.gradle`:
```groovy
android {
    defaultConfig {
        externalNativeBuild {
            cmake {
                // Compiler flags
                cppFlags "-std=c++17", "-frtti", "-fexceptions"
                
                // Arguments
                arguments "-DANDROID_STL=c++_shared"
            }
        }
    }
}
```

## Troubleshooting

### Issue: Gradle Sync Failed

**Solution:**
```bash
# Clear Gradle cache
./gradlew clean

# Delete .gradle directory
rm -rf .gradle/

# Sync again in Android Studio
```

### Issue: NDK Not Found

**Solution:**
1. Open SDK Manager
2. Install NDK (Side by side)
3. Edit `local.properties`:
   ```properties
   ndk.dir=/path/to/Android/sdk/ndk/25.x.x
   ```

### Issue: CMake Error

**Solution:**
1. Install CMake via SDK Manager
2. Specify version in `app/build.gradle`:
   ```groovy
   externalNativeBuild {
       cmake {
           version '3.22.1'
       }
   }
   ```

### Issue: Java Version Mismatch

**Solution:**
```bash
# Check Java version
java -version

# Set JAVA_HOME
export JAVA_HOME=/path/to/jdk-11

# In Android Studio:
# File → Settings → Build Tools → Gradle → Gradle JDK
# Select JDK 11 or later
```

### Issue: Out of Memory

**Solution:**
Edit `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```

### Issue: Native Build Failed

**Solution:**
```bash
# Clean native build
./gradlew clean

# Rebuild native libraries
./gradlew externalNativeBuildDebug

# Check CMakeLists.txt for errors
```

## Advanced Build Options

### Build Variants

Create different build types in `app/build.gradle`:
```groovy
android {
    buildTypes {
        debug {
            debuggable true
            minifyEnabled false
        }
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
        staging {
            debuggable true
            minifyEnabled false
        }
    }
}
```

### Product Flavors

Create different app versions:
```groovy
android {
    flavorDimensions "version"
    productFlavors {
        free {
            dimension "version"
            applicationIdSuffix ".free"
            versionNameSuffix "-free"
        }
        paid {
            dimension "version"
            applicationIdSuffix ".paid"
            versionNameSuffix "-paid"
        }
    }
}
```

### Signing Configuration

For release builds:

1. Generate keystore:
   ```bash
   keytool -genkey -v -keystore release.keystore -alias zbuntu -keyalg RSA -keysize 2048 -validity 10000
   ```

2. Configure in `app/build.gradle`:
   ```groovy
   android {
       signingConfigs {
           release {
               storeFile file('release.keystore')
               storePassword 'your_password'
               keyAlias 'zbuntu'
               keyPassword 'your_password'
           }
       }
       buildTypes {
           release {
               signingConfig signingConfigs.release
           }
       }
   }
   ```

## Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumented Tests
```bash
# On connected device
./gradlew connectedAndroidTest
```

### Lint Checks
```bash
./gradlew lint
```

## Performance Optimization

### Enable R8
```groovy
android {
    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true
        }
    }
}
```

### Use App Bundle
```bash
./gradlew bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`

### Optimize Native Code
```groovy
externalNativeBuild {
    cmake {
        cppFlags "-O3", "-DNDEBUG"
    }
}
```

## Continuous Integration

### GitHub Actions Example

Create `.github/workflows/build.yml`:
```yaml
name: Android CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build with Gradle
      run: ./gradlew assembleDebug
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

## Next Steps

After successful build:
1. Test the application thoroughly
2. Read the [ARCHITECTURE.md](ARCHITECTURE.md) for code structure
3. Check the [README.md](README.md) for usage instructions
4. Report issues on GitHub

## Resources

- [Android Developer Guide](https://developer.android.com/guide)
- [Gradle User Guide](https://docs.gradle.org/)
- [CMake Documentation](https://cmake.org/documentation/)
- [NDK Guide](https://developer.android.com/ndk/guides)

## Support

For build issues:
- Check the [Troubleshooting](#troubleshooting) section
- Search existing GitHub issues
- Create a new issue with:
  - Build command used
  - Full error message
  - System information (OS, Android Studio version)
  - Gradle version
