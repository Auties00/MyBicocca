.PHONY: all build clean deps test install build-all help

APP_NAME := bicoccapp-emulator
OUTPUT_DIR := bin

# Default target
all: build

# Install dependencies
deps:
	@echo "Installing dependencies..."
	@go mod download
	@go mod tidy
	@echo "Dependencies installed"

# Build for current platform
build: deps
	@echo "Building for current platform..."
	@mkdir -p $(OUTPUT_DIR)
ifeq ($(OS),Windows_NT)
	@go build -ldflags="-s -w" -trimpath -o $(OUTPUT_DIR)/$(APP_NAME).exe ./cmd/emulator
	@echo "Build complete: $(OUTPUT_DIR)/$(APP_NAME).exe"
else
	@go build -ldflags="-s -w" -trimpath -o $(OUTPUT_DIR)/$(APP_NAME) ./cmd/emulator
	@echo "Build complete: $(OUTPUT_DIR)/$(APP_NAME)"
endif

# Cross-compile for all platforms
build-all: build-linux-amd64 build-linux-arm64 build-darwin-amd64 build-darwin-arm64 build-windows-amd64 build-windows-arm64
	@echo "All builds complete"

# Run tests
test:
	@echo "Running tests..."
	@go test -v ./...

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	@rm -rf $(OUTPUT_DIR)
	@go clean
	@echo "Clean complete"

# Install to system
install: build
	@echo "Installing $(APP_NAME)..."
	@go install
	@echo "Installation complete"

# Build specific platform
build-linux-amd64: deps
	@mkdir -p $(OUTPUT_DIR)
	@GOOS=linux GOARCH=amd64 CGO_ENABLED=0 go build -ldflags="-s -w" -trimpath -o $(OUTPUT_DIR)/$(APP_NAME)-linux-amd64 ./cmd/emulator

build-linux-arm64: deps
	@mkdir -p $(OUTPUT_DIR)
	@GOOS=linux GOARCH=arm64 CGO_ENABLED=0 go build -ldflags="-s -w" -trimpath -o $(OUTPUT_DIR)/$(APP_NAME)-linux-arm64 ./cmd/emulator

build-darwin-amd64: deps
	@mkdir -p $(OUTPUT_DIR)
	@GOOS=darwin GOARCH=amd64 CGO_ENABLED=0 go build -ldflags="-s -w" -trimpath -o $(OUTPUT_DIR)/$(APP_NAME)-darwin-amd64 ./cmd/emulator

build-darwin-arm64: deps
	@mkdir -p $(OUTPUT_DIR)
	@GOOS=darwin GOARCH=arm64 CGO_ENABLED=0 go build -ldflags="-s -w" -trimpath -o $(OUTPUT_DIR)/$(APP_NAME)-darwin-arm64 ./cmd/emulator

build-windows-amd64: deps
	@mkdir -p $(OUTPUT_DIR)
	@GOOS=windows GOARCH=amd64 CGO_ENABLED=0 go build -ldflags="-s -w" -trimpath -o $(OUTPUT_DIR)/$(APP_NAME)-windows-amd64.exe ./cmd/emulator

build-windows-arm64: deps
	@mkdir -p $(OUTPUT_DIR)
	@GOOS=windows GOARCH=arm64 CGO_ENABLED=0 go build -ldflags="-s -w" -trimpath -o $(OUTPUT_DIR)/$(APP_NAME)-windows-arm64.exe ./cmd/emulator

# Help target
help:
	@echo "Available targets:"
	@echo "  make build              - Build for current platform"
	@echo "  make build-all          - Cross-compile for all platforms"
	@echo "  make build-linux-amd64  - Build for Linux x86_64"
	@echo "  make build-linux-arm64  - Build for Linux ARM64"
	@echo "  make build-darwin-amd64 - Build for macOS x86_64"
	@echo "  make build-darwin-arm64 - Build for macOS ARM64 (Apple Silicon)"
	@echo "  make build-windows-amd64 - Build for Windows x86_64"
	@echo "  make build-windows-arm64 - Build for Windows ARM64"
	@echo "  make deps               - Install dependencies"
	@echo "  make test               - Run tests"
	@echo "  make clean              - Clean build artifacts"
	@echo "  make install            - Install to system"
	@echo "  make help               - Show this help message"
