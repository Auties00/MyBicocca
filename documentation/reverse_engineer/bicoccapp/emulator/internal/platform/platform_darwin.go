//go:build darwin

package platform

import (
	"fmt"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
)

const (
	osName        = "mac"
	pathSeparator = ":"
)

func getDefaultAndroidHome() string {
	return filepath.Join(os.Getenv("HOME"), "Library", "Android", "sdk")
}

func getSDKToolsURL() string {
	return "https://dl.google.com/android/repository/commandlinetools-mac-11076708_latest.zip"
}

func refreshEnv() {
	brewPaths := []string{"/opt/homebrew/bin", "/usr/local/bin"}
	currentPath := os.Getenv("PATH")
	for _, brewPath := range brewPaths {
		if _, err := os.Stat(brewPath); err == nil && !strings.Contains(currentPath, brewPath) {
			os.Setenv("PATH", brewPath+string(os.PathListSeparator)+currentPath)
		}
	}
}

func addToPath(newPath string) error {
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
				f.WriteString(exportLine)
				f.Close()
			}
		}
	}

	return os.Setenv("PATH", newPath+":"+os.Getenv("PATH"))
}

func ensurePackageManager() error {
	if CommandExists("brew") {
		return nil
	}

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

	if !CommandExists("brew") {
		return fmt.Errorf("homebrew installation completed but command not found - restart terminal")
	}

	return nil
}

func installPackage(name, pkg string) error {
	if !CommandExists("brew") {
		return fmt.Errorf("homebrew not found")
	}

	cmd := exec.Command("brew", "install", pkg)
	cmd.Run()

	refreshEnv()
	return nil
}

func getGitPackage() string {
	return "git"
}

func getSQLitePackage() string {
	return "sqlite3"
}

func getPythonPackage() string {
	return "python3"
}

func getJavaPackage() string {
	return "openjdk@17"
}

func configurePythonPath() error {
	return nil
}

func findJavaHome() string {
	output, _ := exec.Command("/usr/libexec/java_home").Output()
	return strings.TrimSpace(string(output))
}

func killExistingEmulators() {
	exec.Command("pkill", "-9", "emulator").Run()
	exec.Command("pkill", "-9", "qemu-system").Run()
}

func getEmulatorBinaryName() string {
	return "emulator"
}

func getRootAVDScript(rootAVDDir string) string {
	return filepath.Join(rootAVDDir, "rootAVD.sh")
}

func escapeAppleScript(s string) string {
	s = strings.ReplaceAll(s, `\`, `\\`)
	s = strings.ReplaceAll(s, `"`, `\"`)
	return s
}

func launchInTerminal(command string) (*exec.Cmd, error) {
	script := fmt.Sprintf(`tell application "Terminal"
    activate
    do script "%s; echo ''; echo 'Press Enter to close...'; read"
end tell`, escapeAppleScript(command))
	cmd := exec.Command("osascript", "-e", script)
	return cmd, nil
}

func configureDetachedProcess(cmd *exec.Cmd) {
	// No special configuration needed for macOS
}
