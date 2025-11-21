/**
 * Flutter Channel Research Monitor
 *
 * PURPOSE:
 * This script monitors ALL Flutter channel communications in the app.
 * It logs method calls, arguments, return values, and event streams.
 * Use this to understand how the app works and detect changes in detection mechanisms.
 *
 * MONITORING STRATEGY:
 * - Attempts high-level hooks (MethodChannel, EventChannel, BasicMessageChannel)
 * - Always installs FlutterJNI fallback for obfuscated apps
 *
 * USAGE:
 * frida -U -l "research.js" -f it.bicoccapp.unimib
 *
 * NOTE:
 * This is a pure monitoring script - it does NOT bypass or modify any security checks.
 */

const Config = {
    classes: {
        MethodChannel: 'io.flutter.plugin.common.MethodChannel',
        MethodChannelCallHandler: 'io.flutter.plugin.common.MethodChannel$MethodCallHandler',
        MethodChannelResult: 'io.flutter.plugin.common.MethodChannel$Result',
        EventChannel: 'io.flutter.plugin.common.EventChannel',
        EventChannelStreamHandler: 'io.flutter.plugin.common.EventChannel$StreamHandler',
        EventChannelEventSink: 'io.flutter.plugin.common.EventChannel$EventSink',
        BasicMessageChannel: 'io.flutter.plugin.common.BasicMessageChannel',
        FlutterJNI: 'io.flutter.embedding.engine.FlutterJNI',
        String: 'java.lang.String',
        Object: 'java.lang.Object',
        ByteBuffer: 'java.nio.ByteBuffer'
    }
}

function shouldMonitor(channelName) {
    return channelName != null
}

function generateRandomClassName(prefix) {
    return 'com.frida.' + prefix + Math.random().toString(36).substring(2, 9)
}

function serializeValue(value) {
    if (value === null || value === undefined) return null

    const type = typeof value
    if (type === "string" || type === "number" || type === "boolean") return value

    // Handle Java objects
    if (value.$className) {
        try {
            // Java Map → JavaScript object
            if (value.$className.includes("Map")) {
                const map = {}
                const entries = value.entrySet().iterator()
                while (entries.hasNext()) {
                    const entry = entries.next()
                    map[entry.getKey().toString()] = serializeValue(entry.getValue())
                }
                return map
            }

            // Java List → JavaScript array
            if (value.$className.includes("List")) {
                const arr = []
                for (let i = 0; i < value.size(); i++) {
                    arr.push(serializeValue(value.get(i)))
                }
                return arr
            }

            // Other Java objects → type + string representation
            return {
                type: value.$className,
                value: value.toString()
            }
        } catch (e) {
            return "[Serialization error: " + e + "]"
        }
    }

    // Handle JavaScript arrays
    if (Array.isArray(value)) {
        return value.map(serializeValue)
    }

    // Handle plain JavaScript objects
    try {
        return JSON.parse(JSON.stringify(value))
    } catch (e) {
        return value.toString()
    }
}

function createResultWrapper(originalResult, channelName, methodName) {
    const retainedResult = Java.retain(originalResult)

    const ResultWrapper = Java.registerClass({
        name: generateRandomClassName('ResultMonitor'),
        implements: [Java.use(Config.classes.MethodChannelResult)],
        methods: {
            success: function (result) {
                console.log("┌─ RESPONSE ─────────────────────────────┐")
                console.log("│ Type:   SUCCESS")
                console.log("│ Value:  " + JSON.stringify(serializeValue(result), null, 2))
                console.log("└────────────────────────────────────────┘\n")

                retainedResult.success(result)
            },
            error: function (errorCode, errorMessage, errorDetails) {
                console.log("┌─ RESPONSE ─────────────────────────────┐")
                console.log("│ Type:    ERROR")
                console.log("│ Code:    " + errorCode)
                console.log("│ Message: " + errorMessage)
                console.log("│ Details: " + JSON.stringify(serializeValue(errorDetails), null, 2))
                console.log("└────────────────────────────────────────┘\n")

                retainedResult.error(errorCode, errorMessage, errorDetails)
            },
            notImplemented: function () {
                console.log("┌─ RESPONSE ─────────────────────────────┐")
                console.log("│ Type: NOT IMPLEMENTED")
                console.log("└────────────────────────────────────────┘\n")

                retainedResult.notImplemented()
            }
        }
    })

    return ResultWrapper.$new()
}

function hookMethodChannel() {
    try {
        const MethodChannel = Java.use(Config.classes.MethodChannel)

        // Hook: Flutter → Java (setMethodCallHandler)
        hookMethodCallHandler(MethodChannel)

        // Hook: Java → Flutter (invokeMethod)
        hookInvokeMethod(MethodChannel)

        console.log("[✓] MethodChannel monitoring installed")
        return true

    } catch (e) {
        console.log("[✗] MethodChannel monitoring failed: " + e.message)
        return false
    }
}

function hookMethodCallHandler(MethodChannel) {
    const setMethodCallHandler = MethodChannel.setMethodCallHandler.overload(Config.classes.MethodChannelCallHandler)

    setMethodCallHandler.implementation = function(handler) {
        const channelName = this.name.value

        if (shouldMonitor(channelName) && handler != null) {
            console.log("[HANDLER SET] MethodChannel: " + channelName)

            const retainedHandler = Java.retain(handler)

            const HandlerWrapper = Java.registerClass({
                name: generateRandomClassName('MethodMonitor'),
                implements: [Java.use(Config.classes.MethodChannelCallHandler)],
                methods: {
                    onMethodCall: function (call, result) {
                        const methodName = call.method.value
                        const args = call.arguments.value

                        console.log("\n╔════════════════════════════════════════╗")
                        console.log("║ FLUTTER → JAVA                         ║")
                        console.log("╚════════════════════════════════════════╝")
                        console.log("Channel:   " + channelName)
                        console.log("Method:    " + methodName)
                        console.log("Arguments: " + JSON.stringify(serializeValue(args), null, 2))
                        console.log("────────────────────────────────────────")

                        const wrappedResult = createResultWrapper(result, channelName, methodName)
                        retainedHandler.onMethodCall(call, wrappedResult)
                    }
                }
            })

            return setMethodCallHandler.call(this, HandlerWrapper.$new())
        }

        return setMethodCallHandler.call(this, handler)
    }
}

function hookInvokeMethod(MethodChannel) {
    // Hook: invokeMethod(String, Object)
    try {
        const invokeMethod = MethodChannel.invokeMethod.overload(Config.classes.String, Config.classes.Object)
        invokeMethod.implementation = function(method, args) {
            const channelName = this.name.value

            if (shouldMonitor(channelName)) {
                console.log("\n╔════════════════════════════════════════╗")
                console.log("║ JAVA → FLUTTER                         ║")
                console.log("╚════════════════════════════════════════╝")
                console.log("Channel:   " + channelName)
                console.log("Method:    " + method)
                console.log("Arguments: " + JSON.stringify(serializeValue(args), null, 2))
                console.log("────────────────────────────────────────")
            }

            return invokeMethod.call(this, method, args)
        }
    } catch (e) {
        console.log("[!] invokeMethod(String, Object) not found")
    }

    // Hook: invokeMethod(String, Object, Result)
    try {
        const invokeMethod = MethodChannel.invokeMethod.overload(Config.classes.String, Config.classes.Object, Config.classes.MethodChannelResult)
        invokeMethod.implementation = function(method, args, result) {
            const channelName = this.name.value

            if (shouldMonitor(channelName)) {
                console.log("\n╔════════════════════════════════════════╗")
                console.log("║ JAVA → FLUTTER (with callback)        ║")
                console.log("╚════════════════════════════════════════╝")
                console.log("Channel:   " + channelName)
                console.log("Method:    " + method)
                console.log("Arguments: " + JSON.stringify(serializeValue(args), null, 2))
                console.log("────────────────────────────────────────")
            }

            return invokeMethod.call(this, method, args, result)
        }
    } catch (e) {
        console.log("[!] invokeMethod(String, Object, Result) not found")
    }
}

function hookEventChannel() {
    try {
        const EventChannel = Java.use(Config.classes.EventChannel)
        const setStreamHandler = EventChannel.setStreamHandler.overload(Config.classes.EventChannelStreamHandler)
        setStreamHandler.implementation = function(handler) {
            const channelName = this.name.value

            if (shouldMonitor(channelName) && handler != null) {
                console.log("\n[EVENT CHANNEL] Handler set: " + channelName)

                const retainedHandler = Java.retain(handler)

                const StreamHandlerWrapper = Java.registerClass({
                    name: generateRandomClassName('EventMonitor'),
                    implements: [Java.use(Config.classes.EventChannelStreamHandler)],
                    methods: {
                        onListen: function (listenArgs, eventSink) {
                            console.log("[EVENT STREAM] onListen called for: " + channelName)

                            const wrappedEventSink = createEventSinkWrapper(eventSink, channelName)
                            retainedHandler.onListen(listenArgs, wrappedEventSink)
                        },
                        onCancel: function (cancelArgs) {
                            console.log("[EVENT STREAM] Cancelled: " + channelName)
                            retainedHandler.onCancel(cancelArgs)
                        }
                    }
                })

                return setStreamHandler.call(this, StreamHandlerWrapper.$new())
            }

            return setStreamHandler.call(this, handler)
        }

        console.log("[✓] EventChannel monitoring installed")
        return true
    } catch (e) {
        console.log("[✗] EventChannel monitoring failed: " + e.message)
        return false
    }
}

function createEventSinkWrapper(eventSink, channelName) {
    const retainedEventSink = Java.retain(eventSink)
    const EventSinkWrapper = Java.registerClass({
        name: generateRandomClassName('SinkMonitor'),
        implements: [Java.use(Config.classes.EventChannelEventSink)],
        methods: {
            success: function (event) {
                console.log("\n╔════════════════════════════════════════╗")
                console.log("║ EVENT STREAM DATA                      ║")
                console.log("╚════════════════════════════════════════╝")
                console.log("Channel: " + channelName)
                console.log("Event:   " + JSON.stringify(serializeValue(event), null, 2))
                console.log("────────────────────────────────────────\n")

                retainedEventSink.success(event)
            },
            error: function (errorCode, errorMessage, errorDetails) {
                console.log("\n[EVENT ERROR] " + channelName)
                console.log("  Code: " + errorCode)
                console.log("  Message: " + errorMessage)

                retainedEventSink.error(errorCode, errorMessage, errorDetails)
            },
            endOfStream: function () {
                console.log("\n[EVENT STREAM] Ended: " + channelName)
                retainedEventSink.endOfStream()
            }
        }
    })
    return EventSinkWrapper.$new()
}

function hookBasicMessageChannel() {
    try {
        const BasicMessageChannel = Java.use(Config.classes.BasicMessageChannel)

        // Hook: send(Object)
        try {
            const send = BasicMessageChannel.send.overload(Config.classes.Object)

            send.implementation = function(message) {
                const channelName = this.name.value

                if (shouldMonitor(channelName)) {
                    console.log("\n╔════════════════════════════════════════╗")
                    console.log("║ BASIC MESSAGE: JAVA → FLUTTER          ║")
                    console.log("╚════════════════════════════════════════╝")
                    console.log("Channel: " + channelName)
                    console.log("Message: " + JSON.stringify(serializeValue(message), null, 2))
                    console.log("────────────────────────────────────────")
                }

                return send.call(this, message)
            }
        } catch (e) {
            console.log("[!] BasicMessageChannel.send(Object) not found")
        }

        console.log("[✓] BasicMessageChannel monitoring installed")
        return true

    } catch (e) {
        console.log("[✗] BasicMessageChannel monitoring failed: " + e.message)
        return false
    }
}

function hookFlutterJNI() {
    try {
        const FlutterJNI = Java.use(Config.classes.FlutterJNI)

        // Hook: Java → Flutter (dispatchPlatformMessage)
        FlutterJNI.dispatchPlatformMessage.implementation = function(channelName, byteBuffer, position, responseId) {
            console.log("\n╔════════════════════════════════════════╗")
            console.log("║ JAVA → FLUTTER (JNI)                   ║")
            console.log("╚════════════════════════════════════════╝")
            console.log("Channel:     " + channelName)
            console.log("Response ID: " + responseId)
            console.log("Position:    " + position)
            console.log("────────────────────────────────────────")

            return this.dispatchPlatformMessage(channelName, byteBuffer, position, responseId)
        }

        // Hook: Java → Flutter (dispatchEmptyPlatformMessage)
        FlutterJNI.dispatchEmptyPlatformMessage.implementation = function(channelName, responseId) {
            console.log("\n╔════════════════════════════════════════╗")
            console.log("║ JAVA → FLUTTER (JNI - Empty)           ║")
            console.log("╚════════════════════════════════════════╝")
            console.log("Channel:     " + channelName)
            console.log("Response ID: " + responseId)
            console.log("────────────────────────────────────────")

            return this.dispatchEmptyPlatformMessage(channelName, responseId)
        }

        // Hook: Flutter → Java (handlePlatformMessage)
        FlutterJNI.handlePlatformMessage.implementation = function(channelName, byteBuffer, replyId, messageData) {
            console.log("\n╔════════════════════════════════════════╗")
            console.log("║ FLUTTER → JAVA (JNI)                   ║")
            console.log("╚════════════════════════════════════════╝")
            console.log("Channel:  " + channelName)
            console.log("Reply ID: " + replyId)
            console.log("────────────────────────────────────────")

            return this.handlePlatformMessage(channelName, byteBuffer, replyId, messageData)
        }

        // Hook: Flutter → Java (handlePlatformMessageResponse)
        FlutterJNI.handlePlatformMessageResponse.implementation = function(responseId, byteBuffer) {
            console.log("\n┌─ RESPONSE (JNI) ───────────────────────┐")
            console.log("│ Response ID: " + responseId)
            console.log("└────────────────────────────────────────┘")

            return this.handlePlatformMessageResponse(responseId, byteBuffer)
        }

        console.log("[✓] FlutterJNI fallback monitoring installed")
        return true

    } catch (e) {
        console.log("[✗] FlutterJNI fallback monitoring failed: " + e.message)
        return false
    }
}

Java.perform(function() {
    console.log("[*] BicoccApp Research - Starting...")
    hookMethodChannel()
    hookEventChannel()
    hookBasicMessageChannel()
    hookFlutterJNI()
})