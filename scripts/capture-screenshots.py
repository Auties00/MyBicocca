#!/usr/bin/env python3
"""Retake the app's screenshots from the demo build on an emulator.

Builds and installs the demo variant, pins the status bar, then walks the catalogue in
android/app/src/demo/assets/screenshots.json and saves one PNG per shot, per language, per
palette and per light/dark mode.

    python scripts/capture-screenshots.py
    python scripts/capture-screenshots.py --modes dark --only Screenshot_Fees
    python scripts/capture-screenshots.py --changed origin/main
    python scripts/capture-screenshots.py --check-missing
    python scripts/capture-screenshots.py --palettes Default Oceano Bosco
    python scripts/capture-screenshots.py --languages en

Output goes to screenshots/demo/<language>/<light|dark>/, with other palettes in
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

REPO = Path(__file__).resolve().parent.parent
ANDROID = REPO / "android"
CATALOG = ANDROID / "app/src/demo/assets/screenshots.json"
APK_DIR = ANDROID / "app/build/outputs/apk/demo"
RES = ANDROID / "app/src/main/res"
SCREEN_DIR = "android/app/src/main/java/it/attendance100/mybicocca/ui/screen/"
PACKAGE = "it.attendance100.mybicocca"
RECEIVER = f"{PACKAGE}/.demo.capture.DemoCaptureReceiver"
STATUS_BAR_TIME = "1030"
# Titles of the windows Android shows for an unresponsive or crashed app.
ERROR_DIALOG = "Application Not Responding|Application Error"
NODE = re.compile(r'text="([^"]*)"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"')
STRING = re.compile(r'<string name="([^"]+)"[^>]*>(.*?)</string>', re.S)

# Subscreen folders whose name differs from the route class they render.
SUBSCREEN_ROUTES = {
    "booking": {"SheetRoute.Appelli"},
    "coursepicker": {"SheetRoute.CourseEditionPicker"},
    "eventdetail": {"SheetRoute.CalendarEvent"},
    "isee": {"SheetRoute.Isee", "SheetRoute.IseeDetail"},
    "language": {"SheetRoute.SettingsLanguage"},
    "refunds": {"SheetRoute.Refunds", "SheetRoute.RefundDetail"},
    "studyplanedit": {"SheetRoute.StudyPlan"},
    "titles": {"SheetRoute.Titles", "SheetRoute.TitleDetail"},
    "yeardetail": {"SheetRoute.EnrollmentDetail"},
}


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

    def tap_label(self, label, attempts=3, gap=1.5):
        """Taps the node showing [label]; retries because a slow emulator can show it late."""
        for attempt in range(attempts):
            if attempt:
                time.sleep(gap)
            self.shell("uiautomator dump /sdcard/window.xml")
            xml = self.shell("cat /sdcard/window.xml")
            # The app may restyle a label (the calendar switcher upper-cases its own), so compare without case.
            for text, *bounds in NODE.findall(xml):
                if html.unescape(text).casefold() == label.casefold():
                    left, top, right, bottom = map(int, bounds)
                    self.shell(f"input tap {(left + right) // 2} {(top + bottom) // 2}")
                    return True
        return False

    def blocked(self):
        """Whether a system 'isn't responding' or 'has stopped' dialog is covering the app."""
        return bool(self.shell(f"dumpsys window windows | grep -E '{ERROR_DIALOG}'", check=False))

    def clear_dialog(self):
        """Dismisses a system error dialog if one is up; False if it is still there afterwards.

        A freshly booted CI emulator is slow enough that the launcher can fail to answer in time, and
        the dialog then sits on top of every shot and takes the focus uiautomator reads taps from.
        """
        for _ in range(3):
            if not self.blocked():
                return True
            if not self.tap_label("Wait", attempts=1):
                self.shell("am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS", check=False)
            time.sleep(1)
        return not self.blocked()

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


def shot_folder(out, language, palette, mode):
    return out / language / (mode if palette == "Default" else f"{palette.lower()}-{mode}")


def missing_shots(shots, out, languages, palettes, modes):
    """Shot names missing at least one PNG on disk for the requested languages, palettes and modes."""
    return [
        shot["name"]
        for shot in shots
        if any(
            not (shot_folder(out, language, palette, mode) / f"{shot['name']}.png").is_file()
            for language in languages
            for palette in palettes
            for mode in modes
        )
    ]


def shots_for_screen_path(shots, rel_path):
    """Shot names whose screen lives under a changed path in ui/screen/."""
    parts = rel_path.split("/")
    area = parts[0].casefold()
    if len(parts) >= 3 and parts[1] == "subscreen":
        sub = parts[2].casefold()
        if sub == "buildingslist":
            return [shot["name"] for shot in shots if shot.get("buildingsList")]
        if sub == "buildingdetail":
            return [shot["name"] for shot in shots if shot.get("building")]
        target_routes = {r.casefold() for r in SUBSCREEN_ROUTES.get(sub, set())} | {
            f"approute.{sub}",
            f"sheetroute.{sub}",
        }
        return [
            shot["name"]
            for shot in shots
            if any(route["type"].casefold() in target_routes for route in shot.get("routes", []))
        ]
    if area in {"profile", "settings"}:
        target = f"approute.{area}"
        return [
            shot["name"]
            for shot in shots
            if [r["type"].casefold() for r in shot.get("routes", [])] == [target]
        ]
    return [
        shot["name"]
        for shot in shots
        if shot["tab"].casefold() == area and not shot.get("routes")
    ]


def changed_shots(shots, base_ref, out, languages, palettes, modes):
    """Shots that are missing on disk, altered in screenshots.json, or backed by a changed screen."""
    needed = set(missing_shots(shots, out, languages, palettes, modes))

    catalog_rel = CATALOG.relative_to(REPO).as_posix()
    base_catalog = subprocess.run(
        ["git", "show", f"{base_ref}:{catalog_rel}"],
        cwd=REPO,
        capture_output=True,
        text=True,
    )
    if base_catalog.returncode == 0:
        before = {shot["name"]: shot for shot in json.loads(base_catalog.stdout).get("shots", [])}
        for shot in shots:
            if before.get(shot["name"]) != shot:
                needed.add(shot["name"])

    diff = subprocess.run(
        ["git", "diff", "--name-only", f"{base_ref}...HEAD"],
        cwd=REPO,
        capture_output=True,
        text=True,
    )
    if diff.returncode == 0:
        for line in diff.stdout.splitlines():
            path = line.strip().replace("\\", "/")
            if path.startswith(SCREEN_DIR):
                needed.update(shots_for_screen_path(shots, path[len(SCREEN_DIR):]))

    return [shot["name"] for shot in shots if shot["name"] in needed]


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
    if not device.clear_dialog():
        problems.append(f"{where}: a system dialog covers the screen; shot skipped")
        print(f"  {shot['name']} (system dialog)")
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
    frame = steady_frame(device)
    # Checked after the frame is taken, so a dialog that appeared meanwhile can't slip into the file.
    if device.blocked():
        problems.append(f"{where}: a system dialog covered the screen; shot skipped")
        print(f"  {shot['name']} (system dialog)")
        return
    path = folder / f"{shot['name']}.png"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(frame)
    print(f"  {shot['name']}")


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--serial", help="emulator to use when more than one is running")
    parser.add_argument("--out", type=Path, default=REPO / "screenshots" / "demo", help="output folder")
    parser.add_argument("--languages", nargs="+", default=app_languages(), help="app languages to capture (default: all)")
    parser.add_argument("--modes", nargs="+", choices=["light", "dark"], default=["light", "dark"])
    parser.add_argument("--palettes", nargs="+", default=["Default"], help="app colour palettes to capture")
    parser.add_argument("--only", nargs="+", help="capture just these shot names")
    parser.add_argument("--changed", metavar="BASE", help="capture only shots missing on disk or changed since BASE")
    parser.add_argument("--list-changed", metavar="BASE", help="list shots missing on disk or changed since BASE and exit")
    parser.add_argument("--check-missing", action="store_true", help="list shots missing any PNG on disk and exit 1 if non-empty")
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

    if args.check_missing:
        missing = missing_shots(shots, args.out, args.languages, args.palettes, args.modes)
        for name in missing:
            print(name)
        sys.exit(1 if missing else 0)

    if args.list_changed:
        for name in changed_shots(shots, args.list_changed, args.out, args.languages, args.palettes, args.modes):
            print(name)
        return

    if args.changed:
        names = changed_shots(shots, args.changed, args.out, args.languages, args.palettes, args.modes)
        if not names:
            print("No new, changed or missing screenshots.")
            return
        print(f"Capturing {len(names)} changed/missing shot(s): {', '.join(names)}")
        shots = [shot for shot in shots if shot["name"] in set(names)]

    device = Device(pick_emulator(args.serial))
    if not args.skip_install:
        if not args.skip_build:
            build()
        install(device)

    was_night = "yes" in device.shell("cmd uimode night")
    # Keeps "isn't responding" and crash dialogs from being shown at all; the old value is put back after.
    hidden_dialogs = device.shell("settings get global hide_error_dialogs")
    device.shell("settings put global hide_error_dialogs 1")
    pin_status_bar(device)
    problems = []
    try:
        for language in args.languages:
            strings = strings_for(language)
            for palette in args.palettes:
                for mode in args.modes:
                    folder = shot_folder(args.out, language, palette, mode)
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
        if hidden_dialogs in ("", "null"):
            device.shell("settings delete global hide_error_dialogs")
        else:
            device.shell(f"settings put global hide_error_dialogs {hidden_dialogs}")

    if problems:
        print("\nFinished with problems:")
        for problem in problems:
            print(f"  {problem}")
        sys.exit(1)
    print("\nDone.")


if __name__ == "__main__":
    main()
