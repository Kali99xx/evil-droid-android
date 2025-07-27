#!/bin/bash

# Evil-Droid Android Build Script
# This script helps build and install the Evil-Droid Android application

echo "======================================"
echo "Evil-Droid Android Build Script"
echo "======================================"

# Check if Android SDK is available
if ! command -v adb &> /dev/null; then
    echo "❌ Android SDK not found. Please install Android SDK and add it to PATH."
    exit 1
fi

# Check if Gradle wrapper exists
if [ ! -f "./gradlew" ]; then
    echo "❌ Gradle wrapper not found. Please run this script from the project root directory."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

echo "🔧 Building Evil-Droid Android..."

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Build debug APK
echo "🏗️  Building debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    
    # Check if device is connected
    DEVICE_COUNT=$(adb devices | grep -v "List of devices" | grep -c "device")
    
    if [ $DEVICE_COUNT -gt 0 ]; then
        echo "📱 Android device detected. Installing APK..."
        
        # Install the APK
        adb install -r app/build/outputs/apk/debug/app-debug.apk
        
        if [ $? -eq 0 ]; then
            echo "✅ APK installed successfully!"
            echo "🚀 You can now launch Evil-Droid Android from your device."
        else
            echo "❌ APK installation failed. Please install manually."
            echo "📁 APK location: app/build/outputs/apk/debug/app-debug.apk"
        fi
    else
        echo "📱 No Android device detected."
        echo "📁 APK built successfully at: app/build/outputs/apk/debug/app-debug.apk"
        echo "📋 To install manually:"
        echo "   1. Transfer the APK to your Android device"
        echo "   2. Enable 'Install from Unknown Sources' in device settings"
        echo "   3. Install the APK"
    fi
else
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi

echo ""
echo "======================================"
echo "Build process completed!"
echo "======================================"
echo ""
echo "📋 Next steps:"
echo "   1. Ensure your device is rooted"
echo "   2. Grant root permissions when prompted"
echo "   3. Allow storage permissions"
echo "   4. Start using Evil-Droid Android!"
echo ""
echo "⚠️  Security Notice:"
echo "   This app is for educational and authorized testing only."
echo "   Use responsibly and only on devices you own or have permission to test."
