package platform

import (
	"os/exec"
)

// Info holds platform-specific information
type Info struct {
	OS   string
	Arch string
}

// Platform-specific functions that must be implemented by each platform
type Platform interface {
	GetDefaultAndroidHome() string
	GetSDKToolsURL() string
	RefreshEnv()
	AddToPath(path string) error
	EnsurePackageManager() error
	InstallPackage(name, pkg string) error
	ConfigurePythonPath() error
	FindJavaHome() string
	KillExistingEmulators()
	GetEmulatorBinaryName() string
	GetRootAVDScript(rootAVDDir string) string
	LaunchInTerminal(command string) (*exec.Cmd, error)
}

// Package name getters
func GetGitPackage() string {
	return getGitPackage()
}

func GetSQLitePackage() string {
	return getSQLitePackage()
}

func GetPythonPackage() string {
	return getPythonPackage()
}

func GetJavaPackage() string {
	return getJavaPackage()
}

// Get platform info
func GetInfo() Info {
	return Info{
		OS:   osName,
		Arch: detectArchitecture(),
	}
}

// Global platform-specific functions (implemented per platform)
func GetDefaultAndroidHome() string {
	return getDefaultAndroidHome()
}

func GetSDKToolsURL() string {
	return getSDKToolsURL()
}

func RefreshEnv() {
	refreshEnv()
}

func AddToPath(path string) error {
	return addToPath(path)
}

func EnsurePackageManager() error {
	return ensurePackageManager()
}

func InstallPackage(name, pkg string) error {
	return installPackage(name, pkg)
}

func ConfigurePythonPath() error {
	return configurePythonPath()
}

func FindJavaHome() string {
	return findJavaHome()
}

func KillExistingEmulators() {
	killExistingEmulators()
}

func GetEmulatorBinaryName() string {
	return getEmulatorBinaryName()
}

func GetRootAVDScript(rootAVDDir string) string {
	return getRootAVDScript(rootAVDDir)
}

func LaunchInTerminal(command string) (*exec.Cmd, error) {
	return launchInTerminal(command)
}

func ConfigureDetachedProcess(cmd *exec.Cmd) {
	configureDetachedProcess(cmd)
}

func CommandExists(command string) bool {
	_, err := exec.LookPath(command)
	return err == nil
}

func detectArchitecture() string {
	return detectArch()
}
