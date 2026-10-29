<script lang="ts">
	import { courseShapes, shapePath, type ShapeName } from '#lib/features/elearning/shapes.ts';
	import { reducedMotion } from '#lib/ui/media.ts';

	interface Props {
		courseId: number;
		/** The shapes turn while the course loads, as Android's hero does. */
		spinning: boolean;
		/** A card takes the cover's end: the small shape keeps to the title's column. */
		aside?: boolean;
	}

	let { courseId, spinning, aside = false }: Props = $props();

	/** The angle each outline repeats itself after, so a spin can stop where it started. */
	const SYMMETRY: Readonly<Record<ShapeName, number>> = {
		burst: 24,
		puffy: 60,
		cookie: 30,
		sunny: 45,
		cookie6: 60,
		cookie4: 90
	};
	const SPINS = [
		{ duration: 8000, turn: 1 },
		{ duration: 5500, turn: -1 },
		{ duration: 3500, turn: 1 }
	] as const;

	const names = $derived(courseShapes(courseId));
	const shapes: SVGSVGElement[] = $state([]);

	$effect(() => {
		if (!spinning || reducedMotion.current) return;
		const running = shapes.map((shape, index) =>
			shape.animate([{ rotate: '0deg' }, { rotate: `${SPINS[index].turn * 360}deg` }], {
				duration: SPINS[index].duration,
				iterations: Infinity
			})
		);
		const settling = names;
		return () => running.forEach((animation, index) => settle(animation, index, settling[index]));
	});

	/* Eases on to the next angle that looks upright, never spinning back. */
	function settle(animation: Animation, index: number, name: ShapeName) {
		const { duration, turn } = SPINS[index];
		const angle = ((Number(animation.currentTime ?? 0) % duration) / duration) * 360;
		const target = Math.ceil(angle / SYMMETRY[name]) * SYMMETRY[name];
		animation.cancel();
		shapes[index]?.animate([{ rotate: `${turn * angle}deg` }, { rotate: `${turn * target}deg` }], {
			duration: 700,
			easing: 'cubic-bezier(0.25, 1, 0.5, 1)'
		});
	}
</script>

<div class={['shapes', { aside }]} aria-hidden="true">
	{#each names as name, index (index)}
		<div class={['slot', `slot-${index}`]}>
			<svg bind:this={shapes[index]} viewBox="0 0 100 100" focusable="false">
				<path d={shapePath(name)} />
			</svg>
		</div>
	{/each}
</div>

<style>
	.shapes {
		position: absolute;
		inset: 0;
		z-index: -1;
		pointer-events: none;
	}

	.slot {
		position: absolute;
	}

	svg {
		display: block;
		inline-size: 100%;
		block-size: 100%;
		overflow: visible;
	}

	.slot-0 {
		inset-block-start: -12%;
		inset-inline-end: -6%;
		inline-size: clamp(220px, 46cqi, 420px);
		aspect-ratio: 1;
		fill: var(--shape-large);
	}

	.slot-1 {
		inset-block-end: -18%;
		inset-inline-start: -10%;
		inline-size: clamp(150px, 26cqi, 260px);
		aspect-ratio: 1;
		fill: var(--shape-medium);
	}

	.slot-2 {
		inset-block-end: 22%;
		inset-inline-end: 34%;
		inline-size: clamp(64px, 9cqi, 96px);
		aspect-ratio: 1;
		fill: var(--shape-small);
	}

	/* On the band the small shape keeps to the title's column: at its end, tucked behind the card's
	   corner when there is one (the cover sets the card's width and its own side padding). */
	@container hero (width >= 720px) {
		.slot-2 {
			inset-block: auto 22%;
			inset-inline: auto 22%;
		}

		.aside .slot-2 {
			inset-block: 16% auto;
			inset-inline: auto calc(var(--pad-x) + var(--side-w) - 28px);
		}
	}

	@supports (animation-timeline: scroll()) {
		@media (prefers-reduced-motion: no-preference) {
			.slot {
				animation: linear both;
				animation-timeline: scroll(root);
				animation-range: 0 480px;
			}

			.slot-0 {
				animation-name: drift-large;
			}

			.slot-1 {
				animation-name: drift-medium;
			}

			.slot-2 {
				animation-name: drift-small;
			}
		}
	}

	@keyframes drift-large {
		to {
			opacity: 0.7;
			transform: translateY(140px) rotate(40deg);
		}
	}

	@keyframes drift-medium {
		to {
			opacity: 0.7;
			transform: translateY(110px) rotate(-55deg);
		}
	}

	@keyframes drift-small {
		to {
			opacity: 0.7;
			transform: translateY(160px) rotate(75deg);
		}
	}
</style>
