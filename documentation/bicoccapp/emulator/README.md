# Android Emulator Docker

A Docker image for running a rooted Android emulator with Magisk, LSPosed and bypass module for BicoccApp.

## Requirements

- Docker 20.10+
- At least 8GB RAM available
- At least 20GB disk space

## Setup

### Hardware Acceleration

- Linux: recommended environment because it natively supports KVM
- Windows: Windows Subsystem for Linux is recommended
- MacOS: No hardware acceleration is available

To enable hardware acceleration:

```bash
# Verify KVM is available
ls -la /dev/kvm

# If not present, enable KVM
sudo modprobe kvm
sudo modprobe kvm_intel  # or kvm_amd for AMD processors

# Add your user to kvm group
sudo usermod -aG kvm $USER
```

### Run

```
docker compose up --build -d android-emulator
```

## Connecting from Host

### ADB Connection

```bash
# Connect to the emulator
adb connect localhost:5555

# Verify connection
adb devices
# Output:
# List of devices attached
# localhost:5555    device

# Use ADB normally
adb shell
adb install myapp.apk
```

## Exposed Ports

| Port | Description      |
|------|------------------|
| 5037 | ADB Server       |
| 5554 | Emulator Console |
| 5555 | ADB Connection   |