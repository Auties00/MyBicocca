#!/bin/bash

set -e

BOLD='\033[1m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
GRAY='\033[0;90m'
NC='\033[0m'

print_section() {
    echo ""
    echo -e "${BOLD}${CYAN}▶ $1${NC}"
    echo -e "${GRAY}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_info() {
    echo -e "${BLUE}●${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_step() {
    echo -e "${BOLD}${GREEN}[$1/$2]${NC} ${BOLD}$3${NC}"
}

command_exists() {
    command -v "$1" >/dev/null 2>&1
}

reload_env() {
    local os_type=$(uname -s)

    if [[ "$os_type" == "Linux" ]] && (grep -qi microsoft /proc/version 2>/dev/null || grep -qi wsl /proc/version 2>/dev/null); then
        local win_path=$(powershell.exe -NoProfile -NonInteractive -Command '[Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [Environment]::GetEnvironmentVariable("Path","User")' 2>/dev/null | tr -d '\r\n')
        [[ -z "$win_path" ]] && return 1
        local unix_path=""
        IFS=';' read -ra paths <<< "$win_path"
        for path in "${paths[@]}"; do
            if [[ -n "$path" ]]; then
                local converted=$(echo "$path" | sed 's|\\|/|g' | sed 's|^\([A-Za-z]\):|/\L\1|')
                [[ -n "$converted" ]] && unix_path="${unix_path}${unix_path:+:}${converted}"
            fi
        done
        export PATH="$unix_path"
    elif [[ "$os_type" == MINGW* ]] || [[ "$os_type" == MSYS* ]] || [[ "$os_type" == CYGWIN* ]]; then
        local git_bash_path=$(echo "$PATH" | tr ':' '\n' | grep -E '^/(usr|bin|mingw|c/Program Files/Git)' | tr '\n' ':' | sed 's/:$//')
        local win_path=$(powershell.exe -NoProfile -NonInteractive -Command '[Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [Environment]::GetEnvironmentVariable("Path","User")' 2>/dev/null | tr -d '\r\n')
        [[ -z "$win_path" ]] && return 1
        local unix_path=""
        IFS=';' read -ra paths <<< "$win_path"
        for path in "${paths[@]}"; do
            if [[ -n "$path" ]]; then
                local converted=$(echo "$path" | sed 's|\\|/|g' | sed 's|^\([A-Za-z]\):|/\L\1|')
                [[ -n "$converted" ]] && unix_path="${unix_path}${unix_path:+:}${converted}"
            fi
        done
        export PATH="${git_bash_path}:${unix_path}"
    elif [[ "$os_type" == "Darwin" ]]; then
        local original_path="$PATH"
        PATH="/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin"
        local files=("/etc/profile" "$HOME/.profile" "$HOME/.bash_profile" "$HOME/.bashrc" "$HOME/.zprofile" "$HOME/.zshrc")
        for file in "${files[@]}"; do
            [[ -f "$file" ]] && source "$file" 2>/dev/null
        done
        [[ -x /usr/libexec/path_helper ]] && eval "$(/usr/libexec/path_helper -s)" 2>/dev/null
        [[ -z "$PATH" ]] && PATH="$original_path" && return 1
        export PATH
    else
        local original_path="$PATH"
        PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"
        local files=("/etc/environment" "/etc/profile" "$HOME/.profile" "$HOME/.bashrc" "$HOME/.bash_profile")
        for file in "${files[@]}"; do
            [[ -f "$file" ]] && source "$file" 2>/dev/null
        done
        [[ -z "$PATH" ]] && PATH="$original_path" && return 1
        export PATH
    fi
}

add_to_path_permanent() {
    local new_path=$1

    if [[ "$OS" == "windows" ]]; then
        powershell.exe -Command "[Environment]::SetEnvironmentVariable('Path', [Environment]::GetEnvironmentVariable('Path', 'User') + ';$new_path', 'User')" >/dev/null 2>&1 || true
        export PATH="$PATH:$new_path"
    elif [[ "$OS" == "mac" ]]; then
        local shell_rc=""
        if [ -f "$HOME/.zshrc" ]; then
            shell_rc="$HOME/.zshrc"
        elif [ -f "$HOME/.bash_profile" ]; then
            shell_rc="$HOME/.bash_profile"
        fi

        if [ -n "$shell_rc" ]; then
            if ! grep -q "$new_path" "$shell_rc"; then
                echo "export PATH=\"\$PATH:$new_path\"" >> "$shell_rc"
            fi
        fi
        export PATH="$PATH:$new_path"
    else
        if [ -f "$HOME/.bashrc" ]; then
            if ! grep -q "$new_path" "$HOME/.bashrc"; then
                echo "export PATH=\"\$PATH:$new_path\"" >> "$HOME/.bashrc"
            fi
        fi
        export PATH="$PATH:$new_path"
    fi

    reload_env
}

add_env_variable_permanent() {
    local var_name=$1
    local var_value=$2
    local additional_paths=$3

    export "$var_name"="$var_value"

    if [[ "$OS" == "windows" ]]; then
        powershell.exe -Command "[Environment]::SetEnvironmentVariable('$var_name', '$var_value', 'User')" >/dev/null 2>&1 || true

        if [ -n "$additional_paths" ]; then
            IFS=':' read -ra paths <<< "$additional_paths"
            for path in "${paths[@]}"; do
                add_to_path_permanent "$path"
            done
        fi
    elif [[ "$OS" == "mac" ]]; then
        local shell_rc=""
        if [ -f "$HOME/.zshrc" ]; then
            shell_rc="$HOME/.zshrc"
        elif [ -f "$HOME/.bash_profile" ]; then
            shell_rc="$HOME/.bash_profile"
        fi

        if [ -n "$shell_rc" ] && ! grep -q "export $var_name=" "$shell_rc"; then
            echo "export $var_name=\"$var_value\"" >> "$shell_rc"

            if [ -n "$additional_paths" ]; then
                echo "export PATH=\"\$PATH:$additional_paths\"" >> "$shell_rc"
            fi
        fi

        if [ -n "$additional_paths" ]; then
            export PATH="$PATH:$additional_paths"
        fi
    else
        if [ -f "$HOME/.bashrc" ] && ! grep -q "export $var_name=" "$HOME/.bashrc"; then
            echo "export $var_name=\"$var_value\"" >> "$HOME/.bashrc"

            if [ -n "$additional_paths" ]; then
                echo "export PATH=\"\$PATH:$additional_paths\"" >> "$HOME/.bashrc"
            fi
        fi

        if [ -n "$additional_paths" ]; then
            export PATH="$PATH:$additional_paths"
        fi
    fi

    reload_env
}

ensure_winget() {
    if [[ "$OS" != "windows" ]]; then
        return 0
    fi

    if command_exists winget; then
        return 0
    fi

    print_info "Installing winget..."

    powershell.exe -ExecutionPolicy Bypass -Command "
    \$progressPreference = 'silentlyContinue'
    Install-PackageProvider -Name NuGet -Force | Out-Null
    Install-Module -Name Microsoft.WinGet.Client -Force -Repository PSGallery | Out-Null
    Repair-WinGetPackageManager -AllUsers | Out-Null
    " >/dev/null 2>&1

    reload_env

    if command_exists winget; then
        print_success "winget installed successfully"
        return 0
    else
        print_error "Failed to install winget"
        print_warning "Please install winget manually:"
        print_info "1. Download from: ${CYAN}https://github.com/microsoft/winget-cli/releases/latest${NC}"
        print_info "2. Or install from Microsoft Store: ${CYAN}https://www.microsoft.com/p/app-installer/9nblggh4nns1${NC}"
        print_info "3. After installation, restart your terminal and run this script again"
        return 1
    fi
}

install_package() {
    local package=$1
    local install_cmd=$2

    print_info "Installing $package..."

    if [[ "$OS" == "linux" ]]; then
        if command_exists apt-get; then
            sudo apt-get update -qq >/dev/null 2>&1 && sudo apt-get install -y -qq "$install_cmd" >/dev/null 2>&1
        elif command_exists yum; then
            sudo yum install -y -q "$install_cmd" >/dev/null 2>&1
        elif command_exists pacman; then
            sudo pacman -S --noconfirm "$install_cmd" >/dev/null 2>&1
        else
            print_error "No package manager found. Please install $package manually."
            return 1
        fi
    elif [[ "$OS" == "mac" ]]; then
        if command_exists brew; then
            brew install "$install_cmd" >/dev/null 2>&1
        else
            print_error "Homebrew not found. Please install it from https://brew.sh"
            return 1
        fi
    elif [[ "$OS" == "windows" ]]; then
        ensure_winget || return 1
        winget install "$install_cmd" --silent --accept-package-agreements --accept-source-agreements >/dev/null 2>&1 || true
    fi

    reload_env

    if command_exists "$package"; then
        print_success "$package installed successfully"
        return 0
    else
        print_error "Failed to install $package"
        return 1
    fi
}

spinner() {
    local pid=$1
    local message=$2
    local spinstr='⠋⠙⠹⠸⠼⠴⠦⠧⠇⠏'
    local temp

    echo -n "  "
    while kill -0 "$pid" 2>/dev/null; do
        temp=${spinstr#?}
        printf " [${CYAN}%c${NC}]  %s" "$spinstr" "$message"
        spinstr=$temp${spinstr%"$temp"}
        sleep 0.1
        printf "\r"
    done
    printf "    \r"
}

cleanup() {
    if [ -d "$TEMP_DIR" ]; then
        rm -rf "$TEMP_DIR" 2>/dev/null || true
    fi
}
trap cleanup EXIT

if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    OS="windows"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    OS="mac"
else
    OS="linux"
fi

print_section "System Tools"

print_info "Detected OS: ${BOLD}$OS${NC}"

if command_exists git; then
    print_success "git is already installed"
else
    install_package "git" "git"
fi

if command_exists curl; then
    print_success "curl is already installed"
elif command_exists wget; then
    print_success "wget is already installed"
else
    install_package "curl" "curl"
fi

if command_exists xz; then
    print_success "xz is already installed"
elif command_exists 7z; then
    print_success "7z is already installed"
else
    if [[ "$OS" == "windows" ]]; then
        install_package "7z" "7zip"
    else
        install_package "xz" "xz-utils"
    fi
fi

if command_exists unzip; then
    print_success "unzip is already installed"
else
    if [[ "$OS" == "windows" ]]; then
        print_warning "unzip not found. You may need to install it via Git Bash or use 7z."
    else
        install_package "unzip" "unzip"
    fi
fi

print_section "Python Environment"

PYTHON_CMD=""

if python3 --version >/dev/null 2>&1; then
    PYTHON_VERSION=$(python3 --version 2>&1 | grep -oP '(?<=Python )\d+' | head -1)
    if [ "$PYTHON_VERSION" == "3" ]; then
        PYTHON_CMD="python3"
        print_success "Python 3 found (python3)"
    fi
fi

if [ -z "$PYTHON_CMD" ] && python --version >/dev/null 2>&1; then
    PYTHON_VERSION=$(python --version 2>&1 | grep -oP '(?<=Python )\d+' | head -1)
    if [ "$PYTHON_VERSION" == "3" ]; then
        PYTHON_CMD="python"
        print_success "Python 3 found (python)"
    fi
fi

if [ -z "$PYTHON_CMD" ]; then
    print_info "Python 3 not found. Installing python3..."

    if [[ "$OS" == "windows" ]]; then
        install_package "python3" "python3.12"

        PYTHON_INSTALL_PATH="$LOCALAPPDATA\\Programs\\Python"
        if [ -d "$PYTHON_INSTALL_PATH" ]; then
            PYTHON_VER_DIR=$(ls -d "$PYTHON_INSTALL_PATH"/Python3* 2>/dev/null | head -1)
            if [ -n "$PYTHON_VER_DIR" ]; then
                add_to_path_permanent "$PYTHON_VER_DIR"
                add_to_path_permanent "$PYTHON_VER_DIR\\Scripts"
            fi
        fi

        PYTHON_APPDATA="$LOCALAPPDATA\\Microsoft\\WindowsApps"
        if [ -d "$PYTHON_APPDATA" ]; then
            add_to_path_permanent "$PYTHON_APPDATA"
        fi
    else
        install_package "python3" "python3"

        if [[ "$OS" == "mac" ]]; then
            [ -d "/usr/local/bin" ] && add_to_path_permanent "/usr/local/bin"
            [ -d "$HOME/Library/Python/3.12/bin" ] && add_to_path_permanent "$HOME/Library/Python/3.12/bin"
            [ -d "$HOME/Library/Python/3.11/bin" ] && add_to_path_permanent "$HOME/Library/Python/3.11/bin"
        else
            [ -d "$HOME/.local/bin" ] && add_to_path_permanent "$HOME/.local/bin"
        fi
    fi

    if python3 --version >/dev/null 2>&1; then
        PYTHON_CMD="python3"
        print_success "Python 3 installed successfully"
    elif python --version >/dev/null 2>&1; then
        PYTHON_CMD="python"
        print_success "Python 3 installed successfully"
    else
        print_error "Failed to install Python 3"
        if [[ "$OS" == "windows" ]]; then
            print_info "Please install Python 3 manually from: ${CYAN}https://www.python.org/downloads/${NC}"
            print_warning "Make sure to check 'Add Python to PATH' during installation"
            print_warning "After installation, restart your terminal and run this script again"
        fi
        exit 1
    fi
fi

if ! $PYTHON_CMD -m pip --version >/dev/null 2>&1; then
    print_info "Installing pip..."
    $PYTHON_CMD -m ensurepip --upgrade >/dev/null 2>&1

    if ! $PYTHON_CMD -m pip --version >/dev/null 2>&1; then
        print_error "Failed to install pip"
        print_info "Please install pip manually: ${CYAN}$PYTHON_CMD -m ensurepip --upgrade${NC}"
        exit 1
    fi
fi

PYTHON_VERSION_FULL=$($PYTHON_CMD --version 2>&1)
print_success "Python 3 environment ready ($PYTHON_VERSION_FULL)"

print_section "Frida Tools"

if command_exists frida; then
    CURRENT_FRIDA=$($PYTHON_CMD -m pip show frida-tools 2>/dev/null | grep Version | cut -d' ' -f2)
    print_success "frida-tools already installed (v$CURRENT_FRIDA)"
else
    print_info "Installing frida-tools (this may take a minute)..."
    $PYTHON_CMD -m pip install --quiet frida-tools >/dev/null 2>&1
    reload_env

    if command_exists frida; then
      print_success "frida-tools installed successfully"
    else
      print_error "frida-tools installation failed"
      print_warning "Try manually: ${CYAN}$PYTHON_CMD -m pip install frida-tools${NC}"
      exit 1
    fi
fi

FRIDA_VERSION=$(frida --version 2>/dev/null || echo "17.5.1")
print_info "Frida version: ${BOLD}$FRIDA_VERSION${NC}"

print_section "Java Development Kit"

if java -version >/dev/null 2>&1; then
    JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
    print_success "Java is already installed (version $JAVA_VERSION)"
else
    print_info "Java not found. Installing JDK..."

    if [[ "$OS" == "windows" ]]; then
        install_package "Java" "Oracle.JDK.17"
    elif [[ "$OS" == "mac" ]]; then
        install_package "Java" "openjdk@17"
    else
        javaPackageName
        if command_exists apt-get; then
            javaPackageName="openjdk-17-jdk"
        elif command_exists yum; then
            javaPackageName="java-17-openjdk-devel"
        elif command_exists pacman; then
            javaPackageName="jdk17-openjdk"
        fi

        install_package "Java" "$javaPackageName"
    fi

    if java -version >/dev/null 2>&1; then
        JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
        print_success "Java installed successfully (version $JAVA_VERSION)"
        print_info "JAVA_HOME: $JAVA_HOME"
    else
        print_error "Failed to install Java"
        exit 1
    fi
fi

    if [ -z "$JAVA_HOME" ]; then
        if [[ "$OS" == "windows" ]]; then
            JAVA_PATH=$(where java 2>/dev/null | head -1)
            if [ -n "$JAVA_PATH" ]; then
                JAVA_HOME=$(echo "$JAVA_PATH" | sed 's|\\|/|g')
            fi
        elif [[ "$OS" == "mac" ]]; then
            JAVA_HOME=$(/usr/libexec/java_home 2>/dev/null)
        else
            JAVA_HOME=$(readlink -f $(which java) | sed "s:/bin/java::")
        fi

        if [ -n "$JAVA_HOME" ]; then
            add_env_variable_permanent "JAVA_HOME" "$JAVA_HOME" "\$JAVA_HOME/bin"
            print_info "JAVA_HOME set to: $JAVA_HOME"
        fi
    else
        print_success "JAVA_HOME already configured: $JAVA_HOME"
    fi

print_section "Android SDK Command-Line Tools"

if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    ANDROID_SDK_DEFAULT="$LOCALAPPDATA/Android/Sdk"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    ANDROID_SDK_DEFAULT="$HOME/Library/Android/sdk"
else
    ANDROID_SDK_DEFAULT="$HOME/Android/Sdk"
fi

if [ -z "$ANDROID_HOME" ]; then
    ANDROID_HOME="$ANDROID_SDK_DEFAULT"
    ANDROID_PATHS="\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools:\$ANDROID_HOME/emulator"
    add_env_variable_permanent "ANDROID_HOME" "$ANDROID_HOME" "$ANDROID_PATHS"
    print_info "ANDROID_HOME set to: $ANDROID_HOME"
else
    print_success "ANDROID_HOME already configured: $ANDROID_HOME"
fi

if ! command_exists sdkmanager; then
    print_info "Installing Command-Line Tools..."

    mkdir -p "$ANDROID_HOME/cmdline-tools"

    if [[ "$OS" == "linux" ]]; then
        SDK_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
    elif [[ "$OS" == "mac" ]]; then
        SDK_URL="https://dl.google.com/android/repository/commandlinetools-mac-11076708_latest.zip"
    elif [[ "$OS" == "windows" ]]; then
        SDK_URL="https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
    fi

    SDK_ZIP="$TEMP_DIR/cmdline-tools.zip"

    if command_exists curl; then
        curl -L "$SDK_URL" -o "$SDK_ZIP" 2>&1
    elif command_exists wget; then
        wget -q "$SDK_URL" -O "$SDK_ZIP" 2>&1
    fi

    print_info "Extracting command-line tools..."
    if command_exists unzip; then
        unzip -q "$SDK_ZIP" -d "$TEMP_DIR"
    elif command_exists 7z; then
        7z x "$SDK_ZIP" -o"$TEMP_DIR" >/dev/null 2>&1
    else
        print_error "No extraction tool found (unzip or 7z)"
        exit 1
    fi

    if [ -d "$ANDROID_HOME/cmdline-tools/latest" ]; then
        rm -rf "$ANDROID_HOME/cmdline-tools/latest"
    fi

    mv "$TEMP_DIR/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"

    print_info "Configuring command-line tools..."
    (yes | sdkmanager "platform-tools" >/dev/null 2>&1) || true
    (yes | sdkmanager "emulator" >/dev/null 2>&1)  || true

    print_info "Adding command-line tools to the path..."
    ANDROID_PATHS="\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools:\$ANDROID_HOME/emulator"
    add_env_variable_permanent "ANDROID_HOME" "$ANDROID_HOME" "$ANDROID_PATHS"
    add_to_path_permanent "$ANDROID_HOME/cmdline-tools/latest/bin"

    if command_exists sdkmanager; then
        print_success "Command-Line Tools installed successfully"
    else
        print_error "Failed to install Command-Line Tools"
        exit 1
    fi
fi

print_section "SDK Licenses"

print_info "Accepting Android SDK licenses..."
yes 2>/dev/null | sdkmanager --licenses >/dev/null 2>&1 || true
print_success "SDK licenses accepted"

print_section "System Image"

if sdkmanager --list_installed 2>/dev/null | grep -q "system-images;android-33;google_apis_playstore;x86_64"; then
    print_success "System image already installed"
else
    print_info "Downloading Android 13 (API 33) system image..."
    print_warning "This may take several minutes..."

    if sdkmanager "system-images;android-33;google_apis_playstore;x86_64" >/dev/null 2>&1; then
        print_success "System image downloaded successfully"
    else
        print_error "Failed to download system image"
        print_info "Please check your internet connection and try again"
        exit 1
    fi
fi

print_section "Virtual Device"

AVD_NAME="Pixel_7_Pro_API_33"
if [ ! -d "$HOME/.android/avd/${AVD_NAME}.avd" ]; then
    print_info "Creating AVD: ${BOLD}$AVD_NAME${NC}"
    echo "no" | avdmanager create avd -n "$AVD_NAME" -k "system-images;android-33;google_apis_playstore;x86_64" --device "pixel_7_pro" >/dev/null 2>&1
    print_success "AVD created successfully"
else
    print_success "AVD already exists"
fi

print_section "Starting Emulator"

print_info "Launching emulator (this may take a few minutes)..."

EMULATOR_BIN="$ANDROID_HOME/emulator/emulator"
if [[ "$OS" == "windows" ]]; then
    EMULATOR_BIN="$ANDROID_HOME/emulator/emulator.exe"
fi

if [ ! -f "$EMULATOR_BIN" ]; then
    print_error "Emulator binary not found at: $EMULATOR_BIN"
    print_info "Please restart your terminal to refresh PATH, then run the script again"
    exit 1
fi

if ! command_exists adb; then
    print_error "adb not found in PATH"
    print_info "Please restart your terminal to refresh PATH, then run the script again"
    exit 1
fi

if [[ "$OS" == "windows" ]]; then
    taskkill //F //IM emulator.exe >/dev/null 2>&1 || true
    taskkill //F //IM qemu-system-x86_64.exe >/dev/null 2>&1 || true
else
    pkill -9 -f "emulator.*avd" >/dev/null 2>&1 || true
    pkill -9 qemu-system >/dev/null 2>&1 || true
fi
sleep 3

adb kill-server >/dev/null 2>&1 || true
adb start-server >/dev/null 2>&1
sleep 2

print_info "Starting AVD: ${BOLD}$AVD_NAME${NC}"

if [[ "$OS" == "windows" ]]; then
    start "" "$EMULATOR_BIN" -avd "$AVD_NAME" -writable-system -no-snapshot-load >NUL 2>&1 &
else
    nohup "$EMULATOR_BIN" -avd "$AVD_NAME" -writable-system -no-snapshot-load >/dev/null 2>&1 &
    disown 2>/dev/null || true
fi

sleep 5

print_info "Waiting for device to be detected..."
TIMEOUT=180
ELAPSED=0
until adb devices 2>/dev/null | grep -q "emulator"; do
    if [ $ELAPSED -ge $TIMEOUT ]; then
        print_error "Timeout waiting for emulator to start"
        print_warning "Troubleshooting:"
        print_info "1. Check if the emulator window opened"
        print_info "2. Check if Intel HAXM or AMD Hypervisor is installed"
        print_info "3. Check if virtualization (VT-x/AMD-V) is enabled in BIOS"
        exit 1
    fi
    sleep 3
    ELAPSED=$((ELAPSED + 3))
    echo -ne "  ${CYAN}◐${NC} Waiting... (${ELAPSED}s/${TIMEOUT}s)\r"
done
echo -ne "\r\033[K"

adb wait-for-device >/dev/null 2>&1
print_success "Emulator detected"
print_info "Waiting for full boot..."

while [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
    sleep 0.5
done
echo -ne "\r\033[K"

print_success "Emulator fully booted"
sleep 5

print_section "Checking Root Status"

ROOT_CHECK=$(adb shell "su -c 'id'" 2>/dev/null | grep -i "uid=0" || echo "")

if [ -n "$ROOT_CHECK" ]; then
    print_success "Device is already rooted!"
else
    print_info "Installing Magisk..."

    ROOTAVD_LOCAL="C:/Users/Alessandro Autiero/rootAVD"
    if [ -d "$ROOTAVD_LOCAL" ]; then
        print_success "Found local rootAVD installation"
        ROOTAVD_DIR="$ROOTAVD_LOCAL"
    else
        ROOTAVD_DIR="$TEMP_DIR/rootAVD"
        git clone -q --depth 1 https://gitlab.com/newbit/rootAVD.git "$ROOTAVD_DIR" >/dev/null 2>&1
        print_success "rootAVD downloaded"
    fi

    print_section "Patching Ramdisk"

    RAMDISK_PATH="$ANDROID_HOME/system-images/android-33/google_apis/x86_64/ramdisk.img"
    WORK_DIR="$TEMP_DIR/magisk_work"
    mkdir -p "$WORK_DIR"
    cd "$WORK_DIR"

    MAGISK_APK=$(find "$ROOTAVD_DIR" -name "*.apk" -type f | head -1)
    if [ -z "$MAGISK_APK" ]; then
        print_error "Magisk APK not found in rootAVD directory"
        exit 1
    fi

    if command_exists unzip; then
        unzip -q "$MAGISK_APK" "lib/x86_64/*" -d "$WORK_DIR" 2>/dev/null || true
        unzip -q "$MAGISK_APK" "assets/*" -d "$WORK_DIR" 2>/dev/null || true
    fi

    MAGISKBOOT=$(find "$WORK_DIR" -name "*magiskboot*" -o -name "boot" -type f | head -1)
    if [ -z "$MAGISKBOOT" ]; then
        if [ -f "$ROOTAVD_DIR/magiskboot" ]; then
            cp "$ROOTAVD_DIR/magiskboot" "$WORK_DIR/"
            MAGISKBOOT="$WORK_DIR/magiskboot"
        else
            print_error "magiskboot not found"
            exit 1
        fi
    fi
    chmod +x "$MAGISKBOOT"

    cp "$RAMDISK_PATH" "$WORK_DIR/ramdisk.img"

    print_info "Unpacking ramdisk..."
    "$MAGISKBOOT" unpack "$WORK_DIR/ramdisk.img" >/dev/null 2>&1

    print_info "Patching ramdisk with Magisk..."

    for file in $(find "$WORK_DIR" -name "lib*.so" -type f); do
        newname=$(basename "$file" | sed 's/^lib//;s/.so$//')
        cp "$file" "$WORK_DIR/$newname"
        chmod +x "$WORK_DIR/$newname"
    done

    "$MAGISKBOOT" cpio ramdisk.cpio \
        "add 0750 init magiskinit" \
        "mkdir 0750 overlay.d" \
        "mkdir 0750 overlay.d/sbin" \
        "add 0644 overlay.d/sbin/magisk32.xz magisk32" \
        "add 0644 overlay.d/sbin/magisk64.xz magisk64" \
        "patch" \
        "backup ramdisk.cpio.orig" >/dev/null 2>&1

    print_info "Repacking ramdisk..."
    "$MAGISKBOOT" repack "$WORK_DIR/ramdisk.img" >/dev/null 2>&1

    print_info "Installing patched ramdisk..."
    adb push "$WORK_DIR/new-boot.img" /data/local/tmp/ramdisk.img >/dev/null 2>&1
    adb shell "su -c 'cp /data/local/tmp/ramdisk.img /system/system-images/android-33/google_apis/x86_64/ramdisk.img'" 2>/dev/null || true

    print_success "Ramdisk patched successfully"

    print_section "Finalizing Root"

    print_info "Rebooting emulator..."
    adb reboot >/dev/null 2>&1

    sleep 10
    adb wait-for-device >/dev/null 2>&1

    while [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]; do
        echo -ne "  ${CYAN}◐${NC} Rebooting...\r"
        sleep 0.5
        echo -ne "  ${CYAN}◓${NC} Rebooting...\r"
        sleep 0.5
        echo -ne "  ${CYAN}◑${NC} Rebooting...\r"
        sleep 0.5
        echo -ne "  ${CYAN}◒${NC} Rebooting...\r"
        sleep 0.5
    done
    echo -ne "\r\033[K"

    print_success "Emulator rebooted successfully"
    sleep 5

    print_section "Verifying Root Access"
    ROOT_VERIFY=$(adb shell "su -c 'id'" 2>/dev/null | grep -i "uid=0" || echo "")

    if [ -n "$ROOT_VERIFY" ]; then
        print_success "Root access verified!"
    else
        print_error "Root verification failed"
        print_warning "Device may not be properly rooted"
    fi

    cd - >/dev/null
fi

print_section "Installing LSPosed"

LSPOSED_INSTALLED=$(adb shell pm list packages 2>/dev/null | grep -iE "lsposed|manager" || echo "")

if [ -n "$LSPOSED_INSTALLED" ]; then
    print_success "LSPosed already installed"
else
    print_info "Installing LSPosed..."

    LSPOSED_ZIP="$TEMP_DIR/LSPosed.zip"
    LSPOSED_EXTRACT_DIR="$TEMP_DIR/lsposed_extracted"
    LSPOSED_APK="$TEMP_DIR/LSPosed.apk"
    LSPOSED_URL="https://github.com/mywalkb/LSPosed_mod/releases/download/v1.9.3_mod/LSPosed-v1.9.3_mod-7244-zygisk-release.zip"

    if command_exists curl; then
        curl -L "$LSPOSED_URL" -o "$LSPOSED_ZIP"
    elif command_exists wget; then
        wget -q "$LSPOSED_URL" -O "$LSPOSED_ZIP"
    fi

    if [ ! -f "$LSPOSED_ZIP" ]; then
        print_error "Failed to download LSPosed"
        exit 1
    fi

    print_info "Extracting LSPosed..."
    mkdir -p "$LSPOSED_EXTRACT_DIR"

    if command_exists unzip; then
        unzip -q "$LSPOSED_ZIP" -d "$LSPOSED_EXTRACT_DIR"
    elif command_exists 7z; then
        7z x "$LSPOSED_ZIP" -o"$LSPOSED_EXTRACT_DIR" >/dev/null 2>&1
    else
        print_error "No extraction tool found (unzip or 7z)"
        exit 1
    fi

    LSPOSED_EXTRACTED_APK=$(find "$LSPOSED_EXTRACT_DIR" -name "*.apk" -type f | head -1)

    if [ -n "$LSPOSED_EXTRACTED_APK" ]; then
        cp "$LSPOSED_EXTRACTED_APK" "$LSPOSED_APK"
        print_success "LSPosed APK extracted"
    else
        print_error "Could not find APK in downloaded ZIP"
        exit 1
    fi

    print_info "Installing LSPosed on device..."
    INSTALL_OUTPUT=$(adb install -r "$LSPOSED_APK" 2>&1)
    INSTALL_RESULT=$?

    if [ $INSTALL_RESULT -eq 0 ]; then
        print_success "LSPosed installed successfully"
    else
        print_error "Failed to install LSPosed"
        echo "$INSTALL_OUTPUT"
        exit 1
    fi

    sleep 2
    VERIFY_INSTALL=$(adb shell pm list packages 2>/dev/null | grep -iE "lsposed|manager" || echo "")
    if [ -n "$VERIFY_INSTALL" ]; then
        print_success "Installation verified"
    fi
fi

print_section "BicoccApp Bypass Module"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BYPASS_APK="$SCRIPT_DIR/magisk/bin/bypass.apk"

if [ ! -f "$BYPASS_APK" ]; then
    print_error "Bypass module not found at: $BYPASS_APK"
    exit 1
fi

BYPASS_INSTALLED=$(adb shell pm list packages | grep -i "bypass" || echo "")

if [ -n "$BYPASS_INSTALLED" ]; then
    print_success "Bypass module already installed"
else
    print_info "Installing bypass module..."
    adb install -r "$BYPASS_APK" >/dev/null 2>&1

    if [ $? -eq 0 ]; then
        print_success "Bypass module installed successfully"
    else
        print_error "Failed to install bypass module"
        exit 1
    fi
fi

print_section "Activating Module"

print_info "Attempting to activate bypass module..."

BYPASS_PACKAGE=$(adb shell pm list packages | grep -i bypass | cut -d: -f2 | tr -d '\r\n')

if [ -n "$BYPASS_PACKAGE" ]; then
    ACTIVATION_RESULT=$(adb shell "su -c 'am broadcast -a org.lsposed.manager.ENABLE_MODULE -e package $BYPASS_PACKAGE -e target it.bicoccapp.unimib'" 2>/dev/null || echo "")

    if [ -n "$ACTIVATION_RESULT" ]; then
        print_success "Module activation attempted via broadcast"
    else
        print_warning "Automatic activation not available"
        echo ""
        echo -e "${BOLD}Please complete these steps manually:${NC}"
        echo -e "  ${BOLD}1.${NC} Open ${BOLD}LSPosed Manager${NC} app on the emulator"
        echo -e "  ${BOLD}2.${NC} Go to ${BOLD}Modules${NC} section"
        echo -e "  ${BOLD}3.${NC} Enable the ${BOLD}$BYPASS_PACKAGE${NC} module"
        echo -e "  ${BOLD}4.${NC} Select ${BOLD}it.bicoccapp.unimib${NC} as the target app"
        echo -e "  ${BOLD}5.${NC} Reboot the device if prompted"
        echo ""
        read -p "$(echo -e ${BOLD}Press Enter once you have completed these steps...${NC}) "
    fi
else
    print_error "Could not determine bypass package name"
fi

print_section "Installing Frida Server"

if frida-ps -U >/dev/null 2>&1; then
    print_success "Frida server is already running!"
else
    FRIDA_EXISTS=$(adb shell "su -c 'test -f /data/local/tmp/frida-server && echo yes || echo no'" 2>/dev/null | tr -d '\r\n')

    if [ "$FRIDA_EXISTS" == "yes" ]; then
        print_info "frida-server found on device, starting..."

        FRIDA_RUNNING=$(adb shell "su -c 'pgrep -f frida-server'" 2>/dev/null | tr -d '\r\n')

        if [ -n "$FRIDA_RUNNING" ]; then
            adb shell "su -c 'pkill -9 frida-server'" 2>/dev/null
            sleep 2
        fi

        adb shell "su -c '/data/local/tmp/frida-server &'" 2>/dev/null &
        sleep 5
    else
        print_info "Downloading Frida server..."

        FRIDA_SERVER_URL="https://github.com/frida/frida/releases/download/$FRIDA_VERSION/frida-server-$FRIDA_VERSION-android-x86_64.xz"
        FRIDA_SERVER_XZ="$TEMP_DIR/frida-server.xz"
        FRIDA_SERVER_BIN="$TEMP_DIR/frida-server"

        if command_exists curl; then
            curl -L "$FRIDA_SERVER_URL" -o "$FRIDA_SERVER_XZ"
        elif command_exists wget; then
            wget -q "$FRIDA_SERVER_URL" -O "$FRIDA_SERVER_XZ"
        fi

        print_info "Extracting frida-server..."
        if command_exists xz; then
            xz -d "$FRIDA_SERVER_XZ"
        elif command_exists 7z; then
            7z x "$FRIDA_SERVER_XZ" -o"$TEMP_DIR" >/dev/null 2>&1
            rm "$FRIDA_SERVER_XZ"
        fi

        chmod +x "$FRIDA_SERVER_BIN"

        print_info "Installing frida-server on device..."
        adb push "$FRIDA_SERVER_BIN" /data/local/tmp/frida-server >/dev/null 2>&1
        adb shell "su -c 'chmod 755 /data/local/tmp/frida-server'" >/dev/null 2>&1
        adb shell "su -c '/data/local/tmp/frida-server &'" >/dev/null 2>&1 &
        sleep 5
    fi

    print_section "Verification"

    CONNECTED=false
    for i in {1..3}; do
        if frida-ps -U >/dev/null 2>&1; then
            CONNECTED=true
            break
        fi
        if [ $i -lt 3 ]; then
            sleep 3
        fi
    done

    if [ "$CONNECTED" = true ]; then
        print_success "Frida server is running!"
        PROCESS_COUNT=$(frida-ps -U 2>/dev/null | wc -l)
        print_info "Detected $((PROCESS_COUNT - 1)) running processes"
    else
        print_error "Failed to connect to frida-server"
        print_warning "You may need to start it manually: ${CYAN}adb shell su -c /data/local/tmp/frida-server &${NC}"
    fi
fi

print_section "Installing BicoccApp"

BICOCAPP_INSTALLED=$(adb shell pm list packages | grep "it.bicoccapp.unimib" || echo "")

if [ -n "$BICOCAPP_INSTALLED" ]; then
    print_success "BicoccApp is already installed"
else
    print_info "Opening Play Store for BicoccApp..."

    PLAYSTORE_URL="https://play.google.com/store/apps/details?id=it.bicoccapp.unimib&hl=it"
    adb shell am start -a android.intent.action.VIEW -d "$PLAYSTORE_URL" >/dev/null 2>&1

    echo ""
    echo -e "${BOLD}${YELLOW}╔════════════════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BOLD}${YELLOW}║  ⚠  MANUAL ACTION REQUIRED${NC}                                                "
    echo -e "${BOLD}${YELLOW}╚════════════════════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${BOLD}Please complete these steps on the emulator:${NC}"
    echo ""
    echo -e "  ${BOLD}1.${NC} Log in to the ${BOLD}Google Play Store${NC} if prompted"
    echo -e "  ${BOLD}2.${NC} Tap the ${BOLD}Install${NC} button for BicoccApp"
    echo -e "  ${BOLD}3.${NC} Wait for the installation to complete"
    echo ""
    echo -e "${GRAY}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    read -p "$(echo -e ${BOLD}Press Enter once BicoccApp is installed...${NC}) "

    BICOCAPP_CHECK=$(adb shell pm list packages | grep "it.bicoccapp.unimib" || echo "")

    if [ -n "$BICOCAPP_CHECK" ]; then
        print_success "BicoccApp installation verified!"
    else
        print_error "BicoccApp not found after installation"
        exit 1
    fi
fi

print_section "Launching BicoccApp"
print_info "Opening BicoccApp..."

adb shell monkey -p it.bicoccapp.unimib -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
sleep 3

print_success "BicoccApp launched successfully"

print_section "Setup Complete!"
echo -e "  ${BOLD}AVD Name:${NC}           $AVD_NAME"
echo -e "  ${BOLD}Android Version:${NC}    13 (API 33)"
echo -e "  ${BOLD}Device:${NC}             Pixel 7 Pro"
echo -e "  ${BOLD}Root Method:${NC}        Magisk (via RootAVD)"
echo -e "  ${BOLD}LSPosed:${NC}            Installed"
echo -e "  ${BOLD}Frida Version:${NC}      $FRIDA_VERSION"
echo -e "  ${BOLD}BicoccApp:${NC}          Installed and Running"