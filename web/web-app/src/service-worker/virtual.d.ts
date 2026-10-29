declare module 'virtual:map-assets' {
	/** Where `vite.config.ts` serves the Android basemap: `/map-assets/<content hash>/`. */
	export const mapAssetsBase: string;
	/** The basemap's files short of its tiles (style, glyphs, sprite), relative to `mapAssetsBase`. */
	export const mapAssetFiles: string[];
}

declare module 'virtual:precache' {
	/** The build's files (relative to the base path) that every page starts from. */
	export const precache: string[];
}
