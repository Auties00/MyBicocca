<script lang="ts" generics="T extends string">
	import type { Snippet } from 'svelte';
	import { internalHref, type Href, type Icon } from './types.ts';
	import { reducedMotion } from './media.ts';

	interface TabItem {
		value: T;
		label: string;
		/** Route-driven tabs: each tab is a link and `value` marks the current page. */
		href?: Href;
		icon?: Icon;
		/** Small count after the label (e.g. unread posts). */
		count?: number;
	}

	interface Props {
		items: readonly TabItem[];
		value: T;
		/** Accessible name of the tab list or navigation. */
		label: string;
		/** Button tabs only: renders the active panel with the right roles and ids. */
		panel?: Snippet<[T]>;
		onchange?: (value: T) => void;
		/** The tabs share the strip's width equally (a phone fitting five tabs). */
		fill?: boolean;
		/** Smaller labels and paddings. */
		dense?: boolean;
	}

	let {
		items,
		value = $bindable(),
		label,
		panel,
		onchange,
		fill = false,
		dense = false
	}: Props = $props();

	const id = $props.id();
	const links = $derived(items.some((item) => item.href));

	let list = $state<HTMLElement>();
	let strip = $state<HTMLElement>();
	let x = $state(0);
	let w = $state(0);
	let ready = $state(false);

	/*
	 * The indicator follows the active tab: measured, then clipped out of a full-width bar. The
	 * observer's first report (after layout, before paint) takes the measure: read right away, as
	 * a route's new page is being built, it would force that layout.
	 */
	$effect(() => {
		if (!list) return;
		const current = value;
		const measure = () => {
			const active = list?.querySelector<HTMLElement>(`[data-value="${CSS.escape(current)}"]`);
			if (!active) return;
			x = active.offsetLeft;
			w = active.offsetWidth;
			reveal(x, w);
			if (!ready) requestAnimationFrame(() => (ready = true));
		};
		const observer = new ResizeObserver(measure);
		observer.observe(list);
		return () => observer.disconnect();
	});

	/* A strip wider than its box scrolls the current tab into view, without moving the page. */
	function reveal(left: number, width: number) {
		if (!strip || strip.scrollWidth <= strip.clientWidth) return;
		const margin = 24;
		const start = strip.scrollLeft;
		const end = start + strip.clientWidth;
		const target =
			left - margin < start
				? left - margin
				: left + width + margin > end
					? left + width + margin - strip.clientWidth
					: start;
		if (target === start) return;
		const smooth = ready && !reducedMotion.current;
		strip.scrollTo({ left: target, behavior: smooth ? 'smooth' : 'instant' });
	}

	function select(next: T) {
		if (next === value) return;
		value = next;
		onchange?.(next);
	}

	function keydown(event: KeyboardEvent) {
		if (links) return;
		const keys = ['ArrowLeft', 'ArrowRight', 'Home', 'End'];
		if (!keys.includes(event.key)) return;
		event.preventDefault();
		const i = items.findIndex((item) => item.value === value);
		const last = items.length - 1;
		const next =
			event.key === 'Home'
				? 0
				: event.key === 'End'
					? last
					: event.key === 'ArrowRight'
						? (i + 1) % items.length
						: (i - 1 + items.length) % items.length;
		select(items[next].value);
		list?.querySelector<HTMLElement>(`[data-value="${CSS.escape(items[next].value)}"]`)?.focus();
	}
</script>

{#snippet inner(item: TabItem)}
	{#if item.icon}<item.icon aria-hidden="true" />{/if}
	<span class="text">{item.label}</span>
	{#if item.count !== undefined}<span class="count">{item.count}</span>{/if}
{/snippet}

<div class={['tabs', { ready, fill, dense }]} bind:this={strip}>
	{#if links}
		<nav aria-label={label}>
			<ul class="list" role="list" bind:this={list} style:--x="{x}px" style:--w="{w}px">
				{#each items as item (item.value)}
					<li>
						<a
							class={['tab', { active: item.value === value }]}
							href={internalHref(item.href)}
							data-value={item.value}
							aria-current={item.value === value ? 'page' : undefined}
						>
							{@render inner(item)}
						</a>
					</li>
				{/each}
			</ul>
		</nav>
	{:else}
		<div
			class="list"
			role="tablist"
			aria-label={label}
			tabindex="-1"
			bind:this={list}
			style:--x="{x}px"
			style:--w="{w}px"
			onkeydown={keydown}
		>
			{#each items as item (item.value)}
				<button
					type="button"
					class={['tab', { active: item.value === value }]}
					role="tab"
					id="{id}-tab-{item.value}"
					data-value={item.value}
					aria-selected={item.value === value}
					aria-controls={panel ? `${id}-panel-${item.value}` : undefined}
					tabindex={item.value === value ? 0 : -1}
					onclick={() => select(item.value)}
				>
					{@render inner(item)}
				</button>
			{/each}
		</div>
	{/if}
</div>

{#if panel && !links}
	<div
		class="panel"
		role="tabpanel"
		id="{id}-panel-{value}"
		aria-labelledby="{id}-tab-{value}"
		tabindex="0"
	>
		{@render panel(value)}
	</div>
{/if}

<style>
	.tabs {
		position: relative;
		overflow-x: auto;
		scrollbar-width: none;
		overscroll-behavior-x: contain;
		box-shadow: inset 0 -1px 0 var(--hairline);
	}

	.list {
		position: relative;
		display: flex;
		gap: 4px;
		inline-size: max-content;
		min-inline-size: 100%;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.list::after {
		content: '';
		position: absolute;
		inset: auto 0 0 0;
		block-size: 2px;
		background: var(--accent);
		clip-path: inset(0 calc(100% - var(--x) - var(--w)) 0 var(--x) round 2px);
		pointer-events: none;
	}

	.ready .list::after {
		transition: clip-path var(--dur-spring) var(--spring-standard);
	}

	.tab {
		position: relative;
		display: inline-flex;
		align-items: center;
		gap: 6px;
		min-block-size: var(--control-h-lg);
		padding-inline: 12px;
		color: var(--text-muted);
		font: var(--type-ui-strong);
		text-decoration: none;
		white-space: nowrap;
		border-radius: var(--r-sm) var(--r-sm) 0 0;
		touch-action: manipulation;
		transition: color var(--dur-fast) var(--ease-out);
	}

	.tab :global(svg) {
		inline-size: var(--icon-sm);
		block-size: var(--icon-sm);
	}

	.tab:focus-visible {
		outline-offset: -2px;
	}

	@media (hover: hover) {
		.tab:hover {
			color: var(--text);
			transition-duration: 0s;
		}
	}

	.active {
		color: var(--text);
	}

	.count {
		min-inline-size: 20px;
		padding-inline: 6px;
		border-radius: var(--r-pill);
		background: var(--surface-sunken);
		color: var(--text-muted);
		font: var(--type-label);
		font-variant-numeric: tabular-nums;
		text-align: center;
		line-height: 20px;
	}

	.active .count {
		background: var(--container);
		color: var(--on-container);
	}

	.fill .list {
		gap: 0;
	}

	.fill .list > :global(*) {
		display: flex;
		flex: 1 1 auto;
	}

	.fill .tab {
		flex: 1;
		justify-content: center;
		padding-inline: 4px;
	}

	.dense .tab {
		gap: 4px;
		padding-inline: 8px;
		font-size: 0.8125rem;
	}

	.fill.dense .tab {
		padding-inline: 4px;
	}

	.panel {
		padding-block-start: var(--sp-4);
	}

	.panel:focus-visible {
		outline-offset: 4px;
		border-radius: var(--r-xs);
	}
</style>
