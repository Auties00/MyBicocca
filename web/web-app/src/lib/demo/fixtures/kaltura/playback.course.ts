import { DemoCourses, DemoElearning } from '../../persona.ts';
import { route, text } from '../../router.ts';

/** The length of the open test stream the demo's recordings play, in seconds. */
export const DEMO_LECTURE_SECONDS = 635;

/** The demo has no lectures of its own: every recording plays this open test stream. */
const STREAM = 'https://test-streams.mux.dev/x36xhzz';

const RENDITIONS = [
	[
		'BANDWIDTH=6221600,CODECS="mp4a.40.2,avc1.640028",RESOLUTION=1920x1080',
		'url_8/193039199_mp4_h264_aac_fhd_7.m3u8'
	],
	[
		'BANDWIDTH=2149280,CODECS="mp4a.40.2,avc1.64001f",RESOLUTION=1280x720',
		'url_0/193039199_mp4_h264_aac_hd_7.m3u8'
	],
	[
		'BANDWIDTH=836280,CODECS="mp4a.40.2,avc1.64001f",RESOLUTION=848x480',
		'url_6/193039199_mp4_h264_aac_hq_7.m3u8'
	],
	[
		'BANDWIDTH=460560,CODECS="mp4a.40.5,avc1.420016",RESOLUTION=512x288',
		'url_4/193039199_mp4_h264_aac_7.m3u8'
	]
] as const;

/** The entry Moodle's player page names for a demo recording. */
export function demoEntryId(cmId: number): string {
	return `1_demo${cmId}`;
}

/** The demo's recordings: the second activity of each week of each course. */
function isRecording(entryId: string): boolean {
	return DemoCourses.all.some((course) =>
		(DemoElearning.topics.get(course) ?? []).some(
			(_, week) => demoEntryId(DemoElearning.cmId(course, 10 + week * 10 + 1)) === entryId
		)
	);
}

export const routes = [
	route(
		'GET https://cdnapisec.kaltura.com/p/:partner/sp/:sub/playManifest/entryId/:entryId/protocol/https/format/applehttp/a.m3u8',
		({ params }) => {
			if (!isRecording(params.entryId)) {
				return new Response('<h1>404 Not Found</h1>', {
					status: 404,
					headers: { 'content-type': 'text/html' }
				});
			}
			const lines = RENDITIONS.flatMap(([info, uri]) => [
				`#EXT-X-STREAM-INF:PROGRAM-ID=1,${info}`,
				`${STREAM}/${uri}`
			]);
			return text(['#EXTM3U', ...lines, ''].join('\n'), 'application/vnd.apple.mpegurl');
		}
	)
];
