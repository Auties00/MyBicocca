/**
 * app.jsx — MyBicocca BLE Beacons Interactive Material 3 Prototype.
 * Implements:
 *   - Exact MyBicocca palettes (Default, Oceano, Bosco × Light/Dark)
 *   - Authentic MyBicoccaTopBar morph, SegmentedSwitch, SegmentedDirectory,
 *     BookingSummaryCard, OrganicShapes, and DetailFactCard components
 *   - List View (3D vertical front beacon + label, pkt/s, power) &
 *     Grid View (3D vertical front beacon + label only)
 *   - True 3D Shared-Element Hero Transition (0° vertical front → 3/4 view)
 *     with live interactive Scrubber (0%..100%) and 4-Layout Comparison Canvas
 */

const { useState, useEffect, useRef, useMemo, useCallback } = React;

function lerp(a, b, t) {
  return a + (b - a) * t;
}

function clamp01(t) {
  return t < 0 ? 0 : t > 1 ? 1 : t;
}

// Material 3 Expressive spatial easing approximation
function m3Ease(t) {
  const x = clamp01(t);
  return x < 0.5
    ? 4 * x * x * x
    : 1 - Math.pow(-2 * x + 2, 3) / 2;
}

function fadeThroughCollapsed(p) {
  return clamp01((0.5 - p) / 0.5);
}

function fadeThroughExpanded(p) {
  return clamp01((p - 0.5) / 0.5);
}

function mixHex(hexA, hexB, t) {
  const parse = (h) => {
    const clean = (h || '#ffd9df').replace('#', '');
    const n = parseInt(clean.length === 3 ? clean.split('').map((c) => c + c).join('') : clean.slice(0, 6), 16);
    return [(n >> 16) & 255, (n >> 8) & 255, n & 255];
  };
  const [r1, g1, b1] = parse(hexA);
  const [r2, g2, b2] = parse(hexB);
  const r = Math.round(lerp(r1, r2, t));
  const g = Math.round(lerp(g1, g2, t));
  const b = Math.round(lerp(b1, b2, t));
  return `rgb(${r},${g},${b})`;
}

function getOrganicCardMeta(idx, viewMode, palette) {
  const even = idx % 2 === 0;
  return {
    lobes: even ? 6 : 8,
    dip: viewMode === 'grid' ? (even ? 0.18 : 0.14) : 0.18,
    size: viewMode === 'grid' ? 74 : 62,
    rotation: idx * (viewMode === 'grid' ? 0.4 : 0.35),
    color: palette.primaryContainer,
  };
}

function getSignalBadgeStyle(frameType) {
  const t = (frameType || '').toLowerCase();
  if (t.includes('uid')) {
    return {
      shortLabel: 'UID',
      fullLabel: 'ED-UID',
      bg: '#123C25',
      fg: '#79E8AA',
      dot: '#4ADE80',
    };
  }
  if (t.includes('url')) {
    return {
      shortLabel: 'URL',
      fullLabel: 'ED-URL',
      bg: '#143840',
      fg: '#7CE8FF',
      dot: '#22D3EE',
    };
  }
  if (t.includes('tlm')) {
    return {
      shortLabel: 'TLM',
      fullLabel: 'ED-TLM',
      bg: '#4E1F06',
      fg: '#FFB58A',
      dot: '#FB923C',
    };
  }
  if (t.includes('alt')) {
    return {
      shortLabel: 'Alt',
      fullLabel: 'AltBeacon',
      bg: '#341B59',
      fg: '#D2B8FF',
      dot: '#C084FC',
    };
  }
  return {
    shortLabel: 'iB',
    fullLabel: 'iBeacon',
    bg: '#103457',
    fg: '#8DC8FF',
    dot: '#60A5FA',
  };
}

function getLocalRect(el, containerEl) {
  if (!el || !containerEl) return null;
  const er = el.getBoundingClientRect();
  const cr = containerEl.getBoundingClientRect();
  const sx = cr.width / (containerEl.offsetWidth || 390);
  const sy = cr.height / (containerEl.offsetHeight || 844);
  return {
    left: (er.left - cr.left) / sx,
    top: (er.top - cr.top) / sy,
    width: er.width / sx,
    height: er.height / sy,
  };
}

// Lightweight Material Symbols / Outlined Vector Icons
function IconSvg({ name, size = 20, color = 'currentColor', filled = false }) {
  const s = { width: size, height: size, display: 'block', flexShrink: 0 };
  switch (name) {
    case 'search':
      return (
        <svg style={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <circle cx="11" cy="11" r="7" />
          <path d="M20 20l-3.5-3.5" />
        </svg>
      );
    case 'back':
      return (
        <svg style={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2.1" strokeLinecap="round" strokeLinejoin="round">
          <path d="M19 12H5" />
          <path d="M12 19l-7-7 7-7" />
        </svg>
      );
    case 'close':
      return (
        <svg style={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2.1" strokeLinecap="round">
          <path d="M18 6L6 18M6 6l12 12" />
        </svg>
      );
    case 'star':
      return (
        <svg style={s} viewBox="0 0 24 24" fill={filled ? color : 'none'} stroke={color} strokeWidth="2" strokeLinejoin="round">
          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />
        </svg>
      );
    case 'list':
      return (
        <svg style={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round">
          <line x1="8" y1="6" x2="21" y2="6" />
          <line x1="8" y1="12" x2="21" y2="12" />
          <line x1="8" y1="18" x2="21" y2="18" />
          <circle cx="4" cy="6" r="1" fill={color} />
          <circle cx="4" cy="12" r="1" fill={color} />
          <circle cx="4" cy="18" r="1" fill={color} />
        </svg>
      );
    case 'grid':
      return (
        <svg style={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinejoin="round">
          <rect x="3" y="3" width="7.5" height="7.5" rx="2" />
          <rect x="13.5" y="3" width="7.5" height="7.5" rx="2" />
          <rect x="3" y="13.5" width="7.5" height="7.5" rx="2" />
          <rect x="13.5" y="13.5" width="7.5" height="7.5" rx="2" />
        </svg>
      );
    case 'bolt':
      return (
        <svg style={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M13 2L4 14h7l-1 8 9-12h-7l1-8z" />
        </svg>
      );
    case 'signal':
      return (
        <svg style={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round">
          <path d="M5 12.55a11 11 0 0 1 14.08 0" />
          <path d="M8.53 16.11a6 6 0 0 1 6.95 0" />
          <circle cx="12" cy="19.5" r="1" fill={color} />
        </svg>
      );
    case 'pin':
      return (
        <svg style={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M21 10c0 7-9 13-9 13S3 17 3 10a9 9 0 0 1 18 0z" />
          <circle cx="12" cy="10" r="3" />
        </svg>
      );
    case 'bluetooth':
      return (
        <svg style={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M6.5 6.5l11 11L12 23V1l5.5 5.5-11 11" />
        </svg>
      );
    case 'chip':
      return (
        <svg style={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <rect x="6" y="6" width="12" height="12" rx="2" />
          <path d="M9 1v5M15 1v5M9 18v5M15 18v5M1 9h5M1 15h5M18 9h5M18 15h5" />
        </svg>
      );
    case 'battery':
      return (
        <svg style={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <rect x="2" y="7" width="18" height="10" rx="2" />
          <line x1="22" y1="10" x2="22" y2="14" />
          <rect x="5" y="10" width="9" height="4" rx="0.5" fill={color} stroke="none" />
        </svg>
      );
    case 'code':
      return (
        <svg style={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <polyline points="16 18 22 12 16 6" />
          <polyline points="8 6 2 12 8 18" />
        </svg>
      );
    case 'cube':
      return (
        <svg style={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M12 2l8.5 5v10L12 22l-8.5-5V7L12 2z" />
          <path d="M12 22V12M12 12l8.5-5M12 12L3.5 7" />
        </svg>
      );
    case 'chevron':
      return (
        <svg style={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M9 18l6-6-6-6" />
        </svg>
      );
    case 'tune':
      return (
        <svg style={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round">
          <line x1="4" y1="6" x2="20" y2="6" />
          <circle cx="9" cy="6" r="2.2" fill="currentColor" stroke="none" />
          <line x1="4" y1="12" x2="20" y2="12" />
          <circle cx="15" cy="12" r="2.2" fill="currentColor" stroke="none" />
          <line x1="4" y1="18" x2="20" y2="18" />
          <circle cx="11" cy="18" r="2.2" fill="currentColor" stroke="none" />
        </svg>
      );
    case 'more-vert':
      return (
        <svg style={s} viewBox="0 0 24 24" fill={color}>
          <circle cx="12" cy="5.5" r="1.75" />
          <circle cx="12" cy="12" r="1.75" />
          <circle cx="12" cy="18.5" r="1.75" />
        </svg>
      );
    case 'edit':
      return (
        <svg style={s} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M12 20h9" />
          <path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
        </svg>
      );
    default:
      return null;
  }
}

/**
 * Mirrors OrganicShapes.kt (SmoothCookieShape) as crisp SVG background halos.
 */
function OrganicHalo({ lobes = 6, dip = 0.2, size = 84, color, rotation = 0, hidden = false }) {
  const pathData = useMemo(() => {
    const samples = 120;
    const cx = 50, cy = 50, rx = 48, ry = 48;
    let d = '';
    for (let i = 0; i < samples; i++) {
      const a = (i / samples) * Math.PI * 2 - Math.PI / 2 + rotation;
      const r = 1 - dip * (1 - Math.cos(lobes * (a - rotation))) / 2;
      const x = (cx + Math.cos(a) * rx * r).toFixed(2);
      const y = (cy + Math.sin(a) * ry * r).toFixed(2);
      d += (i === 0 ? `M${x},${y}` : ` L${x},${y}`);
    }
    return d + ' Z';
  }, [lobes, dip, rotation]);

  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 100 100"
      style={{
        position: 'absolute',
        pointerEvents: 'none',
        left: '50%',
        top: '50%',
        transform: 'translate(-50%, -50%)',
        visibility: hidden ? 'hidden' : 'visible',
      }}
    >
      <path d={pathData} fill={color} />
    </svg>
  );
}

/**
 * Smoothly morphs the card's 6/8-lobe SmoothCookie shape into the Detail stage's 12-lobe cookie
 * along Hero transition progress t in [0..1].
 */
function MorphingOrganicHalo({
  fromLobes = 6,
  fromDip = 0.18,
  fromRot = 0,
  fromColor,
  toLobes = 12,
  toDip = 0.10,
  toRot = 0,
  toColor,
  size,
  t,
}) {
  const samples = 144;
  const cx = 50, cy = 50, rx = 48, ry = 48;
  let d = '';
  for (let i = 0; i < samples; i++) {
    const a = (i / samples) * Math.PI * 2 - Math.PI / 2;
    const rFrom = 1 - fromDip * (1 - Math.cos(fromLobes * (a - fromRot))) / 2;
    const rTo = 1 - toDip * (1 - Math.cos(toLobes * (a - toRot))) / 2;
    const r = lerp(rFrom, rTo, t);
    const x = (cx + Math.cos(a) * rx * r).toFixed(2);
    const y = (cy + Math.sin(a) * ry * r).toFixed(2);
    d += (i === 0 ? `M${x},${y}` : ` L${x},${y}`);
  }
  d += ' Z';

  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 100 100"
      style={{
        position: 'absolute',
        pointerEvents: 'none',
        left: '50%',
        top: '50%',
        transform: 'translate(-50%, -50%)',
      }}
    >
      <path d={d} fill={mixHex(fromColor, toColor, t)} />
    </svg>
  );
}

/**
 * Renders the 3D Beacon onto a <canvas> at given orientation (pitch, yaw, roll)
 * and label/style/palette.
 */
function BeaconCanvas({
  label,
  subLabel = 'U14 - Floor 1',
  labelStyle,
  pitch = 0,
  yaw = 0,
  roll = 0,
  width = 64,
  height = 96,
  fixedBufferW = null,
  fixedBufferH = null,
  palette,
  dark,
  hidden = false,
  holeColor,
  shadow = true,
  innerRef = null,
}) {
  const localRef = useRef(null);

  const setRefs = useCallback((el) => {
    localRef.current = el;
    if (typeof innerRef === 'function') innerRef(el);
    else if (innerRef) innerRef.current = el;
  }, [innerRef]);

  useEffect(() => {
    if (!localRef.current || !window.Beacon3D) return;
    window.Beacon3D.renderBeaconCanvas(localRef.current, {
      label,
      subLabel,
      labelStyle,
      pitch,
      yaw,
      roll,
      width,
      height,
      fixedBufferW,
      fixedBufferH,
      isHeroFlight: Boolean(fixedBufferW && fixedBufferH),
      themePalette: palette,
      dark,
      holeColor,
      shadow,
    });
  }, [label, subLabel, labelStyle, pitch, yaw, roll, width, height, fixedBufferW, fixedBufferH, palette, dark, holeColor, shadow]);

  return (
    <canvas
      ref={setRefs}
      style={{
        width: `${width}px`,
        height: `${height}px`,
        display: 'block',
        visibility: hidden ? 'hidden' : 'visible',
        pointerEvents: 'none',
      }}
    />
  );
}

/**
 * Replicates MyBicoccaTopBar.kt:
 *   - Collapsed pill (56px height, 28px radius, floating 16px side margins)
 *   - Morphs smoothly along navProgress (0..1) or searchProgress (0..1)
 *     into full-width sub-page / search top bar.
 */
function MyBicoccaTopBar({
  navProgress,
  searchOpen,
  searchQuery,
  onSearchQueryChange,
  onToggleSearch,
  onBack,
  subPageTitle,
  palette,
  onEditBeacon,
  liveScanning,
  onToggleLiveScan,
}) {
  const [menuOpen, setMenuOpen] = useState(false);
  const p = clamp01(Math.max(navProgress, searchOpen ? 1 : 0));
  const isSubPage = navProgress > 0.05;
  const isSearchMode = searchOpen && !isSubPage;

  useEffect(() => {
    if (!isSubPage && menuOpen) setMenuOpen(false);
  }, [isSubPage, menuOpen]);

  const hMargin = lerp(16, 0, p);
  const topGap = lerp(12, 0, p);
  const radius = lerp(28, 0, p);

  const collapsedAlpha = fadeThroughCollapsed(p);
  const expandedAlpha = fadeThroughExpanded(p);

  return (
    <div
      style={{
        position: 'relative',
        zIndex: 30,
        paddingTop: `${topGap}px`,
        paddingLeft: `${hMargin}px`,
        paddingRight: `${hMargin}px`,
        flexShrink: 0,
      }}
    >
      <div
        style={{
          height: '56px',
          borderRadius: `${radius}px`,
          backgroundColor: p > 0.5 ? palette.surfaceContainer : palette.surfaceContainerHigh,
          borderBottom: p > 0.7 ? `1px solid ${palette.outlineVariant}` : '1px solid transparent',
          display: 'flex',
          alignItems: 'center',
          padding: '0 8px',
          gap: '6px',
          boxShadow: p < 0.35 ? '0 3px 14px rgba(0,0,0,0.06)' : 'none',
          transition: 'background-color 0.15s ease',
        }}
      >
        {/* Leading IconButton: Search <-> Back Arrow */}
        <button
          type="button"
          className="m3-icon-btn"
          aria-label={p > 0.5 ? 'Back' : 'Search'}
          onClick={() => {
            if (isSubPage) onBack();
            else onToggleSearch(!searchOpen);
          }}
          style={{ color: palette.onSurface }}
        >
          <div style={{ position: 'relative', width: 20, height: 20 }}>
            <div style={{
              position: 'absolute', inset: 0,
              opacity: collapsedAlpha,
              transform: `scale(${lerp(0.6, 1, collapsedAlpha)})`,
            }}>
              <IconSvg name="search" size={20} color={palette.onSurface} />
            </div>
            <div style={{
              position: 'absolute', inset: 0,
              opacity: expandedAlpha,
              transform: `scale(${lerp(0.6, 1, expandedAlpha)})`,
            }}>
              <IconSvg name="back" size={20} color={palette.onSurface} />
            </div>
          </div>
        </button>

        {/* Center Title / Wordmark / Search Input */}
        <div
          onClick={() => {
            if (!isSubPage && !searchOpen && p < 0.1) onToggleSearch(true);
          }}
          style={{
            flex: 1,
            minWidth: 0,
            height: '100%',
            position: 'relative',
            display: 'flex',
            alignItems: 'center',
            cursor: (!isSubPage && !searchOpen) ? 'pointer' : 'default',
          }}
        >
          {/* Wordmark: centered at p=0, morphs out */}
          <div
            style={{
              position: 'absolute',
              left: 0,
              right: 0,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '6px',
              opacity: collapsedAlpha,
              transform: `translateX(${lerp(0, -32, p)}px) scale(${lerp(0.92, 1, collapsedAlpha)})`,
              pointerEvents: collapsedAlpha > 0.1 ? 'auto' : 'none',
            }}
          >
            <span style={{ fontSize: 19, fontWeight: 700, letterSpacing: '-0.02em' }}>
              <span style={{ color: palette.onSurface }}>My</span>
              <span style={{ color: '#D81648' }}>Bicocca</span>
            </span>
            <span
              style={{
                fontSize: 10,
                fontWeight: 700,
                letterSpacing: '0.04em',
                padding: '2px 7px',
                borderRadius: '999px',
                backgroundColor: palette.primaryContainer,
                color: palette.onPrimaryContainer,
              }}
            >
              U14 BLE
            </span>
          </div>

          {/* Sub-page Title */}
          {isSubPage && (
            <div
              style={{
                opacity: expandedAlpha,
                transform: `scale(${lerp(0.92, 1, expandedAlpha)})`,
                transformOrigin: 'left center',
                paddingLeft: '4px',
                whiteSpace: 'nowrap',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                fontSize: 17,
                fontWeight: 700,
                letterSpacing: '-0.015em',
                color: palette.onSurface,
              }}
            >
              {subPageTitle}
            </div>
          )}

          {/* Active Search Field */}
          {isSearchMode && (
            <input
              type="text"
              autoFocus
              value={searchQuery}
              onChange={(e) => onSearchQueryChange(e.target.value)}
              placeholder="Search B2, Lab 102, iBeacon..."
              style={{
                width: '100%',
                border: 'none',
                outline: 'none',
                background: 'transparent',
                color: palette.onSurface,
                fontSize: 15,
                fontWeight: 500,
                fontFamily: 'inherit',
                opacity: expandedAlpha,
              }}
            />
          )}
        </div>

        {/* Trailing Slot */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '2px' }}>
          {!isSubPage && !isSearchMode && (
            <button
              type="button"
              className="m3-icon-btn"
              title={liveScanning ? 'Pause live RSSI jitter' : 'Resume live RSSI scan'}
              onClick={onToggleLiveScan}
              style={{ opacity: collapsedAlpha }}
            >
              <div
                style={{
                  width: 34,
                  height: 34,
                  borderRadius: '50%',
                  backgroundColor: liveScanning ? palette.primaryContainer : palette.surfaceContainerHighest,
                  display: 'grid',
                  placeItems: 'center',
                  position: 'relative',
                }}
              >
                <IconSvg
                  name="signal"
                  size={18}
                  color={liveScanning ? palette.onPrimaryContainer : palette.onSurfaceVariant}
                />
                {liveScanning && (
                  <span
                    className="live-dot"
                    style={{ backgroundColor: palette.primary }}
                  />
                )}
              </div>
            </button>
          )}
          {isSearchMode && searchQuery.length > 0 && (
            <button
              type="button"
              className="m3-icon-btn"
              aria-label="Clear search"
              onClick={() => onSearchQueryChange('')}
              style={{ color: palette.onSurface }}
            >
              <IconSvg name="close" size={18} color={palette.onSurface} />
            </button>
          )}
          {isSubPage && (
            <div style={{ position: 'relative' }}>
              <button
                id="btnDetailMoreMenu"
                type="button"
                className="m3-icon-btn"
                aria-label="More options"
                onClick={() => setMenuOpen((o) => !o)}
                style={{
                  opacity: expandedAlpha,
                  color: palette.onSurfaceVariant,
                }}
              >
                <IconSvg
                  name="more-vert"
                  size={20}
                  color={palette.onSurfaceVariant}
                />
              </button>

              {menuOpen && (
                <>
                  <div
                    style={{ position: 'fixed', inset: 0, zIndex: 49 }}
                    onClick={() => setMenuOpen(false)}
                  />
                  <div
                    id="detailMoreDropdown"
                    style={{
                      position: 'absolute',
                      top: 44,
                      right: 2,
                      minWidth: 184,
                      backgroundColor: palette.surfaceContainerHigh,
                      border: `1px solid ${palette.outlineVariant}`,
                      borderRadius: 16,
                      padding: '6px 0',
                      boxShadow: '0 12px 32px rgba(0,0,0,0.6)',
                      zIndex: 55,
                      display: 'flex',
                      flexDirection: 'column',
                    }}
                  >
                    <button
                      id="menuItemEditBeacon"
                      type="button"
                      onClick={() => {
                        setMenuOpen(false);
                        if (onEditBeacon) onEditBeacon();
                      }}
                      style={{
                        border: 'none',
                        background: 'transparent',
                        padding: '10px 14px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: 10,
                        fontSize: 13.5,
                        fontWeight: 700,
                        color: palette.onSurface,
                        fontFamily: 'inherit',
                        cursor: 'pointer',
                        textAlign: 'left',
                      }}
                    >
                      <IconSvg name="edit" size={16} color={palette.primary} />
                      <span>Edit</span>
                    </button>
                    <button
                      type="button"
                      onClick={() => setMenuOpen(false)}
                      style={{
                        border: 'none',
                        background: 'transparent',
                        padding: '10px 14px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: 10,
                        fontSize: 13,
                        fontWeight: 600,
                        color: palette.onSurfaceVariant,
                        fontFamily: 'inherit',
                        cursor: 'pointer',
                        textAlign: 'left',
                      }}
                    >
                      <IconSvg name="chip" size={16} color={palette.onSurfaceVariant} />
                      <span>Copy MAC Address</span>
                    </button>
                    <button
                      type="button"
                      onClick={() => setMenuOpen(false)}
                      style={{
                        border: 'none',
                        background: 'transparent',
                        padding: '10px 14px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: 10,
                        fontSize: 13,
                        fontWeight: 600,
                        color: palette.onSurfaceVariant,
                        fontFamily: 'inherit',
                        cursor: 'pointer',
                        textAlign: 'left',
                      }}
                    >
                      <IconSvg name="signal" size={16} color={palette.onSurfaceVariant} />
                      <span>Refresh Telemetry</span>
                    </button>
                  </div>
                </>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

/**
 * Replicates SegmentedSwitch.kt:
 *   50px pill switch with outline border and animated sliding primaryContainer thumb.
 */
function SegmentedSwitch({ options, selected, onSelect, palette }) {
  const idx = Math.max(0, options.findIndex((o) => o.id === selected));
  return (
    <div
      style={{
        position: 'relative',
        height: 46,
        borderRadius: 999,
        border: `1px solid ${palette.outline}80`,
        padding: 4,
        display: 'grid',
        gridTemplateColumns: `repeat(${options.length}, minmax(0, 1fr))`,
        backgroundColor: palette.surfaceContainerLowest,
        userSelect: 'none',
      }}
    >
      <div
        style={{
          position: 'absolute',
          top: 4,
          bottom: 4,
          left: 4,
          width: `calc((100% - 8px) / ${options.length})`,
          transform: `translateX(${idx * 100}%)`,
          transition: 'transform 0.28s cubic-bezier(0.2, 0.9, 0.3, 1)',
          borderRadius: 999,
          backgroundColor: palette.primaryContainer,
          pointerEvents: 'none',
        }}
      />
      {options.map((opt) => {
        const active = opt.id === selected;
        return (
          <button
            key={opt.id}
            type="button"
            onClick={() => onSelect(opt.id)}
            style={{
              position: 'relative',
              zIndex: 1,
              border: 'none',
              background: 'transparent',
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 6,
              fontFamily: 'inherit',
              fontSize: 13,
              fontWeight: 700,
              color: active ? palette.onPrimaryContainer : palette.onSurfaceVariant,
              transition: 'color 0.2s',
            }}
          >
            {opt.icon && (
              <IconSvg
                name={opt.icon}
                size={16}
                color={active ? palette.onPrimaryContainer : palette.onSurfaceVariant}
              />
            )}
            <span>{opt.label}</span>
          </button>
        );
      })}
    </div>
  );
}

/**
 * Replicates DetailFactCard.kt:
 *   surfaceContainer card (20px radius), 44px surfaceContainerHighest icon chip (14px radius),
 *   labelSmall over bodyLarge (optional mono).
 */
function DetailFactCard({ icon, label, value, mono = false, palette }) {
  return (
    <div
      style={{
        backgroundColor: palette.surfaceContainer,
        borderRadius: 20,
        padding: 14,
        display: 'flex',
        alignItems: 'center',
        gap: 14,
      }}
    >
      <div
        style={{
          width: 42,
          height: 42,
          borderRadius: 13,
          backgroundColor: palette.surfaceContainerHighest,
          display: 'grid',
          placeItems: 'center',
          flexShrink: 0,
        }}
      >
        <IconSvg name={icon} size={20} color={palette.onSurfaceVariant} />
      </div>
      <div style={{ minWidth: 0, flex: 1 }}>
        <div
          style={{
            fontSize: 11,
            fontWeight: 700,
            color: palette.onSurfaceVariant,
            letterSpacing: '0.02em',
            marginBottom: 2,
          }}
        >
          {label}
        </div>
        <div
          style={{
            fontSize: mono ? 12.5 : 14.5,
            fontWeight: mono ? 600 : 600,
            fontFamily: mono ? "'JetBrains Mono', ui-monospace, monospace" : 'inherit',
            color: palette.onSurface,
            wordBreak: 'break-all',
            lineHeight: 1.35,
          }}
        >
          {value}
        </div>
      </div>
    </div>
  );
}

function DetailSlotPlaceholder({ title, slotColor, fields, palette }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
      <div
        style={{
          backgroundColor: palette.surfaceContainer,
          border: `1px solid ${palette.outlineVariant}`,
          borderRadius: 18,
          padding: '12px 14px',
          display: 'flex',
          alignItems: 'center',
          gap: 10,
        }}
      >
        <div
          style={{
            width: 10,
            height: 10,
            borderRadius: '50%',
            backgroundColor: slotColor,
            flexShrink: 0,
          }}
        />
        <div style={{ minWidth: 0, flex: 1 }}>
          <div style={{ fontSize: 12.5, fontWeight: 800, color: palette.onSurface }}>
            {title} — Cipher Reader Pending
          </div>
          <div style={{ fontSize: 11, color: palette.onSurfaceVariant, marginTop: 2, lineHeight: 1.35 }}>
            Encrypted Confidex Viking Slim slot registers require the cipher reader module (placeholder schema below).
          </div>
        </div>
      </div>

      <div
        style={{
          backgroundColor: palette.surfaceContainer,
          borderRadius: 20,
          padding: '6px 14px',
          border: `1px solid ${palette.outlineVariant}`,
        }}
      >
        {fields.map((f, idx) => (
          <div
            key={f.label}
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              gap: 10,
              padding: '10px 0',
              borderBottom: idx < fields.length - 1 ? `1px solid ${palette.outlineVariant}` : 'none',
            }}
          >
            <span style={{ fontSize: 12, fontWeight: 600, color: palette.onSurfaceVariant }}>
              {f.label}
            </span>
            <span
              style={{
                fontSize: 12,
                fontWeight: 700,
                fontFamily: f.mono ? "'JetBrains Mono', ui-monospace, monospace" : 'inherit',
                color: f.pending ? palette.onSurfaceVariant : palette.onSurface,
                textAlign: 'right',
                wordBreak: 'break-all',
                maxWidth: '58%',
              }}
            >
              {f.value}
            </span>
          </div>
        ))}
      </div>
    </div>
  );
}

function SaveBeaconModalSheet({ beacon, palette, labelStyle, dark, onSave, onClose }) {
  const [label, setLabel] = useState(beacon.label || '');
  const [code, setCode] = useState(beacon.code || '');
  const [building, setBuilding] = useState(beacon.building || 'U14');
  const [floor, setFloor] = useState(beacon.floor || 'Floor 1');
  const [positionDescription, setPositionDescription] = useState(beacon.positionDescription || beacon.zone || '');

  const inputStyle = {
    width: '100%',
    borderRadius: 12,
    border: `1px solid ${palette.outline}`,
    backgroundColor: palette.surfaceContainer,
    color: palette.onSurface,
    padding: '7px 10px',
    fontSize: 13,
    fontWeight: 600,
    fontFamily: 'inherit',
    outline: 'none',
  };

  return (
    <div
      style={{
        position: 'absolute',
        inset: 0,
        zIndex: 60,
        backgroundColor: 'rgba(0, 0, 0, 0.64)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'flex-end',
      }}
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div
        id="saveBeaconModalSheet"
        style={{
          backgroundColor: palette.surfaceContainerLow,
          borderTop: `1px solid ${palette.outlineVariant}`,
          borderRadius: '26px 26px 0 0',
          padding: '8px 16px 14px',
          display: 'flex',
          flexDirection: 'column',
          gap: 8,
          maxHeight: '88%',
          overflowY: 'auto',
          boxShadow: '0 -12px 40px rgba(0,0,0,0.65)',
        }}
      >
        <div style={{ width: 38, height: 4, borderRadius: 99, backgroundColor: palette.outlineVariant, alignSelf: 'center', marginBottom: 2 }} />
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{ position: 'relative', width: 58, height: 74, display: 'grid', placeItems: 'center', flexShrink: 0 }}>
            <OrganicHalo lobes={6} dip={0.18} size={66} color={palette.primaryContainer} />
            <div style={{ position: 'relative', zIndex: 1 }}>
              <BeaconCanvas
                label={(label || beacon.label || 'B2').trim()}
                subLabel={`${building.trim() || 'U14'} - ${floor.trim() || 'Floor 1'}`}
                labelStyle={labelStyle}
                width={36}
                height={70}
                palette={palette}
                dark={dark}
                holeColor={palette.primaryContainer}
                shadow={false}
              />
            </div>
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ fontSize: 16, fontWeight: 800, color: palette.onSurface }}>
              Save Beacon to Inventory
            </div>
            <div style={{ fontSize: 11.5, color: palette.onSurfaceVariant, marginTop: 2 }}>
              {labelStyle === 'printed' ? 'Live printed-label preview' : 'Live label preview'} · {beacon.mac}
            </div>
          </div>
          <button
            type="button"
            className="m3-icon-btn"
            onClick={onClose}
            aria-label="Close modal"
            style={{ color: palette.onSurfaceVariant }}
          >
            <IconSvg name="close" size={18} color={palette.onSurfaceVariant} />
          </button>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 8 }}>
          <div>
            <div style={{ fontSize: 10.5, fontWeight: 700, color: palette.onSurfaceVariant, marginBottom: 4 }}>
              Label (on 3D Model)
            </div>
            <input
              type="text"
              value={label}
              onChange={(e) => setLabel(e.target.value)}
              placeholder="B2"
              style={inputStyle}
            />
          </div>
          <div>
            <div style={{ fontSize: 10.5, fontWeight: 700, color: palette.onSurfaceVariant, marginBottom: 4 }}>
              Alphanumeric Code
            </div>
            <input
              type="text"
              value={code}
              onChange={(e) => setCode(e.target.value.toUpperCase())}
              placeholder="K4M2"
              style={{ ...inputStyle, fontFamily: "'JetBrains Mono', ui-monospace, monospace" }}
            />
          </div>
          <div>
            <div style={{ fontSize: 10.5, fontWeight: 700, color: palette.onSurfaceVariant, marginBottom: 4 }}>
              Building
            </div>
            <input
              type="text"
              value={building}
              onChange={(e) => setBuilding(e.target.value)}
              placeholder="U14"
              style={inputStyle}
            />
          </div>
          <div>
            <div style={{ fontSize: 10.5, fontWeight: 700, color: palette.onSurfaceVariant, marginBottom: 4 }}>
              Floor
            </div>
            <input
              type="text"
              value={floor}
              onChange={(e) => setFloor(e.target.value)}
              placeholder="Floor 1"
              style={inputStyle}
            />
          </div>
        </div>

        <div>
          <div style={{ fontSize: 10.5, fontWeight: 700, color: palette.onSurfaceVariant, marginBottom: 4 }}>
            Position / Description
          </div>
          <input
            type="text"
            value={positionDescription}
            onChange={(e) => setPositionDescription(e.target.value)}
            placeholder="Near Lab 102 door (2.2 m pillar)"
            style={inputStyle}
          />
        </div>

        <div style={{ display: 'flex', gap: 6, marginTop: 2 }}>
          <button
            type="button"
            onClick={onClose}
            style={{
              flex: 1,
              border: `1px solid ${palette.outlineVariant}`,
              borderRadius: 14,
              padding: '10px 12px',
              backgroundColor: palette.surfaceContainer,
              color: palette.onSurfaceVariant,
              fontWeight: 700,
              fontSize: 13,
              fontFamily: 'inherit',
              cursor: 'pointer',
            }}
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={() => {
              const cleanLabel = label.trim() || beacon.label;
              const cleanCode = code.trim() || beacon.code;
              const cleanBuilding = building.trim() || 'U14';
              const cleanFloor = floor.trim() || 'Floor 1';
              const cleanPos = positionDescription.trim() || beacon.zone;
              onSave({
                ...beacon,
                label: cleanLabel,
                code: cleanCode,
                building: cleanBuilding,
                floor: cleanFloor,
                positionDescription: cleanPos,
                zone: `${cleanBuilding} · ${cleanFloor} · ${cleanPos.split('—')[0].trim()}`,
              });
            }}
            style={{
              flex: 1.5,
              border: 'none',
              borderRadius: 14,
              padding: '10px 12px',
              backgroundColor: palette.primary,
              color: palette.onPrimary,
              fontWeight: 800,
              fontSize: 13,
              fontFamily: 'inherit',
              cursor: 'pointer',
            }}
          >
            Save to Inventory
          </button>
        </div>
      </div>
    </div>
  );
}

/**
 * Renders either List View (Beacon + Label, pkt/s, Power)
 * or Grid View (ONLY Beacon + Label) in one of the 4 MyBicocca layout variants.
 */
function BeaconCollection({
  layoutId,
  viewMode,
  beacons,
  labelStyle,
  palette,
  dark,
  hiddenBeaconId,
  onSelectBeacon,
  registerBeaconRef,
}) {
  if (beacons.length === 0) {
    return (
      <div
        style={{
          padding: 28,
          textAlign: 'center',
          color: palette.onSurfaceVariant,
          backgroundColor: palette.surfaceContainer,
          borderRadius: 20,
          fontSize: 13,
        }}
      >
        No beacons match your filter.
      </div>
    );
  }

  // Group by floor for Layout A (SegmentedDirectory)
  const grouped = useMemo(() => {
    const f1 = beacons.filter((b) => b.floor === 'Floor 1');
    const rest = beacons.filter((b) => b.floor !== 'Floor 1');
    const out = [];
    if (f1.length) out.push({ title: 'U14 · Floor 1', subtitle: 'Labs & Corridor East', items: f1 });
    if (rest.length) out.push({ title: 'U14 · Ground & Floor 2', subtitle: 'Atrium & Hallways', items: rest });
    return out;
  }, [beacons]);

  // GRID VIEW (Strictly ONLY the 3D Beacon Model + Label)
  if (viewMode === 'grid') {
    if (layoutId === 'segmented') {
      return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          {grouped.map((grp) => (
            <div key={grp.title} style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              <div
                style={{
                  backgroundColor: palette.surfaceContainer,
                  borderRadius: '20px 20px 6px 6px',
                  padding: '12px 16px 10px',
                }}
              >
                <div style={{ fontSize: 15, fontWeight: 700, color: palette.onSurface }}>{grp.title}</div>
                <div style={{ fontSize: 11.5, color: palette.onSurfaceVariant }}>{grp.subtitle}</div>
              </div>
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
                  gap: 4,
                }}
              >
                {grp.items.map((b, idx) => {
                  const isLastRow = idx >= grp.items.length - (grp.items.length % 2 === 0 ? 2 : 1);
                  const isLeft = idx % 2 === 0;
                  const rBL = isLastRow && isLeft ? 20 : 6;
                  const rBR = isLastRow && !isLeft ? 20 : 6;
                  return (
                    <button
                      key={b.id}
                      type="button"
                      className="m3-card-btn"
                      onClick={() => onSelectBeacon(b)}
                      style={{
                        backgroundColor: palette.surfaceContainer,
                        borderRadius: `6px 6px ${rBR}px ${rBL}px`,
                        padding: '14px 10px 12px',
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        gap: 8,
                      }}
                    >
                      <div ref={(el) => registerBeaconRef(b.id, el)}>
                        <BeaconCanvas
                          label={b.label}
                          subLabel={`${b.building || 'U14'} - ${b.floor || 'Floor 1'}`}
                          labelStyle={labelStyle}
                          width={64}
                          height={140}
                          palette={palette}
                          dark={dark}
                          holeColor={palette.surfaceContainer}
                          hidden={hiddenBeaconId === b.id}
                        />
                      </div>
                      <span style={{ fontSize: 16, fontWeight: 700, color: palette.onSurface }}>
                        {b.label}
                      </span>
                    </button>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      );
    }

    if (layoutId === 'summary') {
      return (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: 10 }}>
          {beacons.map((b) => (
            <button
              key={b.id}
              type="button"
              className="m3-card-btn m3-press-tight"
              onClick={() => onSelectBeacon(b)}
              style={{
                backgroundColor: palette.surfaceContainer,
                borderRadius: 24,
                overflow: 'hidden',
                display: 'flex',
                flexDirection: 'column',
                padding: 0,
              }}
            >
              <div
                style={{
                  padding: '14px 10px 10px',
                  display: 'flex',
                  justifyContent: 'center',
                  width: '100%',
                }}
              >
                <div ref={(el) => registerBeaconRef(b.id, el)}>
                  <BeaconCanvas
                    label={b.label}
                    subLabel={`${b.building || 'U14'} - ${b.floor || 'Floor 1'}`}
                    labelStyle={labelStyle}
                    width={64}
                    height={140}
                    palette={palette}
                    dark={dark}
                    holeColor={palette.surfaceContainer}
                    hidden={hiddenBeaconId === b.id}
                  />
                </div>
              </div>
              <div
                style={{
                  width: '100%',
                  backgroundColor: palette.surfaceContainerHigh,
                  padding: '9px 12px',
                  textAlign: 'center',
                  fontSize: 15.5,
                  fontWeight: 700,
                  color: palette.onSurface,
                }}
              >
                {b.label}
              </div>
            </button>
          ))}
        </div>
      );
    }

    if (layoutId === 'organic') {
      return (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, minmax(0, 1fr))', gap: 8 }}>
          {beacons.map((b, idx) => {
            const sig = getSignalBadgeStyle(b.frameType);
            const meta = getOrganicCardMeta(idx, 'grid', palette);
            return (
              <button
                key={b.id}
                type="button"
                className="m3-card-btn"
                onClick={() => onSelectBeacon(b)}
                style={{
                  backgroundColor: palette.surfaceContainerLow,
                  border: `1px solid ${palette.outlineVariant}`,
                  borderRadius: 20,
                  padding: '8px 6px 8px',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: 6,
                }}
              >
                <div style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 2px' }}>
                  <span
                    style={{
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: 4,
                      padding: '2px 6px',
                      borderRadius: 999,
                      backgroundColor: sig.bg,
                      color: sig.fg,
                      fontSize: 9.5,
                      fontWeight: 800,
                      letterSpacing: '0.02em',
                    }}
                  >
                    <span style={{ width: 5, height: 5, borderRadius: '50%', backgroundColor: sig.dot }} />
                    {sig.shortLabel}
                  </span>
                  <span style={{ width: 6, height: 6, borderRadius: '50%', backgroundColor: palette.primary }} />
                </div>

                <div style={{ position: 'relative', width: 78, height: 92, display: 'grid', placeItems: 'center' }}>
                  <OrganicHalo
                    lobes={meta.lobes}
                    dip={meta.dip}
                    size={meta.size}
                    color={meta.color}
                    rotation={meta.rotation}
                    hidden={hiddenBeaconId === b.id}
                  />
                  <div ref={(el) => registerBeaconRef(b.id, el)} style={{ position: 'relative', zIndex: 1 }}>
                    <BeaconCanvas
                      label={b.label}
                      subLabel={`${b.building || 'U14'} - ${b.floor || 'Floor 1'}`}
                      labelStyle={labelStyle}
                      width={44}
                      height={88}
                      palette={palette}
                      dark={dark}
                      holeColor={meta.color}
                      hidden={hiddenBeaconId === b.id}
                    />
                  </div>
                </div>

                <div style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 4, padding: '0 2px' }}>
                  <span
                    style={{
                      fontFamily: "'JetBrains Mono', ui-monospace, monospace",
                      fontSize: 11.5,
                      fontWeight: 700,
                      letterSpacing: '0.03em',
                      color: palette.onSurface,
                      whiteSpace: 'nowrap',
                    }}
                  >
                    {b.code}
                  </span>
                  <span
                    style={{
                      fontFamily: "'JetBrains Mono', ui-monospace, monospace",
                      fontSize: 10,
                      fontWeight: 700,
                      color: palette.onSurfaceVariant,
                      whiteSpace: 'nowrap',
                    }}
                  >
                    {b.rssi} dBm
                  </span>
                </div>
              </button>
            );
          })}
        </div>
      );
    }

    // Layout D: Telemetry Matrix (3-column dense Grid: Model + Label only)
    return (
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, minmax(0, 1fr))', gap: 8 }}>
        {beacons.map((b) => (
          <button
            key={b.id}
            type="button"
            className="m3-card-btn"
            onClick={() => onSelectBeacon(b)}
            style={{
              backgroundColor: palette.surfaceContainerLowest,
              border: `1px solid ${palette.outlineVariant}`,
              borderRadius: 18,
              padding: '10px 6px',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: 6,
            }}
          >
            <div ref={(el) => registerBeaconRef(b.id, el)}>
              <BeaconCanvas
                label={b.label}
                subLabel={`${b.building || 'U14'} - ${b.floor || 'Floor 1'}`}
                labelStyle={labelStyle}
                width={50}
                height={112}
                palette={palette}
                dark={dark}
                holeColor={palette.surfaceContainerLowest}
                hidden={hiddenBeaconId === b.id}
              />
            </div>
            <span
              style={{
                fontFamily: "'JetBrains Mono', ui-monospace, monospace",
                fontSize: 13.5,
                fontWeight: 700,
                color: palette.onSurface,
              }}
            >
              {b.label}
            </span>
          </button>
        ))}
      </div>
    );
  }

  // LIST VIEW (3D Beacon Model + Label, Packets/s, Power)
  if (layoutId === 'segmented') {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
        {grouped.map((grp) => (
          <div key={grp.title} style={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
            <div
              style={{
                backgroundColor: palette.surfaceContainer,
                borderRadius: '20px 20px 4px 4px',
                padding: '14px 16px 10px',
              }}
            >
              <div style={{ fontSize: 16, fontWeight: 700, color: palette.onSurface }}>{grp.title}</div>
              <div style={{ fontSize: 12, color: palette.onSurfaceVariant }}>{grp.subtitle}</div>
            </div>
            {grp.items.map((b, idx) => {
              const isLast = idx === grp.items.length - 1;
              return (
                <button
                  key={b.id}
                  type="button"
                  className="m3-card-btn"
                  onClick={() => onSelectBeacon(b)}
                  style={{
                    backgroundColor: palette.surfaceContainer,
                    borderRadius: isLast ? '4px 4px 20px 20px' : '4px',
                    padding: '10px 14px',
                    display: 'flex',
                    alignItems: 'center',
                    gap: 14,
                    textAlign: 'left',
                  }}
                >
                  <div ref={(el) => registerBeaconRef(b.id, el)}>
                    <BeaconCanvas
                      label={b.label}
                      subLabel={`${b.building || 'U14'} - ${b.floor || 'Floor 1'}`}
                      labelStyle={labelStyle}
                      width={38}
                      height={100}
                      palette={palette}
                      dark={dark}
                      holeColor={palette.surfaceContainer}
                      hidden={hiddenBeaconId === b.id}
                    />
                  </div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                      <span style={{ fontSize: 17, fontWeight: 700, color: palette.onSurface }}>{b.label}</span>
                      <span
                        style={{
                          fontSize: 11,
                          fontWeight: 600,
                          color: palette.onPrimaryContainer,
                          backgroundColor: palette.primaryContainer,
                          padding: '2px 8px',
                          borderRadius: 999,
                        }}
                      >
                        {b.rssi} dBm
                      </span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 12, fontSize: 12.5, color: palette.onSurfaceVariant }}>
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                        <IconSvg name="bolt" size={14} color={palette.primary} />
                        <strong>{b.packetsPerSec.toFixed(1)}</strong> pkt/s
                      </span>
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                        <IconSvg name="signal" size={14} color={palette.onSurfaceVariant} />
                        Tx {b.txPower} dBm
                      </span>
                    </div>
                  </div>
                  <IconSvg name="chevron" size={18} color={palette.onSurfaceVariant} />
                </button>
              );
            })}
          </div>
        ))}
      </div>
    );
  }

  if (layoutId === 'summary') {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
        {beacons.map((b) => (
          <button
            key={b.id}
            type="button"
            className="m3-card-btn m3-press-tight"
            onClick={() => onSelectBeacon(b)}
            style={{
              backgroundColor: palette.surfaceContainer,
              borderRadius: 24,
              overflow: 'hidden',
              display: 'flex',
              flexDirection: 'column',
              padding: 0,
              textAlign: 'left',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 14, padding: '12px 16px', width: '100%' }}>
              <div ref={(el) => registerBeaconRef(b.id, el)}>
                <BeaconCanvas
                  label={b.label}
                  subLabel={`${b.building || 'U14'} - ${b.floor || 'Floor 1'}`}
                  labelStyle={labelStyle}
                  width={38}
                  height={102}
                  palette={palette}
                  dark={dark}
                  holeColor={palette.surfaceContainer}
                  hidden={hiddenBeaconId === b.id}
                />
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 8 }}>
                  <span style={{ fontSize: 18, fontWeight: 700, color: palette.onSurface }}>{b.label}</span>
                  <span
                    style={{
                      fontFamily: "'JetBrains Mono', ui-monospace, monospace",
                      fontSize: 12.5,
                      fontWeight: 700,
                      color: palette.primary,
                    }}
                  >
                    {b.rssi} dBm
                  </span>
                </div>
                <div style={{ fontSize: 12.5, color: palette.onSurfaceVariant, marginTop: 2 }}>
                  {b.zone}
                </div>
              </div>
            </div>
            <div
              style={{
                width: '100%',
                backgroundColor: palette.surfaceContainerHigh,
                padding: '9px 16px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                fontSize: 12.5,
                fontWeight: 600,
                color: palette.onSurfaceVariant,
              }}
            >
              <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5 }}>
                <IconSvg name="bolt" size={14} color={palette.onSurfaceVariant} />
                {b.packetsPerSec.toFixed(1)} pkt/s
              </span>
              <span style={{ display: 'inline-flex', alignItems: 'center', gap: 5 }}>
                <IconSvg name="signal" size={14} color={palette.onSurfaceVariant} />
                Power: Tx {b.txPower} dBm
              </span>
            </div>
          </button>
        ))}
      </div>
    );
  }

  if (layoutId === 'organic') {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: 7 }}>
        {beacons.map((b, idx) => (
          <button
            key={b.id}
            type="button"
            className="m3-card-btn"
            onClick={() => onSelectBeacon(b)}
            style={{
              backgroundColor: palette.surfaceContainerLow,
              border: `1px solid ${palette.outlineVariant}`,
              borderRadius: 18,
              padding: '6px 12px',
              display: 'flex',
              alignItems: 'center',
              gap: 10,
              textAlign: 'left',
            }}
          >
            <div style={{ position: 'relative', width: 56, height: 70, display: 'grid', placeItems: 'center', flexShrink: 0 }}>
              {(() => {
                const meta = getOrganicCardMeta(idx, 'list', palette);
                return (
                  <>
                    <OrganicHalo
                      lobes={meta.lobes}
                      dip={meta.dip}
                      size={meta.size}
                      color={meta.color}
                      rotation={meta.rotation}
                      hidden={hiddenBeaconId === b.id}
                    />
                    <div ref={(el) => registerBeaconRef(b.id, el)} style={{ position: 'relative', zIndex: 1 }}>
                      <BeaconCanvas
                        label={b.label}
                        subLabel={`${b.building || 'U14'} - ${b.floor || 'Floor 1'}`}
                        labelStyle={labelStyle}
                        width={34}
                        height={68}
                        palette={palette}
                        dark={dark}
                        holeColor={meta.color}
                        hidden={hiddenBeaconId === b.id}
                      />
                    </div>
                  </>
                );
              })()}
            </div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 2 }}>
                <span style={{ fontSize: 15.5, fontWeight: 800, color: palette.onSurface }}>{b.label}</span>
                <span
                  style={{
                    fontFamily: "'JetBrains Mono', ui-monospace, monospace",
                    fontSize: 10.5,
                    fontWeight: 700,
                    padding: '1px 6px',
                    borderRadius: 6,
                    backgroundColor: palette.surfaceContainerHigh,
                    color: palette.onSurfaceVariant,
                  }}
                >
                  {b.code}
                </span>
                {(() => {
                  const sig = getSignalBadgeStyle(b.frameType);
                  return (
                    <span
                      style={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: 4,
                        fontSize: 10.5,
                        fontWeight: 700,
                        padding: '1px 7px',
                        borderRadius: 999,
                        backgroundColor: sig.bg,
                        color: sig.fg,
                      }}
                    >
                      <span style={{ width: 5, height: 5, borderRadius: '50%', backgroundColor: sig.dot }} />
                      {sig.fullLabel}
                    </span>
                  );
                })()}
              </div>
              <div
                style={{
                  fontSize: 11.5,
                  color: palette.onSurfaceVariant,
                  whiteSpace: 'nowrap',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  marginBottom: 4,
                }}
              >
                {b.zone}
              </div>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 5 }}>
                <span
                  style={{
                    fontSize: 10.5,
                    fontWeight: 700,
                    padding: '2px 7px',
                    borderRadius: 999,
                    backgroundColor: palette.primaryContainer,
                    color: palette.onPrimaryContainer,
                  }}
                >
                  {b.packetsPerSec.toFixed(1)} pkt/s
                </span>
                <span
                  style={{
                    fontSize: 10.5,
                    fontWeight: 700,
                    padding: '2px 7px',
                    borderRadius: 999,
                    backgroundColor: palette.surfaceContainerHigh,
                    color: palette.onSurface,
                  }}
                >
                  Tx {b.txPower} · {b.rssi} dBm
                </span>
              </div>
            </div>
            <IconSvg name="chevron" size={16} color={palette.onSurfaceVariant} />
          </button>
        ))}
      </div>
    );
  }

  // Layout D: Telemetry Hardware Rack
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
      {beacons.map((b) => {
        const bars = b.rssi > -60 ? 4 : b.rssi > -70 ? 3 : b.rssi > -80 ? 2 : 1;
        return (
          <button
            key={b.id}
            type="button"
            className="m3-card-btn"
            onClick={() => onSelectBeacon(b)}
            style={{
              backgroundColor: palette.surfaceContainerLowest,
              border: `1px solid ${palette.outlineVariant}`,
              borderRadius: 18,
              padding: '10px 12px',
              display: 'flex',
              alignItems: 'center',
              gap: 12,
              textAlign: 'left',
            }}
          >
            <div ref={(el) => registerBeaconRef(b.id, el)}>
              <BeaconCanvas
                label={b.label}
                subLabel={`${b.building || 'U14'} - ${b.floor || 'Floor 1'}`}
                labelStyle={labelStyle}
                width={36}
                height={96}
                palette={palette}
                dark={dark}
                holeColor={palette.surfaceContainerLowest}
                hidden={hiddenBeaconId === b.id}
              />
            </div>
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 4 }}>
                <span
                  style={{
                    fontFamily: "'JetBrains Mono', ui-monospace, monospace",
                    fontSize: 16,
                    fontWeight: 700,
                    color: palette.onSurface,
                  }}
                >
                  {b.label}
                </span>
                <div style={{ display: 'flex', alignItems: 'flex-end', gap: 2.5, height: 14 }}>
                  {[1, 2, 3, 4].map((lv) => (
                    <span
                      key={lv}
                      style={{
                        width: 3.5,
                        height: lv * 3.2,
                        borderRadius: 2,
                        backgroundColor: lv <= bars ? palette.primary : palette.outlineVariant,
                      }}
                    />
                  ))}
                </div>
              </div>
              <div style={{ display: 'flex', gap: 10, fontFamily: "'JetBrains Mono', ui-monospace, monospace", fontSize: 11.5, color: palette.onSurfaceVariant }}>
                <span>{b.packetsPerSec.toFixed(1)} pkt/s</span>
                <span>•</span>
                <span>Tx {b.txPower} dBm</span>
                <span>•</span>
                <span style={{ color: palette.primary, fontWeight: 700 }}>{b.rssi} dBm</span>
              </div>
            </div>
          </button>
        );
      })}
    </div>
  );
}

/**
 * Full Phone Experience (List/Grid Screen + Shared-Element 3D Hero Transition + Detail Screen)
 */
function BeaconPhoneExperience({
  layoutId,
  viewMode,
  onViewModeChange,
  labelStyle,
  palette,
  dark,
  beacons,
  selectedBeaconId,
  heroProgress,
  onOpenBeacon,
  onBackFromDetail,
  onSaveBeacon,
  compactScale = 1,
}) {
  const [searchOpen, setSearchOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [floorFilter, setFloorFilter] = useState('All');
  const [favorites, setFavorites] = useState(() => ({ b2: true }));
  const [liveScanning, setLiveScanning] = useState(true);
  const [detailScrollTop, setDetailScrollTop] = useState(0);
  const [detailTab, setDetailTab] = useState('general');
  const [editingBeacon, setEditingBeacon] = useState(null);

  const phoneRef = useRef(null);
  const cardRefs = useRef({});
  const targetRef = useRef(null);
  const capturedFromRect = useRef(null);
  const tabStripRef = useRef(null);

  useEffect(() => {
    setDetailScrollTop(0);
    setDetailTab('general');
    setEditingBeacon(null);
    if (tabStripRef.current) tabStripRef.current.scrollLeft = 0;
  }, [selectedBeaconId]);

  const registerBeaconRef = useCallback((id, el) => {
    if (el) cardRefs.current[id] = el;
  }, []);

  const filteredBeacons = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    return beacons.filter((b) => {
      if (floorFilter !== 'All' && b.floor !== floorFilter) return false;
      if (!q) return true;
      return (
        b.label.toLowerCase().includes(q) ||
        b.zone.toLowerCase().includes(q) ||
        b.frameType.toLowerCase().includes(q) ||
        b.mac.toLowerCase().includes(q)
      );
    });
  }, [beacons, floorFilter, searchQuery]);

  const activeBeacon = useMemo(
    () => beacons.find((b) => b.id === selectedBeaconId) || null,
    [beacons, selectedBeaconId]
  );

  const activeBeaconIdx = useMemo(() => {
    if (!activeBeacon) return 0;
    const idx = filteredBeacons.findIndex((b) => b.id === activeBeacon.id);
    return idx >= 0 ? idx : 0;
  }, [activeBeacon, filteredBeacons]);

  const organicCardMeta = useMemo(
    () => getOrganicCardMeta(activeBeaconIdx, viewMode, palette),
    [activeBeaconIdx, viewMode, palette]
  );

  // Capture source rect whenever heroProgress resets to 0 or activeBeacon changes
  useEffect(() => {
    if (selectedBeaconId && phoneRef.current && cardRefs.current[selectedBeaconId]) {
      const rect = getLocalRect(cardRefs.current[selectedBeaconId], phoneRef.current);
      if (rect) capturedFromRect.current = rect;
    }
  }, [selectedBeaconId, viewMode, layoutId]);

  const handleCardClick = (beacon) => {
    if (phoneRef.current && cardRefs.current[beacon.id]) {
      capturedFromRect.current = getLocalRect(cardRefs.current[beacon.id], phoneRef.current);
    }
    onOpenBeacon(beacon.id);
  };

  const p = clamp01(heroProgress);
  const eased = m3Ease(p);
  const showHeroOverlay = Boolean(activeBeacon && p > 0);
  const showDetailLayer = Boolean(activeBeacon && p > 0.001);

  // Measure target bounding box for the 3D Hero flight (tracks Detail scroll automatically via detailScrollTop re-render)
  const toRect = (targetRef.current && phoneRef.current)
    ? getLocalRect(targetRef.current, phoneRef.current)
    : { left: 55, top: 76 - detailScrollTop, width: 280, height: 190 };

  const fromRect = capturedFromRect.current || (
    (activeBeacon && cardRefs.current[activeBeacon.id] && phoneRef.current)
      ? getLocalRect(cardRefs.current[activeBeacon.id], phoneRef.current)
      : { left: 28, top: 230, width: 44, height: 104 }
  );

  const heroBox = showHeroOverlay && fromRect && toRect ? {
    left: lerp(fromRect.left, toRect.left, eased),
    top: lerp(fromRect.top, toRect.top, eased),
    width: lerp(fromRect.width, toRect.width, eased),
    height: lerp(fromRect.height, toRect.height, eased),
    pitch: lerp(0, -58, eased),
    yaw: lerp(0, -12, eased),
    roll: lerp(0, 56, eased),
    haloSize: lerp(organicCardMeta.size, 190, eased),
    haloColor: mixHex(organicCardMeta.color, palette.primaryContainer, eased),
  } : null;

  return (
    <div
      ref={phoneRef}
      className="phone-screen"
      style={{
        backgroundColor: palette.surface,
        color: palette.onSurface,
        transform: compactScale !== 1 ? `scale(${compactScale})` : undefined,
        transformOrigin: 'top left',
      }}
    >
      {/* Morphing MyBicocca Top Bar */}
      <MyBicoccaTopBar
        navProgress={p}
        searchOpen={searchOpen}
        searchQuery={searchQuery}
        onSearchQueryChange={setSearchQuery}
        onToggleSearch={(open) => {
          setSearchOpen(open);
          if (!open) setSearchQuery('');
        }}
        onBack={onBackFromDetail}
        subPageTitle={activeBeacon ? activeBeacon.code : ''}
        palette={palette}
        onEditBeacon={() => {
          if (activeBeacon) setEditingBeacon(activeBeacon);
        }}
        liveScanning={liveScanning}
        onToggleLiveScan={() => setLiveScanning((v) => !v)}
      />

      {/* Screen Content Container */}
      <div style={{ position: 'relative', flex: 1, minHeight: 0 }}>
        {/* LAYER 1: List / Grid Home Screen */}
        <div
          className="phone-scroll"
          style={{
            position: 'absolute',
            inset: 0,
            padding: '10px 16px 24px',
            opacity: clamp01(1 - p * 1.45),
            transform: `scale(${lerp(1, 0.96, eased)})`,
            pointerEvents: p > 0.25 ? 'none' : 'auto',
          }}
        >
          {/* Header summary + SegmentedSwitch (List vs Grid) */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10, marginBottom: 12 }}>
            <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', padding: '2px 2px 0' }}>
              <div>
                <div style={{ fontSize: 21, fontWeight: 800, letterSpacing: '-0.02em', color: palette.onSurface }}>
                  Nearby Beacons
                </div>
                <div style={{ fontSize: 12, color: palette.onSurfaceVariant }}>
                  Building U14 · {filteredBeacons.length} active transmitters
                </div>
              </div>
              <span
                style={{
                  fontSize: 11,
                  fontWeight: 700,
                  padding: '4px 10px',
                  borderRadius: 999,
                  backgroundColor: palette.surfaceContainerHigh,
                  color: palette.onSurfaceVariant,
                }}
              >
                {viewMode === 'list' ? 'Telemetry' : 'Models'}
              </span>
            </div>

            <SegmentedSwitch
              options={[
                { id: 'list', label: 'List View', icon: 'list' },
                { id: 'grid', label: 'Grid View', icon: 'grid' },
              ]}
              selected={viewMode}
              onSelect={onViewModeChange}
              palette={palette}
            />

            {/* Floor filter chips */}
            <div style={{ display: 'flex', gap: 6, overflowX: 'auto', paddingBottom: 2 }}>
              {['All', 'Floor 1', 'Ground', 'Floor 2'].map((fl) => {
                const active = floorFilter === fl;
                return (
                  <button
                    key={fl}
                    type="button"
                    onClick={() => setFloorFilter(fl)}
                    style={{
                      border: `1px solid ${active ? palette.primary : palette.outlineVariant}`,
                      backgroundColor: active ? palette.primaryContainer : palette.surfaceContainerLowest,
                      color: active ? palette.onPrimaryContainer : palette.onSurfaceVariant,
                      borderRadius: 10,
                      padding: '5px 11px',
                      fontSize: 12,
                      fontWeight: 600,
                      fontFamily: 'inherit',
                      cursor: 'pointer',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    {fl}
                  </button>
                );
              })}
            </div>
          </div>

          <BeaconCollection
            layoutId={layoutId}
            viewMode={viewMode}
            beacons={filteredBeacons}
            labelStyle={labelStyle}
            palette={palette}
            dark={dark}
            hiddenBeaconId={p > 0.001 ? selectedBeaconId : null}
            onSelectBeacon={handleCardClick}
            registerBeaconRef={registerBeaconRef}
          />
        </div>

        {/* LAYER 2: Detail Screen (3/4 3D Hero Stage Target + DetailFactCards) */}
        {showDetailLayer && activeBeacon && (
          <div
            className="phone-scroll"
            onScroll={(e) => setDetailScrollTop(e.currentTarget.scrollTop)}
            style={{
              position: 'absolute',
              inset: 0,
              zIndex: 10,
              backgroundColor: palette.surface,
              opacity: clamp01((p - 0.12) / 0.48),
              pointerEvents: p > 0.65 ? 'auto' : 'none',
              paddingBottom: 28,
            }}
          >
            {/* 3D Hero Stage at top of Detail Screen (Landing Target for persistent Layer 3) */}
            <div
              style={{
                position: 'relative',
                height: 250,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                background: `linear-gradient(180deg, ${palette.surfaceContainer} 0%, ${palette.surface} 100%)`,
                borderBottom: `1px solid ${palette.outlineVariant}`,
                overflow: 'hidden',
              }}
            >
              <div
                ref={targetRef}
                style={{
                  width: 280,
                  height: 190,
                  marginBottom: 16,
                  position: 'relative',
                  zIndex: 1,
                }}
              />

              {/* Top-Left Corner: Building */}
              <div
                style={{
                  position: 'absolute',
                  top: 10,
                  left: 12,
                  fontSize: 11,
                  fontWeight: 800,
                  padding: '3px 10px',
                  borderRadius: 999,
                  backgroundColor: palette.surfaceContainerHigh,
                  border: `1px solid ${palette.outlineVariant}`,
                  color: palette.onSurface,
                  zIndex: 2,
                }}
              >
                {activeBeacon.building || 'U14'}
              </div>

              {/* Top-Right Corner: Floor */}
              <div
                style={{
                  position: 'absolute',
                  top: 10,
                  right: 12,
                  fontSize: 11,
                  fontWeight: 700,
                  padding: '3px 10px',
                  borderRadius: 999,
                  backgroundColor: palette.surfaceContainerHigh,
                  border: `1px solid ${palette.outlineVariant}`,
                  color: palette.onSurfaceVariant,
                  zIndex: 2,
                }}
              >
                {activeBeacon.floor}
              </div>

              {/* Bottom Bar: Colored Signal Type Badges (Left) + Packet Rate, Tx Power, Live RSSI (Right) */}
              <div
                style={{
                  position: 'absolute',
                  bottom: 10,
                  left: 12,
                  right: 12,
                  display: 'flex',
                  alignItems: 'flex-end',
                  justifyContent: 'space-between',
                  gap: 6,
                  zIndex: 2,
                }}
              >
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4 }}>
                  {[
                    activeBeacon.activeSlots?.UID && getSignalBadgeStyle('uid'),
                    activeBeacon.activeSlots?.UUID && getSignalBadgeStyle('ibeacon'),
                    activeBeacon.activeSlots?.TLM && getSignalBadgeStyle('tlm'),
                    activeBeacon.activeSlots?.URL && getSignalBadgeStyle('url'),
                  ].filter(Boolean).map((sig) => (
                    <span
                      key={sig.shortLabel}
                      style={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: 4,
                        fontSize: 9.5,
                        fontWeight: 800,
                        padding: '3px 7px',
                        borderRadius: 999,
                        backgroundColor: sig.bg,
                        color: sig.fg,
                      }}
                    >
                      <span style={{ width: 5, height: 5, borderRadius: '50%', backgroundColor: sig.dot }} />
                      {sig.fullLabel}
                    </span>
                  ))}
                </div>

                <div
                  style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: 5,
                    fontSize: 10,
                    fontWeight: 700,
                    fontFamily: "'JetBrains Mono', ui-monospace, monospace",
                    padding: '3px 8px',
                    borderRadius: 999,
                    backgroundColor: palette.surfaceContainerHigh,
                    border: `1px solid ${palette.outlineVariant}`,
                    color: palette.onSurfaceVariant,
                    whiteSpace: 'nowrap',
                    flexShrink: 0,
                  }}
                >
                  <span style={{ color: palette.onSurface }}>{activeBeacon.packetsPerSec.toFixed(1)} pkt/s</span>
                  <span style={{ opacity: 0.45 }}>·</span>
                  <span>Tx {activeBeacon.txPower}</span>
                  <span style={{ opacity: 0.45 }}>·</span>
                  <span style={{ color: palette.onPrimaryContainer, fontWeight: 800 }}>{activeBeacon.rssi} dBm</span>
                </div>
              </div>
            </div>

            {/* 5 Detail Tabs under the 3D Model */}
            <div
              ref={tabStripRef}
              className="phone-scroll"
              style={{
                position: 'relative',
                display: 'flex',
                gap: 6,
                overflowX: 'auto',
                padding: '10px 16px 2px',
                borderBottom: `1px solid ${palette.outlineVariant}`,
              }}
            >
              {[
                { id: 'general', label: 'General', dot: null },
                { id: 'uid', label: 'Eddystone UID', dot: '#4ADE80' },
                { id: 'ibeacon', label: 'iBeacon (UUID)', dot: '#60A5FA' },
                { id: 'tlm', label: 'Eddystone TLM', dot: '#FB923C' },
                { id: 'url', label: 'Eddystone URL', dot: '#22D3EE' },
              ].map((tb) => {
                const active = detailTab === tb.id;
                return (
                  <button
                    key={tb.id}
                    type="button"
                    data-detail-tab={tb.id}
                    onClick={(e) => {
                      setDetailTab(tb.id);
                      const btn = e.currentTarget;
                      const strip = tabStripRef.current || btn.parentElement;
                      if (strip) {
                        const targetLeft = btn.offsetLeft - (strip.clientWidth - btn.offsetWidth) / 2;
                        strip.scrollTo({ left: Math.max(0, targetLeft), behavior: 'smooth' });
                      }
                    }}
                    style={{
                      border: 'none',
                      borderBottom: `2.5px solid ${active ? palette.primary : 'transparent'}`,
                      background: 'transparent',
                      padding: '6px 8px 9px',
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: 5,
                      fontSize: 12,
                      fontWeight: active ? 800 : 600,
                      color: active ? palette.onSurface : palette.onSurfaceVariant,
                      fontFamily: 'inherit',
                      whiteSpace: 'nowrap',
                      flexShrink: 0,
                      cursor: 'pointer',
                    }}
                  >
                    {tb.dot && (
                      <span style={{ width: 6, height: 6, borderRadius: '50%', backgroundColor: tb.dot }} />
                    )}
                    {tb.label}
                  </button>
                );
              })}
            </div>

            {/* Detail Tab Content */}
            <div
              style={{
                padding: '12px 16px 0',
                display: 'flex',
                flexDirection: 'column',
                gap: 10,
                opacity: clamp01((p - 0.45) / 0.55),
                transform: `translateY(${lerp(26, 0, clamp01((p - 0.35) / 0.65))}px)`,
              }}
            >
              {detailTab === 'general' && (
                <>
                  {/* General Slot & Radio Config Card */}
                  <div
                    style={{
                      backgroundColor: palette.surfaceContainer,
                      border: `1px solid ${palette.outlineVariant}`,
                      borderRadius: 20,
                      padding: 13,
                      display: 'flex',
                      flexDirection: 'column',
                      gap: 9,
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                      <span style={{ fontSize: 10.5, fontWeight: 800, letterSpacing: '0.05em', color: palette.onSurfaceVariant }}>
                        GENERAL
                      </span>
                      <span style={{ fontSize: 10, fontWeight: 700, color: palette.onSurfaceVariant }}>
                        Confidex Viking Slim
                      </span>
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span style={{ fontSize: 12, color: palette.onSurfaceVariant, fontWeight: 600 }}>Power Mode</span>
                      <span
                        style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: 5,
                          fontSize: 11,
                          fontWeight: 800,
                          padding: '2px 9px',
                          borderRadius: 999,
                          backgroundColor: activeBeacon.powerMode === false ? palette.surfaceContainerHigh : '#123C25',
                          color: activeBeacon.powerMode === false ? palette.onSurfaceVariant : '#79E8AA',
                        }}
                      >
                        <span
                          style={{
                            width: 6,
                            height: 6,
                            borderRadius: '50%',
                            backgroundColor: activeBeacon.powerMode === false ? palette.outline : '#4ADE80',
                          }}
                        />
                        {activeBeacon.powerMode === false ? 'OFF' : 'ON'}
                      </span>
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: `1px solid ${palette.outlineVariant}`, paddingTop: 8 }}>
                      <span style={{ fontSize: 12, color: palette.onSurfaceVariant, fontWeight: 600 }}>MAC Address</span>
                      <span style={{ fontSize: 12.5, fontWeight: 700, fontFamily: "'JetBrains Mono', ui-monospace, monospace", color: palette.onSurface }}>
                        {activeBeacon.mac}
                      </span>
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: `1px solid ${palette.outlineVariant}`, paddingTop: 8 }}>
                      <span style={{ fontSize: 12, color: palette.onSurfaceVariant, fontWeight: 600 }}>BLE Tx interval (ms)</span>
                      <span style={{ fontSize: 12.5, fontWeight: 700, fontFamily: "'JetBrains Mono', ui-monospace, monospace", color: palette.onSurface }}>
                        {activeBeacon.txIntervalMs || 200} ms
                      </span>
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: `1px solid ${palette.outlineVariant}`, paddingTop: 8 }}>
                      <span style={{ fontSize: 12, color: palette.onSurfaceVariant, fontWeight: 600 }}>Connectable mode</span>
                      <span style={{ fontSize: 12, fontWeight: 700, color: palette.onSurface }}>
                        {activeBeacon.connectableMode || 'Non-connectable (Locked)'}
                      </span>
                    </div>

                    <div style={{ borderTop: `1px solid ${palette.outlineVariant}`, paddingTop: 8 }}>
                      <div style={{ fontSize: 12, color: palette.onSurfaceVariant, fontWeight: 600, marginBottom: 6 }}>
                        Active slots
                      </div>
                      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, minmax(0, 1fr))', gap: 6 }}>
                        {[
                          { key: 'UID', style: getSignalBadgeStyle('uid') },
                          { key: 'URL', style: getSignalBadgeStyle('url') },
                          { key: 'TLM', style: getSignalBadgeStyle('tlm') },
                          { key: 'UUID', style: getSignalBadgeStyle('ibeacon') },
                        ].map((sl) => {
                          const isOn = Boolean(activeBeacon.activeSlots?.[sl.key]);
                          return (
                            <div
                              key={sl.key}
                              style={{
                                borderRadius: 10,
                                padding: '6px 4px',
                                backgroundColor: isOn ? sl.style.bg : palette.surfaceContainerLow,
                                border: `1px solid ${isOn ? sl.style.dot + '66' : palette.outlineVariant}`,
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                gap: 4,
                                fontSize: 11,
                                fontWeight: 800,
                                color: isOn ? sl.style.fg : palette.onSurfaceVariant,
                                opacity: isOn ? 1 : 0.6,
                              }}
                            >
                              <span
                                style={{
                                  width: 6,
                                  height: 6,
                                  borderRadius: '50%',
                                  backgroundColor: isOn ? sl.style.dot : palette.outline,
                                }}
                              />
                              {sl.key}
                            </div>
                          );
                        })}
                      </div>
                    </div>

                    <button
                      type="button"
                      disabled
                      style={{
                        border: `1px solid ${palette.outlineVariant}`,
                        borderRadius: 12,
                        padding: '9px 10px',
                        backgroundColor: palette.surfaceContainerHigh,
                        color: palette.onSurfaceVariant,
                        fontSize: 11.5,
                        fontWeight: 700,
                        fontFamily: 'inherit',
                        opacity: 0.72,
                        cursor: 'not-allowed',
                      }}
                    >
                      Restore Slot Configurations (Cipher Pending)
                    </button>
                  </div>

                  {/* Hardware IDs & Advertisement */}
                  <DetailFactCard
                    icon="bluetooth"
                    label="Canonical Beacon ID & Median RSSI"
                    value={`${activeBeacon.canonicalId} · med ${activeBeacon.medianRssi} dBm`}
                    mono
                    palette={palette}
                  />
                  <DetailFactCard
                    icon="battery"
                    label="Telemetry (TLM Battery & Temp)"
                    value={`${activeBeacon.batteryMv} mV · ${activeBeacon.tempC.toFixed(1)} °C`}
                    palette={palette}
                  />
                  <DetailFactCard
                    icon="code"
                    label="Raw Advertisement Hex"
                    value={activeBeacon.advHex}
                    mono
                    palette={palette}
                  />

                  {/* Position / Description (Bottom of General Tab) */}
                  <div
                    style={{
                      backgroundColor: palette.surfaceContainer,
                      border: `1px solid ${palette.outlineVariant}`,
                      borderRadius: 20,
                      padding: 13,
                      display: 'flex',
                      flexDirection: 'column',
                      gap: 7,
                    }}
                  >
                    <span style={{ fontSize: 10.5, fontWeight: 800, letterSpacing: '0.05em', color: palette.onSurfaceVariant }}>
                      POSITION / DESCRIPTION
                    </span>
                    <div style={{ fontSize: 13, fontWeight: 600, color: palette.onSurface, lineHeight: 1.4 }}>
                      {activeBeacon.positionDescription || activeBeacon.zone}
                    </div>
                  </div>
                </>
              )}

              {detailTab === 'uid' && (
                <DetailSlotPlaceholder
                  title="Eddystone UID"
                  slotColor="#4ADE80"
                  palette={palette}
                  fields={[
                    { label: 'UID Tx power (dBm)', value: 'Pending cipher reader', pending: true },
                    {
                      label: 'UID Calibrated Tx Power (dBm)',
                      value: activeBeacon.frameType === 'Eddystone-UID' ? `${activeBeacon.txPower} dBm (adv)` : 'Pending cipher reader',
                      pending: activeBeacon.frameType !== 'Eddystone-UID',
                    },
                    {
                      label: 'UID namespace',
                      value: activeBeacon.canonicalId.startsWith('ed:') ? activeBeacon.canonicalId.split(':')[1] : 'Pending cipher reader',
                      mono: true,
                      pending: !activeBeacon.canonicalId.startsWith('ed:'),
                    },
                    {
                      label: 'UID instance',
                      value: activeBeacon.canonicalId.startsWith('ed:') ? activeBeacon.canonicalId.split(':')[2] : 'Pending cipher reader',
                      mono: true,
                      pending: !activeBeacon.canonicalId.startsWith('ed:'),
                    },
                  ]}
                />
              )}

              {detailTab === 'ibeacon' && (
                <DetailSlotPlaceholder
                  title="iBeacon (UUID)"
                  slotColor="#60A5FA"
                  palette={palette}
                  fields={[
                    { label: 'UUID Tx power (dBm)', value: 'Pending cipher reader', pending: true },
                    {
                      label: 'UUID Calibrated Tx power',
                      value: activeBeacon.canonicalId.startsWith('ib:') ? `${activeBeacon.txPower} dBm (adv)` : 'Pending cipher reader',
                      pending: !activeBeacon.canonicalId.startsWith('ib:'),
                    },
                    {
                      label: 'UUID',
                      value: activeBeacon.canonicalId.startsWith('ib:') ? activeBeacon.canonicalId.split(':')[1] : 'Pending cipher reader',
                      mono: true,
                      pending: !activeBeacon.canonicalId.startsWith('ib:'),
                    },
                    {
                      label: 'UUID Major',
                      value: activeBeacon.canonicalId.startsWith('ib:') ? activeBeacon.canonicalId.split(':')[2] : 'Pending cipher reader',
                      mono: true,
                      pending: !activeBeacon.canonicalId.startsWith('ib:'),
                    },
                    {
                      label: 'UUID Minor',
                      value: activeBeacon.canonicalId.startsWith('ib:') ? activeBeacon.canonicalId.split(':')[3] : 'Pending cipher reader',
                      mono: true,
                      pending: !activeBeacon.canonicalId.startsWith('ib:'),
                    },
                  ]}
                />
              )}

              {detailTab === 'tlm' && (
                <DetailSlotPlaceholder
                  title="Eddystone TLM"
                  slotColor="#FB923C"
                  palette={palette}
                  fields={[
                    { label: 'TLM Tx interval factor', value: 'Pending cipher reader', pending: true },
                    { label: 'TLM Tx power (dBm)', value: 'Pending cipher reader', pending: true },
                    { label: 'TLM Calibrated Tx Power (dBm)', value: 'Pending cipher reader', pending: true },
                    {
                      label: 'Proprietary TLM mode',
                      value: activeBeacon.batteryMv ? `Standard TLM (${activeBeacon.batteryMv} mV · ${activeBeacon.tempC.toFixed(1)} °C)` : 'Pending cipher reader',
                      pending: !activeBeacon.batteryMv,
                    },
                  ]}
                />
              )}

              {detailTab === 'url' && (
                <DetailSlotPlaceholder
                  title="Eddystone URL"
                  slotColor="#22D3EE"
                  palette={palette}
                  fields={[
                    { label: 'URL Tx power (dBm)', value: 'Pending cipher reader', pending: true },
                    { label: 'URL', value: 'Pending cipher reader', pending: true },
                    { label: 'URL Calibrated Tx power (dBm)', value: 'Pending cipher reader', pending: true },
                  ]}
                />
              )}
            </div>
          </div>
        )}
      </div>

      {/* Modal Bottom Sheet for Saving / Editing Inventory Metadata */}
      {editingBeacon && (
        <SaveBeaconModalSheet
          beacon={editingBeacon}
          palette={palette}
          labelStyle={labelStyle}
          dark={dark}
          onSave={(updated) => {
            if (onSaveBeacon) onSaveBeacon(updated);
            setEditingBeacon(null);
          }}
          onClose={() => setEditingBeacon(null)}
        />
      )}

      {/* LAYER 3: Persistent Shared 3D Hero Beacon (+ Morphing Organic Halo) for ALL 0 < p <= 1 */}
      {showHeroOverlay && heroBox && activeBeacon && (
        <div
          style={{
            position: 'absolute',
            inset: 0,
            zIndex: p >= 0.9 ? 25 : 45,
            pointerEvents: 'none',
            clipPath: p >= 0.9 ? 'inset(57px 0px 0px 0px)' : 'none',
          }}
        >
          <div
            style={{
              position: 'absolute',
              left: `${heroBox.left}px`,
              top: `${heroBox.top}px`,
              width: `${heroBox.width}px`,
              height: `${heroBox.height}px`,
              pointerEvents: 'none',
            }}
          >
            {layoutId === 'organic' && (
              <MorphingOrganicHalo
                fromLobes={organicCardMeta.lobes}
                fromDip={organicCardMeta.dip}
                fromRot={organicCardMeta.rotation}
                fromColor={organicCardMeta.color}
                toLobes={12}
                toDip={0.10}
                toRot={0}
                toColor={palette.primaryContainer}
                size={heroBox.haloSize}
                t={eased}
              />
            )}
            <div style={{ position: 'relative', zIndex: 1 }}>
              <BeaconCanvas
                label={activeBeacon.label}
                subLabel={`${activeBeacon.building || 'U14'} - ${activeBeacon.floor || 'Floor 1'}`}
                labelStyle={labelStyle}
                pitch={heroBox.pitch}
                yaw={heroBox.yaw}
                roll={heroBox.roll}
                width={Math.max(24, heroBox.width)}
                height={Math.max(32, heroBox.height)}
                fixedBufferW={290}
                fixedBufferH={220}
                palette={palette}
                dark={dark}
                holeColor={layoutId === 'organic' ? heroBox.haloColor : palette.surfaceContainerLow}
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

/**
 * Top-Level Prototype Orchestrator with Tweaks Dock, Hero Transition Scrubber,
 * and Single-Phone vs 4-Layout Comparison Canvas toggle.
 */
function App() {
  const [workspaceMode, setWorkspaceMode] = useState('phone'); // 'phone' | 'canvas'
  const [paletteKey, setPaletteKey] = useState('default');
  const [dark, setDark] = useState(true);
  const [layoutId, setLayoutId] = useState('organic');
  const [viewMode, setViewMode] = useState('list');
  const [labelStyle, setLabelStyle] = useState('printed');
  const [tweaksOpen, setTweaksOpen] = useState(true);

  const [beacons, setBeacons] = useState(INITIAL_BEACONS);
  const [selectedBeaconId, setSelectedBeaconId] = useState(null);
  const [heroProgress, setHeroProgress] = useState(0);
  const [animSpeed, setAnimSpeed] = useState(1);

  const animRef = useRef(null);
  const palette = MYBICOCCA_THEMES[paletteKey][dark ? 'dark' : 'light'];

  // Subtle live campus scan simulation (keeps telemetry feeling alive without distracting)
  useEffect(() => {
    const id = setInterval(() => {
      setBeacons((prev) =>
        prev.map((b, idx) => {
          if (Math.random() > 0.35) return b;
          const delta = Math.round((Math.random() - 0.5) * 3);
          const nextRssi = Math.max(-92, Math.min(-42, b.rssi + delta));
          return { ...b, rssi: nextRssi };
        })
      );
    }, 1800);
    return () => clearInterval(id);
  }, []);

  const animateHeroTo = useCallback((fromVal, toVal, onDone) => {
    if (animRef.current) cancelAnimationFrame(animRef.current);
    const start = performance.now();
    const duration = (540 / animSpeed);

    function step(now) {
      const t = Math.min(1, (now - start) / duration);
      const next = lerp(fromVal, toVal, t);
      setHeroProgress(next);
      if (t < 1) {
        animRef.current = requestAnimationFrame(step);
      } else {
        animRef.current = null;
        if (onDone) onDone();
      }
    }
    animRef.current = requestAnimationFrame(step);
  }, [animSpeed]);

  const handleOpenBeacon = useCallback((id) => {
    setSelectedBeaconId(id);
    animateHeroTo(0, 1);
  }, [animateHeroTo]);

  const handleBackFromDetail = useCallback(() => {
    animateHeroTo(heroProgress, 0, () => {
      setSelectedBeaconId(null);
    });
  }, [animateHeroTo, heroProgress]);

  const handleSaveBeacon = useCallback((updated) => {
    setBeacons((prev) => prev.map((b) => (b.id === updated.id ? updated : b)));
  }, []);

  const handleScrubChange = (val) => {
    if (animRef.current) {
      cancelAnimationFrame(animRef.current);
      animRef.current = null;
    }
    const next = parseFloat(val);
    if (!selectedBeaconId && next > 0) {
      setSelectedBeaconId('b2');
    }
    setHeroProgress(next);
    if (next === 0) {
      setSelectedBeaconId(null);
    }
  };

  const handleReplayHero = () => {
    const targetId = selectedBeaconId || 'b2';
    setSelectedBeaconId(targetId);
    setHeroProgress(0);
    requestAnimationFrame(() => {
      animateHeroTo(0, 1);
    });
  };

  return (
    <div
      className="studio-root"
      style={{
        backgroundColor: dark ? '#110D0E' : '#ECE6E7',
        color: dark ? '#F3E7E9' : '#211A1B',
      }}
    >
      {/* Studio Top Header Strip */}
      <header className="studio-bar" style={{ borderColor: palette.outlineVariant, backgroundColor: palette.surfaceContainerLow, color: palette.onSurface }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{ fontWeight: 800, fontSize: 16, letterSpacing: '-0.02em' }}>
            My<span style={{ color: '#D81648' }}>Bicocca</span> Beacons · M3 Design Exploration
          </div>
          <div className="pill-tabs" style={{ backgroundColor: palette.surfaceContainerHigh }}>
            <button
              type="button"
              onClick={() => setWorkspaceMode('phone')}
              style={{
                backgroundColor: workspaceMode === 'phone' ? palette.primaryContainer : 'transparent',
                color: workspaceMode === 'phone' ? palette.onPrimaryContainer : palette.onSurfaceVariant,
              }}
            >
              Interactive Phone
            </button>
            <button
              type="button"
              onClick={() => setWorkspaceMode('canvas')}
              style={{
                backgroundColor: workspaceMode === 'canvas' ? palette.primaryContainer : 'transparent',
                color: workspaceMode === 'canvas' ? palette.onPrimaryContainer : palette.onSurfaceVariant,
              }}
            >
              Compare All 4 Layouts
            </button>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <a
            href="Beacon%203D%20Model.html"
            className="studio-link-btn"
            style={{ backgroundColor: palette.surfaceContainerHigh, color: palette.onSurface }}
          >
            <IconSvg name="cube" size={16} color={palette.primary} />
            <span>3D Model (GLB/OBJ/Lottie)</span>
          </a>
          <button
            type="button"
            className="studio-toggle-btn"
            onClick={() => setTweaksOpen((o) => !o)}
            style={{
              backgroundColor: tweaksOpen ? palette.primary : palette.surfaceContainerHigh,
              color: tweaksOpen ? palette.onPrimary : palette.onSurface,
            }}
          >
            <IconSvg name="tune" size={16} color={tweaksOpen ? palette.onPrimary : palette.onSurface} />
            <span>{tweaksOpen ? 'Hide Tweaks' : 'Tweaks'}</span>
          </button>
        </div>
      </header>

      {/* Main Stage */}
      <main className="studio-stage">
        {workspaceMode === 'phone' ? (
          <div className="phone-bezel-wrap">
            <div
              className="phone-bezel"
              style={{
                boxShadow: dark
                  ? '0 32px 90px rgba(0,0,0,0.72), 0 0 0 1px rgba(255,255,255,0.08)'
                  : '0 28px 80px rgba(20,10,14,0.22), 0 0 0 1px rgba(0,0,0,0.08)',
              }}
            >
              <BeaconPhoneExperience
                layoutId={layoutId}
                viewMode={viewMode}
                onViewModeChange={setViewMode}
                labelStyle={labelStyle}
                palette={palette}
                dark={dark}
                beacons={beacons}
                selectedBeaconId={selectedBeaconId}
                heroProgress={heroProgress}
                onOpenBeacon={handleOpenBeacon}
                onBackFromDetail={handleBackFromDetail}
                onSaveBeacon={handleSaveBeacon}
              />
            </div>
          </div>
        ) : (
          <div className="comparison-grid">
            {LAYOUT_DESCRIPTIONS.map((desc) => (
              <div key={desc.id} className="comparison-col">
                <div
                  className="comparison-meta"
                  style={{
                    backgroundColor: palette.surfaceContainer,
                    border: `1px solid ${layoutId === desc.id ? palette.primary : palette.outlineVariant}`,
                    color: palette.onSurface,
                  }}
                >
                  <div>
                    <div style={{ fontSize: 13.5, fontWeight: 800 }}>{desc.name}</div>
                    <div style={{ fontSize: 11, color: palette.onSurfaceVariant }}>{desc.source} — {desc.summary}</div>
                  </div>
                  <button
                    type="button"
                    onClick={() => {
                      setLayoutId(desc.id);
                      setWorkspaceMode('phone');
                    }}
                    style={{
                      border: 'none',
                      borderRadius: 999,
                      padding: '5px 10px',
                      fontSize: 11,
                      fontWeight: 700,
                      backgroundColor: palette.primaryContainer,
                      color: palette.onPrimaryContainer,
                      cursor: 'pointer',
                      whiteSpace: 'nowrap',
                    }}
                  >
                    Focus
                  </button>
                </div>
                <div
                  className="phone-bezel-mini"
                  style={{
                    width: 312,
                    height: 675,
                    boxShadow: '0 18px 46px rgba(0,0,0,0.18)',
                  }}
                >
                  <BeaconPhoneExperience
                    layoutId={desc.id}
                    viewMode={viewMode}
                    onViewModeChange={setViewMode}
                    labelStyle={labelStyle}
                    palette={palette}
                    dark={dark}
                    beacons={beacons}
                    selectedBeaconId={selectedBeaconId}
                    heroProgress={heroProgress}
                    onOpenBeacon={handleOpenBeacon}
                    onBackFromDetail={handleBackFromDetail}
                    onSaveBeacon={handleSaveBeacon}
                    compactScale={0.8}
                  />
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      {/* Floating Tweaks & Hero Scrubber Panel */}
      {tweaksOpen && (
        <aside
          className="tweaks-panel"
          style={{
            backgroundColor: palette.surfaceContainerLowest,
            border: `1px solid ${palette.outlineVariant}`,
            color: palette.onSurface,
          }}
        >
          <div className="tweaks-section">
            <div className="tweaks-label" style={{ color: palette.onSurfaceVariant }}>
              MyBicocca Palette &amp; Mode
            </div>
            <div style={{ display: 'flex', gap: 6 }}>
              {[
                { id: 'default', label: 'Red', dot: '#D81648' },
                { id: 'oceano', label: 'Oceano', dot: '#415F91' },
                { id: 'bosco', label: 'Bosco', dot: '#386A20' },
              ].map((th) => {
                const active = paletteKey === th.id;
                return (
                  <button
                    key={th.id}
                    type="button"
                    className="tweak-chip"
                    onClick={() => setPaletteKey(th.id)}
                    style={{
                      flex: 1,
                      backgroundColor: active ? palette.primaryContainer : palette.surfaceContainer,
                      color: active ? palette.onPrimaryContainer : palette.onSurface,
                      borderColor: active ? palette.primary : 'transparent',
                    }}
                  >
                    <span className="swatch" style={{ backgroundColor: th.dot }} />
                    {th.label}
                  </button>
                );
              })}
              <button
                type="button"
                className="tweak-chip"
                onClick={() => setDark((d) => !d)}
                style={{
                  backgroundColor: dark ? palette.primary : palette.surfaceContainerHigh,
                  color: dark ? palette.onPrimary : palette.onSurface,
                }}
              >
                {dark ? '☾ Dark' : '☀ Light'}
              </button>
            </div>
          </div>

          <div className="tweaks-section">
            <div className="tweaks-label" style={{ color: palette.onSurfaceVariant }}>
              Label Visual Style (Q2 = C)
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, minmax(0, 1fr))', gap: 6 }}>
              {[
                { id: 'tape', label: 'Masking Tape' },
                { id: 'printed', label: 'Printed' },
                { id: 'tonal', label: 'Bicocca Band' },
              ].map((st) => {
                const active = labelStyle === st.id;
                return (
                  <button
                    key={st.id}
                    type="button"
                    className="tweak-chip"
                    onClick={() => setLabelStyle(st.id)}
                    style={{
                      backgroundColor: active ? palette.primaryContainer : palette.surfaceContainer,
                      color: active ? palette.onPrimaryContainer : palette.onSurface,
                      borderColor: active ? palette.primary : 'transparent',
                    }}
                  >
                    {st.label}
                  </button>
                );
              })}
            </div>
          </div>

          {workspaceMode === 'phone' && (
            <div className="tweaks-section">
              <div className="tweaks-label" style={{ color: palette.onSurfaceVariant }}>
                MyBicocca Layout Direction
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: 6 }}>
                {LAYOUT_DESCRIPTIONS.map((l) => {
                  const active = layoutId === l.id;
                  return (
                    <button
                      key={l.id}
                      type="button"
                      className="tweak-chip"
                      onClick={() => setLayoutId(l.id)}
                      style={{
                        backgroundColor: active ? palette.primaryContainer : palette.surfaceContainer,
                        color: active ? palette.onPrimaryContainer : palette.onSurface,
                        borderColor: active ? palette.primary : 'transparent',
                      }}
                    >
                      {l.short}
                    </button>
                  );
                })}
              </div>
            </div>
          )}

          {/* Hero Transition Scrubber */}
          <div
            className="tweaks-section"
            style={{
              backgroundColor: palette.surfaceContainer,
              borderRadius: 16,
              padding: 10,
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 6 }}>
              <span className="tweaks-label" style={{ margin: 0, color: palette.onSurface }}>
                3D Hero Transition Scrubber
              </span>
              <span
                style={{
                  fontFamily: "'JetBrains Mono', monospace",
                  fontSize: 11.5,
                  fontWeight: 700,
                  color: palette.primary,
                }}
              >
                {Math.round(heroProgress * 100)}%
              </span>
            </div>
            <input
              type="range"
              min="0"
              max="1"
              step="0.005"
              value={heroProgress}
              onChange={(e) => handleScrubChange(e.target.value)}
              style={{ width: '100%', accentColor: palette.primary, cursor: 'pointer' }}
            />
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: 10.5, color: palette.onSurfaceVariant, marginTop: 2, marginBottom: 8 }}>
              <span>0° Front (Card)</span>
              <span>3/4 Table View (Detail)</span>
            </div>
            <div style={{ display: 'flex', gap: 6 }}>
              <button
                type="button"
                onClick={handleReplayHero}
                style={{
                  flex: 1,
                  border: 'none',
                  borderRadius: 10,
                  padding: '8px 10px',
                  backgroundColor: palette.primary,
                  color: palette.onPrimary,
                  fontWeight: 700,
                  fontSize: 12,
                  fontFamily: 'inherit',
                  cursor: 'pointer',
                }}
              >
                ▶ Replay Hero Turn
              </button>
              <button
                type="button"
                onClick={() => setAnimSpeed((s) => (s === 1 ? 0.35 : 1))}
                style={{
                  border: `1px solid ${palette.outlineVariant}`,
                  borderRadius: 10,
                  padding: '8px 10px',
                  backgroundColor: animSpeed < 1 ? palette.primaryContainer : palette.surfaceContainerLowest,
                  color: animSpeed < 1 ? palette.onPrimaryContainer : palette.onSurface,
                  fontWeight: 700,
                  fontSize: 11.5,
                  fontFamily: 'inherit',
                  cursor: 'pointer',
                }}
              >
                {animSpeed < 1 ? '0.35× Slow' : '1× Speed'}
              </button>
            </div>
          </div>
        </aside>
      )}
    </div>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
