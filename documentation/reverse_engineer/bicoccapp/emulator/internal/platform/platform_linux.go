//go:build linux

package platform

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
)

const (
	osName        = "linux"
	pathSeparator = ":"
)

func getDefaultAndroidHome() string {
	return filepath.Join(os.Getenv("HOME"), "Android", "Sdk")
}

func getSDKToolsURL() string {
	return "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
}

func refreshEnv() {
	currentPath := os.Getenv("PATH")
	commonPaths := []string{"/usr/local/bin", "/usr/bin", "/bin"}
	for _, p := range commonPaths {
		if !strings.Contains(currentPath, p) {
			os.Setenv("PATH", currentPath+string(os.PathListSeparator)+p)
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
	if CommandExists("apt-get") || CommandExists("yum") || CommandExists("pacman") {
		return nil
	}
	return fmt.Errorf("no supported package manager found (apt-get, yum, or pacman)")
}

func installPackage(name, pkg string) error {
	var cmd *exec.Cmd

	if CommandExists("apt-get") {
		cmd = exec.Command("sudo", "apt-get", "install", "-y", "-qq", pkg)
	} else if CommandExists("yum") {
		cmd = exec.Command("sudo", "yum", "install", "-y", "-q", pkg)
	} else if CommandExists("pacman") {
		cmd = exec.Command("sudo", "pacman", "-S", "--noconfirm", pkg)
	} else {
		return fmt.Errorf("no supported package manager found")
	}

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
	return "openjdk-17-jdk"
}

func configurePythonPath() error {
	return nil
}

func findJavaHome() string {
	if javaPath, _ := exec.LookPath("java"); javaPath != "" {
		return filepath.Dir(filepath.Dir(javaPath))
	}
	return ""
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

func launchInTerminal(command string) (*exec.Cmd, error) {
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
			cmd := exec.Command(term.name, term.args...)
			return cmd, nil
		}
	}

	return nil, fmt.Errorf("no terminal emulator found")
}

func configureDetachedProcess(cmd *exec.Cmd) {
	// No special configuration needed for Linux
}
