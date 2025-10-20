/**
 * Flutter Channel Research Monitor
 * 
 * PURPOSE:
 * This script monitors ALL Flutter channel communications in the app.
 * It logs method calls, arguments, return values, and event streams.
 * Use this to understand how the app works and detect changes in detection mechanisms.
 * 
 * WHAT IT MONITORS:
 * - MethodChannel: Bidirectional method calls between Flutter and native code
 * - EventChannel: Streaming events (used by Talsec for threat notifications)
 * - BasicMessageChannel: Raw message passing
 * - StandardMethodCodec: Method encoding/decoding at the codec level
 * 
 * USAGE:
 * frida -U -l "research.js" -f it.bicoccapp.unimib
 * 
 * NOTE:
 * This is a pure monitoring script - it does NOT bypass or modify any security checks.
 * Use poc.js for the actual bypass.
 */

Java.perform(function() {
    console.log("[*] Talsec Security Research - Starting...\n");
    
    // ============================================
    // HELPER: Check if channel should be monitored
    // ============================================
    
    function shouldMonitor(channelName) {
        return channelName != null;
    }
    
    // ============================================
    // MONITOR: MethodChannel (Flutter ↔ Java)
    // ============================================
    // MethodChannel is used for bidirectional method calls.
    // We monitor both directions: Flutter→Java and Java→Flutter.
    
    try {
        var MethodChannel = Java.use("io.flutter.plugin.common.MethodChannel");
        
        // DIRECTION: Flutter → Java
        // When Flutter calls a method on the Java side
        var setMethodCallHandler = MethodChannel.setMethodCallHandler.overload('io.flutter.plugin.common.MethodChannel$MethodCallHandler');
        setMethodCallHandler.implementation = function(handler) {
            var channelName = this.name.value;
            
            if (shouldMonitor(channelName) && handler != null) {
                console.log("[HANDLER SET] MethodChannel: " + channelName);
                
                var retainedHandler = Java.retain(handler);
                
                var HandlerWrapper = Java.registerClass({
                    name: 'com.frida.MethodMonitor' + Math.random().toString(36).substr(2, 9),
                    implements: [Java.use('io.flutter.plugin.common.MethodChannel$MethodCallHandler')],
                    methods: {
                        onMethodCall: function(call, result) {
                            var methodName = call.method.value;
                            var args = call.arguments.value;
                            
                            console.log("\n╔════════════════════════════════════════╗");
                            console.log("║ FLUTTER → JAVA                         ║");
                            console.log("╚════════════════════════════════════════╝");
                            console.log("Channel:   " + channelName);
                            console.log("Method:    " + methodName);
                            console.log("Arguments: " + JSON.stringify(serializeValue(args), null, 2));
                            console.log("────────────────────────────────────────");
                            
                            // Wrap result to capture return value
                            var wrappedResult = wrapResult(result, channelName, methodName);
                            retainedHandler.onMethodCall(call, wrappedResult);
                        }
                    }
                });
                
                return setMethodCallHandler.call(this, HandlerWrapper.$new());
            }
            
            return setMethodCallHandler.call(this, handler);
        };
        
        // DIRECTION: Java → Flutter
        // When Java sends data to Flutter
        var invokeMethod1 = MethodChannel.invokeMethod.overload('java.lang.String', 'java.lang.Object');
        invokeMethod1.implementation = function(method, args) {
            var channelName = this.name.value;
            
            if (shouldMonitor(channelName)) {
                console.log("\n╔════════════════════════════════════════╗");
                console.log("║ JAVA → FLUTTER                         ║");
                console.log("╚════════════════════════════════════════╝");
                console.log("Channel:   " + channelName);
                console.log("Method:    " + method);
                console.log("Arguments: " + JSON.stringify(serializeValue(args), null, 2));
                console.log("────────────────────────────────────────");
            }
            
            return invokeMethod1.call(this, method, args);
        };
        
        var invokeMethod2 = MethodChannel.invokeMethod.overload('java.lang.String', 'java.lang.Object', 'io.flutter.plugin.common.MethodChannel$Result');
        invokeMethod2.implementation = function(method, args, result) {
            var channelName = this.name.value;
            
            if (shouldMonitor(channelName)) {
                console.log("\n╔════════════════════════════════════════╗");
                console.log("║ JAVA → FLUTTER (with callback)        ║");
                console.log("╚════════════════════════════════════════╝");
                console.log("Channel:   " + channelName);
                console.log("Method:    " + method);
                console.log("Arguments: " + JSON.stringify(serializeValue(args), null, 2));
                console.log("────────────────────────────────────────");
            }
            
            return invokeMethod2.call(this, method, args, result);
        };
        
        console.log("[✓] MethodChannel monitoring installed");
        
    } catch (e) {
        console.log("[✗] MethodChannel error: " + e);
    }
    
    // ============================================
    // MONITOR: EventChannel (Event Streams)
    // ============================================
    // EventChannel is used for streaming events from Java to Flutter.
    // Talsec uses this to send continuous threat notifications.
    
    try {
        var EventChannel = Java.use("io.flutter.plugin.common.EventChannel");
        
        var setStreamHandler = EventChannel.setStreamHandler.overload('io.flutter.plugin.common.EventChannel$StreamHandler');
        setStreamHandler.implementation = function(handler) {
            var channelName = this.name.value;
            
            if (shouldMonitor(channelName)) {
                console.log("\n[EVENT CHANNEL] Handler set: " + channelName);
                
                if (handler != null) {
                    var retainedHandler = Java.retain(handler);
                    
                    var StreamHandlerWrapper = Java.registerClass({
                        name: 'com.frida.EventMonitor' + Math.random().toString(36).substr(2, 9),
                        implements: [Java.use('io.flutter.plugin.common.EventChannel$StreamHandler')],
                        methods: {
                            onListen: function(listenArgs, eventSink) {
                                console.log("[EVENT STREAM] onListen called for: " + channelName);
                                
                                var retainedEventSink = Java.retain(eventSink);
                                
                                // Wrap EventSink to monitor events
                                var EventSinkWrapper = Java.registerClass({
                                    name: 'com.frida.SinkMonitor' + Math.random().toString(36).substr(2, 9),
                                    implements: [Java.use('io.flutter.plugin.common.EventChannel$EventSink')],
                                    methods: {
                                        success: function(event) {
                                            console.log("\n╔════════════════════════════════════════╗");
                                            console.log("║ EVENT STREAM DATA                      ║");
                                            console.log("╚════════════════════════════════════════╝");
                                            console.log("Channel: " + channelName);
                                            console.log("Event:   " + JSON.stringify(serializeValue(event), null, 2));
                                            console.log("────────────────────────────────────────\n");
                                            
                                            // Forward to Flutter
                                            retainedEventSink.success(event);
                                        },
                                        error: function(errorCode, errorMessage, errorDetails) {
                                            console.log("\n[EVENT ERROR] " + channelName);
                                            console.log("  Code: " + errorCode);
                                            console.log("  Message: " + errorMessage);
                                            
                                            // Forward to Flutter
                                            retainedEventSink.error(errorCode, errorMessage, errorDetails);
                                        },
                                        endOfStream: function() {
                                            console.log("\n[EVENT STREAM] Ended: " + channelName);
                                            retainedEventSink.endOfStream();
                                        }
                                    }
                                });
                                
                                retainedHandler.onListen(listenArgs, EventSinkWrapper.$new());
                            },
                            onCancel: function(cancelArgs) {
                                console.log("[EVENT STREAM] Cancelled: " + channelName);
                                retainedHandler.onCancel(cancelArgs);
                            }
                        }
                    });
                    
                    return setStreamHandler.call(this, StreamHandlerWrapper.$new());
                }
            }
            
            return setStreamHandler.call(this, handler);
        };
        
        console.log("[✓] EventChannel monitoring installed");
        
    } catch (e) {
        console.log("[✗] EventChannel error: " + e);
    }
    
    // ============================================
    // MONITOR: BasicMessageChannel (Raw Messages)
    // ============================================
    // BasicMessageChannel allows simple message passing without method structure.
    // Some plugins use this instead of MethodChannel.
    
    try {
        var BasicMessageChannel = Java.use("io.flutter.plugin.common.BasicMessageChannel");
        
        // Java → Flutter
        try {
            var send1 = BasicMessageChannel.send.overload('java.lang.Object');
            send1.implementation = function(message) {
                var channelName = this.name.value;
                
                if (shouldMonitor(channelName)) {
                    console.log("\n[BASIC MESSAGE] Java → Flutter");
                    console.log("  Channel: " + channelName);
                    console.log("  Message: " + JSON.stringify(serializeValue(message), null, 2));
                }
                
                return send1.call(this, message);
            };
        } catch (e) {}
        
        console.log("[✓] BasicMessageChannel monitoring installed");
        
    } catch (e) {
        console.log("[✗] BasicMessageChannel error: " + e);
    }
    
    // ============================================
    // MONITOR: StandardMethodCodec (Codec Level)
    // ============================================
    // Monitor at the encoding/decoding level to catch all method calls,
    // even if higher-level hooks fail.
    
    try {
        var StandardMethodCodec = Java.use("io.flutter.plugin.common.StandardMethodCodec");
        
        // Decode: Flutter → Java
        var decodeMethodCall = StandardMethodCodec.decodeMethodCall.overload('java.nio.ByteBuffer');
        decodeMethodCall.implementation = function(buffer) {
            var result = decodeMethodCall.call(this, buffer);
            
            try {
                var method = result.method.value;
                
                // Only log security-related methods
                if (method.toLowerCase().includes("root") ||
                    method.toLowerCase().includes("jail") ||
                    method.toLowerCase().includes("start") ||
                    method.toLowerCase().includes("check") ||
                    method.toLowerCase().includes("detect")) {
                    
                    var args = result.arguments.value;
                    console.log("\n[CODEC DECODE] " + method + " | Args: " + JSON.stringify(serializeValue(args)));
                }
            } catch (e) {}
            
            return result;
        };
        
        // Encode errors: Java → Flutter
        var encodeErrorEnvelope = StandardMethodCodec.encodeErrorEnvelope.overload('java.lang.String', 'java.lang.String', 'java.lang.Object');
        encodeErrorEnvelope.implementation = function(errorCode, errorMessage, errorDetails) {
            console.log("\n[CODEC ERROR]");
            console.log("  Code: " + errorCode);
            console.log("  Message: " + errorMessage);
            console.log("  Details: " + JSON.stringify(serializeValue(errorDetails), null, 2));
            
            return encodeErrorEnvelope.call(this, errorCode, errorMessage, errorDetails);
        };
        
        console.log("[✓] StandardMethodCodec monitoring installed");
        
    } catch (e) {
        console.log("[✗] StandardMethodCodec error: " + e);
    }

    console.log("[✓] Monitoring active - All security channel traffic logged");
});

// ============================================
// HELPER: Wrap Result to capture return values
// ============================================

function wrapResult(originalResult, channelName, methodName) {
    var retainedResult = Java.retain(originalResult);
    
    var ResultWrapper = Java.registerClass({
        name: 'com.frida.ResultMonitor' + Math.random().toString(36).substr(2, 9),
        implements: [Java.use('io.flutter.plugin.common.MethodChannel$Result')],
        methods: {
            success: function(result) {
                console.log("┌─ RESPONSE ─────────────────────────────┐");
                console.log("│ Type:   SUCCESS");
                console.log("│ Value:  " + JSON.stringify(serializeValue(result), null, 2));
                console.log("└────────────────────────────────────────┘\n");
                
                retainedResult.success(result);
            },
            error: function(errorCode, errorMessage, errorDetails) {
                console.log("┌─ RESPONSE ─────────────────────────────┐");
                console.log("│ Type:    ERROR");
                console.log("│ Code:    " + errorCode);
                console.log("│ Message: " + errorMessage);
                console.log("│ Details: " + JSON.stringify(serializeValue(errorDetails), null, 2));
                console.log("└────────────────────────────────────────┘\n");
                
                retainedResult.error(errorCode, errorMessage, errorDetails);
            },
            notImplemented: function() {
                console.log("┌─ RESPONSE ─────────────────────────────┐");
                console.log("│ Type: NOT IMPLEMENTED");
                console.log("└────────────────────────────────────────┘\n");
                
                retainedResult.notImplemented();
            }
        }
    });
    
    return ResultWrapper.$new();
}

// ============================================
// HELPER: Serialize Java objects to JSON
// ============================================

function serializeValue(value) {
    if (value === null || value === undefined) return null;
    
    var type = typeof value;
    if (type === "string" || type === "number" || type === "boolean") return value;
    
    // Handle Java objects
    if (value.$className) {
        try {
            // Java Map → JavaScript object
            if (value.$className.includes("Map")) {
                var map = {};
                var entries = value.entrySet().iterator();
                while (entries.hasNext()) {
                    var entry = entries.next();
                    map[entry.getKey().toString()] = serializeValue(entry.getValue());
                }
                return map;
            }
            
            // Java List → JavaScript array
            if (value.$className.includes("List")) {
                var arr = [];
                for (var i = 0; i < value.size(); i++) {
                    arr.push(serializeValue(value.get(i)));
                }
                return arr;
            }
            
            // Other Java objects → type + string representation
            return { 
                type: value.$className, 
                value: value.toString() 
            };
        } catch (e) {
            return "[Serialization error: " + e + "]";
        }
    }
    
    // Handle JavaScript arrays
    if (Array.isArray(value)) {
        return value.map(serializeValue);
    }
    
    // Handle plain JavaScript objects
    try {
        return JSON.parse(JSON.stringify(value));
    } catch (e) {
        return value.toString();
    }
}
