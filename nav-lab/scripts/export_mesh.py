"""Converts designs/mybicocca-beacons/viking-slim-mesh.js into app/src/main/res/raw/viking_slim.mesh.

Little-endian layout:
  "VKSM", int32 version (1), int32 vertexCount, int32 indexCount, int32 profileCount,
  profileCount float32 (the profile values below, in order), vertexCount * 6 float32
  (x, y, z, nx, ny, nz; positions in mm), indexCount uint16.
"""
import json
import struct
import sys
from pathlib import Path

PROFILE_KEYS = ["widthMm", "heightMm", "thicknessMm", "plateauZ", "plateauHalfW", "midZ", "midHalfW",
                "baseZ", "baseHalfW", "bandHalfH", "holeY", "holeR"]

root = Path(__file__).resolve().parent.parent
src = root / "designs/mybicocca-beacons/viking-slim-mesh.js"
dst = root / "app/src/main/res/raw/viking_slim.mesh"

text = src.read_text()
data = json.loads(text[text.index("{"):text.rstrip().rstrip(";").rindex("}") + 1])
mesh = data["three"]
pos, nrm, idx = mesh["positions"], mesh["normals"], mesh["indices"]
vcount = len(pos) // 3
if max(idx) > 0xFFFF:
    sys.exit("too many vertices for uint16 indices")

out = bytearray(b"VKSM")
out += struct.pack("<iiii", 1, vcount, len(idx), len(PROFILE_KEYS))
out += struct.pack(f"<{len(PROFILE_KEYS)}f", *(data["profile"][k] for k in PROFILE_KEYS))
for i in range(vcount):
    out += struct.pack("<6f", pos[3 * i] * 1000, pos[3 * i + 1] * 1000, pos[3 * i + 2] * 1000,
                       nrm[3 * i], nrm[3 * i + 1], nrm[3 * i + 2])
out += struct.pack(f"<{len(idx)}H", *idx)
dst.write_bytes(out)
print(f"{dst.name}: {vcount} vertices, {len(idx) // 3} triangles, {len(out)} bytes")
