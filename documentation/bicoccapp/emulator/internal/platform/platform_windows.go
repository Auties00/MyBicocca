//go:build windows

package platform

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
	"syscall"
)

const (
	osName           = "windows"
	pathSeparator    = ";"
	createNewConsole = 0x10
)

func getDefaultAndroidHome() string {
	return filepath.Join(os.Getenv("LOCALAPPDATA"), "Android", "Sdk")
}

func getSDKToolsURL() string {
	return "https://dl.google.com/android/repository/commandlinetools-win-11076708_latest.zip"
}

func refreshEnv() {
	output, err := exec.Command("powershell.exe", "-Command", `
		[System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
	`).Output()
	if err == nil {
		os.Setenv("PATH", strings.TrimSpace(string(output)))
	}
}

func addToPath(newPath string) error {
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

	os.Setenv("PATH", newPath+";"+os.Getenv("PATH"))
	return nil
}

func ensurePackageManager() error {
	if CommandExists("winget") {
		return nil
	}

	psScript := `
		$progressPreference = 'silentlyContinue'
		Install-PackageProvider -Name NuGet -Force | Out-Null
		Install-Module -Name Microsoft.WinGet.Client -Force -Repository PSGallery | Out-Null
		Repair-WinGetPackageManager -AllUsers | Out-Null
	`

	cmd := exec.Command("powershell.exe", "-ExecutionPolicy", "Bypass", "-Command", psScript)
	cmd.Run()

	refreshEnv()

	if !CommandExists("winget") {
		return fmt.Errorf("winget installation completed but command not found - restart terminal")
	}

	return nil
}

func installPackage(name, pkg string) error {
	if !CommandExists("winget") {
		return fmt.Errorf("winget not found")
	}

	cmd := exec.Command("winget", "install", pkg, "--silent", "--accept-package-agreements", "--accept-source-agreements")
	cmd.Run()

	refreshEnv()
	return nil
}

func getGitPackage() string {
	return "Git.Git"
}

func getPythonPackage() string {
	return "Python.Python.3.12"
}

func getJavaPackage() string {
	return "Oracle.JDK.17"
}

func configurePythonPath() error {
	pythonPath := filepath.Join(os.Getenv("LOCALAPPDATA"), "Programs", "Python", "Python312")
	return addToPath(pythonPath)
}

func findJavaHome() string {
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

func killExistingEmulators() {
	for _, proc := range []string{"qemu-system-x86_64.exe", "emulator.exe"} {
		exec.Command("taskkill", "/F", "/IM", proc).Run()
	}
}

func getEmulatorBinaryName() string {
	return "emulator.exe"
}

func getRootAVDScript(rootAVDDir string) string {
	return filepath.Join(rootAVDDir, "rootAVD.bat")
}

func launchInTerminal(command string) (*exec.Cmd, error) {
	cmdStr := fmt.Sprintf(`%s & echo. & echo Press any key to close... & pause >nul`, command)
	cmd := exec.Command("cmd", "/c", cmdStr)
	return cmd, nil
}

func configureDetachedProcess(cmd *exec.Cmd) {
	cmd.SysProcAttr = &syscall.SysProcAttr{
		CreationFlags:    createNewConsole,
		NoInheritHandles: true,
	}
}
