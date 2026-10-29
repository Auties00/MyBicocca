<script lang="ts">
	import { session } from '#lib/data/session.svelte.ts';
	import { courseShapes, shapePath } from '#lib/features/elearning/shapes.ts';
	import { thumbnailUrl } from '../video/stream.svelte.ts';

	interface Props {
		courseId: number;
		/** Known once the recording was played; resolving it here would log a view in Moodle. */
		entryId: string | null;
		/** Width asked of Kaltura, in pixels. */
		width?: number;
	}

	let { courseId, entryId, width = 640 }: Props = $props();

	const shapes = $derived(courseShapes(courseId + 3));
	const source = $derived(thumbnailUrl(session.current, entryId, width) ?? null);
	let loaded = $state<string | null>(null);
	let failed = $state<string | null>(null);
</script>

<span class="thumbnail">
	<svg class="placeholder" viewBox="0 0 160 100" aria-hidden="true" focusable="false">
		<path class="a" d={shapePath(shapes[0])} transform="translate(92 -18) scale(0.9)" />
		<path class="b" d={shapePath(shapes[1])} transform="translate(-14 52) scale(0.62)" />
	</svg>
	{#if source !== null && failed !== source}
		<img
			src={source}
			alt=""
			{width}
			height={Math.round((width * 10) / 16)}
			loading="lazy"
			decoding="async"
			class={{ shown: loaded === source }}
			onload={() => (loaded = source)}
			onerror={() => (failed = source)}
		/>
	{/if}
</span>

<style>
	.thumbnail {
		position: absolute;
		inset: 0;
		display: block;
		background: color-mix(in oklch, var(--course-container) 70%, var(--surface-sunken));
	}

	.placeholder {
		position: absolute;
		inset: 0;
		inline-size: 100%;
		block-size: 100%;
	}

	.a {
		fill: color-mix(in oklch, var(--course-bar) 16%, transparent);
	}

	.b {
		fill: color-mix(in oklch, var(--gold) 16%, transparent);
	}

	img {
		position: absolute;
		inset: 0;
		inline-size: 100%;
		block-size: 100%;
		object-fit: cover;
		opacity: 0;
		transition: opacity var(--dur-slow) var(--ease-out);
	}

	img.shown {
		opacity: 1;
	}
</style>
