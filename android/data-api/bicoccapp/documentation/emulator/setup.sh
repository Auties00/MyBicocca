#!/bin/bash

# Exit on error
set -e

# ==========================================
# CONSTANTS & CONFIGURATION
# ==========================================
AVD_NAME="Pixel_7_Pro_API_33"
ANDROID_API="33"
DEVICE_TYPE="pixel_7_pro"
BICOCC_APP_PACKAGE="it.bicoccapp.unimib"
BICOCCAPP_URL="https://github.com/Auties00/MyBicocca/raw/refs/heads/main/documentation/bicoccapp/emulator/dependencies/BicoccApp.zip"
LSPOSED_URL="https://github.com/Auties00/MyBicocca/raw/refs/heads/main/documentation/bicoccapp/emulator/dependencies/LSPosed-v1.10.2-7199-zygisk-debug.zip"
ROOT_AVD_URL="https://gitlab.com/newbit/rootAVD/-/archive/master/rootAVD-master.zip?ref_type=heads"
LSPOSED_MODULE_URL="https://github.com/Auties00/MyBicocca/raw/refs/heads/main/documentation/bicoccapp/magisk/bin/bypass.apk"
LSPOSED_MODULE_NAME="it.attendance100.bicoccapp"

# Timeouts
EMULATOR_BOOT_TIMEOUT=180
ADB_CONNECT_TIMEOUT=60

# Colors
COLOR_SUCCESS='\033[0;32m'
COLOR_ERROR='\033[0;31m'
COLOR_INFO='\033[0;36m'
COLOR_WARNING='\033[0;33m'
COLOR_RESET='\033[0m'

# Global State
DETECTED_OS=""
DETECTED_ARCH=""
SDK_ROOT=""
TEMP_DIR=""
LOG_FILE=""

# Extension helpers
EXE_EXT="" # .exe for binaries on Windows
BAT_EXT="" # .bat for scripts on Windows

# Binary Paths (Initialized in define_tool_paths)
BIN_ADB=""
BIN_EMULATOR=""
BIN_SDKMANAGER=""
BIN_AVDMANAGER=""

# ==========================================
# LOGGING & UTILITIES
# ==========================================

# Spinner characters for loading animation
SPINNER_FRAMES=("⠋" "⠙" "⠹" "⠸" "⠼" "⠴" "⠦" "⠧" "⠇" "⠏")
SPINNER_PID=""

setup_logging() {
    TEMP_DIR=$(mktemp -d)
    LOG_FILE="${TEMP_DIR}/install.log"
    touch "$LOG_FILE"
}

# Start a spinner animation
start_spinner() {
    local message="$1"
    local indent="${2:-  }"

    # Print message with spinner
    printf "${indent}${COLOR_INFO}${SPINNER_FRAMES[0]}${COLOR_RESET} ${message}..."

    # Start background spinner process
    (
        local i=0
        while true; do
            i=$(( (i + 1) % ${#SPINNER_FRAMES[@]} ))
            printf "\r${indent}${COLOR_INFO}${SPINNER_FRAMES[$i]}${COLOR_RESET} ${message}..."
            sleep 0.1
        done
    ) &

    SPINNER_PID=$!
}

# Stop spinner and show result
stop_spinner() {
    local status="$1"
    local indent="${2:-  }"
    local message="$3"

    # Kill spinner if running
    if [ -n "$SPINNER_PID" ]; then
        if kill -0 $SPINNER_PID 2>/dev/null; then
            kill -9 $SPINNER_PID 2>/dev/null || true
            wait $SPINNER_PID 2>/dev/null || true
        fi
    fi

    # Clear the entire line completely (\033[2K clears whole line)
    printf "\r\033[2K"

    # Print result with fresh line
    if [ "$status" = "success" ]; then
        echo -e "${indent}${COLOR_SUCCESS}✓${COLOR_RESET} ${message}"
    elif [ "$status" = "skip" ]; then
        echo -e "${indent}${COLOR_WARNING}⊘${COLOR_RESET} ${message}"
    else
        echo -e "${indent}${COLOR_ERROR}✗${COLOR_RESET} ${message}"
    fi

    SPINNER_PID=""
}

# Print a major section header
print_section() {
    echo
    echo -e "${COLOR_INFO}┌─────────────────────────────────────────────┐${COLOR_RESET}"
    echo -e "${COLOR_INFO}│${COLOR_RESET}${1}"
    echo -e "${COLOR_INFO}└─────────────────────────────────────────────┘${COLOR_RESET}"
}

# Execute an action with loading animation
# Usage: run_action "Description" [indent_level] command arg1 arg2 ...
run_action() {
    local msg="$1"
    local indent="  "

    # Check if second argument is aInstalling Platform Toolsn indent level
    if [[ "$2" =~ ^[0-9]+$ ]]; then
        local level="$2"
        shift 2
        # Build indent string (2 spaces per level)
        indent=$(printf '  %.0s' $(seq 1 $level))
    else
        shift
    fi

    start_spinner "$msg" "$indent"

    # Create a temporary log file for this specific action
    local action_log="${TEMP_DIR}/action_$$.log"

    # Temporarily disable exit on error for this command
    set +e
    "$@" > "$action_log" 2>&1
    local exit_code=$?
    set -e

    # Stop spinner first, before showing any error or exiting
    if [ $exit_code -eq 0 ]; then
        stop_spinner "success" "$indent" "$msg"
        return 0
    else
        stop_spinner "error" "$indent" "$msg (Exit Code: $exit_code)"

        # Show error details if there are any
        if [ -f "$action_log" ] && [ -s "$action_log" ]; then
            echo -e "${indent}${COLOR_ERROR}Error details:${COLOR_RESET}"
            sed "s/^/${indent}  /" "$action_log"
        fi

        # Exit immediately (cleanup trap will handle final message)
        exit $exit_code
    fi
}

# Print an action without executing (for manual control)
print_action() {
    local msg="$1"
    local status="$2"  # "success", "skip", or "error"
    local indent="${3:-  }"

    if [ "$status" = "success" ]; then
        echo -e "${indent}${COLOR_SUCCESS}✓${COLOR_RESET} ${msg}"
    elif [ "$status" = "skip" ]; then
        echo -e "${indent}${COLOR_WARNING}⊘${COLOR_RESET} ${msg}"
    else
        echo -e "${indent}${COLOR_ERROR}✗${COLOR_RESET} ${msg}"
    fi
}

print_header() {
    clear

    echo -e "${COLOR_SUCCESS}╔════════════════════════════════════════════╗${COLOR_RESET}"
    echo -e "${COLOR_SUCCESS}║                                            ║${COLOR_RESET}"
    echo -e "${COLOR_SUCCESS}║   ${COLOR_INFO}Android Emulator Setup Automation${COLOR_SUCCESS}   ${COLOR_RESET}"
    echo -e "${COLOR_SUCCESS}║                                            ║${COLOR_RESET}"
    echo -e "${COLOR_SUCCESS}╚════════════════════════════════════════════╝${COLOR_RESET}"
}

print_footer() {
  echo
  echo -e "${COLOR_SUCCESS}╔═══════════════════════════════════════════╗${COLOR_RESET}"
  echo -e "${COLOR_SUCCESS}║                                           ║${COLOR_RESET}"
  echo -e "${COLOR_SUCCESS}║  ${COLOR_INFO}✓ Setup Completed Successfully!${COLOR_SUCCESS}${COLOR_RESET}"
  echo -e "${COLOR_SUCCESS}║                                           ║${COLOR_RESET}"
  echo -e "${COLOR_SUCCESS}╚═══════════════════════════════════════════╝${COLOR_RESET}"
}

# ==========================================
# SYSTEM DETECTION
# ==========================================

configure_env() {
  if [ "$IS_DOCKER" = "TRUE" ]; then
    Xvfb :1 -screen 0 1920x1080x24 &
    fluxbox -display :1 &
    x11vnc -display :1 -rfbport 5900 -rfbauth /root/.vnc/passwd -forever -shared &
    /opt/noVNC/utils/novnc_proxy --vnc localhost:5900 --listen 6080 &
  fi
}

detect_system() {
    local uname_out
    uname_out="$(uname -s)"

    case "${uname_out}" in
        Linux*)
            DETECTED_OS="linux"
            ;;
        Darwin*)
            DETECTED_OS="mac"
            ;;
        CYGWIN*|MINGW*|MSYS*)
            DETECTED_OS="windows"
            EXE_EXT=".exe"
            BAT_EXT=".bat"
            ;;
        *)
            echo -e "${COLOR_ERROR}Unknown OS${COLOR_RESET}"
            exit 1
            ;;
    esac

    local arch
    arch="$(uname -m)"
    case "$arch" in
        x86_64|amd64) DETECTED_ARCH="x86_64" ;;
        arm64|aarch64) DETECTED_ARCH="arm64" ;;
        *) DETECTED_ARCH="x86_64" ;;
    esac
}

check_dependencies() {
    print_section "System Requirements"

    local missing=0
    for cmd in java unzip curl; do
        if command -v $cmd &> /dev/null; then
            print_action "Checking $cmd" "success"
        else
            print_action "Checking $cmd" "error"
            missing=1
        fi
    done

    if [ $missing -eq 1 ]; then
        exit 1
    fi

    run_action "Verifying Java version" check_java_version
}

check_java_version() {
    local java_ver_str
    local java_ver

    # Get Java version, handle potential errors
    if ! java_ver_str=$(java -version 2>&1 | head -n1); then
        echo "ERROR: Failed to execute 'java -version' command"
        return 1
    fi

    # Check if we got output
    if [ -z "$java_ver_str" ]; then
        echo "ERROR: 'java -version' returned empty output"
        return 1
    fi

    # Parse version number
    java_ver=$(echo "$java_ver_str" | sed -n 's/.*version "\([0-9]*\).*/\1/p')

    # Handle Java 8 style versioning (1.8.x)
    if [ "$java_ver" = "1" ]; then
        java_ver=$(echo "$java_ver_str" | sed -n 's/.*version "1\.\([0-9]*\).*/\1/p')
    fi

    # Validate we got a number
    if [ -z "$java_ver" ]; then
        echo "ERROR: Could not parse Java version from output: $java_ver_str"
        return 1
    fi

    if ! [[ "$java_ver" =~ ^[0-9]+$ ]]; then
        echo "ERROR: Parsed version is not a number: '$java_ver' (from: $java_ver_str)"
        return 1
    fi

    if [ "$java_ver" -lt 17 ]; then
        echo "ERROR: Java 17+ required. Found version $java_ver"
        return 1
    fi

    # Success - return 0
    return 0
}

# ==========================================
# SDK SETUP
# ==========================================

configure_sdk_paths() {
    case "$DETECTED_OS" in
        windows)
            if [ -n "$LOCALAPPDATA" ]; then
                SDK_ROOT="${LOCALAPPDATA}/Android/Sdk"
            else
                SDK_ROOT="${USERPROFILE}/AppData/Local/Android/Sdk"
            fi
            ;;
        mac)   SDK_ROOT="${HOME}/Library/Android/sdk" ;;
        linux) SDK_ROOT="${HOME}/Android/Sdk" ;;
    esac

    mkdir -p "$SDK_ROOT"

    # Export these so rootAVD can find the relative system-images path
    export ANDROID_SDK_ROOT="$SDK_ROOT"
    export ANDROID_HOME="$SDK_ROOT"
}

# Helper to update global path variables
define_tool_paths() {
    # ADB and Emulator are binaries (.exe on windows)
    BIN_ADB="${SDK_ROOT}/platform-tools/adb${EXE_EXT}"
    BIN_EMULATOR="${SDK_ROOT}/emulator/emulator${EXE_EXT}"

    # SDKManager and AVDManager are scripts (.bat on windows)
    BIN_SDKMANAGER="${SDK_ROOT}/cmdline-tools/latest/bin/sdkmanager${BAT_EXT}"
    BIN_AVDMANAGER="${SDK_ROOT}/cmdline-tools/latest/bin/avdmanager${BAT_EXT}"
}

install_sdk_tools() {
    print_section "Android SDK Tools"

    local cmdline_tools_zip="${TEMP_DIR}/cmdline-tools.zip"

    # Initial definition to check existence
    define_tool_paths

    if [ ! -f "$BIN_SDKMANAGER" ]; then
        local url=""
        case "$DETECTED_OS" in
            linux)   url="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip" ;;
            mac)     url="https://dl.google.com/android/repository/commandlinetools-mac-11076708_latest.zip" ;;
            windows) url="https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip" ;;
        esac

        run_action "Downloading SDK Command Line Tools" curl -L -o "$cmdline_tools_zip" "$url"
        run_action "Extracting SDK Tools" unzip -q "$cmdline_tools_zip" -d "${TEMP_DIR}/cmdline-extract"

        mkdir -p "${SDK_ROOT}/cmdline-tools"

        # Remove old version if exists
        if [ -d "${SDK_ROOT}/cmdline-tools/latest" ]; then
             rm -rf "${SDK_ROOT}/cmdline-tools/latest"
        fi

        mv "${TEMP_DIR}/cmdline-extract/cmdline-tools" "${SDK_ROOT}/cmdline-tools/latest"

        # Re-define paths now that files exist
        define_tool_paths
    else
        print_action "SDK Command Line Tools already installed" "skip"
    fi
}

install_sdk_components() {
    print_section "Android SDK Components"

    local arch_tag="x86_64"
    if [ "$DETECTED_ARCH" = "arm64" ]; then
        arch_tag="arm64-v8a"
    fi

    local sys_img="system-images;android-${ANDROID_API};google_apis_playstore;${arch_tag}"

    # Accept licenses
    run_action "Accepting SDK licenses" bash -c "yes | \"$BIN_SDKMANAGER\" --licenses"

    run_action "Installing Platform Tools" "$BIN_SDKMANAGER" "platform-tools"
    run_action "Installing Emulator" "$BIN_SDKMANAGER" "emulator"
    run_action "Installing System Image ($arch_tag)" "$BIN_SDKMANAGER" "$sys_img"
}

# ==========================================
# EMULATOR MANAGEMENT
# ==========================================

create_avd() {
    print_section "Android Virtual Device"

    local arch_tag="x86_64"
    if [ "$DETECTED_ARCH" = "arm64" ]; then
        arch_tag="arm64-v8a"
    fi
    local sys_img="system-images;android-${ANDROID_API};google_apis_playstore;${arch_tag}"

    # Check if AVD exists using the binary directly
    if ! "$BIN_EMULATOR" -list-avds | grep -q "^${AVD_NAME}$"; then
        # We pipe 'no' to answer "Do you wish to create a custom hardware profile?"
        run_action "Creating AVD: ${AVD_NAME}" \
            bash -c "echo no | \"$BIN_AVDMANAGER\" create avd -n '${AVD_NAME}' -k '$sys_img' --device '$DEVICE_TYPE' --force"
    else
        print_action "AVD ${AVD_NAME} already exists" "skip"
    fi
}

kill_existing_emulators() {
    # Check OS specific kill commands
    if [ "$DETECTED_OS" = "windows" ]; then
        taskkill //F //IM qemu-system-x86_64.exe > /dev/null 2>&1 || true
        taskkill //F //IM emulator.exe > /dev/null 2>&1 || true
    else
        pkill -9 emulator > /dev/null 2>&1 || true
        pkill -9 qemu-system > /dev/null 2>&1 || true
    fi
}

start_emulator() {
    run_action "Stopping existing instances" kill_existing_emulators

    local emu_args="-avd ${AVD_NAME} -writable-system -no-snapshot-load -no-boot-anim"

    if [ "${IS_DOCKER}" = "TRUE" ]; then
         emu_args="$emu_args -no-window -no-audio"
    fi

    # Start emulator in background using explicit binary path
    run_action "Launching emulator process" \
        nohup "$BIN_EMULATOR" $emu_args > "${TEMP_DIR}/emulator_out.log" 2>&1 &

    wait_for_boot
}

wait_for_boot() {
    local indent="  "
    start_spinner "Waiting for device boot" "$indent"

    local start_time
    start_time=$(date +%s)

    # 1. Wait for ADB connection
    while true; do
        if "$BIN_ADB" devices 2>/dev/null | grep -q "emulator-"; then
            break
        fi

        local current_time
        current_time=$(date +%s)
        if [ $((current_time - start_time)) -ge $ADB_CONNECT_TIMEOUT ]; then
            stop_spinner "error" "$indent" "Waiting for device boot (ADB Timeout)"
            return 1
        fi
        sleep 2
    done

    # 2. Wait for System Boot
    while true; do
        local boot_anim
        local boot_completed

        boot_anim=$("$BIN_ADB" shell getprop init.svc.bootanim 2>/dev/null | tr -d '\r')
        boot_completed=$("$BIN_ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')

        if [ "$boot_completed" = "1" ] && [ "$boot_anim" = "stopped" ]; then
            stop_spinner "success" "$indent" "Waiting for device boot"
            sleep 3
            return 0
        fi

        local current_time
        current_time=$(date +%s)
        if [ $((current_time - start_time)) -ge $EMULATOR_BOOT_TIMEOUT ]; then
            stop_spinner "error" "$indent" "Waiting for device boot (Boot Timeout)"
            return 1
        fi
        sleep 3
    done
}

wait_for_shutdown() {
    local indent="  "
    start_spinner "Waiting for shutdown" "$indent"

    local start_time
    start_time=$(date +%s)

    while "$BIN_ADB" devices 2>/dev/null | grep "emulator-" | grep -q "device"; do
        local current_time
        current_time=$(date +%s)

        if [ $((current_time - start_time)) -ge 60 ]; then
             stop_spinner "error" "$indent" "Waiting for shutdown (Timeout)"
             return 1
        fi
        sleep 2
    done

    stop_spinner "success" "$indent" "Waiting for shutdown"
}

# ==========================================
# ROOTING & MAGISK
# ==========================================

setup_root() {
    print_section "Root Access"

    local check_su
    check_su=$("$BIN_ADB" shell su -c id 2>/dev/null || echo "not_root")

    if [[ "$check_su" == *"uid=0"* ]]; then
        print_action "Device already rooted" "skip"
        return 0
    fi

    run_action "Downloading rootAVD" curl -L -o "${TEMP_DIR}/rootAVD.zip" "$ROOT_AVD_URL"
    run_action "Extracting rootAVD" unzip -q "${TEMP_DIR}/rootAVD.zip" -d "${TEMP_DIR}"

    local root_dir="${TEMP_DIR}/rootAVD-master"
    local ramdisk_path

    if [ "$DETECTED_ARCH" = "arm64" ]; then
        ramdisk_path="system-images/android-${ANDROID_API}/google_apis_playstore/arm64-v8a/ramdisk.img"
    else
        ramdisk_path="system-images/android-${ANDROID_API}/google_apis_playstore/x86_64/ramdisk.img"
    fi

    local script_cmd
    if [ "$DETECTED_OS" = "windows" ]; then
        script_cmd="${root_dir}/rootAVD.bat"
    else
        script_cmd="${root_dir}/rootAVD.sh"
        chmod +x "$script_cmd"
    fi

    # Ensure PATH has ADB for rootAVD execution
    export PATH="${SDK_ROOT}/platform-tools:$PATH"

    # Important: switch to rootAVD directory before running
    cd "$root_dir"

    run_action "Patching ramdisk with Magisk" "$script_cmd" "$ramdisk_path"

    # Return to previous directory
    cd - > /dev/null

    # Wait for device shutdown
    wait_for_shutdown

    # Restart emulator
    start_emulator
}

configure_magisk() {
    print_section "Magisk Configuration"

    # Check and configure Root Auto-Allow
    local current_su_mode
    current_su_mode=$("$BIN_ADB" shell "su -c 'magisk resetprop persist.sys.su.mode'" 2>/dev/null | tr -d '\r\n')
    if [ "$current_su_mode" != "2" ]; then
        run_action "Enabling Root Auto-Allow mode" \
            "$BIN_ADB" shell "su -c 'magisk resetprop persist.sys.su.mode 2'"
    else
        print_action "Root Auto-Allow already enabled" "skip"
    fi

    # Check and enable Zygisk
    local zygisk_status
    zygisk_status=$("$BIN_ADB" shell "su -c 'magisk --sqlite \"select value from settings where key=\\\"zygisk\\\"\"'" 2>/dev/null | tr -d '\r\n')
    if [ "$zygisk_status" != "value=1" ]; then
        run_action "Enabling Zygisk framework" \
            "$BIN_ADB" shell "su -c \"magisk --sqlite 'replace into settings (key,value) values(\\\"zygisk\\\",1);'\""
        run_action "Rebooting to apply Zygisk" "$BIN_ADB" reboot
        wait_for_boot
    else
        print_action "Zygisk already enabled" "skip"
    fi
}

fix_magisk_environment() {
    print_section "Magisk Environment Fix"

    # Grants permissions and attempts to click the "Additional Setup" OK button

    run_action "Granting notification permissions" \
        "$BIN_ADB" shell pm grant com.topjohnwu.magisk android.permission.POST_NOTIFICATIONS

    run_action "Launching Magisk application" \
        "$BIN_ADB" shell monkey -p com.topjohnwu.magisk -c android.intent.category.LAUNCHER 1

    local indent="  "
    start_spinner "Handling Additional Setup dialog" "$indent"

    local found=0
    local dump_file="${TEMP_DIR}/dump.xml"
    local device_dump="/sdcard/dump.xml"

    # Handle Git Bash path conversion issue for on-device path
    if [ "$DETECTED_OS" = "windows" ]; then
        device_dump="//sdcard/dump.xml"
    fi

    for _ in {1..20}; do
        # Remove last dump, if any
        "$BIN_ADB" shell rm -rf "$device_dump" >/dev/null 2>&1 || true

        # Dump UI on device
        "$BIN_ADB" shell uiautomator dump "$device_dump" >/dev/null 2>&1

        # Pull dump to local machine
        cd "$TEMP_DIR"
        "$BIN_ADB" pull "$device_dump" dump.xml >/dev/null 2>&1
        cd - > /dev/null

        if [ -f "$dump_file" ]; then
            # Check if the OK button text is in the file
            if grep -q 'text="OK"' "$dump_file"; then
                # Load OK button coordinates
                local coords
                coords=$(sed -nE '/text="OK"/ s/.*bounds="\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]".*/\1 \2 \3 \4/p' "$dump_file" | head -n1)
                if [ -n "$coords" ]; then
                    # Read the space-separated values directly into individual variables
                    read -r x1 y1 x2 y2 <<< "$coords"

                    # Calculate center point
                    local x=$(( (x1 + x2) / 2 ))
                    local y=$(( (y1 + y2) / 2 ))

                    # Click
                    "$BIN_ADB" shell input tap "$x" "$y" 2>/dev/null

                    # Break out
                    found=1
                    break
                fi
            fi
        fi
        sleep 2
    done

    if [ $found -eq 1 ]; then
        stop_spinner "success" "$indent" "Handling Additional Setup dialog"
        # Magisk app reboots the device automatically after this click
        wait_for_shutdown
        wait_for_boot
    else
        stop_spinner "skip" "$indent" "Handling Additional Setup dialog (Not found)"
    fi
}

# ==========================================
# MODULES & APPS
# ==========================================

install_lsposed() {
    print_section "LSPosed Framework"

    if "$BIN_ADB" shell "su -c 'ls -1 /data/adb/modules/'" 2>/dev/null | grep -q "zygisk_lsposed"; then
        print_action "LSPosed already installed" "skip"
        return
    fi

    local lsposed_source="${TEMP_DIR}/LSPosed.zip"
    run_action "Downloading LSPosed module" curl -L -o "$lsposed_source" "$LSPOSED_URL"

    local lsposed_destination="/sdcard/"
    if [ "$DETECTED_OS" = "windows" ]; then
        lsposed_destination="//sdcard//"
    fi

    cd "$TEMP_DIR"
    run_action "Pushing LSPosed to device" "$BIN_ADB" push "$lsposed_source" "$lsposed_destination"
    cd - > /dev/null

    # Try installing LSPosed. If it fails, the Magisk environment likely needs fixing.
    local indent="  "
    local lsposed_log="${TEMP_DIR}/lsposed_install.log"

    start_spinner "Installing LSPosed module" "$indent"

    if "$BIN_ADB" shell "su -c 'magisk --install-module /sdcard/LSPosed.zip'" > "$lsposed_log" 2>&1; then
        stop_spinner "success" "$indent" "Installing LSPosed module"
    else
        stop_spinner "error" "$indent" "Installing LSPosed module (Environment issue)"

        # Run the fix
        fix_magisk_environment

        echo
        print_section "LSPosed Framework (Retry)"

        start_spinner "Installing LSPosed module" "$indent"
        if "$BIN_ADB" shell "su -c 'magisk --install-module /sdcard/LSPosed.zip'" > "$lsposed_log" 2>&1; then
             stop_spinner "success" "$indent" "Installing LSPosed module"
        else
             stop_spinner "error" "$indent" "Installing LSPosed module"
             echo -e "${indent}${COLOR_ERROR}Error details:${COLOR_RESET}"
             sed "s/^/${indent}  /" "$lsposed_log"
             return 1
        fi
    fi

    run_action "Rebooting for LSPosed" "$BIN_ADB" reboot
    wait_for_boot
}

install_bicoccapp() {
    print_section "BicoccApp Installation"

    if "$BIN_ADB" shell pm list packages 2>/dev/null | grep -q "$BICOCC_APP_PACKAGE"; then
        print_action "BicoccApp already installed" "skip"
        return
    fi

    run_action "Downloading BicoccApp package" curl -L -o "${TEMP_DIR}/BicoccApp.zip" "$BICOCCAPP_URL"
    run_action "Extracting BicoccApp APKs" unzip -q "${TEMP_DIR}/BicoccApp.zip" -d "${TEMP_DIR}/bicoccapp"

    cd "${TEMP_DIR}/bicoccapp"
    local apk_list
    apk_list=$(find . -name "*.apk")

    run_action "Installing BicoccApp split APKs" "$BIN_ADB" install-multiple -r $apk_list
    cd - > /dev/null
}

setup_bypass() {
    print_section "Bypass Module Configuration"

    # Get the module list and parse it
    local module_list
    module_list=$("$BIN_ADB" shell "su -c '/data/adb/lspd/bin/cli modules ls'" 2>/dev/null)

    # Parse the output to check if module exists and its status
    local module_status
    module_status=$(echo "$module_list" | awk -v pkg="${LSPOSED_MODULE_NAME}" '
        $1 == pkg { print $NF; exit }
    ')

    # Check if module is installed
    if [ -z "$module_status" ]; then
        run_action "Downloading Bypass module APK" curl -L -o "${TEMP_DIR}/bypass.apk" "$LSPOSED_MODULE_URL"
        run_action "Installing Bypass module APK" "$BIN_ADB" install -r "${TEMP_DIR}/bypass.apk"
        module_status="disabled"
    else
        print_action "Bypass module already installed" "skip"
    fi

    # Check and set module scope if not already set
    if ! "$BIN_ADB" shell "su -c '/data/adb/lspd/bin/cli scope ls $LSPOSED_MODULE_NAME'" 2>/dev/null | grep -q "${BICOCC_APP_PACKAGE}/0"; then
        run_action "Configuring module scope" \
                 "$BIN_ADB" shell "su -c '/data/adb/lspd/bin/cli scope set -a ${LSPOSED_MODULE_NAME} ${BICOCC_APP_PACKAGE}/0'"
    else
        print_action "Module scope already configured" "skip"
    fi

    # Enable module if it's disabled
    if [ "$module_status" != "enabled" ]; then
        run_action "Enabling Bypass module" \
                 "$BIN_ADB" shell "su -c '/data/adb/lspd/bin/cli modules set -e ${LSPOSED_MODULE_NAME}'"
    else
        print_action "Bypass module already enabled" "skip"
    fi
}

# ==========================================
# MAIN EXECUTION
# ==========================================

cleanup() {
    local exit_code=$?
    local exit_signal=$1

    # Kill spinner if still running - use force kill and wait properly
    if [ -n "$SPINNER_PID" ]; then
        # Try graceful kill first
        kill -TERM $SPINNER_PID 2>/dev/null || true
        # Wait a tiny bit
        sleep 0.05 2>/dev/null || true
        # Force kill if still alive
        kill -9 $SPINNER_PID 2>/dev/null || true
        # Wait for it to die
        wait $SPINNER_PID 2>/dev/null || true
        # Clear the line and move to new line
        printf "\r\033[2K\n"
    fi

    if [ -d "$TEMP_DIR" ]; then
        rm -rf "$TEMP_DIR"
    fi

    # Show appropriate exit message
    if [ "$exit_signal" = "INT" ]; then
        echo -e "${COLOR_WARNING}╔═══════════════════════════════════════════╗${COLOR_RESET}"
        echo -e "${COLOR_WARNING}║                                           ║${COLOR_RESET}"
        echo -e "${COLOR_WARNING}║  ⊘ Setup Interrupted by User             ${COLOR_WARNING}${COLOR_RESET}"
        echo -e "${COLOR_WARNING}║                                           ║${COLOR_RESET}"
        echo -e "${COLOR_WARNING}╚═══════════════════════════════════════════╝${COLOR_RESET}"
        echo
        exit 130
    elif [ "$exit_signal" = "TERM" ]; then
        echo -e "${COLOR_WARNING}╔═══════════════════════════════════════════╗${COLOR_RESET}"
        echo -e "${COLOR_WARNING}║                                           ║${COLOR_RESET}"
        echo -e "${COLOR_WARNING}║  ⊘ Setup Terminated                      ${COLOR_WARNING}${COLOR_RESET}"
        echo -e "${COLOR_WARNING}║                                           ║${COLOR_RESET}"
        echo -e "${COLOR_WARNING}╚═══════════════════════════════════════════╝${COLOR_RESET}"
        echo
        exit 143
    elif [ $exit_code -ne 0 ]; then
        echo -e "${COLOR_ERROR}╔═══════════════════════════════════════════╗${COLOR_RESET}"
        echo -e "${COLOR_ERROR}║                                           ║${COLOR_RESET}"
        printf "${COLOR_ERROR}║  ✗ Setup Failed (Exit Code: %-3d)          ${COLOR_RESET}\n" "$exit_code"
        echo -e "${COLOR_ERROR}║                                           ║${COLOR_RESET}"
        echo -e "${COLOR_ERROR}╚═══════════════════════════════════════════╝${COLOR_RESET}"
        echo
    fi

    exit $exit_code
}

# Set up trap handlers
trap 'cleanup INT' INT
trap 'cleanup TERM' TERM
trap 'cleanup EXIT' EXIT ERR

print_header
setup_logging
configure_env
detect_system
check_dependencies
configure_sdk_paths
install_sdk_tools
define_tool_paths
install_sdk_components
create_avd
start_emulator
setup_root
configure_magisk
install_lsposed
install_bicoccapp
setup_bypass
print_footer