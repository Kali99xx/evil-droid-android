# Evil-Droid Android

A native Android application converted from the Evil-Droid Linux GUI penetration testing framework. This app provides automated exploitation capabilities for Android platforms, including APK installation, payload execution, and device management.

## Features

- **Root Access Verification**: Checks and verifies root privileges on the device
- **Automated APK Installation**: Install APK files via root or system installer
- **Payload Execution**: Generate and execute payloads for target applications
- **Real-time Logging**: Color-coded log output with timestamps
- **Device Information**: Display device details and system information
- **Modern UI**: Clean, minimalist interface using Material Design

## Prerequisites

- **Rooted Android Device**: Root access is required for full functionality
- **Android 7.0 (API 24) or higher**
- **Storage Permissions**: Required for APK file access
- **Developer Options**: USB debugging enabled (optional)

## Installation

### Method 1: Build from Source

1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd evil-droid-android
   ```

2. **Open in Android Studio**:
   - Open Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the project directory and select it

3. **Build the Project**:
   - Wait for Gradle sync to complete
   - Build → Make Project (Ctrl+F9)
   - Build → Build Bundle(s) / APK(s) → Build APK(s)

4. **Install on Device**:
   - Enable "Unknown Sources" in device settings
   - Transfer the APK to your device
   - Install the APK

### Method 2: Direct APK Installation

1. Download the pre-built APK from releases
2. Enable "Install from Unknown Sources" in device settings
3. Install the APK on your rooted Android device

## Usage

### Initial Setup

1. **Launch the App**: Open Evil-Droid Android from your app drawer
2. **Grant Permissions**: Allow storage permissions when prompted
3. **Check Root Access**: Tap "Check Root Access" to verify root privileges

### Basic Operations

#### 1. Check Root Access
- Tap the "Check Root Access" button
- The app will verify if your device has root privileges
- Device information will be displayed in the log

#### 2. Install APK
- Enter the full path to your APK file in the "APK File Path" field
- Or tap "Browse APK" to select a file (if file manager supports it)
- Tap "Install APK" to begin installation
- The app will attempt root installation first, then fallback to system installer

#### 3. Execute Payload
- Enter the target package name (e.g., `com.whatsapp`)
- Optionally specify an APK file path
- Tap "Run Payload" to execute the payload
- Monitor the log output for execution status

#### 4. Log Management
- All operations are logged with timestamps and color coding
- Green: Success messages
- Red: Error messages
- Blue: Information messages
- Orange: Warning messages
- Tap "Clear Log" to clear the log output

## Technical Details

### Architecture

- **MainActivity.kt**: Main UI controller and user interaction handler
- **ShellExecutor.kt**: Utility class for executing shell commands with root privileges
- **Modern UI**: Material Design components with dark theme support

### Key Components

#### ShellExecutor Utility
- `executeCommand()`: Execute standard shell commands
- `executeRootCommand()`: Execute commands with root privileges
- `checkRootAccess()`: Verify root availability
- `executePayload()`: Generate and execute payloads
- `installApkRoot()`: Install APK using package manager
- `getPackageUid()`: Retrieve package UID information

#### Security Features
- Root privilege verification
- Command execution sandboxing
- Error handling and logging
- Permission management

### Supported Operations

1. **APK Installation**:
   - Root-based installation using `pm install`
   - Fallback to system installer via Intent
   - File validation and error handling

2. **Payload Execution**:
   - Package UID extraction
   - Automated payload generation
   - Target application manipulation
   - Real-time execution monitoring

3. **Device Management**:
   - Root access verification
   - Device information retrieval
   - Package listing and management
   - ADB connection status

## Permissions

The app requires the following permissions:

- `INTERNET`: For potential network operations
- `READ_EXTERNAL_STORAGE`: To access APK files
- `WRITE_EXTERNAL_STORAGE`: For file operations
- `REQUEST_INSTALL_PACKAGES`: For APK installation
- `MANAGE_EXTERNAL_STORAGE`: For Android 11+ file access

## Security Considerations

⚠️ **Important Security Notice**:

- This app is designed for **educational and authorized penetration testing purposes only**
- Only use on devices you own or have explicit permission to test
- Root access provides elevated privileges - use responsibly
- The app can install and execute arbitrary code - ensure APK files are from trusted sources
- Monitor all operations through the built-in logging system

## Troubleshooting

### Common Issues

1. **Root Access Denied**:
   - Ensure your device is properly rooted
   - Grant root permissions when prompted by your root manager (SuperSU, Magisk, etc.)
   - Check if root access is enabled in your root manager settings

2. **APK Installation Failed**:
   - Verify the APK file path is correct
   - Ensure the APK file exists and is readable
   - Check if "Install from Unknown Sources" is enabled
   - Try using the system installer as fallback

3. **Payload Execution Failed**:
   - Verify the target package name is correct
   - Ensure the target application is installed
   - Check root permissions are granted
   - Review log output for specific error messages

4. **File Access Issues**:
   - Grant all requested storage permissions
   - For Android 11+, enable "All files access" in app settings
   - Ensure APK files are in accessible storage locations

### Log Analysis

Monitor the color-coded log output:
- **Green messages**: Operations completed successfully
- **Red messages**: Errors that need attention
- **Blue messages**: Informational status updates
- **Orange messages**: Warnings about potential issues

## Development

### Building from Source

1. **Requirements**:
   - Android Studio Arctic Fox or newer
   - Android SDK 34
   - Kotlin 1.9.10+
   - Gradle 8.2+

2. **Dependencies**:
   - AndroidX libraries
   - Material Design Components
   - Kotlin Coroutines
   - ViewBinding

3. **Build Commands**:
   ```bash
   ./gradlew assembleDebug      # Build debug APK
   ./gradlew assembleRelease    # Build release APK
   ./gradlew installDebug       # Install debug version
   ```

### Customization

The app can be customized by modifying:
- **Payload Logic**: Update `ShellExecutor.executePayload()` method
- **UI Theme**: Modify colors and themes in `res/values/`
- **Commands**: Add new shell commands in `ShellExecutor` utility
- **Logging**: Enhance log formatting in `MainActivity.appendLog()`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly on rooted devices
5. Submit a pull request

## License

This project is for educational and authorized testing purposes only. Users are responsible for complying with all applicable laws and regulations.

## Disclaimer

This tool is provided for educational and authorized penetration testing purposes only. The developers are not responsible for any misuse or damage caused by this application. Always ensure you have proper authorization before testing on any device or network.

## Support

For issues, questions, or contributions:
- Create an issue in the repository
- Review the troubleshooting section
- Check device compatibility and root status

---

**Remember**: Always use this tool responsibly and only on systems you own or have explicit permission to test.
