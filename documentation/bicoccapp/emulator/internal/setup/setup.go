package setup

import (
	"fmt"
	"os"
	"os/exec"
	"path/filepath"

	"emulator/internal/android"
	"emulator/internal/platform"
	"emulator/internal/ui"
)

const (
	AVDName           = "Pixel_7_Pro_API_33"
	AndroidAPI        = "33"
	DeviceType        = "pixel_7_pro"
	BicoccAppPackage  = "it.bicoccapp.unimib"
	LSPosedURL        = "https://github.com/Auties00/MyBicocca/raw/refs/heads/main/documentation/bicoccapp/emulator/dependencies/LSPosed-v1.10.2-7199-zygisk-debug.zip"
	RootAVDRepo       = "https://gitlab.com/newbit/rootAVD.git"
	LSPosedModuleURL  = "https://github.com/Auties00/MyBicocca/raw/refs/heads/main/documentation/bicoccapp/magisk/bin/bypass.apk"
	LSPosedModuleName = "it.attendance100.bicoccapp"
)

type Runner struct {
	TempDir     string
	AndroidHome string
	JavaHome    string
	PythonCmd   string
	ArchConfig  *platform.ArchConfig
}

func NewRunner() *Runner {
	return &Runner{}
}

func (r *Runner) Run() error {
	ui.Init()

	steps := []struct {
		name string
		fn   func() error
	}{
		{"Initialize", r.stepInitialize},
		{"System Tools", r.stepSystemTools},
		{"Python", r.stepPython},
		{"Java", r.stepJava},
		{"Android SDK", r.stepAndroidSDK},
		{"Emulator", r.stepEmulator},
		{"LSPosed", r.stepLSPosed},
		{"BicoccApp", r.stepBicoccApp},
		{"Bypass Module", r.stepBypass},
	}

	for _, step := range steps {
		if err := step.fn(); err != nil {
			ui.PrintError(fmt.Sprintf("Setup failed at: %s", step.name))
			ui.PrintSubWarning("Error: " + err.Error())
			return err
		}
	}

	platformInfo := platform.GetInfo()
	ui.PrintComplete(AVDName, AndroidAPI, platformInfo.Arch)

	if r.TempDir != "" {
		os.RemoveAll(r.TempDir)
	}

	return nil
}

func (r *Runner) stepInitialize() error {
	ui.PrintInfo("Initializing")

	platformInfo := platform.GetInfo()
	ui.PrintSubInfo(fmt.Sprintf("OS: %s", platformInfo.OS))

	r.ArchConfig = platform.GetArchConfig()
	platform.WarnIfUnknownArch(ui.PrintSubWarning)
	ui.PrintSubInfo(fmt.Sprintf("Architecture: %s", r.ArchConfig.Name))

	var err error
	r.TempDir, err = os.MkdirTemp("", "android-setup-*")
	if err != nil {
		return fmt.Errorf("failed to create temp directory: %w", err)
	}
	ui.PrintSubInfo(fmt.Sprintf("Temp directory: %s", r.TempDir))

	return nil
}

func (r *Runner) stepSystemTools() error {
	ui.PrintInfo("Setting up system tools")

	ui.PrintSubInfo("Checking if package manager is available (this might take a while)")
	if err := platform.EnsurePackageManager(); err != nil {
		return fmt.Errorf("package manager setup failed: %w", err)
	}
	ui.PrintSubInfo("Package manager ready")

	if err := r.ensureGit(); err != nil {
		return fmt.Errorf("git setup failed: %w", err)
	}

	return nil
}

func (r *Runner) ensureGit() error {
	if platform.CommandExists("git") {
		ui.PrintSubInfo("Git detected")
		return nil
	}

	ui.PrintSubInfo("Installing Git")
	if err := platform.InstallPackage("Git", platform.GetGitPackage()); err != nil {
		return err
	}

	if !platform.CommandExists("git") {
		return fmt.Errorf("git installation completed but command not found - restart terminal")
	}

	return nil
}

func (r *Runner) stepPython() error {
	ui.PrintInfo("Setting up Python")

	if _, err := exec.Command("python3", "--version").Output(); err == nil {
		r.PythonCmd = "python3"
		ui.PrintSubInfo("Python3 detected")
		return nil
	}

	if _, err := exec.Command("python", "--version").Output(); err == nil {
		r.PythonCmd = "python"
		ui.PrintSubInfo("Python detected")
		return nil
	}

	ui.PrintSubInfo("Installing Python")
	if err := platform.InstallPackage("Python", platform.GetPythonPackage()); err != nil {
		return err
	}

	if err := platform.ConfigurePythonPath(); err != nil {
		return fmt.Errorf("failed to configure Python PATH: %w", err)
	}

	if _, err := exec.Command("python3", "--version").Output(); err == nil {
		r.PythonCmd = "python3"
		return nil
	}

	if _, err := exec.Command("python", "--version").Output(); err == nil {
		r.PythonCmd = "python"
		return nil
	}

	return fmt.Errorf("python installation completed but not functioning - restart terminal")
}

func (r *Runner) stepJava() error {
	ui.PrintInfo("Setting up Java")

	if platform.CommandExists("java") {
		ui.PrintSubInfo("Java detected")
	} else {
		ui.PrintSubInfo("Installing Java")
		if err := platform.InstallPackage("Java", platform.GetJavaPackage()); err != nil {
			return err
		}
	}

	r.JavaHome = os.Getenv("JAVA_HOME")
	if r.JavaHome == "" {
		r.JavaHome = platform.FindJavaHome()
		if r.JavaHome != "" {
			os.Setenv("JAVA_HOME", r.JavaHome)
			ui.PrintSubInfo(fmt.Sprintf("JAVA_HOME: %s", r.JavaHome))
		}
	} else {
		ui.PrintSubInfo(fmt.Sprintf("JAVA_HOME: %s", r.JavaHome))
	}

	return nil
}

func (r *Runner) stepAndroidSDK() error {
	ui.PrintInfo("Setting up Android SDK")

	r.AndroidHome = os.Getenv("ANDROID_HOME")
	if r.AndroidHome == "" {
		r.AndroidHome = platform.GetDefaultAndroidHome()
		os.Setenv("ANDROID_HOME", r.AndroidHome)
	}
	ui.PrintSubInfo(fmt.Sprintf("ANDROID_HOME: %s", r.AndroidHome))

	sdk := android.NewSDK(r.AndroidHome, r.TempDir)

	if !platform.CommandExists("sdkmanager") {
		ui.PrintSubInfo("Installing SDK tools")
		if path, err := sdk.DownloadAndInstallTools(platform.GetSDKToolsURL()); err != nil {
			return err
		} else {
			platform.AddToPath(path)
		}
	} else {
		ui.PrintSubInfo("SDK manager detected")
	}

	if installed, err := sdk.InstallComponent("platform-tools", "Platform-Tools"); err != nil {
		return err
	} else if installed {
		ui.PrintSubInfo("Platform-Tools installed")
		platform.AddToPath(filepath.Join(r.AndroidHome, "platform-tools"))
	}

	if installed, err := sdk.InstallComponent("emulator", "Emulator"); err != nil {
		return err
	} else if installed {
		ui.PrintSubInfo("Emulator installed")
		platform.AddToPath(filepath.Join(r.AndroidHome, "emulator"))
	}

	if installed, err := sdk.InstallComponent(r.ArchConfig.SystemImage, "System image"); err != nil {
		return err
	} else if installed {
		ui.PrintSubInfo("System image installed")
	}

	platform.RefreshEnv()

	return nil
}

func (r *Runner) stepEmulator() error {
	ui.PrintInfo("Setting up emulator")

	emulator := android.NewEmulator(r.AndroidHome, r.TempDir)

	avds, err := emulator.List()
	if err != nil {
		return err
	}

	avdExists := false
	for _, avd := range avds {
		if avd == AVDName {
			avdExists = true
			break
		}
	}

	if !avdExists {
		ui.PrintSubInfo(fmt.Sprintf("Creating AVD: %s", AVDName))
		avdPath := filepath.Join(os.Getenv("HOME"), ".android", "avd", AVDName+".avd")
		if err := emulator.Create(AVDName, r.ArchConfig.SystemImage, DeviceType, avdPath); err != nil {
			return err
		}
	} else {
		ui.PrintSubInfo("AVD already exists")
	}

	ui.PrintSubInfo("Starting emulator")
	if err := emulator.Start(AVDName, platform.GetEmulatorBinaryName(), platform.KillExistingEmulators, platform.ConfigureDetachedProcess); err != nil {
		return fmt.Errorf("failed to start emulator: %w", err)
	}

	ui.PrintInfo("Configuring root access")
	if emulator.HasRootAccess() {
		ui.PrintSubInfo("Root access confirmed")
	} else {
		ui.PrintSubInfo("Setting up rootAVD")
		rootAVDScript := platform.GetRootAVDScript(filepath.Join(r.TempDir, "rootAVD"))
		if err := emulator.SetupRoot(RootAVDRepo, rootAVDScript, r.ArchConfig.RamdiskPath); err != nil {
			return err
		}
	}

	magisk := android.NewMagisk(r.TempDir)

	ui.PrintSubInfo("Checking Zygisk status")
	if magisk.IsZygiskEnabled() {
		ui.PrintSubInfo("Zygisk is already enabled")
	} else {
		ui.PrintSubInfo("Enabling Zygisk")
		if err := magisk.EnableZygisk(); err != nil {
			ui.PrintSubWarning(fmt.Sprintf("Failed to enable Zygisk: %v", err))
		} else {
			ui.PrintSubInfo("Zygisk enabled successfully")
		}
	}

	ui.PrintSubInfo("Configuring Magisk auto-allow")
	if err := magisk.ConfigureAutoAllow(); err != nil {
		ui.PrintSubWarning(fmt.Sprintf("Failed to set Magisk auto-allow: %v", err))
	}

	return nil
}

func (r *Runner) stepLSPosed() error {
	ui.PrintInfo("Installing LSPosed")

	magisk := android.NewMagisk(r.TempDir)
	emulator := android.NewEmulator(r.AndroidHome, r.TempDir)

	if err := magisk.InstallLSPosed(LSPosedURL, emulator); err != nil {
		return err
	}

	ui.PrintSubInfo("LSPosed installed successfully")
	return nil
}

func (r *Runner) stepBicoccApp() error {
	ui.PrintInfo("Setting up BicoccApp")

	magisk := android.NewMagisk(r.TempDir)

	if magisk.IsAppInstalled(BicoccAppPackage) {
		ui.PrintSubInfo("Bicoccapp detected")
		return nil
	}

	ui.PrintSubInfo("Bicoccapp is not installed: please install it manually from the Google Play Store (you might need to log in)")
	return magisk.WaitForApp(BicoccAppPackage)
}

func (r *Runner) stepBypass() error {
	ui.PrintInfo("Setting up bypass module")

	magisk := android.NewMagisk(r.TempDir)

	if err := magisk.InstallBypassModule(LSPosedModuleURL, LSPosedModuleName, BicoccAppPackage); err != nil {
		return err
	}

	ui.PrintSubInfo("Bypass module configured successfully")
	return nil
}
