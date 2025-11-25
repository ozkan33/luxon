# LUXON - Smart Light Assistant

An Android application that measures ambient light using the device's light sensor and provides real-time feedback about whether the lighting conditions are ideal for work.

## Features

- **Real-time Light Measurement**: Uses the device's built-in light sensor to measure ambient light in lux
- **Visual Feedback**: Circular gauge showing current light level with color-coded status
- **Status Messages**: 
  - Green: Ideal lighting (300-600 lux)
  - Red: Insufficient lighting (<150 lux)
  - Yellow: Too bright (>600 lux)
- **Recommendations**: Contextual tips based on current light levels
- **Information**: Educational content about ideal lighting conditions

## Design

The app follows the LUXON design system with:
- Color scheme: Golden Yellow (#D4AF63), Reddish Orange (#C56A67), Green (#548D6F)
- Modern Material Design 3 UI
- Turkish language support

## Requirements

- Android 7.0 (API level 24) or higher
- Device with light sensor

## Building

1. Open the project in Android Studio
2. Sync Gradle files
3. Build and run on a device or emulator

## Usage

1. Launch the app
2. The app will automatically start measuring ambient light
3. View the circular gauge to see current light levels
4. Read the status message for feedback
5. Use the buttons to:
   - Adjust desk position (future feature)
   - View recommendations
   - Read information about lighting
   - Exit the app

## Technical Details

- Built with Kotlin and Jetpack Compose
- Uses Android SensorManager for light sensor access
- Material Design 3 components
- Clean architecture with separation of concerns

