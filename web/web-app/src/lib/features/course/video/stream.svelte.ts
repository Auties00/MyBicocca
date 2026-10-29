import type Hls from 'hls.js';
import type { SessionScope } from '#lib/data/session.svelte.ts';
import { parseMaster, writeMaster, type MasterPlaylist } from './hls.ts';

/** Bicocca's Kaltura partner. */
const PARTNER = 2351962;

/** The entry's master playlist on Kaltura's CDN. */
function hlsUrl(entryId: string): string {
	return `https://cdnapisec.kaltura.com/p/${PARTNER}/sp/${PARTNER}00/playManifest/entryId/${encodeURIComponent(entryId)}/protocol/https/format/applehttp/a.m3u8`;
}

/**
 * A frame of the recording, public: no token, no session. The demo's recordings play a public
 * test stream that has no frames on Kaltura, so they have none.
 */
export function thumbnailUrl(
	scope: SessionScope | undefined,
	entryId: string | null | undefined,
	width = 640
): string | undefined {
	if (scope === undefined || __DEMO__ || !entryId) return undefined;
	return `https://cdnapisec.kaltura.com/p/${PARTNER}/thumbnail/entry_id/${encodeURIComponent(entryId)}/width/${width}`;
}

/**
 * The entry's renditions, read fresh on every play: Kaltura signs the variant addresses for
 * about a day. A withdrawn entry answers 404.
 */
export async function loadMaster(scope: SessionScope, entryId: string): Promise<MasterPlaylist> {
	const text = await scope.api.kaltura.playback.getHlsPlaylist(PARTNER, entryId);
	return parseMaster(text, hlsUrl(entryId));
}

/** The browser plays HLS neither natively nor through Media Source Extensions. */
export class UnsupportedPlayback extends Error {
	constructor() {
		super('This browser plays no HLS');
		this.name = 'UnsupportedPlayback';
	}
}

/** A fatal error hls.js gave up on. */
export class PlaybackFailure extends Error {
	constructor(detail: string) {
		super(`HLS playback failed: ${detail}`);
		this.name = 'PlaybackFailure';
	}
}

/**
 * A stream playing in a `<video>`: natively where the browser plays HLS, else through hls.js,
 * loaded on demand (Firefox). The master is played from a blob, so the demo's and the live
 * playlist take the same path; a native player that refuses blobs (Safari) falls back to
 * Kaltura's own address. A quality is pinned by playing that rendition alone (natively) or by
 * locking hls.js on its level.
 */
export class StreamSource {
	/** -1 for automatic, else the index of the pinned rendition (highest bitrate first). */
	quality = $state(-1);
	readonly master: MasterPlaylist;

	readonly #video: HTMLVideoElement;
	readonly #blob: string;
	readonly #fallback: string;
	#hls: Hls | undefined;

	private constructor(video: HTMLVideoElement, master: MasterPlaylist, fallback: string) {
		this.#video = video;
		this.master = master;
		this.#fallback = fallback;
		this.#blob = URL.createObjectURL(
			new Blob([writeMaster(master, master.renditions)], { type: 'application/vnd.apple.mpegurl' })
		);
	}

	/**
	 * Plays `master` in `video`. Rejects with `UnsupportedPlayback` where no HLS plays, with the
	 * signal's reason once it aborts before the video is touched; `onfail` hears of a failure
	 * hls.js can't recover from.
	 */
	static async attach(
		video: HTMLVideoElement,
		master: MasterPlaylist,
		entryId: string,
		{ signal, onfail }: { signal: AbortSignal; onfail: (error: PlaybackFailure) => void }
	): Promise<StreamSource> {
		if (video.canPlayType('application/vnd.apple.mpegurl') !== '') {
			const source = new StreamSource(video, master, hlsUrl(entryId));
			source.#play(source.#blob);
			return source;
		}
		const { default: Hls } = await import('hls.js');
		signal.throwIfAborted();
		if (!Hls.isSupported()) throw new UnsupportedPlayback();
		const source = new StreamSource(video, master, hlsUrl(entryId));
		const hls = new Hls();
		let recovered = false;
		hls.on(Hls.Events.ERROR, (_event, data) => {
			if (!data.fatal) return;
			if (data.type === Hls.ErrorTypes.MEDIA_ERROR && !recovered) {
				recovered = true;
				hls.recoverMediaError();
			} else onfail(new PlaybackFailure(data.details));
		});
		hls.loadSource(source.#blob);
		hls.attachMedia(video);
		source.#hls = hls;
		return source;
	}

	#play(url: string): void {
		const video = this.#video;
		if (url === this.#blob) {
			const refused = () => {
				if (video.src === this.#blob && video.readyState === 0) video.src = this.#fallback;
			};
			video.addEventListener('error', refused, { once: true });
			video.addEventListener('loadedmetadata', () => video.removeEventListener('error', refused), {
				once: true
			});
		}
		video.src = url;
	}

	/** Pins rendition `index`, or goes back to automatic with -1, keeping the place. */
	select(index: number): void {
		if (index === this.quality) return;
		this.quality = index;
		const hls = this.#hls;
		if (hls !== undefined) {
			const bandwidth = this.master.renditions[index]?.bandwidth;
			hls.currentLevel =
				index < 0 ? -1 : hls.levels.findIndex((level) => level.bitrate === bandwidth);
			return;
		}
		const video = this.#video;
		const { currentTime, paused, playbackRate } = video;
		video.addEventListener(
			'loadedmetadata',
			() => {
				video.currentTime = currentTime;
				video.playbackRate = playbackRate;
				if (!paused) void video.play().catch(() => undefined);
			},
			{ once: true }
		);
		this.#play(index < 0 ? this.#blob : this.master.renditions[index].uri);
	}

	destroy(): void {
		this.#hls?.destroy();
		this.#video.removeAttribute('src');
		this.#video.load();
		URL.revokeObjectURL(this.#blob);
	}
}
