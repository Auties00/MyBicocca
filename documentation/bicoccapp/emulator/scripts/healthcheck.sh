#!/bin/bash

if [ -f /var/run/emulator.pid ]; then
    EMULATOR_PID=$(cat /var/run/emulator.pid)
    if ! kill -0 $EMULATOR_PID 2>/dev/null; then
        echo "Emulator process not running"
        exit 1
    fi
else
    echo "Emulator PID file not found"
    exit 1
fi

if ! adb devices 2>/dev/null | grep -q "emulator"; then
    echo "ADB cannot connect to emulator"
    exit 1
fi

BOOT_COMPLETED=$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
if [ "$BOOT_COMPLETED" != "1" ]; then
    echo "Device not fully booted"
    exit 1
fi

echo "Healthy"
exit 0
