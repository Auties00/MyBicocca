#!/bin/bash
set -e

configure_emulator() {
  echo "Configuring emulator..."

  echo "Starting XVFB..."
  Xvfb :1 -screen 0 1920x1080x24 &
  echo "Started XVFB!"

  echo "Starting display manager..."
  fluxbox -display :1 &
  echo "Started display manager!"

  echo "Starting VNC server..."
  x11vnc -display :1 -rfbport 5900 -rfbauth /root/.vnc/passwd -forever -shared &
  echo "Started VNC server!"

  echo "Starting noVNC..."
  /opt/noVNC/utils/novnc_proxy --vnc localhost:5900 --listen 6080 &
  echo "Started noVNC!"

  echo "Emulator configured!"
}

configure_adb_server() {
    echo "Configuring ADB server..."

    echo "Killing existing ADB server..."
    adb kill-server 2>/dev/null || true

    echo "Starting ADB server..."
    adb -a start-server

    echo "Routing TCP port 5555 to adb server..."
    if ! pgrep -f "socat.*5555" > /dev/null; then
        socat TCP-LISTEN:5555,fork,reuseaddr TCP:127.0.0.1:5555 &
    fi
    
    echo "ADB server configured!"
}

kill_emulator() {
   echo "Checking for existing emulators..."
   echo "Killing existing emulators using adb..."
   adb emu kill 2>/dev/null || true
      if pgrep -f "emulator.*-avd" > /dev/null; then
          echo "Emulator is still running: killing process"
          pkill -9 -f "emulator.*-avd" 2>/dev/null || true
          echo "Killed emulator process!"
      fi
      if pgrep -f "qemu-system" > /dev/null; then
          echo "Killing QEMU processes..."
          pkill -9 -f "qemu-system" 2>/dev/null || true
          echo "Killed QEMU process!"
      fi
      if [ -f /var/run/emulator.pid ]; then
          echo "Removing stale PID file..."
          rm -f /var/run/emulator.pid
          echo "Removed stale PID file!"
      fi
      echo "Existing emulators killed"
}

start_emulator() {
    RAMDISK_PATH="${RAMDISK_PATH:-system-images/android-33/google_apis_playstore/x86_64/ramdisk.img}"
    export RAMDISK_PATH

    echo "Starting Android emulator..."

    kill_emulator

    echo "Starting emulator..."
    EMULATOR_CMD="emulator -avd ${AVD_NAME} \
        -writable-system \
        -no-snapshot-load"
    echo "Command: $EMULATOR_CMD"
    $EMULATOR_CMD &
    EMULATOR_PID=$!
    echo $EMULATOR_PID > /var/run/emulator.pid
    echo "Emulator started, PID: $EMULATOR_PID"

    wait_for_emulator_boot
}

wait_for_emulator_boot() {
    echo "Waiting for emulator to boot..."
    TIMEOUT=180
    INTERVAL=5
    ELAPSED=0
    
    echo "Waiting for device connection..."
    
    while [ $ELAPSED -lt $TIMEOUT ]; do
        if adb devices 2>/dev/null | grep -q "emulator"; then
            echo "Device connected"
            break
        fi
        sleep $INTERVAL
        ELAPSED=$((ELAPSED + INTERVAL))
    done
    
    if [ $ELAPSED -ge $TIMEOUT ]; then
        echo "ERROR: Timeout waiting for device connection"
        exit 1
    fi
    
    adb wait-for-device
    
    echo "Waiting for boot completion..."
    ELAPSED=0
    
    while [ $ELAPSED -lt $TIMEOUT ]; do
        BOOT_COMPLETED=$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')
        if [ "$BOOT_COMPLETED" = "1" ]; then
            echo "Boot completed"
            break
        fi
        sleep $INTERVAL
        ELAPSED=$((ELAPSED + INTERVAL))
        echo "Still booting... ($ELAPSED seconds)"
    done
    
    if [ $ELAPSED -ge $TIMEOUT ]; then
        echo "ERROR: Boot timeout after $TIMEOUT seconds"
        exit 1
    fi
    
    ELAPSED=0
    while [ $ELAPSED -lt 60 ]; do
        if adb shell pm list packages 2>/dev/null | grep -q "package:"; then
            echo "Package manager ready"
            break
        fi
        sleep 2
        ELAPSED=$((ELAPSED + 2))
    done
    
    echo "Device fully booted and ready"
}

wait_for_emulator_shutdown() {
    echo "Waiting for emulator to shutdown..."
    TIMEOUT=180
    INTERVAL=5
    ELAPSED=0
    while [ $ELAPSED -lt $TIMEOUT ]; do
        if adb devices 2>/dev/null | grep -q "emulator"; then
            sleep $INTERVAL
            ELAPSED=$((ELAPSED + INTERVAL))
        fi
        echo "Device disconnected"
        break
    done

    if [ $ELAPSED -ge $TIMEOUT ]; then
        echo "ERROR: Timeout waiting for device connection"
        exit 1
    fi
}

configure_emulator_root() {
  echo "Configuring root access..."
  
  ROOT_CHECK=$(adb shell "su -c id" 2>/dev/null || echo "")
  if echo "$ROOT_CHECK" | grep -q "uid=0"; then
      echo "Root access already configured"
  else
      echo "Running rootAVD to enable root access..."
      
      cd /opt/rootAVD
      
      if [ -z "$RAMDISK_PATH" ]; then
          RAMDISK_PATH="system-images/android-33/google_apis_playstore/x86_64/ramdisk.img"
      fi
      
      chmod +x rootAVD.sh
      ./rootAVD.sh "${RAMDISK_PATH}"

      echo "Waiting for rootAVD to complete..."
      wait_for_emulator_shutdown
      
      echo "Restarting emulator after rooting..."
      start_emulator
      
      ROOT_CHECK=$(adb shell "su -c id" 2>/dev/null || echo "")
      if echo "$ROOT_CHECK" | grep -q "uid=0"; then
          echo "Root access enabled"
      else
          echo "WARNING: Root access verification failed"
          exit 1
      fi
  fi
  
  echo "Checking Zygisk status..."
  ZYGISK_STATUS=$(adb shell "su -c \"magisk --sqlite 'select value from settings where (key=\\\"zygisk\\\");'\"" 2>/dev/null || echo "")
  if echo "$ZYGISK_STATUS" | grep -q "value=1"; then
      echo "Zygisk is already enabled"
  else
      echo "Enabling Zygisk..."
      adb shell "su -c \"magisk --sqlite 'replace into settings (key,value) values(\\\"zygisk\\\",1);'\"" 2>/dev/null || true
      echo "Zygisk enabled"
  fi
  
  echo "Configuring Magisk settings..."
  adb shell "su -c 'magisk --sqlite \"PRAGMA user_version=7\"'" 2>/dev/null || true
  adb shell "su -c 'magisk resetprop persist.sys.su.mode 2'" 2>/dev/null || true
  echo "Magisk configured"
}

get_ui_dump() {
    adb shell rm /sdcard/window_dump.xml 2>/dev/null
    if ! adb shell uiautomator dump /sdcard/window_dump.xml 2>&1 | grep -q "dump complete"; then
        echo "Failed to dump UI" >&2
        return 1
    fi
    adb shell cat /sdcard/window_dump.xml
}

is_text_on_screen() {
    local text="$1"
    local output
    output=$(get_ui_dump 2>/dev/null)
    [[ $? -eq 0 ]] && echo "$output" | grep -iq "$text"
}

click_button_by_text() {
    local text="$1"
    local xml_content

    xml_content=$(get_ui_dump)
    if [[ $? -ne 0 ]]; then
        echo "UI dump failed" >&2
        return 1
    fi

    # Parse XML to find the node with matching text and extract bounds
    local bounds
    bounds=$(echo "$xml_content" | grep -i "text=\"$text\"" | grep -oP 'bounds="\K[^"]+' | head -1)

    if [[ -z "$bounds" ]]; then
        echo "button '$text' not found" >&2
        return 1
    fi

    # Parse bounds [x1,y1][x2,y2] -> extract coordinates
    local coords
    coords=$(echo "$bounds" | tr -d '[]' | tr '][' ',')

    IFS=',' read -r x1 y1 x2 y2 <<< "$coords"

    if [[ -z "$x1" || -z "$y1" || -z "$x2" || -z "$y2" ]]; then
        echo "Failed to parse coordinates" >&2
        return 1
    fi

    # Calculate center
    local center_x=$(( (x1 + x2) / 2 ))
    local center_y=$(( (y1 + y2) / 2 ))

    echo "  Clicking '$text' at ($center_x, $center_y)"
    adb shell input tap "$center_x" "$center_y"
}

fix_magisk_environment() {
    echo "Finalizing Magisk environment..."

    if ! adb shell pm grant com.topjohnwu.magisk android.permission.POST_NOTIFICATIONS; then
        echo "Magisk permission grant failed" >&2
        return 1
    fi

    echo "Opening Magisk app"
    if ! adb shell monkey -p com.topjohnwu.magisk -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1; then
        echo "Magisk app launch failed" >&2
        return 1
    fi
    echo "Opened Magisk app!"

    echo "Waiting for setup dialog..."
    for _ in {1..10}; do
        if is_text_on_screen "Additional Setup"; then
            echo "Setup dialog detected"
            break
        fi
        sleep 1
    done

    echo "Clicking OK button..."
    if ! click_button_by_text "OK"; then
        echo "Failed to click OK" >&2
        return 1
    fi
    echo "Clicked on OK button!"

    wait_for_emulator_shutdown

    wait_for_emulator_boot
}

configure_lsposed() {
  echo "Configuring LSPosed..."

  MODULES=$(adb shell "su -c 'ls -1 /data/adb/modules'" 2>/dev/null || echo "")
  if echo "$MODULES" | grep -qi "lsposed"; then
      echo "LSPosed already installed"
      exit 0
  fi

  echo "Pushing LSPosed module to device..."
  adb push /opt/modules/LSPosed.zip /sdcard/LSPosed.zip

  echo "Installing LSPosed via Magisk..."
  INSTALL_RESULT=$(adb shell "su -c 'magisk --install-module /sdcard/LSPosed.zip'" 2>&1 || echo "FAILED")

  if echo "$INSTALL_RESULT" | grep -qi "FAILED\|error"; then
      fix_magisk_environment
      echo "Retrying LSPosed installation..."
      INSTALL_RESULT=$(adb shell "su -c 'magisk --install-module /sdcard/LSPosed.zip'" 2>&1 || echo "")
  fi

  adb shell rm /sdcard/LSPosed.zip 2>/dev/null || true

  MODULES=$(adb shell "su -c 'ls -1 /data/adb/modules'" 2>/dev/null || echo "")
  if echo "$MODULES" | grep -qi "lsposed"; then
      echo "LSPosed installed successfully"
  else
      echo "WARNING: LSPosed installation may have failed - check manually"
  fi

  echo "Rebooting to activate LSPosed..."
  adb reboot 2>/dev/null || true

  wait_for_emulator_boot
}

configure_bicoccapp() {
  BICOCC_APP_PACKAGE="${BICOCC_APP_PACKAGE:-it.bicoccapp.unimib}"
  BICOCCAPP_DIR="/opt/modules/bicoccapp"

  echo "Configuring BicoccApp..."

  if adb shell pm list packages 2>/dev/null | grep -q "$BICOCC_APP_PACKAGE"; then
      echo "BicoccApp already installed"
      exit 0
  fi

  APK_FILES=$(find "$BICOCCAPP_DIR" -name "*.apk" -type f 2>/dev/null | sort)

  if [ -z "$APK_FILES" ]; then
      echo "ERROR: No APK files found in $BICOCCAPP_DIR"
      exit 1
  fi

  APK_COUNT=$(echo "$APK_FILES" | wc -l)
  echo "Found $APK_COUNT APK file(s)"

  for apk in $APK_FILES; do
      echo "  - $(basename "$apk")"
  done

  echo "Installing split APKs..."

  # shellcheck disable=SC2086
  if adb install-multiple -r $APK_FILES; then
      echo "BicoccApp installed successfully"
  else
      echo "install-multiple failed, trying alternative method..."

      TOTAL_SIZE=0
      for apk in $APK_FILES; do
          SIZE=$(stat -c%s "$apk" 2>/dev/null || stat -f%z "$apk" 2>/dev/null)
          TOTAL_SIZE=$((TOTAL_SIZE + SIZE))
      done

      echo "Creating install session (total size: $TOTAL_SIZE bytes)"

      SESSION_OUTPUT=$(adb shell pm install-create -S "$TOTAL_SIZE" 2>&1)
      SESSION_ID=$(echo "$SESSION_OUTPUT" | grep -oE '[0-9]+' | head -1)

      if [ -z "$SESSION_ID" ]; then
          echo "ERROR: Failed to create install session: $SESSION_OUTPUT"
          exit 1
      fi

      echo "Session ID: $SESSION_ID"

      INDEX=0
      for apk in $APK_FILES; do
          APK_NAME=$(basename "$apk")
          APK_SIZE=$(stat -c%s "$apk" 2>/dev/null || stat -f%z "$apk" 2>/dev/null)

          echo "Writing $APK_NAME ($APK_SIZE bytes)..."

          adb push "$apk" "/data/local/tmp/$APK_NAME" > /dev/null 2>&1
          adb shell pm install-write -S "$APK_SIZE" "$SESSION_ID" "$INDEX" "/data/local/tmp/$APK_NAME"
          adb shell rm "/data/local/tmp/$APK_NAME"

          INDEX=$((INDEX + 1))
      done

      echo "Committing install session..."
      COMMIT_OUTPUT=$(adb shell pm install-commit "$SESSION_ID" 2>&1)

      if echo "$COMMIT_OUTPUT" | grep -qi "success"; then
          echo "BicoccApp installed successfully (session method)"
      else
          echo "ERROR: Installation failed: $COMMIT_OUTPUT"
          exit 1
      fi
  fi

  if adb shell pm list packages 2>/dev/null | grep -q "$BICOCC_APP_PACKAGE"; then
      echo "BicoccApp installation verified"
  else
      echo "WARNING: BicoccApp installation could not be verified"
      exit 1
  fi
}

configure_bicoccapp_bypass() {
  BICOCC_APP_PACKAGE="${BICOCC_APP_PACKAGE:-it.bicoccapp.unimib}"
  LSPOSED_MODULE_NAME="${LSPOSED_MODULE_NAME:-it.attendance100.bicoccapp}"

  echo "Setting up bypass module..."

  if ! adb shell "su -c 'ls /data/adb/lspd/bin/cli'" 2>/dev/null | grep -q "cli"; then
      echo "WARNING: LSPosed CLI not found - LSPosed may not be properly installed"
      echo "Please ensure LSPosed is installed and device has been rebooted"
      exit 1
  fi

  MODULES=$(adb shell "su -c '/data/adb/lspd/bin/cli modules ls'" 2>/dev/null || echo "")
  if echo "$MODULES" | grep -q "$LSPOSED_MODULE_NAME"; then
      echo "Bypass module already installed"
  else
      echo "Installing bypass module APK..."
      adb install -r /opt/modules/bypass.apk

      if ! adb shell pm list packages | grep -q "$LSPOSED_MODULE_NAME"; then
          echo "WARNING: Bypass module installation may have failed"
      else
          echo "Bypass module installed"
      fi
  fi

  echo "Configuring module scope..."
  SCOPES=$(adb shell "su -c '/data/adb/lspd/bin/cli scope ls $LSPOSED_MODULE_NAME'" 2>/dev/null || echo "")

  if echo "$SCOPES" | grep -q "$BICOCC_APP_PACKAGE"; then
      echo "Bicoccapp already in module scope"
  else
      echo "Adding Bicoccapp to module scope..."
      adb shell "su -c '/data/adb/lspd/bin/cli scope set -a $LSPOSED_MODULE_NAME ${BICOCC_APP_PACKAGE}/0'" 2>/dev/null || true
      echo "Scope configured"
  fi

  echo "Enabling bypass module..."
  adb shell "su -c '/data/adb/lspd/bin/cli modules set -e $LSPOSED_MODULE_NAME'" 2>/dev/null || true
  echo "Bypass module enabled"

  if adb shell pm list packages | grep -q "$BICOCC_APP_PACKAGE"; then
      echo "BicoccApp is installed"
  else
      echo "BicoccApp not yet installed"
  fi
}

monitor_emulator() {
    MONITOR_INTERVAL=10

    while true; do
        if [ -f /var/run/emulator.pid ]; then
            EMULATOR_PID=$(cat /var/run/emulator.pid)
            if ! kill -0 "$EMULATOR_PID" 2>/dev/null; then
                echo "Emulator process (PID: $EMULATOR_PID) has terminated"
                break
            fi
        fi

        if ! adb devices 2>/dev/null | grep -q "emulator"; then
            echo "Emulator disconnected from ADB"
            break
        fi

        sleep $MONITOR_INTERVAL
    done

    echo "Emulator has disconnected. Exiting..."
    rm -f /var/run/emulator.pid
}

main() {
    echo "============================================"
    echo "  Android Emulator Docker Container"
    echo "============================================"
    echo ""

    configure_emulator
    configure_adb_server
    start_emulator
    configure_emulator_root
    configure_lsposed
    configure_bicoccapp
    configure_bicoccapp_bypass

    echo ""
    echo "============================================"
    echo "Emulator is ready!"
    echo "============================================"

    monitor_emulator
}

trap 'echo "Shutting down..."; adb emu kill 2>/dev/null; exit 0' SIGTERM SIGINT
main