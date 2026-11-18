<script lang="ts" module>
	const chips = Object.values(
		import.meta.glob<string>('../assets/chips/chip*.svg', {
			query: '?url',
			import: 'default',
			eager: true
		})
	).sort((a, b) => chipNumber(a) - chipNumber(b));

	function chipNumber(url: string): number {
		return Number(/chip(\d+)/.exec(url)?.[1] ?? 0);
	}
</script>

<script lang="ts">
	import { m } from '#lib/paraglide/messages.js';
	import { haptics } from '#lib/ui/haptics.svelte.ts';
	import { chipIndex, institutionalEmail, titleCase } from '../card.ts';
	import { CardMotion } from '../cardMotion.ts';
	import CardBack from './CardBack.svelte';
	import CardFront from './CardFront.svelte';
	import { reducedMotion } from '#lib/ui/media.ts';

	interface Props {
		accountId: string;
		name: string;
		number: string;
		username: string;
		photo: string | null;
		/** The back is about to be seen: time to load what only it shows. */
		onreveal?: () => void;
	}

	let { accountId, name, number, username, photo, onreveal }: Props = $props();

	const id = $props.id();

	let face = $state<'front' | 'back'>('front');
	let revealed = $state(false);
	let motion: CardMotion | undefined;

	const holder = $derived(name.trim() || m.profile_badge_student());
	const chip = $derived(chips[chipIndex(accountId)] ?? chips[0]);
	const email = $derived(institutionalEmail(username));

	function reveal() {
		if (revealed) return;
		revealed = true;
		onreveal?.();
	}

	function physics(stage: HTMLElement) {
		const card = stage.querySelector<HTMLElement>('.card');
		if (card === null) return;
		const instance = new CardMotion({
			stage,
			card,
			surface: stage,
			still: () => reducedMotion.current,
			onface: (next) => {
				face = next;
				if (next === 'back') reveal();
			},
			onhaptic: (pattern) => haptics.vibrate(pattern)
		});
		motion = instance;
		const resized = new ResizeObserver(() => instance.refresh());
		resized.observe(stage);
		const stop = deviceTilt(instance);
		return () => {
			resized.disconnect();
			stop();
			instance.destroy();
			motion = undefined;
		};
	}

	/**
	 * The device's tilt moves the art as on Android, measured from how the phone is held when the
	 * page opens. Only where no permission prompt is needed (not iOS), and not with reduced motion.
	 */
	function deviceTilt(instance: CardMotion): () => void {
		type Prompting = { requestPermission?: () => Promise<string> };
		if (
			typeof DeviceOrientationEvent === 'undefined' ||
			typeof (DeviceOrientationEvent as unknown as Prompting).requestPermission === 'function' ||
			!matchMedia('(pointer: coarse)').matches
		)
			return () => {};
		let rest: { beta: number; gamma: number } | undefined;
		const radians = Math.PI / 180;
		const clamp = (value: number) => Math.min(1, Math.max(-1, value));
		const listener = (event: DeviceOrientationEvent) => {
			if (reducedMotion.current || event.beta === null || event.gamma === null) return;
			if (!Number.isFinite(event.beta) || !Number.isFinite(event.gamma)) return;
			rest ??= { beta: event.beta, gamma: event.gamma };
			instance.tilt(
				clamp(((event.gamma - rest.gamma) * radians) / 2.5),
				-clamp(((event.beta - rest.beta) * radians) / 2.5)
			);
		};
		addEventListener('deviceorientation', listener);
		return () => removeEventListener('deviceorientation', listener);
	}

	function keydown(event: KeyboardEvent) {
		if (event.defaultPrevented || event.altKey || event.ctrlKey || event.metaKey) return;
		if (event.key === 'Enter' || event.key === ' ' || event.key === 'ArrowRight') {
			event.preventDefault();
			reveal();
			motion?.flip(false);
		} else if (event.key === 'ArrowLeft') {
			event.preventDefault();
			reveal();
			motion?.flip(true);
		}
	}

	$effect(() => {
		void reducedMotion.current;
		motion?.refresh();
	});
</script>

<div class={['badge', { still: reducedMotion.current }]} data-face={face}>
	<!--
		The card's text is only drawn: the control is a flat, text-free button over it, so its name
		is the one announced and the faces stay decoration.
	-->
	<div
		class="stage"
		role="presentation"
		{@attach physics}
		onpointerdown={(event) => {
			reveal();
			motion?.pointerdown(event);
		}}
		onpointermove={(event) => motion?.pointermove(event)}
		onpointerup={(event) => motion?.pointerup(event)}
		onpointercancel={(event) => motion?.pointercancel(event)}
		onpointerleave={(event) => motion?.pointerleave(event)}
		onpointerenter={reveal}
	>
		<div class="card">
			<div class="face front" aria-hidden="true">
				<CardFront name={holder} {number} {chip} />
				<span class="foil"></span>
			</div>
			<div class="face back" aria-hidden="true">
				<CardBack signature={titleCase(holder)} {email} {photo} {revealed} />
				<span class="foil"></span>
			</div>
		</div>
		<button
			type="button"
			class="hit"
			aria-label={m.profile_card_label({ name: holder, number })}
			aria-describedby="{id}-face"
			onfocus={reveal}
			onkeydown={keydown}
		></button>
	</div>
	<p class="sr-only" id="{id}-face" aria-live="polite">
		{face === 'front' ? m.profile_card_front() : m.profile_card_back({ email })}
	</p>
</div>

<style>
	.badge {
		--fill: light-dark(#f01d59, #9c0c35);
		--ink: #fff8f7;
		--u: calc(100cqw / 380);
		container-type: inline-size;
		inline-size: 100%;
	}

	.stage {
		--tx: 0.5;
		--ty: 0.5;
		position: relative;
		perspective: calc(380 * var(--u) * 3);
		border-radius: calc(20 * var(--u));
		touch-action: pan-y;
		-webkit-tap-highlight-color: transparent;
		user-select: none;
		-webkit-user-select: none;
		cursor: grab;
	}

	.stage:active {
		cursor: grabbing;
	}

	.hit {
		position: absolute;
		inset: 0;
		z-index: 1;
		padding: 0;
		border: 0;
		border-radius: calc(20 * var(--u));
		background: transparent;
		cursor: inherit;
		touch-action: pan-y;
		-webkit-tap-highlight-color: transparent;
	}

	.hit:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 4px;
	}

	.card {
		position: relative;
		aspect-ratio: 1.6111112;
		transform-style: preserve-3d;
		will-change: transform;
	}

	.face {
		position: absolute;
		inset: 0;
		overflow: hidden;
		border-radius: calc(20 * var(--u));
		background: var(--fill);
		color: var(--ink);
		box-shadow:
			0 1px 2px oklch(20% 0.05 14 / 0.12),
			0 calc(12 * var(--u)) calc(28 * var(--u)) calc(-12 * var(--u)) oklch(25% 0.1 14 / 0.35);
		backface-visibility: hidden;
		isolation: isolate;
	}

	.back {
		transform: rotateY(180deg);
	}

	/* Two multiply washes, as Android paints them: a fixed rainbow and a foil under the pointer. */
	.face::after,
	.foil {
		content: '';
		position: absolute;
		inset: 0;
		mix-blend-mode: multiply;
		pointer-events: none;
	}

	.face::after {
		background: linear-gradient(
			to bottom right,
			rgb(255 0 255 / 0.12) 0%,
			rgb(0 255 255 / 0.12) 18.75%,
			rgb(255 255 0 / 0.12) 37.5%,
			rgb(255 192 203 / 0.12) 56.25%,
			transparent 75%
		);
	}

	.foil {
		background: var(--foil, none);
	}

	/* Reduced motion: no turning, the faces cross-fade. */
	.still .card {
		transform: none;
	}

	.still .face {
		backface-visibility: visible;
		transition: opacity var(--dur-base) var(--ease-out);
	}

	.still .back {
		transform: none;
		opacity: 0;
	}

	.still[data-face='back'] .back {
		opacity: 1;
	}

	.still[data-face='back'] .front {
		opacity: 0;
	}
</style>
