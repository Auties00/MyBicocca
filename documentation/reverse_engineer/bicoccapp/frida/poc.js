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
 * 1. Intercepting the Talsec EventChannel that streams threat notifications to Flutter
 * 2. Blocking all threat events before they reach the Flutter layer
 * 3. Intercepting flutter_jailbreak_detection and returning false for all checks
 * 4. Intercepting http_certificate_pinning and returning CONNECTION_SECURE for all checks
 *
 * USAGE:
 * frida -U -l "poc.js" -f it.bicoccapp.unimib
 *
 * NOTE:
 * Use research.js to monitor all channel traffic
 */

Java.perform(function() {
    console.log("[*] Bypass starting...\n");

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

        console.log("[✓] EventChannel filter installed");

    } catch (e) {
        console.log("[✗] EventChannel filter error: " + e);
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
            }else if(channelName.includes("http_certificate_pinning")) {
                console.log("[+] Intercepted ssl pinning channel");

                var retainedHandler = Java.retain(handler);

                var HandlerWrapper = Java.registerClass({
                    name: 'com.frida.SslPinningBypass' + Math.random().toString(36).substr(2, 9),
                    implements: [Java.use('io.flutter.plugin.common.MethodChannel$MethodCallHandler')],
                    methods: {
                        onMethodCall: function(call, result) {
                            var methodName = call.method.value;

                            if (methodName === "check") {

                                console.log("[BYPASSED] " + methodName + " -> CONNECTION_SECURE");
                                result.success(Java.use("java.lang.String").valueOf("CONNECTION_SECURE"));
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

        console.log("[✓] MethodChannel filter installed");
    } catch (e) {
        console.log("[✗] MethodChannel filter error: " + e);
    }

    console.log("[✓] Bypass installed");
});
