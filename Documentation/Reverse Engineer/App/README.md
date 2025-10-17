# BicoccApp Root Detection Bypass

## Overview

The BicoccApp implements security measures to prevent it from running on rooted devices and to block HTTP traffic interception (SSL Pinning).

  * **Problem**: To bypass SSL pinning using tools like HTTP Toolkit, we need root access. However, the app detects this root access and shuts down. The app uses **Talsec** and **flutter\_jailbreak\_detection**, popular Flutter security plugins, for these checks.
  * **Solution**: We use a Frida script (`bypass.js`) to intercept and neutralize these security checks at runtime. The script hooks into the specific Flutter channels used by these plugins and blocks any "threat" events from being reported, tricking the app into thinking it's running in a secure environment.

-----

## Step-by-Step Setup

Follow these steps to prepare your environment.

### 1\. Install Dependencies

First, install the necessary tools on your computer.

  * **Install Frida Tools**
    Open Command Prompt and run:

    ```cmd
    pip install frida-tools
    ```

  * **Download Frida Server**
    Download the **`frida-server-*-android-x86_64.xz`** file from the latest [Frida GitHub Releases](https://github.com/frida/frida/releases). Use a tool like 7-Zip to extract it. You will need the unzipped `frida-server` file later.

  * **Install Android SDK Command-Line Tools**

    1.  Download the "Command line tools only" package from the [Android Studio download page](https://www.google.com/search?q=https://developer.android.com/studio%23command-line-tools-only).
    2.  Create a directory (e.g., `C:\Android\sdk`) and unzip the package contents into it. The final path should look like `C:\Android\sdk\cmdline-tools\latest\bin`.
    3.  Set the `ANDROID_HOME` environment variable and add the required tools to your system `PATH`. In PowerShell:
        ```powershell
        # Use your actual SDK path
        $sdk_path = "C:\Android\sdk"
        [System.Environment]::SetEnvironmentVariable('ANDROID_HOME', $sdk_path, 'User')
        [System.Environment]::SetEnvironmentVariable('PATH', "$($env:PATH);$sdk_path\cmdline-tools\latest\bin;$sdk_path\platform-tools", 'User')
        ```
    4.  Restart your terminal and accept the SDK licenses:
        ```cmd
        sdkmanager --licenses
        ```

  * **Install HTTP Toolkit**
    Go to the [HTTP Toolkit website](https://httptoolkit.com/) and download the installer for Windows. Follow the installation prompts.

-----

### 2\. Create the Android Emulator

Next, we'll download the system image and create the specified Android Virtual Device (AVD).

1.  **Download the System Image**

    ```cmd
    sdkmanager "system-images;android-33;google_apis;x86_64"
    ```

2.  **Create the AVD**

    ```cmd
    avdmanager create avd -n "Pixel_7_Pro_API_33" -k "system-images;android-33;google_apis;x86_64" --device "pixel_7_pro"
    ```

-----

### 3\. Root the Emulator with RootAVD

Now, we'll use the RootAVD tool and the `FAKEBOOTIMG` method to install Magisk and gain root access.

1.  **Clone RootAVD**

    ```cmd
    git clone https://gitlab.com/newbit/rootAVD.git
    cd rootAVD
    ```

2.  **Start the Emulator**
    Launch the AVD you just created. **Wait for it to fully boot** before proceeding.

    ```cmd
    emulator -avd Pixel_7_Pro_API_33
    ```

3.  **Run the RootAVD Script**
    In the `rootavd` directory, run the `rootAVD.bat` script with the `FAKEBOOTIMG` argument.

    ```cmd
    rootAVD.bat %LOCALAPPDATA%\Android\Sdk\system-images\android-33\google_apis_playstore\x86_64\ramdisk.img FAKEBOOTIMG
    ```

4.  **Patch the Image with Magisk (Inside Emulator)**
    The script will automatically install and launch the Magisk app on your emulator. You must complete the patching process within the app:

    1.  In the Magisk app, tap the **Install** button.
    2.  Select the option **"Select and Patch a File"**.
    3.  A file manager will open. Navigate to the **Download** folder and select **`fakeboot.img`**.
    4.  Tap **"LET'S GO"** and wait for Magisk to finish patching the file.
    5.  Once it says "Done\!", close the Magisk app. The script in your terminal will automatically detect the patched file and finish its process.
    6.  Reboot the emulator to apply the changes:
        ```cmd
        adb reboot
        ```

-----

### 4\. Install Frida Server

With a rooted emulator running, the next setup step is to install and run the Frida server.

1.  **Push and Run Frida Server**
    Open a new terminal. Run the following commands to copy `frida-server` to the device, set its permissions, and run it as root.

    ```cmd
    :: Replace with the actual path to your extracted frida-server
    adb push C:\path\to\frida-server /data/local/tmp/frida-server

    :: Open a shell to the device
    adb shell

    :: In the adb shell, run the following commands:
    su
    chmod 755 /data/local/tmp/frida-server
    /data/local/tmp/frida-server &
    exit
    exit
    ```

2.  **Verify Connection**
    Check that Frida can see your emulator by listing the running processes.

    ```cmd
    frida-ps -U
    ```

-----

### 5\. Configure HTTP Toolkit

Before running the app, set up HTTP Toolkit to intercept its network traffic.

1.  Launch HTTP Toolkit on your computer.

2.  In the 'Intercept' section, find and click on the **'Android device connected via ADB'** option.

3.  HTTP Toolkit will automatically detect your running emulator.

4.  A prompt will appear on your emulator asking you to install the HTTP Toolkit helper app and trust its security certificate. **Accept these prompts**.

5.  Once completed, HTTP Toolkit will start capturing all network traffic from your emulator, allowing you to inspect the app's requests.

-----

## Usage

With the environment fully configured, you can now install the app and run the bypass script.

### 1\. Install BicoccApp

1.  On your emulator, open the **Google Play Store**.
2.  Sign in with a valid Google Account.
3.  Search for and install **BicoccApp** (package name: `it.bicoccapp.unimib`).

### 2\. Run the Bypass Script

1.  Save the provided JavaScript code from the original prompt into a file named **`bypass.js`**.
2.  Run the following command in your terminal. This will launch the BicoccApp and immediately inject your script to apply the bypasses.
    ```cmd
    frida -U -l "bypass.js" -f it.bicoccapp.unimib
    ```

The app should now launch and function normally, without detecting root. All network traffic will be visible in the HTTP Toolkit window.
