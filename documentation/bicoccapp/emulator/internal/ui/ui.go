package ui

import (
	"bufio"
	"fmt"
	"os"
	"time"

	"github.com/charmbracelet/lipgloss"
)

var (
	colorSuccess    = lipgloss.NewStyle().Foreground(lipgloss.Color("#04B575"))
	colorError      = lipgloss.NewStyle().Foreground(lipgloss.Color("#FF0000"))
	colorStep       = lipgloss.NewStyle().Foreground(lipgloss.Color("#00AAFF")).Bold(true)
	colorSub        = lipgloss.NewStyle().Foreground(lipgloss.Color("#888888"))
	colorSubWarning = lipgloss.NewStyle().Foreground(lipgloss.Color("#FFFF00"))
	colorInfo       = lipgloss.NewStyle().Foreground(lipgloss.Color("#FFFFFF"))
	colorTiming     = lipgloss.NewStyle().Foreground(lipgloss.Color("#888888")).Italic(true)
)

var startTime time.Time

func Init() {
	startTime = time.Now()
}

func getElapsedTime() string {
	elapsed := time.Since(startTime)
	return fmt.Sprintf("[%02d:%02d]", int(elapsed.Minutes()), int(elapsed.Seconds())%60)
}

func PrintInfo(message string) {
	fmt.Println(colorStep.Render(fmt.Sprintf("\n%s %s", message, colorTiming.Render(getElapsedTime()))))
}

func PrintError(message string) {
	fmt.Println(colorError.Render(fmt.Sprintf("%s %s", message, colorTiming.Render(getElapsedTime()))))
}

func PrintSubInfo(message string) {
	fmt.Println(colorSub.Render(fmt.Sprintf("  [+] %s", message)))
}

func PrintSubWarning(message string) {
	fmt.Println(colorSubWarning.Render(fmt.Sprintf("  [!] %s", message)))
}

func PrintComplete(avdName, androidAPI, architecture string) {
	PrintInfo("Configuration complete")
	PrintSubInfo("AVD Name: " + avdName)
	PrintSubInfo("Android Version: 13 (API " + androidAPI + ")")
	PrintSubInfo("Device: Pixel 7 Pro")
	PrintSubInfo("Architecture: " + architecture)
}

func PauseBeforeExit() {
	fmt.Println()
	fmt.Println(colorInfo.Render("Press Enter to exit..."))
	reader := bufio.NewReader(os.Stdin)
	reader.ReadString('\n')
}
