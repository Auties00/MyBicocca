# BicoccApp Security Analysis & Bypass

## Abstract

This file documents the reverse engineering and security analysis of BicoccApp's client-side security mechanisms. 
Through static and dynamic analysis, we identified and successfully bypassed the application's root detection and SSL certificate pinning implementations using Frida instrumentation.

## Table of Contents

- [Analysis](#analysis)
- [Threat Model](#threat-model)
- [Research Methodology](#research-methodology)
- [Findings](#findings)
- [Usage](#usage)
- [References](#references)

## Analysis

### Static Analysis

**Tool Used**: [JADX](https://github.com/skylot/jadx) (Dex to Java Decompiler)

**Approach**:
1. Extracted APK structure and analyzed `AndroidManifest.xml`
2. Decompiled DEX bytecode to Java source
3. Analyzed native libraries (`libapp.so`, `libflutter.so`)
4. Extracted string tables from native binaries

We discovered that the app is developed using Flutter, a multiplatform framework developed by Google.

### Dependency Analysis

**Method**: String extraction from `libapp.so` using `strings` command

```bash
strings libapp.so | grep -i "package:"
```

**Identified Security Dependencies**:

| Package                                                                             | Purpose                                    |
|-------------------------------------------------------------------------------------|--------------------------------------------|
| [talsec/freerasp](https://www.talsec.app/)                                          | Runtime Application Self-Protection (RASP) |
| [flutter_jailbreak_detection](https://pub.dev/packages/flutter_jailbreak_detection) | Root/Jailbreak detection                   |
| [http_certificate_pinning](https://pub.dev/packages/http_certificate_pinning)       | SSL Certificate Pinning                    |

## Threat Model

### Attack Surface

The application implements multiple layers of client-side security:

1. **Root Detection**: Prevents execution on rooted devices
2. **SSL Certificate Pinning**: Prevents MITM attacks and traffic inspection
3. **Integrity Checks**: Runtime verification of application integrity

### Attack Objective

Bypass client-side security controls to enable:
- HTTP/HTTPS traffic interception using proxy tools (HTTPToolkit, Burp Suite)
- Dynamic analysis on rooted Android devices
- Security research and vulnerability assessment

## Research Methodology

### Phase 1: Dynamic Channel Analysis

**Objective**: Understand Flutter's communication patterns between Dart and native layers.

**Tool**: `research.js` - Comprehensive Flutter channel monitor

**Approach**:
1. Hook all `MethodChannel` and `EventChannel` initialization points
2. Intercept channel handler registration
3. Monitor bidirectional data flow:
    - Flutter → Java: Method calls and arguments
    - Java → Flutter: Return values and events
4. Serialize and log all communications

**Key Observations**:

```
Channel: talsec.app/freerasp/methods
Type: MethodChannel
Direction: Flutter → Java
Method: start
Arguments: null

Channel: talsec.app/freerasp/events  
Type: EventChannel
Direction: Java → Flutter
Event: {"threatType": "root", "detected": true}

Channel: flutter_jailbreak_detection
Type: MethodChannel
Direction: Flutter → Java
Method: jailbroken
Return: true

Channel: http_certificate_pinning
Type: MethodChannel
Direction: Flutter → Java
Method: check
Return: "CONNECTION_NOT_SECURE"
```

### Phase 2: Security Control Analysis

**Findings**:

1. **Talsec/Freerasp**:
    - Uses `EventChannel` for streaming threat notifications
    - Implements native security checks (JNI)
    - Sends JSON-serialized threat events to Flutter layer

2. **flutter_jailbreak_detection**:
    - Uses `MethodChannel` for synchronous checks
    - Methods: `jailbroken()`, `developerMode()`, `canMockLocation()`
    - Returns boolean values

3. **http_certificate_pinning**:
    - Uses `MethodChannel` for certificate validation
    - Method: `check()`
    - Returns: `"CONNECTION_SECURE"` or `"CONNECTION_NOT_SECURE"`

## Findings

### Security Architecture

```
┌─────────────────────────────────────────┐
│         Flutter (Dart Layer)            │
│  ┌─────────────────────────────────┐    │
│  │   Application Business Logic    │    │
│  └─────────────────────────────────┘    │
│              ▲         ▲                │
│              │         │                │
│      MethodChannel  EventChannel        │
│              │         │                │
└──────────────┼─────────┼────────────────┘
               │         │
               ▼         ▼
┌─────────────────────────────────────────┐
│      Native Android (Java/Kotlin)       │
│  ┌─────────────┐  ┌─────────────────┐   │
│  │   Talsec    │  │  Root Detection │   │
│  │  (Native)   │  │   SSL Pinning   │   │
│  └─────────────┘  └─────────────────┘   │
└─────────────────────────────────────────┘
```

### Attack Vectors

**1. EventChannel Interception** (Talsec)
- Intercept `setStreamHandler()` method
- Wrap `EventSink` with proxy that drops threat events
- Result: Threat notifications never reach Flutter layer

**2. MethodChannel Response Manipulation** (Jailbreak Detection)
- Intercept `onMethodCall()` handler
- Override `Result.success()` to return `false`
- Result: All root detection checks return negative

**3. MethodChannel Response Manipulation** (SSL Pinning)
- Intercept `onMethodCall()` handler for `check()` method
- Override `Result.success()` to return `"CONNECTION_SECURE"`
- Result: All certificate validations pass

## Usage

### Prerequisites

- Rooted Android device
- Magisk with Zygisk enabled
- Frida server installed

### Tutorial

```bash
# Start Frida server
adb shell su -c /data/local/tmp/frida-server &

# Monitor all channels (research mode)
frida -U -l research.js -f it.bicoccapp.unimib

# Apply bypass
frida -U -l poc.js -f it.bicoccapp.unimib
```

## References

### Documentation

- [Flutter Platform Channels](https://docs.flutter.dev/development/platform-integration/platform-channels)
- [Frida JavaScript API](https://frida.re/docs/javascript-api/)

### Security Research

- [Talsec Documentation](https://www.talsec.app/)
- [Flutter Security Best Practices](https://docs.flutter.dev/security)
- [Android SSL Pinning Bypass Techniques](https://blog.netspi.com/four-ways-bypass-android-ssl-verification-certificate-pinning/)

### Tools

- [JADX - Dex to Java Decompiler](https://github.com/skylot/jadx)
- [Frida - Dynamic Instrumentation Toolkit](https://frida.re/)
- [HTTPToolkit - HTTP(S) Debugging Proxy](https://httptoolkit.tech/)