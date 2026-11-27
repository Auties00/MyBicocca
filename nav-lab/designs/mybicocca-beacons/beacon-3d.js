/**
 * beacon-3d.js — Confidex Viking Slim 3D Canvas Renderer + Lottie JSON Exporter.
 * Uses the cleaned Blender mesh exported to window.VIKING_SLIM_MESH
 * (32.00 × 74.63 × 14.37 mm) plus programmatic wrap band & dynamic label decal:
 *   - Rotation order Ry(yaw) * Rx(pitch) * Rz(roll):
 *       0° front card view = (0, 0, 0) vertical upright
 *       3/4 table-top hero view = (pitch: -58°, yaw: -12°, roll: 56°)
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

  function dot3(a, b) { return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]; }
  function norm3(v) {
    const l = Math.hypot(v[0], v[1], v[2]) || 1;
    return [v[0] / l, v[1] / l, v[2] / l];
  }
  function signedArea2D(pts) {
    let area2 = 0;
    const n = pts.length;
    for (let i = 0; i < n; i++) {
      const a = pts[i];
      const b = pts[(i + 1) % n];
      area2 += a.x * b.y - b.x * a.y;
    }
    return area2;
  }

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

  // Fallback silhouette paths matched to Confidex Viking Slim (32 x 74.63 mm) for Lottie export
  function sampleVikingSilhouette(inset = 0) {
    const pts = [];
    const hw = 16.0 - inset;
    const earR = 8.4 - inset * 0.5;
    const earCy = 28.9;
    for (let i = 0; i <= 14; i++) {
      const a = 0.08 * Math.PI + (i / 14) * (0.84 * Math.PI);
      pts.push([Math.cos(a) * earR, earCy + Math.sin(a) * earR]);
    }
    pts.push([-hw * 0.86, 26.8 - inset * 0.5]);
    pts.push([-hw, 21.0 - inset * 0.5]);
    pts.push([-hw, -21.0 + inset * 0.5]);
    pts.push([-hw * 0.86, -26.8 + inset * 0.5]);
    for (let i = 0; i <= 14; i++) {
      const a = 1.08 * Math.PI + (i / 14) * (0.84 * Math.PI);
      pts.push([Math.cos(a) * earR, -earCy + Math.sin(a) * earR]);
    }
    pts.push([hw * 0.86, -26.8 + inset * 0.5]);
    pts.push([hw, -21.0 + inset * 0.5]);
    pts.push([hw, 21.0 - inset * 0.5]);
    pts.push([hw * 0.86, 26.8 - inset * 0.5]);
    return pts;
  }

  const OUTER_BASE = sampleVikingSilhouette(0);
  const BODY_MID = sampleVikingSilhouette(0.6);
  const BODY_PLATEAU = sampleVikingSilhouette(2.15);

  const labelCache = new Map();

  function drawLabelContent(g, W, H, labelText, labelStyle, themePalette, subLabel = 'U14 - Floor 1') {
    const text = (labelText || 'B2').trim() || 'B2';
    const floorSubText = (subLabel || 'U14 - Floor 1').trim() || 'U14 - Floor 1';

    function drawFittedLabel(fontFamily, weight, color, maxWidthFrac = 0.90, maxH = 195, yOffset = 0, shadowColor = null) {
      const size = 180;
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
    }

    if (labelStyle === 'tape') {
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
        190,
        2,
        'rgba(32, 29, 27, 0.16)'
      );
      g.restore();
    } else if (labelStyle === 'printed') {
      g.strokeStyle = 'rgba(24, 22, 26, 0.30)';
      g.lineWidth = 3.0;
      g.beginPath();
      g.moveTo(14, 34); g.lineTo(14, 16); g.lineTo(32, 16);
      g.moveTo(W - 14, 34); g.lineTo(W - 14, 16); g.lineTo(W - 32, 16);
      g.moveTo(14, H - 34); g.lineTo(14, H - 16); g.lineTo(32, H - 16);
      g.moveTo(W - 14, H - 34); g.lineTo(W - 14, H - 16); g.lineTo(W - 32, H - 16);
      g.stroke();

      drawFittedLabel(
        '"Plus Jakarta Sans", "Inter", system-ui, -apple-system, sans-serif',
        '800',
        '#171518',
        0.92,
        200,
        -28
      );

      const subBaseSize = 48;
      const subFontFamily = '"Plus Jakarta Sans", "Inter", system-ui, -apple-system, sans-serif';
      g.font = `800 ${subBaseSize}px ${subFontFamily}`;
      const subMeasured = Math.max(1, g.measureText(floorSubText).width);
      const subFitted = Math.min(40, Math.max(24, Math.floor(subBaseSize * ((W * 0.72) / subMeasured))));
      g.font = `800 ${subFitted}px ${subFontFamily}`;
      g.textAlign = 'center';
      g.textBaseline = 'middle';
      g.fillStyle = 'rgba(23, 21, 24, 0.86)';
      g.fillText(floorSubText, W * 0.5, H - 56);
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
        185,
        0
      );
    }
  }

  function getLabelDecalCanvas(labelText, labelStyle, themePalette, subLabel = 'U14 - Floor 1') {
    const key = `${labelText}|${subLabel}|${labelStyle}|${themePalette.primary}|${themePalette.primaryContainer}|${themePalette.onPrimaryContainer}`;
    if (labelCache.has(key)) return labelCache.get(key);
    if (labelCache.size > 100) labelCache.clear();

    const W = 380;
    const H = 440;
    const c = document.createElement('canvas');
    c.width = W;
    c.height = H;
    const g = c.getContext('2d');

    drawLabelContent(g, W, H, labelText, labelStyle, themePalette, subLabel);

    labelCache.set(key, c);
    return c;
  }

  function drawVectorLabelOnPlateau(ctx, labelText, labelStyle, themePalette, corners, subLabel = 'U14 - Floor 1') {
    if (signedArea2D(corners) <= 0.5) return;
    const [pTL, pTR, pBR, pBL] = corners;
    const W = 380;
    const H = 440;

    ctx.save();
    ctx.beginPath();
    ctx.moveTo(pTL.x, pTL.y);
    ctx.lineTo(pTR.x, pTR.y);
    ctx.lineTo(pBR.x, pBR.y);
    ctx.lineTo(pBL.x, pBL.y);
    ctx.closePath();
    ctx.clip();

    const cScreenX = (pTL.x + pTR.x + pBR.x + pBL.x) * 0.25;
    const cScreenY = (pTL.y + pTR.y + pBR.y + pBL.y) * 0.25;
    const uVecX = ((pTR.x + pBR.x) - (pTL.x + pBL.x)) * 0.5 / W;
    const uVecY = ((pTR.y + pBR.y) - (pTL.y + pBL.y)) * 0.5 / W;
    const vVecX = ((pBL.x + pBR.x) - (pTL.x + pTR.x)) * 0.5 / H;
    const vVecY = ((pBL.y + pBR.y) - (pTL.y + pTR.y)) * 0.5 / H;
    const oScreenX = cScreenX - uVecX * (W * 0.5) - vVecX * (H * 0.5);
    const oScreenY = cScreenY - uVecY * (W * 0.5) - vVecY * (H * 0.5);

    ctx.transform(uVecX, uVecY, vVecX, vVecY, oScreenX, oScreenY);
    drawLabelContent(ctx, W, H, labelText, labelStyle, themePalette, subLabel);
    ctx.restore();
  }

  /**
   * Builds the programmatic wrap-around center band hugging the exact Viking Slim cross-section:
   *   plateau (x=+-13.88, z=6.97) -> chamfer (x=+-15.44, z=3.60) -> sidewall (x=+-16.12, z=-6.95)
   */
  function buildBandFaces(labelStyle, themePalette) {
    if (labelStyle !== 'tape' && labelStyle !== 'tonal') return [];
    const bandColor = labelStyle === 'tape'
      ? '#F2EDE0'
      : (themePalette.primaryContainer || '#FFD9DF');
    const bandMat = {
      base: bandColor,
      rough: labelStyle === 'tape' ? 0.78 : 0.35,
      spec: labelStyle === 'tape' ? 0.08 : 0.22,
    };

    const p = (window.VIKING_SLIM_MESH && window.VIKING_SLIM_MESH.profile) || {
      plateauZ: 6.79,
      plateauHalfW: 13.84,
      midZ: 3.57,
      midHalfW: 15.36,
      baseZ: -7.18,
      baseHalfW: 16.00,
      bandHalfH: 16.5,
    };

    const yT = p.bandHalfH;
    const yB = -p.bandHalfH;
    const xPlat = p.plateauHalfW + 0.12;
    const zPlat = p.plateauZ + 0.20;
    const xMid = p.midHalfW + 0.24;
    const zMid = p.midZ + 0.14;
    const xBot = p.baseHalfW + 0.24;
    const zBot = p.baseZ + 0.25;

    return [
      // Top plateau band surface (layer 2 prevents coplanar Z-fighting against subdivided hull plateau)
      {
        pts: [[-xPlat, yT, zPlat], [-xPlat, yB, zPlat], [xPlat, yB, zPlat], [xPlat, yT, zPlat]],
        normal: [0, 0, 1],
        mat: bandMat,
        layer: 2,
      },
      // Right chamfer wrap
      {
        pts: [[xPlat, yT, zPlat], [xPlat, yB, zPlat], [xMid, yB, zMid], [xMid, yT, zMid]],
        normal: norm3([zPlat - zMid, 0, xMid - xPlat]),
        mat: bandMat,
        layer: 1,
      },
      // Right drafted sidewall wrap
      {
        pts: [[xMid, yT, zMid], [xMid, yB, zMid], [xBot, yB, zBot], [xBot, yT, zBot]],
        normal: norm3([zMid - zBot, 0, xBot - xMid]),
        mat: bandMat,
        layer: 1,
      },
      // Left chamfer wrap
      {
        pts: [[-xMid, yT, zMid], [-xMid, yB, zMid], [-xPlat, yB, zPlat], [-xPlat, yT, zPlat]],
        normal: norm3([-(zPlat - zMid), 0, xMid - xPlat]),
        mat: bandMat,
        layer: 1,
      },
      // Left drafted sidewall wrap
      {
        pts: [[-xBot, yT, zBot], [-xBot, yB, zBot], [-xMid, yB, zMid], [-xMid, yT, zMid]],
        normal: norm3([-(zMid - zBot), 0, xBot - xMid]),
        mat: bandMat,
        layer: 1,
      },
    ];
  }

  function projectPoint(p3, rotM, camDist, fovScale, cx, cy) {
    const tp = transformPoint(p3, rotM);
    const zCam = camDist - tp[2];
    const k = fovScale / Math.max(zCam, 20);
    return {
      x: cx + tp[0] * k,
      y: cy - tp[1] * k,
      z: tp[2],
    };
  }

  let rasCanvas = null;
  let rasCtx = null;
  let rasW = 0;
  let rasH = 0;
  let rasImgData = null;
  let rasPix32 = null;
  let rasZBuf = null;

  function ensureRasterBuffer(w, h) {
    if (!rasCanvas) {
      rasCanvas = document.createElement('canvas');
      rasCtx = rasCanvas.getContext('2d');
    }
    if (rasW !== w || rasH !== h) {
      rasW = w;
      rasH = h;
      rasCanvas.width = w;
      rasCanvas.height = h;
      rasImgData = rasCtx.createImageData(w, h);
      rasPix32 = new Uint32Array(rasImgData.data.buffer);
      rasZBuf = new Float32Array(w * h);
    }
    rasPix32.fill(0);
    rasZBuf.fill(0);
  }

  function rasterizeGouraudTriangle(
    x0, y0, z0, r0, g0, b0,
    x1, y1, z1, r1, g1, b1,
    x2, y2, z2, r2, g2, b2,
    zBias
  ) {
    const area = (x1 - x0) * (y2 - y0) - (y1 - y0) * (x2 - x0);
    if (area >= -1e-3) return;
    const invArea = 1.0 / area;

    const minX = Math.max(0, Math.floor(Math.min(x0, x1, x2)));
    const maxX = Math.min(rasW - 1, Math.ceil(Math.max(x0, x1, x2)));
    const minY = Math.max(0, Math.floor(Math.min(y0, y1, y2)));
    const maxY = Math.min(rasH - 1, Math.ceil(Math.max(y0, y1, y2)));
    if (minX > maxX || minY > maxY) return;

    const e0dx = -(y2 - y1) * invArea;
    const e0dy = (x2 - x1) * invArea;
    const e1dx = -(y0 - y2) * invArea;
    const e1dy = (x0 - x2) * invArea;

    const startPx = minX + 0.5;
    const startPy = minY + 0.5;
    let w0Row = ((x1 - startPx) * (y2 - startPy) - (y1 - startPy) * (x2 - startPx)) * invArea;
    let w1Row = ((x2 - startPx) * (y0 - startPy) - (y2 - startPy) * (x0 - startPx)) * invArea;

    for (let y = minY; y <= maxY; y++) {
      let w0 = w0Row;
      let w1 = w1Row;
      let rowIdx = y * rasW + minX;
      for (let x = minX; x <= maxX; x++, rowIdx++) {
        const w2 = 1.0 - w0 - w1;
        if (w0 >= -1e-4 && w1 >= -1e-4 && w2 >= -1e-4) {
          const z = w0 * z0 + w1 * z1 + w2 * z2 + zBias;
          if (z > rasZBuf[rowIdx]) {
            rasZBuf[rowIdx] = z;
            const rRaw = (w0 * r0 + w1 * r1 + w2 * r2 + 0.5) | 0;
            const gRaw = (w0 * g0 + w1 * g1 + w2 * g2 + 0.5) | 0;
            const bRaw = (w0 * b0 + w1 * b1 + w2 * b2 + 0.5) | 0;
            const r = rRaw < 0 ? 0 : rRaw > 255 ? 255 : rRaw;
            const g = gRaw < 0 ? 0 : gRaw > 255 ? 255 : gRaw;
            const b = bRaw < 0 ? 0 : bRaw > 255 ? 255 : bRaw;
            rasPix32[rowIdx] = (255 << 24) | (b << 16) | (g << 8) | r;
          }
        }
        w0 += e0dx;
        w1 += e1dx;
      }
      w0Row += e0dy;
      w1Row += e1dy;
    }
  }

  let vBufCount = 0;
  let vScreenX = null;
  let vScreenY = null;
  let vWorldZ = null;
  let vColR = null;
  let vColG = null;
  let vColB = null;

  function ensureVertexBuffers(count) {
    if (vBufCount < count) {
      vBufCount = count;
      vScreenX = new Float32Array(count);
      vScreenY = new Float32Array(count);
      vWorldZ = new Float32Array(count);
      vColR = new Uint8Array(count);
      vColG = new Uint8Array(count);
      vColB = new Uint8Array(count);
    }
  }

  function renderBeaconCanvas(canvas, opts = {}) {
    if (!canvas) return;
    const rectW = opts.width || canvas.clientWidth || parseFloat(canvas.style.width) || 120;
    const rectH = opts.height || canvas.clientHeight || parseFloat(canvas.style.height) || 160;
    const hasFixedBuf = Boolean(opts.fixedBufferW && opts.fixedBufferH);
    const dpr = Math.min(window.devicePixelRatio || 1, opts.isHeroFlight ? 2.0 : 2.5);
    const targetW = Math.max(1, Math.round((opts.fixedBufferW || rectW) * dpr));
    const targetH = Math.max(1, Math.round((opts.fixedBufferH || rectH) * dpr));

    if (canvas.width !== targetW || canvas.height !== targetH) {
      canvas.width = targetW;
      canvas.height = targetH;
    }

    const ctx = canvas.getContext('2d');
    ctx.save();
    ctx.clearRect(0, 0, targetW, targetH);
    ctx.scale(targetW / Math.max(1, rectW), targetH / Math.max(1, rectH));

    const pitch = opts.pitch ?? 0;
    const yaw = opts.yaw ?? 0;
    const roll = opts.roll ?? 0;
    const label = opts.label || 'B2';
    const subLabel = opts.subLabel ?? 'U14 - Floor 1';
    const labelStyle = opts.labelStyle || 'printed';
    const dark = Boolean(opts.dark);
    const themePalette = opts.themePalette || {
      primary: '#D81648',
      primaryContainer: '#FFD9DF',
      onPrimaryContainer: '#400014',
      surface: dark ? '#0B0808' : '#FFFBFB',
    };

    const rotM = makeRotationMatrix(pitch, yaw, roll);
    const camDist = 250;

    // Auto-compute exact scale from Confidex Viking Slim bounds (32.00 x 74.63 x 14.37 mm)
    const bbox = [
      [-16.2, -37.5, -7.3], [16.2, -37.5, -7.3],
      [-16.2,  37.5, -7.3], [16.2,  37.5, -7.3],
      [-16.2, -37.5,  7.2], [16.2, -37.5,  7.2],
      [-16.2,  37.5,  7.2], [16.2,  37.5,  7.2],
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
    const zoom = opts.zoom || 1;
    const pxPerMm = Math.min((rectW * 0.90) / spanX, (rectH * 0.88) / spanY) * zoom;
    const fovScale = pxPerMm * camDist;

    const midProjX = ((minX + maxX) * 0.5) * pxPerMm;
    const midProjY = ((minY + maxY) * 0.5) * pxPerMm;
    const cx = rectW * 0.5 - midProjX;
    const cy = rectH * 0.5 - midProjY;

    // Table contact shadow when laying flat, subtle floor shadow when upright
    if (opts.shadow !== false) {
      const layProgress = clamp((Math.abs(pitch) + Math.abs(roll)) / 95, 0, 1);
      ctx.save();
      const topP = projectPoint([0, 29, -7.18], rotM, camDist, fovScale, cx, cy);
      const botP = projectPoint([0, -29, -7.18], rotM, camDist, fovScale, cx, cy);
      const midS = projectPoint([0, 0, -7.18], rotM, camDist, fovScale, cx, cy);
      const shAlpha = dark ? 0.50 : 0.22;

      if (layProgress > 0.25) {
        const ang = Math.atan2(botP.y - topP.y, botP.x - topP.x);
        const len = Math.max(pxPerMm * 18, Math.hypot(botP.x - topP.x, botP.y - topP.y) * 0.65);
        const thick = pxPerMm * 16.5;
        ctx.translate(midS.x + pxPerMm * 1.4, midS.y + pxPerMm * 4.5);
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
        const shRx = pxPerMm * 13.5;
        const grad = ctx.createRadialGradient(rectW * 0.5, shY, 1, rectW * 0.5, shY, shRx);
        grad.addColorStop(0, `rgba(0,0,0,${shAlpha * 0.7})`);
        grad.addColorStop(1, 'rgba(0,0,0,0)');
        ctx.fillStyle = grad;
        ctx.beginPath();
        ctx.ellipse(rectW * 0.5, shY, shRx, pxPerMm * 3.8, 0, 0, Math.PI * 2);
        ctx.fill();
      }
      ctx.restore();
    }

    const keyLight = norm3([0.45, 0.62, 0.65]);
    const fillLight = norm3([-0.6, -0.22, 0.48]);
    const rimLight = norm3([-0.3, 0.4, -0.85]);
    const halfVec = norm3([keyLight[0], keyLight[1], keyLight[2] + 1]);

    const whiteRgb = hexToRgb('#F3F4F6');

    function shadeVertex(nx, ny, nz, baseRgb, specStrength) {
      const nDotK = nx * keyLight[0] + ny * keyLight[1] + nz * keyLight[2];
      const nDotF = nx * fillLight[0] + ny * fillLight[1] + nz * fillLight[2];
      const nDotR = nx * rimLight[0] + ny * rimLight[1] + nz * rimLight[2];
      const nDotH = nx * halfVec[0] + ny * halfVec[1] + nz * halfVec[2];
      const hemi = 0.58 + 0.42 * ny;
      const spec = nDotH > 0 ? Math.pow(nDotH, 26) * specStrength * 255 : 0;

      let lit = 0.56
        + hemi * 0.14
        + (nDotK > 0 ? nDotK * 0.34 : 0)
        + (nDotF > 0 ? nDotF * 0.12 : 0)
        + (nDotR > 0 ? nDotR * 0.10 : 0);
      if (dark) lit *= 0.94;

      return [
        clamp(Math.round(baseRgb.r * lit + spec), 0, 255),
        clamp(Math.round(baseRgb.g * lit + spec), 0, 255),
        clamp(Math.round(baseRgb.b * lit + spec), 0, 255),
      ];
    }

    const maxRasDim = opts.isHeroFlight ? 1100 : 2200;
    const desiredScale = dpr * (opts.isHeroFlight ? 1.5 : 1.85);
    const ss = clamp(Math.min(maxRasDim / Math.max(rectW, rectH, 1), desiredScale), dpr, 3.0);
    const rw = Math.max(64, Math.round(rectW * ss));
    const rh = Math.max(64, Math.round(rectH * ss));
    ensureRasterBuffer(rw, rh);

    const fovScaleRas = fovScale * ss;
    const cxRas = cx * ss;
    const cyRas = cy * ss;

    const m0 = rotM[0], m1 = rotM[1], m2 = rotM[2];
    const m3 = rotM[3], m4 = rotM[4], m5 = rotM[5];
    const m6 = rotM[6], m7 = rotM[7], m8 = rotM[8];

    // 1. Per-pixel Z-buffered Gouraud rasterization of Blender Viking Slim mesh (.three indexed triangles)
    const meshSrc = window.VIKING_SLIM_MESH && window.VIKING_SLIM_MESH.three;
    if (meshSrc) {
      const pos = meshSrc.positions;
      const norm = meshSrc.normals;
      const idx = meshSrc.indices;
      const vCount = (pos.length / 3) | 0;
      ensureVertexBuffers(vCount);

      for (let i = 0; i < vCount; i++) {
        const i3 = i * 3;
        const x = pos[i3] * 1000;
        const y = pos[i3 + 1] * 1000;
        const z = pos[i3 + 2] * 1000;
        const tx = m0 * x + m1 * y + m2 * z;
        const ty = m3 * x + m4 * y + m5 * z;
        const tz = m6 * x + m7 * y + m8 * z;
        const zCam = Math.max(camDist - tz, 20);
        const k = fovScaleRas / zCam;
        vScreenX[i] = cxRas + tx * k;
        vScreenY[i] = cyRas - ty * k;
        vWorldZ[i] = 1.0 / zCam;

        const lnx = norm[i3], lny = norm[i3 + 1], lnz = norm[i3 + 2];
        const nx = m0 * lnx + m1 * lny + m2 * lnz;
        const ny = m3 * lnx + m4 * lny + m5 * lnz;
        const nz = m6 * lnx + m7 * lny + m8 * lnz;
        const [r, g, b] = shadeVertex(nx, ny, nz, whiteRgb, 0.20);
        vColR[i] = r;
        vColG[i] = g;
        vColB[i] = b;
      }

      const iLen = idx.length;
      for (let t = 0; t < iLen; t += 3) {
        const i0 = idx[t];
        const i1 = idx[t + 1];
        const i2 = idx[t + 2];
        rasterizeGouraudTriangle(
          vScreenX[i0], vScreenY[i0], vWorldZ[i0], vColR[i0], vColG[i0], vColB[i0],
          vScreenX[i1], vScreenY[i1], vWorldZ[i1], vColR[i1], vColG[i1], vColB[i1],
          vScreenX[i2], vScreenY[i2], vWorldZ[i2], vColR[i2], vColG[i2], vColB[i2],
          0
        );
      }
    }

    // 2. Z-buffered rasterization of programmatic center wrap band
    const bandFaces = buildBandFaces(labelStyle, themePalette);
    const bandInvZ = (p) => 1.0 / Math.max(camDist - (p.z + 0.45), 20);
    for (const bf of bandFaces) {
      const baseRgb = hexToRgb(bf.mat.base);
      const wn = norm3(transformPoint(bf.normal, rotM));
      const [br, bg, bb] = shadeVertex(wn[0], wn[1], wn[2], baseRgb, bf.mat.spec || 0.15);
      const pts = bf.pts;
      const p0 = projectPoint(pts[0], rotM, camDist, fovScaleRas, cxRas, cyRas);
      const p1 = projectPoint(pts[1], rotM, camDist, fovScaleRas, cxRas, cyRas);
      const p2 = projectPoint(pts[2], rotM, camDist, fovScaleRas, cxRas, cyRas);
      const p3 = projectPoint(pts[3], rotM, camDist, fovScaleRas, cxRas, cyRas);
      const z0 = bandInvZ(p0);
      const z1 = bandInvZ(p1);
      const z2 = bandInvZ(p2);
      const z3 = bandInvZ(p3);
      rasterizeGouraudTriangle(
        p0.x, p0.y, z0, br, bg, bb,
        p1.x, p1.y, z1, br, bg, bb,
        p2.x, p2.y, z2, br, bg, bb,
        0
      );
      rasterizeGouraudTriangle(
        p0.x, p0.y, z0, br, bg, bb,
        p2.x, p2.y, z2, br, bg, bb,
        p3.x, p3.y, z3, br, bg, bb,
        0
      );
    }

    rasCtx.putImageData(rasImgData, 0, 0);
    ctx.imageSmoothingEnabled = true;
    ctx.imageSmoothingQuality = 'high';
    ctx.drawImage(rasCanvas, 0, 0, rw, rh, 0, 0, rectW, rectH);

    // 3. Draw resolution-independent vector label directly onto the projected top plateau
    const prof = (window.VIKING_SLIM_MESH && window.VIKING_SLIM_MESH.profile) || {
      plateauZ: 6.79,
      plateauHalfW: 13.84,
      bandHalfH: 16.5,
    };
    const zDecal = labelStyle === 'printed' ? prof.plateauZ + 0.07 : prof.plateauZ + 0.24;
    const bandHalfW = prof.plateauHalfW - 0.10;
    const bandHalfH = prof.bandHalfH - 0.20;
    const pTL = projectPoint([-bandHalfW, bandHalfH, zDecal], rotM, camDist, fovScale, cx, cy);
    const pTR = projectPoint([bandHalfW, bandHalfH, zDecal], rotM, camDist, fovScale, cx, cy);
    const pBR = projectPoint([bandHalfW, -bandHalfH, zDecal], rotM, camDist, fovScale, cx, cy);
    const pBL = projectPoint([-bandHalfW, -bandHalfH, zDecal], rotM, camDist, fovScale, cx, cy);
    drawVectorLabelOnPlateau(ctx, label, labelStyle, themePalette, [pTL, pTR, pBR, pBL], subLabel);

    ctx.restore();
  }

  /**
   * Generates a valid Lottie JSON animation (512x512, 60fps, 60 frames)
   * animating the Viking Slim beacon from vertical front-facing (frame 0)
   * to table-top 3/4 perspective view (frame 60).
   */
  function generateBeaconLottieJson(opts = {}) {
    const label = opts.label || 'B2';
    const labelStyle = opts.labelStyle || 'printed';
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

    const bandPts = [[-13.84, 16.5], [13.84, 16.5], [13.84, -16.5], [-13.84, -16.5]];

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
          p: { a: 0, k: [256, 256, -30] },
          a: { a: 0, k: [0, -14, 0] },
          s: { a: 0, k: [100, 100, 100] },
        },
        t: {
          d: {
            k: [{
              s: {
                s: 44,
                f: 'sans-serif',
                t: label,
                j: 2,
                tr: 0,
                lh: 50,
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
            { ty: 'sh', nm: 'BandPath', ks: { a: 1, k: buildKeyframedShape(bandPts, 6.97) } },
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
            { ty: 'sh', nm: 'PlateauPath', ks: { a: 1, k: buildKeyframedShape(BODY_PLATEAU, 6.79) } },
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
        nm: 'Outer_Shell_Profile',
        sr: 1,
        ks: { o: { a: 0, k: 100 }, r: { a: 0, k: 0 }, p: { a: 0, k: [256, 256, 0] }, a: { a: 0, k: [0, 0, 0] }, s: { a: 0, k: [100, 100, 100] } },
        shapes: [{
          ty: 'gr',
          nm: 'EarsGroup',
          it: [
            { ty: 'sh', nm: 'OuterPath', ks: { a: 1, k: buildKeyframedShape(OUTER_BASE, -7.18) } },
            { ty: 'fl', nm: 'EarsFill', c: { a: 0, k: rgb01('#CFD5E0') }, o: { a: 0, k: 100 } },
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
      nm: `Viking_Slim_${label}_Table_Turn`,
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
    a.download = `viking_slim_${(opts.label || 'b2').toLowerCase()}_turn.json`;
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
