package android

import (
	"encoding/xml"
	"fmt"
	"io"
	"os"
	"os/exec"
	"path/filepath"
	"strconv"
	"strings"
	"time"
)

const (
	EmulatorTimeout = 120 // seconds
)

type Emulator struct {
	AndroidHome string
	TempDir     string
}

func NewEmulator(androidHome, tempDir string) *Emulator {
	return &Emulator{
		AndroidHome: androidHome,
		TempDir:     tempDir,
	}
}

func (e *Emulator) List() ([]string, error) {
	cmd := exec.Command("emulator", "-list-avds")
	output, err := cmd.Output()
	if err != nil {
		return nil, fmt.Errorf("failed to list AVDs: %w", err)
	}

	var avds []string
	for _, avd := range strings.Split(strings.TrimSpace(string(output)), "\n") {
		if trimmed := strings.TrimSpace(avd); trimmed != "" {
			avds = append(avds, trimmed)
		}
	}
	return avds, nil
}

func (e *Emulator) Create(name, systemImage, deviceType, avdPath string) error {
	_, err := runCommand("avdmanager", "create", "avd",
		"-n", name,
		"-k", systemImage,
		"--device", deviceType,
		"--force",
		"-p", avdPath)
	if err != nil {
		return fmt.Errorf("AVD creation failed: %w", err)
	}
	return nil
}

func (e *Emulator) Start(name string, binaryName string, killFunc func(), configureDetached func(*exec.Cmd)) error {
	emulatorBin := filepath.Join(e.AndroidHome, "emulator", binaryName)

	if _, err := os.Stat(emulatorBin); os.IsNotExist(err) {
		return fmt.Errorf("emulator binary not found: %s", emulatorBin)
	}

	killFunc()

	if _, err := runCommand("adb", "kill-server"); err != nil {
		return fmt.Errorf("ADB kill-server failed: %w", err)
	}
	if _, err := runCommand("adb", "start-server"); err != nil {
		return fmt.Errorf("ADB start-server failed: %w", err)
	}

	cmd, err := launchDetached("emulator", []string{"-avd", name, "-writable-system", "-no-snapshot-load"}, configureDetached)
	if err != nil {
		return fmt.Errorf("emulator start failed: %w", err)
	}

	_ = cmd // Keep reference

	return e.WaitForStatus(true)
}

func (e *Emulator) WaitForStatus(online bool) error {
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
			// Non-fatal, just log
		}

		bootStart := time.Now()
		bootTimeout := 300 * time.Second
		for time.Since(bootStart) < bootTimeout {
			output, err := runCommand("adb", "shell", "getprop", "sys.boot_completed")
			if err == nil && strings.TrimSpace(output) == "1" {
				return nil
			}
			time.Sleep(2 * time.Second)
		}

		return fmt.Errorf("boot timeout after %d seconds", int(bootTimeout.Seconds()))
	}

	return nil
}

func (e *Emulator) Reboot() error {
	if _, err := runCommand("adb", "reboot"); err != nil {
		// Non-fatal
	}

	return e.WaitForStatus(true)
}

func (e *Emulator) HasRootAccess() bool {
	output, _ := runCommand("adb", "shell", "su", "-c", "id")
	return strings.Contains(strings.ToLower(output), "uid=0")
}

func (e *Emulator) SetupRoot(rootAVDRepo, rootAVDScript, ramdiskPath string) error {
	rootAVDDir := filepath.Join(e.TempDir, "rootAVD")
	if _, err := runCommand("git", "clone", rootAVDRepo, rootAVDDir); err != nil {
		return fmt.Errorf("rootAVD clone failed: %w", err)
	}

	cmd := exec.Command(rootAVDScript, ramdiskPath)
	cmd.Dir = rootAVDDir
	if err := cmd.Start(); err != nil {
		return fmt.Errorf("rootAVD start failed: %w", err)
	}

	if err := cmd.Wait(); err != nil {
		return fmt.Errorf("rootAVD failed: %w", err)
	}

	if err := e.WaitForStatus(false); err != nil {
		return fmt.Errorf("rootAVD wait failed: %w", err)
	}

	return e.Start("", "", nil, nil) // Restart with existing config
}

func (e *Emulator) GetUIDump() (string, error) {
	runCommand("adb", "shell", "rm", "/sdcard/window_dump.xml")
	runCommand("adb", "shell", "uiautomator", "dump", "/sdcard/window_dump.xml")
	return runCommand("adb", "shell", "cat", "/sdcard/window_dump.xml")
}

func (e *Emulator) IsTextOnScreen(text string) bool {
	output, err := e.GetUIDump()
	return err == nil && strings.Contains(strings.ToLower(output), strings.ToLower(text))
}

func (e *Emulator) ClickButtonByText(text string) error {
	xmlContent, err := e.GetUIDump()
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

					_, err := runCommand("adb", "shell", "input", "tap", fmt.Sprintf("%d", centerX), fmt.Sprintf("%d", centerY))
					return err
				}
			}
		}
	}

	return fmt.Errorf("button '%s' not found", text)
}

func launchDetached(name string, args []string, configureDetached func(*exec.Cmd)) (*exec.Cmd, error) {
	// This will be called from main with platform-specific launcher
	cmd := exec.Command(name, args...)
	if configureDetached != nil {
		configureDetached(cmd)
	}
	if err := cmd.Start(); err != nil {
		return nil, err
	}
	return cmd, nil
}
