<script lang="ts">
	import type { Snippet } from 'svelte';
	import { on } from 'svelte/events';
	import { fly } from 'svelte/transition';
	import { m } from '#lib/paraglide/messages.js';
	import { haptics } from '#lib/ui/haptics.svelte.ts';

	interface Props {
		/** Height of the collapsed sheet, in px: the room left under the month grid. */
		collapsed: number;
		/** 0 collapsed … 1 expanded; the page fades the Today button with it. */
		progress?: number;
		children: Snippet<[expanded: boolean]>;
	}

	let { collapsed, progress = $bindable(0), children }: Props = $props();

	/** Android's fling threshold, 800 px/s. */
	const FLING = 0.8;

	let viewport = $state(800);
	let dragging = $state(false);
	let sheet = $state<HTMLElement>();

	const expandedHeight = $derived(Math.round(viewport * 0.79));
	const low = $derived(Math.min(collapsed, expandedHeight - 40));
	const travel = $derived(Math.max(1, expandedHeight - low));
	const expanded = $derived(progress === 1);

	function settle(target: 0 | 1) {
		if (target !== progress && (target === 0 || target === 1)) haptics.feather();
		progress = target;
	}

	function measure() {
		viewport = window.innerHeight;
		return on(window, 'resize', () => (viewport = window.innerHeight));
	}

	/* Collapsed, the whole sheet drags; expanded, only its header does and the list scrolls. */
	function drag(element: HTMLElement) {
		let start: { y: number; progress: number; id: number } | null = null;
		let moved = false;
		let lastY = 0;
		let lastT = 0;
		let velocity = 0;
		const down = (event: PointerEvent) => {
			if (event.button !== 0) return;
			const fromHeader = (event.target as Element).closest('[data-sheet-header]') !== null;
			if (progress === 1 && !fromHeader) return;
			start = { y: event.clientY, progress, id: event.pointerId };
			moved = false;
			lastY = event.clientY;
			lastT = event.timeStamp;
			velocity = 0;
		};
		const move = (event: PointerEvent) => {
			if (!start || event.pointerId !== start.id) return;
			const dy = event.clientY - start.y;
			if (!moved) {
				if (Math.abs(dy) < 6) return;
				moved = true;
				dragging = true;
				element.setPointerCapture(event.pointerId);
			}
			const next = Math.min(1, Math.max(0, start.progress - dy / travel));
			if ((next === 0 || next === 1) && next !== progress) haptics.feather();
			progress = next;
			const dt = event.timeStamp - lastT;
			if (dt > 0) velocity = (event.clientY - lastY) / dt;
			lastY = event.clientY;
			lastT = event.timeStamp;
		};
		const up = (event: PointerEvent) => {
			if (!start || event.pointerId !== start.id) return;
			start = null;
			if (!moved) return;
			dragging = false;
			if (velocity < -FLING) settle(1);
			else if (velocity > FLING) settle(0);
			else settle(progress >= 0.5 ? 1 : 0);
		};
		/* A drag is not a tap: the row under the finger must not open. */
		const click = (event: MouseEvent) => {
			if (moved) {
				event.stopPropagation();
				event.preventDefault();
				moved = false;
			}
		};
		const offs = [
			on(element, 'pointerdown', down),
			on(element, 'pointermove', move),
			on(element, 'pointerup', up),
			on(element, 'pointercancel', up),
			on(element, 'click', click, { capture: true })
		];
		return () => offs.forEach((off) => off());
	}

	function keydown(event: KeyboardEvent) {
		if (event.key === 'Escape' && progress > 0) {
			event.preventDefault();
			settle(0);
		}
	}
</script>

<svelte:window onkeydown={keydown} />

{#if progress > 0}
	<button
		type="button"
		class="scrim"
		data-no-pull
		style:opacity={progress}
		tabindex={-1}
		aria-label={m.calendar_agenda_collapse()}
		onclick={() => settle(0)}
	></button>
{/if}
<div class="presence" data-no-pull transition:fly={{ y: 60, duration: 220 }} {@attach measure}>
	<div
		bind:this={sheet}
		class={['sheet', { dragging, expanded }]}
		style:block-size="{expandedHeight}px"
		style:--offset="{(1 - progress) * travel}px"
		{@attach drag}
	>
		<div class="header" data-sheet-header>
			<button
				type="button"
				class="handle"
				aria-label={expanded ? m.calendar_agenda_collapse() : m.calendar_agenda_expand()}
				aria-expanded={expanded}
				onclick={() => settle(expanded ? 0 : 1)}
			>
				<span aria-hidden="true"></span>
			</button>
		</div>
		<div class="content">
			{@render children(expanded)}
		</div>
	</div>
</div>

<style>
	.scrim {
		position: fixed;
		inset: 0;
		z-index: calc(var(--z-nav) - 2);
		background: var(--scrim);
		cursor: default;
	}

	.presence {
		position: fixed;
		inset-inline: 0;
		inset-block-end: 0;
		z-index: calc(var(--z-nav) - 1);
		pointer-events: none;
	}

	.sheet {
		display: flex;
		flex-direction: column;
		border-radius: var(--r-xl) var(--r-xl) 0 0;
		background: var(--surface-raised);
		box-shadow: var(--elev-3), var(--inner-light);
		transform: translateY(var(--offset));
		transition: transform var(--dur-sheet) var(--spring-standard);
		touch-action: none;
		pointer-events: auto;
		will-change: transform;
	}

	.dragging {
		transition: none;
	}

	.header {
		display: grid;
		place-items: center;
		block-size: 24px;
		flex: none;
		cursor: grab;
	}

	.dragging .header {
		cursor: grabbing;
	}

	.handle {
		display: grid;
		place-items: center;
		inline-size: 64px;
		block-size: 24px;
		cursor: inherit;
	}

	.handle span {
		inline-size: 40px;
		block-size: 4px;
		border-radius: var(--r-pill);
		background: var(--g8);
	}

	.handle:focus-visible {
		outline: 2px solid var(--focus);
		border-radius: var(--r-pill);
	}

	.content {
		flex: 1;
		min-block-size: 0;
		overflow: hidden;
		padding: 4px 20px calc(var(--shell-inset-bottom) + 18px);
	}

	.expanded .content {
		overflow-y: auto;
		overscroll-behavior: contain;
		touch-action: pan-y;
	}
</style>
