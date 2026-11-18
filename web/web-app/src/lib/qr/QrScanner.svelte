<script lang="ts">
	import { untrack } from 'svelte';
	import IconCameraSlash from '~icons/ph/camera-slash';
	import IconKeyboard from '~icons/ph/keyboard';
	import IconX from '~icons/ph/x';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import { tooltip } from '#lib/ui/tooltip.svelte.ts';
	import { openCamera, type CameraFailure } from './camera.ts';
	import { qrReader, type QrReader } from './reader.ts';

	interface Props {
		/** The dialog's name: what is being scanned. */
		title: string;
		/** Under the viewfinder while scanning: what to point the camera at. */
		hint: string;
		/** The action that leaves the scanner to type the code instead. */
		manualLabel: string;
		/** The first QR code read; the scanner has closed. */
		onscan: (raw: string) => void;
		/** Dismissed with the close button or Escape. */
		onclose: () => void;
		/** The student would rather type or paste the code; without it, that closes the scanner. */
		onmanual?: () => void;
	}

	let { title, hint, manualLabel, onscan, onclose, onmanual }: Props = $props();

	const id = $props.id();

	/** Frames are checked a few times a second: enough to feel instant, light on the battery. */
	const DETECT_EVERY_MS = 180;

	let video = $state<HTMLVideoElement>();
	let phase = $state<'starting' | 'scanning' | CameraFailure>('starting');
	/* A camera facing the user, or a webcam that doesn't say, reads as a mirror. */
	let mirrored = $state(false);
	let ending: (() => void) | undefined;

	let stream: MediaStream | undefined;
	let frame = 0;
	let open = true;
	let reader: QrReader | undefined;
	/* The platform's detector or the app's own in a worker, readied while the camera starts. */
	const ready = qrReader().then((created) => {
		if (open) reader = created;
		else created.close();
	});

	function stop() {
		cancelAnimationFrame(frame);
		for (const track of stream?.getTracks() ?? []) track.stop();
		stream = undefined;
		reader?.close();
		reader = undefined;
	}

	function detectLoop() {
		let last = 0;
		let busy = false;
		const tick = async (time: number) => {
			frame = requestAnimationFrame(tick);
			if (busy || !reader || time - last < DETECT_EVERY_MS || !video || video.readyState < 2) {
				return;
			}
			last = time;
			busy = true;
			try {
				const text = await reader.read(video);
				if (text && open) leave(() => onscan(text));
			} catch {
				// A frame that can't be read yet: the next one will do.
			} finally {
				busy = false;
			}
		};
		frame = requestAnimationFrame(tick);
	}

	async function start() {
		phase = 'starting';
		const result = await openCamera();
		if (typeof result === 'string') {
			phase = result;
			return;
		}
		if (!open || !video) {
			for (const track of result.getTracks()) track.stop();
			return;
		}
		stream = result;
		mirrored = result.getVideoTracks()[0]?.getSettings().facingMode !== 'environment';
		video.srcObject = result;
		await Promise.all([video.play().catch(() => undefined), ready]);
		if (!open) return;
		phase = 'scanning';
		detectLoop();
	}

	let dialogElement: HTMLDialogElement | undefined;

	/*
	 * Opening focuses the dialog's first button, whose tooltip reads state: untracked, so the
	 * attachment never reruns and drops the camera it just asked for.
	 */
	function present(dialog: HTMLDialogElement) {
		dialogElement = dialog;
		untrack(() => {
			dialog.showModal();
			void start();
		});
		return () => {
			open = false;
			stop();
		};
	}

	/*
	 * Closes the dialog, then runs `then`. Focus inside it is let go first: a dialog closing with
	 * no earlier focus to return to doesn't blur its buttons, and a tooltip still shown on one
	 * would be released only as the dialog is torn down.
	 */
	function leave(then?: () => void) {
		ending = then;
		open = false;
		stop();
		const focused = document.activeElement;
		if (focused instanceof HTMLElement && dialogElement?.contains(focused)) focused.blur();
		dialogElement?.close();
	}

	function manual() {
		leave(onmanual);
	}

	/* Every way out goes through the dialog's close: Escape, the buttons, a scan. */
	function closed() {
		open = false;
		stop();
		(ending ?? onclose)();
	}
</script>

<dialog class="scanner" aria-labelledby="{id}-title" {@attach present} onclose={closed}>
	<div class="bar">
		<p class="title" id="{id}-title">{title}</p>
		<button
			type="button"
			class="round manual"
			aria-label={manualLabel}
			onclick={manual}
			{@attach tooltip(manualLabel)}
		>
			<IconKeyboard aria-hidden="true" />
		</button>
		<button
			type="button"
			class="round close"
			aria-label={m.ui_close()}
			onclick={() => leave()}
			{@attach tooltip(m.ui_close())}
		>
			<IconX aria-hidden="true" />
		</button>
	</div>

	<div class="viewport">
		<video
			bind:this={video}
			class={{ live: phase === 'scanning', mirrored }}
			playsinline
			muted
			aria-hidden="true"
		></video>

		{#if phase === 'starting' || phase === 'scanning'}
			<div class="reticle" aria-hidden="true">
				<span class="corner tl"></span>
				<span class="corner tr"></span>
				<span class="corner bl"></span>
				<span class="corner br"></span>
				{#if phase === 'scanning'}<span class="line"></span>{/if}
			</div>
			<p class="hint" role="status">
				{phase === 'starting' ? m.ui_camera_starting() : hint}
			</p>
		{:else}
			<div class="problem" role="alert">
				<span class="problem-icon"><IconCameraSlash aria-hidden="true" /></span>
				{#if phase === 'denied'}
					<p>{m.ui_camera_denied()}</p>
					<p class="small">{m.ui_camera_blocked()}</p>
					<Button variant="solid" onclick={start}>{m.ui_camera_allow()}</Button>
				{:else}
					<p>{m.ui_camera_unavailable()}</p>
					<Button variant="solid" icon={IconKeyboard} onclick={manual}>{manualLabel}</Button>
				{/if}
			</div>
		{/if}
	</div>
</dialog>

<style>
	/* Phones: the whole screen is the viewfinder, as on Android. */
	.scanner {
		position: fixed;
		inset: 0;
		display: flex;
		flex-direction: column;
		inline-size: 100vw;
		max-inline-size: none;
		block-size: 100dvh;
		max-block-size: none;
		margin: 0;
		padding: 0;
		border: 0;
		overflow: hidden;
		color: oklch(98% 0 0);
		background: oklch(0% 0 0);
		color-scheme: dark;
	}

	.scanner::backdrop {
		background: oklch(0% 0 0);
	}

	.bar {
		position: absolute;
		inset-block-start: max(16px, var(--safe-top));
		inset-inline: 16px;
		z-index: 1;
		display: flex;
		align-items: center;
		gap: var(--sp-2);
	}

	.title {
		position: absolute;
		inline-size: 1px;
		block-size: 1px;
		overflow: hidden;
		clip-path: inset(50%);
		white-space: nowrap;
	}

	/* Close on the left and the keyboard on the right, where Android puts them. */
	.close {
		order: -1;
		margin-inline-end: auto;
	}

	.viewport {
		position: relative;
		flex: 1;
		min-block-size: 0;
		overflow: hidden;
		container-type: size;
	}

	/*
	 * Wider screens: a dialog over the page, its controls in its own header, the viewfinder a
	 * portrait window rather than a laptop's whole screen.
	 */
	@media (width >= 600px) {
		.scanner {
			inset: 0;
			inline-size: min(480px, calc(100vw - 2 * var(--sp-6)));
			block-size: min(640px, calc(100dvh - 2 * var(--sp-6)));
			margin: auto;
			border-radius: var(--r-xl);
			box-shadow: var(--elev-3);
		}

		.scanner::backdrop {
			background: var(--scrim);
		}

		.bar {
			position: static;
			padding: var(--sp-3) var(--sp-3) var(--sp-3) var(--sp-5);
			background: oklch(14% 0 0);
		}

		.title {
			position: static;
			inline-size: auto;
			block-size: auto;
			flex: 1;
			margin: 0;
			overflow: visible;
			clip-path: none;
			font: var(--type-title-s);
		}

		.close {
			order: 0;
			margin-inline-end: 0;
		}

		.round {
			inline-size: 40px;
			block-size: 40px;
			background: transparent;
			box-shadow: none;
		}

		.round:hover {
			background: oklch(100% 0 0 / 0.12);
		}
	}

	video {
		position: absolute;
		inset: 0;
		inline-size: 100%;
		block-size: 100%;
		object-fit: cover;
		opacity: 0;
		transition: opacity var(--dur-slow) var(--ease-out);
	}

	video.live {
		opacity: 1;
	}

	video.mirrored {
		transform: scaleX(-1);
	}

	.round {
		display: grid;
		place-items: center;
		inline-size: 48px;
		block-size: 48px;
		border: 0;
		border-radius: 50%;
		color: inherit;
		background: oklch(0% 0 0 / 0.45);
		box-shadow: inset 0 0 0 1px oklch(100% 0 0 / 0.16);
		cursor: pointer;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.round:hover {
		background: oklch(0% 0 0 / 0.6);
	}

	.round:focus-visible {
		outline: 2px solid oklch(100% 0 0);
		outline-offset: 2px;
	}

	.round :global(svg) {
		inline-size: 22px;
		block-size: 22px;
	}

	/* A square window onto the camera: the corners in the brand's colour, the rest dimmed. */
	.reticle {
		--size: min(264px, 72cqmin);
		position: absolute;
		inset-block-start: 50%;
		inset-inline-start: 50%;
		inline-size: var(--size);
		block-size: var(--size);
		translate: -50% -50%;
		border-radius: var(--r-xl);
		box-shadow:
			0 0 0 1px oklch(100% 0 0 / 0.35),
			0 0 0 100vmax oklch(0% 0 0 / 0.45);
	}

	.corner {
		position: absolute;
		inline-size: 36px;
		block-size: 36px;
		border: 4px solid var(--brand);
	}

	.tl {
		inset-block-start: -2px;
		inset-inline-start: -2px;
		border-inline-end: 0;
		border-block-end: 0;
		border-start-start-radius: var(--r-xl);
	}

	.tr {
		inset-block-start: -2px;
		inset-inline-end: -2px;
		border-inline-start: 0;
		border-block-end: 0;
		border-start-end-radius: var(--r-xl);
	}

	.bl {
		inset-block-end: -2px;
		inset-inline-start: -2px;
		border-inline-end: 0;
		border-block-start: 0;
		border-end-start-radius: var(--r-xl);
	}

	.br {
		inset-block-end: -2px;
		inset-inline-end: -2px;
		border-inline-start: 0;
		border-block-start: 0;
		border-end-end-radius: var(--r-xl);
	}

	.line {
		position: absolute;
		inset-inline: 14px;
		inset-block-start: 14px;
		block-size: 2px;
		border-radius: var(--r-pill);
		background: var(--brand);
		box-shadow: 0 0 12px var(--brand);
		animation: sweep 1.7s linear infinite alternate;
	}

	@keyframes sweep {
		to {
			transform: translateY(calc(var(--size) - 30px));
		}
	}

	.hint {
		position: absolute;
		inset-block-end: max(32px, calc(var(--safe-bottom) + 16px));
		inset-inline-start: 50%;
		translate: -50% 0;
		margin: 0;
		padding: 10px 18px;
		border-radius: var(--r-pill);
		background: oklch(0% 0 0 / 0.55);
		font: var(--type-ui-strong);
		white-space: nowrap;
	}

	.problem {
		position: absolute;
		inset: 0;
		display: grid;
		place-content: center;
		justify-items: center;
		gap: var(--sp-3);
		padding: var(--sp-6);
		text-align: center;
	}

	.problem p {
		max-inline-size: 34ch;
		margin: 0;
		font: var(--type-body);
	}

	.problem .small {
		margin-block-end: var(--sp-3);
		color: oklch(80% 0 0);
		font: var(--type-meta);
	}

	.problem-icon {
		display: grid;
		place-items: center;
		inline-size: 72px;
		block-size: 72px;
		margin-block-end: var(--sp-2);
		border-radius: 50%;
		background: oklch(100% 0 0 / 0.12);
	}

	.problem-icon :global(svg) {
		inline-size: 32px;
		block-size: 32px;
	}

	@media (prefers-reduced-motion: reduce) {
		.line {
			animation: none;
			inset-block-start: 50%;
		}
	}
</style>
