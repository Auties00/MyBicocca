<script lang="ts">
	import type { Snippet } from 'svelte';
	import { formatTime } from '#lib/format/dates.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { externalHref, internalHref, type CourseSlot, type Href } from './types.ts';

	interface Props {
		start: Date;
		end?: Date;
		title: string;
		/** Room, building, type: "U6 · Aula 04 · Lezione". */
		subtitle?: string;
		/** Course colour of the bar and fill; neutral without it. */
		course?: CourseSlot;
		/** past fades out; current is marked "In corso". */
		state?: 'past' | 'current' | 'upcoming';
		href?: Href;
		onclick?: () => void;
		/** A status chip or badge on the right ("Esame", "Annullata"). */
		badge?: Snippet;
	}

	let {
		start,
		end,
		title,
		subtitle,
		course,
		state = 'upcoming',
		href,
		onclick,
		badge
	}: Props = $props();

	const internal = $derived(internalHref(href));
	const external = $derived(externalHref(href));
	const classes = $derived([
		'event',
		state,
		{ course: !!course, interactive: !!(href || onclick) }
	]);
</script>

{#snippet content()}
	<span class="bar" aria-hidden="true"></span>
	<span class="main">
		<span class="title">{title}</span>
		{#if subtitle}<span class="subtitle">{subtitle}</span>{/if}
	</span>
	{#if state === 'current'}
		<span class="live">{m.ui_now_happening()}</span>
	{/if}
	{#if badge}<span class="badge">{@render badge()}</span>{/if}
{/snippet}

<li class="row" data-course={course}>
	<span class="time">
		<time datetime={start.toISOString()}>{formatTime(start)}</time>
		{#if end}<time class="end" datetime={end.toISOString()}>{formatTime(end)}</time>{/if}
	</span>
	{#if external}
		<a class={classes} href={external} rel="external noopener noreferrer" target="_blank">
			{@render content()}
		</a>
	{:else if internal}
		<a class={classes} href={internal}>{@render content()}</a>
	{:else if onclick}
		<button type="button" class={classes} {onclick}>{@render content()}</button>
	{:else}
		<div class={classes}>{@render content()}</div>
	{/if}
</li>

<style>
	.row {
		display: grid;
		grid-template-columns: 48px minmax(0, 1fr);
		gap: 12px;
		align-items: stretch;
	}

	.time {
		display: grid;
		align-content: start;
		padding-block-start: 12px;
		font: var(--type-ui-strong);
		font-variant-numeric: tabular-nums;
		text-align: end;
	}

	.end {
		color: var(--text-muted);
		font-weight: 400;
	}

	.event {
		--fill: var(--container);
		--bar: var(--accent);
		--ink: var(--accent-text);
		position: relative;
		display: flex;
		align-items: center;
		gap: 12px;
		min-block-size: 56px;
		padding: 10px 12px 10px 15px;
		border-radius: var(--r-md);
		background: var(--fill);
		color: var(--text);
		text-align: start;
		text-decoration: none;
		overflow: hidden;
		transition:
			transform var(--dur-spring) var(--spring-pop),
			background-color var(--dur-fast) var(--ease-out);
	}

	.course {
		--fill: var(--course-container);
		--bar: var(--course-bar);
		--ink: var(--course-ink);
	}

	.bar {
		position: absolute;
		inset: 0 auto 0 0;
		inline-size: 3px;
		background: var(--bar);
	}

	.past {
		--fill: color-mix(in oklch, var(--container) 35%, var(--surface));
		--bar: var(--g7);
		color: var(--text-muted);
	}

	.past.course {
		--fill: color-mix(in oklch, var(--course-container) 35%, var(--surface));
	}

	.current {
		box-shadow: inset 0 0 0 1.5px color-mix(in oklch, var(--bar) 60%, transparent);
	}

	.current .bar {
		animation: breathe 2.4s var(--ease-in-out) 1;
	}

	.main {
		flex: 1;
		display: grid;
		gap: 2px;
		min-inline-size: 0;
	}

	.title {
		font: var(--type-body-strong);
		font-size: 0.9375rem;
		line-height: 1.3;
	}

	.subtitle {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.live {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		flex-shrink: 0;
		color: var(--ink);
		font: 600 0.6875rem / 1.2 var(--font-ui);
		letter-spacing: 0.05em;
		text-transform: uppercase;
	}

	.live::before {
		content: '';
		inline-size: 6px;
		block-size: 6px;
		border-radius: 50%;
		background: currentColor;
	}

	.interactive {
		cursor: pointer;
		touch-action: manipulation;
	}

	@media (hover: hover) {
		.interactive:hover {
			background: color-mix(in oklch, var(--fill), var(--text) 5%);
			transition-duration: var(--dur-spring), 0s;
		}
	}

	.interactive:active {
		transform: scale(0.985);
		transition-duration: var(--dur-instant), 0s;
	}

	.interactive:focus-visible {
		outline-offset: 2px;
	}

	@container (width < 360px) {
		.row {
			grid-template-columns: 40px minmax(0, 1fr);
			gap: 8px;
		}
	}

	@keyframes breathe {
		50% {
			opacity: 0.35;
		}
	}
</style>
