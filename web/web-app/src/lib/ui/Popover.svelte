<script lang="ts" module>
	export type PopoverPlacement =
		'bottom-start' | 'bottom-end' | 'top-start' | 'top-end' | 'right-start' | 'right-end';

	/** Spread these on the trigger element (a Button or a native button). */
	export interface PopoverTriggerProps {
		onclick: (event: MouseEvent) => void;
		'aria-expanded': boolean;
		'aria-controls': string;
		'aria-haspopup': 'menu' | 'dialog';
		style: string;
	}
</script>

<script lang="ts">
	import { flushSync, type Snippet } from 'svelte';

	interface Props {
		/** Renders the opener; spread the given props on it. */
		trigger: Snippet<[PopoverTriggerProps]>;
		children: Snippet;
		open?: boolean;
		placement?: PopoverPlacement;
		/** Accessible name of the popover surface. */
		label?: string;
		/** menu: the content is a role=menu list (see Menu). */
		kind?: 'dialog' | 'menu';
		/** CSS width of the surface. */
		width?: string;
		/** After opening, with the surface element: move focus here. */
		onopen?: (surface: HTMLElement) => void;
	}

	let {
		trigger,
		children,
		open = $bindable(false),
		placement = 'bottom-start',
		label,
		kind = 'dialog',
		width,
		onopen
	}: Props = $props();

	const id = $props.id();
	const anchor = `--popover-${id}`;
	const anchored = typeof CSS !== 'undefined' && CSS.supports('anchor-name', '--a');

	let surface = $state<HTMLElement>();
	/* The content is built on first open and kept: a row's menu costs nothing until it opens. */
	let built = $state(false);
	let opener: HTMLElement | null = null;
	let closedAt = 0;
	let x = $state<number>();
	let y = $state<number>();

	/* A click on the trigger first light-dismisses the open popover; don't reopen it right after. */
	const reopenGuard = 250;

	function toggle(event: MouseEvent) {
		if (!surface) return;
		if (!open && performance.now() - closedAt < reopenGuard) return;
		opener = event.currentTarget instanceof HTMLElement ? event.currentTarget : null;
		if (open) {
			surface.hidePopover();
			return;
		}
		/* In the DOM before it shows, for `onopen` to focus inside it. */
		if (!built) {
			built = true;
			flushSync();
		}
		showFrom(opener);
	}

	function showFrom(source: HTMLElement | null) {
		if (!surface) return;
		if (!anchored && source) place(source);
		try {
			surface.showPopover(source ? { source } : undefined);
		} catch {
			surface.showPopover();
		}
	}

	/* Without CSS anchor positioning: place it once from the trigger's box. */
	function place(source: HTMLElement) {
		if (!surface) return;
		const r = source.getBoundingClientRect();
		const gap = 6;
		const [side, align] = placement.split('-');
		if (side === 'bottom') y = r.bottom + gap;
		else if (side === 'top') y = r.top - gap;
		else y = align === 'start' ? r.top : r.bottom;
		if (side === 'right') x = r.right + gap;
		else x = align === 'start' ? r.left : r.right;
	}

	function ontoggle(event: ToggleEvent) {
		open = event.newState === 'open';
		if (open && surface) onopen?.(surface);
		else closedAt = performance.now();
	}

	$effect(() => {
		if (!surface) return;
		const shown = surface.matches(':popover-open');
		if (open && !shown) {
			/* Opened from outside: build the content first, and show it once that has rendered. */
			if (built) showFrom(opener);
			else built = true;
		} else if (!open && shown) surface.hidePopover();
	});

	const triggerProps: PopoverTriggerProps = $derived({
		onclick: toggle,
		'aria-expanded': open,
		'aria-controls': id,
		'aria-haspopup': kind,
		style: `anchor-name: ${anchor}`
	});
</script>

{@render trigger(triggerProps)}

<div
	bind:this={surface}
	{id}
	class={['popover', placement, { manual: !anchored }]}
	popover="auto"
	role={kind === 'menu' ? 'presentation' : 'dialog'}
	aria-label={kind === 'menu' ? undefined : label}
	style:position-anchor={anchor}
	style:inline-size={width}
	style:--x={x === undefined ? undefined : `${x}px`}
	style:--y={y === undefined ? undefined : `${y}px`}
	{ontoggle}
>
	{#if built}{@render children()}{/if}
</div>

<style>
	.popover {
		position: fixed;
		inset: auto;
		margin: 6px;
		min-inline-size: 220px;
		max-inline-size: min(360px, calc(100vw - 16px));
		max-block-size: calc(100dvh - 32px);
		overflow: auto;
		overscroll-behavior: contain;
		padding: 6px;
		border-radius: var(--r-lg);
		background: var(--surface-raised);
		color: var(--text);
		box-shadow: var(--elev-3), var(--inner-light);
		position-try-fallbacks: flip-block, flip-inline;
		opacity: 0;
		transform: scale(0.97) translateY(var(--from-y, -4px));
		transform-origin: var(--origin, top left);
		transition:
			opacity 120ms var(--ease-out),
			transform 120ms var(--ease-out),
			overlay 120ms allow-discrete,
			display 120ms allow-discrete;
	}

	.popover:popover-open {
		opacity: 1;
		transform: none;
		transition-duration: 180ms;
		transition-timing-function: var(--ease-out-expo);
	}

	@starting-style {
		.popover:popover-open {
			opacity: 0;
			transform: scale(0.97) translateY(var(--from-y, -4px));
		}
	}

	.bottom-start {
		position-area: block-end span-inline-end;
	}

	.bottom-end {
		position-area: block-end span-inline-start;
		--origin: top right;
	}

	.top-start {
		position-area: block-start span-inline-end;
		--origin: bottom left;
		--from-y: 4px;
	}

	.top-end {
		position-area: block-start span-inline-start;
		--origin: bottom right;
		--from-y: 4px;
	}

	.right-start {
		position-area: inline-end span-block-end;
	}

	.right-end {
		position-area: inline-end span-block-start;
		--origin: bottom left;
		--from-y: 4px;
	}

	.manual {
		margin: 0;
		inset: var(--y) auto auto var(--x);
	}

	.manual.bottom-end,
	.manual.top-end {
		translate: -100% 0;
	}

	.manual.top-start,
	.manual.top-end,
	.manual.right-end {
		translate: var(--tx, 0) -100%;
	}

	.manual.top-end {
		--tx: -100%;
	}
</style>
