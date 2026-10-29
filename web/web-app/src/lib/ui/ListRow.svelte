<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { HTMLAttributes } from 'svelte/elements';
	import IconCaretRight from '~icons/ph/caret-right';
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import IconChip from './IconChip.svelte';
	import {
		externalHref,
		internalHref,
		type ChipTone,
		type CourseSlot,
		type Href,
		type Icon
	} from './types.ts';

	/**
	 * Other attributes (`data-*`, pointer and focus handlers) go on the row's link or button, which
	 * carries `data-row-link`: a `leading` snippet can answer the row's hover with
	 * `:global([data-row-link]:hover) .mine`.
	 */
	interface Props extends Omit<HTMLAttributes<HTMLElement>, 'title' | 'children' | 'onclick'> {
		title: string;
		/** Language of the title when it differs from the UI's (Esse3's Italian names in English). */
		titleLang?: string;
		/** Max lines for the title; `all` shows it whole (long office names). */
		titleLines?: 1 | 2 | 3 | 'all';
		/** Beside the title: a Badge that neither squeezes the description nor grows the row. */
		badge?: Snippet;
		/** Read by screen readers after the title: what the row shows only visually (a grade tile). */
		spoken?: string;
		/** Supporting text under the title; a snippet can colour part of it (a status word). */
		description?: string | Snippet;
		/** Right-aligned secondary value: a date, an amount, a count. */
		meta?: string;
		/** Keeps `meta` on the right in narrow rows too, where it otherwise drops under the title. */
		metaEnd?: boolean;
		/** Icon shown in a tinted chip; use `leading` for an avatar or anything else. */
		icon?: Icon;
		iconTone?: ChipTone;
		/** Tints the icon chip with a course colour. */
		course?: CourseSlot;
		leading?: Snippet;
		/** Right side, inside the link: a status chip. Replaces the chevron unless `chevron`. */
		trailing?: Snippet;
		/**
		 * The chevron of a link or button row. By default it shows unless `trailing` is given;
		 * true keeps it next to `trailing`, false drops it.
		 */
		chevron?: boolean;
		/** A Switch or Checkbox: the whole row becomes its label and toggles it. */
		control?: Snippet;
		/** Buttons beside the row's link, outside it (Retry, Share, Download). */
		actions?: Snippet;
		href?: Href;
		onclick?: (event: MouseEvent) => void;
		/** The open item of a list-detail list. */
		selected?: boolean;
		/** Lit from elsewhere: its pin pointed at on the map. */
		highlighted?: boolean;
		/** Out of reach for now (closed, locked) yet still a link: the title in the muted ink. */
		muted?: boolean;
		disabled?: boolean;
		/** 0–1: paints a tonal fill from the left behind the row (attendance, completion). */
		progress?: number;
		/** Max lines for the description. */
		lines?: 1 | 2 | 3;
		/**
		 * A list beside an open detail: one line each for title and description, `meta` kept on
		 * the right, no chevron.
		 */
		dense?: boolean;
		/** The row's link, button or label, e.g. to give the focus back to it. */
		element?: HTMLElement;
	}

	let {
		title,
		titleLang,
		titleLines = 2,
		badge,
		spoken,
		description,
		meta,
		metaEnd = false,
		icon,
		iconTone = 'neutral',
		course,
		leading,
		trailing,
		chevron,
		control,
		actions,
		href,
		onclick,
		selected = false,
		highlighted = false,
		muted = false,
		disabled = false,
		progress,
		lines = 2,
		dense = false,
		element = $bindable(),
		...rest
	}: Props = $props();

	const internal = $derived(disabled ? undefined : internalHref(href));
	const external = $derived(disabled ? undefined : externalHref(href));
	const interactive = $derived(!!(internal || external || onclick || control));
	const classes = $derived(['inner', { interactive, 'meta-end': metaEnd || dense }]);
	const caret = $derived(
		!dense && chevron !== false && !external && !!(internal || onclick) && (chevron || !trailing)
	);
	const clamp = $derived(dense ? 1 : titleLines === 'all' ? 'none' : titleLines);
</script>

{#snippet content()}
	{#if leading}
		<span class="leading">{@render leading()}</span>
	{:else if icon}
		<span class="leading"><IconChip {icon} tone={iconTone} {course} /></span>
	{/if}
	<span class="main">
		<span class="head">
			<span class="title" lang={titleLang} style:--title-lines={clamp}>{title}</span>
			{#if badge}<span class="badge">{@render badge()}</span>{/if}
		</span>
		{#if spoken}<span class="sr-only">{spoken}</span>{/if}
		{#if description}
			<span class="description" style:--lines={dense ? 1 : lines}>
				{#if typeof description === 'string'}{description}{:else}{@render description()}{/if}
			</span>
		{/if}
	</span>
	{#if meta}<span class="meta">{meta}</span>{/if}
	{#if control}
		<span class="trailing">{@render control()}</span>
	{:else if trailing}
		<span class="trailing">{@render trailing()}</span>
	{/if}
	{#if external && chevron !== false && !dense}
		<span class="chevron"><IconArrowSquareOut aria-hidden="true" /></span>
	{:else if caret}
		<span class="chevron"><IconCaretRight aria-hidden="true" /></span>
	{/if}
{/snippet}

<li
	class={[
		'row',
		{
			selected,
			highlighted,
			muted,
			disabled,
			'has-progress': progress !== undefined,
			'has-actions': !!actions
		}
	]}
	style:--progress={progress === undefined ? undefined : Math.min(1, Math.max(0, progress))}
>
	{#if external}
		<a
			{...rest}
			class={classes}
			href={external}
			rel="external noopener noreferrer"
			target="_blank"
			data-row-link
			bind:this={element}
		>
			{@render content()}
		</a>
	{:else if internal}
		<a
			{...rest}
			class={classes}
			href={internal}
			aria-current={selected ? 'page' : undefined}
			data-row-link
			bind:this={element}
		>
			{@render content()}
		</a>
	{:else if control}
		<label {...rest} class={classes} data-row-link bind:this={element}>
			{@render content()}
		</label>
	{:else if onclick}
		<button
			{...rest}
			type="button"
			class={classes}
			{onclick}
			{disabled}
			aria-pressed={selected || undefined}
			data-row-link
			bind:this={element}
		>
			{@render content()}
		</button>
	{:else}
		<div {...rest} class={classes} aria-disabled={disabled || undefined} bind:this={element}>
			{@render content()}
		</div>
	{/if}
	{#if actions}<span class="actions">{@render actions()}</span>{/if}
</li>

<style>
	/* --row-fill: a list inside a card tints its rows apart from the card. */
	.row {
		position: relative;
		background: var(--row-fill, var(--surface));
		box-shadow: var(--inner-light);
		container-type: inline-size;
		overflow: clip;
	}

	.has-progress::before {
		content: '';
		position: absolute;
		inset: 0;
		background: var(--container);
		transform: scaleX(var(--progress));
		transform-origin: left;
		transition: transform var(--dur-slow) var(--ease-out);
	}

	@starting-style {
		.has-progress::before {
			transform: scaleX(0);
		}
	}

	.inner {
		position: relative;
		display: grid;
		grid-template-columns: auto minmax(0, 1fr) auto auto auto;
		grid-template-areas: 'leading main meta trailing chevron';
		align-items: center;
		column-gap: 12px;
		inline-size: 100%;
		min-block-size: 60px;
		padding: 10px 16px;
		border-radius: inherit;
		color: var(--text);
		text-align: start;
		text-decoration: none;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.interactive {
		cursor: pointer;
		touch-action: manipulation;
	}

	.inner:focus-visible {
		outline-offset: -2px;
	}

	@media (hover: hover) {
		.interactive:hover {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	.interactive:active {
		background: var(--state-press);
		transition-duration: 0s;
	}

	/*
	 * With actions the link ends before them: the row as a whole takes the hover and press fills,
	 * laid over its own.
	 */
	.has-actions {
		display: grid;
		grid-template-columns: minmax(0, 1fr) auto;
		align-items: center;
	}

	.has-actions > .inner {
		padding-inline-end: 4px;
		background: none;
	}

	@media (hover: hover) {
		.has-actions:not(.selected):has(> .interactive:hover) {
			background-image: linear-gradient(var(--state-hover));
		}
	}

	.has-actions:not(.selected):has(> .interactive:active) {
		background-image: linear-gradient(var(--state-press));
	}

	.actions {
		position: relative;
		display: flex;
		align-items: center;
		gap: 2px;
		padding-inline-end: 8px;
	}

	.highlighted {
		background: color-mix(in oklch, var(--container) 70%, var(--surface));
	}

	.selected {
		background: var(--container);
	}

	.muted .title {
		color: var(--text-muted);
	}

	.selected .inner,
	.selected .actions {
		color: var(--on-container);
	}

	/* Softened by mixing rather than opacity, so a coloured word in a description keeps its tone. */
	.selected .description,
	.selected .meta,
	.selected .chevron {
		color: color-mix(in oklch, var(--on-container) 80%, transparent);
	}

	/* A chip tinted like the selected row's fill would vanish into it. */
	.selected .leading :global(.chip) {
		--chip-bg: var(--surface);
	}

	.disabled {
		opacity: 0.55;
	}

	.leading {
		grid-area: leading;
		display: flex;
	}

	.main {
		grid-area: main;
		display: grid;
		gap: 2px;
		min-inline-size: 0;
		padding-block: 4px;
	}

	.head {
		display: flex;
		align-items: center;
		gap: 8px;
		min-inline-size: 0;
	}

	.title {
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: var(--title-lines);
		line-clamp: var(--title-lines);
		overflow: hidden;
		min-inline-size: 0;
		font: var(--type-row-title);
		letter-spacing: var(--track-row-title);
	}

	.badge {
		display: flex;
		flex: none;
	}

	.description {
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: var(--lines);
		line-clamp: var(--lines);
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}

	.meta {
		grid-area: meta;
		color: var(--text-muted);
		font: var(--type-ui);
		font-variant-numeric: tabular-nums;
		white-space: nowrap;
		text-align: end;
	}

	.trailing {
		grid-area: trailing;
		display: flex;
		align-items: center;
		gap: 8px;
	}

	.chevron {
		grid-area: chevron;
		display: flex;
		color: var(--text-subtle);
		margin-inline-end: -4px;
	}

	.chevron :global(svg) {
		inline-size: 18px;
		block-size: 18px;
	}

	@media (pointer: fine) {
		.inner {
			min-block-size: 52px;
			padding-block: 8px;
		}
	}

	@container (width < 380px) {
		.inner:not(.meta-end) {
			grid-template-columns: auto minmax(0, 1fr) auto auto;
			grid-template-areas:
				'leading main trailing chevron'
				'leading meta trailing chevron';
		}

		.inner:not(.meta-end) .meta {
			justify-self: start;
			margin-block: -4px 4px;
			font: var(--type-meta);
		}
	}
</style>
