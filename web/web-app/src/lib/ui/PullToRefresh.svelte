<script lang="ts" module>
	/* Counted: pull areas nest (a thread in its forum), and only the last one out lets go. */
	let containing = 0;

	function containOverscroll(): () => void {
		if (containing++ === 0) document.documentElement.style.overscrollBehaviorY = 'contain';
		return () => {
			if (--containing === 0) document.documentElement.style.overscrollBehaviorY = '';
		};
	}
</script>

<script lang="ts">
	import type { Snippet } from 'svelte';
	import { on } from 'svelte/events';
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import { m } from '#lib/paraglide/messages.js';
	import { haptics } from './haptics.svelte.ts';
	import { expanded } from './media.ts';
	import Spinner from './Spinner.svelte';

	interface Props {
		/** Runs on release; the spinner stays until it settles and `refreshing` is false again. */
		onrefresh: () => unknown;
		/** A refresh in flight, so a pull that starts one keeps spinning until it lands. */
		refreshing?: boolean;
		/** What the spinner announces. */
		label?: string;
		children: Snippet;
	}

	let { onrefresh, refreshing = false, label = m.common_refreshing(), children }: Props = $props();

	/** How far to pull, in px, before letting go refreshes. */
	const THRESHOLD = 72;

	let pull = $state(0);
	let pulling = $state(false);
	let running = $state(false);
	/* A pull started the refresh: the spinner waits for it, not for one started elsewhere. */
	let pulled = $state(false);
	const busy = $derived(running || (pulled && refreshing));

	$effect(() => {
		if (pulled && !busy) {
			pulled = false;
			pull = 0;
		}
	});

	/*
	 * Only from the top: a pull inside content scrolled down is just scrolling back up. Surfaces
	 * with a drag of their own (`data-no-pull`, the agenda sheet) keep it.
	 */
	function atTop(target: EventTarget | null, root: HTMLElement): boolean {
		if (window.scrollY > 0) return false;
		if (!(target instanceof Element) || target.closest('[data-no-pull]')) return false;
		/* Nested pull areas (a forum inside its course): the innermost one refreshes. */
		if (target.closest('[data-pull]') !== root) return false;
		for (let node: Element | null = target; node && node !== root; node = node.parentElement) {
			if (node.scrollTop > 0) return false;
		}
		return true;
	}

	/*
	 * Phones and tablets, where pages show no refresh button. While attached the page's overscroll
	 * is contained, so the browser's own pull-to-reload, which would reload the whole app, never
	 * starts alongside it.
	 */
	function gesture(element: HTMLElement) {
		const releaseOverscroll = containOverscroll();
		let startX = 0;
		let startY: number | null = null;
		/* Undecided until the finger moves: a sideways swipe (chips, tabs) is left alone. */
		let decided = false;
		const offs = [
			on(
				element,
				'touchstart',
				(event) => {
					decided = false;
					startY =
						event.touches.length === 1 && !busy && atTop(event.target, element)
							? event.touches[0].clientY
							: null;
					startX = event.touches[0]?.clientX ?? 0;
				},
				{ passive: true }
			),
			on(
				element,
				'touchmove',
				(event) => {
					if (startY === null || event.touches.length !== 1) return;
					const dx = event.touches[0].clientX - startX;
					const dy = event.touches[0].clientY - startY;
					if (!decided) {
						if (Math.abs(dx) < 6 && Math.abs(dy) < 6) return;
						decided = true;
						if (dy <= 0 || Math.abs(dx) > Math.abs(dy)) {
							startY = null;
							return;
						}
					}
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
			on(element, 'touchend', () => {
				startY = null;
				void release();
			}),
			on(element, 'touchcancel', () => {
				startY = null;
				pulling = false;
				if (!busy) pull = 0;
			})
		];
		return () => {
			releaseOverscroll();
			offs.forEach((off) => off());
		};
	}

	async function release() {
		if (busy) return;
		const armed = pulling && pull >= THRESHOLD;
		pulling = false;
		if (!armed) {
			pull = 0;
			return;
		}
		haptics.tap();
		pull = THRESHOLD;
		pulled = true;
		running = true;
		try {
			await onrefresh();
		} finally {
			running = false;
		}
	}
</script>

<div class="pull" data-pull {@attach expanded.current ? undefined : gesture}>
	<div
		class={['indicator', { pulling, ready: pull >= THRESHOLD, busy }]}
		style:--pull="{pull}px"
		style:--turn={pull / THRESHOLD}
		aria-hidden={!busy}
	>
		{#if busy}
			<Spinner size={18} {label} />
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
		overflow-x: clip;
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
