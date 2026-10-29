import { describe, expect, it } from 'vitest';
import { parseMaster, renditionLabel, writeMaster } from './hls.ts';

const BASE =
	'https://cdnapisec.kaltura.com/p/2351962/sp/235196200/playManifest/entryId/1_x/protocol/https/format/applehttp/a.m3u8';

const MASTER = `#EXTM3U
#EXT-X-VERSION:4
#EXT-X-MEDIA:TYPE=AUDIO,GROUP-ID="aud",URI="audio/index.m3u8"
#EXT-X-STREAM-INF:BANDWIDTH=460560,RESOLUTION=512x288,CODECS="mp4a.40.5,avc1.420016"
https://cfvod.kaltura.com/hls/p/2351962/flavorId/1_low/index.m3u8?Policy=a&Signature=b

#EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=2149280,RESOLUTION=1280x720,FRAME-RATE=25.000
hd/index.m3u8
#EXT-X-STREAM-INF:BANDWIDTH=96000
#EXT-X-STREAM-INF:BANDWIDTH=64000,CODECS="mp4a.40.2"
audio-only.m3u8
`;

describe('parseMaster', () => {
	const master = parseMaster(MASTER, BASE);

	it('lists renditions highest bitrate first, with their size, rate and flavor', () => {
		expect(
			master.renditions.map(({ bandwidth, height, frameRate, flavorId }) => ({
				bandwidth,
				height,
				frameRate,
				flavorId
			}))
		).toEqual([
			{ bandwidth: 2149280, height: 720, frameRate: 25, flavorId: null },
			{ bandwidth: 460560, height: 288, frameRate: null, flavorId: '1_low' },
			{ bandwidth: 64000, height: null, frameRate: null, flavorId: null }
		]);
	});

	it('makes every address absolute, so the playlist plays from a blob', () => {
		expect(master.renditions[0].uri).toBe(
			'https://cdnapisec.kaltura.com/p/2351962/sp/235196200/playManifest/entryId/1_x/protocol/https/format/applehttp/hd/index.m3u8'
		);
		expect(master.header[2]).toContain(
			'URI="https://cdnapisec.kaltura.com/p/2351962/sp/235196200/playManifest/entryId/1_x/protocol/https/format/applehttp/audio/index.m3u8"'
		);
	});

	it('writes a playlist back with the chosen renditions only', () => {
		const pinned = writeMaster(master, [master.renditions[1]]).split('\n');
		expect(pinned.filter((line) => line.startsWith('#EXT-X-STREAM-INF'))).toHaveLength(1);
		expect(pinned).toContain(master.renditions[1].uri);
		expect(pinned[0]).toBe('#EXTM3U');
	});

	it('labels a rendition by its height, else its flavor or bitrate', () => {
		expect(master.renditions.map(renditionLabel)).toEqual(['720p', '288p', '64k']);
	});
});
