/** One rendition of an HLS master playlist. */
export interface Rendition {
	/** `EXT-X-STREAM-INF` line, as written. */
	info: string;
	/** The variant playlist, absolute. */
	uri: string;
	bandwidth: number;
	width: number | null;
	height: number | null;
	frameRate: number | null;
	/** Kaltura's flavor, when the address names one. */
	flavorId: string | null;
}

export interface MasterPlaylist {
	/** The lines before the first rendition (`#EXTM3U`, version, media groups), URIs absolute. */
	header: string[];
	/** Highest bitrate first. */
	renditions: Rendition[];
}

function attribute(info: string, name: string): string | undefined {
	const match = new RegExp(`(?:^|[:,])${name}=("[^"]*"|[^,]*)`).exec(info);
	return match?.[1].replace(/^"|"$/g, '');
}

/**
 * Reads a master playlist; relative addresses resolve against `base`, so the playlist can be
 * played again from anywhere (a blob URL, with one rendition pinned).
 */
export function parseMaster(text: string, base: string): MasterPlaylist {
	const lines = text.split(/\r?\n/).map((line) => line.trim());
	const header: string[] = [];
	const renditions: Rendition[] = [];
	for (let index = 0; index < lines.length; index++) {
		const line = lines[index];
		if (!line.startsWith('#EXT-X-STREAM-INF:')) {
			if (renditions.length === 0 && line !== '') {
				header.push(
					line.replace(/URI="([^"]+)"/, (_, uri: string) => `URI="${new URL(uri, base).href}"`)
				);
			}
			continue;
		}
		const next = lines.findIndex((candidate, at) => at > index && candidate !== '');
		if (next < 0 || lines[next].startsWith('#')) continue;
		const uri = lines[next];
		index = next;
		const resolution = /^(\d+)x(\d+)$/.exec(attribute(line, 'RESOLUTION') ?? '');
		const frameRate = Number.parseFloat(attribute(line, 'FRAME-RATE') ?? '');
		renditions.push({
			info: line,
			uri: new URL(uri, base).href,
			bandwidth: Number.parseInt(attribute(line, 'BANDWIDTH') ?? '0', 10) || 0,
			width: resolution ? Number(resolution[1]) : null,
			height: resolution ? Number(resolution[2]) : null,
			frameRate: Number.isFinite(frameRate) ? frameRate : null,
			flavorId: /\/flavorId\/([^/]+)\//.exec(uri)?.[1] ?? null
		});
	}
	renditions.sort((a, b) => b.bandwidth - a.bandwidth);
	return { header, renditions };
}

/** The master playlist again, with only `renditions`. */
export function writeMaster(master: MasterPlaylist, renditions: readonly Rendition[]): string {
	const body = renditions.flatMap((rendition) => [rendition.info, rendition.uri]);
	return [...master.header, ...body, ''].join('\n');
}

/** A rendition's label: its height ("720p"), else its flavor. */
export function renditionLabel(rendition: Rendition): string {
	if (rendition.height !== null) return `${rendition.height}p`;
	return rendition.flavorId ?? `${Math.round(rendition.bandwidth / 1000)}k`;
}
