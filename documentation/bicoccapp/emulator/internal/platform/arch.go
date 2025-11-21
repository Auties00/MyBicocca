package platform

import (
	"fmt"
	"path/filepath"
	"runtime"
)

// ArchConfig holds architecture-specific configuration
type ArchConfig struct {
	Name            string
	SystemImage     string
	FridaServerArch string
	RamdiskPath     string
}

func detectArch() string {
	arch := runtime.GOARCH
	switch arch {
	case "amd64":
		return "x86_64"
	case "arm64":
		return "arm64"
	default:
		return "x86_64"
	}
}

// GetArchConfig returns the architecture-specific configuration
func GetArchConfig() *ArchConfig {
	archName := detectArch()

	if archName == "arm64" {
		return &ArchConfig{
			Name:            "arm64",
			SystemImage:     "system-images;android-33;google_apis_playstore;arm64-v8a",
			FridaServerArch: "android-arm64",
			RamdiskPath:     filepath.Join("system-images", "android-33", "google_apis_playstore", "arm64-v8a", "ramdisk.img"),
		}
	}

	return &ArchConfig{
		Name:            "x86_64",
		SystemImage:     "system-images;android-33;google_apis_playstore;x86_64",
		FridaServerArch: "android-x86_64",
		RamdiskPath:     filepath.Join("system-images", "android-33", "google_apis_playstore", "x86_64", "ramdisk.img"),
	}
}

// WarnIfUnknownArch prints a warning if the architecture is unknown
func WarnIfUnknownArch(printWarning func(string)) {
	arch := runtime.GOARCH
	if arch != "amd64" && arch != "arm64" {
		printWarning(fmt.Sprintf("Unknown architecture '%s', defaulting to x86_64", arch))
	}
}
