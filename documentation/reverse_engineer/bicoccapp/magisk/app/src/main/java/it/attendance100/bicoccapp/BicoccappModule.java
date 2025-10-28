package it.attendance100.bicoccapp;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class BicoccappModule implements IXposedHookLoadPackage {
    private static final String TARGET_PACKAGE = "it.bicoccapp.unimib";

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(TARGET_PACKAGE)) {
            return;
        }

        XposedBridge.log("[✓] Hooked into: " + lpparam.packageName);

        // Hook EventChannel for Talsec/Freerasp bypass
        hookEventChannel(lpparam);

        // Hook MethodChannel for jailbreak detection and SSL pinning bypass
        hookMethodChannel(lpparam);
    }

    private void hookEventChannel(LoadPackageParam lpparam) {
        try {
            Class<?> eventChannelClass = XposedHelpers.findClass(
                "io.flutter.plugin.common.EventChannel",
                lpparam.classLoader
            );

            XposedHelpers.findAndHookMethod(
                eventChannelClass,
                "setStreamHandler",
                "io.flutter.plugin.common.EventChannel$StreamHandler",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Object eventChannel = param.thisObject;
                        Object handler = param.args[0];

                        String channelName = (String) XposedHelpers.getObjectField(eventChannel, "name");

                        // Intercept Talsec/Freerasp channels
                        if (channelName.contains("talsec") || channelName.contains("freerasp")) {
                            XposedBridge.log("[+] Intercepted Talsec EventChannel: " + channelName);

                            // Create blocking wrapper
                            Object wrappedHandler = createBlockingStreamHandler(param, handler);
                            param.args[0] = wrappedHandler;
                        }
                    }
                }
            );

            XposedBridge.log("[✓] EventChannel hook installed");
        } catch (Throwable e) {
            XposedBridge.log("[✗] EventChannel hook error: " + e.getMessage());
        }
    }

    private Object createBlockingStreamHandler(XC_MethodHook.MethodHookParam param, Object originalHandler) {
        try {
            Class<?> streamHandlerInterface = XposedHelpers.findClass(
                "io.flutter.plugin.common.EventChannel$StreamHandler",
                param.thisObject.getClass().getClassLoader()
            );

            return java.lang.reflect.Proxy.newProxyInstance(
                param.thisObject.getClass().getClassLoader(),
                new Class<?>[]{streamHandlerInterface},
                (proxy, method, args) -> {
                    String methodName = method.getName();

                    if ("onListen".equals(methodName)) {
                        Object eventSink = args[1];
                        Object wrappedSink = createBlockingEventSink(param, eventSink);
                        args[1] = wrappedSink;
                        return method.invoke(originalHandler, args);
                    } else if ("onCancel".equals(methodName)) {
                        return method.invoke(originalHandler, args);
                    }

                    return null;
                }
            );
        } catch (Exception e) {
            XposedBridge.log("[✗] Error creating StreamHandler wrapper: " + e.getMessage());
            return originalHandler;
        }
    }

    private Object createBlockingEventSink(XC_MethodHook.MethodHookParam param, Object originalSink) {
        try {
            Class<?> eventSinkInterface = XposedHelpers.findClass(
                "io.flutter.plugin.common.EventChannel$EventSink",
                param.thisObject.getClass().getClassLoader()
            );

            return java.lang.reflect.Proxy.newProxyInstance(
                param.thisObject.getClass().getClassLoader(),
                new Class<?>[]{eventSinkInterface},
                (proxy, method, args) -> {
                    String methodName = method.getName();

                    if ("success".equals(methodName)) {
                        XposedBridge.log("[BLOCKED] Talsec threat event");
                        return null; // Block the event
                    } else if ("error".equals(methodName)) {
                        XposedBridge.log("[BLOCKED] Talsec error event");
                        return null; // Block the event
                    } else if ("endOfStream".equals(methodName)) {
                        return method.invoke(originalSink, args);
                    }

                    return null;
                }
            );
        } catch (Exception e) {
            XposedBridge.log("[✗] Error creating EventSink wrapper: " + e.getMessage());
            return originalSink;
        }
    }

    private void hookMethodChannel(LoadPackageParam lpparam) {
        try {
            Class<?> methodChannelClass = XposedHelpers.findClass(
                "io.flutter.plugin.common.MethodChannel",
                lpparam.classLoader
            );

            XposedHelpers.findAndHookMethod(
                methodChannelClass,
                "setMethodCallHandler",
                "io.flutter.plugin.common.MethodChannel$MethodCallHandler",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Object methodChannel = param.thisObject;
                        Object handler = param.args[0];

                        String channelName = (String) XposedHelpers.getObjectField(methodChannel, "name");

                        // Intercept jailbreak detection channel
                        if (channelName.contains("flutter_jailbreak_detection")) {
                            XposedBridge.log("[+] Intercepted jailbreak detection channel");
                            Object wrappedHandler = createJailbreakBypassHandler(param, handler);
                            param.args[0] = wrappedHandler;
                        }
                        // Intercept SSL pinning channel
                        else if (channelName.contains("http_certificate_pinning")) {
                            XposedBridge.log("[+] Intercepted SSL pinning channel");
                            Object wrappedHandler = createSslPinningBypassHandler(param, handler);
                            param.args[0] = wrappedHandler;
                        }
                    }
                }
            );

            XposedBridge.log("[✓] MethodChannel hook installed");
        } catch (Throwable e) {
            XposedBridge.log("[✗] MethodChannel hook error: " + e.getMessage());
        }
    }

    private Object createJailbreakBypassHandler(XC_MethodHook.MethodHookParam param, Object originalHandler) {
        try {
            Class<?> methodCallHandlerInterface = XposedHelpers.findClass(
                "io.flutter.plugin.common.MethodChannel$MethodCallHandler",
                param.thisObject.getClass().getClassLoader()
            );

            return java.lang.reflect.Proxy.newProxyInstance(
                param.thisObject.getClass().getClassLoader(),
                new Class<?>[]{methodCallHandlerInterface},
                (proxy, method, args) -> {
                    if ("onMethodCall".equals(method.getName())) {
                        Object call = args[0];
                        Object result = args[1];

                        String methodName = (String) XposedHelpers.getObjectField(call, "method");

                        // Bypass jailbreak/root checks
                        if ("jailbroken".equals(methodName) ||
                            "canMockLocation".equals(methodName) ||
                            "developerMode".equals(methodName)) {

                            XposedBridge.log("[BYPASSED] " + methodName + " -> false");

                            // Call result.success(false)
                            XposedHelpers.callMethod(result, "success", Boolean.FALSE);
                            return null;
                        }

                        // Pass through other methods
                        return method.invoke(originalHandler, args);
                    }

                    return null;
                }
            );
        } catch (Exception e) {
            XposedBridge.log("[✗] Error creating jailbreak bypass handler: " + e.getMessage());
            return originalHandler;
        }
    }

    private Object createSslPinningBypassHandler(XC_MethodHook.MethodHookParam param, Object originalHandler) {
        try {
            Class<?> methodCallHandlerInterface = XposedHelpers.findClass(
                "io.flutter.plugin.common.MethodChannel$MethodCallHandler",
                param.thisObject.getClass().getClassLoader()
            );

            return java.lang.reflect.Proxy.newProxyInstance(
                param.thisObject.getClass().getClassLoader(),
                new Class<?>[]{methodCallHandlerInterface},
                (proxy, method, args) -> {
                    if ("onMethodCall".equals(method.getName())) {
                        Object call = args[0];
                        Object result = args[1];

                        String methodName = (String) XposedHelpers.getObjectField(call, "method");

                        // Bypass SSL pinning check
                        if ("check".equals(methodName)) {
                            XposedBridge.log("[BYPASSED] SSL pinning check -> CONNECTION_SECURE");

                            // Call result.success("CONNECTION_SECURE")
                            XposedHelpers.callMethod(result, "success", "CONNECTION_SECURE");
                            return null;
                        }

                        // Pass through other methods
                        return method.invoke(originalHandler, args);
                    }

                    return null;
                }
            );
        } catch (Exception e) {
            XposedBridge.log("[✗] Error creating SSL pinning bypass handler: " + e.getMessage());
            return originalHandler;
        }
    }
}