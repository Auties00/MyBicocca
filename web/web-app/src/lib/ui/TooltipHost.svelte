<script lang="ts">
	import Kbd from './Kbd.svelte';
	import { tooltipId, tooltipState } from './tooltip.svelte.ts';

	let el = $state<HTMLElement>();
	let x = $state(0);
	let y = $state(0);

	const gap = 8;
	const margin = 8;

	$effect(() => {
		const shown = tooltipState.current;
		if (!el) return;
		if (!shown) {
			if (el.matches(':popover-open')) el.hidePopover();
			return;
		}
		if (!el.matches(':popover-open')) {
			/* Throws while another popover is opening (a menu opened from the keyboard): skip this one. */
			try {
				el.showPopover();
			} catch {
				return;
			}
		}
		const a = shown.anchor.getBoundingClientRect();
		const w = el.offsetWidth;
		const h = el.offsetHeight;
		let place = shown.placement;
		if (place === 'top' && a.top - h - gap < margin) place = 'bottom';
		else if (place === 'bottom' && a.bottom + h + gap > innerHeight - margin) place = 'top';
		else if (place === 'right' && a.right + w + gap > innerWidth - margin) place = 'left';
		else if (place === 'left' && a.left - w - gap < margin) place = 'right';

		let left =
			place === 'left'
				? a.left - w - gap
				: place === 'right'
					? a.right + gap
					: a.left + a.width / 2 - w / 2;
		let top =
			place === 'top'
				? a.top - h - gap
				: place === 'bottom'
					? a.bottom + gap
					: a.top + a.height / 2 - h / 2;
		left = Math.min(Math.max(left, margin), innerWidth - w - margin);
		top = Math.min(Math.max(top, margin), innerHeight - h - margin);
		x = left;
		y = top;
	});
</script>

<svelte:window
	onscroll={() => tooltipState.current && tooltipState.release(tooltipState.current.anchor)}
/>

<div
	bind:this={el}
	id={tooltipId}
	class="tooltip"
	popover="manual"
	role="tooltip"
	style:translate="{x}px {y}px"
>
	{#if tooltipState.current}
		<span>{tooltipState.current.text}</span>
		{#if tooltipState.current.shortcut}
			<Kbd keys={tooltipState.current.shortcut} tone="inverse" />
		{/if}
	{/if}
</div>

<style>
	.tooltip {
		position: fixed;
		inset: 0 auto auto 0;
		margin: 0;
		align-items: center;
		gap: 8px;
		max-inline-size: min(320px, calc(100vw - 16px));
		padding: 6px 10px;
		border-radius: var(--r-xs);
		background: light-dark(var(--g12), var(--g11));
		color: var(--g1);
		font: var(--type-label);
		letter-spacing: 0.01em;
		box-shadow: var(--elev-2);
		pointer-events: none;
		opacity: 0;
		transition:
			opacity var(--dur-fast) var(--ease-out),
			display var(--dur-fast) allow-discrete,
			overlay var(--dur-fast) allow-discrete;
	}

	.tooltip:popover-open {
		display: flex;
		opacity: 1;
	}

	@starting-style {
		.tooltip:popover-open {
			opacity: 0;
		}
	}
</style>
