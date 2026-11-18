<script lang="ts" module>
	/** The lines a loading header draws bars for. */
	export interface HeaderSkeleton {
		/** Default true. */
		eyebrow?: boolean;
		/** Lines of the title; default 2, so a long name never pushes the page down on arrival. */
		title?: 1 | 2;
		/** Default true; a number of lines for a subtitle that wraps. */
		subtitle?: boolean | 1 | 2;
	}
</script>

<script lang="ts">
	import { untrack, type Snippet } from 'svelte';
	import IconArrowLeft from '~icons/ph/arrow-left';
	import { m } from '#lib/paraglide/messages.js';
	import {
		getPageChrome,
		hasPageChrome,
		topbarHeight,
		type PageChrome
	} from './pageChrome.svelte.ts';
	import Skeleton from './Skeleton.svelte';
	import { internalHref, type Href, type Icon } from './types.ts';

	interface Props {
		title: string;
		/** What the top bar shows once the title scrolls under it, where the title is a sentence. */
		chromeTitle?: string;
		/** Small line above the title (a date, a breadcrumb-like context). */
		eyebrow?: string;
		/** The eyebrow is a code (a course code): never translated, tabular figures. */
		eyebrowCode?: boolean;
		/** One sentence under the title, or a snippet for a richer line. */
		subtitle?: string | Snippet;
		/** warning for a stale or degraded line ("Non aggiornate · dati di 3 ore fa"). */
		subtitleTone?: 'muted' | 'warning' | 'danger';
		/** Glyph before the subtitle, e.g. a cloud for stale data. */
		subtitleIcon?: Icon;
		/** Parent page. Shown here from 600 px up; on phones the top bar shows the back arrow. */
		back?: { href: Href; label: string };
		/**
		 * Page actions, right of the title. Text buttons wrap under it on narrow screens; icon
		 * buttons alone always stay beside it.
		 */
		actions?: Snippet;
		/** Extra row under the subtitle: filters, a status line, "aggiornato 5 min fa". */
		meta?: Snippet;
		/** A bigger editorial title, for the landing page. */
		display?: boolean;
		/** The title in the UI face, for pages whose serif moment is elsewhere (a grade). */
		sans?: boolean;
		/** Language of the title when it differs from the UI's (Esse3's Italian names in English). */
		titleLang?: string;
		/**
		 * While the page's data loads, bars stand in for each text not given yet; the back link
		 * and the top bar's back arrow are there from the start.
		 */
		loading?: boolean | HeaderSkeleton;
	}

	let {
		title,
		chromeTitle,
		eyebrow,
		eyebrowCode = false,
		subtitle,
		subtitleTone = 'muted',
		subtitleIcon: SubtitleIcon,
		back,
		actions,
		meta,
		display = false,
		sans = false,
		titleLang,
		loading = false
	}: Props = $props();

	const chrome = hasPageChrome() ? getPageChrome() : undefined;
	const backHref = $derived(internalHref(back?.href));
	const skeleton = $derived(
		loading === false
			? undefined
			: { eyebrow: true, title: 2, subtitle: true, ...(loading === true ? {} : loading) }
	);
	const titleBars = $derived(skeleton !== undefined && !title);

	/* What this header last wrote, to tell it from what the next page's header writes. */
	let written: { title?: string; back?: PageChrome['back'] } = {};

	/* Updated in place, never cleared first: the top bar never sees a header without its back. */
	$effect(() => {
		if (!chrome) return;
		chrome.title = titleBars ? undefined : (chromeTitle ?? title);
		chrome.back = back && { href: back.href, label: back.label };
		written = untrack(() => ({ title: chrome.title, back: chrome.back }));
	});

	/* Cleared on unmount, unless the next page's header already wrote its own. */
	$effect(() => {
		if (!chrome) return;
		return () => {
			if (chrome.back === written.back) chrome.back = undefined;
			if (chrome.title === written.title) chrome.title = undefined;
			chrome.collapsed = false;
		};
	});

	/* Collapsed once the title's baseline passes under the sticky top bar. */
	function watch(heading: HTMLElement) {
		if (!chrome) return;
		const observer = new IntersectionObserver(
			([entry]) => {
				chrome.collapsed = !entry.isIntersecting && entry.boundingClientRect.top < topbarHeight;
			},
			{ rootMargin: `-${topbarHeight}px 0px 0px 0px` }
		);
		observer.observe(heading);
		return () => observer.disconnect();
	}
</script>

<header class={['header', { display, sans }]}>
	{#if back && backHref}
		<a class="back" href={backHref}>
			<IconArrowLeft aria-hidden="true" />
			{back.label}
		</a>
	{/if}
	<div class="row">
		<div class="titles">
			{#if eyebrow}
				<p class={['eyebrow', { code: eyebrowCode }]} translate={eyebrowCode ? 'no' : undefined}>
					{eyebrow}
				</p>
			{:else if skeleton?.eyebrow}
				<p class="eyebrow"><Skeleton variant="text" width="min(200px, 45%)" /></p>
			{/if}
			<h1
				class="title"
				tabindex="-1"
				lang={titleBars ? undefined : titleLang}
				aria-busy={titleBars || undefined}
				{@attach watch}
			>
				{#if titleBars}
					<span class="sr-only">{m.common_loading()}</span>
					<Skeleton variant="text" lines={skeleton?.title} width="min(520px, 85%)" />
				{:else}
					{title}
				{/if}
			</h1>
			{#if subtitle}
				<svelte:element
					this={typeof subtitle === 'string' ? 'p' : 'div'}
					class={['subtitle', subtitleTone]}
				>
					{#if SubtitleIcon}<SubtitleIcon class="subtitle-icon" aria-hidden="true" />{/if}
					{#if typeof subtitle === 'string'}{subtitle}{:else}{@render subtitle()}{/if}
				</svelte:element>
			{:else if skeleton?.subtitle}
				<p class="subtitle">
					<Skeleton
						variant="text"
						lines={typeof skeleton.subtitle === 'number' ? skeleton.subtitle : 1}
						width="min(360px, 70%)"
					/>
				</p>
			{/if}
		</div>
		{#if actions}<div class="actions">{@render actions()}</div>{/if}
	</div>
	{#if meta}<div class="meta">{@render meta()}</div>{/if}
</header>

<style>
	.header {
		display: grid;
		grid-template-columns: minmax(0, 1fr);
		gap: 12px;
		padding-block: 8px 4px;
		container-type: inline-size;
	}

	.back {
		display: none;
		align-items: center;
		gap: 6px;
		justify-self: start;
		margin-inline-start: -8px;
		padding: 6px 10px 6px 8px;
		border-radius: var(--r-pill);
		color: var(--text-muted);
		font: var(--type-ui);
		text-decoration: none;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.back :global(svg) {
		inline-size: 16px;
		block-size: 16px;
	}

	@media (width >= 600px) {
		.back {
			display: inline-flex;
		}
	}

	@media (hover: hover) {
		.back:hover {
			background: var(--state-hover);
			color: var(--text);
			transition-duration: 0s;
		}
	}

	.row {
		display: flex;
		flex-wrap: wrap;
		align-items: flex-end;
		justify-content: space-between;
		gap: 16px 24px;
		min-inline-size: 0;
	}

	.titles {
		display: grid;
		gap: 6px;
		min-inline-size: min(100%, 280px);
		flex: 1 1 auto;
	}

	/* Icon buttons alone (and their menus) never take a row of their own. :has() can't nest. */
	.row:has(> .actions > :global(.icon-only)):not(
			:has(> .actions > :not(:global(.icon-only), :global([popover])))
		) {
		flex-wrap: nowrap;
		gap: 16px;
	}

	.row:has(> .actions > :global(.icon-only)):not(
			:has(> .actions > :not(:global(.icon-only), :global([popover])))
		)
		.titles {
		min-inline-size: 0;
	}

	.eyebrow {
		color: var(--accent-text);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.eyebrow.code {
		font-variant-numeric: tabular-nums slashed-zero;
	}

	.title {
		font: var(--type-display-m);
		letter-spacing: var(--track-display-m);
		text-wrap: balance;
		outline: none;
	}

	.display .title {
		font: var(--type-display-l);
		letter-spacing: var(--track-display-l);
	}

	.sans .title {
		font: 650 clamp(1.75rem, 1.4rem + 1.2vw, 2.25rem) / 1.15 var(--font-ui);
		letter-spacing: -0.022em;
	}

	.subtitle {
		max-inline-size: 60ch;
		color: var(--text-muted);
		font-size: 1rem;
	}

	.subtitle.warning {
		color: var(--warning);
	}

	.subtitle.danger {
		color: var(--danger);
	}

	.subtitle :global(.subtitle-icon) {
		display: inline;
		inline-size: 1.125em;
		block-size: 1.125em;
		margin-inline-end: 6px;
		vertical-align: -0.2em;
	}

	.actions {
		display: flex;
		flex-wrap: wrap;
		gap: 8px;
	}

	.actions:empty {
		display: none;
	}

	.meta {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: 8px;
		min-inline-size: 0;
	}
</style>
