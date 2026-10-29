/**
 * Unpacks a PMTiles archive into plain `z/x/y` tiles, for hosts that can't answer byte ranges.
 * Used by the map assets plugin in `vite.config.ts`, at build time and in the dev servers.
 */
import { readFile } from 'node:fs/promises';
import { PMTiles, TileType, type Source } from 'pmtiles';

export interface BasemapTiles {
	minzoom: number;
	maxzoom: number;
	/** The area the archive covers: `[west, south, east, north]`. */
	bounds: [number, number, number, number];
	/** Each tile's decompressed contents, by `z/x/y`. */
	tiles: Map<string, Uint8Array>;
}

const column = (lon: number, z: number) => Math.floor(((lon + 180) / 360) * 2 ** z);

function row(lat: number, z: number): number {
	const radians = (lat * Math.PI) / 180;
	const mercator = Math.log(Math.tan(radians) + 1 / Math.cos(radians));
	return Math.floor(((1 - mercator / Math.PI) / 2) * 2 ** z);
}

export async function unpackBasemap(file: string): Promise<BasemapTiles> {
	const bytes = await readFile(file);
	const source: Source = {
		getKey: () => file,
		getBytes: async (offset, length) => ({
			data: bytes.buffer.slice(bytes.byteOffset + offset, bytes.byteOffset + offset + length)
		})
	};
	const archive = new PMTiles(source);
	const header = await archive.getHeader();
	if (header.tileType !== TileType.Mvt) throw new Error(`${file}: not a vector tile archive`);
	const tiles = new Map<string, Uint8Array>();
	// The header's bounds hold every tile, so a walk over them finds them all.
	for (let z = header.minZoom; z <= header.maxZoom; z++) {
		const [west, east] = [column(header.minLon, z), column(header.maxLon, z)];
		const [north, south] = [row(header.maxLat, z), row(header.minLat, z)];
		for (let x = west; x <= east; x++) {
			for (let y = north; y <= south; y++) {
				const tile = await archive.getZxy(z, x, y);
				if (tile) tiles.set(`${z}/${x}/${y}`, new Uint8Array(tile.data));
			}
		}
	}
	if (tiles.size !== header.numAddressedTiles) {
		throw new Error(
			`${file}: found ${tiles.size} of ${header.numAddressedTiles} tiles inside its bounds`
		);
	}
	return {
		minzoom: header.minZoom,
		maxzoom: header.maxZoom,
		bounds: [header.minLon, header.minLat, header.maxLon, header.maxLat],
		tiles
	};
}
