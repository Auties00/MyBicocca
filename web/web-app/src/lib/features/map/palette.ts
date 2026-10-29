export type MapRole =
	| 'ground'
	| 'urban'
	| 'park'
	| 'water'
	| 'building'
	| 'buildingOutline'
	| 'road'
	| 'roadCasing'
	| 'highway'
	| 'highwayCasing'
	| 'label'
	| 'roadLabel'
	| 'halo';

export type MapColors = Record<MapRole, string>;

/**
 * Each role as Carta tokens: a paper basemap tinted with the palette's hue, so it follows light,
 * dark and every palette. Resolved in the page by {@link resolveColors}. Mixes are in OKLab, so a
 * tint fades toward grey instead of turning through the hues in between. Dark keeps the ground
 * near-neutral, a step apart per role, so the pins stay the only saturated marks.
 */
const ROLE_TOKENS: Record<MapRole, string> = {
	ground: 'var(--bg)',
	urban: 'color-mix(in oklab, var(--g3) 55%, var(--bg))',
	park: 'light-dark(color-mix(in oklab, var(--gold-container) 40%, var(--g3)), color-mix(in oklab, var(--gold-container) 15%, var(--g2)))',
	water: 'light-dark(var(--c4), color-mix(in oklab, var(--c3) 40%, var(--g2)))',
	building:
		'light-dark(color-mix(in oklab, var(--c3) 55%, var(--bg)), color-mix(in oklab, var(--c3) 20%, var(--g3)))',
	buildingOutline:
		'light-dark(color-mix(in oklab, var(--c5) 55%, var(--bg)), color-mix(in oklab, var(--c4) 20%, var(--g5)))',
	road: 'light-dark(var(--surface), color-mix(in oklab, var(--g3) 70%, var(--bg)))',
	roadCasing:
		'light-dark(color-mix(in oklab, var(--c4) 70%, var(--bg)), color-mix(in oklab, var(--c3) 30%, var(--g3)))',
	highway: 'light-dark(var(--c4), color-mix(in oklab, var(--c5) 60%, var(--g3)))',
	highwayCasing: 'light-dark(var(--c5), var(--c4))',
	label: 'color-mix(in oklab, var(--c12) 30%, var(--text-muted))',
	roadLabel: 'color-mix(in oklab, var(--c11) 20%, var(--text-subtle))',
	halo: 'var(--bg)'
};

/**
 * Protomaps basemap (v5) layer ids by the role they take, as Android's `MapStyle.kt` groups them.
 * Layers left out keep the style file's own colours.
 */
const FILLS: ReadonlyArray<readonly [MapRole, readonly string[]]> = [
	['ground', ['earth']],
	['park', ['landcover', 'landuse_park', 'landuse_urban_green', 'landuse_zoo', 'landuse_beach']],
	[
		'urban',
		[
			'landuse_hospital',
			'landuse_industrial',
			'landuse_school',
			'landuse_aerodrome',
			'landuse_runway',
			'landuse_pedestrian',
			'landuse_pier'
		]
	],
	['water', ['water']]
];

const LINES: ReadonlyArray<readonly [MapRole, readonly string[]]> = [
	['water', ['water_stream', 'water_river']],
	[
		'roadCasing',
		[
			'roads_tunnels_other_casing',
			'roads_tunnels_minor_casing',
			'roads_tunnels_link_casing',
			'roads_tunnels_major_casing',
			'roads_minor_service_casing',
			'roads_minor_casing',
			'roads_link_casing',
			'roads_major_casing_late',
			'roads_major_casing_early',
			'roads_bridges_other_casing',
			'roads_bridges_link_casing',
			'roads_bridges_minor_casing',
			'roads_bridges_major_casing'
		]
	],
	[
		'road',
		[
			'roads_pier',
			'roads_other',
			'roads_link',
			'roads_minor_service',
			'roads_minor',
			'roads_major',
			'roads_tunnels_other',
			'roads_tunnels_minor',
			'roads_tunnels_link',
			'roads_tunnels_major',
			'roads_bridges_other',
			'roads_bridges_minor',
			'roads_bridges_link',
			'roads_bridges_major'
		]
	],
	['highway', ['roads_highway', 'roads_tunnels_highway', 'roads_bridges_highway']],
	[
		'highwayCasing',
		[
			'roads_highway_casing_late',
			'roads_highway_casing_early',
			'roads_tunnels_highway_casing',
			'roads_bridges_highway_casing'
		]
	]
];

const LABELS: ReadonlyArray<readonly [MapRole, readonly string[]]> = [
	[
		'roadLabel',
		[
			'roads_labels_minor',
			'roads_labels_major',
			'address_label',
			'water_waterway_label',
			'water_label_ocean',
			'water_label_lakes',
			'earth_label_islands'
		]
	],
	['label', ['places_subplace', 'places_region', 'places_locality', 'places_country']]
];

/**
 * Layers the campus map never shows: points of interest crowd the pins, rail lines and one-way
 * arrows the streets people walk.
 */
export const HIDDEN_LAYERS = ['pois', 'roads_rail', 'roads_oneway'];

export interface PaintOp {
	layer: string;
	property:
		| 'background-color'
		| 'fill-color'
		| 'fill-outline-color'
		| 'line-color'
		| 'text-color'
		| 'text-halo-color';
	value: string;
}

/** Every paint property `colors` sets, layer by layer. */
export function paintOps(colors: MapColors): PaintOp[] {
	const ops: PaintOp[] = [
		{ layer: 'background', property: 'background-color', value: colors.ground }
	];
	for (const [role, layers] of FILLS) {
		for (const layer of layers) ops.push({ layer, property: 'fill-color', value: colors[role] });
	}
	ops.push(
		{ layer: 'buildings', property: 'fill-color', value: colors.building },
		{ layer: 'buildings', property: 'fill-outline-color', value: colors.buildingOutline }
	);
	for (const [role, layers] of LINES) {
		for (const layer of layers) ops.push({ layer, property: 'line-color', value: colors[role] });
	}
	for (const [role, layers] of LABELS) {
		for (const layer of layers) {
			ops.push(
				{ layer, property: 'text-color', value: colors[role] },
				{ layer, property: 'text-halo-color', value: colors.halo }
			);
		}
	}
	return ops;
}

/**
 * The roles' colours as `rgba()` for MapLibre, read from the tokens where `host` sits, so its
 * theme and palette apply. A canvas converts whatever space the browser computes in to sRGB.
 */
export function resolveColors(host: HTMLElement): MapColors {
	const probe = document.createElement('span');
	probe.hidden = true;
	host.append(probe);
	const context = document.createElement('canvas').getContext('2d', { willReadFrequently: true });
	const colors = {} as MapColors;
	for (const [role, expression] of Object.entries(ROLE_TOKENS) as Array<[MapRole, string]>) {
		probe.style.color = expression;
		const computed = getComputedStyle(probe).color;
		colors[role] = context ? toRgba(context, computed) : computed;
	}
	probe.remove();
	return colors;
}

function toRgba(context: CanvasRenderingContext2D, color: string): string {
	context.clearRect(0, 0, 1, 1);
	context.fillStyle = color;
	context.fillRect(0, 0, 1, 1);
	const [r, g, b, a] = context.getImageData(0, 0, 1, 1).data;
	return `rgba(${r}, ${g}, ${b}, ${Math.round((a / 255) * 1000) / 1000})`;
}
