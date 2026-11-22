/**
 * beacon-3d.js — Parametric 3D BLE Beacon Renderer (HTML5 Canvas) + Lottie JSON Exporter.
 * Models the sleek, elongated white BLE beacon (~27 x 84 x 13 mm):
 *   - Slim stadium main housing (~57 mm body) + smooth perimeter chamfer + flat top plateau
 *   - Elongated north & south mounting flanges ("ears") with flush recessed through-holes
 *   - Smooth concave V-notched shoulder bevels flanking both mounting holes
 *   - Edge-to-edge center label band ("tape" | "printed" | "tonal") with minimal side padding
 *   - Rotation order Ry(yaw) * Rx(pitch) * Rz(roll):
 *       0° front card view = (0, 0, 0) vertical upright
 *       3/4 table-top hero view = (pitch: -48°, yaw: 14°, roll: -62°) laying horizontally on a table
 *
 * Exports to window.Beacon3D.
 */
(() => {
  const DEG = Math.PI / 180;

  function clamp(v, lo, hi) {
    return v < lo ? lo : v > hi ? hi : v;
  }

  function lerp(a, b, t) {
    return a + (b - a) * t;
  }

  function hexToRgb(hex) {
    const clean = (hex || '#ffffff').replace('#', '');
    const n = parseInt(clean.length === 3
      ? clean.split('').map((c) => c + c).join('')
      : clean.slice(0, 6), 16);
    return { r: (n >> 16) & 255, g: (n >> 8) & 255, b: n & 255 };
  }

  function rgbStr(c, alpha = 1) {
    if (alpha >= 0.999) return `rgb(${c.r},${c.g},${c.b})`;
    return `rgba(${c.r},${c.g},${c.b},${alpha.toFixed(3)})`;
  }

  function sub3(a, b) { return [a[0] - b[0], a[1] - b[1], a[2] - b[2]]; }
  function cross3(a, b) {
    return [
      a[1] * b[2] - a[2] * b[1],
      a[2] * b[0] - a[0] * b[2],
      a[0] * b[1] - a[1] * b[0],
    ];
  }
  function dot3(a, b) { return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]; }
  function norm3(v) {
    const l = Math.hypot(v[0], v[1], v[2]) || 1;
    return [v[0] / l, v[1] / l, v[2] / l];
  }

  /**
   * Rotation order: Ry(yaw) * Rx(pitch) * Rz(roll).
   * Applying roll first lets a vertical upright beacon rotate horizontally on the XY plane,
   * then pitch tilts it back to lay flat on a horizontal table surface viewed from above.
   */
  function makeRotationMatrix(pitchDeg, yawDeg, rollDeg) {
    const px = pitchDeg * DEG;
    const py = yawDeg * DEG;
    const pz = rollDeg * DEG;
    const cx = Math.cos(px), sx = Math.sin(px);
    const cy = Math.cos(py), sy = Math.sin(py);
    const cz = Math.cos(pz), sz = Math.sin(pz);
    return [
      cy * cz + sy * sx * sz, -cy * sz + sy * sx * cz, sy * cx,
      cx * sz,                cx * cz,                 -sx,
      -sy * cz + cy * sx * sz, sy * sz + cy * sx * cz, cy * cx,
    ];
  }

  function transformPoint(p, m) {
    return [
      m[0] * p[0] + m[1] * p[1] + m[2] * p[2],
      m[3] * p[0] + m[4] * p[1] + m[5] * p[2],
      m[6] * p[0] + m[7] * p[1] + m[8] * p[2],
    ];
  }

  // Smooth cubic Hermite / Bezier sampler helper
  function sampleCubicBezier(p0, p1, p2, p3, steps) {
    const out = [];
    for (let i = 1; i <= steps; i++) {
      const t = i / steps;
      const u = 1 - t;
      const x = u * u * u * p0[0] + 3 * u * u * t * p1[0] + 3 * u * t * t * p2[0] + t * t * t * p3[0];
      const y = u * u * u * p0[1] + 3 * u * u * t * p1[1] + 3 * u * t * t * p2[1] + t * t * t * p3[1];
      out.push([x, y]);
    }
    return out;
  }

  /**
   * Generates smooth CCW outer footprint in mm:
   * Sleek elongated body: 27mm wide (x in [-13.5, +13.5]), 84mm tall (y in [-42, +42]).
   * Screw holes sit at (0, +-34.5).
   */
  function sampleOuterContour() {
    const pts = [];
    const earCy = 34.4;
    const earR = 7.6; // Top tip reaches 34.4 + 7.6 = 42.0mm

    // Top semi-circular ear tab (from right side a=0.14*PI around top to left side a=0.86*PI)
    const earSegs = 22;
    for (let i = 0; i <= earSegs; i++) {
      const a = 0.10 * Math.PI + (i / earSegs) * (0.80 * Math.PI);
      pts.push([Math.cos(a) * earR, earCy + Math.sin(a) * earR]);
    }

    // Left shoulder smooth S-curve down to left sidewall (x=-13.5, y=22)
    const topLeftStart = pts[pts.length - 1];
    pts.push(...sampleCubicBezier(
      topLeftStart,
      [-8.8, 31.2],
      [-13.4, 29.0],
      [-13.5, 22.0],
      10
    ));

    // Left straight sidewall
    pts.push([-13.5, 16.0]);
    pts.push([-13.5, 0.0]);
    pts.push([-13.5, -16.0]);
    pts.push([-13.5, -22.0]);

    // Bottom-left shoulder curve to bottom ear start
    const botLeftEnd = [Math.cos(1.10 * Math.PI) * earR, -earCy + Math.sin(1.10 * Math.PI) * earR];
    pts.push(...sampleCubicBezier(
      [-13.5, -22.0],
      [-13.4, -29.0],
      [-8.8, -31.2],
      botLeftEnd,
      10
    ));

    // Bottom semi-circular ear tab
    for (let i = 1; i <= earSegs; i++) {
      const a = 1.10 * Math.PI + (i / earSegs) * (0.80 * Math.PI);
      pts.push([Math.cos(a) * earR, -earCy + Math.sin(a) * earR]);
    }

    // Bottom-right shoulder curve up to right sidewall
    const botRightStart = pts[pts.length - 1];
    pts.push(...sampleCubicBezier(
      botRightStart,
      [8.8, -31.2],
      [13.4, -29.0],
      [13.5, -22.0],
      10
    ));

    // Right straight sidewall
    pts.push([13.5, -16.0]);
    pts.push([13.5, 0.0]);
    pts.push([13.5, 16.0]);
    pts.push([13.5, 22.0]);

    // Top-right shoulder curve back to top ear start
    pts.push(...sampleCubicBezier(
      [13.5, 22.0],
      [13.4, 29.0],
      [8.8, 31.2],
      pts[0],
      10
    ).slice(0, -1));

    return pts;
  }

  /**
   * Generates smooth CCW raised body contour (mid-wall or top plateau),
   * featuring the signature concave U-notch hugging the mounting holes at y = +-34.4.
   */
  function sampleBodyTopContour(insetMm = 0) {
    const halfW = 13.15 - insetMm;
    const cornerY = 23.8 - insetMm * 0.65;
    const peakY = 29.2 - insetMm * 0.95;
    const notchCenterY = 25.4 - insetMm * 0.9;
    const shoulderX = 8.4 - insetMm * 0.65;
    const pts = [];

    // Top concave U-notch (right-to-left CCW: x = +shoulderX -> 0 -> -shoulderX)
    const notchSegs = 16;
    for (let i = 0; i <= notchSegs; i++) {
      const t = 1 - (i / notchSegs) * 2; // +1 to -1
      const x = t * shoulderX;
      const absT = Math.abs(t);
      const y = notchCenterY + Math.pow(absT, 1.55) * (peakY - notchCenterY);
      pts.push([x, y]);
    }

    // Top-left rounded cheek curve down to left straight wall
    pts.push(...sampleCubicBezier(
      [-shoulderX, peakY],
      [-halfW + 0.6, peakY - 0.6],
      [-halfW, cornerY + 2.8],
      [-halfW, cornerY],
      8
    ));

    // Left straight sidewall
    pts.push([-halfW, 16.0]);
    pts.push([-halfW, 0.0]);
    pts.push([-halfW, -16.0]);
    pts.push([-halfW, -cornerY]);

    // Bottom-left rounded cheek curve
    pts.push(...sampleCubicBezier(
      [-halfW, -cornerY],
      [-halfW, -cornerY - 2.8],
      [-halfW + 0.6, -peakY + 0.6],
      [-shoulderX, -peakY],
      8
    ));

    // Bottom concave U-notch (left-to-right CCW: x = -shoulderX -> 0 -> +shoulderX)
    for (let i = 1; i <= notchSegs; i++) {
      const t = -1 + (i / notchSegs) * 2; // -1 to +1
      const x = t * shoulderX;
      const absT = Math.abs(t);
      const y = -(notchCenterY + Math.pow(absT, 1.55) * (peakY - notchCenterY));
      pts.push([x, y]);
    }

    // Bottom-right rounded cheek curve
    pts.push(...sampleCubicBezier(
      [shoulderX, -peakY],
      [halfW - 0.6, -peakY + 0.6],
      [halfW, -cornerY - 2.8],
      [halfW, -cornerY],
      8
    ));

    // Right straight sidewall
    pts.push([halfW, -16.0]);
    pts.push([halfW, 0.0]);
    pts.push([halfW, 16.0]);
    pts.push([halfW, cornerY]);

    // Top-right rounded cheek curve back to start
    pts.push(...sampleCubicBezier(
      [halfW, cornerY],
      [halfW, cornerY + 2.8],
      [halfW - 0.6, peakY - 0.6],
      [shoulderX, peakY],
      8
    ).slice(0, -1));

    return pts;
  }

  const OUTER_BASE = sampleOuterContour();
  const BODY_MID = sampleBodyTopContour(0);
  const BODY_PLATEAU = sampleBodyTopContour(1.65);

  // Label canvas cache (offscreen 360x440 transparent decal with minimal side padding)
  const labelCache = new Map();

  function getLabelDecalCanvas(labelText, labelStyle, themePalette) {
    const key = `${labelText}|${labelStyle}|${themePalette.primary}|${themePalette.primaryContainer}|${themePalette.onPrimaryContainer}`;
    if (labelCache.has(key)) return labelCache.get(key);
    if (labelCache.size > 100) labelCache.clear();

    const W = 360;
    const H = 440;
    const c = document.createElement('canvas');
    c.width = W;
    c.height = H;
    const g = c.getContext('2d');

    const text = (labelText || 'B2').trim() || 'B2';

    // Measure text and dynamically size so horizontal side padding is minimal (~6% each side)
    function drawFittedLabel(fontFamily, weight, color, maxWidthFrac = 0.90, maxH = 190, yOffset = 0, shadowColor = null) {
      let size = 180;
      g.font = `${weight} ${size}px ${fontFamily}`;
      const measured = Math.max(1, g.measureText(text).width);
      const targetW = W * maxWidthFrac;
      const fittedSize = Math.min(maxH, Math.floor(size * (targetW / measured)));
      g.font = `${weight} ${fittedSize}px ${fontFamily}`;
      g.textAlign = 'center';
      g.textBaseline = 'middle';

      const cy = H * 0.5 + yOffset;
      if (shadowColor) {
        g.fillStyle = shadowColor;
        g.fillText(text, W * 0.5 + 1.5, cy + 1.5);
      }
      g.fillStyle = color;
      g.fillText(text, W * 0.5, cy);
      return fittedSize;
    }

    if (labelStyle === 'tape') {
      // Subtle full-width crepe texture grain lines (edge-to-edge)
      g.strokeStyle = 'rgba(130, 118, 92, 0.13)';
      g.lineWidth = 1.6;
      for (let y = 8; y < H - 4; y += 8) {
        g.beginPath();
        g.moveTo(0, y);
        for (let x = 24; x <= W; x += 24) {
          g.lineTo(x, y + Math.sin(x * 0.07 + y) * 1.1);
        }
        g.stroke();
      }
      g.save();
      g.translate(W * 0.5, H * 0.5);
      g.rotate(-0.015);
      g.translate(-W * 0.5, -H * 0.5);
      drawFittedLabel(
        '"Comic Sans MS", "Segoe Print", "Bradley Hand", "Chalkboard SE", cursive, sans-serif',
        '700',
        '#24211F',
        0.90,
        185,
        2,
        'rgba(32, 29, 27, 0.16)'
      );
      g.restore();
    } else if (labelStyle === 'printed') {
      // Minimal alignment corner ticks hugging the edge
      g.strokeStyle = 'rgba(28, 27, 30, 0.24)';
      g.lineWidth = 2.5;
      g.beginPath();
      g.moveTo(14, 34); g.lineTo(14, 16); g.lineTo(32, 16);
      g.moveTo(W - 14, 34); g.lineTo(W - 14, 16); g.lineTo(W - 32, 16);
      g.moveTo(14, H - 34); g.lineTo(14, H - 16); g.lineTo(32, H - 16);
      g.moveTo(W - 14, H - 34); g.lineTo(W - 14, H - 16); g.lineTo(W - 32, H - 16);
      g.stroke();

      drawFittedLabel(
        '"Plus Jakarta Sans", "Inter", system-ui, -apple-system, sans-serif',
        '800',
        '#1B191C',
        0.90,
        175,
        -12
      );

      g.font = '700 24px "JetBrains Mono", ui-monospace, monospace';
      g.textAlign = 'center';
      g.textBaseline = 'middle';
      g.fillStyle = 'rgba(27, 25, 28, 0.45)';
      g.fillText('BLE · U14', W * 0.5, H - 38);
    } else {
      const ink = themePalette.onPrimaryContainer || '#400014';
      const accent = themePalette.primary || '#D81648';
      g.fillStyle = accent;
      g.fillRect(0, 14, W, 12);
      g.fillRect(0, H - 26, W, 12);

      drawFittedLabel(
        '"Plus Jakarta Sans", system-ui, -apple-system, sans-serif',
        '800',
        ink,
        0.90,
        180,
        0
      );
    }

    labelCache.set(key, c);
    return c;
  }

  /**
   * Builds all 3D polygons (faces) for the beacon in local space (mm, centered at 0,0,0).
   */
  function buildBeaconFaces(labelStyle, themePalette) {
    const faces = [];
    const whiteMat = { base: '#F5F6F8', rough: 0.42, spec: 0.24 };
    const bevelMat = { base: '#ECEEF2', rough: 0.34, spec: 0.34 };
    const earMat = { base: '#EEF0F4', rough: 0.48, spec: 0.18 };
    const holeWallMat = { base: '#B4B9C2', rough: 0.72, spec: 0.05 };

    // 1. Lower ear deck & base shell (z = -6.4 to -1.6, layer 0)
    const zBaseBot = -6.4;
    const zBaseTop = -1.6;
    for (let i = 0; i < OUTER_BASE.length; i++) {
      const p0 = OUTER_BASE[i];
      const p1 = OUTER_BASE[(i + 1) % OUTER_BASE.length];
      faces.push({
        pts: [
          [p0[0], p0[1], zBaseBot],
          [p1[0], p1[1], zBaseBot],
          [p1[0], p1[1], zBaseTop],
          [p0[0], p0[1], zBaseTop],
        ],
        mat: earMat,
        layer: 0,
      });
    }

    // Complete ear deck polygon so no crescent gaps appear at steep 3/4 angles
    faces.push({
      pts: OUTER_BASE.map((p) => [p[0], p[1], zBaseTop]),
      mat: earMat,
      localNormal: [0, 0, 1],
      layer: 0,
    });

    // Flush chamfered hole lip + inner bore wall around (0, +-34.4)
    const holeR = 3.1;
    const collarR = 4.3;
    const holeSegs = 20;

    function addHoleRing(sign) {
      const cy = sign * 34.4;
      for (let i = 0; i < holeSegs; i++) {
        const th0 = (i / holeSegs) * Math.PI * 2;
        const th1 = ((i + 1) / holeSegs) * Math.PI * 2;
        const x0c = Math.cos(th0) * collarR, y0c = cy + Math.sin(th0) * collarR;
        const x1c = Math.cos(th1) * collarR, y1c = cy + Math.sin(th1) * collarR;
        const x0h = Math.cos(th0) * holeR, y0h = cy + Math.sin(th0) * holeR;
        const x1h = Math.cos(th1) * holeR, y1h = cy + Math.sin(th1) * holeR;

        faces.push({
          pts: [
            [x0h, y0h, zBaseTop - 0.35],
            [x0c, y0c, zBaseTop],
            [x1c, y1c, zBaseTop],
            [x1h, y1h, zBaseTop - 0.35],
          ],
          mat: bevelMat,
          localNormal: [0, 0, 1],
          layer: 0,
        });
        faces.push({
          pts: [
            [x0h, y0h, zBaseTop - 0.35],
            [x1h, y1h, zBaseTop - 0.35],
            [x1h, y1h, zBaseBot],
            [x0h, y0h, zBaseBot],
          ],
          mat: holeWallMat,
          doubleSided: true,
          isHoleWall: true,
          layer: 0,
        });
      }
    }
    addHoleRing(1);
    addHoleRing(-1);

    // Bottom shell cap
    faces.push({
      pts: [...OUTER_BASE].reverse().map((p) => [p[0], p[1], zBaseBot]),
      mat: { base: '#D8DCE3', rough: 0.7, spec: 0.05 },
      localNormal: [0, 0, -1],
      layer: 0,
    });

    // 2. Main raised body walls (zBaseTop -> zMid -> zPlateau, layer 1)
    const zMid = 4.3;
    const zPlateau = 6.4;
    for (let i = 0; i < BODY_MID.length; i++) {
      const m0 = BODY_MID[i];
      const m1 = BODY_MID[(i + 1) % BODY_MID.length];
      const t0 = BODY_PLATEAU[i];
      const t1 = BODY_PLATEAU[(i + 1) % BODY_PLATEAU.length];

      faces.push({
        pts: [
          [m0[0], m0[1], zBaseTop],
          [m1[0], m1[1], zBaseTop],
          [m1[0], m1[1], zMid],
          [m0[0], m0[1], zMid],
        ],
        mat: whiteMat,
        layer: 1,
      });
      faces.push({
        pts: [
          [m0[0], m0[1], zMid],
          [m1[0], m1[1], zMid],
          [t1[0], t1[1], zPlateau],
          [t0[0], t0[1], zPlateau],
        ],
        mat: bevelMat,
        layer: 1,
      });
    }

    // 3. Top Plateau (layer 1)
    faces.push({
      pts: BODY_PLATEAU.map((p) => [p[0], p[1], zPlateau]),
      mat: whiteMat,
      localNormal: [0, 0, 1],
      layer: 1,
    });

    // 4. Center Wrap Band (Masking Tape or Bicocca Tonal Band, layer 2)
    if (labelStyle === 'tape' || labelStyle === 'tonal') {
      const bandColor = labelStyle === 'tape'
        ? '#F2EDE0'
        : (themePalette.primaryContainer || '#FFD9DF');
      const bandMat = {
        base: bandColor,
        rough: labelStyle === 'tape' ? 0.78 : 0.35,
        spec: labelStyle === 'tape' ? 0.08 : 0.25,
      };
      const bandY = 16.0;
      const off = 0.22;
      const xPlat = 11.50 + off;
      const xMid = 13.15 + off;

      // Single undivided plateau band surface (layer 2 prevents plateau Z-fighting)
      faces.push({
        pts: [
          [-xPlat, bandY, zPlateau + off],
          [-xPlat, -bandY, zPlateau + off],
          [xPlat, -bandY, zPlateau + off],
          [xPlat, bandY, zPlateau + off],
        ],
        mat: bandMat,
        localNormal: [0, 0, 1],
        layer: 2,
      });
      // Right chamfer & sidewall wrap flush with hull_main_body (z >= -1.2)
      faces.push({
        pts: [
          [xPlat, bandY, zPlateau + off],
          [xPlat, -bandY, zPlateau + off],
          [xMid, -bandY, zMid + off],
          [xMid, bandY, zMid + off],
        ],
        mat: bandMat,
        layer: 2,
      });
      faces.push({
        pts: [
          [xMid, bandY, zMid + off],
          [xMid, -bandY, zMid + off],
          [xMid, -bandY, zBaseTop + 0.35],
          [xMid, bandY, zBaseTop + 0.35],
        ],
        mat: bandMat,
        layer: 2,
      });
      // Left chamfer & sidewall wrap flush with hull_main_body
      faces.push({
        pts: [
          [-xMid, bandY, zMid + off],
          [-xMid, -bandY, zMid + off],
          [-xPlat, -bandY, zPlateau + off],
          [-xPlat, bandY, zPlateau + off],
        ],
        mat: bandMat,
        layer: 2,
      });
      faces.push({
        pts: [
          [-xMid, bandY, zBaseTop + 0.35],
          [-xMid, -bandY, zBaseTop + 0.35],
          [-xMid, -bandY, zMid + off],
          [-xMid, bandY, zMid + off],
        ],
        mat: bandMat,
        layer: 2,
      });
    }

    return faces;
  }

  function projectPoint(p3, rotM, camDist, fovScale, cx, cy) {
    const tp = transformPoint(p3, rotM);
    const zCam = camDist - tp[2];
    const k = fovScale / Math.max(zCam, 20);
    return {
      x: cx + tp[0] * k,
      y: cy - tp[1] * k,
      z: tp[2],
      k,
      tp,
    };
  }

  function drawMappedTriangle(ctx, img, s0, s1, s2, d0, d1, d2) {
    const cx = (d0.x + d1.x + d2.x) / 3;
    const cy = (d0.y + d1.y + d2.y) / 3;
    function pad(p) {
      const vx = p.x - cx, vy = p.y - cy;
      const l = Math.hypot(vx, vy) || 1;
      return { x: p.x + (vx / l) * 0.45, y: p.y + (vy / l) * 0.45 };
    }
    const p0 = pad(d0), p1 = pad(d1), p2 = pad(d2);

    ctx.save();
    ctx.beginPath();
    ctx.moveTo(p0.x, p0.y);
    ctx.lineTo(p1.x, p1.y);
    ctx.lineTo(p2.x, p2.y);
    ctx.closePath();
    ctx.clip();

    const denom = s0.u * (s1.v - s2.v) + s1.u * (s2.v - s0.v) + s2.u * (s0.v - s1.v);
    if (Math.abs(denom) < 1e-6) {
      ctx.restore();
      return;
    }

    const a = (d0.x * (s1.v - s2.v) + d1.x * (s2.v - s0.v) + d2.x * (s0.v - s1.v)) / denom;
    const b = (d0.y * (s1.v - s2.v) + d1.y * (s2.v - s0.v) + d2.y * (s0.v - s1.v)) / denom;
    const c = (d0.x * (s2.u - s1.u) + d1.x * (s0.u - s2.u) + d2.x * (s1.u - s0.u)) / denom;
    const d = (d0.y * (s2.u - s1.u) + d1.y * (s0.u - s2.u) + d2.y * (s1.u - s0.u)) / denom;
    const e = (d0.x * (s1.u * s2.v - s2.u * s1.v) + d1.x * (s2.u * s0.v - s0.u * s2.v) + d2.x * (s0.u * s1.v - s1.u * s0.v)) / denom;
    const f = (d0.y * (s1.u * s2.v - s2.u * s1.v) + d1.y * (s2.u * s0.v - s0.u * s2.v) + d2.y * (s0.u * s1.v - s1.u * s0.v)) / denom;

    ctx.transform(a, b, c, d, e, f);
    ctx.drawImage(img, 0, 0);
    ctx.restore();
  }

  function renderBeaconCanvas(canvas, opts = {}) {
    if (!canvas) return;
    const dpr = Math.min(window.devicePixelRatio || 1, 2.5);
    const rectW = opts.width || canvas.clientWidth || parseFloat(canvas.style.width) || 120;
    const rectH = opts.height || canvas.clientHeight || parseFloat(canvas.style.height) || 160;
    const targetW = Math.max(1, Math.round(rectW * dpr));
    const targetH = Math.max(1, Math.round(rectH * dpr));

    if (canvas.width !== targetW || canvas.height !== targetH) {
      canvas.width = targetW;
      canvas.height = targetH;
    }

    const ctx = canvas.getContext('2d');
    ctx.save();
    ctx.scale(dpr, dpr);
    ctx.clearRect(0, 0, rectW, rectH);

    const pitch = opts.pitch ?? 0;
    const yaw = opts.yaw ?? 0;
    const roll = opts.roll ?? 0;
    const label = opts.label || 'B2';
    const labelStyle = opts.labelStyle || 'tape';
    const dark = Boolean(opts.dark);
    const themePalette = opts.themePalette || {
      primary: '#D81648',
      primaryContainer: '#FFD9DF',
      onPrimaryContainer: '#400014',
      surface: dark ? '#0B0808' : '#FFFBFB',
    };

    const rotM = makeRotationMatrix(pitch, yaw, roll);
    const camDist = 250;

    // Auto-compute exact scale from projected bounding box so tall OR horizontal table-top poses fit cleanly
    const bbox = [
      [-14.0, -42.5, -6.8], [14.0, -42.5, -6.8],
      [-14.0,  42.5, -6.8], [14.0,  42.5, -6.8],
      [-14.0, -42.5,  6.8], [14.0, -42.5,  6.8],
      [-14.0,  42.5,  6.8], [14.0,  42.5,  6.8],
    ];
    let minX = Infinity, maxX = -Infinity, minY = Infinity, maxY = -Infinity;
    for (const pt of bbox) {
      const tp = transformPoint(pt, rotM);
      const k = camDist / Math.max(camDist - tp[2], 30);
      const px = tp[0] * k;
      const py = -tp[1] * k;
      if (px < minX) minX = px;
      if (px > maxX) maxX = px;
      if (py < minY) minY = py;
      if (py > maxY) maxY = py;
    }
    const spanX = Math.max(10, maxX - minX);
    const spanY = Math.max(10, maxY - minY);
    const pxPerMm = Math.min((rectW * 0.90) / spanX, (rectH * 0.88) / spanY);
    const fovScale = pxPerMm * camDist;

    // Center the projected bounding box inside the canvas
    const midProjX = ((minX + maxX) * 0.5) * pxPerMm;
    const midProjY = ((minY + maxY) * 0.5) * pxPerMm;
    const cx = rectW * 0.5 - midProjX;
    const cy = rectH * 0.5 - midProjY;

    // Contact shadow: vertical footing when upright, full table contact shadow when laying flat
    if (opts.shadow !== false) {
      const layProgress = clamp((Math.abs(pitch) + Math.abs(roll)) / 95, 0, 1);
      ctx.save();
      const topP = projectPoint([0, 32, -6.4], rotM, camDist, fovScale, cx, cy);
      const botP = projectPoint([0, -32, -6.4], rotM, camDist, fovScale, cx, cy);
      const midS = projectPoint([0, 0, -6.4], rotM, camDist, fovScale, cx, cy);
      const shAlpha = dark ? 0.50 : 0.22;

      if (layProgress > 0.25) {
        // Table-top contact shadow stretched along the beacon's horizontal axis
        const ang = Math.atan2(botP.y - topP.y, botP.x - topP.x);
        const len = Math.max(pxPerMm * 18, Math.hypot(botP.x - topP.x, botP.y - topP.y) * 0.62);
        const thick = pxPerMm * 14.5;
        ctx.translate(midS.x + pxPerMm * 1.5, midS.y + pxPerMm * 4.2);
        ctx.rotate(ang);
        ctx.scale(1, thick / len);
        const grad = ctx.createRadialGradient(0, 0, len * 0.1, 0, 0, len);
        grad.addColorStop(0, `rgba(0,0,0,${shAlpha * layProgress})`);
        grad.addColorStop(0.65, `rgba(0,0,0,${shAlpha * 0.45 * layProgress})`);
        grad.addColorStop(1, 'rgba(0,0,0,0)');
        ctx.fillStyle = grad;
        ctx.beginPath();
        ctx.arc(0, 0, len, 0, Math.PI * 2);
        ctx.fill();
      } else {
        const shY = rectH * 0.5 + (spanY * 0.5) * pxPerMm + 2;
        const shRx = pxPerMm * 12;
        const grad = ctx.createRadialGradient(rectW * 0.5, shY, 1, rectW * 0.5, shY, shRx);
        grad.addColorStop(0, `rgba(0,0,0,${shAlpha * 0.7})`);
        grad.addColorStop(1, 'rgba(0,0,0,0)');
        ctx.fillStyle = grad;
        ctx.beginPath();
        ctx.ellipse(rectW * 0.5, shY, shRx, pxPerMm * 3.5, 0, 0, Math.PI * 2);
        ctx.fill();
      }
      ctx.restore();
    }

    const faces = buildBeaconFaces(labelStyle, themePalette);

    const keyLight = norm3([0.45, 0.62, 0.65]);
    const fillLight = norm3([-0.6, -0.22, 0.48]);
    const rimLight = norm3([-0.3, 0.4, -0.85]);

    const projectedFaces = [];
    for (const face of faces) {
      const worldPts = face.pts.map((p) => transformPoint(p, rotM));
      let normal;
      if (face.localNormal) {
        normal = norm3(transformPoint(face.localNormal, rotM));
      } else {
        const e1 = sub3(worldPts[1], worldPts[0]);
        const e2 = sub3(worldPts[2], worldPts[0]);
        normal = norm3(cross3(e1, e2));
      }

      const centroid = worldPts.reduce((acc, p) => [acc[0] + p[0], acc[1] + p[1], acc[2] + p[2]], [0, 0, 0])
        .map((v) => v / worldPts.length);
      const toCam = norm3([0 - centroid[0], 0 - centroid[1], camDist - centroid[2]]);
      const facing = dot3(normal, toCam);

      if (!face.doubleSided && facing <= -0.02) continue;
      if (face.doubleSided && facing < 0) {
        normal = [-normal[0], -normal[1], -normal[2]];
      }

      const baseRgb = hexToRgb(face.mat.base);
      const nDotK = Math.max(0, dot3(normal, keyLight));
      const nDotF = Math.max(0, dot3(normal, fillLight));
      const nDotR = Math.max(0, dot3(normal, rimLight));
      const hemi = 0.5 + 0.5 * normal[1];

      const halfVec = norm3([keyLight[0], keyLight[1], keyLight[2] + 1]);
      const spec = Math.pow(Math.max(0, dot3(normal, halfVec)), 28) * (face.mat.spec || 0.2);

      let lit = 0.54 + hemi * 0.14 + nDotK * 0.34 + nDotF * 0.12 + nDotR * 0.10;
      if (face.isHoleWall) lit *= 0.68;
      if (dark) lit *= 0.94;

      const shaded = {
        r: clamp(Math.round(baseRgb.r * lit + spec * 255), 0, 255),
        g: clamp(Math.round(baseRgb.g * lit + spec * 255), 0, 255),
        b: clamp(Math.round(baseRgb.b * lit + spec * 255), 0, 255),
      };

      const screenPts = face.pts.map((p) => projectPoint(p, rotM, camDist, fovScale, cx, cy));
      projectedFaces.push({ screenPts, shaded, avgZ: centroid[2], layer: face.layer ?? 0 });
    }

    projectedFaces.sort((a, b) => (a.layer - b.layer) || (a.avgZ - b.avgZ));

    for (const pf of projectedFaces) {
      ctx.beginPath();
      ctx.moveTo(pf.screenPts[0].x, pf.screenPts[0].y);
      for (let i = 1; i < pf.screenPts.length; i++) {
        ctx.lineTo(pf.screenPts[i].x, pf.screenPts[i].y);
      }
      ctx.closePath();
      const fill = rgbStr(pf.shaded);
      ctx.fillStyle = fill;
      ctx.fill();
      ctx.strokeStyle = fill;
      ctx.lineWidth = 0.55;
      ctx.stroke();
    }

    // Punch crisp dark/background inner circles for the 2 mounting through-holes
    const holeBg = opts.holeColor || (dark ? '#171113' : '#DCD6D8');
    for (const sign of [1, -1]) {
      const cyMm = sign * 34.4;
      const holePtsFront = [];
      const holePtsBack = [];
      const segs = 20;
      for (let i = 0; i < segs; i++) {
        const th = (i / segs) * Math.PI * 2;
        const hx = Math.cos(th) * 3.05;
        const hy = cyMm + Math.sin(th) * 3.05;
        holePtsFront.push(projectPoint([hx, hy, -1.6], rotM, camDist, fovScale, cx, cy));
        holePtsBack.push(projectPoint([hx, hy, -6.4], rotM, camDist, fovScale, cx, cy));
      }
      ctx.save();
      ctx.beginPath();
      holePtsFront.forEach((p, idx) => (idx === 0 ? ctx.moveTo(p.x, p.y) : ctx.lineTo(p.x, p.y)));
      ctx.closePath();
      ctx.clip();

      ctx.fillStyle = dark ? '#3B3438' : '#9E9AA0';
      ctx.fillRect(0, 0, rectW, rectH);

      ctx.beginPath();
      holePtsBack.forEach((p, idx) => (idx === 0 ? ctx.moveTo(p.x, p.y) : ctx.lineTo(p.x, p.y)));
      ctx.closePath();
      ctx.fillStyle = holeBg;
      ctx.fill();
      ctx.restore();
    }

    // Crisp rim crease around top plateau
    ctx.save();
    ctx.strokeStyle = dark ? 'rgba(0,0,0,0.22)' : 'rgba(90, 95, 110, 0.16)';
    ctx.lineWidth = Math.max(0.65, pxPerMm * 0.22);
    ctx.beginPath();
    BODY_PLATEAU.forEach((p, idx) => {
      const sp = projectPoint([p[0], p[1], 6.4], rotM, camDist, fovScale, cx, cy);
      if (idx === 0) ctx.moveTo(sp.x, sp.y);
      else ctx.lineTo(sp.x, sp.y);
    });
    ctx.closePath();
    ctx.stroke();
    ctx.restore();

    // 5. Project edge-to-edge label decal onto the top plateau (true 3D foreshortened 4x4 mesh)
    const decal = getLabelDecalCanvas(label, labelStyle, themePalette);
    const zDecal = labelStyle === 'printed' ? 6.45 : 6.66;
    const bandHalfW = 11.45;
    const bandHalfH = 15.8;
    const gridN = 4;

    const meshVerts = [];
    for (let gy = 0; gy <= gridN; gy++) {
      const row = [];
      const vNorm = gy / gridN;
      const yMm = lerp(bandHalfH, -bandHalfH, vNorm);
      for (let gx = 0; gx <= gridN; gx++) {
        const uNorm = gx / gridN;
        const xMm = lerp(-bandHalfW, bandHalfW, uNorm);
        const sp = projectPoint([xMm, yMm, zDecal], rotM, camDist, fovScale, cx, cy);
        row.push({
          screen: sp,
          uv: { u: uNorm * decal.width, v: vNorm * decal.height },
        });
      }
      meshVerts.push(row);
    }

    for (let gy = 0; gy < gridN; gy++) {
      for (let gx = 0; gx < gridN; gx++) {
        const v00 = meshVerts[gy][gx];
        const v10 = meshVerts[gy][gx + 1];
        const v01 = meshVerts[gy + 1][gx];
        const v11 = meshVerts[gy + 1][gx + 1];
        drawMappedTriangle(ctx, decal, v00.uv, v10.uv, v11.uv, v00.screen, v10.screen, v11.screen);
        drawMappedTriangle(ctx, decal, v00.uv, v11.uv, v01.uv, v00.screen, v11.screen, v01.screen);
      }
    }

    ctx.restore();
  }

  /**
   * Generates a valid Lottie JSON animation (512x512, 60fps, 60 frames)
   * animating the tall BLE beacon from vertical front-facing (frame 0)
   * to table-top 3/4 perspective view (frame 60).
   */
  function generateBeaconLottieJson(opts = {}) {
    const label = opts.label || 'B2';
    const labelStyle = opts.labelStyle || 'tape';
    const themePalette = opts.themePalette || {
      primary: '#D81648',
      primaryContainer: '#FFD9DF',
      onPrimaryContainer: '#400014',
    };

    const frames = 60;
    const sampleFrames = [0, 12, 24, 36, 48, 60];
    const camDist = 250;
    const fovScale = 4.3 * camDist;

    function projectLottieShape(pts2d, zOffset, pitch, yaw, roll) {
      const m = makeRotationMatrix(pitch, yaw, roll);
      return pts2d.map(([x, y]) => {
        const tp = transformPoint([x, y, zOffset], m);
        const k = fovScale / (camDist - tp[2]);
        return [
          Number((tp[0] * k).toFixed(2)),
          Number((-tp[1] * k).toFixed(2)),
        ];
      });
    }

    function buildKeyframedShape(pts2d, zOffset) {
      const k = [];
      for (let idx = 0; idx < sampleFrames.length; idx++) {
        const f = sampleFrames[idx];
        const p = f / frames;
        const ease = p < 0.5 ? 2 * p * p : 1 - Math.pow(-2 * p + 2, 2) / 2;
        const pitch = lerp(0, -58, ease);
        const yaw = lerp(0, -12, ease);
        const roll = lerp(0, 56, ease);
        const verts = projectLottieShape(pts2d, zOffset, pitch, yaw, roll);
        const kf = {
          t: f,
          s: [{
            i: verts.map(() => [0, 0]),
            o: verts.map(() => [0, 0]),
            v: verts,
            c: true,
          }],
        };
        if (idx < sampleFrames.length - 1) {
          kf.i = { x: [0.42], y: [1] };
          kf.o = { x: [0.58], y: [0] };
        }
        k.push(kf);
      }
      return k;
    }

    function rgb01(hex) {
      const c = hexToRgb(hex);
      return [Number((c.r / 255).toFixed(3)), Number((c.g / 255).toFixed(3)), Number((c.b / 255).toFixed(3)), 1];
    }

    const bandHex = labelStyle === 'tape' ? '#F2EDE0'
      : labelStyle === 'tonal' ? (themePalette.primaryContainer || '#FFD9DF')
      : '#F5F6F8';
    const textHex = labelStyle === 'tonal' ? (themePalette.onPrimaryContainer || '#400014') : '#23201E';

    const bandPts = [[-11.5, 16.0], [11.5, 16.0], [11.5, -16.0], [-11.5, -16.0]];

    const layers = [
      {
        ddd: 1,
        ind: 1,
        ty: 5,
        nm: 'Beacon_Label_Text',
        sr: 1,
        ks: {
          o: { a: 0, k: 100 },
          rx: { a: 1, k: [{ t: 0, s: [0], i: { x: [0.42], y: [1] }, o: { x: [0.58], y: [0] } }, { t: 60, s: [-58] }] },
          ry: { a: 1, k: [{ t: 0, s: [0], i: { x: [0.42], y: [1] }, o: { x: [0.58], y: [0] } }, { t: 60, s: [-12] }] },
          rz: { a: 1, k: [{ t: 0, s: [0], i: { x: [0.42], y: [1] }, o: { x: [0.58], y: [0] } }, { t: 60, s: [56] }] },
          p: { a: 0, k: [256, 256, -28] },
          a: { a: 0, k: [0, -14, 0] },
          s: { a: 0, k: [100, 100, 100] },
        },
        t: {
          d: {
            k: [{
              s: {
                s: 42,
                f: 'sans-serif',
                t: label,
                j: 2,
                tr: 0,
                lh: 48,
                ls: 0,
                fc: rgb01(textHex).slice(0, 3),
              },
              t: 0,
            }],
          },
          p: {},
          m: { g: 1, a: { a: 0, k: [0, 0] } },
          a: [],
        },
        ip: 0,
        op: 60,
        st: 0,
      },
      {
        ddd: 0,
        ind: 2,
        ty: 4,
        nm: 'Center_Label_Band',
        sr: 1,
        ks: { o: { a: 0, k: 100 }, r: { a: 0, k: 0 }, p: { a: 0, k: [256, 256, 0] }, a: { a: 0, k: [0, 0, 0] }, s: { a: 0, k: [100, 100, 100] } },
        shapes: [{
          ty: 'gr',
          nm: 'BandGroup',
          it: [
            { ty: 'sh', nm: 'BandPath', ks: { a: 1, k: buildKeyframedShape(bandPts, 6.6) } },
            { ty: 'fl', nm: 'BandFill', c: { a: 0, k: rgb01(bandHex) }, o: { a: 0, k: 100 } },
            { ty: 'tr', p: { a: 0, k: [0, 0] }, a: { a: 0, k: [0, 0] }, s: { a: 0, k: [100, 100] }, r: { a: 0, k: 0 }, o: { a: 0, k: 100 } },
          ],
        }],
        ip: 0,
        op: 60,
        st: 0,
      },
      {
        ddd: 0,
        ind: 3,
        ty: 4,
        nm: 'Top_Chamfer_Plateau',
        sr: 1,
        ks: { o: { a: 0, k: 100 }, r: { a: 0, k: 0 }, p: { a: 0, k: [256, 256, 0] }, a: { a: 0, k: [0, 0, 0] }, s: { a: 0, k: [100, 100, 100] } },
        shapes: [{
          ty: 'gr',
          nm: 'PlateauGroup',
          it: [
            { ty: 'sh', nm: 'PlateauPath', ks: { a: 1, k: buildKeyframedShape(BODY_PLATEAU, 6.4) } },
            { ty: 'fl', nm: 'PlateauFill', c: { a: 0, k: rgb01('#F8F9FA') }, o: { a: 0, k: 100 } },
            { ty: 'st', nm: 'PlateauStroke', c: { a: 0, k: rgb01('#D5D9E0') }, o: { a: 0, k: 100 }, w: { a: 0, k: 1.5 } },
            { ty: 'tr', p: { a: 0, k: [0, 0] }, a: { a: 0, k: [0, 0] }, s: { a: 0, k: [100, 100] }, r: { a: 0, k: 0 }, o: { a: 0, k: 100 } },
          ],
        }],
        ip: 0,
        op: 60,
        st: 0,
      },
      {
        ddd: 0,
        ind: 4,
        ty: 4,
        nm: 'Main_Body_Wall',
        sr: 1,
        ks: { o: { a: 0, k: 100 }, r: { a: 0, k: 0 }, p: { a: 0, k: [256, 256, 0] }, a: { a: 0, k: [0, 0, 0] }, s: { a: 0, k: [100, 100, 100] } },
        shapes: [{
          ty: 'gr',
          nm: 'BodyGroup',
          it: [
            { ty: 'sh', nm: 'BodyPath', ks: { a: 1, k: buildKeyframedShape(BODY_MID, 1.5) } },
            { ty: 'fl', nm: 'BodyFill', c: { a: 0, k: rgb01('#E7EAF0') }, o: { a: 0, k: 100 } },
            { ty: 'tr', p: { a: 0, k: [0, 0] }, a: { a: 0, k: [0, 0] }, s: { a: 0, k: [100, 100] }, r: { a: 0, k: 0 }, o: { a: 0, k: 100 } },
          ],
        }],
        ip: 0,
        op: 60,
        st: 0,
      },
      {
        ddd: 0,
        ind: 5,
        ty: 4,
        nm: 'Mounting_Ears_Shell',
        sr: 1,
        ks: { o: { a: 0, k: 100 }, r: { a: 0, k: 0 }, p: { a: 0, k: [256, 256, 0] }, a: { a: 0, k: [0, 0, 0] }, s: { a: 0, k: [100, 100, 100] } },
        shapes: [{
          ty: 'gr',
          nm: 'EarsGroup',
          it: [
            { ty: 'sh', nm: 'OuterPath', ks: { a: 1, k: buildKeyframedShape(OUTER_BASE, -2.5) } },
            { ty: 'fl', nm: 'EarsFill', c: { a: 0, k: rgb01('#DCE0E8') }, o: { a: 0, k: 100 } },
            { ty: 'tr', p: { a: 0, k: [0, 0] }, a: { a: 0, k: [0, 0] }, s: { a: 0, k: [100, 100] }, r: { a: 0, k: 0 }, o: { a: 0, k: 100 } },
          ],
        }],
        ip: 0,
        op: 60,
        st: 0,
      },
      {
        ddd: 0,
        ind: 6,
        ty: 4,
        nm: 'Rear_Shell_Depth',
        sr: 1,
        ks: { o: { a: 0, k: 100 }, r: { a: 0, k: 0 }, p: { a: 0, k: [256, 256, 0] }, a: { a: 0, k: [0, 0, 0] }, s: { a: 0, k: [100, 100, 100] } },
        shapes: [{
          ty: 'gr',
          nm: 'BackGroup',
          it: [
            { ty: 'sh', nm: 'BackPath', ks: { a: 1, k: buildKeyframedShape(OUTER_BASE, -6.4) } },
            { ty: 'fl', nm: 'BackFill', c: { a: 0, k: rgb01('#C3C9D4') }, o: { a: 0, k: 100 } },
            { ty: 'tr', p: { a: 0, k: [0, 0] }, a: { a: 0, k: [0, 0] }, s: { a: 0, k: [100, 100] }, r: { a: 0, k: 0 }, o: { a: 0, k: 100 } },
          ],
        }],
        ip: 0,
        op: 60,
        st: 0,
      },
    ];

    return {
      v: '5.7.4',
      fr: 60,
      ip: 0,
      op: frames,
      w: 512,
      h: 512,
      nm: `BLE_Beacon_${label}_Table_Turn`,
      ddd: 1,
      assets: [],
      layers,
    };
  }

  function downloadLottie(opts = {}) {
    const data = generateBeaconLottieJson(opts);
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `ble_beacon_${(opts.label || 'b2').toLowerCase()}_turn.json`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => URL.revokeObjectURL(url), 4000);
  }

  window.Beacon3D = {
    renderBeaconCanvas,
    getLabelDecalCanvas,
    generateBeaconLottieJson,
    downloadLottie,
    OUTER_BASE,
    BODY_MID,
    BODY_PLATEAU,
  };
})();
