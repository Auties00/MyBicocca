/**
 * BicoccApp Root Detection Bypass
 * 
 * CONTEXT:
 * Android blocks us from intercepting BicoccApp's HTTP traffic using SSL pinning.
 * To disable SSL pinning, we can use HTTPToolkit with root access. 
 * However, BicoccApp detects root and refuses to run on rooted devices.
 * BicoccApp is written in Flutter and uses Talsec (https://www.talsec.app/) for security checks. 
 * This was discovered by decompiling the app using Jadx.
 * 
 * SOLUTION:
 * This Frida script bypasses root detection by:
 * 1. Intercepting the Talsec EventChannel that streams threat notifications to Flutter
 * 2. Blocking all threat events before they reach the Flutter's layer
 * 3. Intercepting flutter_jailbreak_detection and returning false for all checks
 * 
 * USAGE:
 * frida -U -l "app.js" -f it.bicoccapp.unimib
 */

Java.perform(function() {
    console.log("[*] Talsec Security Bypass - Starting...\n");
    
    // Talsec uses an EventChannel to stream threat notifications.
    // We intercept the stream and block all events from reaching Flutter.
    try {
        var EventChannel = Java.use("io.flutter.plugin.common.EventChannel");
        var setStreamHandler = EventChannel.setStreamHandler.overload('io.flutter.plugin.common.EventChannel$StreamHandler');
        
        setStreamHandler.implementation = function(handler) {
            var channelName = this.name.value;
            
            // Only intercept Talsec/Freerasp channels
            if (channelName.includes("talsec") || channelName.includes("freerasp")) {
                console.log("[+] Intercepted Talsec EventChannel: " + channelName);
                
                var retainedHandler = Java.retain(handler);
                
                // Create a wrapper that blocks all threat events
                var StreamHandlerWrapper = Java.registerClass({
                    name: 'com.frida.TalsecBypass' + Math.random().toString(36).substr(2, 9),
                    implements: [Java.use('io.flutter.plugin.common.EventChannel$StreamHandler')],
                    methods: {
                        onListen: function(listenArgs, eventSink) {
                            var retainedEventSink = Java.retain(eventSink);
                            
                            // Wrap the EventSink to intercept events
                            var EventSinkWrapper = Java.registerClass({
                                name: 'com.frida.EventBlocker' + Math.random().toString(36).substr(2, 9),
                                implements: [Java.use('io.flutter.plugin.common.EventChannel$EventSink')],
                                methods: {
                                    // Block threat notifications
                                    success: function(event) {
                                        console.log("[BLOCKED] Talsec threat event");
                                        console.log("  Data: " + JSON.stringify(serializeValue(event), null, 2));
                                        // Don't forward to Flutter
                                    },
                                    // Block error notifications
                                    error: function(errorCode, errorMessage, errorDetails) {
                                        console.log("[BLOCKED] Talsec error event");
                                        // Don't forward to Flutter
                                    },
                                    // Allow stream to end normally
                                    endOfStream: function() {
                                        retainedEventSink.endOfStream();
                                    }
                                }
                            });
                            
                            // Call original handler with our blocking wrapper
                            retainedHandler.onListen(listenArgs, EventSinkWrapper.$new());
                        },
                        onCancel: function(cancelArgs) {
                            retainedHandler.onCancel(cancelArgs);
                        }
                    }
                });
                
                return setStreamHandler.call(this, StreamHandlerWrapper.$new());
            }
            
            // Pass through non-Talsec channels unchanged
            return setStreamHandler.call(this, handler);
        };
        
        console.log("[+] Talsec EventChannel blocker installed");
        
    } catch (e) {
        console.log("[-] EventChannel error: " + e);
    }
    
    // The flutter_jailbreak_detection plugin checks for root/jailbreak.
    // We intercept its method calls and return false (not jailbroken).
    try {
        var MethodChannel = Java.use("io.flutter.plugin.common.MethodChannel");
        var setMethodCallHandler = MethodChannel.setMethodCallHandler.overload('io.flutter.plugin.common.MethodChannel$MethodCallHandler');
        
        setMethodCallHandler.implementation = function(handler) {
            var channelName = this.name.value;
            
            // Only intercept jailbreak detection channel
            if (channelName.includes("flutter_jailbreak_detection")) {
                console.log("[+] Intercepted jailbreak detection channel");
                
                var retainedHandler = Java.retain(handler);
                
                var HandlerWrapper = Java.registerClass({
                    name: 'com.frida.JailbreakBypass' + Math.random().toString(36).substr(2, 9),
                    implements: [Java.use('io.flutter.plugin.common.MethodChannel$MethodCallHandler')],
                    methods: {
                        onMethodCall: function(call, result) {
                            var methodName = call.method.value;
                            
                            // Return false for all jailbreak/root checks
                            if (methodName === "jailbroken" || 
                                methodName === "canMockLocation" || 
                                methodName === "developerMode") {
                                
                                console.log("[BYPASSED] " + methodName + " -> false");
                                result.success(Java.use("java.lang.Boolean").valueOf(false));
                                return;
                            }
                            
                            // Pass through other methods
                            retainedHandler.onMethodCall(call, result);
                        }
                    }
                });
                
                return setMethodCallHandler.call(this, HandlerWrapper.$new());
            }
            
            // Pass through non-jailbreak channels unchanged
            return setMethodCallHandler.call(this, handler);
        };
        
        console.log("[+] Jailbreak detection bypass installed");
        
    } catch (e) {
        console.log("[-] Jailbreak bypass error: " + e);
    }
    
    console.log("\n[+] All bypasses active - App should work without detection\n");
});

// ============================================
// HELPER: Serialize Java objects for logging
// ============================================

function serializeValue(value) {
    if (value === null || value === undefined) return null;
    
    var type = typeof value;
    if (type === "string" || type === "number" || type === "boolean") return value;
    
    if (value.$className) {
        // Handle Java Maps
        if (value.$className.includes("Map")) {
            try {
                var map = {};
                var entries = value.entrySet().iterator();
                while (entries.hasNext()) {
                    var entry = entries.next();
                    map[entry.getKey().toString()] = serializeValue(entry.getValue());
                }
                return map;
            } catch (e) {}
        }
        
        // Handle Java Lists
        if (value.$className.includes("List")) {
            try {
                var arr = [];
                for (var i = 0; i < value.size(); i++) {
                    arr.push(serializeValue(value.get(i)));
                }
                return arr;
            } catch (e) {}
        }
        
        return { type: value.$className, value: value.toString() };
    }
    
    if (Array.isArray(value)) return value.map(serializeValue);
    
    try {
        return JSON.parse(JSON.stringify(value));
    } catch (e) {
        return value.toString();
    }
}
