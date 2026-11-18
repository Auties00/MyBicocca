#!/usr/bin/env python3
"""Retake the app's screenshots from the demo build on an emulator.

Builds and installs the demo variant, pins the status bar, then walks the catalogue in
android/app/src/demo/assets/screenshots.json and saves one PNG per shot, per language, per
palette and per light/dark mode.

    python android/tools/screenshots/capture-screenshots.py
    python android/tools/screenshots/capture-screenshots.py --modes dark --only Screenshot_Fees
    python android/tools/screenshots/capture-screenshots.py --palettes Default Oceano Bosco
    python android/tools/screenshots/capture-screenshots.py --languages en

Output goes to android/tools/screenshots/<language>/<light|dark>/, next to this script, with other palettes in
<language>/<palette>-<mode>/. Languages default to every one the app declares.

The demo build shares the real app's application id, so it is only ever installed on an
emulator: installing it on a phone would replace the real app and its data.
"""

import argparse
import html
import json
import re
import subprocess
import sys
import time
from pathlib import Path

ANDROID = Path(__file__).resolve().parents[2]
CATALOG = ANDROID / "app/src/demo/assets/screenshots.json"
APK_DIR = ANDROID / "app/build/outputs/apk/demo"
RES = ANDROID / "app/src/main/res"
PACKAGE = "it.attendance100.mybicocca"
RECEIVER = f"{PACKAGE}/.demo.capture.DemoCaptureReceiver"
STATUS_BAR_TIME = "1030"
NODE = re.compile(r'text="([^"]*)"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"')
STRING = re.compile(r'<string name="([^"]+)"[^>]*>(.*?)</string>', re.S)


class Device:
    def __init__(self, serial):
        self.serial = serial

    def adb(self, *args, check=True, binary=False):
        result = subprocess.run(["adb", "-s", self.serial, *args], capture_output=True)
        if check and result.returncode != 0:
            sys.exit(f"adb {' '.join(args)} failed:\n{result.stderr.decode(errors='replace')}")
        return result.stdout if binary else result.stdout.decode(errors="replace").strip()

    def shell(self, command, check=True):
        return self.adb("shell", command, check=check)

    def status_bar(self, command):
        self.shell(f"am broadcast -a com.android.systemui.demo -e command {command}")

    def send(self, key, value):
        self.shell(f"am broadcast -n {RECEIVER} --es {key} {value}")

    def tap_label(self, label):
        self.shell("uiautomator dump /sdcard/window.xml")
        xml = self.shell("cat /sdcard/window.xml")
        # The app may restyle a label (the calendar switcher upper-cases its own), so compare without case.
        for text, *bounds in NODE.findall(xml):
            if html.unescape(text).casefold() == label.casefold():
                left, top, right, bottom = map(int, bounds)
                self.shell(f"input tap {(left + right) // 2} {(top + bottom) // 2}")
                return True
        return False

    def crashed(self):
        return "CrashActivity" in self.shell("dumpsys activity activities | grep topResumedActivity", check=False)

    def stack(self):
        """The routes on the app's back stack, bottom first; empty until its shell is up."""
        answer = self.shell(f"am broadcast -n {RECEIVER} --ez stack true", check=False)
        match = re.search(r'data="([^"]*)"', answer)
        return [route for route in match.group(1).split(",") if route] if match else []

    def frame(self):
        return self.adb("exec-out", "screencap", "-p", binary=True)


def pick_emulator(requested):
    lines = subprocess.run(["adb", "devices"], capture_output=True, text=True).stdout.splitlines()[1:]
    online = [line.split()[0] for line in lines if line.strip().endswith("device")]
    emulators = [serial for serial in online if serial.startswith("emulator-")]
    if requested:
        if requested not in emulators:
            sys.exit(f"{requested} is not a running emulator. Running emulators: {emulators or 'none'}")
        return requested
    if len(emulators) != 1:
        sys.exit(f"Expected exactly one running emulator, found {emulators or 'none'}. Pass --serial to choose.")
    return emulators[0]


def build():
    wrapper = str(ANDROID / "gradlew.bat") if sys.platform == "win32" else "./gradlew"
    print("Building the demo APK...")
    if subprocess.run([wrapper, ":app:assembleDemo", "-q"], cwd=ANDROID).returncode != 0:
        sys.exit("The demo build failed.")


def install(device):
    abi = device.shell("getprop ro.product.cpu.abi")
    candidates = sorted(APK_DIR.glob(f"*-{abi}-*.apk")) or sorted(APK_DIR.glob("*-universal-*.apk"))
    if not candidates:
        sys.exit(f"No demo APK for {abi} in {APK_DIR}. Run without --skip-build.")
    print(f"Installing {candidates[-1].name}...")
    device.adb("install", "-r", str(candidates[-1]))
    device.shell(f"pm grant {PACKAGE} android.permission.POST_NOTIFICATIONS", check=False)


def app_languages():
    """The languages the app declares, default first, so a new translation is picked up on its own."""
    return re.findall(r'android:name="([^"]+)"', (RES / "xml/locales_config.xml").read_text(encoding="utf-8"))


def strings_for(language):
    """String resources as the app resolves them for a language: its own values over the default ones."""
    strings = {}
    for folder in (RES / "values", RES / f"values-{language}"):
        for file in sorted(folder.glob("strings*.xml")):
            for name, value in STRING.findall(file.read_text(encoding="utf-8")):
                strings[name] = html.unescape(value).replace("\\'", "'").replace('\\"', '"').strip()
    return strings


def pin_status_bar(device):
    device.shell("settings put global sysui_demo_allowed 1")
    for command in (
        "enter",
        f"clock -e hhmm {STATUS_BAR_TIME}",
        "notifications -e visible false",
        "battery -e level 100 -e plugged false",
        "network -e wifi show -e level 4 -e fully true",
        "network -e mobile hide",
        "status -e location hide -e bluetooth hide -e volume hide -e alarm hide",
    ):
        device.status_bar(command)


def launch(device, wait):
    """Starts the app fresh and waits until its shell is on screen, however long the splash takes."""
    device.shell(f"am force-stop {PACKAGE}")
    device.shell(f"monkey -p {PACKAGE} -c android.intent.category.LAUNCHER 1")
    deadline = time.time() + wait
    while time.time() < deadline and not device.stack():
        time.sleep(1)
    if not device.stack():
        sys.exit(f"The app did not reach its main screen within {wait:.0f} seconds.")
    time.sleep(2)


def expected_stack(shot):
    return ["AppRoute.TabRoot"] + [route["type"] for route in shot.get("routes", [])]


def steady_frame(device, attempts=8, gap=0.7):
    """A frame that matches the one before it: transitions and loading have finished."""
    previous = device.frame()
    for _ in range(attempts):
        time.sleep(gap)
        current = device.frame()
        if current == previous:
            return current
        previous = current
    return previous


def capture(device, shot, folder, where, strings, args, problems):
    for attempt in range(2):
        device.send("shot", shot["name"])
        time.sleep(args.settle)
        if device.stack() == expected_stack(shot):
            break
    else:
        problems.append(f"{where}: landed on {device.stack()}, expected {expected_stack(shot)}; shot skipped")
        print(f"  {shot['name']} (wrong screen)")
        return
    for key in shot.get("tap", []):
        if key not in strings:
            problems.append(f"{where}: no string resource named {key}")
        elif not device.tap_label(strings[key]):
            problems.append(f'{where}: no "{strings[key]}" on screen to tap')
        time.sleep(args.settle)
    if device.crashed():
        problems.append(f"{where}: the app crashed, shot skipped")
        print(f"  {shot['name']} (crashed)")
        launch(device, args.launch_wait)
        return
    path = folder / f"{shot['name']}.png"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(steady_frame(device))
    print(f"  {shot['name']}")


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--serial", help="emulator to use when more than one is running")
    parser.add_argument("--out", type=Path, default=Path(__file__).resolve().parent, help="output folder")
    parser.add_argument("--languages", nargs="+", default=app_languages(), help="app languages to capture (default: all)")
    parser.add_argument("--modes", nargs="+", choices=["light", "dark"], default=["light", "dark"])
    parser.add_argument("--palettes", nargs="+", default=["Default"], help="app colour palettes to capture")
    parser.add_argument("--only", nargs="+", help="capture just these shot names")
    parser.add_argument("--skip-build", action="store_true", help="install the APK already built")
    parser.add_argument("--skip-install", action="store_true", help="use the build already on the emulator")
    parser.add_argument("--launch-wait", type=float, default=60, help="longest to wait for the app to start, in seconds")
    parser.add_argument("--settle", type=float, default=2, help="seconds to wait after each navigation or tap")
    args = parser.parse_args()

    shots = json.loads(CATALOG.read_text(encoding="utf-8"))["shots"]
    if args.only:
        unknown = set(args.only) - {shot["name"] for shot in shots}
        if unknown:
            sys.exit(f"Not in the catalogue: {sorted(unknown)}")
        shots = [shot for shot in shots if shot["name"] in args.only]

    device = Device(pick_emulator(args.serial))
    if not args.skip_install:
        if not args.skip_build:
            build()
        install(device)

    was_night = "yes" in device.shell("cmd uimode night")
    pin_status_bar(device)
    problems = []
    try:
        for language in args.languages:
            strings = strings_for(language)
            for palette in args.palettes:
                for mode in args.modes:
                    folder = args.out / language / (mode if palette == "Default" else f"{palette.lower()}-{mode}")
                    print(f"\n{language} / {palette} / {mode} -> {folder}")
                    device.shell(f"cmd uimode night {'yes' if mode == 'dark' else 'no'}")
                    device.send("language", language)
                    device.send("palette", palette)
                    time.sleep(1)
                    launch(device, args.launch_wait)
                    for shot in shots:
                        where = f"{language}/{folder.name}/{shot['name']}"
                        capture(device, shot, folder, where, strings, args, problems)
    finally:
        device.status_bar("exit")
        device.shell(f"cmd uimode night {'yes' if was_night else 'no'}")

    if problems:
        print("\nFinished with problems:")
        for problem in problems:
            print(f"  {problem}")
        sys.exit(1)
    print("\nDone.")


if __name__ == "__main__":
    main()
