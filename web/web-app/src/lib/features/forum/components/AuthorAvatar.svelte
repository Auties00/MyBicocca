<script lang="ts">
	import { shapePath, type ShapeName } from '#lib/features/elearning/shapes.ts';
	import { courseSlot } from '#lib/ui/courseSlot.ts';

	interface Props {
		/** The author's full name; the same name always gets the same shape and colour. */
		name: string;
		size?: number;
	}

	let { name, size = 40 }: Props = $props();

	/* Moodle pictures are almost always the placeholder: initials on a shape tell people apart. */
	const SHAPES: readonly ShapeName[] = ['cookie6', 'puffy', 'burst', 'sunny', 'cookie'];

	const slot = $derived(courseSlot(name));
	const shape = $derived(SHAPES[courseSlot(`${name}·shape`) % SHAPES.length]);
	const initials = $derived(
		name
			.trim()
			.split(/\s+/)
			.filter(Boolean)
			.filter((_, index, all) => index === 0 || index === all.length - 1)
			.map((part) => part[0]?.toLocaleUpperCase())
			.join('')
	);
</script>

<span class="avatar" data-course={slot} style:--size="{size}px" aria-hidden="true">
	<svg viewBox="0 0 100 100" focusable="false"><path d={shapePath(shape)} /></svg>
	<span class="initials">{initials}</span>
</span>

<style>
	.avatar {
		position: relative;
		display: inline-grid;
		place-items: center;
		flex-shrink: 0;
		inline-size: var(--size);
		block-size: var(--size);
		user-select: none;
	}

	svg {
		position: absolute;
		inset: 0;
		inline-size: 100%;
		block-size: 100%;
		fill: light-dark(oklch(87% 0.075 var(--course-h)), oklch(38% 0.085 var(--course-h)));
	}

	.initials {
		position: relative;
		color: light-dark(oklch(36% 0.11 var(--course-h)), oklch(93% 0.045 var(--course-h)));
		font: 650 calc(var(--size) * 0.36) / 1 var(--font-ui);
		letter-spacing: 0.02em;
	}
</style>
