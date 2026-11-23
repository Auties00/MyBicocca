"""Exports U14's floor plans from the map-conversion-pipeline DXFs into app/src/main/assets/plans/.

Usage: python scripts/export_floorplans.py <map-conversion-pipeline>/U14

Same frame as the printed plans and layouts.csv: metres, origin at floor 1's WNW outer corner, x along the
building, y across it (offsets from the visit-1 kit's u14-frame.json). One JSON per floor:
  {"floor": "1", "bounds": [x0, y0, x1, y1],
   "layers": {"walls": [[x, y, x, y, ...], ...], "openings": [...], "stairs": [...]},
   "rooms": [[x, y, "1040", "lab"], ...]}
Room labels keep the room number and use only; the drawing's staff names are left out.
"""
import json
import sys
from pathlib import Path

import ezdxf
from ezdxf import path as dxfpath

SRC = Path(sys.argv[1])
OUT = Path(__file__).resolve().parent.parent / "app/src/main/assets/plans"
OFFSETS = {"1i": (-3980.486, -480.805), "T": (-3980.386, -318.829), "1": (-3980.386, -154.34),
           "2": (-3980.386, 6.813), "3": (-3980.386, 168.546)}
LAYERS = {"02-Muri": "walls", "03-Serramenti": "openings", "04-Scale": "stairs"}
SKIP = {"TEXT", "MTEXT", "ATTRIB", "ATTDEF", "HATCH", "DIMENSION", "POINT"}
USES = {"SCALA": "stairs", "ASCENSORE": "lift", "CORRIDOIO": "corridor", "WC": "WC", "WC DISABILI": "WC",
        "AULA": "classroom", "ATRIO": "atrium", "PORTINERIA": "porter", "LABORATORIO": "lab", "UFFICIO": "office",
        "STUDIO": "office", "DEPOSITO": "storage", "SALA RIUNIONI": "meeting"}


def polylines(entity):
    if entity.dxftype() == "INSERT":
        for v in entity.virtual_entities():
            yield from polylines(v)
        return
    if entity.dxftype() in SKIP:
        return
    try:
        p = dxfpath.make_path(entity)
    except Exception:
        return
    pts = [(v.x, v.y) for v in p.flattening(3.0)]
    if len(pts) >= 2:
        yield pts


def to_frame(pts, off):
    flat = []
    for x, y in pts:
        flat += [round(x / 100 + off[0], 3), round(y / 100 + off[1], 3)]
    return flat


def room_labels(floor_bounds):
    doc = ezdxf.readfile(SRC / "U14.dxf")
    rows = []
    for ins in doc.modelspace().query('INSERT[name=="Etichetta_Vani"]'):
        d = {a.dxf.tag: a.dxf.text for a in ins.attribs}
        num = (d.get("VANO:NUMEROLOCALE") or "").strip()
        use = (d.get("VANO:DestinazioneUso") or "").strip().upper()
        if num:
            rows.append((ins.dxf.insert.x / 100, ins.dxf.insert.y / 100, num, USES.get(use, "")))
    return rows


OUT.mkdir(parents=True, exist_ok=True)
labels = room_labels(None)
for floor, off in OFFSETS.items():
    doc = ezdxf.readfile(SRC / "U14_floors" / f"floor_{floor}" / f"{floor}.dxf")
    layers = {name: [] for name in LAYERS.values()}
    for layer, name in LAYERS.items():
        for e in doc.modelspace().query(f'*[layer=="{layer}"]'):
            layers[name] += [to_frame(p, off) for p in polylines(e)]
    xs = [v for poly in layers["walls"] for v in poly[0::2]]
    ys = [v for poly in layers["walls"] for v in poly[1::2]]
    bounds = [min(xs), min(ys), max(xs), max(ys)]
    rooms = []
    for x, y, num, use in labels:
        fx, fy = x + off[0], y + off[1]
        if bounds[0] - 1 <= fx <= bounds[2] + 1 and bounds[1] - 1 <= fy <= bounds[3] + 1:
            rooms.append([round(fx, 2), round(fy, 2), num, use])
    data = {"floor": floor, "bounds": [round(b, 2) for b in bounds], "layers": layers, "rooms": rooms}
    path = OUT / f"U14_{floor}.json"
    path.write_text(json.dumps(data, separators=(",", ":")))
    print(f"{path.name}: {sum(len(v) for v in layers.values())} polylines, {len(rooms)} rooms, "
          f"bounds {data['bounds']}, {path.stat().st_size // 1024} KB")
