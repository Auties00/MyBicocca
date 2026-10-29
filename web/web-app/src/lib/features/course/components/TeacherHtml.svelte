<script lang="ts">
	import type { Attachment } from 'svelte/attachments';
	import { session } from '#lib/data/session.svelte.ts';
	import { sanitizeHtml } from '#lib/format/html.ts';
	import Prose from '#lib/ui/Prose.svelte';
	import { fetchFile } from '../file/opener.svelte.ts';
	import { blobUrl } from '../file/save.ts';
	import { classifyFile, mimeFor } from '../files.ts';

	interface Props {
		/** Teacher HTML as Moodle sends it; it is sanitized here. */
		html: string;
		size?: 'sm' | 'md';
		/** Clamps to this many lines, as previews do. */
		lines?: number;
	}

	let { html, size = 'md', lines }: Props = $props();

	const clean = $derived(sanitizeHtml(html));

	/** A transparent pixel: an image waiting for its bytes shows its placeholder, not a broken icon. */
	const BLANK = 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7';

	/* Moodle's images need the token: fetched with it once on screen, and shown as blobs. */
	function moodleImages(markup: string): Attachment<HTMLElement> {
		return (root) => {
			const scope = session.current;
			const images = root.querySelectorAll<HTMLImageElement>('img[data-moodle-src]');
			if (scope === undefined || images.length === 0 || markup === '') return;
			const alts = new Map(Array.from(images, (image) => [image, image.alt]));
			for (const image of images) {
				const [width, height] = [image.getAttribute('width'), image.getAttribute('height')];
				// The blank pixel is square; the placeholder keeps the size the teacher gave.
				if (width && height) image.style.aspectRatio = `${width} / ${height}`;
				image.alt = '';
				image.src = BLANK;
				image.classList.add('pending');
			}
			const urls: string[] = [];
			let live = true;
			const observer = new IntersectionObserver(
				(entries) => {
					for (const entry of entries) {
						if (!entry.isIntersecting) continue;
						const image = entry.target as HTMLImageElement;
						observer.unobserve(image);
						const url = image.dataset.moodleSrc ?? '';
						const name = new URL(url).pathname.split('/').at(-1) ?? '';
						fetchFile(scope, { url, modified: null })
							.then((bytes) => {
								if (!live) return;
								const src = blobUrl(bytes, mimeFor(classifyFile(name, null), name));
								urls.push(src);
								image.alt = alts.get(image) ?? '';
								image.src = src;
								image.classList.remove('pending');
								image.style.removeProperty('aspect-ratio');
							})
							.catch(() => image.classList.add('missing'));
					}
				},
				{ rootMargin: '200px' }
			);
			images.forEach((image) => observer.observe(image));
			return () => {
				live = false;
				observer.disconnect();
				urls.forEach((url) => URL.revokeObjectURL(url));
			};
		};
	}
</script>

<div class={['teacher', { clamped: lines !== undefined }]} style:--lines={lines}>
	<Prose {size}>
		<div class="html" {@attach moodleImages(clean)}>
			<!-- eslint-disable-next-line svelte/no-at-html-tags -- sanitized by sanitizeHtml -->
			{@html clean}
		</div>
	</Prose>
</div>

<style>
	.teacher {
		min-inline-size: 0;
	}

	.clamped .html {
		display: -webkit-box;
		overflow: hidden;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: var(--lines);
		line-clamp: var(--lines);
	}

	.clamped .html :global(:where(p, ul, ol, blockquote, pre, table, figure) + *) {
		margin-block-start: 0;
	}

	.clamped .html :global(img) {
		display: none;
	}

	.html :global(img) {
		max-inline-size: 100%;
		background: var(--surface-sunken);
	}

	.html :global(img.pending) {
		border-radius: var(--r-sm);
		background: var(--track);
		animation: placeholder 1.6s var(--ease-in-out) infinite alternate;
	}

	.html :global(img.pending:not([width])) {
		inline-size: 100%;
	}

	.html :global(img.pending:not([height])) {
		block-size: 120px;
	}

	@keyframes placeholder {
		to {
			opacity: 0.55;
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.html :global(img.pending) {
			animation: none;
		}
	}

	.html :global(img.missing) {
		display: none;
	}
</style>
