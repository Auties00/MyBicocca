<script lang="ts">
	import { untrack } from 'svelte';
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconArrowCounterClockwise from '~icons/ph/arrow-counter-clockwise';
	import IconCornersIn from '~icons/ph/corners-in';
	import IconCornersOut from '~icons/ph/corners-out';
	import IconGearSix from '~icons/ph/gear-six';
	import IconPause from '~icons/ph/pause-fill';
	import IconPictureInPicture from '~icons/ph/picture-in-picture';
	import IconPlay from '~icons/ph/play-fill';
	import IconSkipBack from '~icons/ph/skip-back-fill';
	import IconSkipForward from '~icons/ph/skip-forward-fill';
	import IconSpeakerHigh from '~icons/ph/speaker-high';
	import IconSpeakerSlash from '~icons/ph/speaker-slash';
	import IconVideoCamera from '~icons/ph/video-camera-slash-duotone';
	import { toDataError, type DataError } from '#lib/data/errors.ts';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { formatNumber } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import Menu, { type MenuEntry } from '#lib/ui/Menu.svelte';
	import { isTypingTarget } from '#lib/ui/platform.ts';
	import { registerShortcuts } from '#lib/ui/shortcuts.svelte.ts';
	import Spinner from '#lib/ui/Spinner.svelte';
	import { formatClock, formatClockLong } from '../../format.ts';
	import { kalturaEntryQuery } from '../../queries.ts';
	import { markWatched } from '../../video/completion.ts';
	import { renditionLabel } from '../../video/hls.ts';
	import { saveProgress, type VideoProgress } from '../../video/progress.ts';
	import {
		loadMaster,
		StreamSource,
		thumbnailUrl,
		UnsupportedPlayback
	} from '../../video/stream.svelte.ts';

	interface Props {
		courseId: number;
		cmId: number;
		title: string;
		sectionTitle: string | null;
		courseTitle: string;
		/** Moodle takes "watched" from here only when the student ticks the activity by hand. */
		manual: boolean;
		/** Ticked in Moodle already. */
		completed: boolean;
		/** Where the student got to last time. */
		saved: VideoProgress | undefined;
		/** Whether `saved` is known: playing before would start over and save that. */
		ready: boolean;
		onprevious?: () => void;
		onnext?: () => void;
		/** The recording reached its end. */
		onended: () => void;
	}

	let {
		courseId,
		cmId,
		title,
		sectionTitle,
		courseTitle,
		manual,
		completed,
		saved,
		ready,
		onprevious,
		onnext,
		onended
	}: Props = $props();

	const SPEEDS = [0.75, 1, 1.25, 1.5, 1.75, 2];
	const SAVE_EVERY_MS = 5000;
	const HIDE_AFTER_MS = 3000;
	const SKIP_SECONDS = 10;

	const entry = query(() => {
		const scope = session.current;
		return scope && kalturaEntryQuery(scope, cmId);
	});

	let video = $state<HTMLVideoElement>();
	let stage = $state<HTMLElement>();
	let source = $state.raw<StreamSource>();
	let phase = $state<'loading' | 'ready' | 'failed'>('loading');
	let failure = $state<DataError>();
	let unsupported = $state(false);
	let attempt = $state(0);

	let paused = $state(true);
	let currentTime = $state(0);
	let duration = $state(0);
	let muted = $state(false);
	let playbackRate = $state(1);
	let buffered = $state<{ start: number; end: number }[]>([]);
	let waiting = $state(false);
	let fullscreen = $state(false);
	let shown = $state(true);
	let menuOpen = $state(false);

	const unavailable = $derived(failure?.kind === 'not_found');
	const loadedTo = $derived(buffered.find((range) => range.start <= currentTime + 1)?.end ?? 0);
	const touch = typeof matchMedia !== 'undefined' && matchMedia('(pointer: coarse)').matches;
	const pip = typeof document !== 'undefined' && document.pictureInPictureEnabled === true;

	/** The recording being played, as it was when it started: saves after a switch use it. */
	interface Recording {
		cmId: number;
		courseId: number;
		title: string;
		sectionTitle: string | null;
		courseTitle: string;
		manual: boolean;
		completed: boolean;
		entryId: string;
		resumeAt: number;
	}

	let active: Recording | undefined;
	let resumed = false;

	/* Entry, master playlist, then the stream; a new recording or a retry starts over. */
	$effect(() => {
		const element = video;
		const scope = session.current;
		const entryId = entry.data;
		void attempt;
		if (entry.error && entryId === undefined) {
			phase = 'failed';
			failure = entry.error;
			return;
		}
		if (!ready || element === undefined || scope === undefined || entryId === undefined) return;
		const recording: Recording = untrack(() => ({
			cmId,
			courseId,
			title,
			sectionTitle,
			courseTitle,
			manual,
			completed,
			entryId,
			resumeAt: saved && !saved.completed ? saved.position : 0
		}));
		const controller = new AbortController();
		const { signal } = controller;
		let attached: StreamSource | undefined;
		active = recording;
		resumed = false;
		phase = 'loading';
		failure = undefined;
		loadMaster(scope, entryId)
			.then((master) => {
				signal.throwIfAborted();
				return StreamSource.attach(element, master, entryId, { signal, onfail: fail });
			})
			.then((created) => {
				attached = created;
				source = created;
			})
			.catch((error: unknown) => !signal.aborted && fail(error));
		return () => {
			controller.abort();
			untrack(() => save(recording));
			active = undefined;
			attached?.destroy();
			source = undefined;
		};
	});

	function fail(error: unknown) {
		phase = 'failed';
		unsupported = error instanceof UnsupportedPlayback;
		failure = toDataError(error);
	}

	function retry() {
		if (entry.error && entry.data === undefined) void entry.refresh();
		attempt++;
	}

	/* Picks up where the student left off, unless the recording was watched to the end. */
	function loaded() {
		phase = 'ready';
		if (resumed || video === undefined || active === undefined) return;
		resumed = true;
		const at = active.resumeAt;
		if (at > 5 && (duration === 0 || at < duration - 5)) video.currentTime = at;
		void video.play().catch(() => (paused = true));
	}

	let lastSave = 0;
	function save(recording = active, ended = false) {
		const scope = session.current;
		if (scope === undefined || recording === undefined || (currentTime <= 0 && !ended)) return;
		lastSave = Date.now();
		const length = Number.isFinite(duration) ? duration : 0;
		void saveProgress(scope, {
			cmId: recording.cmId,
			courseId: recording.courseId,
			title: recording.title,
			sectionTitle: recording.sectionTitle,
			courseTitle: recording.courseTitle,
			entryId: recording.entryId,
			position: ended ? length : currentTime,
			duration: length,
			completed: ended || undefined
		}).then(({ previous, saved: now }) => {
			if (
				now.completed &&
				previous?.completed !== true &&
				recording.manual &&
				!recording.completed
			) {
				markWatched(scope, recording.courseId, recording.cmId).catch(() => undefined);
			}
		});
	}

	function timeupdate() {
		if (!paused && Date.now() - lastSave > SAVE_EVERY_MS) save();
	}

	function ended() {
		save(active, true);
		onended();
	}

	function toggle() {
		if (video === undefined || phase !== 'ready') return;
		if (video.paused) void video.play().catch(() => undefined);
		else video.pause();
	}

	function seekBy(seconds: number) {
		if (video === undefined) return;
		video.currentTime = Math.max(0, Math.min(duration || Infinity, video.currentTime + seconds));
		wake();
	}

	async function toggleFullscreen() {
		if (stage === undefined) return;
		if (document.fullscreenElement) {
			await document.exitFullscreen().catch(() => undefined);
			return;
		}
		await stage.requestFullscreen({ navigationUI: 'hide' }).catch(() => undefined);
		const orientation = screen.orientation as ScreenOrientation & {
			lock?: (orientation: 'landscape') => Promise<void>;
		};
		if (touch) await orientation.lock?.('landscape').catch(() => undefined);
	}

	async function togglePip() {
		if (video === undefined) return;
		if (document.pictureInPictureElement) await document.exitPictureInPicture();
		else await video.requestPictureInPicture().catch(() => undefined);
	}

	/* The controls step aside after 3 s of playing untouched, unless a menu is open. */
	let hideTimer: ReturnType<typeof setTimeout> | undefined;
	function wake() {
		shown = true;
		clearTimeout(hideTimer);
		hideTimer = setTimeout(() => {
			if (!paused && !menuOpen && !stage?.matches(':focus-within:has(:focus-visible)'))
				shown = false;
		}, HIDE_AFTER_MS);
	}

	$effect(() => {
		if (paused || menuOpen) {
			shown = true;
			clearTimeout(hideTimer);
		} else untrack(wake);
	});

	/* Taps: one shows or hides the controls; two on a side skip 10 s. Clicks play and pause. */
	let lastTap = 0;
	function pointerup(event: PointerEvent) {
		if (event.target !== event.currentTarget) return;
		if (event.pointerType === 'mouse') {
			toggle();
			wake();
			return;
		}
		const now = event.timeStamp;
		if (now - lastTap < 300) {
			const { left, width } = (event.currentTarget as HTMLElement).getBoundingClientRect();
			seekBy(event.clientX - left < width / 2 ? -SKIP_SECONDS : SKIP_SECONDS);
			lastTap = 0;
			return;
		}
		lastTap = now;
		if (shown && !paused) {
			shown = false;
			clearTimeout(hideTimer);
		} else wake();
	}

	registerShortcuts(() => [
		{ keys: [['space'], ['k']], label: m.course_video_keys_play() },
		{ keys: [['j'], ['left']], label: m.course_video_back() },
		{ keys: [['l'], ['right']], label: m.course_video_forward() },
		{ keys: [['m']], label: m.course_video_keys_mute() },
		{ keys: [['f']], label: m.course_video_fullscreen() },
		...(onprevious ? [{ keys: [['shift', 'p']], label: m.course_video_keys_previous() }] : []),
		...(onnext ? [{ keys: [['shift', 'n']], label: m.course_video_keys_next() }] : [])
	]);

	function keydown(event: KeyboardEvent) {
		if (event.defaultPrevented || event.ctrlKey || event.metaKey || event.altKey) return;
		if (isTypingTarget(event.target) || document.querySelector('dialog[open]')) return;
		if (event.target instanceof HTMLInputElement && event.target.type === 'range') return;
		const key = event.key.toLowerCase();
		const actions: Record<string, () => void> = {
			' ': toggle,
			k: toggle,
			j: () => seekBy(-SKIP_SECONDS),
			arrowleft: () => seekBy(-SKIP_SECONDS),
			l: () => seekBy(SKIP_SECONDS),
			arrowright: () => seekBy(SKIP_SECONDS),
			f: () => void toggleFullscreen(),
			m: () => (muted = !muted)
		};
		if (event.shiftKey && key === 'n' && onnext) actions.n = onnext;
		if (event.shiftKey && key === 'p' && onprevious) actions.p = onprevious;
		const action = actions[key];
		if (action === undefined || (key === ' ' && event.target instanceof HTMLButtonElement)) return;
		event.preventDefault();
		action();
		wake();
	}

	/* The system's media controls (lock screen, headset keys) drive this player too. */
	$effect(() => {
		if (!('mediaSession' in navigator)) return;
		const media = navigator.mediaSession;
		const artwork = thumbnailUrl(session.current, entry.data, 512);
		media.metadata = new MediaMetadata({
			title,
			artist: sectionTitle ?? courseTitle,
			album: courseTitle,
			artwork: artwork ? [{ src: artwork, sizes: '512x320', type: 'image/jpeg' }] : []
		});
		const handlers: [MediaSessionAction, MediaSessionActionHandler | null][] = [
			['play', () => void video?.play()],
			['pause', () => video?.pause()],
			['seekbackward', () => seekBy(-SKIP_SECONDS)],
			['seekforward', () => seekBy(SKIP_SECONDS)],
			['previoustrack', onprevious ?? null],
			['nexttrack', onnext ?? null]
		];
		for (const [action, handler] of handlers) {
			try {
				media.setActionHandler(action, handler);
			} catch {
				// Browsers without an action ignore it.
			}
		}
		return () => {
			for (const [action] of handlers) {
				try {
					media.setActionHandler(action, null);
				} catch {
					// As above.
				}
			}
		};
	});

	const qualityItems = $derived.by((): MenuEntry[] => {
		const current = source;
		if (current === undefined || current.master.renditions.length < 2) return [];
		return [
			{ heading: m.course_video_quality() },
			{
				label: m.course_video_quality_auto(),
				description: m.course_video_quality_auto_detail(),
				checked: current.quality === -1,
				onselect: () => current.select(-1)
			},
			...current.master.renditions.map((rendition, index) => ({
				label: renditionLabel(rendition),
				description: m.course_video_kbps({
					kbps: formatNumber(Math.round(rendition.bandwidth / 1000))
				}),
				checked: current.quality === index,
				onselect: () => current.select(index)
			}))
		];
	});

	const settings = $derived<MenuEntry[]>([
		{ heading: m.course_video_speed() },
		...SPEEDS.map((speed) => ({
			label: speed === 1 ? m.course_video_speed_normal() : `${formatNumber(speed)}×`,
			checked: playbackRate === speed,
			onselect: () => (playbackRate = speed)
		})),
		...(qualityItems.length > 0 ? [{ separator: true } as const, ...qualityItems] : [])
	]);
</script>

<svelte:window onkeydown={keydown} onpagehide={() => save()} />
<svelte:document
	onvisibilitychange={() => document.visibilityState === 'hidden' && save()}
	onfullscreenchange={() => (fullscreen = document.fullscreenElement === stage)}
/>

<div
	class={['stage', { shown: shown || phase !== 'ready', fullscreen, paused }]}
	bind:this={stage}
	onpointermove={(event) => event.pointerType === 'mouse' && wake()}
	onfocusin={wake}
	role="region"
	aria-label={m.course_video_player({ title })}
>
	<video
		bind:this={video}
		bind:paused
		bind:currentTime
		bind:duration
		bind:muted
		bind:playbackRate
		bind:buffered
		poster={thumbnailUrl(session.current, entry.data, 1280)}
		playsinline
		preload="metadata"
		onloadedmetadata={loaded}
		ontimeupdate={timeupdate}
		onpause={() => save()}
		onended={ended}
		onwaiting={() => (waiting = true)}
		onplaying={() => (waiting = false)}
		oncanplay={() => (waiting = false)}
		onerror={() => phase !== 'loading' && source !== undefined && fail(video?.error)}
	></video>

	<div class="surface" onpointerup={pointerup} aria-hidden="true"></div>

	{#if phase === 'failed'}
		<div class="overlay error" role="alert">
			<IconVideoCamera class="error-icon" aria-hidden="true" />
			<p class="error-title">
				{unsupported
					? m.course_video_unsupported()
					: unavailable
						? m.course_video_unavailable()
						: m.course_video_failed()}
			</p>
			{#if unsupported}
				<p class="error-detail">{m.course_video_unsupported_body()}</p>
			{:else if !unavailable && failure}
				<p class="error-detail">{errorMessage(failure)}</p>
				<Button variant="solid" icon={IconArrowClockwise} onclick={retry}>{m.common_retry()}</Button
				>
			{/if}
		</div>
	{:else if phase === 'loading' || waiting}
		<div class="overlay" role="status" aria-label={m.course_video_loading()}>
			<Spinner size={44} />
		</div>
	{/if}

	{#if phase === 'ready'}
		<div class="center" class:touch>
			{#if touch}
				<button
					type="button"
					class="round small"
					disabled={!onprevious}
					onclick={onprevious}
					aria-label={m.course_video_previous()}
				>
					<IconSkipBack aria-hidden="true" />
				</button>
			{/if}
			<button
				type="button"
				class="round big"
				onclick={toggle}
				aria-label={paused ? m.course_video_play() : m.course_video_pause()}
			>
				{#if paused}<IconPlay aria-hidden="true" />{:else}<IconPause aria-hidden="true" />{/if}
			</button>
			{#if touch}
				<button
					type="button"
					class="round small"
					disabled={!onnext}
					onclick={onnext}
					aria-label={m.course_video_next()}
				>
					<IconSkipForward aria-hidden="true" />
				</button>
			{/if}
		</div>

		<div class="controls">
			<input
				class="scrub"
				type="range"
				min="0"
				max={duration || 0}
				step="any"
				bind:value={currentTime}
				oninput={wake}
				aria-label={m.course_video_position()}
				aria-valuetext={m.course_video_position_value({
					position: formatClockLong(currentTime),
					total: formatClockLong(duration)
				})}
				style:--played={duration > 0 ? currentTime / duration : 0}
				style:--loaded={duration > 0 ? loadedTo / duration : 0}
				onkeydown={(event) => {
					if (event.key === 'ArrowLeft' || event.key === 'ArrowRight') {
						event.preventDefault();
						seekBy(event.key === 'ArrowLeft' ? -SKIP_SECONDS : SKIP_SECONDS);
					}
				}}
			/>
			<div class="row">
				<!-- On touch screens the centre transport owns play and pause. -->
				{#if !touch}
					<button
						type="button"
						class="control"
						onclick={toggle}
						aria-label={paused ? m.course_video_play() : m.course_video_pause()}
					>
						{#if paused}<IconPlay aria-hidden="true" />{:else}<IconPause aria-hidden="true" />{/if}
					</button>
				{/if}
				<button
					type="button"
					class="control skip"
					onclick={() => seekBy(-SKIP_SECONDS)}
					aria-label={m.course_video_back()}
				>
					<IconArrowCounterClockwise aria-hidden="true" />
				</button>
				<button
					type="button"
					class="control skip"
					onclick={() => seekBy(SKIP_SECONDS)}
					aria-label={m.course_video_forward()}
				>
					<IconArrowClockwise aria-hidden="true" />
				</button>
				<button
					type="button"
					class="control"
					onclick={() => (muted = !muted)}
					aria-label={muted ? m.course_video_unmute() : m.course_video_mute()}
					aria-pressed={muted}
				>
					{#if muted}<IconSpeakerSlash aria-hidden="true" />{:else}<IconSpeakerHigh
							aria-hidden="true"
						/>{/if}
				</button>
				<span class="time">
					{formatClock(currentTime)}
					<span class="total">/ {formatClock(duration)}</span>
				</span>
				<span class="spacer"></span>
				<Menu
					items={settings}
					label={m.course_video_settings()}
					placement="top-end"
					bind:open={menuOpen}
				>
					{#snippet trigger(props)}
						<button type="button" class="control" {...props} aria-label={m.course_video_settings()}>
							<IconGearSix aria-hidden="true" />
							{#if playbackRate !== 1}<span class="rate">{formatNumber(playbackRate)}×</span>{/if}
						</button>
					{/snippet}
				</Menu>
				{#if pip}
					<button
						type="button"
						class="control"
						onclick={() => void togglePip()}
						aria-label={m.course_video_pip()}
					>
						<IconPictureInPicture aria-hidden="true" />
					</button>
				{/if}
				<button
					type="button"
					class="control"
					onclick={() => void toggleFullscreen()}
					aria-label={fullscreen ? m.course_video_exit_fullscreen() : m.course_video_fullscreen()}
				>
					{#if fullscreen}<IconCornersIn aria-hidden="true" />{:else}<IconCornersOut
							aria-hidden="true"
						/>{/if}
				</button>
			</div>
		</div>
	{/if}
</div>

<style>
	.stage {
		--ink: oklch(100% 0 0);
		position: relative;
		overflow: hidden;
		aspect-ratio: 16 / 9;
		max-block-size: calc(100dvh - 180px);
		inline-size: 100%;
		margin-inline: auto;
		background: oklch(0% 0 0);
		color: var(--ink);
		container-type: inline-size;
	}

	.fullscreen {
		max-block-size: none;
		aspect-ratio: auto;
		block-size: 100%;
	}

	video {
		position: absolute;
		inset: 0;
		inline-size: 100%;
		block-size: 100%;
		object-fit: contain;
	}

	.surface {
		position: absolute;
		inset: 0;
		cursor: pointer;
	}

	.stage:not(.shown) {
		cursor: none;
	}

	.stage:not(.shown) .surface {
		cursor: none;
	}

	.overlay {
		position: absolute;
		inset: 0;
		display: grid;
		place-content: center;
		justify-items: center;
		gap: 10px;
		padding: 24px;
		text-align: center;
		pointer-events: none;
	}

	.error {
		background: oklch(0% 0 0 / 0.72);
		pointer-events: auto;
	}

	.overlay :global(.error-icon) {
		inline-size: 44px;
		block-size: 44px;
		opacity: 0.8;
	}

	.error-title {
		font: var(--type-title-s);
	}

	.error-detail {
		max-inline-size: 44ch;
		margin-block-end: 8px;
		opacity: 0.8;
		font: var(--type-meta);
	}

	.center {
		position: absolute;
		inset-block-start: 50%;
		inset-inline-start: 50%;
		display: flex;
		align-items: center;
		gap: clamp(16px, 8cqi, 56px);
		transform: translate(-50%, -50%);
		opacity: 0;
		pointer-events: none;
		transition: opacity var(--dur-base) var(--ease-out);
	}

	.shown .center.touch,
	.paused .center {
		opacity: 1;
	}

	.shown .center {
		pointer-events: auto;
	}

	.round {
		display: grid;
		place-items: center;
		border: 0;
		border-radius: 50%;
		background: oklch(0% 0 0 / 0.5);
		color: var(--ink);
		cursor: pointer;
		touch-action: manipulation;
		transition: transform var(--dur-instant) var(--ease-out);
	}

	.round:active {
		transform: scale(0.94);
	}

	.round:disabled {
		opacity: 0.35;
		cursor: default;
	}

	.big {
		inline-size: 72px;
		block-size: 72px;
	}

	.big :global(svg) {
		inline-size: 32px;
		block-size: 32px;
	}

	.small {
		inline-size: 52px;
		block-size: 52px;
	}

	.small :global(svg) {
		inline-size: 22px;
		block-size: 22px;
	}

	/* A phone's stage is barely 220 px tall: the transport keeps clear of the scrub bar. */
	@container (width < 480px) {
		.big {
			inline-size: 56px;
			block-size: 56px;
		}

		.big :global(svg) {
			inline-size: 26px;
			block-size: 26px;
		}

		.small {
			inline-size: 40px;
			block-size: 40px;
		}

		.small :global(svg) {
			inline-size: 18px;
			block-size: 18px;
		}

		.center {
			margin-block-start: -12px;
		}
	}

	.controls {
		position: absolute;
		inset: auto 0 0;
		display: grid;
		gap: 2px;
		padding: 40px 12px 8px;
		background: linear-gradient(transparent, oklch(0% 0 0 / 0.75));
		opacity: 0;
		transform: translateY(8px);
		transition:
			opacity var(--dur-base) var(--ease-out),
			transform var(--dur-base) var(--ease-out);
	}

	.shown .controls {
		opacity: 1;
		transform: none;
	}

	.stage:not(.shown) .controls {
		pointer-events: none;
	}

	.row {
		display: flex;
		align-items: center;
		gap: 2px;
	}

	.control {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		gap: 4px;
		min-inline-size: 40px;
		block-size: 40px;
		padding-inline: 8px;
		border: 0;
		border-radius: var(--r-pill);
		background: none;
		color: var(--ink);
		cursor: pointer;
		font: var(--type-label);
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.control:hover {
			background: oklch(100% 0 0 / 0.14);
			transition-duration: 0s;
		}
	}

	.control :global(svg) {
		inline-size: 20px;
		block-size: 20px;
	}

	.control:focus-visible,
	.round:focus-visible,
	.scrub:focus-visible {
		outline: 2px solid var(--ink);
		outline-offset: 2px;
	}

	.rate {
		font-variant-numeric: tabular-nums;
	}

	.time {
		padding-inline: 8px;
		font: var(--type-label);
		font-variant-numeric: tabular-nums;
		white-space: nowrap;
	}

	.total {
		opacity: 0.7;
	}

	.spacer {
		flex: 1;
	}

	@container (width < 420px) {
		.skip {
			display: none;
		}
	}

	.scrub {
		--track: oklch(100% 0 0 / 0.25);
		--fill: var(--course-bar, var(--accent));
		inline-size: 100%;
		block-size: 20px;
		margin: 0;
		background: transparent;
		cursor: pointer;
		appearance: none;
	}

	.scrub::-webkit-slider-runnable-track {
		block-size: 4px;
		border-radius: var(--r-pill);
		background: linear-gradient(
			to right,
			var(--fill) calc(var(--played) * 100%),
			oklch(100% 0 0 / 0.45) calc(var(--played) * 100%) calc(var(--loaded) * 100%),
			var(--track) calc(var(--loaded) * 100%)
		);
	}

	.scrub::-moz-range-track {
		block-size: 4px;
		border-radius: var(--r-pill);
		background: linear-gradient(
			to right,
			var(--fill) calc(var(--played) * 100%),
			oklch(100% 0 0 / 0.45) calc(var(--played) * 100%) calc(var(--loaded) * 100%),
			var(--track) calc(var(--loaded) * 100%)
		);
	}

	.scrub::-webkit-slider-thumb {
		inline-size: 14px;
		block-size: 14px;
		margin-block-start: -5px;
		border-radius: 50%;
		background: var(--fill);
		box-shadow: 0 0 0 3px oklch(100% 0 0 / 0.9);
		appearance: none;
		transition: transform var(--dur-fast) var(--ease-out);
	}

	.scrub::-moz-range-thumb {
		inline-size: 14px;
		block-size: 14px;
		border: 0;
		border-radius: 50%;
		background: var(--fill);
		box-shadow: 0 0 0 3px oklch(100% 0 0 / 0.9);
	}

	.scrub:active::-webkit-slider-thumb {
		transform: scale(1.25);
	}

	@media (prefers-reduced-motion: reduce) {
		.controls,
		.center {
			transition: none;
		}
	}
</style>
