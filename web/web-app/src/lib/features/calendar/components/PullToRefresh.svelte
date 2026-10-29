<script lang="ts">
	import type { Snippet } from 'svelte';
	import { on } from 'svelte/events';
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import { m } from '#lib/paraglide/messages.js';
	import { haptics } from '#lib/ui/haptics.svelte.ts';
	import Spinner from '#lib/ui/Spinner.svelte';

	interface Props {
		onrefresh: () => Promise<void>;
		children: Snippet;
	}

	let { onrefresh, children }: Props = $props();

	/** How far to pull, in px, before letting go refreshes. */
	const THRESHOLD = 72;

	let pull = $state(0);
	let pulling = $state(false);
	let running = $state(false);

	/*
	 * Only from the top: a pull inside content scrolled down is just scrolling back up. Surfaces
	 * with a drag of their own (`data-no-pull`, the agenda sheet) keep it.
	 */
	function atTop(target: EventTarget | null, root: HTMLElement): boolean {
		if (!(target instanceof Element) || target.closest('[data-no-pull]')) return false;
		for (let node: Element | null = target; node && node !== root; node = node.parentElement) {
			if (node.scrollTop > 0) return false;
		}
		return true;
	}

	function gesture(element: HTMLElement) {
		let startY: number | null = null;
		const offs = [
			on(
				element,
				'touchstart',
				(event) => {
					startY =
						event.touches.length === 1 && !running && atTop(event.target, element)
							? event.touches[0].clientY
							: null;
				},
				{ passive: true }
			),
			on(
				element,
				'touchmove',
				(event) => {
					if (startY === null || event.touches.length !== 1) return;
					const dy = event.touches[0].clientY - startY;
					if (dy <= 0) {
						pull = 0;
						pulling = false;
						return;
					}
					pulling = true;
					pull = Math.min(110, dy * 0.5);
				},
				{ passive: true }
			),
			on(element, 'touchend', async () => {
				startY = null;
				pulling = false;
				if (pull < THRESHOLD) {
					pull = 0;
					return;
				}
				haptics.tap();
				pull = THRESHOLD;
				running = true;
				try {
					await onrefresh();
				} finally {
					running = false;
					pull = 0;
				}
			})
		];
		return () => offs.forEach((off) => off());
	}
</script>

<div class="pull" {@attach gesture}>
	<div
		class={['indicator', { pulling, ready: pull >= THRESHOLD, busy: running }]}
		style:--pull="{pull}px"
		style:--turn={pull / THRESHOLD}
		aria-hidden={!running}
	>
		{#if running}
			<Spinner size={18} label={m.calendar_refreshing()} />
		{:else}
			<IconArrowClockwise aria-hidden="true" />
		{/if}
	</div>
	{@render children()}
</div>

<style>
	.pull {
		position: relative;
		flex: 1;
		display: flex;
		flex-direction: column;
		min-block-size: 0;
	}

	.indicator {
		position: absolute;
		inset-block-start: -40px;
		inset-inline-start: 50%;
		z-index: var(--z-sticky);
		display: grid;
		place-items: center;
		inline-size: 40px;
		block-size: 40px;
		margin-inline-start: -20px;
		border-radius: 50%;
		background: var(--surface-raised);
		box-shadow: var(--elev-2);
		color: var(--accent-text);
		opacity: min(1, var(--turn));
		transform: translateY(var(--pull)) rotate(calc(var(--turn) * 270deg));
		transition:
			transform var(--dur-base) var(--ease-out),
			opacity var(--dur-base) var(--ease-out);
		pointer-events: none;
	}

	.pulling {
		transition: none;
	}

	.indicator :global(svg) {
		inline-size: 20px;
		block-size: 20px;
	}

	.ready {
		color: var(--on-accent);
		background: var(--accent);
	}

	.busy {
		opacity: 1;
		transform: translateY(var(--pull));
		background: var(--surface-raised);
		color: var(--accent-text);
	}
</style>
