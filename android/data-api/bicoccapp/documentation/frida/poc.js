/**
 * BicoccApp Root Detection & SSL Pinning Bypass
 *
 * CONTEXT:
 * To analyze the HTTP requests sent by BicoccApp we can use a MiM Proxy like HTTPToolkit.
 * Newer versions of Android, though, block all network traffic when using a MiM proxy through SSL pinning.
 * By dumping the packages used in BicoccApp's APK, we discovered that the package used to implement SSL pinning is http_certificate_pinning.
 * We can disable the check using Frida on a rooted device, but BicoccApp refuses to run on modified devices.
 * BicoccApp uses Talsec (https://www.talsec.app/) and the flutter_jailbreak_detection Flutter plugin for these security checks.
 * This was discovered by decompiling the app using Jadx.
 *
 * SOLUTION:
 * This Frida script bypasses root detection and SSL pinning by:
 * - Intercepting and blocking the Talsec EventChannel that streams threat notifications to Flutter
 * - Intercepting flutter_jailbreak_detection and returning false for all checks
 * - Intercepting http_certificate_pinning and returning CONNECTION_SECURE for all checks
 *
 * USAGE:
 * frida -U -l "poc.js" -f it.bicoccapp.unimib
 *
 * NOTE:
 * Use research.js to monitor all channel traffic
 */

const Config = {
    classes: {
        String: 'java.lang.String',
        ByteBuffer: 'java.nio.ByteBuffer',
        FlutterJNI: 'io.flutter.embedding.engine.FlutterJNI',
    },
    channels: {
        talsec: ['talsec', 'freerasp'],
        jailbreak: ['flutter_jailbreak_detection'],
        sslPinning: ['http_certificate_pinning']
    }
}

function isTalsecChannel(channelName) {
    if (!channelName) return false
    return Config.channels.talsec.some(pattern => channelName.toLowerCase().includes(pattern))
}

function isJailbreakChannel(channelName) {
    if (!channelName) return false
    return Config.channels.jailbreak.some(pattern => channelName.toLowerCase().includes(pattern))
}

function isSslPinningChannel(channelName) {
    if (!channelName) return false
    return Config.channels.sslPinning.some(pattern => channelName.toLowerCase().includes(pattern))
}

// Builds a success envelope that contains the boolean value false
// Reversed from https://github.com/flutter/flutter/blob/b3298ebef4c3cc8709e1a837ebe78fcabf294a8e/engine/src/flutter/shell/platform/android/io/flutter/plugin/common/StandardMethodCodec.java#L58
function createBooleanFalseResponse() {
    const ByteBuffer = Java.use(Config.classes.ByteBuffer)

    const buffer = ByteBuffer.allocateDirect(2)

    buffer.put(0x00) // Success envelope
    buffer.put(0x02) // Boolean FALSE

    buffer.flip()

    return buffer
}

// Builds a success envelope that contains the string value "CONNECTION_SECURE"
// Reversed from https://github.com/flutter/flutter/blob/b3298ebef4c3cc8709e1a837ebe78fcabf294a8e/engine/src/flutter/shell/platform/android/io/flutter/plugin/common/StandardMethodCodec.java#L58
function createConnectionSecureResponse() {
    const ByteBuffer = Java.use(Config.classes.ByteBuffer)
    const String = Java.use(Config.classes.String)

    const strBytes = String.$new('CONNECTION_SECURE').getBytes()
    const length = strBytes.length
    const sizeBytes = length < 254 ? 1 : (length <= 0xffff ? 3 : 5)

    const buffer = ByteBuffer.allocateDirect(2 + sizeBytes + length)

    buffer.put(0x00) // Success envelope
    buffer.put(0x07) // STRING type code

    // Write size
    if (length < 254) {
        buffer.put(length)
    } else if (length <= 0xffff) {
        buffer.put(254)
        buffer.put(length & 0xFF)
        buffer.put((length >> 8) & 0xFF)
    } else {
        buffer.put(255)
        buffer.put(length & 0xFF)
        buffer.put((length >> 8) & 0xFF)
        buffer.put((length >> 16) & 0xFF)
        buffer.put((length >> 24) & 0xFF)
    }

    buffer.put(strBytes)

    buffer.flip()

    return buffer
}


function hookMethodChannel() {
    try {
        const FlutterJNI = Java.use(Config.classes.FlutterJNI)
        const originalHandlePlatformMessage = FlutterJNI.handlePlatformMessage
        FlutterJNI.handlePlatformMessage.implementation = function (channelName, byteBuffer, replyId, messageData) {
            if (isJailbreakChannel(channelName)) {
                console.log("[OVERRIDE] MethodChannel call on " + channelName + " -> false")
                const response = createBooleanFalseResponse()
                const responseSize = response.remaining()
                this.invokePlatformMessageResponseCallback(replyId, response, responseSize)
                if (messageData !== 0) {
                    this.cleanupMessageData(messageData)
                }
            } else if (isSslPinningChannel(channelName)) {
                console.log("[OVERRIDE] MethodChannel call on " + channelName + " -> CONNECTION_SECURE")
                const response = createConnectionSecureResponse()
                const responseSize = response.remaining()
                this.invokePlatformMessageResponseCallback(replyId, response, responseSize)
                if (messageData !== 0) {
                    this.cleanupMessageData(messageData)
                }
            } else {
                return originalHandlePlatformMessage.call(this, channelName, byteBuffer, replyId, messageData)
            }
        }
        console.log("[✓] MethodChannel hooked")
    } catch (e) {
        console.log("[✗] MethodChannel hooking failed: " + e.message)
    }
}

function hookEventStream() {
    try {
        const FlutterJNI = Java.use(Config.classes.FlutterJNI)
        const originalDispatchPlatformMessage = FlutterJNI.dispatchPlatformMessage
        FlutterJNI.dispatchPlatformMessage.implementation = function (channelName, byteBuffer, position, responseId) {
            if (isTalsecChannel(channelName)) {
                console.log("[BLOCKED] EventStream event from " + channelName)
            }else {
                return originalDispatchPlatformMessage.call(this, channelName, byteBuffer, position, responseId)
            }
        }
        console.log("[✓] EventChannel hooked")
    } catch (e) {
        console.log("[✗] EventChannel hooking failed: " + e.message)
    }
}

Java.perform(function() {
    console.log("[*] BicoccApp PoC - Starting...")
    hookMethodChannel()
    hookEventStream()
})