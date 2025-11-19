# Android Emulator

An automated installer for setting up and running an Android emulator environment with Frida, LSPosed, and BicoccApp for reverse engineering.

## Installation Options

### Option 1: Using Precompiled Binaries (Recommended)

Precompiled binaries are available in the `bin/` directory for all platforms:

- **Linux**: `bin/emulator-linux-amd64` (x86) or `bin/emulator-linux-arm64` (ARM)
- **macOS**: `bin/emulator-darwin-amd64` (Intel) or `bin/emulator-darwin-arm64` (Apple Silicon)
- **Windows**: `bin/emulator-windows-amd64.exe` (x86) or `bin/emulator-windows-arm64.exe` (ARM)

### Option 2: Building from Source

#### Prerequisites

- Go 1.25.4 or later
- Make

#### Building

```bash
# Build for your current platform
make build

# Build for all platforms
make build-all
```