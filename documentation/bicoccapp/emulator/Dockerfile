FROM --platform=linux/amd64 ubuntu:22.04

LABEL maintainer="Android Emulator Docker"
LABEL description="Rooted Android Emulator with Magiskk, LSPosed, BicoccApp and Bypass Module"

# Prevent interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Android SDK configuration
ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH="${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/emulator"

# Java configuration
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Emulator configuration
ENV AVD_NAME=Pixel_7_Pro_API_33
ENV ANDROID_API=33
ENV DEVICE_TYPE=pixel_7_pro
ENV EMULATOR_TIMEOUT=300

# App configuration
ENV BICOCC_APP_PACKAGE=it.bicoccapp.unimib
ENV LSPOSED_MODULE_NAME=it.attendance100.bicoccapp

# Display configuration
ENV DISPLAY=:1
ENV RESOLUTION=1920x1080x24

# VNC configuration
ENV VNC_PORT=5900
ENV NOVNC_PORT=6080
ENV VNC_PASSWORD=android

# Install system dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-17-jdk \
    wget \
    curl \
    unzip \
    git \
    libgl1-mesa-glx \
    libpulse0 \
    libasound2 \
    libxcomposite1 \
    libxcursor1 \
    libxi6 \
    libxtst6 \
    libxrandr2 \
    libnss3 \
    libnspr4 \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libcups2 \
    libdrm2 \
    libxkbcommon0 \
    libxdamage1 \
    libpango-1.0-0 \
    libcairo2 \
    libgdk-pixbuf2.0-0 \
    libgtk-3-0 \
    qemu-kvm \
    libvirt-daemon-system \
    bridge-utils \
    cpu-checker \
    sqlite3 \
    xz-utils \
    socat \
    supervisor \
    procps \
    xvfb \
    x11vnc \
    fluxbox \
    net-tools \
    python3 \
    python3-numpy \
    && rm -rf /var/lib/apt/lists/*

# Install noVNC and websockify
RUN git clone --branch v1.4.0 --depth 1 https://github.com/novnc/noVNC.git /opt/noVNC && \
    git clone --branch v0.11.0 --depth 1 https://github.com/novnc/websockify /opt/noVNC/utils/websockify && \
    ln -s /opt/noVNC/vnc.html /opt/noVNC/index.html

# Create Android SDK directory
RUN mkdir -p ${ANDROID_HOME}

# Download and install Android command-line tools
RUN cd /tmp && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip && \
    unzip -q cmdline-tools.zip && \
    mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    mv cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest && \
    rm cmdline-tools.zip

# Accept licenses and install SDK components
RUN yes | sdkmanager --licenses > /dev/null 2>&1 || true

# Install platform-tools, emulator, and system image
ENV SYSTEM_IMAGE="system-images;android-${ANDROID_API};google_apis_playstore;x86_64"
ENV RAMDISK_PATH="system-images/android-${ANDROID_API}/google_apis_playstore/x86_64/ramdisk.img"

RUN sdkmanager --install "platform-tools" "emulator" "$SYSTEM_IMAGE" && \
    echo "SYSTEM_IMAGE=$SYSTEM_IMAGE" > /opt/system-image.env && \
    echo "RAMDISK_PATH=$RAMDISK_PATH" >> /opt/system-image.env

# Create AVD
RUN echo "no" | avdmanager create avd \
        -n ${AVD_NAME} \
        -k "$SYSTEM_IMAGE" \
        --device ${DEVICE_TYPE} \
        --force

# Configure AVD for better Docker compatibility
RUN mkdir -p /root/.android/avd/${AVD_NAME}.avd && \
    echo "hw.lcd.density=420" >> /root/.android/avd/${AVD_NAME}.avd/config.ini && \
    echo "hw.lcd.height=2400" >> /root/.android/avd/${AVD_NAME}.avd/config.ini && \
    echo "hw.lcd.width=1080" >> /root/.android/avd/${AVD_NAME}.avd/config.ini && \
    echo "hw.ramSize=4096" >> /root/.android/avd/${AVD_NAME}.avd/config.ini && \
    echo "hw.keyboard=yes" >> /root/.android/avd/${AVD_NAME}.avd/config.ini && \
    echo "hw.gpu.enabled=yes" >> /root/.android/avd/${AVD_NAME}.avd/config.ini && \
    echo "hw.gpu.mode=swiftshader_indirect" >> /root/.android/avd/${AVD_NAME}.avd/config.ini && \
    echo "disk.dataPartition.size=8G" >> /root/.android/avd/${AVD_NAME}.avd/config.ini

# Clone rootAVD repository
RUN git clone https://gitlab.com/newbit/rootAVD.git /opt/rootAVD

# Download LSPosed module
RUN mkdir -p /opt/modules && \
    wget -q "https://github.com/Auties00/MyBicocca/raw/refs/heads/main/documentation/bicoccapp/emulator/dependencies/LSPosed-v1.10.2-7199-zygisk-debug.zip" \
        -O /opt/modules/LSPosed.zip

# Download bypass APK
RUN wget -q "https://github.com/Auties00/MyBicocca/raw/refs/heads/main/documentation/bicoccapp/magisk/bin/bypass.apk" \
        -O /opt/modules/bypass.apk

# Download BicoccApp split APKs
RUN wget -q "https://github.com/Auties00/MyBicocca/raw/refs/heads/main/documentation/bicoccapp/emulator/dependencies/BicoccApp.zip" \
        -O /opt/modules/BicoccApp.zip && \
    mkdir -p /opt/modules/bicoccapp && \
    unzip -q /opt/modules/BicoccApp.zip -d /opt/modules/bicoccapp && \
    rm /opt/modules/BicoccApp.zip

# Create scripts directory
RUN mkdir -p /opt/scripts

# Copy entrypoint and setup scripts
COPY scripts/entrypoint.sh /opt/scripts/entrypoint.sh
COPY scripts/healthcheck.sh /opt/scripts/healthcheck.sh

RUN chmod +x /opt/scripts/*.sh

# Create VNC password file
RUN mkdir -p /root/.vnc && \
    x11vnc -storepasswd ${VNC_PASSWORD} /root/.vnc/passwd

# Expose ports
# 5554: Emulator console
# 5555: ADB connection
# 5037: ADB server
# 5556-5585: Additional emulator ports
# 5900: VNC server
# 6080: noVNC web interface
EXPOSE 5037 5554 5555 5556-5585 5900 6080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD /opt/scripts/healthcheck.sh

# Set working directory
WORKDIR /opt

# Volume for persistent AVD data
VOLUME ["/root/.android"]

# Entrypoint
ENTRYPOINT ["/opt/scripts/entrypoint.sh"]