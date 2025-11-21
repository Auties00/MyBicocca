package it.attendance100.bicoccapp;

import android.app.Application;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BicoccappModule implements IXposedHookLoadPackage {
    private static final String TARGET_PACKAGE = "it.bicoccapp.unimib";
    private static final String FLUTTER_JNI_CLASS = "io.flutter.embedding.engine.FlutterJNI";
    private static final String TAG = "BicoccappModule";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(TARGET_PACKAGE)) {
            return;
        }

        XposedBridge.log(TAG + ": Target app detected: " + TARGET_PACKAGE);

        // Wait for Application.onCreate to ensure all classes are loaded
        XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log(TAG + ": Application created, installing hooks...");

                try {
                    installHooks(lpparam.classLoader);
                    XposedBridge.log(TAG + ": ✓ Bypass installed successfully");
                } catch (Throwable t) {
                    XposedBridge.log(TAG + ": ✗ Failed to install bypass: " + t.getMessage());
                    XposedBridge.log(t);
                }
            }
        });
    }

    private static boolean isTalsecChannel(String channelName) {
        if (channelName == null) return false;
        String lower = channelName.toLowerCase();
        return lower.contains("talsec") || lower.contains("freerasp");
    }

    private static boolean isJailbreakChannel(String channelName) {
        if (channelName == null) return false;
        return channelName.toLowerCase().contains("flutter_jailbreak_detection");
    }

    private static boolean isSslPinningChannel(String channelName) {
        if (channelName == null) return false;
        return channelName.toLowerCase().contains("http_certificate_pinning");
    }

    private static ByteBuffer createBooleanFalseResponse() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(2);
        buffer.put((byte) 0x00); // Success envelope
        buffer.put((byte) 0x02); // Boolean FALSE
        buffer.flip();
        return buffer;
    }

    private static ByteBuffer createConnectionSecureResponse() {
        byte[] strBytes = "CONNECTION_SECURE".getBytes(StandardCharsets.UTF_8);
        int length = strBytes.length;
        int sizeBytes = length < 254 ? 1 : (length <= 0xffff ? 3 : 5);

        ByteBuffer buffer = ByteBuffer.allocateDirect(2 + sizeBytes + length);

        buffer.put((byte) 0x00); // Success envelope
        buffer.put((byte) 0x07); // STRING type code

        // Write size using StandardMessageCodec's variable-length encoding
        if (length < 254) {
            buffer.put((byte) length);
        } else if (length <= 0xffff) {
            buffer.put((byte) 254);
            buffer.put((byte) (length & 0xFF));
            buffer.put((byte) ((length >> 8) & 0xFF));
        } else {
            buffer.put((byte) 255);
            buffer.put((byte) (length & 0xFF));
            buffer.put((byte) ((length >> 8) & 0xFF));
            buffer.put((byte) ((length >> 16) & 0xFF));
            buffer.put((byte) ((length >> 24) & 0xFF));
        }

        buffer.put(strBytes);
        buffer.flip();

        return buffer;
    }

    private static void installHooks(ClassLoader classLoader) throws Throwable {
        Class<?> flutterJNIClass = XposedHelpers.findClass(FLUTTER_JNI_CLASS, classLoader);

        hookMethodChannel(flutterJNIClass);
        hookEventStream(flutterJNIClass);
    }

    private static void hookMethodChannel(Class<?> flutterJNIClass) {
        XposedHelpers.findAndHookMethod(
            flutterJNIClass,
            "handlePlatformMessage",
            String.class,           // channelName
            ByteBuffer.class,       // byteBuffer
            int.class,              // replyId
            long.class,             // messageData
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String channelName = (String) param.args[0];
                    int replyId = (int) param.args[2];
                    long messageData = (long) param.args[3];

                    // Intercept jailbreak detection
                    if (isJailbreakChannel(channelName)) {
                        XposedBridge.log(TAG + ": [OVERRIDE] " + channelName + " -> false");

                        ByteBuffer response = createBooleanFalseResponse();
                        int responseSize = response.remaining();

                        // Send response back to Flutter
                        XposedHelpers.callMethod(
                            param.thisObject,
                            "invokePlatformMessageResponseCallback",
                            replyId,
                            response,
                            responseSize
                        );

                        // Cleanup
                        if (messageData != 0) {
                            XposedHelpers.callMethod(
                                param.thisObject,
                                "cleanupMessageData",
                                messageData
                            );
                        }

                        // Skip original method
                        param.setResult(null);
                        return;
                    }

                    // Intercept SSL pinning
                    if (isSslPinningChannel(channelName)) {
                        XposedBridge.log(TAG + ": [OVERRIDE] " + channelName + " -> CONNECTION_SECURE");

                        ByteBuffer response = createConnectionSecureResponse();
                        int responseSize = response.remaining();

                        // Send response back to Flutter
                        XposedHelpers.callMethod(
                            param.thisObject,
                            "invokePlatformMessageResponseCallback",
                            replyId,
                            response,
                            responseSize
                        );

                        // Cleanup
                        if (messageData != 0) {
                            XposedHelpers.callMethod(
                                param.thisObject,
                                "cleanupMessageData",
                                messageData
                            );
                        }

                        // Skip original method
                        param.setResult(null);
                    }
                }
            }
        );

        XposedBridge.log(TAG + ": ✓ MethodChannel hooked");
    }

    private static void hookEventStream(Class<?> flutterJNIClass) {
        XposedHelpers.findAndHookMethod(
            flutterJNIClass,
            "dispatchPlatformMessage",
            String.class,           // channelName
            ByteBuffer.class,       // byteBuffer
            int.class,              // position
            int.class,              // responseId
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String channelName = (String) param.args[0];

                    // Block Talsec events
                    if (isTalsecChannel(channelName)) {
                        XposedBridge.log(TAG + ": [BLOCKED] " + channelName);
                        param.setResult(null); // Skip original method
                    }
                }
            }
        );

        XposedBridge.log(TAG + ": ✓ EventStream hooked");
    }
}