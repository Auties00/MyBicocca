package main

import (
	"errors"
	"fmt"
	"os"

	"setup_emulator/internal/setup"
	"setup_emulator/internal/ui"
)

func main() {
	runner := setup.NewRunner()

	if err := runner.Run(); err != nil {
		handleError(err)
		ui.PauseBeforeExit()
		os.Exit(1)
	}

	ui.PauseBeforeExit()
}

func handleError(err error) {
	ui.PrintSubWarning("Error details:")

	// Unwrap nested errors
	for err != nil {
		ui.PrintSubWarning(fmt.Sprintf("  - %v", err))
		err = errors.Unwrap(err)
	}
}
