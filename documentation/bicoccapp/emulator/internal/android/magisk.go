package android

import (
	"fmt"
	"path/filepath"
	"strings"
	"time"
)

type Magisk struct {
	TempDir string
}

func NewMagisk(tempDir string) *Magisk {
	return &Magisk{
		TempDir: tempDir,
	}
}

func (m *Magisk) IsZygiskEnabled() bool {
	command := "\"magisk --sqlite 'select value from settings where (key=\\\"zygisk\\\");'\""
	output, err := runCommand("adb", "shell", "su", "-c", command)
	return err == nil && strings.Contains(output, "value=1")
}

func (m *Magisk) EnableZygisk() error {
	command := "\"magisk --sqlite 'replace into settings (key,value) values(\\\"zygisk\\\",1);'\""
	_, err := runCommand("adb", "shell", "su", "-c", command)
	return err
}

func (m *Magisk) ConfigureAutoAllow() error {
	command := "magisk resetprop persist.sys.su.mode 2"
	_, err := runCommand("adb", "shell", "su", "-c", command)
	return err
}

func (m *Magisk) FixEnvironment() error {
	if _, err := runCommand("adb", "shell", "pm", "grant", "com.topjohnwu.magisk", "android.permission.POST_NOTIFICATIONS"); err != nil {
		return fmt.Errorf("Magisk permission grant failed: %w", err)
	}

	_, err := runCommand("adb", "shell", "monkey", "-p", "com.topjohnwu.magisk", "-c", "android.intent.category.LAUNCHER", "1")
	if err != nil {
		return fmt.Errorf("Magisk app launch failed: %w", err)
	}

	return nil
}

func (m *Magisk) InstallLSPosed(lsposedURL string, emulator *Emulator) error {
	modulesOutput, err := runCommand("adb", "shell", "su", "-c", "ls", "-1", "/data/adb/modules")
	if err != nil {
		return fmt.Errorf("failed to list modules: %w", err)
	}

	if strings.Contains(modulesOutput, "lsposed") {
		return nil // Already installed
	}

	zipPath := filepath.Join(m.TempDir, "LSPosed.zip")
	if err := downloadFile(zipPath, lsposedURL); err != nil {
		return fmt.Errorf("LSPosed download failed: %w", err)
	}

	devicePath := "/sdcard/LSPosed.zip"
	if _, err := runCommand("adb", "push", zipPath, devicePath); err != nil {
		return fmt.Errorf("LSPosed push failed: %w", err)
	}

	installCmd := fmt.Sprintf("su -c 'magisk --install-module %s'", devicePath)
	if _, err := runCommand("adb", "shell", installCmd); err != nil {
		if err := m.FixEnvironment(); err != nil {
			return fmt.Errorf("Magisk environment fix failed: %w", err)
		}

		if _, err := runCommand("adb", "shell", installCmd); err != nil {
			return fmt.Errorf("LSPosed installation failed: %w", err)
		}
	}

	runCommand("adb", "shell", "rm", devicePath)

	return emulator.Reboot()
}

func (m *Magisk) InstallBypassModule(moduleURL, moduleName, targetPackage string) error {
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

	if _, exists := modules[moduleName]; !exists {
		bypassAPK := filepath.Join(m.TempDir, "bypass.apk")
		if err := downloadFile(bypassAPK, moduleURL); err != nil {
			return fmt.Errorf("bypass module download failed: %w", err)
		}

		if _, err := runCommand("adb", "install", "-r", bypassAPK); err != nil {
			return fmt.Errorf("bypass module installation failed: %w", err)
		}
	}

	scopes, err := runCommand("adb", "shell", "su", "-c", "/data/adb/lspd/bin/cli", "scope", "ls", moduleName)
	if err != nil {
		return fmt.Errorf("failed to list scopes: %w", err)
	}

	if !strings.Contains(scopes, targetPackage) {
		if _, err := runCommand("adb", "shell", "su", "-c", "/data/adb/lspd/bin/cli", "scope", "set", "-a", moduleName, targetPackage+"/0"); err != nil {
			return fmt.Errorf("failed to set scopes: %w", err)
		}
	}

	if _, err := runCommand("adb", "shell", "su", "-c", "/data/adb/lspd/bin/cli", "modules", "set", "-e", moduleName); err != nil {
		return fmt.Errorf("failed to enable module: %w", err)
	}

	return nil
}

func (m *Magisk) WaitForApp(packageName string) error {
	for {
		output, err := runCommand("adb", "shell", "pm", "list", "packages")
		if err == nil && strings.Contains(output, packageName) {
			return nil
		}
		time.Sleep(5 * time.Second)
	}
}

func (m *Magisk) IsAppInstalled(packageName string) bool {
	output, err := runCommand("adb", "shell", "pm", "list", "packages")
	return err == nil && strings.Contains(output, packageName)
}
