<script lang="ts" module>
	export type Detent = 'peek' | 'half' | 'full';
</script>

<script lang="ts">
	import { onDestroy, type Snippet } from 'svelte';
	import { m } from '#lib/paraglide/messages.js';
	import { setSheetHead } from './sheetHead.ts';

	interface Props {
		children: Snippet;
		label: string;
		detent: Detent;
		heights: Record<Detent, number>;
		/** How much of the sheet shows: the detent's height, or where a drag holds it. */
		visible: number;
		/** The height a drag holds the sheet at; undefined when it lets go. */
		ondrag: (height: number | undefined) => void;
		ondetent: (detent: Detent) => void;
		/** The height of the shown level's head, which the lowered sheet keeps in view. */
		onhead: (height: number) => void;
	}

	let { children, label, detent, heights, visible, ondrag, ondetent, onhead }: Props = $props();

	setSheetHead((node) => {
		const observer = new ResizeObserver(() => onhead(node.offsetHeight));
		observer.observe(node);
		return () => observer.disconnect();
	});

	const order: readonly Detent[] = ['peek', 'half', 'full'];

	let dragging = $state(false);
	let start = 0;
	let origin = 0;
	let lastY = 0;
	let lastT = 0;
	let velocity = 0;
	let pointer: number | undefined;
	let moved = false;

	/** Where a drag may start: anywhere while lowered, only the grip and header when raised. */
	function grabs(event: PointerEvent): boolean {
		if (event.button !== 0 || !(event.target instanceof Element)) return false;
		if (event.target.closest('input, textarea, select')) return false;
		return detent !== 'full' || !!event.target.closest('[data-grip], [data-level-head]');
	}

	/* The drag is followed on the window, so it goes on when the finger leaves the sheet. */
	function pointerdown(event: PointerEvent) {
		if (!grabs(event)) return;
		pointer = event.pointerId;
		start = lastY = event.clientY;
		lastT = event.timeStamp;
		origin = heights[detent];
		velocity = 0;
		moved = false;
		window.addEventListener('pointermove', pointermove);
		window.addEventListener('pointerup', pointerup);
		window.addEventListener('pointercancel', pointerup);
	}

	function release() {
		pointer = undefined;
		window.removeEventListener('pointermove', pointermove);
		window.removeEventListener('pointerup', pointerup);
		window.removeEventListener('pointercancel', pointerup);
	}

	onDestroy(release);

	function pointermove(event: PointerEvent) {
		if (event.pointerId !== pointer) return;
		const dy = event.clientY - start;
		if (!moved) {
			if (Math.abs(dy) < 6) return;
			moved = true;
			dragging = true;
		}
		const height = origin - dy;
		const max = heights.full;
		const min = heights.peek;
		/* Past the end detents the sheet resists, as if on a spring. */
		ondrag(
			height > max
				? max + Math.sqrt(height - max) * 2
				: height < min
					? min - Math.sqrt(min - height) * 2
					: height
		);
		const dt = event.timeStamp - lastT;
		if (dt > 0) velocity = (event.clientY - lastY) / dt;
		lastY = event.clientY;
		lastT = event.timeStamp;
	}

	function pointerup(event: PointerEvent) {
		if (event.pointerId !== pointer) return;
		release();
		if (!moved || !dragging) return;
		const height = visible;
		dragging = false;
		ondrag(undefined);
		/* A flick moves one detent its way; a slow drag settles on the nearest. */
		const index = order.indexOf(detent);
		let next: Detent;
		if (Math.abs(velocity) > 0.5) {
			next = order[Math.max(0, Math.min(order.length - 1, index + (velocity < 0 ? 1 : -1)))];
		} else {
			next = order.reduce((best, candidate) =>
				Math.abs(heights[candidate] - height) < Math.abs(heights[best] - height) ? candidate : best
			);
		}
		ondetent(next);
	}

	/* A drag must not also activate the row it started on. */
	function click(event: MouseEvent) {
		if (moved) {
			event.preventDefault();
			event.stopPropagation();
			moved = false;
		}
	}

	/*
	 * Typing a search, or reaching the list from the keyboard, raises the sheet to show it. A tap
	 * also focuses the row it lands on, but it keeps the sheet where it is: only keyboard focus
	 * (:focus-visible) counts.
	 */
	function focusin(event: FocusEvent) {
		const target = event.target;
		if (detent === 'full' || !(target instanceof Element)) return;
		if (target.closest('input')) ondetent('full');
		else if (
			target.matches(':focus-visible') &&
			!target.closest('[data-grip], [data-level-head]')
		) {
			ondetent('full');
		}
	}

	function wheel(event: WheelEvent) {
		if (detent !== 'full' && event.deltaY > 0) ondetent('full');
	}

	function toggle() {
		ondetent(detent === 'full' ? 'half' : detent === 'half' ? 'full' : 'half');
	}
</script>

<section
	class={['sheet', detent, { dragging }]}
	aria-label={label}
	style:--full={`${heights.full}px`}
	style:--shown={`${visible}px`}
	onpointerdown={pointerdown}
	onclickcapture={click}
	onfocusin={focusin}
	onwheel={wheel}
>
	<div class="grip" data-grip>
		<button
			type="button"
			class="handle"
			aria-label={detent === 'full' ? m.map_sheet_collapse() : m.map_sheet_expand()}
			aria-expanded={detent === 'full'}
			onclick={toggle}
		>
			<span class="bar"></span>
		</button>
	</div>
	<div class="content">
		{@render children()}
	</div>
</section>

<style>
	.sheet {
		position: absolute;
		inset-inline: 0;
		inset-block-end: 0;
		z-index: 2;
		display: flex;
		flex-direction: column;
		block-size: var(--full);
		border-radius: var(--r-xl) var(--r-xl) 0 0;
		background: var(--bg);
		box-shadow:
			0 0 0 1px var(--hairline),
			0 -8px 32px -12px color-mix(in oklch, var(--shadow-color) 28%, transparent),
			var(--inner-light);
		transform: translateY(calc(var(--full) - var(--shown)));
		transition: transform var(--dur-sheet) var(--ease-sheet);
		touch-action: none;
		--level-end: calc(var(--shell-inset-bottom) + 24px);
	}

	.dragging {
		transition: none;
	}

	/* Lowered, only the header shows: the body would peek around the floating tab bar. */
	.peek:not(.dragging) {
		--level-body-opacity: 0;
	}

	.full .content {
		touch-action: pan-y;
	}

	.grip {
		display: flex;
		justify-content: center;
		flex: none;
	}

	.handle {
		display: grid;
		place-items: center;
		inline-size: 64px;
		block-size: 24px;
		border-radius: var(--r-pill);
		cursor: grab;
		touch-action: none;
	}

	.handle:focus-visible {
		outline-offset: -4px;
	}

	.bar {
		inline-size: 36px;
		block-size: 4px;
		border-radius: 2px;
		background: var(--border-strong);
	}

	.content {
		flex: 1;
		min-block-size: 0;
	}
</style>
