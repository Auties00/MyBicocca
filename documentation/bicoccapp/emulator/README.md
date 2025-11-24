# Android Emulator

A rooted Android emulator setup with Magisk, LSPosed, and bypass module for BicoccApp. 
Can be run either in Docker or locally using the automated setup script.

## What the Script Does

The setup script automatically:

1. **System Detection**: Detects your OS and architecture
2. **Dependency Check**: Verifies Java, curl, and unzip are installed
3. **Android SDK Installation**: Downloads and installs Android SDK tools
4. **SDK Components**: Installs platform-tools, emulator, and system images
5. **AVD Creation**: Creates a Pixel 7 Pro API 33 virtual device
6. **Emulator Launch**: Starts the emulator
7. **Root Access**: Patches the emulator with Magisk using rootAVD
8. **Magisk Configuration**: Enables Zygisk and configures root access
9. **LSPosed Installation**: Installs LSPosed framework as a Magisk module
10. **BicoccApp Installation**: Installs BicoccApp split APKs
11. **Bypass Module Setup**: Installs and configures the bypass module for BicoccApp

## Requirements

### Docker Method

- docker

### Local Method

- curl
- unzip
- Java 17+

## Setup Methods

### Method 1: Docker

Docker provides an isolated, reproducible environment.

#### Hardware Acceleration Setup (Linux only)

For optimal performance on Linux, enable KVM hardware acceleration:

```bash
# Verify KVM is available
ls -la /dev/kvm

# If not present, enable KVM modules
sudo modprobe kvm
sudo modprobe kvm_intel  # or kvm_amd for AMD processors

# Add your user to the kvm group
sudo usermod -aG kvm $USER

# Log out and back in for group changes to take effect
```

#### Running with Docker

```bash
# Build and start the emulator
docker compose up --build -d android-emulator

# View logs
docker compose logs -f android-emulator

# Stop the emulator
docker compose down
```

#### Accessing the Emulator UI

The Docker setup includes VNC and noVNC for viewing the emulator screen:

- **noVNC (Web)**: Open `http://localhost:6080` in your browser
- **VNC**: Connect to `localhost:5900` with password `android`

#### Docker Exposed Ports

| Port | Description            |
|------|------------------------|
| 5037 | ADB Server             |
| 5554 | Emulator Console       |
| 5555 | ADB Connection         |
| 5900 | VNC Server             |
| 6080 | noVNC Web Interface    |

### Method 2: Local Setup

The automated setup script handles the complete installation and configuration process.

#### Running the Setup Script

**Linux/macOS:**
```bash
chmod +x setup.sh
./setup.sh
```

**Windows (Git Bash):**
```bash
bash setup.sh
```