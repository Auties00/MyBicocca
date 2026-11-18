<script lang="ts">
	import type { Snippet } from 'svelte';
	import { getPageChrome, hasPageChrome } from './pageChrome.svelte.ts';

	interface Props {
		children: Snippet;
		/**
		 * reading ≈ 720 px (detail pages, forms, settings); dashboard ≈ 1200 px (hubs, grids);
		 * full = edge to edge (calendar, map, list-detail).
		 */
		width?: 'reading' | 'dashboard' | 'full';
		/** Removes the side gutters too (full-bleed canvases such as the map). */
		bleed?: boolean;
		/**
		 * `true`: exactly the height left under the top bar, with no padding for the phone tab bar; the
		 * page scrolls inside itself and keeps clear of the bar with `--shell-inset-bottom` (calendar).
		 * `'viewport'`: the whole viewport, under the floating bars on phones too, with no shell padding
		 * and no scrollbar gutter (map); floating UI keeps clear with `--shell-inset-top` and
		 * `--shell-inset-bottom`.
		 */
		fill?: boolean | 'viewport';
		/** `false`: a focused flow (editor, multi-step form) hides the phone tab bar; the top bar and Back stay. */
		tabBar?: boolean;
		/** A navigation that changes only the search params (`?open=…`) moves focus to the heading too. */
		focusOnQuery?: boolean;
	}

	let {
		children,
		width = 'dashboard',
		bleed = false,
		fill = false,
		tabBar = true,
		focusOnQuery = false
	}: Props = $props();

	const chrome = hasPageChrome() ? getPageChrome() : undefined;

	$effect(() => {
		if (!tabBar) return chrome?.holdTabBarHidden();
	});
</script>

<div
	class={['page', width, { bleed, fill: fill !== false }]}
	data-page-fill={fill === false ? undefined : fill === true ? '' : fill}
	data-focus-on-query={focusOnQuery || undefined}
>
	{@render children()}
</div>

<style>
	.page {
		--page-gap: var(--sp-6);
		display: flex;
		flex-direction: column;
		gap: var(--page-gap);
		inline-size: 100%;
		max-inline-size: calc(var(--max) + 2 * var(--gutter));
		margin-inline: auto;
		padding: var(--sp-4) var(--gutter) var(--sp-9);
		container: page / inline-size;
	}

	@media (width >= 1024px) {
		.page {
			--page-gap: var(--sp-7);
			padding-block-start: var(--sp-6);
		}
	}

	.reading {
		--max: var(--w-reading);
	}

	.dashboard {
		--max: var(--w-dashboard);
	}

	.full {
		--max: 100vw;
	}

	.bleed {
		padding: 0;
		gap: 0;
	}

	/* The shell sizes its content area to the viewport around a filling page (AppShell). */
	.fill {
		flex: 1;
		min-block-size: 0;
	}
</style>
