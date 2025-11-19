package main

import (
	"archive/zip"
	"bufio"
	"encoding/xml"
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
	FridaVersion      = "17.5.1"
	AVDName           = "Pixel_7_Pro_API_33"
	AndroidAPI        = "33"
	DeviceType        = "pixel_7_pro"
	BicoccAppPackage  = "it.bicoccapp.unimib"
	LSPosedURL        = "https://github.com/mywalkb/LSPosed_mod/releases/download/v1.9.3_mod/LSPosed-v1.9.3_mod-7244-zygisk-release.zip"
	RootAVDRepo       = "https://gitlab.com/newbit/rootAVD.git"
	EmulatorTimeout   = 180
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
)

var (
	colorSuccess    = lipgloss.NewStyle().Foreground(lipgloss.Color("#04B575"))
	colorError      = lipgloss.NewStyle().Foreground(lipgloss.Color("#FF0000"))
	colorStep       = lipgloss.NewStyle().Foreground(lipgloss.Color("#00AAFF")).Bold(true)
	colorSub        = lipgloss.NewStyle().Foreground(lipgloss.Color("#888888"))
	colorSubWarning = lipgloss.NewStyle().Foreground(lipgloss.Color("#FFFF00"))
	colorInfo       = lipgloss.NewStyle().Foreground(lipgloss.Color("#FFFFFF"))
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
		sb.WriteString(fmt.Sprintf("\nError: %v", e.Err))
	}
	if e.Output != "" {
		sb.WriteString(fmt.Sprintf("\nOutput: %s", e.Output))
	}
	return sb.String()
}

func printStep(message string) {
	fmt.Println(colorStep.Render(message))
}

func printSubStep(message string) {
	fmt.Println(colorSub.Render("  → " + message))
}

func printSubStepWarning(message string) {
	fmt.Println(colorSubWarning.Render("  → " + message))
}

func printError(message string) {
	fmt.Println(colorError.Render("✗ " + message))
}

func printInfo(message string) {
	fmt.Println(colorInfo.Render("  " + message))
}

func printComplete() {
	fmt.Println()
	fmt.Println(colorSuccess.Render("═══════════════════════════════════════════"))
	fmt.Println(colorSuccess.Render("✓ Setup Complete!"))
	fmt.Println(colorSuccess.Render("═══════════════════════════════════════════"))
	fmt.Println()
	printInfo("Configuration:")
	printInfo("  • AVD Name: " + AVDName)
	printInfo("  • Android Version: 13 (API " + AndroidAPI + ")")
	printInfo("  • Device: Pixel 7 Pro")
	printInfo("  • Architecture: " + detectedArch)
	printInfo("  • Frida Version: " + FridaVersion)
	fmt.Println()
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
	printSubStep("Running command: " + command)

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
			{"lxterminal", []string{"-e", "bash", "-c", cmdStr}},
			{"xterm", []string{"-e", "bash", "-c", cmdStr}},
			{"rxvt", []string{"-e", "bash", "-c", cmdStr}},
			{"urxvt", []string{"-e", "bash", "-c", cmdStr}},
			{"terminator", []string{"-e", "bash", "-c", cmdStr}},
			{"tilix", []string{"-e", "bash", "-c", cmdStr}},
			{"alacritty", []string{"-e", "bash", "-c", cmdStr}},
			{"kitty", []string{"-e", "bash", "-c", cmdStr}},
			{"sakura", []string{"-e", "bash", "-c", cmdStr}},
			{"terminology", []string{"-e", "bash", "-c", cmdStr}},
			{"st", []string{"-e", "bash", "-c", cmdStr}},
		}

		var found bool
		for _, term := range terminals {
			if _, err := exec.LookPath(term.name); err == nil {
				cmd = exec.Command(term.name, term.args...)
				found = true
				break
			}
		}

		if !found {
			return nil, fmt.Errorf("no terminal emulator found.")
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
		return nil, err
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
	printSubStep("Detected CPU architecture: " + arch)

	switch arch {
	case "amd64":
		return "x86_64"
	case "arm64":
		return "arm64"
	default:
		printSubStepWarning("Unknown architecture '" + arch + "', defaulting to x86_64")
		return "x86_64"
	}
}

func configureArchitecture() {
	if detectedArch == "arm64" {
		systemImage = "system-images;android-33;google_apis_playstore;arm64-v8a"
		fridaServerArch = "android-arm64"
		ramdiskPath = filepath.Join("system-images", "android-33", "google_apis_playstore", "arm64-v8a", "ramdisk.img")
		printSubStep("Using ARM64 emulator and Frida server")
	} else {
		systemImage = "system-images;android-33;google_apis_playstore;x86_64"
		fridaServerArch = "android-x86_64"
		ramdiskPath = filepath.Join("system-images", "android-33", "google_apis_playstore", "x86_64", "ramdisk.img")
		printSubStep("Using x86_64 emulator and Frida server")
	}
}

func refreshEnv() error {
	printSubStep("Refreshing environment variables")

	switch detectedOS {
	case "windows":
		cmd := exec.Command("powershell.exe", "-Command", `
			$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
			[Environment]::SetEnvironmentVariable("Path", $env:Path, "Process")
		`)
		if err := cmd.Run(); err != nil {
			return fmt.Errorf("failed to refresh environment: %w", err)
		}

		output, err := exec.Command("powershell.exe", "-Command", `
			[System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
		`).Output()
		if err == nil {
			err := os.Setenv("PATH", strings.TrimSpace(string(output)))
			if err != nil {
				return err
			}
		}

	case "mac":
		brewPaths := []string{"/opt/homebrew/bin", "/usr/local/bin"}
		currentPath := os.Getenv("PATH")
		for _, brewPath := range brewPaths {
			if _, err := os.Stat(brewPath); err == nil {
				if !strings.Contains(currentPath, brewPath) {
					err := os.Setenv("PATH", brewPath+string(os.PathListSeparator)+currentPath)
					if err != nil {
						return err
					}
					printSubStep("Added " + brewPath + " to PATH")
				}
			}
		}

	case "linux":
		currentPath := os.Getenv("PATH")
		commonPaths := []string{"/usr/local/bin", "/usr/bin", "/bin", "/usr/local/sbin", "/usr/sbin", "/sbin"}
		for _, p := range commonPaths {
			if !strings.Contains(currentPath, p) {
				err := os.Setenv("PATH", currentPath+string(os.PathListSeparator)+p)
				if err != nil {
					return err
				}
			}
		}
	}

	return nil
}

func addToPath(newPath string) error {
	printSubStep("Adding to PATH: " + newPath)

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
			printSubStep("Path already in PATH")
			return nil
		}

		var updatedPath string
		if currentPath == "" {
			updatedPath = newPath
		} else {
			updatedPath = newPath + ";" + currentPath
		}

		cmd = exec.Command("powershell.exe", "-Command",
			fmt.Sprintf(`[Environment]::SetEnvironmentVariable("Path", "%s", "User")`, updatedPath))
		if err := cmd.Run(); err != nil {
			return fmt.Errorf("failed to update PATH: %w", err)
		}

		if err := os.Setenv("PATH", newPath+";"+os.Getenv("PATH")); err != nil {
			return err
		}

	case "mac", "linux":
		home := os.Getenv("HOME")
		shellConfigs := []string{
			filepath.Join(home, ".bashrc"),
			filepath.Join(home, ".zshrc"),
			filepath.Join(home, ".profile"),
		}

		exportLine := fmt.Sprintf("\nexport PATH=\"%s:$PATH\"\n", newPath)

		for _, config := range shellConfigs {
			if _, err := os.Stat(config); err == nil {
				content, _ := os.ReadFile(config)
				if strings.Contains(string(content), newPath) {
					continue
				}

				f, err := os.OpenFile(config, os.O_APPEND|os.O_WRONLY, 0644)
				if err != nil {
					continue
				}
				if _, err := f.WriteString(exportLine); err != nil {
					return err
				}
				if err := f.Close(); err != nil {
					return err
				}
			}
		}

		err := os.Setenv("PATH", newPath+":"+os.Getenv("PATH"))
		if err != nil {
			return err
		}
	}

	return nil
}

func downloadFile(filepath, url string) error {
	printSubStep("Downloading: " + url)
	resp, err := http.Get(url)
	if err != nil {
		return fmt.Errorf("failed to download from '%s': %w", url, err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("failed to download from '%s': HTTP status %d", url, resp.StatusCode)
	}

	out, err := os.Create(filepath)
	if err != nil {
		return fmt.Errorf("failed to create file '%s': %w", filepath, err)
	}
	defer out.Close()

	_, err = io.Copy(out, resp.Body)
	if err != nil {
		return fmt.Errorf("failed to write downloaded content: %w", err)
	}

	return nil
}

func unzip(src, dest string) error {
	r, err := zip.OpenReader(src)
	if err != nil {
		return fmt.Errorf("failed to open zip file: %w", err)
	}
	defer r.Close()

	for _, f := range r.File {
		fpath := filepath.Join(dest, f.Name)

		if f.FileInfo().IsDir() {
			if err := os.MkdirAll(fpath, os.ModePerm); err != nil {
				return fmt.Errorf("failed to create directory: %w", err)
			}
			continue
		}

		if err = os.MkdirAll(filepath.Dir(fpath), os.ModePerm); err != nil {
			return fmt.Errorf("failed to create parent directory: %w", err)
		}

		outFile, err := os.OpenFile(fpath, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, f.Mode())
		if err != nil {
			return fmt.Errorf("failed to create file: %w", err)
		}

		rc, err := f.Open()
		if err != nil {
			if err := outFile.Close(); err != nil {
				return err
			}
			return fmt.Errorf("failed to open file in archive: %w", err)
		}

		_, err = io.Copy(outFile, rc)
		if err := outFile.Close(); err != nil {
			return err
		}
		if err := rc.Close(); err != nil {
			return err
		}
		if err != nil {
			return fmt.Errorf("failed to extract file: %w", err)
		}
	}
	return nil
}

func extractXZ(xzPath, outputPath string) error {
	if !commandExists("xz") {
		return fmt.Errorf("xz utility not found. Please install xz-utils package")
	}

	printSubStep("Decompressing xz archive")
	cmd := exec.Command("xz", "-d", xzPath)
	var stderr strings.Builder
	cmd.Stderr = &stderr

	if err := cmd.Run(); err != nil {
		return fmt.Errorf("failed to decompress: %w\nStderr: %s", err, stderr.String())
	}

	decompressedPath := strings.TrimSuffix(xzPath, ".xz")
	if _, err := os.Stat(decompressedPath); os.IsNotExist(err) {
		return fmt.Errorf("decompressed file not found after extraction")
	}

	if err := os.Rename(decompressedPath, outputPath); err != nil {
		return fmt.Errorf("failed to move decompressed file: %w", err)
	}
	return nil
}

func ensureWinget() error {
	printSubStep("Checking for winget")
	if commandExists("winget") {
		printSubStep("winget is already installed")
		return nil
	}

	printSubStep("Installing winget via PowerShell")
	psScript := `
		$progressPreference = 'silentlyContinue'
		Install-PackageProvider -Name NuGet -Force | Out-Null
		Install-Module -Name Microsoft.WinGet.Client -Force -Repository PSGallery | Out-Null
		Repair-WinGetPackageManager -AllUsers | Out-Null
	`

	cmd := exec.Command("powershell.exe", "-ExecutionPolicy", "Bypass", "-Command", psScript)
	_ = cmd.Run() // Ignore error

	if err := refreshEnv(); err != nil {
		return fmt.Errorf("failed to refresh environment: %w", err)
	}

	if !commandExists("winget") {
		return fmt.Errorf("winget installation completed but command not found. Please restart terminal")
	}

	printSubStep("winget installed successfully")
	return nil
}

func ensureBrew() error {
	printSubStep("Checking for Homebrew")
	if commandExists("brew") {
		printSubStep("Homebrew is already installed")
		return nil
	}

	printSubStep("Downloading Homebrew installer")
	installScriptURL := "https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh"
	resp, err := http.Get(installScriptURL)
	if err != nil {
		return fmt.Errorf("failed to download Homebrew installer: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("failed to download Homebrew installer: HTTP %d", resp.StatusCode)
	}

	printSubStep("Running Homebrew installer")
	cmd := exec.Command("/bin/bash")
	cmd.Stdin = resp.Body
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	if err := cmd.Run(); err != nil {
		return fmt.Errorf("failed to install Homebrew: %w", err)
	}

	if err := refreshEnv(); err != nil {
		return fmt.Errorf("failed to refresh environment: %w", err)
	}

	if !commandExists("brew") {
		return fmt.Errorf("homebrew installation completed but command not found. Please restart terminal")
	}

	printSubStep("Homebrew installed successfully")
	return nil
}

func ensureGit() error {
	printSubStep("Checking for Git")
	if commandExists("git") {
		printSubStep("Git is already installed")
		return nil
	}

	printSubStep("Git not found, installing")
	pkg := "git"
	if detectedOS == "windows" {
		pkg = "Git.Git"
	}
	if err := installPackage("Git", pkg); err != nil {
		return err
	}

	if !commandExists("git") {
		return fmt.Errorf("git installed but command not found. Please restart terminal")
	}

	printSubStep("Git installed successfully")
	return nil
}

func ensureSQLite3() error {
	printSubStep("Checking for SQLite3")
	if commandExists("sqlite3") {
		printSubStep("SQLite3 is already installed")
		return nil
	}

	printSubStep("SQLite3 not found, installing")
	pkg := "sqlite3"
	if detectedOS == "windows" {
		pkg = "SQLite.SQLite"
	}
	if err := installPackage("SQLite3", pkg); err != nil {
		return err
	}

	if !commandExists("sqlite3") {
		return fmt.Errorf("sqlite3 installed but command not found. Please restart terminal")
	}

	printSubStep("SQLite3 installed successfully")
	return nil
}

func installPackage(name, pkg string) error {
	var cmd *exec.Cmd

	printSubStep("Installing " + name)

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
		return fmt.Errorf("unsupported operating system: %s", detectedOS)
	}

	if cmd == nil {
		return fmt.Errorf("failed to create installation command")
	}

	var stdout, stderr strings.Builder
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr

	err := cmd.Run()
	if err != nil {
		printSubStepWarning(fmt.Sprintf("%s installation completed with error (this could be fine): %v", name, err))
	}

	if err := refreshEnv(); err != nil {
		printSubStepWarning(fmt.Sprintf("Failed to refresh environment: %v", err))
	}

	return nil
}

func stepInitialize() error {
	printStep("Initializing")

	printSubStep("Detecting operating system")
	detectedOS = detectOS()
	printSubStep("Detected OS: " + detectedOS)

	printSubStep("Detecting CPU architecture")
	detectedArch = detectArchitecture()

	printSubStep("Configuring architecture-specific settings")
	configureArchitecture()

	printSubStep("Creating temporary directory")
	var err error
	tempDir, err = os.MkdirTemp("", "android-setup-*")
	if err != nil {
		return fmt.Errorf("failed to create temporary directory: %w", err)
	}
	printSubStep("Temporary directory: " + tempDir)

	return nil
}

func stepSystemTools() error {
	printStep("Setting up system tools")

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
	printStep("Setting up Python")

	printSubStep("Checking for Python")
	_, python3Err := runCommand("python3", "--version")
	if python3Err == nil {
		pythonCmd = "python3"
		printSubStep("Found Python (python3)")
		return nil
	}

	_, pythonErr := runCommand("python", "--version")
	if pythonErr == nil {
		pythonCmd = "python"
		printSubStep("Found Python (python)")
		return nil
	}

	printSubStep("Python not found, installing")
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

	printSubStep("Verifying Python installation")
	_, verifyPython3Err := runCommand("python3", "--version")
	if verifyPython3Err == nil {
		pythonCmd = "python3"
		return nil
	}

	_, verifyPythonErr := runCommand("python", "--version")
	if verifyPythonErr == nil {
		pythonCmd = "python"
		return nil
	}

	return fmt.Errorf("python installed but not functioning. Please restart terminal")
}

func stepFrida() error {
	printStep("Setting up Frida tools")

	printSubStep("Checking for Frida tools")
	if !commandExists("frida") {
		printSubStep("Installing frida-tools via pip")
		if _, err := runCommand(pythonCmd, "-m", "pip", "install", "--quiet", "frida-tools"); err != nil {
			return err
		}
		printSubStep("Frida tools installed")
	} else {
		printSubStep("Frida tools already installed")
	}

	return nil
}

func stepJava() error {
	printStep("Setting up Java")

	printSubStep("Checking for Java")
	if !commandExists("java") {
		printSubStep("Java not found, installing")
		pkg := "openjdk-17-jdk"
		if detectedOS == "windows" {
			pkg = "Oracle.JDK.17"
		} else if detectedOS == "mac" {
			pkg = "openjdk@17"
		}
		if err := installPackage("Java", pkg); err != nil {
			return err
		}
	} else {
		printSubStep("Java already installed")
	}

	printSubStep("Configuring JAVA_HOME")
	javaHome = os.Getenv("JAVA_HOME")
	if javaHome == "" {
		if detectedOS == "mac" {
			output, _ := runCommand("/usr/libexec/java_home")
			javaHome = output
		} else if detectedOS == "windows" {
			javaHome = findJavaHomeWindows()
		} else {
			javaPath, _ := exec.LookPath("java")
			if javaPath != "" {
				javaHome = filepath.Dir(filepath.Dir(javaPath))
			}
		}
		if javaHome != "" {
			err := os.Setenv("JAVA_HOME", javaHome)
			if err != nil {
				return err
			}
			printSubStep("JAVA_HOME set to: " + javaHome)
		}
	} else {
		printSubStep("JAVA_HOME already set: " + javaHome)
	}

	return nil
}

func findJavaHomeWindows() string {
	printSubStep("Detecting Java installation on Windows")

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
					printSubStep("Found JDK at: " + potentialHome)
					return potentialHome
				}
			}
		}
	}

	return ""
}

func stepAndroidSDK() error {
	printStep("Setting up Android SDK")

	printSubStep("Configuring Android SDK location")
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
		err := os.Setenv("ANDROID_HOME", androidHome)
		if err != nil {
			return err
		}
		printSubStep("ANDROID_HOME set to: " + androidHome)
	} else {
		printSubStep("ANDROID_HOME: " + androidHome)
	}

	printSubStep("Checking for SDK manager")
	if !commandExists("sdkmanager") {
		printSubStep("SDK manager not found, downloading")
		if err := downloadAndInstallSDKTools(); err != nil {
			return err
		}
	} else {
		printSubStep("SDK manager found")
	}

	if installedPlatformTools, err := installSDKComponent("platform-tools", "Platform-Tools"); err != nil {
		return err
	} else if installedPlatformTools {
		platformToolsPath := filepath.Join(androidHome, "platform-tools")
		if err := addToPath(platformToolsPath); err != nil {
			return fmt.Errorf("failed to add platform-tools to PATH: %w", err)
		}
	}

	if installedEmulator, err := installSDKComponent("emulator", "Emulator"); err != nil {
		return err
	} else if installedEmulator {
		emulatorPath := filepath.Join(androidHome, "emulator")
		if err := addToPath(emulatorPath); err != nil {
			return fmt.Errorf("failed to add emulator to PATH: %w", err)
		}
	}

	if _, err := installSDKComponent(systemImage, "System image"); err != nil {
		return err
	}

	err := refreshEnv()
	if err != nil {
		return err
	}

	configureArchitecture()

	return nil
}

func downloadAndInstallSDKTools() error {
	printSubStep("Creating cmdline-tools directory")
	cmdlineToolsDir := filepath.Join(androidHome, "cmdline-tools")
	err := os.MkdirAll(cmdlineToolsDir, 0755)
	if err != nil {
		return err
	}

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

	printSubStep("Extracting command-line tools")
	extractDir := filepath.Join(tempDir, "extracted")
	if err := unzip(zipPath, extractDir); err != nil {
		return err
	}

	printSubStep("Installing command-line tools")
	latestDir := filepath.Join(cmdlineToolsDir, "latest")
	if err := os.RemoveAll(latestDir); err != nil {
		return err
	}

	srcDir := filepath.Join(extractDir, "cmdline-tools")
	if err := os.Rename(srcDir, latestDir); err != nil {
		return err
	}

	binPath := filepath.Join(latestDir, "bin")
	if err := addToPath(binPath); err != nil {
		return err
	}
	printSubStep("Command-line tools installed")

	return nil
}

// TODO: Refactor
func installSDKComponent(component, name string) (bool, error) {
	output, _ := runCommand("sdkmanager", "--list_installed")
	if strings.Contains(output, component) {
		printSubStep(name + " already installed")
		return false, nil
	}

	printSubStep("Installing " + name)

	cmd := exec.Command("sdkmanager", component)
	stdin, _ := cmd.StdinPipe()

	var stdout, stderr strings.Builder
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr

	if err := cmd.Start(); err != nil {
		return false, &CommandError{
			Command: "sdkmanager",
			Args:    []string{component},
			Err:     fmt.Errorf("failed to start: %w", err),
		}
	}

	stdin.Write([]byte("y\n"))
	stdin.Close()

	err := cmd.Wait()
	if err != nil {
		return false, &CommandError{
			Command: "sdkmanager",
			Args:    []string{component},
			Output:  strings.TrimSpace(stdout.String()),
			Err:     err,
		}
	}

	printSubStep(name + " installed")
	return true, nil
}

func stepEmulator() error {
	printStep("Setting up emulator")

	printSubStep("Checking for existing AVD")

	cmd := exec.Command("emulator", "-list-avds")
	output, err := cmd.Output()
	if err != nil {
		return fmt.Errorf("failed to list AVDs: %w", err)
	}

	avdSet := make(map[string]bool)
	avdList := strings.Split(strings.TrimSpace(string(output)), "\n")
	for _, avd := range avdList {
		if trimmed := strings.TrimSpace(avd); trimmed != "" {
			avdSet[trimmed] = true
		}
	}

	if _, avdExists := avdSet[AVDName]; !avdExists {
		printSubStep("Creating AVD: " + AVDName)
		_, err := runCommand("avdmanager", "create", "avd",
			"-n", AVDName,
			"-k", systemImage,
			"--device", DeviceType,
			"--force",
			"-p", filepath.Join(os.Getenv("HOME"), ".android", "avd", AVDName+".avd"))
		if err != nil {
			return fmt.Errorf("failed to start AVD creation: %w", err)
		}
		printSubStep("AVD created")
	} else {
		printSubStep("AVD already exists")
	}

	return startEmulator()
}

func startEmulator() error {
	emulatorBin := filepath.Join(androidHome, "emulator", "emulator")
	if detectedOS == "windows" {
		emulatorBin += ".exe"
	}

	if _, err := os.Stat(emulatorBin); os.IsNotExist(err) {
		return fmt.Errorf("emulator binary not found at: %s", emulatorBin)
	}

	printSubStep("Killing any existing emulator instances")
	_ = killExistingEmulators()

	printSubStep("Restarting ADB server")
	if _, err := runCommand("adb", "kill-server"); err != nil {
		return fmt.Errorf("failed to kill adb server: %w", err)
	}
	if _, err := runCommand("adb", "start-server"); err != nil {
		return fmt.Errorf("failed to start adb server: %w", err)
	}

	printSubStep("Starting emulator in a new window: " + AVDName)
	if _, err := runDetachedCommand("emulator", "-avd", AVDName, "-writable-system", "-no-snapshot-load"); err != nil {
		return fmt.Errorf("failed to start emulator: %w", err)
	}
	printSubStep("Emulator started in separate window")

	printSubStep("Waiting for emulator to boot")
	if err := waitForDevice(); err != nil {
		return fmt.Errorf("emulator failed to start: %w", err)
	}

	return nil
}

func killExistingEmulators() error {
	var cmd *exec.Cmd

	if detectedOS == "windows" {
		processes := []string{
			"qemu-system-x86_64.exe",
			"qemu-system-i386.exe",
			"emulator.exe",
			"emulator-x86.exe",
			"emulator-arm.exe",
		}
		for _, proc := range processes {
			cmd = exec.Command("taskkill", "/F", "/IM", proc)
			_ = cmd.Run()
		}
	} else {
		cmd = exec.Command("pkill", "-9", "emulator")
		_ = cmd.Run()

		cmd = exec.Command("pkill", "-9", "qemu-system")
		_ = cmd.Run()
	}

	return nil
}

func waitForDevice() error {
	elapsed := 0
	for elapsed < EmulatorTimeout {
		if elapsed%15 == 0 {
			printSubStep("Waiting for device...")
		}

		output, err := runCommand("adb", "devices")
		if err == nil && strings.Contains(output, "emulator") {
			break
		}
		time.Sleep(3 * time.Second)
		elapsed += 3
	}

	if elapsed >= EmulatorTimeout {
		return fmt.Errorf("timeout waiting for emulator after %d seconds", EmulatorTimeout)
	}

	printSubStep("Device detected, waiting for boot completion")
	if _, err := runCommand("adb", "wait-for-device"); err != nil {
		return fmt.Errorf("adb wait-for-device failed: %w", err)
	}

	bootTimeout := 300
	bootElapsed := 0
	for {
		if bootElapsed%20 == 0 && bootElapsed > 0 {
			printSubStep(fmt.Sprintf("Booting... (%d/%d seconds)", bootElapsed, bootTimeout))
		}

		output, err := runCommand("adb", "shell", "getprop", "sys.boot_completed")
		if err == nil && strings.TrimSpace(output) == "1" {
			break
		}

		time.Sleep(3 * time.Second)
		bootElapsed += 3

		if bootElapsed >= bootTimeout {
			return fmt.Errorf("timeout waiting for boot after %d seconds", bootTimeout)
		}
	}

	printSubStep("Boot completed, system stabilizing")
	time.Sleep(7 * time.Second)

	return nil
}

func stepRoot() error {
	printStep("Configuring root access")

	printSubStep("Checking root access")
	output, _ := runCommand("adb", "shell", "su", "-c", "id")

	if strings.Contains(strings.ToLower(output), "uid=0") {
		printSubStep("Root access confirmed (uid=0)")
	} else {
		printSubStepWarning("Root access not available, setting up rootAVD")

		rootAVDDir = filepath.Join(tempDir, "rootAVD")
		printSubStep("Cloning rootAVD repository")

		if _, err := runCommand("git", "clone", RootAVDRepo, rootAVDDir); err != nil {
			return fmt.Errorf("failed to clone rootAVD: %w", err)
		}

		var rootAVDScript string
		if detectedOS == "windows" {
			rootAVDScript = filepath.Join(rootAVDDir, "rootAVD.bat")
		} else {
			rootAVDScript = filepath.Join(rootAVDDir, "rootAVD.sh")
		}
		printSubStep("Found rootAVD script: " + rootAVDScript)

		cmd := exec.Command(rootAVDScript, ramdiskPath)
		cmd.Dir = rootAVDDir
		if err := cmd.Start(); err != nil {
			return fmt.Errorf("failed to start rootAVD: %w", err)
		}

		printSubStep("Running rootAVD script")
		if err := cmd.Wait(); err != nil {
			return fmt.Errorf("rootAVD failed: %w", err)
		}

		printSubStep("Waiting for changes to take effect")
		time.Sleep(10 * time.Second)

		printSubStep("Restarting emulator")
		if err := startEmulator(); err != nil {
			return err
		}
	}

	printSubStep("Configuring Magisk auto-allow policy")
	if _, err := runCommand("adb", "shell", "su", "-c", "magisk", "--sqlite", "PRAGMA user_version=7"); err != nil {
		return fmt.Errorf("Failed to set database version: " + err.Error())
	}
	if _, err := runCommand("adb", "shell", "su", "-c", "magisk", "resetprop", "persist.sys.su.mode", "2"); err != nil {
		return fmt.Errorf("Failed to set automatic response to allow: " + err.Error())
	}

	return nil
}

func stepLSPosed() error {
	printStep("Installing LSPosed")

	printSubStep("Checking if LSPosed is already installed")
	output, err := runCommand("adb", "shell", "pm", "list", "packages")
	if err != nil {
		return fmt.Errorf("failed to list packages: %w", err)
	}

	if strings.Contains(strings.ToLower(output), "lsposed") {
		printSubStep("LSPosed already installed")
		return nil
	}

	printSubStep("Downloading LSPosed")
	zipPath := filepath.Join(tempDir, "LSPosed.zip")
	if err := downloadFile(zipPath, LSPosedURL); err != nil {
		return fmt.Errorf("failed to download LSPosed: %w", err)
	}

	printSubStep("Pushing LSPosed module to device")
	devicePath := "/sdcard/LSPosed.zip"
	if _, err := runCommand("adb", "push", zipPath, devicePath); err != nil {
		return fmt.Errorf("failed to push LSPosed to device: %w", err)
	}

	printSubStep("Installing LSPosed module via Magisk")
	installCmd := fmt.Sprintf("su -c 'magisk --install-module %s'", devicePath)
	if _, err := runCommand("adb", "shell", installCmd); err != nil {
		if err := fixMagiskEnvironment(); err != nil {
			return fmt.Errorf("failed to fix Magisk environment: %w", err)
		}

		printSubStep("Retrying Magisk install after Magisk env fix")
		if _, err := runCommand("adb", "shell", installCmd); err != nil {
			return fmt.Errorf("failed to install LSPosed module after Magisk env fix: %w", err)
		} else {
			printSubStep("LSPosed installed successfully")
		}
	} else {
		printSubStep("LSPosed installed successfully")
	}

	printSubStep("Cleaning up temporary file")
	if _, err := runCommand("adb", "shell", "rm", devicePath); err != nil {
		printSubStepWarning("Failed to remove temporary file: " + devicePath)
	}

	printSubStep("Rebooting device to apply changes")
	if _, err := runCommand("adb", "reboot"); err != nil {
		return fmt.Errorf("failed to reboot device: %w", err)
	}

	printSubStep("Waiting for device to come back online...")
	if err := waitForDevice(); err != nil {
		return fmt.Errorf("emulator failed to start: %w", err)
	}

	printSubStep("LSPosed installed successfully")
	return nil
}

func getUIDump() (string, error) {
	_, _ = runCommand("adb", "shell", "rm", "/sdcard/window_dump.xml")
	_, _ = runCommand("adb", "shell", "uiautomator", "dump", "/sdcard/window_dump.xml")
	return runCommand("adb", "shell", "cat", "/sdcard/window_dump.xml")
}

func isTextOnScreen(text string) bool {
	output, err := getUIDump()
	if err != nil {
		return false
	}
	return strings.Contains(strings.ToLower(output), strings.ToLower(text))
}

func clickButtonByText(text string) error {
	xmlContent, err := getUIDump()
	if err != nil {
		return fmt.Errorf("failed to get UI dump: %w", err)
	}

	decoder := xml.NewDecoder(strings.NewReader(xmlContent))
	for {
		token, err := decoder.Token()
		if err == io.EOF {
			break
		}
		if err != nil {
			return fmt.Errorf("xml parse error: %w", err)
		}

		switch t := token.(type) {
		case xml.StartElement:
			if t.Name.Local == "node" {
				var currentText string
				var bounds string
				for _, attr := range t.Attr {
					if attr.Name.Local == "text" {
						currentText = attr.Value
					}
					if attr.Name.Local == "bounds" {
						bounds = attr.Value
					}
				}

				if strings.EqualFold(currentText, text) {
					cleaned := strings.ReplaceAll(bounds, "][", ",")
					cleaned = strings.ReplaceAll(cleaned, "[", "")
					cleaned = strings.ReplaceAll(cleaned, "]", "")
					coords := strings.Split(cleaned, ",")

					if len(coords) != 4 {
						continue
					}

					x1, _ := strconv.Atoi(coords[0])
					y1, _ := strconv.Atoi(coords[1])
					x2, _ := strconv.Atoi(coords[2])
					y2, _ := strconv.Atoi(coords[3])

					centerX := (x1 + x2) / 2
					centerY := (y1 + y2) / 2

					printSubStep(fmt.Sprintf("Tapping '%s' at %d, %d", text, centerX, centerY))
					_, err := runCommand("adb", "shell", "input", "tap", fmt.Sprintf("%d", centerX), fmt.Sprintf("%d", centerY))
					return err
				}
			}
		}
	}

	return fmt.Errorf("button with text '%s' not found", text)
}

func fixMagiskEnvironment() error {
	printStep("Finalizing Magisk Environment")

	printSubStep("Granting Magisk POST_NOTIFICATIONS permission")
	if _, err := runCommand("adb", "shell", "pm", "grant", "com.topjohnwu.magisk", "android.permission.POST_NOTIFICATIONS"); err != nil {
		return fmt.Errorf("failed to grant Magisk POST_NOTIFICATIONS permission: %w", err)
	}

	printSubStep("Opening Magisk App...")
	_, err := runCommand("adb", "shell", "monkey", "-p", "com.topjohnwu.magisk", "-c", "android.intent.category.LAUNCHER", "1")
	if err != nil {
		return fmt.Errorf("failed to launch Magisk app: %w", err)
	}

	printSubStep("Waiting for Magisk UI...")
	appLoaded := false
	for i := 0; i < 10; i++ {
		if isTextOnScreen("Additional Setup") {
			appLoaded = true
			break
		}
		time.Sleep(1 * time.Second)
	}

	if !appLoaded {
		printSubStepWarning("Magisk app did not seem to load in time, attempting blind interaction anyway...")
	} else {
		printSubStep("Magisk UI detected.")
	}

	printSubStep("Setup dialog detected! Handling it...")
	if err := clickButtonByText("OK"); err != nil {
		return fmt.Errorf("failed to click OK button: %w", err)
	} else {
		printSubStep("Clicked OK on Setup Dialog.")
	}

	time.Sleep(10 * time.Second)
	if err := waitForDevice(); err != nil {
		return fmt.Errorf("emulator failed to start: %w", err)
	}

	return nil
}

func stepBypass() error {
	printStep("Setting up bypass module")

	printSubStep("Checking if bypass module is already installed")
	output, err := runCommand("adb", "shell", "pm", "list", "packages")
	if err != nil {
		return fmt.Errorf("failed to list packages: %w", err)
	}

	if strings.Contains(strings.ToLower(output), LSPosedModuleName) {
		printSubStep("Bypass module already installed")
	} else {
		printSubStep("Downloading bypass module")
		bypassAPK := filepath.Join(tempDir, "bypass.apk")
		if err := downloadFile(bypassAPK, LSPosedModuleUrl); err != nil {
			return fmt.Errorf("failed to download bypass module: %w", err)
		}
		printSubStep("Downloaded bypass module: " + bypassAPK)

		printSubStep("Installing bypass module")
		if _, err := runCommand("adb", "install", "-r", bypassAPK); err != nil {
			return fmt.Errorf("failed to install bypass module: %w", err)
		}
	}

	printSubStep("Enabling bypass module")
	cmd := exec.Command("adb", "shell", "su", "-c", "/data/adb/lspd/bin/cli", "enable", LSPosedModuleName)
	if err := cmd.Run(); err != nil {
		return fmt.Errorf("failed to enable bypass module: %w", err)
	} else {
		printSubStep("Bypass module enabled")
	}

	return nil
}

func stepFridaServer() error {
	printStep("Installing Frida server")

	printSubStep("Checking if frida-server is running")
	if _, err := runCommand("frida-ps", "-U"); err == nil {
		printSubStep("frida-server is already running")
		return nil
	}

	printSubStep("Checking if frida-server exists on device")
	output, err := runCommand("adb", "shell", "su", "-c", "test -f /data/local/tmp/frida-server && echo yes || echo no")
	if err != nil {
		return fmt.Errorf("failed to check frida-server: %w", err)
	}

	if strings.TrimSpace(output) == "yes" {
		printSubStep("frida-server found, restarting")
		if _, err := runCommand("adb", "shell", "su", "-c", "pkill -9 frida-server"); err != nil {
			return fmt.Errorf("failed to kill frida-server: %w", err)
		}
		time.Sleep(2 * time.Second)

		printSubStep("Starting frida-server")
		cmd := exec.Command("adb", "shell", "su", "-c", "/data/local/tmp/frida-server &")
		if err := cmd.Start(); err != nil {
			return fmt.Errorf("failed to start frida-server: %w", err)
		}
		time.Sleep(5 * time.Second)
	} else {
		printSubStep("Downloading frida-server " + FridaVersion)
		fridaURL := fmt.Sprintf("https://github.com/frida/frida/releases/download/%s/frida-server-%s-%s.xz",
			FridaVersion, FridaVersion, fridaServerArch)
		xzPath := filepath.Join(tempDir, "frida-server.xz")

		if err := downloadFile(xzPath, fridaURL); err != nil {
			return fmt.Errorf("failed to download frida-server: %w", err)
		}

		printSubStep("Extracting frida-server")
		binPath := filepath.Join(tempDir, "frida-server")
		if err := extractXZ(xzPath, binPath); err != nil {
			return fmt.Errorf("failed to extract frida-server: %w", err)
		}

		printSubStep("Pushing frida-server to device")
		if _, err := runCommand("adb", "push", binPath, "/data/local/tmp/frida-server"); err != nil {
			return fmt.Errorf("failed to push frida-server: %w", err)
		}

		printSubStep("Setting executable permissions")
		if _, err := runCommand("adb", "shell", "su", "-c", "chmod 755 /data/local/tmp/frida-server"); err != nil {
			return fmt.Errorf("failed to set permissions: %w", err)
		}

		printSubStep("Starting frida-server")
		cmd := exec.Command("adb", "shell", "su", "-c", "/data/local/tmp/frida-server &")
		if err := cmd.Start(); err != nil {
			return fmt.Errorf("failed to start frida-server: %w", err)
		}
		time.Sleep(5 * time.Second)
	}

	printSubStep("Verifying frida-server connection")
	if _, err := runCommand("frida-ps", "-U"); err != nil {
		return fmt.Errorf("frida-server not responding: %w", err)
	}

	return nil
}

func stepBicoccApp() error {
	printStep("Installing BicoccApp")

	printSubStep("Checking if BicoccApp is installed")
	output, err := runCommand("adb", "shell", "pm", "list", "packages")
	if err != nil {
		return fmt.Errorf("failed to list packages: %w", err)
	}

	if strings.Contains(output, BicoccAppPackage) {
		printSubStep("BicoccApp already installed")
		return launchBicoccApp()
	}

	printSubStep("Opening Play Store for BicoccApp")
	playStoreURL := "https://play.google.com/store/apps/details?id=" + BicoccAppPackage + "&hl=it"
	if _, err := runCommand("adb", "shell", "am", "start", "-a", "android.intent.action.VIEW", "-d", playStoreURL); err != nil {
		return fmt.Errorf("failed to open Play Store: %w", err)
	}

	printSubStep("Waiting for manual installation (30 seconds)")
	time.Sleep(30 * time.Second)

	printSubStep("Verifying BicoccApp installation")
	output, err = runCommand("adb", "shell", "pm", "list", "packages")
	if err != nil {
		return fmt.Errorf("failed to verify installation: %w", err)
	}

	if !strings.Contains(output, BicoccAppPackage) {
		return fmt.Errorf("BicoccApp not found. Please install manually from: %s", playStoreURL)
	}

	return launchBicoccApp()
}

func launchBicoccApp() error {
	printSubStep("Launching BicoccApp")
	if _, err := runCommand("adb", "shell", "monkey", "-p", BicoccAppPackage, "-c", "android.intent.category.LAUNCHER", "1"); err != nil {
		return fmt.Errorf("failed to launch BicoccApp: %w", err)
	}
	printSubStep("BicoccApp launched")
	return nil
}

func pauseBeforeExit() {
	fmt.Println()
	fmt.Println(colorInfo.Render("Press Enter to exit..."))
	reader := bufio.NewReader(os.Stdin)
	_, _ = reader.ReadString('\n')
}

func main() {
	steps := []struct {
		name string
		fn   func() error
	}{
		{"Initialize", stepInitialize},
		{"System Tools", stepSystemTools},
		{"Python", stepPython},
		{"Frida", stepFrida},
		{"Java", stepJava},
		{"Android SDK", stepAndroidSDK},
		{"Emulator", stepEmulator},
		{"Root Access", stepRoot},
		{"LSPosed", stepLSPosed},
		{"Bypass Module", stepBypass},
		{"Frida Server", stepFridaServer},
		{"BicoccApp", stepBicoccApp},
	}

	for _, step := range steps {
		if err := step.fn(); err != nil {
			fmt.Println()
			printError("Setup failed at: " + step.name)
			printError(err.Error())

			if cmdErr, ok := err.(*CommandError); ok {
				fmt.Println()
				printInfo("Command Details:")
				printInfo("  Command: " + cmdErr.Command + " " + strings.Join(cmdErr.Args, " "))
				if cmdErr.Output != "" {
					printInfo("  Output: " + cmdErr.Output)
				}
			}
			fmt.Println()
			pauseBeforeExit()
			os.Exit(1)
		}
	}

	printComplete()

	// Cleanup
	if tempDir != "" {
		os.RemoveAll(tempDir)
	}

	// Pause before exit so window doesn't close
	pauseBeforeExit()
}
