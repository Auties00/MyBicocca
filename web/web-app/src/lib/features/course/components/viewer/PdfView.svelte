<script lang="ts">
	import { untrack } from 'svelte';
	import type { PDFDocumentLoadingTask, PDFDocumentProxy, RenderTask } from 'pdfjs-dist';
	import IconDownloadSimple from '~icons/ph/download-simple';
	import IconFilePdf from '~icons/ph/file-pdf-duotone';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Spinner from '#lib/ui/Spinner.svelte';
	import { smartInvert } from '../../file/smartInvert.ts';

	interface Props {
		/** The document; the reader opens it once, so a new file needs a new reader. */
		bytes: Uint8Array;
		name: string;
		/** Pages drawn for dark mode, with Android's smart invert. */
		dark: boolean;
		/** 1 fits the widest page to the reading width. */
		zoom: number;
		ondownload: () => void;
	}

	let { bytes, name, dark, zoom, ondownload }: Props = $props();

	/** The widest a page is drawn at zoom 1, and the room around and between pages (CSS px). */
	const READING_WIDTH = 920;
	const PAD = 16;
	const GAP = 12;
	/** Pages drawn ahead of the scroll, in screen heights; the others give their canvas back. */
	const AHEAD = '150%';
	/** Phones refuse canvases much larger than this. */
	const MAX_PIXELS = 4096 * 4096;

	type Pdfjs = typeof import('../../file/pdfjs.ts');
	interface Size {
		width: number;
		height: number;
	}
	interface Drawn {
		scale: number;
		dark: boolean;
		canvas: HTMLCanvasElement | null;
		text: HTMLElement | undefined;
	}

	let doc = $state.raw<PDFDocumentProxy>();
	/** Each page's size at scale 1; pages not measured yet take the first one's. */
	let sizes = $state.raw<Size[]>([]);
	let failure = $state<'locked' | 'broken'>();
	let available = $state(0);
	let current = $state(1);
	let scroller = $state<HTMLElement>();
	const pages: HTMLElement[] = $state([]);

	const gate = new LoadingGate(() => doc === undefined && failure === undefined);

	const widest = $derived(sizes.reduce((max, size) => Math.max(max, size.width), 0));
	const scale = $derived(
		widest > 0 && available > 0 ? (Math.min(available - 2 * PAD, READING_WIDTH) / widest) * zoom : 0
	);
	const tops = $derived.by(() => {
		let y = PAD;
		return sizes.map((size) => {
			const top = y;
			y += size.height * scale + GAP;
			return top;
		});
	});

	/* The renderer's own bookkeeping, which nothing on screen reads: plain, not reactive. */
	let pdfjs: Pdfjs | undefined;
	// eslint-disable-next-line svelte/prefer-svelte-reactivity
	const near = new Set<number>();
	// eslint-disable-next-line svelte/prefer-svelte-reactivity
	const drawn = new Map<number, Drawn>();
	/** Pages pdf.js couldn't draw: they stay blank rather than being tried again and again. */
	// eslint-disable-next-line svelte/prefer-svelte-reactivity
	const undrawable = new Set<number>();
	let rendering: RenderTask | undefined;
	let closed = false;

	$effect(() => {
		const data = untrack(() => bytes);
		let task: PDFDocumentLoadingTask | undefined;
		let live = true;
		import('../../file/pdfjs.ts')
			.then(async (module) => {
				if (!live) return;
				pdfjs = module;
				task = module.openPdf(data);
				const opened = await task.promise;
				const first = (await opened.getPage(1)).getViewport({ scale: 1 });
				if (!live) return;
				sizes = Array.from({ length: opened.numPages }, () => ({
					width: first.width,
					height: first.height
				}));
				doc = opened;
				void measure(opened, () => live);
			})
			.catch((error: unknown) => {
				if (live)
					failure = (error as Error | null)?.name === 'PasswordException' ? 'locked' : 'broken';
			});
		return () => {
			live = false;
			closed = true;
			rendering?.cancel();
			void task?.destroy();
		};
	});

	/* Pages of other sizes (a landscape slide among portrait ones) take their place once known. */
	async function measure(opened: PDFDocumentProxy, live: () => boolean) {
		const measured = [...untrack(() => sizes)];
		let changed = false;
		for (let index = 1; index < opened.numPages; index++) {
			const viewport = (await opened.getPage(index + 1)).getViewport({ scale: 1 });
			if (!live()) return;
			const known = measured[index];
			if (known.width === viewport.width && known.height === viewport.height) continue;
			measured[index] = { width: viewport.width, height: viewport.height };
			changed = true;
		}
		if (changed) sizes = measured;
	}

	/* Pages near the screen are drawn, nearest first; the rest give their canvas back. */
	$effect(() => {
		const root = scroller;
		if (root === undefined || doc === undefined) return;
		const observer = new IntersectionObserver(
			(entries) => {
				for (const entry of entries) {
					const index = pages.indexOf(entry.target as HTMLElement);
					if (index < 0) continue;
					if (entry.isIntersecting) near.add(index);
					else {
						near.delete(index);
						release(index);
					}
				}
				void draw();
			},
			{ root, rootMargin: `${AHEAD} 0px` }
		);
		for (const page of pages) if (page) observer.observe(page);
		return () => observer.disconnect();
	});

	/* A new zoom or theme redraws what is near; until then the old canvas stretches. */
	$effect(() => {
		void scale;
		void dark;
		untrack(() => void draw());
	});

	function stale(index: number): boolean {
		const page = drawn.get(index);
		return (
			!undrawable.has(index) && (page?.canvas == null || page.scale !== scale || page.dark !== dark)
		);
	}

	let drawing = false;
	async function draw() {
		if (drawing || doc === undefined || scale === 0) return;
		drawing = true;
		try {
			for (;;) {
				const index = [...near]
					.filter(stale)
					.sort((a, b) => Math.abs(a + 1 - current) - Math.abs(b + 1 - current))[0];
				if (index === undefined) return;
				try {
					await drawPage(doc, index);
				} catch {
					if (closed) return;
					undrawable.add(index);
				}
			}
		} finally {
			drawing = false;
		}
	}

	async function drawPage(opened: PDFDocumentProxy, index: number) {
		const target = { scale, dark };
		const page = await opened.getPage(index + 1);
		const viewport = page.getViewport({ scale: target.scale });
		const ratio = Math.min(
			devicePixelRatio || 1,
			Math.sqrt(MAX_PIXELS / (viewport.width * viewport.height))
		);
		const canvas = document.createElement('canvas');
		canvas.width = Math.floor(viewport.width * ratio);
		canvas.height = Math.floor(viewport.height * ratio);
		/* pdf.js draws on an opaque canvas, where text gets coloured (LCD) edges that the dark pass
		   would keep as colour: a dark page gets a translucent one, read back once. */
		const context = target.dark
			? canvas.getContext('2d', { alpha: true, willReadFrequently: true })
			: null;
		rendering = page.render({
			canvas: context === null ? canvas : null,
			canvasContext: context ?? undefined,
			viewport,
			transform: ratio === 1 ? undefined : [ratio, 0, 0, ratio, 0, 0]
		});
		await rendering.promise;
		rendering = undefined;
		if (context !== null) {
			const image = context.getImageData(0, 0, canvas.width, canvas.height);
			smartInvert(image.data);
			context.putImageData(image, 0, 0);
		}
		const element = pages[index];
		if (!near.has(index) || element === undefined) return;
		const before = drawn.get(index);
		if (before?.canvas) before.canvas.replaceWith(canvas);
		else element.prepend(canvas);
		let text = before?.text;
		drawn.set(index, { ...target, canvas, text });
		if (text === undefined && pdfjs !== undefined) {
			text = document.createElement('div');
			text.className = 'textLayer';
			element.append(text);
			const layer = new pdfjs.TextLayer({
				textContentSource: page.streamTextContent(),
				container: text,
				viewport
			});
			drawn.set(index, { ...target, canvas, text });
			await layer.render().catch(() => undefined);
		}
	}

	/* The text layer stays, so the browser's find and a screen reader keep the whole document. */
	function release(index: number) {
		const page = drawn.get(index);
		if (page?.canvas == null) return;
		page.canvas.width = 0;
		page.canvas.height = 0;
		page.canvas.remove();
		page.canvas = null;
	}

	/* Where the reading is, so a new zoom or a page measured above keeps it in place. */
	let anchor: { index: number; within: number } | undefined;

	function pageAt(y: number): number {
		let low = 0;
		let high = tops.length - 1;
		while (low < high) {
			const middle = (low + high + 1) >> 1;
			if (tops[middle] <= y) low = middle;
			else high = middle - 1;
		}
		return low;
	}

	function scrolled() {
		if (scroller === undefined || tops.length === 0) return;
		const { scrollTop, clientHeight } = scroller;
		const index = pageAt(scrollTop);
		anchor = { index, within: (scrollTop - tops[index]) / (sizes[index].height * scale) };
		current = pageAt(scrollTop + clientHeight / 2) + 1;
	}

	$effect(() => {
		const at = tops;
		const root = untrack(() => scroller);
		if (root === undefined || anchor === undefined) return;
		root.scrollTop = at[anchor.index] + anchor.within * sizes[anchor.index].height * scale;
	});
</script>

<div class="reader" class:dark>
	{#if failure}
		<div class="center">
			<EmptyState
				icon={IconFilePdf}
				title={m.course_pdf_failed()}
				body={failure === 'locked' ? m.course_pdf_locked_body() : m.course_pdf_failed_body()}
			>
				{#snippet action()}
					<Button variant="solid" icon={IconDownloadSimple} onclick={ondownload}>
						{m.course_download()}
					</Button>
				{/snippet}
			</EmptyState>
		</div>
	{:else if doc === undefined}
		<div class="center" role="status">
			{#if gate.visible}
				<Spinner size={40} />
				<p class="loading">{m.course_pdf_opening()}</p>
			{/if}
		</div>
	{:else}
		<!-- The keyboard scrolls the reader once it has focus, which Safari gives no scroller. -->
		<!-- svelte-ignore a11y_no_noninteractive_tabindex -->
		<div
			class="scroller"
			bind:this={scroller}
			bind:clientWidth={available}
			onscroll={scrolled}
			tabindex="0"
			role="region"
			aria-label={name}
		>
			<div class="pages" style:--pad="{PAD}px" style:--gap="{GAP}px">
				{#each sizes as size, index (index)}
					<div
						class="page"
						bind:this={pages[index]}
						style:inline-size="{size.width * scale}px"
						style:block-size="{size.height * scale}px"
						style:--total-scale-factor={scale}
					></div>
				{/each}
			</div>
		</div>
		{#if sizes.length > 1}
			<p class="pill" aria-hidden="true">
				{m.course_pdf_page_indicator({ page: current, total: sizes.length })}
			</p>
		{/if}
	{/if}
</div>

<style>
	.reader {
		position: relative;
		block-size: 100%;
		background: var(--surface-sunken);
	}

	.center {
		display: grid;
		place-content: center;
		justify-items: center;
		gap: 14px;
		block-size: 100%;
		padding: var(--sp-6);
	}

	.loading {
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.scroller {
		block-size: 100%;
		overflow: auto;
		overscroll-behavior: contain;
		scrollbar-gutter: stable;
		outline-offset: -3px;
	}

	.pages {
		display: grid;
		justify-items: center;
		gap: var(--gap);
		inline-size: max-content;
		min-inline-size: 100%;
		padding: var(--pad) var(--pad) calc(var(--pad) + var(--shell-inset-bottom, 0px) + 72px);
	}

	.page {
		--scale-round-x: 1px;
		--scale-round-y: 1px;
		position: relative;
		overflow: hidden;
		border-radius: 2px;
		background: #fff;
		box-shadow: var(--elev-1);
	}

	.dark .page {
		background: rgb(24 26 30);
	}

	.page :global(canvas) {
		position: absolute;
		inset: 0;
		inline-size: 100%;
		block-size: 100%;
	}

	.pill {
		position: absolute;
		inset-block-start: 12px;
		inset-inline-start: 50%;
		translate: -50% 0;
		padding: 4px 12px;
		border-radius: var(--r-pill);
		background: color-mix(in oklch, var(--surface-raised) 88%, transparent);
		box-shadow: var(--elev-2);
		color: var(--text);
		font: var(--type-label);
		font-variant-numeric: tabular-nums;
		pointer-events: none;
	}

	/* pdf.js's text layer: transparent text over the canvas, for selection, find and reading. */
	.page :global(.textLayer) {
		position: absolute;
		inset: 0;
		overflow: clip;
		z-index: 0;
		line-height: 1;
		text-align: initial;
		letter-spacing: normal;
		word-spacing: normal;
		text-size-adjust: none;
		forced-color-adjust: none;
		transform-origin: 0 0;
		--text-scale-factor: calc(var(--total-scale-factor) * var(--min-font-size, 1));
		--min-font-size-inv: calc(1 / var(--min-font-size, 1));
	}

	.page :global(.textLayer :is(span, br)) {
		position: absolute;
		color: transparent;
		white-space: pre;
		cursor: text;
		transform-origin: 0% 0%;
	}

	.page :global(.textLayer > :not(.markedContent)),
	.page :global(.textLayer .markedContent span:not(.markedContent)) {
		z-index: 1;
		font-size: calc(var(--text-scale-factor) * var(--font-height, 0));
		transform: rotate(var(--rotate, 0deg)) scaleX(var(--scale-x, 1)) scale(var(--min-font-size-inv));
	}

	.page :global(.textLayer .markedContent) {
		display: contents;
	}

	.page :global(.textLayer ::selection) {
		background: color-mix(in srgb, AccentColor, transparent 50%);
		color: transparent;
	}

	.page :global(.textLayer br::selection) {
		background: transparent;
	}

	.page :global(.textLayer .endOfContent) {
		position: absolute;
		inset: 100% 0 0;
		z-index: 0;
		display: block;
		cursor: default;
		user-select: none;
	}
</style>
