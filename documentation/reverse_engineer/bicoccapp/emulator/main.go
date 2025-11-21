package main

import (
	"archive/zip"
	"bufio"
	"encoding/xml"
	"errors"
	"fmt"
	"io"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
	"strconv"
	"strings"
	"syscall"
	"time"

	"github.com/charmbracelet/lipgloss"
)

const (
	AVDName           = "Pixel_7_Pro_API_33"
	AndroidAPI        = "33"
	DeviceType        = "pixel_7_pro"
	BicoccAppPackage  = "it.bicoccapp.unimib"
	LSPosedURL        = "https://github.com/Auties00/MyBicocca/raw/refs/heads/main/documentation/reverse_engineer/bicoccapp/emulator/dependencies/LSPosed-v1.10.2-7199-zygisk-debug.zip"
	RootAVDRepo       = "https://gitlab.com/newbit/rootAVD.git"
	EmulatorTimeout   = 120 // Reduced from 180
	CreateNewConsole  = 0x10
	LSPosedModuleUrl  = "https://github.com/Auties00/MyBicocca/raw/refs/heads/main/documentation/reverse_engineer/bicoccapp/magisk/bin/bypass.apk"
	LSPosedModuleName = "it.attendance100.bicoccapp"
)

var (
	detectedOS      string
	detectedArch    string
	tempDir         string
	androidHome     string
	javaHome        string
	pythonCmd       string
	systemImage     string
	fridaServerArch string
	ramdiskPath     string
	rootAVDDir      string
	startTime       time.Time
)

var (
	colorSuccess    = lipgloss.NewStyle().Foreground(lipgloss.Color("#04B575"))
	colorError      = lipgloss.NewStyle().Foreground(lipgloss.Color("#FF0000"))
	colorStep       = lipgloss.NewStyle().Foreground(lipgloss.Color("#00AAFF")).Bold(true)
	colorSub        = lipgloss.NewStyle().Foreground(lipgloss.Color("#888888"))
	colorSubWarning = lipgloss.NewStyle().Foreground(lipgloss.Color("#FFFF00"))
	colorInfo       = lipgloss.NewStyle().Foreground(lipgloss.Color("#FFFFFF"))
	colorTiming     = lipgloss.NewStyle().Foreground(lipgloss.Color("#888888")).Italic(true)
)

type CommandError struct {
	Command string
	Args    []string
	Output  string
	Err     error
}

func (e *CommandError) Error() string {
	var sb strings.Builder
	sb.WriteString(fmt.Sprintf("Command failed: %s", e.Command))
	if len(e.Args) > 0 {
		sb.WriteString(" " + strings.Join(e.Args, " "))
	}
	if e.Err != nil {
		sb.WriteString(fmt.Sprintf(" (Error: %v)", e.Err))
	}
	return sb.String()
}

func getElapsedTime() string {
	elapsed := time.Since(startTime)
	return fmt.Sprintf("[%02d:%02d]", int(elapsed.Minutes()), int(elapsed.Seconds())%60)
}

func printInfo(message string) {
	fmt.Println(colorStep.Render(fmt.Sprintf("\n[+] %s %s", message, colorTiming.Render(getElapsedTime()))))
}

func printError(message string) {
	fmt.Println(colorError.Render(fmt.Sprintf("[!] %s %s", message, colorTiming.Render(getElapsedTime()))))
}

func printSubInfo(message string) {
	fmt.Println(colorSub.Render(fmt.Sprintf("  [+] %s", message)))
}

func printSubWarning(message string) {
	fmt.Println(colorSubWarning.Render(fmt.Sprintf("  [!] %s", message)))
}

func printComplete() {
	elapsed := time.Since(startTime)
	printInfo(fmt.Sprintf("Configuration complete (%.1f minutes)", elapsed.Minutes()))
	printSubInfo("AVD Name: " + AVDName)
	printSubInfo("Android Version: 13 (API " + AndroidAPI + ")")
	printSubInfo("Device: Pixel 7 Pro")
	printSubInfo("Architecture: " + detectedArch)
}

func runCommand(name string, args ...string) (string, error) {
	cmd := exec.Command(name, args...)
	var stdout strings.Builder
	cmd.Stdout = &stdout
	cmd.Stderr = &stdout

	err := cmd.Run()
	if err != nil {
		return "", &CommandError{
			Command: name,
			Args:    args,
			Output:  strings.TrimSpace(stdout.String()),
			Err:     err,
		}
	}
	return stdout.String(), nil
}

func runDetachedCommand(name string, args ...string) (*exec.Cmd, error) {
	var cmd *exec.Cmd

	commandBuilder := strings.Builder{}
	commandBuilder.WriteString(name)
	for _, arg := range args {
		commandBuilder.WriteString(" ")
		commandBuilder.WriteString(arg)
	}
	command := commandBuilder.String()

	switch runtime.GOOS {
	case "linux":
		cmdStr := fmt.Sprintf("%s; echo ''; echo 'Press Enter to close...'; read", command)
		terminals := []struct {
			name string
			args []string
		}{
			{"x-terminal-emulator", []string{"-e", "bash", "-c", cmdStr}},
			{"gnome-terminal", []string{"--", "bash", "-c", cmdStr}},
			{"mate-terminal", []string{"-e", "bash", "-c", cmdStr}},
			{"konsole", []string{"-e", "bash", "-c", cmdStr}},
			{"xfce4-terminal", []string{"-e", "bash", "-c", cmdStr}},
			{"xterm", []string{"-e", "bash", "-c", cmdStr}},
		}

		for _, term := range terminals {
			if _, err := exec.LookPath(term.name); err == nil {
				cmd = exec.Command(term.name, term.args...)
				break
			}
		}

		if cmd == nil {
			return nil, fmt.Errorf("no terminal emulator found")
		}

	case "darwin":
		script := fmt.Sprintf(`tell application "Terminal"
    activate
    do script "%s; echo ''; echo 'Press Enter to close...'; read"
end tell`, escapeAppleScript(command))
		cmd = exec.Command("osascript", "-e", script)

	case "windows":
		cmdStr := fmt.Sprintf(`%s & echo. & echo Press any key to close... & pause >nul`, command)
		cmd = exec.Command("cmd", "/c", cmdStr)
		cmd.SysProcAttr = &syscall.SysProcAttr{
			CreationFlags:    CreateNewConsole,
			NoInheritHandles: true,
		}

	default:
		return nil, fmt.Errorf("unsupported operating system: %s", runtime.GOOS)
	}

	if err := cmd.Start(); err != nil {
		return nil, fmt.Errorf("failed to start detached command: %w", err)
	}

	return cmd, nil
}

func escapeAppleScript(s string) string {
	s = strings.ReplaceAll(s, `\`, `\\`)
	s = strings.ReplaceAll(s, `"`, `\"`)
	return s
}

func commandExists(command string) bool {
	_, err := exec.LookPath(command)
	return err == nil
}

func detectOS() string {
	switch runtime.GOOS {
	case "windows":
		return "windows"
	case "darwin":
		return "mac"
	default:
		return "linux"
	}
}

func detectArchitecture() string {
	arch := runtime.GOARCH
	switch arch {
	case "amd64":
		return "x86_64"
	case "arm64":
		return "arm64"
	default:
		printSubWarning(fmt.Sprintf("Unknown architecture '%s', defaulting to x86_64", arch))
		return "x86_64"
	}
}

func configureArchitecture() {
	if detectedArch == "arm64" {
		systemImage = "system-images;android-33;google_apis_playstore;arm64-v8a"
		fridaServerArch = "android-arm64"
		ramdiskPath = filepath.Join("system-images", "android-33", "google_apis_playstore", "arm64-v8a", "ramdisk.img")
	} else {
		systemImage = "system-images;android-33;google_apis_playstore;x86_64"
		fridaServerArch = "android-x86_64"
		ramdiskPath = filepath.Join("system-images", "android-33", "google_apis_playstore", "x86_64", "ramdisk.img")
	}
	printSubInfo(fmt.Sprintf("Using %s emulator and Frida server", detectedArch))
}

func refreshEnv() {
	switch detectedOS {
	case "windows":
		output, err := exec.Command("powershell.exe", "-Command", `
			[System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
		`).Output()
		if err == nil {
			if err := os.Setenv("PATH", strings.TrimSpace(string(output))); err != nil {
				printSubWarning(fmt.Sprintf("%s: %v", "Failed to set PATH", err))
			}
		} else {
			printSubWarning(fmt.Sprintf("%s: %v", "Failed to get PATH from registry", err))
		}

	case "mac":
		brewPaths := []string{"/opt/homebrew/bin", "/usr/local/bin"}
		currentPath := os.Getenv("PATH")
		for _, brewPath := range brewPaths {
			if _, err := os.Stat(brewPath); err == nil && !strings.Contains(currentPath, brewPath) {
				if err := os.Setenv("PATH", brewPath+string(os.PathListSeparator)+currentPath); err != nil {
					printSubWarning(fmt.Sprintf("%s: %v", "Failed to add brew path to PATH", err))
				}
			}
		}

	case "linux":
		currentPath := os.Getenv("PATH")
		commonPaths := []string{"/usr/local/bin", "/usr/bin", "/bin"}
		for _, p := range commonPaths {
			if !strings.Contains(currentPath, p) {
				if err := os.Setenv("PATH", currentPath+string(os.PathListSeparator)+p); err != nil {
					printSubWarning(fmt.Sprintf("%s: %v", "Failed to add common path to PATH", err))
				}
			}
		}
	}
}

func addToPath(newPath string) error {
	switch detectedOS {
	case "windows":
		cmd := exec.Command("powershell.exe", "-Command",
			`[Environment]::GetEnvironmentVariable("Path", "User")`)
		output, err := cmd.Output()
		if err != nil {
			return fmt.Errorf("failed to get current PATH: %w", err)
		}

		currentPath := strings.TrimSpace(string(output))
		if strings.Contains(currentPath, newPath) {
			return nil
		}

		updatedPath := newPath
		if currentPath != "" {
			updatedPath = newPath + ";" + currentPath
		}

		cmd = exec.Command("powershell.exe", "-Command",
			fmt.Sprintf(`[Environment]::SetEnvironmentVariable("Path", "%s", "User")`, updatedPath))
		if err := cmd.Run(); err != nil {
			return fmt.Errorf("failed to update PATH: %w", err)
		}

		if err := os.Setenv("PATH", newPath+";"+os.Getenv("PATH")); err != nil {
			printSubWarning(fmt.Sprintf("%s: %v", "Failed to set PATH in current process", err))
		}
		return nil

	case "mac", "linux":
		home := os.Getenv("HOME")
		shellConfigs := []string{
			filepath.Join(home, ".bashrc"),
			filepath.Join(home, ".zshrc"),
			filepath.Join(home, ".profile"),
		}

		exportLine := fmt.Sprintf("\nexport PATH=\"%s:$PATH\"\n", newPath)

		for _, config := range shellConfigs {
			if content, err := os.ReadFile(config); err == nil {
				if strings.Contains(string(content), newPath) {
					continue
				}

				if f, err := os.OpenFile(config, os.O_APPEND|os.O_WRONLY, 0644); err == nil {
					if _, err := f.WriteString(exportLine); err != nil {
						printSubWarning(fmt.Sprintf("%s: %v", fmt.Sprintf("Failed to write to %s", config), err))
					}
					f.Close()
				}
			}
		}

		return os.Setenv("PATH", newPath+":"+os.Getenv("PATH"))
	}

	return nil
}

func downloadFile(filepath, url string) error {
	printSubInfo(fmt.Sprintf("Downloading from %s", url))
	resp, err := http.Get(url)
	if err != nil {
		return fmt.Errorf("download failed: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("download failed: HTTP %d", resp.StatusCode)
	}

	out, err := os.Create(filepath)
	if err != nil {
		return fmt.Errorf("failed to create file: %w", err)
	}
	defer out.Close()

	_, err = io.Copy(out, resp.Body)
	return err
}

func unzip(src, dest string) error {
	printSubInfo("Extracting archive")
	r, err := zip.OpenReader(src)
	if err != nil {
		return fmt.Errorf("failed to open zip: %w", err)
	}
	defer r.Close()

	for _, f := range r.File {
		fpath := filepath.Join(dest, f.Name)

		if f.FileInfo().IsDir() {
			os.MkdirAll(fpath, os.ModePerm)
			continue
		}

		if err = os.MkdirAll(filepath.Dir(fpath), os.ModePerm); err != nil {
			return err
		}

		outFile, err := os.OpenFile(fpath, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, f.Mode())
		if err != nil {
			return err
		}

		rc, err := f.Open()
		if err != nil {
			outFile.Close()
			return err
		}

		_, err = io.Copy(outFile, rc)
		outFile.Close()
		rc.Close()
		if err != nil {
			return err
		}
	}
	return nil
}

func ensureWinget() error {
	if commandExists("winget") {
		printSubInfo("winget detected")
		return nil
	}

	printSubInfo("Installing winget")
	psScript := `
		$progressPreference = 'silentlyContinue'
		Install-PackageProvider -Name NuGet -Force | Out-Null
		Install-Module -Name Microsoft.WinGet.Client -Force -Repository PSGallery | Out-Null
		Repair-WinGetPackageManager -AllUsers | Out-Null
	`

	cmd := exec.Command("powershell.exe", "-ExecutionPolicy", "Bypass", "-Command", psScript)
	cmd.Run()

	refreshEnv()

	if !commandExists("winget") {
		return fmt.Errorf("winget installation completed but command not found - restart terminal")
	}

	printSubInfo("winget installed")
	return nil
}

func ensureBrew() error {
	if commandExists("brew") {
		printSubInfo("Homebrew detected")
		return nil
	}

	printSubInfo("Installing Homebrew")
	installScriptURL := "https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh"
	resp, err := http.Get(installScriptURL)
	if err != nil {
		return fmt.Errorf("failed to download Homebrew installer: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("failed to download Homebrew installer: HTTP %d", resp.StatusCode)
	}

	cmd := exec.Command("/bin/bash")
	cmd.Stdin = resp.Body
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	if err := cmd.Run(); err != nil {
		return fmt.Errorf("Homebrew installation failed: %w", err)
	}

	refreshEnv()

	if !commandExists("brew") {
		return fmt.Errorf("homebrew installation completed but command not found - restart terminal")
	}

	printSubInfo("Homebrew installed")
	return nil
}

func ensureGit() error {
	if commandExists("git") {
		printSubInfo("Git detected")
		return nil
	}

	printSubInfo("Installing Git")
	pkg := "git"
	if detectedOS == "windows" {
		pkg = "Git.Git"
	}
	if err := installPackage("Git", pkg); err != nil {
		return err
	}

	if !commandExists("git") {
		return fmt.Errorf("git installation completed but command not found - restart terminal")
	}

	return nil
}

func ensureSQLite3() error {
	if commandExists("sqlite3") {
		printSubInfo("SQLite3 detected")
		return nil
	}

	printSubInfo("Installing SQLite3")
	pkg := "sqlite3"
	if detectedOS == "windows" {
		pkg = "SQLite.SQLite"
	}
	if err := installPackage("SQLite3", pkg); err != nil {
		return err
	}

	if !commandExists("sqlite3") {
		return fmt.Errorf("sqlite3 installation completed but command not found - restart terminal")
	}

	return nil
}

func installPackage(name, pkg string) error {
	var cmd *exec.Cmd

	switch detectedOS {
	case "linux":
		if commandExists("apt-get") {
			cmd = exec.Command("sudo", "apt-get", "install", "-y", "-qq", pkg)
		} else if commandExists("yum") {
			cmd = exec.Command("sudo", "yum", "install", "-y", "-q", pkg)
		} else if commandExists("pacman") {
			cmd = exec.Command("sudo", "pacman", "-S", "--noconfirm", pkg)
		} else {
			return fmt.Errorf("no supported package manager found")
		}
	case "mac":
		if !commandExists("brew") {
			return fmt.Errorf("homebrew not found")
		}
		cmd = exec.Command("brew", "install", pkg)
	case "windows":
		if !commandExists("winget") {
			return fmt.Errorf("winget not found")
		}
		cmd = exec.Command("winget", "install", pkg, "--silent", "--accept-package-agreements", "--accept-source-agreements")
	default:
		return fmt.Errorf("unsupported OS: %s", detectedOS)
	}

	var stdout, stderr strings.Builder
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr

	if err := cmd.Run(); err != nil {
		printSubWarning(fmt.Sprintf("%s installation warning: %v (may be fine)", name, err))
	}

	refreshEnv()
	return nil
}

func stepInitialize() error {
	printInfo("Initializing")

	detectedOS = detectOS()
	printSubInfo(fmt.Sprintf("OS: %s", detectedOS))

	detectedArch = detectArchitecture()
	printSubInfo(fmt.Sprintf("Architecture: %s", detectedArch))

	configureArchitecture()

	var err error
	tempDir, err = os.MkdirTemp("", "android-setup-*")
	if err != nil {
		return fmt.Errorf("failed to create temp directory: %w", err)
	}
	printSubInfo(fmt.Sprintf("Temp directory: %s", tempDir))

	return nil
}

func stepSystemTools() error {
	printInfo("Setting up system tools")

	if detectedOS == "windows" {
		if err := ensureWinget(); err != nil {
			return fmt.Errorf("winget setup failed: %w", err)
		}
	} else if detectedOS == "mac" {
		if err := ensureBrew(); err != nil {
			return fmt.Errorf("homebrew setup failed: %w", err)
		}
	}

	if err := ensureGit(); err != nil {
		return fmt.Errorf("git setup failed: %w", err)
	}

	if err := ensureSQLite3(); err != nil {
		return fmt.Errorf("sqlite3 setup failed: %w", err)
	}

	return nil
}

func stepPython() error {
	printInfo("Setting up Python")

	if _, err := runCommand("python3", "--version"); err == nil {
		pythonCmd = "python3"
		printSubInfo("Python3 detected")
		return nil
	}

	if _, err := runCommand("python", "--version"); err == nil {
		pythonCmd = "python"
		printSubInfo("Python detected")
		return nil
	}

	printSubInfo("Installing Python")
	pkg := "python3"
	if detectedOS == "windows" {
		pkg = "Python.Python.3.12"
	}
	if err := installPackage("Python", pkg); err != nil {
		return err
	}

	if detectedOS == "windows" {
		pythonPath := filepath.Join(os.Getenv("LOCALAPPDATA"), "Programs", "Python", "Python312")
		if err := addToPath(pythonPath); err != nil {
			return fmt.Errorf("failed to add Python to PATH: %w", err)
		}
	}

	if _, err := runCommand("python3", "--version"); err == nil {
		pythonCmd = "python3"
		return nil
	}

	if _, err := runCommand("python", "--version"); err == nil {
		pythonCmd = "python"
		return nil
	}

	return fmt.Errorf("python installation completed but not functioning - restart terminal")
}

func stepJava() error {
	printInfo("Setting up Java")

	if commandExists("java") {
		printSubInfo("Java detected")
	} else {
		printSubInfo("Installing Java")
		pkg := "openjdk-17-jdk"
		if detectedOS == "windows" {
			pkg = "Oracle.JDK.17"
		} else if detectedOS == "mac" {
			pkg = "openjdk@17"
		}
		if err := installPackage("Java", pkg); err != nil {
			return err
		}
	}

	javaHome = os.Getenv("JAVA_HOME")
	if javaHome == "" {
		if detectedOS == "mac" {
			output, _ := runCommand("/usr/libexec/java_home")
			javaHome = strings.TrimSpace(output)
		} else if detectedOS == "windows" {
			javaHome = findJavaHomeWindows()
		} else {
			if javaPath, _ := exec.LookPath("java"); javaPath != "" {
				javaHome = filepath.Dir(filepath.Dir(javaPath))
			}
		}
		if javaHome != "" {
			if err := os.Setenv("JAVA_HOME", javaHome); err != nil {
				printSubWarning(fmt.Sprintf("%s: %v", "Failed to set JAVA_HOME", err))
			} else {
				printSubInfo(fmt.Sprintf("JAVA_HOME: %s", javaHome))
			}
		}
	} else {
		printSubInfo(fmt.Sprintf("JAVA_HOME: %s", javaHome))
	}

	return nil
}

func findJavaHomeWindows() string {
	commonPaths := []string{
		filepath.Join(os.Getenv("ProgramFiles"), "Java"),
		filepath.Join(os.Getenv("ProgramFiles(x86)"), "Java"),
		filepath.Join(os.Getenv("LOCALAPPDATA"), "Programs", "Java"),
	}

	for _, basePath := range commonPaths {
		if basePath == "" || basePath == "Java" {
			continue
		}
		entries, err := os.ReadDir(basePath)
		if err != nil {
			continue
		}

		for _, entry := range entries {
			if entry.IsDir() && strings.Contains(strings.ToLower(entry.Name()), "jdk") {
				potentialHome := filepath.Join(basePath, entry.Name())
				if _, err := os.Stat(filepath.Join(potentialHome, "bin", "java.exe")); err == nil {
					return potentialHome
				}
			}
		}
	}

	return ""
}

func stepAndroidSDK() error {
	printInfo("Setting up Android SDK")

	androidHome = os.Getenv("ANDROID_HOME")
	if androidHome == "" {
		switch detectedOS {
		case "windows":
			androidHome = filepath.Join(os.Getenv("LOCALAPPDATA"), "Android", "Sdk")
		case "mac":
			androidHome = filepath.Join(os.Getenv("HOME"), "Library", "Android", "sdk")
		default:
			androidHome = filepath.Join(os.Getenv("HOME"), "Android", "Sdk")
		}
		if err := os.Setenv("ANDROID_HOME", androidHome); err != nil {
			printSubWarning(fmt.Sprintf("%s: %v", "Failed to set ANDROID_HOME", err))
		}
	}
	printSubInfo(fmt.Sprintf("ANDROID_HOME: %s", androidHome))

	if !commandExists("sdkmanager") {
		printSubInfo("Installing SDK tools")
		if err := downloadAndInstallSDKTools(); err != nil {
			return err
		}
	} else {
		printSubInfo("SDK manager detected")
	}

	if installed, err := installSDKComponent("platform-tools", "Platform-Tools"); err != nil {
		return err
	} else if installed {
		if err := addToPath(filepath.Join(androidHome, "platform-tools")); err != nil {
			printSubWarning(fmt.Sprintf("%s: %v", "Failed to add platform-tools to PATH", err))
		}
	}

	if installed, err := installSDKComponent("emulator", "Emulator"); err != nil {
		return err
	} else if installed {
		if err := addToPath(filepath.Join(androidHome, "emulator")); err != nil {
			printSubWarning(fmt.Sprintf("%s: %v", "Failed to add emulator to PATH", err))
		}
	}

	if _, err := installSDKComponent(systemImage, "System image"); err != nil {
		return err
	}

	refreshEnv()
	configureArchitecture()

	return nil
}

func downloadAndInstallSDKTools() error {
	cmdlineToolsDir := filepath.Join(androidHome, "cmdline-tools")
	os.MkdirAll(cmdlineToolsDir, 0755)

	var sdkURL string
	switch detectedOS {
	case "linux":
		sdkURL = "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
	case "mac":
		sdkURL = "https://dl.google.com/android/repository/commandlinetools-mac-11076708_latest.zip"
	case "windows":
		sdkURL = "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
	}

	zipPath := filepath.Join(tempDir, "cmdline-tools.zip")
	if err := downloadFile(zipPath, sdkURL); err != nil {
		return err
	}

	extractDir := filepath.Join(tempDir, "extracted")
	if err := unzip(zipPath, extractDir); err != nil {
		return err
	}

	latestDir := filepath.Join(cmdlineToolsDir, "latest")
	os.RemoveAll(latestDir)

	srcDir := filepath.Join(extractDir, "cmdline-tools")
	if err := os.Rename(srcDir, latestDir); err != nil {
		return err
	}

	return addToPath(filepath.Join(latestDir, "bin"))
}

func installSDKComponent(component, name string) (bool, error) {
	output, _ := runCommand("sdkmanager", "--list_installed")
	if strings.Contains(output, component) {
		printSubInfo(fmt.Sprintf("%s already installed", name))
		return false, nil
	}

	printSubInfo(fmt.Sprintf("Installing %s", name))

	cmd := exec.Command("sdkmanager", component)
	stdin, _ := cmd.StdinPipe()

	var stdout, stderr strings.Builder
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr

	if err := cmd.Start(); err != nil {
		return false, fmt.Errorf("sdkmanager start failed: %w", err)
	}

	stdin.Write([]byte("y\n"))
	stdin.Close()

	if err := cmd.Wait(); err != nil {
		return false, &CommandError{
			Command: "sdkmanager",
			Args:    []string{component},
			Output:  strings.TrimSpace(stdout.String()),
			Err:     err,
		}
	}

	return true, nil
}

func stepEmulator() error {
	printInfo("Setting up emulator")

	cmd := exec.Command("emulator", "-list-avds")
	output, err := cmd.Output()
	if err != nil {
		return fmt.Errorf("failed to list AVDs: %w", err)
	}

	avdExists := false
	for _, avd := range strings.Split(strings.TrimSpace(string(output)), "\n") {
		if strings.TrimSpace(avd) == AVDName {
			avdExists = true
			break
		}
	}

	if !avdExists {
		printSubInfo(fmt.Sprintf("Creating AVD: %s", AVDName))
		_, err := runCommand("avdmanager", "create", "avd",
			"-n", AVDName,
			"-k", systemImage,
			"--device", DeviceType,
			"--force",
			"-p", filepath.Join(os.Getenv("HOME"), ".android", "avd", AVDName+".avd"))
		if err != nil {
			return fmt.Errorf("AVD creation failed: %w", err)
		}
	} else {
		printSubInfo("AVD already exists")
	}

	if err := startEmulator(); err != nil {
		return fmt.Errorf("failed to start emulator: %w", err)
	}

	printInfo("Configuring root access")
	if output, _ := runCommand("adb", "shell", "su", "-c", "id"); strings.Contains(strings.ToLower(output), "uid=0") {
		printSubInfo("Root access confirmed")
	} else {
		printSubInfo("Setting up rootAVD")

		rootAVDDir = filepath.Join(tempDir, "rootAVD")
		if _, err := runCommand("git", "clone", RootAVDRepo, rootAVDDir); err != nil {
			return fmt.Errorf("rootAVD clone failed: %w", err)
		}

		var rootAVDScript string
		if detectedOS == "windows" {
			rootAVDScript = filepath.Join(rootAVDDir, "rootAVD.bat")
		} else {
			rootAVDScript = filepath.Join(rootAVDDir, "rootAVD.sh")
		}

		cmd := exec.Command(rootAVDScript, ramdiskPath)
		cmd.Dir = rootAVDDir
		if err := cmd.Start(); err != nil {
			return fmt.Errorf("rootAVD start failed: %w", err)
		}

		printSubInfo("Running rootAVD script")
		if err := cmd.Wait(); err != nil {
			return fmt.Errorf("rootAVD failed: %w", err)
		}

		printSubInfo("Waiting for emulator to go offline")
		if err := waitForEmulatorStatus(false); err != nil {
			return fmt.Errorf("rootAVD wait failed: %w", err)
		}

		printSubInfo("Restarting emulator")
		if err := startEmulator(); err != nil {
			return fmt.Errorf("failed to start emulator: %w", err)
		}
	}

	printSubInfo("Checking Zygisk status")
	command := "\"magisk --sqlite 'select value from settings where (key=\\\"zygisk\\\");'\""

	zygiskEnabled := false
	if output, err := runCommand("adb", "shell", "su", "-c", command); err == nil {
		if strings.Contains(output, "value=1") {
			zygiskEnabled = true
			printSubInfo("Zygisk is already enabled")
		}
	}

	if !zygiskEnabled {
		printSubInfo("Enabling Zygisk")
		command = "\"magisk --sqlite 'replace into settings (key,value) values(\\\"zygisk\\\",1);'\""
		if _, err := runCommand("adb", "shell", "su", "-c", command); err != nil {
			printSubWarning(fmt.Sprintf("%s: %v", "Failed to enable Zygisk", err))
		} else {
			printSubInfo("Zygisk enabled successfully")
		}
	}

	printSubInfo("Configuring Magisk auto-allow")
	command = "magisk resetprop persist.sys.su.mode 2"
	if _, err := runCommand("adb", "shell", "su", "-c", command); err != nil {
		printSubWarning(fmt.Sprintf("%s: %v", "Failed to set Magisk auto-allow", err))
	}

	return nil
}

func startEmulator() error {
	emulatorBin := filepath.Join(androidHome, "emulator", "emulator")
	if detectedOS == "windows" {
		emulatorBin += ".exe"
	}

	if _, err := os.Stat(emulatorBin); os.IsNotExist(err) {
		return fmt.Errorf("emulator binary not found: %s", emulatorBin)
	}

	killExistingEmulators()

	printSubInfo("Restarting ADB")
	if _, err := runCommand("adb", "kill-server"); err != nil {
		printSubWarning(fmt.Sprintf("%s: %v", "Failed to kill ADB server", err))
	}
	if _, err := runCommand("adb", "start-server"); err != nil {
		return fmt.Errorf("ADB start failed: %w", err)
	}

	printSubInfo(fmt.Sprintf("Starting emulator: %s", AVDName))
	if _, err := runDetachedCommand("emulator", "-avd", AVDName, "-writable-system", "-no-snapshot-load"); err != nil {
		return fmt.Errorf("emulator start failed: %w", err)
	}

	return waitForEmulatorStatus(true)
}

func killExistingEmulators() {
	if detectedOS == "windows" {
		for _, proc := range []string{"qemu-system-x86_64.exe", "emulator.exe"} {
			if err := exec.Command("taskkill", "/F", "/IM", proc).Run(); err != nil {
				printSubWarning(fmt.Sprintf("%s: %v", fmt.Sprintf("Failed to kill %s", proc), err))
			}
		}
	} else {
		if err := exec.Command("pkill", "-9", "emulator").Run(); err != nil {
			printSubWarning(fmt.Sprintf("%s: %v", "Failed to kill emulator processes", err))
		}
		if err := exec.Command("pkill", "-9", "qemu-system").Run(); err != nil {
			printSubWarning(fmt.Sprintf("%s: %v", "Failed to kill qemu processes", err))
		}
	}
}

func rebootEmulator() error {
	printSubInfo("Rebooting emulator")
	if _, err := runCommand("adb", "reboot"); err != nil {
		printSubWarning(fmt.Sprintf("%s: %v", "Failed to reboot device", err))
	}

	return waitForEmulatorStatus(true)
}

func waitForEmulatorStatus(online bool) error {
	start := time.Now()
	for time.Since(start) < EmulatorTimeout*time.Second {
		output, err := runCommand("adb", "devices")
		if err == nil && strings.Contains(output, "emulator") == online {
			break
		}
		time.Sleep(2 * time.Second)
	}

	if time.Since(start) >= EmulatorTimeout*time.Second {
		return fmt.Errorf("emulator status check timeout after %d seconds", EmulatorTimeout)
	}

	if online {
		if _, err := runCommand("adb", "wait-for-device"); err != nil {
			printSubWarning(fmt.Sprintf("%s: %v", "ADB wait-for-device warning", err))
		}

		bootStart := time.Now()
		bootTimeout := 300 * time.Second
		for time.Since(bootStart) < bootTimeout {
			output, err := runCommand("adb", "shell", "getprop", "sys.boot_completed")
			if err == nil && strings.TrimSpace(output) == "1" {
				return nil
			} else {
				time.Sleep(2 * time.Second)
			}
		}

		return fmt.Errorf("boot timeout after %d seconds", int(bootTimeout.Seconds()))
	} else {
		return nil
	}
}

func stepLSPosed() error {
	printInfo("Installing LSPosed")

	modulesOutput, err := runCommand("adb", "shell", "su", "-c", "ls", "-1", "/data/adb/modules")
	if err != nil {
		return fmt.Errorf("failed to list modules: %w", err)
	}

	if strings.Contains(modulesOutput, "lsposed") {
		printSubInfo("LSPosed already installed")
		return nil
	}

	zipPath := filepath.Join(tempDir, "LSPosed.zip")
	if err := downloadFile(zipPath, LSPosedURL); err != nil {
		return fmt.Errorf("LSPosed download failed: %w", err)
	}

	devicePath := "/sdcard/LSPosed.zip"
	printSubInfo("Installing LSPosed module")
	if _, err := runCommand("adb", "push", zipPath, devicePath); err != nil {
		return fmt.Errorf("LSPosed push failed: %w", err)
	}

	installCmd := fmt.Sprintf("su -c 'magisk --install-module %s'", devicePath)
	if _, err := runCommand("adb", "shell", installCmd); err != nil {
		if err := fixMagiskEnvironment(); err != nil {
			return fmt.Errorf("Magisk environment fix failed: %w", err)
		}

		printSubInfo("Retrying LSPosed installation")
		if _, err := runCommand("adb", "shell", installCmd); err != nil {
			return fmt.Errorf("LSPosed installation failed: %w", err)
		}
	}

	if _, err := runCommand("adb", "shell", "rm", devicePath); err != nil {
		printSubWarning(fmt.Sprintf("%s: %v", "Failed to cleanup LSPosed zip", err))
	}

	return rebootEmulator()
}

func getUIDump() (string, error) {
	if _, err := runCommand("adb", "shell", "rm", "/sdcard/window_dump.xml"); err != nil {
		printSubWarning(fmt.Sprintf("%s: %v", "Failed to remove old UI dump", err))
	}
	if _, err := runCommand("adb", "shell", "uiautomator", "dump", "/sdcard/window_dump.xml"); err != nil {
		printSubWarning(fmt.Sprintf("%s: %v", "Failed to dump UI", err))
	}
	return runCommand("adb", "shell", "cat", "/sdcard/window_dump.xml")
}

func isTextOnScreen(text string) bool {
	output, err := getUIDump()
	return err == nil && strings.Contains(strings.ToLower(output), strings.ToLower(text))
}

func clickButtonByText(text string) error {
	xmlContent, err := getUIDump()
	if err != nil {
		return fmt.Errorf("UI dump failed: %w", err)
	}

	decoder := xml.NewDecoder(strings.NewReader(xmlContent))
	for {
		token, err := decoder.Token()
		if err == io.EOF {
			break
		}
		if err != nil {
			return fmt.Errorf("XML parse error: %w", err)
		}

		if t, ok := token.(xml.StartElement); ok && t.Name.Local == "node" {
			var currentText, bounds string
			for _, attr := range t.Attr {
				if attr.Name.Local == "text" {
					currentText = attr.Value
				}
				if attr.Name.Local == "bounds" {
					bounds = attr.Value
				}
			}

			if strings.EqualFold(currentText, text) {
				cleaned := strings.ReplaceAll(strings.ReplaceAll(bounds, "][", ","), "[", "")
				cleaned = strings.ReplaceAll(cleaned, "]", "")
				coords := strings.Split(cleaned, ",")

				if len(coords) == 4 {
					x1, _ := strconv.Atoi(coords[0])
					y1, _ := strconv.Atoi(coords[1])
					x2, _ := strconv.Atoi(coords[2])
					y2, _ := strconv.Atoi(coords[3])

					centerX := (x1 + x2) / 2
					centerY := (y1 + y2) / 2

					printSubInfo(fmt.Sprintf("Clicking '%s' at (%d, %d)", text, centerX, centerY))
					_, err := runCommand("adb", "shell", "input", "tap", fmt.Sprintf("%d", centerX), fmt.Sprintf("%d", centerY))
					return err
				}
			}
		}
	}

	return fmt.Errorf("button '%s' not found", text)
}

func fixMagiskEnvironment() error {
	printSubInfo("Finalizing Magisk environment")

	if _, err := runCommand("adb", "shell", "pm", "grant", "com.topjohnwu.magisk", "android.permission.POST_NOTIFICATIONS"); err != nil {
		return fmt.Errorf("Magisk permission grant failed: %w", err)
	}

	printSubInfo("Opening Magisk app")
	_, err := runCommand("adb", "shell", "monkey", "-p", "com.topjohnwu.magisk", "-c", "android.intent.category.LAUNCHER", "1")
	if err != nil {
		return fmt.Errorf("Magisk app launch failed: %w", err)
	}

	for i := 0; i < 10; i++ {
		if isTextOnScreen("Additional Setup") {
			printSubInfo("Setup dialog detected")
			break
		}
		time.Sleep(1 * time.Second)
	}

	if err := clickButtonByText("OK"); err != nil {
		return fmt.Errorf("failed to click OK: %w", err)
	}

	if err := waitForEmulatorStatus(false); err != nil {
		return fmt.Errorf("failed to wait for device to go offline: %w", err)
	}

	if err := waitForEmulatorStatus(true); err != nil {
		return fmt.Errorf("failed to wait for device to go online: %w", err)
	}

	return nil
}

func stepBypass() error {
	printInfo("Setting up bypass module")

	modulesOutput, err := runCommand("adb", "shell", "su", "-c", "/data/adb/lspd/bin/cli", "modules", "ls")
	if err != nil {
		return fmt.Errorf("failed to list LSPosed modules: %w", err)
	}

	modules := make(map[string]bool)
	lines := strings.Split(modulesOutput, "\n")
	for i, line := range lines {
		if i == 0 {
			continue // Skip header
		}
		fields := strings.Fields(line)
		if len(fields) == 3 {
			modules[fields[0]] = fields[2] == "enabled"
		}
	}

	if _, exists := modules[LSPosedModuleName]; !exists {
		bypassAPK := filepath.Join(tempDir, "bypass.apk")
		if err := downloadFile(bypassAPK, LSPosedModuleUrl); err != nil {
			return fmt.Errorf("bypass module download failed: %w", err)
		}

		printSubInfo("Installing bypass module")
		if _, err := runCommand("adb", "install", "-r", bypassAPK); err != nil {
			return fmt.Errorf("bypass module installation failed: %w", err)
		}
	} else {
		printSubInfo("Bypass module already installed")
	}

	scopes, err := runCommand("adb", "shell", "su", "-c", "/data/adb/lspd/bin/cli", "scope", "ls", LSPosedModuleName)
	if err != nil {
		return fmt.Errorf("failed to list scopes: %w", err)
	}

	if !strings.Contains(scopes, BicoccAppPackage) {
		printSubInfo("Adding Bicoccapp to bypass scopes")
		if _, err := runCommand("adb", "shell", "su", "-c", "/data/adb/lspd/bin/cli", "scope", "set", "-a", LSPosedModuleName, BicoccAppPackage+"/0"); err != nil {
			return fmt.Errorf("failed to set scopes: %w", err)
		}
	} else {
		printSubInfo("Bicoccapp already in scopes")
	}

	printSubInfo("Enabling bypass module")
	if _, err := runCommand("adb", "shell", "su", "-c", "/data/adb/lspd/bin/cli", "modules", "set", "-e", LSPosedModuleName); err != nil {
		return fmt.Errorf("failed to enable module: %w", err)
	}

	return nil
}

func stepBicoccApp() error {
	printInfo("Setting up BicoccApp")

	if installed, _ := isBicoccappInstalled(); installed {
		printSubInfo("Bicoccapp detected")
		return nil
	}

	printSubInfo("Bicoccapp is not installed: please install it manually from the Google Play Store (you might need to log in)")
	for {
		if installed, _ := isBicoccappInstalled(); installed {
			printSubInfo("Bicoccapp installation detected")
			return nil
		}
		time.Sleep(5 * time.Second)
	}
}

func isBicoccappInstalled() (bool, error) {
	output, err := runCommand("adb", "shell", "pm", "list", "packages")
	if err != nil {
		return false, fmt.Errorf("package list failed: %w", err)
	}
	return strings.Contains(output, BicoccAppPackage), nil
}

func pauseBeforeExit() {
	fmt.Println()
	fmt.Println(colorInfo.Render("Press Enter to exit..."))
	reader := bufio.NewReader(os.Stdin)
	reader.ReadString('\n')
}

func main() {
	startTime = time.Now()

	steps := []struct {
		name string
		fn   func() error
	}{
		{"Initialize", stepInitialize},
		{"System Tools", stepSystemTools},
		{"Python", stepPython},
		{"Java", stepJava},
		{"Android SDK", stepAndroidSDK},
		{"Emulator", stepEmulator},
		{"LSPosed", stepLSPosed},
		{"BicoccApp", stepBicoccApp},
		{"Bypass Module", stepBypass},
	}

	for _, step := range steps {
		if err := step.fn(); err != nil {
			printError(fmt.Sprintf("Setup failed at: %s", step.name))
			printSubWarning("Error: " + err.Error())
			var cmdErr *CommandError
			if errors.As(err, &cmdErr) {
				printSubWarning("Command details:")
				printSubWarning(fmt.Sprintf("%s %s", cmdErr.Command, strings.Join(cmdErr.Args, " ")))
				if cmdErr.Output != "" {
					printSubWarning(fmt.Sprintf("Output: %s", cmdErr.Output))
				}
			}
			pauseBeforeExit()
			os.Exit(1)
		}
	}

	printComplete()

	if tempDir != "" {
		if err := os.RemoveAll(tempDir); err != nil {
			printSubWarning(fmt.Sprintf("%s: %v", "Failed to cleanup temporary directory", err))
		}
	}

	pauseBeforeExit()
}
