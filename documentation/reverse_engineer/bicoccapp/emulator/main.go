package main

import (
	"archive/zip"
	"fmt"
	"io"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"runtime"
	"strings"
	"time"

	"github.com/charmbracelet/bubbles/spinner"
	tea "github.com/charmbracelet/bubbletea"
	"github.com/charmbracelet/lipgloss"
)

// Constants
const (
	FridaVersion    = "17.5.1"
	AVDName         = "Pixel_7_Pro_API_33"
	AndroidAPI      = "33"
	SystemImage     = "system-images;android-33;google_apis_playstore;x86_64"
	DeviceType      = "pixel_7_pro"
	BicoccAppPackage = "it.bicoccapp.unimib"
	LSPosedURL      = "https://github.com/mywalkb/LSPosed_mod/releases/download/v1.9.3_mod/LSPosed-v1.9.3_mod-7244-zygisk-release.zip"
	EmulatorTimeout = 180
)

// Runtime variables
var (
	detectedOS    string
	tempDir       string
	androidHome   string
	javaHome      string
	pythonCmd     string
)

// Styles
var (
	checkMark = lipgloss.NewStyle().
			Foreground(lipgloss.Color("#04B575")).
			Bold(true).
			Render("✓")

	crossMark = lipgloss.NewStyle().
			Foreground(lipgloss.Color("#FF0000")).
			Bold(true).
			Render("✗")

	stepTextStyle = lipgloss.NewStyle().
			Foreground(lipgloss.Color("#FFFFFF"))

	progressTextStyle = lipgloss.NewStyle().
				Foreground(lipgloss.Color("#00AAFF"))

	errorTextStyle = lipgloss.NewStyle().
			Foreground(lipgloss.Color("#FF0000"))

	titleStyle = lipgloss.NewStyle().
			Bold(true).
			Foreground(lipgloss.Color("#7D56F4")).
			MarginBottom(1)

	dimStyle = lipgloss.NewStyle().
			Foreground(lipgloss.Color("#666666"))

	percentageStyle = lipgloss.NewStyle().
			Foreground(lipgloss.Color("#7D56F4")).
			Bold(true)
)

// Installation steps
type step int

const (
	stepInit step = iota
	stepSystemTools
	stepPython
	stepFrida
	stepJava
	stepAndroidSDK
	stepEmulator
	stepRoot
	stepLSPosed
	stepBypass
	stepFridaServer
	stepBicoccApp
	stepComplete
)

func (s step) String() string {
	steps := []string{
		"Initializing",
		"Setting up system tools",
		"Configuring Python",
		"Installing Frida tools",
		"Setting up Java",
		"Installing Android SDK",
		"Setting up emulator",
		"Configuring root access",
		"Installing LSPosed",
		"Installing bypass module",
		"Installing Frida server",
		"Installing BicoccApp",
		"Complete",
	}
	if int(s) < len(steps) {
		return steps[s]
	}
	return "Unknown"
}

// Messages
type stepCompleteMsg struct {
	step    step
	message string
}

type stepErrorMsg struct {
	step     step
	stepName string
	err      error
}

type progressMsg struct {
	message string
}

// Model
type model struct {
	currentStep     step
	totalSteps      int
	spinner         spinner.Model
	completedSteps  []string
	currentProgress string
	err             error
	errStep         step
	quitting        bool
}

func initialModel() model {
	s := spinner.New()
	s.Spinner = spinner.Dot
	s.Style = lipgloss.NewStyle().Foreground(lipgloss.Color("#7D56F4"))

	return model{
		currentStep:    stepInit,
		totalSteps:     12,
		spinner:        s,
		completedSteps: []string{},
	}
}

func (m model) Init() tea.Cmd {
	return tea.Batch(
		m.spinner.Tick,
		runSetup,
	)
}

func (m model) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
	switch msg := msg.(type) {
	case tea.KeyMsg:
		if msg.String() == "ctrl+c" || msg.String() == "q" {
			m.quitting = true
			return m, tea.Quit
		}

	case spinner.TickMsg:
		var cmd tea.Cmd
		m.spinner, cmd = m.spinner.Update(msg)
		return m, cmd

	case progressMsg:
		m.currentProgress = string(msg.message)
		return m, nil

	case stepCompleteMsg:
		completedMsg := fmt.Sprintf("%s %s", checkMark, stepTextStyle.Render(msg.message))
		m.completedSteps = append(m.completedSteps, completedMsg)
		m.currentProgress = ""
		m.currentStep = msg.step + 1

		if m.currentStep >= stepComplete {
			m.quitting = true
			return m, tea.Quit
		}

		return m, continueSetup(m.currentStep)

	case stepErrorMsg:
		m.err = msg.err
		m.errStep = msg.step
		m.quitting = true
		return m, tea.Quit
	}

	return m, nil
}

func (m model) View() string {
	if m.quitting {
		if m.err != nil {
			// Build error output
			var output strings.Builder

			output.WriteString(titleStyle.Render("Android Emulator"))
			output.WriteString("\n\n")

			// Show all completed steps
			for _, step := range m.completedSteps {
				output.WriteString(step)
				output.WriteString("\n")
			}

			// Show failed step
			errorMsg := fmt.Sprintf("%s %s",
				crossMark,
				errorTextStyle.Render(m.errStep.String()))
			output.WriteString(errorMsg)
			output.WriteString("\n\n")

			// Show error details
			output.WriteString(errorTextStyle.Render("Error: "))
			output.WriteString(m.err.Error())
			output.WriteString("\n\n")

			output.WriteString(dimStyle.Render("Installation failed. Press Ctrl+C to exit."))
			output.WriteString("\n")

			return output.String()
		}

		// Success output
		var output strings.Builder

		output.WriteString(titleStyle.Render("Android Emulator"))
		output.WriteString("\n\n")

		// Show all completed steps
		for _, step := range m.completedSteps {
			output.WriteString(step)
			output.WriteString("\n")
		}

		output.WriteString("\n")
		output.WriteString(lipgloss.NewStyle().
			Foreground(lipgloss.Color("#04B575")).
			Bold(true).
			Render("✓ Setup Complete!"))
		output.WriteString("\n\n")

		output.WriteString(dimStyle.Render("Configuration:"))
		output.WriteString("\n")
		output.WriteString(dimStyle.Render("  • AVD Name: ") + AVDName)
		output.WriteString("\n")
		output.WriteString(dimStyle.Render("  • Android Version: 13 (API " + AndroidAPI + ")"))
		output.WriteString("\n")
		output.WriteString(dimStyle.Render("  • Device: Pixel 7 Pro"))
		output.WriteString("\n")
		output.WriteString(dimStyle.Render("  • Frida Version: " + FridaVersion))
		output.WriteString("\n\n")

		return output.String()
	}

	// Build in-progress output
	var output strings.Builder

	output.WriteString(titleStyle.Render("Android Emulator"))
	output.WriteString("\n\n")

	// Show completed steps
	for _, step := range m.completedSteps {
		output.WriteString(step)
		output.WriteString("\n")
	}

	// Show current step with spinner
	percentage := float64(m.currentStep) / float64(m.totalSteps) * 100
	currentMsg := fmt.Sprintf("%s %s %s",
		m.spinner.View(),
		progressTextStyle.Render(m.currentStep.String()),
		percentageStyle.Render(fmt.Sprintf("(%.0f%%)", percentage)))
	output.WriteString(currentMsg)
	output.WriteString("\n")

	// Show progress message if any
	if m.currentProgress != "" {
		output.WriteString(dimStyle.Render("  → " + m.currentProgress))
		output.WriteString("\n")
	}

	output.WriteString("\n")
	output.WriteString(dimStyle.Render("Press Ctrl+C to cancel"))
	output.WriteString("\n")

	return output.String()
}

func runSetup() tea.Msg {
	var err error

	// Detect OS
	detectedOS = detectOS()

	// Create temp directory
	tempDir, err = os.MkdirTemp("", "android-setup-*")
	if err != nil {
		return stepErrorMsg{
			step:     stepInit,
			stepName: stepInit.String(),
			err:      fmt.Errorf("failed to create temporary directory: %w", err),
		}
	}

	return stepCompleteMsg{stepInit, "Initialization complete"}
}

func continueSetup(currentStep step) tea.Cmd {
	return func() tea.Msg {
		var err error
		stepName := currentStep.String()

		switch currentStep {
		case stepSystemTools:
			err = setupSystemTools()
		case stepPython:
			err = setupPythonEnvironment()
		case stepFrida:
			err = setupFridaTools()
		case stepJava:
			err = setupJava()
		case stepAndroidSDK:
			err = setupAndroidSDK()
		case stepEmulator:
			err = setupEmulator()
		case stepRoot:
			err = setupRoot()
		case stepLSPosed:
			err = installLSPosed()
		case stepBypass:
			err = installBypassModule()
		case stepFridaServer:
			err = installFridaServer()
		case stepBicoccApp:
			err = installBicoccApp()
		}

		if err != nil {
			return stepErrorMsg{
				step:     currentStep,
				stepName: stepName,
				err:      err,
			}
		}

		return stepCompleteMsg{currentStep, stepName + " completed"}
	}
}

func main() {
	p := tea.NewProgram(initialModel())
	if _, err := p.Run(); err != nil {
		fmt.Printf("Error running program: %v\n", err)
		os.Exit(1)
	}

	// Cleanup
	if tempDir != "" {
		os.RemoveAll(tempDir)
	}
}

// Helper functions

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

func commandExists(command string) bool {
	_, err := exec.LookPath(command)
	return err == nil
}

func refreshEnv() error {
	switch detectedOS {
	case "windows":
		// Refresh PATH from registry on Windows
		cmd := exec.Command("powershell.exe", "-Command", `
			$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
			[Environment]::SetEnvironmentVariable("Path", $env:Path, "Process")
		`)
		if err := cmd.Run(); err != nil {
			return fmt.Errorf("failed to refresh environment: %w", err)
		}

		// Update current process PATH
		output, err := exec.Command("powershell.exe", "-Command", `
			[System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
		`).Output()
		if err == nil {
			os.Setenv("PATH", strings.TrimSpace(string(output)))
		}

	case "mac":
		// Add Homebrew to PATH if it exists but isn't in current PATH
		brewPaths := []string{
			"/opt/homebrew/bin",
			"/usr/local/bin",
		}

		currentPath := os.Getenv("PATH")
		for _, brewPath := range brewPaths {
			if _, err := os.Stat(brewPath); err == nil {
				if !strings.Contains(currentPath, brewPath) {
					os.Setenv("PATH", brewPath+string(os.PathListSeparator)+currentPath)
				}
			}
		}

	case "linux":
		// Refresh PATH from common profile files
		currentPath := os.Getenv("PATH")
		commonPaths := []string{"/usr/local/bin", "/usr/bin", "/bin", "/usr/local/sbin", "/usr/sbin", "/sbin"}
		for _, p := range commonPaths {
			if !strings.Contains(currentPath, p) {
				os.Setenv("PATH", currentPath+string(os.PathListSeparator)+p)
			}
		}
	}

	return nil
}

func runCommand(name string, args ...string) error {
	cmd := exec.Command(name, args...)
	var stderr strings.Builder
	cmd.Stdout = nil
	cmd.Stderr = &stderr

	err := cmd.Run()
	if err != nil {
		stderrStr := stderr.String()
		if stderrStr != "" {
			return fmt.Errorf("command '%s %s' failed: %w\nStderr: %s", name, strings.Join(args, " "), err, stderrStr)
		}
		return fmt.Errorf("command '%s %s' failed: %w", name, strings.Join(args, " "), err)
	}
	return nil
}

func runCommandWithOutput(name string, args ...string) (string, error) {
	cmd := exec.Command(name, args...)
	output, err := cmd.CombinedOutput()
	outputStr := strings.TrimSpace(string(output))
	if err != nil {
		return outputStr, fmt.Errorf("command '%s %s' failed: %w\nOutput: %s", name, strings.Join(args, " "), err, outputStr)
	}
	return outputStr, nil
}

func ensureWinget() error {
	if detectedOS != "windows" {
		return nil
	}

	if commandExists("winget") {
		return nil
	}

	// Install winget using PowerShell
	psScript := `
		$progressPreference = 'silentlyContinue'
		Install-PackageProvider -Name NuGet -Force | Out-Null
		Install-Module -Name Microsoft.WinGet.Client -Force -Repository PSGallery | Out-Null
		Repair-WinGetPackageManager -AllUsers | Out-Null
	`

	cmd := exec.Command("powershell.exe", "-ExecutionPolicy", "Bypass", "-Command", psScript)
	cmd.Stdout = nil
	cmd.Stderr = nil
	if err := cmd.Run(); err != nil {
		return fmt.Errorf("failed to install winget automatically. Please install manually from https://github.com/microsoft/winget-cli/releases/latest")
	}

	// Refresh environment to pick up winget
	if err := refreshEnv(); err != nil {
		return fmt.Errorf("failed to refresh environment after winget install: %w", err)
	}

	// Verify installation
	if !commandExists("winget") {
		return fmt.Errorf("winget installation completed but command not found. Please restart terminal and try again")
	}

	return nil
}

func ensureBrew() error {
	if detectedOS != "mac" {
		return nil
	}

	if commandExists("brew") {
		return nil
	}

	// Download and install Homebrew
	installScriptURL := "https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh"
	resp, err := http.Get(installScriptURL)
	if err != nil {
		return fmt.Errorf("failed to download Homebrew install script: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("failed to download Homebrew install script: HTTP %d", resp.StatusCode)
	}

	cmd := exec.Command("/bin/bash")
	cmd.Stdin = resp.Body
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	if err := cmd.Run(); err != nil {
		return fmt.Errorf("failed to install Homebrew: %w", err)
	}

	// Refresh environment to pick up brew
	if err := refreshEnv(); err != nil {
		return fmt.Errorf("failed to refresh environment after Homebrew install: %w", err)
	}

	// Verify installation
	if !commandExists("brew") {
		return fmt.Errorf("Homebrew installation completed but command not found. Please restart terminal and try again")
	}

	return nil
}

func setupSystemTools() error {
	// Ensure package managers are installed first
	if detectedOS == "windows" {
		// Check if winget exists before trying to ensure it
		wingetPath, wingetErr := exec.LookPath("winget")

		if err := ensureWinget(); err != nil {
			diagInfo := fmt.Sprintf("\nDiagnostics:")
			diagInfo += fmt.Sprintf("\n  OS: %s", runtime.GOOS)
			diagInfo += fmt.Sprintf("\n  ARCH: %s", runtime.GOARCH)
			diagInfo += fmt.Sprintf("\n  Winget lookup error: %v", wingetErr)
			diagInfo += fmt.Sprintf("\n  Winget path: %s", wingetPath)
			diagInfo += fmt.Sprintf("\n  PATH: %s", os.Getenv("PATH"))

			return fmt.Errorf("winget setup failed: %w%s", err, diagInfo)
		}
	} else if detectedOS == "mac" {
		brewPath, brewErr := exec.LookPath("brew")

		if err := ensureBrew(); err != nil {
			diagInfo := fmt.Sprintf("\nDiagnostics:")
			diagInfo += fmt.Sprintf("\n  OS: %s", runtime.GOOS)
			diagInfo += fmt.Sprintf("\n  ARCH: %s", runtime.GOARCH)
			diagInfo += fmt.Sprintf("\n  Brew lookup error: %v", brewErr)
			diagInfo += fmt.Sprintf("\n  Brew path: %s", brewPath)
			diagInfo += fmt.Sprintf("\n  PATH: %s", os.Getenv("PATH"))

			return fmt.Errorf("homebrew setup failed: %w%s", err, diagInfo)
		}
	}
    return nil
}

func installPackage(name, pkg string) error {
	var cmd *exec.Cmd
	var pkgManager string

	switch detectedOS {
	case "linux":
		if commandExists("apt-get") {
			cmd = exec.Command("sudo", "apt-get", "install", "-y", "-qq", pkg)
			pkgManager = "apt-get"
		} else if commandExists("yum") {
			cmd = exec.Command("sudo", "yum", "install", "-y", "-q", pkg)
			pkgManager = "yum"
		} else if commandExists("pacman") {
			cmd = exec.Command("sudo", "pacman", "-S", "--noconfirm", pkg)
			pkgManager = "pacman"
		} else {
			return fmt.Errorf("no supported package manager found on Linux. Requires one of: apt-get, yum, or pacman")
		}
	case "mac":
		if !commandExists("brew") {
			return fmt.Errorf("homebrew not found on macOS. Expected brew to be available at this point. Try running: /bin/bash -c \"$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\"")
		}
		cmd = exec.Command("brew", "install", pkg)
		pkgManager = "brew"
	case "windows":
		if !commandExists("winget") {
			return fmt.Errorf("winget not found on Windows. Expected winget to be available at this point. Please install from: https://github.com/microsoft/winget-cli/releases/latest")
		}

		cmd = exec.Command("winget", "install", pkg, "--silent", "--accept-package-agreements", "--accept-source-agreements")
		pkgManager = "winget"
	default:
		return fmt.Errorf("unsupported operating system: %s (expected: linux, mac, or windows)", detectedOS)
	}

	if cmd == nil {
		return fmt.Errorf("failed to create installation command for package '%s' (name: %s, manager: %s)", pkg, name, pkgManager)
	}

    // Run the command: don't handle the error
	cmd.Run()

	// Refresh environment after package installation
	if err := refreshEnv(); err != nil {
		// Non-fatal - log but continue
		fmt.Fprintf(os.Stderr, "Warning: failed to refresh environment after installing %s: %v\n", name, err)
	}

	return nil
}

func setupPythonEnvironment() error {
	_, python3Err := runCommandWithOutput("python3", "--version")
    if python3Err == nil {
        pythonCmd = "python3"
    	return nil
    }

	_, pythonErr := runCommandWithOutput("python", "--version")
    if pythonErr == nil {
    	pythonCmd = "python"
    	return nil
    }

	if pythonCmd == "" {
		pkg := "python3"
		if detectedOS == "windows" {
			pkg = "Python.Python.3.12"
		}
		if err := installPackage("Python", pkg); err != nil {
			return err
		}

        // Refresh environment after installation
        if detectedOS == "windows" {
        	pythonPath := filepath.Join(os.Getenv("LOCALAPPDATA"), "Programs", "Python", "Python312")
        	if err := addToPath(pythonPath); err != nil {
        	    return fmt.Errorf("failed to add Python to the path: %w", err)
        	}
        }

		// Verify installation
        _, verifyPython3Err := runCommandWithOutput("python3", "--version")
        if verifyPython3Err == nil {
            pythonCmd = "python3"
            return nil
        }

        _, verifyPythonErr := runCommandWithOutput("python", "--version")
        if verifyPythonErr == nil {
            pythonCmd = "python"
            return nil
        }

		return fmt.Errorf("Python installed but not functioning correctly. Please restart terminal and try again")
	}

	return nil
}

func addToPath(newPath string) error {
	switch detectedOS {
	case "windows":
		// Get current user PATH
		cmd := exec.Command("powershell.exe", "-Command",
			`[Environment]::GetEnvironmentVariable("Path", "User")`)
		output, err := cmd.Output()
		if err != nil {
			return fmt.Errorf("failed to get current PATH: %w", err)
		}

		currentPath := strings.TrimSpace(string(output))

		// Check if already in PATH
		if strings.Contains(currentPath, newPath) {
			return nil
		}

		// Add to PATH
		var updatedPath string
		if currentPath == "" {
			updatedPath = newPath
		} else {
			updatedPath = newPath + ";" + currentPath
		}

		// Set permanently
		cmd = exec.Command("powershell.exe", "-Command",
			fmt.Sprintf(`[Environment]::SetEnvironmentVariable("Path", "%s", "User")`, updatedPath))
		if err := cmd.Run(); err != nil {
			return fmt.Errorf("failed to update PATH: %w", err)
		}

		// Update current process
		os.Setenv("PATH", newPath+";"+os.Getenv("PATH"))

	case "mac", "linux":
		home := os.Getenv("HOME")
		shellConfigs := []string{
			filepath.Join(home, ".bashrc"),
			filepath.Join(home, ".zshrc"),
			filepath.Join(home, ".profile"),
		}

		exportLine := fmt.Sprintf("\nexport PATH=\"%s:$PATH\"\n", newPath)

		// Add to shell configs
		for _, config := range shellConfigs {
			if _, err := os.Stat(config); err == nil {
				// Check if already in file
				content, _ := os.ReadFile(config)
				if strings.Contains(string(content), newPath) {
					continue
				}

				f, err := os.OpenFile(config, os.O_APPEND|os.O_WRONLY, 0644)
				if err != nil {
					continue
				}
				f.WriteString(exportLine)
				f.Close()
			}
		}

		// Update current process
		os.Setenv("PATH", newPath+":"+os.Getenv("PATH"))
	}

	return nil
}

func setupFridaTools() error {
	if !commandExists("frida") {
		cmd := exec.Command(pythonCmd, "-m", "pip", "install", "--quiet", "frida-tools")
		cmd.Stdout = nil
		cmd.Stderr = nil
		if err := cmd.Run(); err != nil {
			return err
		}
	}
	return nil
}

func setupJava() error {
	if !commandExists("java") {
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
			output, _ := runCommandWithOutput("/usr/libexec/java_home")
			javaHome = output
		} else {
			javaPath, _ := exec.LookPath("java")
			javaHome = filepath.Dir(filepath.Dir(javaPath))
		}
		if javaHome != "" {
			os.Setenv("JAVA_HOME", javaHome)
		}
	}

	return nil
}

func setupAndroidSDK() error {
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
		os.Setenv("ANDROID_HOME", androidHome)
	}

	if !commandExists("sdkmanager") {
		if err := downloadAndInstallSDKTools(); err != nil {
			return err
		}
	}

	acceptLicenses()

	if err := installSDKComponent("platform-tools", "Platform-Tools"); err != nil {
		return err
	}
	if err := installSDKComponent("emulator", "Emulator"); err != nil {
		return err
	}
	if err := installSDKComponent(SystemImage, "System image"); err != nil {
		return err
	}

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

	binPath := filepath.Join(latestDir, "bin")
	os.Setenv("PATH", binPath+string(os.PathListSeparator)+os.Getenv("PATH"))

	return nil
}

func acceptLicenses() {
	sdkmanager := filepath.Join(androidHome, "cmdline-tools", "latest", "bin", "sdkmanager")
	if detectedOS == "windows" {
		sdkmanager += ".bat"
	}

	cmd := exec.Command(sdkmanager, "--licenses")
	stdin, _ := cmd.StdinPipe()
	cmd.Stdout = nil
	cmd.Stderr = nil

	cmd.Start()
	for i := 0; i < 10; i++ {
		stdin.Write([]byte("y\n"))
	}
	stdin.Close()
	cmd.Wait()
}

func installSDKComponent(component, name string) error {
	sdkmanager := filepath.Join(androidHome, "cmdline-tools", "latest", "bin", "sdkmanager")
	if detectedOS == "windows" {
		sdkmanager += ".bat"
	}

	output, _ := runCommandWithOutput(sdkmanager, "--list_installed")
	if strings.Contains(output, component) {
		return nil
	}

	cmd := exec.Command(sdkmanager, component)
	stdin, _ := cmd.StdinPipe()
	cmd.Stdout = nil
	cmd.Stderr = nil

	cmd.Start()
	stdin.Write([]byte("y\n"))
	stdin.Close()

	return cmd.Wait()
}

func setupEmulator() error {
	avdDir := filepath.Join(os.Getenv("HOME"), ".android", "avd", AVDName+".avd")
	if _, err := os.Stat(avdDir); os.IsNotExist(err) {
		avdmanager := filepath.Join(androidHome, "cmdline-tools", "latest", "bin", "avdmanager")
		if detectedOS == "windows" {
			avdmanager += ".bat"
		}

		cmd := exec.Command(avdmanager, "create", "avd",
			"-n", AVDName,
			"-k", SystemImage,
			"--device", DeviceType)
		stdin, _ := cmd.StdinPipe()
		cmd.Stdout = nil
		cmd.Stderr = nil

		cmd.Start()
		stdin.Write([]byte("no\n"))
		stdin.Close()
		cmd.Wait()
	}

	return startEmulator()
}

func startEmulator() error {
	emulatorBin := filepath.Join(androidHome, "emulator", "emulator")
	if detectedOS == "windows" {
		emulatorBin += ".exe"
	}

	if _, err := os.Stat(emulatorBin); os.IsNotExist(err) {
		return fmt.Errorf("emulator binary not found at expected path '%s'. Android SDK may not be properly installed. ANDROID_HOME: %s", emulatorBin, androidHome)
	}

	killExistingEmulators()
	time.Sleep(3 * time.Second)

	if err := runCommand("adb", "kill-server"); err != nil {
		return fmt.Errorf("failed to kill adb server before starting emulator: %w", err)
	}

	if err := runCommand("adb", "start-server"); err != nil {
		return fmt.Errorf("failed to start adb server: %w", err)
	}
	time.Sleep(2 * time.Second)

	cmd := exec.Command(emulatorBin, "-avd", AVDName, "-writable-system", "-no-snapshot-load")
	var stderr strings.Builder
	cmd.Stderr = &stderr

	if err := cmd.Start(); err != nil {
		return fmt.Errorf("failed to start emulator '%s' with command '%s -avd %s -writable-system -no-snapshot-load': %w", AVDName, emulatorBin, AVDName, err)
	}

	time.Sleep(5 * time.Second)

	if err := waitForDevice(); err != nil {
		stderrStr := stderr.String()
		if stderrStr != "" {
			return fmt.Errorf("emulator failed to start properly: %w\nEmulator stderr: %s", err, stderrStr)
		}
		return fmt.Errorf("emulator started but failed to become ready: %w", err)
	}

	return nil
}

func killExistingEmulators() {
	if detectedOS == "windows" {
		exec.Command("taskkill", "/F", "/IM", "emulator.exe").Run()
		exec.Command("taskkill", "/F", "/IM", "qemu-system-x86_64.exe").Run()
	} else {
		exec.Command("pkill", "-9", "-f", "emulator.*avd").Run()
		exec.Command("pkill", "-9", "qemu-system").Run()
	}
}

func waitForDevice() error {
	elapsed := 0
	var lastOutput string
	var lastErr error

	for elapsed < EmulatorTimeout {
		output, err := runCommandWithOutput("adb", "devices")
		lastOutput = output
		lastErr = err

		if err == nil && strings.Contains(output, "emulator") {
			break
		}
		time.Sleep(3 * time.Second)
		elapsed += 3
	}

	if elapsed >= EmulatorTimeout {
		errMsg := fmt.Sprintf("timeout waiting for emulator to appear in 'adb devices' after %d seconds", EmulatorTimeout)
		if lastErr != nil {
			errMsg += fmt.Sprintf("\nLast adb error: %v", lastErr)
		}
		if lastOutput != "" {
			errMsg += fmt.Sprintf("\nLast adb devices output:\n%s", lastOutput)
		}
		return fmt.Errorf("%s", errMsg)
	}

	if err := runCommand("adb", "wait-for-device"); err != nil {
		return fmt.Errorf("adb wait-for-device command failed: %w", err)
	}

	bootTimeout := 300 // 5 minutes for boot
	bootElapsed := 0
	for bootElapsed < bootTimeout {
		output, err := runCommandWithOutput("adb", "shell", "getprop", "sys.boot_completed")
		if err == nil && strings.TrimSpace(output) == "1" {
			break
		}

		time.Sleep(3 * time.Second)
		bootElapsed += 3

		if bootElapsed >= bootTimeout {
			return fmt.Errorf("timeout waiting for emulator boot to complete after %d seconds. sys.boot_completed property never became '1'", bootTimeout)
		}
	}

	time.Sleep(5 * time.Second)
	return nil
}

func setupRoot() error {
	output, err := runCommandWithOutput("adb", "shell", "su", "-c", "id")
	if err != nil {
		return fmt.Errorf("failed to check root access on emulator: %w\nNote: Emulator may not have root access enabled. Use an AVD image with Google APIs (not Play Store) for root access", err)
	}

	if strings.Contains(strings.ToLower(output), "uid=0") {
		return nil
	}

	return fmt.Errorf("root access not available on emulator. Output from 'su -c id': %s\nNote: For root access, create an AVD with Google APIs system image (not Play Store variant)", output)
}

func installLSPosed() error {
	output, err := runCommandWithOutput("adb", "shell", "pm", "list", "packages")
	if err != nil {
		return fmt.Errorf("failed to list packages on emulator to check for LSPosed: %w", err)
	}

	if strings.Contains(strings.ToLower(output), "lsposed") {
		return nil
	}

	zipPath := filepath.Join(tempDir, "LSPosed.zip")
	if err := downloadFile(zipPath, LSPosedURL); err != nil {
		return fmt.Errorf("failed to download LSPosed from %s: %w", LSPosedURL, err)
	}

	extractDir := filepath.Join(tempDir, "lsposed_extracted")
	if err := unzip(zipPath, extractDir); err != nil {
		return fmt.Errorf("failed to extract LSPosed zip file '%s': %w", zipPath, err)
	}

	apkPath := findAPKInDir(extractDir)
	if apkPath == "" {
		return fmt.Errorf("LSPosed APK not found in extracted directory '%s'. Downloaded zip may be corrupted or structure changed", extractDir)
	}

	if err := runCommand("adb", "install", "-r", apkPath); err != nil {
		return fmt.Errorf("failed to install LSPosed APK '%s' to emulator: %w", apkPath, err)
	}

	return nil
}

func installBypassModule() error {
	execDir, err := os.Executable()
	if err != nil {
		return fmt.Errorf("failed to get executable directory path: %w", err)
	}

	bypassAPK := filepath.Join(filepath.Dir(execDir), "magisk", "bin", "bypass.apk")

	if _, err := os.Stat(bypassAPK); os.IsNotExist(err) {
		return fmt.Errorf("bypass APK not found at expected path '%s': %w\nPlease ensure the bypass.apk file exists in the magisk/bin directory relative to the executable", bypassAPK, err)
	}

	output, err := runCommandWithOutput("adb", "shell", "pm", "list", "packages")
	if err != nil {
		return fmt.Errorf("failed to list packages on emulator to check for bypass module: %w", err)
	}

	if strings.Contains(strings.ToLower(output), "bypass") {
		return nil
	}

	if err := runCommand("adb", "install", "-r", bypassAPK); err != nil {
		return fmt.Errorf("failed to install bypass module APK '%s' to emulator: %w", bypassAPK, err)
	}

	return nil
}

func installFridaServer() error {
	if err := runCommand("frida-ps", "-U"); err == nil {
		return nil
	}

	output, err := runCommandWithOutput("adb", "shell", "su", "-c", "test -f /data/local/tmp/frida-server && echo yes || echo no")
	if err != nil {
		return fmt.Errorf("failed to check if frida-server exists on emulator: %w", err)
	}

	if strings.TrimSpace(output) == "yes" {
		if err := runCommand("adb", "shell", "su", "-c", "pkill -9 frida-server"); err != nil {
			return fmt.Errorf("failed to kill existing frida-server process: %w", err)
		}
		time.Sleep(2 * time.Second)

		cmd := exec.Command("adb", "shell", "su", "-c", "/data/local/tmp/frida-server &")
		if err := cmd.Start(); err != nil {
			return fmt.Errorf("failed to start existing frida-server on emulator: %w", err)
		}
		time.Sleep(5 * time.Second)
	} else {
		fridaURL := fmt.Sprintf("https://github.com/frida/frida/releases/download/%s/frida-server-%s-android-x86_64.xz",
			FridaVersion, FridaVersion)
		xzPath := filepath.Join(tempDir, "frida-server.xz")

		if err := downloadFile(xzPath, fridaURL); err != nil {
			return fmt.Errorf("failed to download frida-server version %s: %w", FridaVersion, err)
		}

		binPath := filepath.Join(tempDir, "frida-server")
		if err := extractXZ(xzPath, binPath); err != nil {
			return fmt.Errorf("failed to extract frida-server from xz archive: %w", err)
		}

		if err := runCommand("adb", "push", binPath, "/data/local/tmp/frida-server"); err != nil {
			return fmt.Errorf("failed to push frida-server binary '%s' to emulator: %w", binPath, err)
		}

		if err := runCommand("adb", "shell", "su", "-c", "chmod 755 /data/local/tmp/frida-server"); err != nil {
			return fmt.Errorf("failed to set executable permissions on frida-server: %w", err)
		}

		cmd := exec.Command("adb", "shell", "su", "-c", "/data/local/tmp/frida-server &")
		if err := cmd.Start(); err != nil {
			return fmt.Errorf("failed to start frida-server on emulator: %w", err)
		}
		time.Sleep(5 * time.Second)
	}

	// Verify frida-server is running
	if err := runCommand("frida-ps", "-U"); err != nil {
		return fmt.Errorf("frida-server installed but not responding. Failed to connect with frida-ps: %w", err)
	}

	return nil
}

func installBicoccApp() error {
	output, err := runCommandWithOutput("adb", "shell", "pm", "list", "packages")
	if err != nil {
		return fmt.Errorf("failed to list packages on emulator to check for BicoccApp: %w", err)
	}

	if strings.Contains(output, BicoccAppPackage) {
		return launchBicoccApp()
	}

	playStoreURL := "https://play.google.com/store/apps/details?id=" + BicoccAppPackage + "&hl=it"
	if err := runCommand("adb", "shell", "am", "start", "-a", "android.intent.action.VIEW", "-d", playStoreURL); err != nil {
		return fmt.Errorf("failed to open Play Store for BicoccApp installation (package: %s): %w\nPlease install manually from: %s", BicoccAppPackage, err, playStoreURL)
	}

	// Wait for manual installation
	time.Sleep(30 * time.Second)

	// Verify installation
	output, err = runCommandWithOutput("adb", "shell", "pm", "list", "packages")
	if err != nil {
		return fmt.Errorf("failed to verify BicoccApp installation: %w", err)
	}

	if !strings.Contains(output, BicoccAppPackage) {
		return fmt.Errorf("BicoccApp (package: %s) not found after waiting for installation. Please install it manually from Play Store: %s", BicoccAppPackage, playStoreURL)
	}

	return launchBicoccApp()
}

func launchBicoccApp() error {
	if err := runCommand("adb", "shell", "monkey", "-p", BicoccAppPackage, "-c", "android.intent.category.LAUNCHER", "1"); err != nil {
		return fmt.Errorf("failed to launch BicoccApp (package: %s): %w", BicoccAppPackage, err)
	}
	return nil
}

func downloadFile(filepath, url string) error {
	resp, err := http.Get(url)
	if err != nil {
		return fmt.Errorf("failed to download from '%s': %w", url, err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("failed to download from '%s': HTTP status %d (%s)", url, resp.StatusCode, resp.Status)
	}

	out, err := os.Create(filepath)
	if err != nil {
		return fmt.Errorf("failed to create file '%s' for download: %w", filepath, err)
	}
	defer out.Close()

	_, err = io.Copy(out, resp.Body)
	if err != nil {
		return fmt.Errorf("failed to write downloaded content to '%s': %w", filepath, err)
	}
	return nil
}

func unzip(src, dest string) error {
	r, err := zip.OpenReader(src)
	if err != nil {
		return fmt.Errorf("failed to open zip file '%s': %w", src, err)
	}
	defer r.Close()

	for _, f := range r.File {
		fpath := filepath.Join(dest, f.Name)

		if f.FileInfo().IsDir() {
			if err := os.MkdirAll(fpath, os.ModePerm); err != nil {
				return fmt.Errorf("failed to create directory '%s' from zip: %w", fpath, err)
			}
			continue
		}

		if err = os.MkdirAll(filepath.Dir(fpath), os.ModePerm); err != nil {
			return fmt.Errorf("failed to create parent directory for '%s': %w", fpath, err)
		}

		outFile, err := os.OpenFile(fpath, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, f.Mode())
		if err != nil {
			return fmt.Errorf("failed to create file '%s' from zip: %w", fpath, err)
		}

		rc, err := f.Open()
		if err != nil {
			outFile.Close()
			return fmt.Errorf("failed to open file '%s' in zip archive: %w", f.Name, err)
		}

		_, err = io.Copy(outFile, rc)
		outFile.Close()
		rc.Close()

		if err != nil {
			return fmt.Errorf("failed to extract file '%s' from zip: %w", f.Name, err)
		}
	}
	return nil
}

func extractXZ(xzPath, outputPath string) error {
	if !commandExists("xz") {
		return fmt.Errorf("xz utility not found on system. Please install xz-utils package")
	}

	cmd := exec.Command("xz", "-d", xzPath)
	var stderr strings.Builder
	cmd.Stderr = &stderr

	if err := cmd.Run(); err != nil {
		return fmt.Errorf("failed to decompress '%s' with xz: %w\nStderr: %s", xzPath, err, stderr.String())
	}

	decompressedPath := strings.TrimSuffix(xzPath, ".xz")
	if _, err := os.Stat(decompressedPath); os.IsNotExist(err) {
		return fmt.Errorf("decompressed file '%s' not found after xz extraction", decompressedPath)
	}

	if err := os.Rename(decompressedPath, outputPath); err != nil {
		return fmt.Errorf("failed to move decompressed file from '%s' to '%s': %w", decompressedPath, outputPath, err)
	}
	return nil
}

func findAPKInDir(dir string) string {
	var apkPath string
	filepath.Walk(dir, func(path string, info os.FileInfo, err error) error {
		if err == nil && !info.IsDir() && strings.HasSuffix(path, ".apk") {
			apkPath = path
			return filepath.SkipDir
		}
		return nil
	})
	return apkPath
}