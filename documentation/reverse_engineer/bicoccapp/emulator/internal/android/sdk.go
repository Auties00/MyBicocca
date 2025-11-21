package android

import (
	"archive/zip"
	"fmt"
	"io"
	"net/http"
	"os"
	"os/exec"
	"path/filepath"
	"strings"
)

type SDK struct {
	AndroidHome string
	TempDir     string
}

func NewSDK(androidHome, tempDir string) *SDK {
	return &SDK{
		AndroidHome: androidHome,
		TempDir:     tempDir,
	}
}

func (s *SDK) DownloadAndInstallTools(sdkURL string, addToPath func(string) error) error {
	cmdlineToolsDir := filepath.Join(s.AndroidHome, "cmdline-tools")
	os.MkdirAll(cmdlineToolsDir, 0755)

	zipPath := filepath.Join(s.TempDir, "cmdline-tools.zip")
	if err := downloadFile(zipPath, sdkURL); err != nil {
		return err
	}

	extractDir := filepath.Join(s.TempDir, "extracted")
	if err := unzip(zipPath, extractDir); err != nil {
		return err
	}

	latestDir := filepath.Join(cmdlineToolsDir, "latest")
	os.RemoveAll(latestDir)

	srcDir := filepath.Join(extractDir, "cmdline-tools")
	if err := os.Rename(srcDir, latestDir); err != nil {
		return err
	}

	return addToPath(filepath.Join(latestDir, "bin"))
}

func (s *SDK) InstallComponent(component, name string) (bool, error) {
	output, _ := runCommand("sdkmanager", "--list_installed")
	if strings.Contains(output, component) {
		return false, nil
	}

	cmd := exec.Command("sdkmanager", component)
	stdin, err := cmd.StdinPipe()
	if err != nil {
		return false, fmt.Errorf("failed to create stdin pipe: %w", err)
	}

	var stdout, stderr strings.Builder
	cmd.Stdout = &stdout
	cmd.Stderr = &stderr

	if err := cmd.Start(); err != nil {
		return false, fmt.Errorf("sdkmanager start failed: %w", err)
	}

	stdin.Write([]byte("y\n"))
	stdin.Close()

	if err := cmd.Wait(); err != nil {
		return false, fmt.Errorf("sdkmanager failed: %w (output: %s)", err, strings.TrimSpace(stdout.String()))
	}

	return true, nil
}

func downloadFile(filepath, url string) error {
	resp, err := http.Get(url)
	if err != nil {
		return fmt.Errorf("download failed: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("download failed: HTTP %d", resp.StatusCode)
	}

	out, err := os.Create(filepath)
	if err != nil {
		return fmt.Errorf("failed to create file: %w", err)
	}
	defer out.Close()

	_, err = io.Copy(out, resp.Body)
	return err
}

func unzip(src, dest string) error {
	r, err := zip.OpenReader(src)
	if err != nil {
		return fmt.Errorf("failed to open zip: %w", err)
	}
	defer r.Close()

	for _, f := range r.File {
		fpath := filepath.Join(dest, f.Name)

		if f.FileInfo().IsDir() {
			os.MkdirAll(fpath, os.ModePerm)
			continue
		}

		if err = os.MkdirAll(filepath.Dir(fpath), os.ModePerm); err != nil {
			return err
		}

		outFile, err := os.OpenFile(fpath, os.O_WRONLY|os.O_CREATE|os.O_TRUNC, f.Mode())
		if err != nil {
			return err
		}

		rc, err := f.Open()
		if err != nil {
			outFile.Close()
			return err
		}

		_, err = io.Copy(outFile, rc)
		outFile.Close()
		rc.Close()
		if err != nil {
			return err
		}
	}
	return nil
}

func runCommand(name string, args ...string) (string, error) {
	cmd := exec.Command(name, args...)
	var stdout strings.Builder
	cmd.Stdout = &stdout
	cmd.Stderr = &stdout

	err := cmd.Run()
	return stdout.String(), err
}
