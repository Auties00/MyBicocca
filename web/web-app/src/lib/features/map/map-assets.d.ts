declare module 'virtual:map-assets' {
	/** Where `vite.config.ts` serves the Android basemap: `/map-assets/<content hash>/`. */
	export const mapAssetsBase: string;
	/** The extent of its vector tiles, at `tiles/{z}/{x}/{y}.pbf` under `mapAssetsBase`. */
	export const mapTiles: {
		minzoom: number;
		maxzoom: number;
		/** `[west, south, east, north]`. */
		bounds: [number, number, number, number];
	};
}
