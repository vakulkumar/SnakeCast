# SnakeCast 🐍📺

A multi-device Android game where a Snake game runs on Android TV and is controlled by a companion Android mobile app in real-time.

## Architecture

```
┌─────────────────────┐          ┌─────────────────────┐
│   Android TV App    │◄────────►│  Mobile Controller  │
│     (Host)          │   Wi-Fi  │      (Client)       │
├─────────────────────┤   TCP    ├─────────────────────┤
│ • Snake Game Logic  │          │ • D-Pad Controls    │
│ • Game UI (Compose) │          │ • Motion Controls   │
│ • TCP Server        │          │ • TCP Client        │
│ • NSD Registration  │          │ • NSD Discovery     │
└─────────────────────┘          └─────────────────────┘
```

## Modules

| Module | Description |
|--------|-------------|
| `shared` | Common networking code (NSD, Sockets, Protocol) |
| `tv-app` | Android TV host with game logic and UI |
| `mobile-app` | Mobile controller with D-Pad and motion controls |

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose (TV uses `androidx.tv` libraries)
- **Concurrency**: Kotlin Coroutines & Flow
- **Networking**: NSD (discovery) + Raw TCP Sockets (control)

## Building

```bash
# Clone and navigate to project
cd SnakeCast

# Build all modules
./gradlew assembleDebug

# TV app APK
tv-app/build/outputs/apk/debug/tv-app-debug.apk

# Mobile app APK
mobile-app/build/outputs/apk/debug/mobile-app-debug.apk
```

## Running

1. **Install TV App** on Android TV or TV emulator
2. **Install Mobile App** on Android phone
3. **Connect both devices** to the same Wi-Fi network
4. **Launch TV App** – displays "Waiting for Controller"
5. **Launch Mobile App** – scans for TV automatically
6. **Tap on TV** in device list to connect
7. **Start playing!** Use D-Pad or toggle to Motion controls

## Control Modes

### 🎮 D-Pad Mode
- Tap directional buttons on-screen
- Haptic feedback on each press

### 📱 Motion Mode  
- Tilt phone to control direction
- 15° tilt threshold with 100ms debounce
- Visual indicator shows active direction

## Game Configuration

| Parameter | Value |
|-----------|-------|
| Grid Size | 20x20 |
| Initial Speed | 150ms/tick |
| Min Speed | 80ms/tick |
| Speed Increase | Every 5 foods |

## Network Protocol

- **Service Type**: `_snakecast._tcp.`
- **Message Format**: Single byte per command
  - `0x01` = UP
  - `0x02` = DOWN
  - `0x03` = LEFT
  - `0x04` = RIGHT

## Project Structure

```
SnakeCast/
├── shared/src/main/java/com/snakecast/shared/
│   ├── Command.kt         # Direction enum, protocol
│   ├── NsdHelper.kt       # NSD registration/discovery
│   ├── SocketServer.kt    # TCP server (TV)
│   └── SocketClient.kt    # TCP client (Mobile)
├── tv-app/src/main/java/com/snakecast/tv/
│   ├── game/
│   │   ├── GameState.kt   # Game data classes
│   │   └── GameViewModel.kt   # Game loop logic
│   ├── server/
│   │   └── GameServer.kt  # NSD + Socket wrapper
│   └── ui/screens/
│       └── GameScreen.kt  # Game board UI
└── mobile-app/src/main/java/com/snakecast/mobile/
    ├── connection/
    │   ├── ConnectionViewModel.kt
    │   └── ConnectionScreen.kt
    ├── controller/
    │   ├── InputManager.kt
    │   ├── MotionSensorManager.kt
    │   └── ControllerScreen.kt
    └── ui/components/
        ├── DPadView.kt
        └── SettingsPanel.kt
```

## License

MIT License
