import { Map as MapLibreMap, setWorkerUrl, type StyleSpecification } from 'maplibre-gl';
import workerUrl from 'maplibre-gl/dist/maplibre-gl-worker.mjs?url';
import 'maplibre-gl/dist/maplibre-gl.css';
import { mapAssetsBase, mapTiles } from 'virtual:map-assets';
import { campus } from './catalog.ts';
import { HIDDEN_LAYERS, paintOps, type MapColors } from './palette.ts';

/*
 * MapLibre, loaded with the map route only. The basemap is Android's: its vector tiles, unpacked
 * from the archive at build time, with the style, glyphs and sprite next to them.
 */

setWorkerUrl(workerUrl);

let style: Promise<StyleSpecification> | undefined;

/** The style with its resources made absolute; the bundled file points at Android assets. */
function loadStyle(): Promise<StyleSpecification> {
	style ??= fetch(`${mapAssetsBase}bicocca_style.json`)
		.then((response) => {
			if (!response.ok) throw new Error(`Map style: HTTP ${response.status}`);
			return response.json() as Promise<StyleSpecification>;
		})
		.catch((error: unknown) => {
			style = undefined;
			throw error;
		});
	return style;
}

function resolvedStyle(base: StyleSpecification, colors: MapColors): StyleSpecification {
	const root = new URL(mapAssetsBase, location.href).href;
	const copy = structuredClone(base);
	copy.glyphs = `${root}glyphs/{fontstack}/{range}.pbf`;
	copy.sprite = `${root}sprite/sprite`;
	for (const [id, source] of Object.entries(copy.sources)) {
		if (source.type !== 'vector') continue;
		// Past the archive's top zoom its tiles are overzoomed; outside its bounds nothing is asked for.
		copy.sources[id] = {
			type: 'vector',
			tiles: [`${root}tiles/{z}/{x}/{y}.pbf`],
			...mapTiles,
			attribution: source.attribution
		};
	}
	const layers = new Map(copy.layers.map((layer) => [layer.id, layer]));
	for (const id of HIDDEN_LAYERS) {
		const layer = layers.get(id);
		if (layer) layer.layout = { ...layer.layout, visibility: 'none' };
	}
	for (const op of paintOps(colors)) {
		const layer = layers.get(op.layer);
		if (layer) Object.assign(layer, { paint: { ...layer.paint, [op.property]: op.value } });
	}
	return copy;
}

export interface Basemap {
	map: MapLibreMap;
	/** Repaints the basemap, for a theme or palette change: no reload, no flash. */
	recolor(colors: MapColors): void;
}

/**
 * The campus map in `container`, painted with `colors` from its first frame. Panning stops at the
 * area the tiles cover; there is no rotation or tilt.
 */
export async function createBasemap(
	container: HTMLElement,
	colors: MapColors,
	label: string
): Promise<Basemap> {
	const map = new MapLibreMap({
		container,
		style: resolvedStyle(await loadStyle(), colors),
		center: campus.center,
		zoom: campus.zoom,
		minZoom: 10,
		maxZoom: 19,
		maxBounds: campus.bounds,
		attributionControl: false,
		dragRotate: false,
		pitchWithRotate: false,
		touchPitch: false,
		renderWorldCopies: false,
		fadeDuration: 150
	});
	map.touchZoomRotate.disableRotation();
	map.keyboard.disableRotation();
	map.getCanvas().setAttribute('aria-label', label);
	return {
		map,
		recolor(next) {
			for (const op of paintOps(next)) {
				if (map.getLayer(op.layer)) map.setPaintProperty(op.layer, op.property, op.value);
			}
		}
	};
}
