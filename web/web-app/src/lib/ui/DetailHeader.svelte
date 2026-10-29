<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { ResolvedPathname } from '$app/types';
	import IconCaretDown from '~icons/ph/caret-down';
	import IconCaretUp from '~icons/ph/caret-up';
	import IconX from '~icons/ph/x';
	import { m } from '#lib/paraglide/messages.js';
	import IconButton from './IconButton.svelte';
	import { getDetailControls, hasDetailControls } from './ListDetail.svelte';
	import Skeleton from './Skeleton.svelte';

	interface Props {
		/** Empty while it loads, with `loading`. */
		title: string;
		/** Language of the title when it differs from the UI's (Esse3's Italian names). */
		titleLang?: string;
		/** Small line above the title: a year, a course code. */
		eyebrow?: string;
		/** The eyebrow is a code: never translated, tabular figures. */
		eyebrowCode?: boolean;
		subtitle?: string | Snippet;
		/** The heading's level: 2 beside the page's h1. */
		level?: 2 | 3;
		/** The heading's id, for the item's region to be labelled by it. */
		id?: string;
		/**
		 * Bars stand in for the texts not given yet: `eyebrow: false` for an item that has none,
		 * `actions: true` to hold the place of a button that comes with the item.
		 */
		loading?: boolean | { eyebrow?: boolean; subtitle?: boolean; actions?: boolean };
		/** Buttons for the item (Download, Open in e-learning), on a row under the titles. */
		actions?: Snippet;
		/** Under everything else: facts, a byline, the item's tools. */
		children?: Snippet;
		/**
		 * The neighbours to step to, for a list that keeps its own order; by default the
		 * surrounding ListDetail's, from its `items`.
		 */
		steps?: { previous?: ResolvedPathname; next?: ResolvedPathname };
		/** False for the pane the list shows by default (a form), which closing would leave in place. */
		closable?: boolean;
	}

	let {
		title,
		titleLang,
		eyebrow,
		eyebrowCode = false,
		subtitle,
		level = 2,
		id,
		loading = false,
		actions,
		children,
		steps: ownSteps,
		closable = true
	}: Props = $props();

	const controls = hasDetailControls() ? getDetailControls() : undefined;
	const steps = $derived(ownSteps ?? controls?.steps);
	const closeHref = $derived(closable ? controls?.closeHref : undefined);
	const skeleton = $derived(
		loading === false
			? undefined
			: { eyebrow: true, subtitle: true, actions: false, ...(loading === true ? {} : loading) }
	);
	const titleBars = $derived(skeleton !== undefined && !title);

	function step(href: ResolvedPathname | undefined, event: MouseEvent) {
		if (href) controls?.step(href, event.currentTarget as HTMLElement);
	}

	/* A plain click closes as Escape does; a modified one opens the list where the browser says. */
	function close(event: MouseEvent) {
		if (!controls || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey) return;
		event.preventDefault();
		controls.close();
	}
</script>

<!--
	The heading of an item open beside its list, which moves focus to it when it opens. Alone on
	one pane the item is the page, and the page's header names it: in a ListDetail this one shows
	only on two panes, decided by the same container query as the panes, so never a frame late.
	Paper has no room for two panes; there the layout that was on screen decides.
-->
<header
	class={['detail-header', { pane: controls !== undefined, beside: controls?.twoPanes === true }]}
>
	<div class="top">
		<div class="titles">
			{#if eyebrow}
				<p class={['eyebrow', { code: eyebrowCode }]} translate={eyebrowCode ? 'no' : undefined}>
					{eyebrow}
				</p>
			{:else if skeleton?.eyebrow}
				<p class="eyebrow"><Skeleton variant="text" width="min(180px, 40%)" /></p>
			{/if}
			<svelte:element
				this={`h${level}`}
				{id}
				class="title"
				tabindex="-1"
				data-detail-focus
				lang={titleBars ? undefined : titleLang}
				aria-busy={titleBars || undefined}
			>
				{#if titleBars}
					<span class="sr-only">{m.common_loading()}</span>
					<Skeleton variant="text" width="min(420px, 70%)" />
				{:else}
					{title}
				{/if}
			</svelte:element>
			{#if subtitle}
				<svelte:element this={typeof subtitle === 'string' ? 'p' : 'div'} class="subtitle">
					{#if typeof subtitle === 'string'}{subtitle}{:else}{@render subtitle()}{/if}
				</svelte:element>
			{:else if skeleton?.subtitle}
				<p class="subtitle"><Skeleton variant="text" width="min(260px, 50%)" /></p>
			{/if}
		</div>
		{#if steps || closeHref}
			<div class="controls">
				{#if steps}
					<IconButton
						icon={IconCaretUp}
						label={m.ui_previous()}
						shortcut={['K']}
						data-detail-step="previous"
						disabled={!steps.previous}
						onclick={(event) => step(steps?.previous, event)}
					/>
					<IconButton
						icon={IconCaretDown}
						label={m.ui_next()}
						shortcut={['J']}
						data-detail-step="next"
						disabled={!steps.next}
						onclick={(event) => step(steps?.next, event)}
					/>
				{/if}
				{#if closeHref}
					<IconButton
						icon={IconX}
						label={m.ui_close()}
						shortcut={['Esc']}
						href={closeHref}
						onclick={close}
					/>
				{/if}
			</div>
		{/if}
	</div>
	{#if actions || skeleton?.actions}
		<div class="actions">
			{#if skeleton?.actions}
				<Skeleton width="11rem" height="var(--control-h-sm)" radius="var(--r-sm)" />
			{:else}
				{@render actions?.()}
			{/if}
		</div>
	{/if}
	{@render children?.()}
</header>

<style>
	.detail-header {
		display: grid;
		gap: var(--sp-3);
		padding-block: var(--sp-1) var(--sp-2);
	}

	.pane {
		display: none;
	}

	@media (width >= 1024px) {
		@container list-detail (width >= 1em) {
			.pane {
				display: grid;
			}
		}
	}

	.top {
		display: flex;
		align-items: flex-start;
		gap: var(--sp-4);
	}

	.titles {
		display: grid;
		flex: 1;
		gap: 4px;
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

	/* The UI face: the page's title above is the view's serif line. */
	.title {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
		text-wrap: balance;
		overflow-wrap: anywhere;
		outline: none;
	}

	.subtitle {
		color: var(--text-muted);
		font: var(--type-ui);
		font-variant-numeric: tabular-nums;
		text-wrap: pretty;
	}

	/* Beside the first line of text: the eyebrow's, else the title's. */
	.controls {
		display: flex;
		flex: none;
		gap: 2px;
		margin-block-start: -6px;
		margin-inline-end: -8px;
	}

	.actions {
		display: flex;
		flex-wrap: wrap;
		align-items: center;
		gap: var(--sp-2);
	}

	.actions:empty {
		display: none;
	}

	@media print {
		.pane.beside {
			display: grid;
		}

		.controls {
			display: none;
		}
	}
</style>
