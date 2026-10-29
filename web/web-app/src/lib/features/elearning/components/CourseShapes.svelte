<script lang="ts">
	import { courseShapes, placementsFor, shapePath } from '../shapes.ts';

	interface Props {
		/** The course whose three shapes are drawn. */
		courseId: number;
		/** What the arrangement is picked by: the course code, else its title. */
		seed: string;
		count: number;
		/** A second box of the same card gets its own arrangement. */
		salt?: number;
	}

	let { courseId, seed, count, salt = 0 }: Props = $props();

	const shapes = $derived(courseShapes(courseId));
	const placements = $derived(placementsFor(seed, count, salt));
</script>

<span class="layer" aria-hidden="true">
	{#each placements as placement, index (index)}
		<svg
			class="shape"
			viewBox="0 0 100 100"
			style:--x={placement.x}
			style:--y={placement.y}
			style:--s={placement.size}
			style:--r="{placement.rotation}deg"
		>
			<path d={shapePath(shapes[index % shapes.length])} />
		</svg>
	{/each}
</span>

<style>
	/* Sized by its box, so the shapes can be placed in fractions of it (container units). */
	.layer {
		position: absolute;
		inset: 0;
		overflow: clip;
		container-type: size;
		pointer-events: none;
	}

	.shape {
		position: absolute;
		inset-inline-start: calc(var(--x) * 100cqw);
		inset-block-start: calc(var(--y) * 100cqh);
		inline-size: calc(var(--s) * 100cqh);
		block-size: calc(var(--s) * 100cqh);
		translate: -50% -50%;
		rotate: var(--r);
		fill: var(--watermark);
	}
</style>
