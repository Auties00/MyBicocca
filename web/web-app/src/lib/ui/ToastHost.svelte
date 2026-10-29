<script lang="ts">
	import type { Attachment } from 'svelte/attachments';
	import { cubicOut } from 'svelte/easing';
	import type { TransitionConfig } from 'svelte/transition';
	import IconCheckCircle from '~icons/ph/check-circle-fill';
	import IconInfo from '~icons/ph/info-fill';
	import IconWarning from '~icons/ph/warning-fill';
	import IconWarningCircle from '~icons/ph/warning-circle-fill';
	import IconX from '~icons/ph/x';
	import { m } from '#lib/paraglide/messages.js';
	import Button from './Button.svelte';
	import { toast, type Toast, type ToastTone } from './toast.svelte.ts';
	import type { Icon } from './types.ts';

	const glyphs: Record<ToastTone, Icon | undefined> = {
		neutral: undefined,
		info: IconInfo,
		success: IconCheckCircle,
		warning: IconWarning,
		danger: IconWarningCircle
	};

	/** Toasts behind the front one peek out this far. */
	const gap = 14;
	const visible = 3;

	let host = $state<HTMLElement>();
	let expanded = $state(false);
	let pageHidden = $state(false);
	let heights = $state<Record<number, number>>({});
	let swipe = $state<{ id: number; dx: number }>();

	/* Re-entering the top layer keeps new toasts above any dialog opened since. */
	$effect(() => {
		if (!host || toast.items.length === 0) return;
		void toast.items[0].id;
		if (host.matches(':popover-open')) host.hidePopover();
		host.showPopover();
	});

	function offset(index: number): number {
		if (!expanded) return index * gap;
		let sum = 0;
		for (const t of toast.items.slice(0, index)) sum += (heights[t.id] ?? 56) + 8;
		return sum;
	}

	/** Each toast's own timer, paused while the stack is expanded or the page is hidden. */
	function lifetime(item: Toast): Attachment<HTMLElement> {
		return () => {
			if (item.duration === 0) return;
			let remaining = item.duration;
			$effect(() => {
				if (expanded || pageHidden) return;
				const started = performance.now();
				const timer = setTimeout(() => toast.dismiss(item.id), remaining);
				return () => {
					clearTimeout(timer);
					remaining -= performance.now() - started;
				};
			});
		};
	}

	let startX = 0;
	function swipeStart(event: PointerEvent, id: number) {
		if (event.target instanceof Element && event.target.closest('button')) return;
		startX = event.clientX;
		swipe = { id, dx: 0 };
		(event.currentTarget as Element).setPointerCapture(event.pointerId);
	}

	function swipeMove(event: PointerEvent) {
		if (swipe) swipe.dx = event.clientX - startX;
	}

	let lastSwipe: { id: number; dx: number } | undefined;
	function swipeEnd() {
		if (!swipe) return;
		lastSwipe = Math.abs(swipe.dx) > 64 ? { ...swipe } : undefined;
		if (lastSwipe) toast.dismiss(lastSwipe.id);
		swipe = undefined;
	}

	/** Leaves sideways after a swipe, otherwise sinks and fades. */
	function leave(node: HTMLElement, id: number): TransitionConfig {
		const dx = lastSwipe?.id === id ? lastSwipe.dx : 0;
		const scale = Number(getComputedStyle(node).getPropertyValue('--scale')) || 1;
		const y = parseFloat(getComputedStyle(node).getPropertyValue('--y')) || 0;
		return {
			duration: 200,
			easing: cubicOut,
			css: (t) =>
				`opacity: ${t}; transform: translateX(${dx + Math.sign(dx) * (1 - t) * 120}px) translateY(${y + (dx ? 0 : (1 - t) * 12)}px) scale(${scale});`
		};
	}

	function act(item: Toast, run: () => void) {
		run();
		toast.dismiss(item.id);
	}
</script>

<svelte:document onvisibilitychange={() => (pageHidden = document.hidden)} />

<div class="sr-only" aria-live="polite" aria-atomic="true">{toast.announcement}</div>

<section
	bind:this={host}
	class="toaster"
	popover="manual"
	aria-label={m.ui_notifications()}
	onpointerenter={() => (expanded = true)}
	onpointerleave={() => (expanded = false)}
	onfocusin={() => (expanded = true)}
	onfocusout={(e) => {
		if (!host?.contains(e.relatedTarget as Node | null)) expanded = false;
	}}
>
	<ol class={['stack', { expanded }]}>
		{#each toast.items as item, index (item.id)}
			{@const Glyph = glyphs[item.tone]}
			{@const dragging = swipe?.id === item.id}
			<li
				class={['toast', item.tone, { behind: index > 0, hidden: index >= visible, dragging }]}
				style:z-index={toast.items.length - index}
				style:--y="{-offset(index)}px"
				style:--scale={expanded ? 1 : 1 - index * 0.05}
				style:--dx="{dragging ? swipe?.dx : 0}px"
				style:--front-h="{heights[toast.items[0].id] ?? 56}px"
				bind:offsetHeight={heights[item.id]}
				inert={index >= visible}
				out:leave={item.id}
				onpointerdown={(e) => swipeStart(e, item.id)}
				onpointermove={swipeMove}
				onpointerup={swipeEnd}
				onpointercancel={swipeEnd}
				{@attach lifetime(item)}
			>
				{#if Glyph}<span class="glyph"><Glyph aria-hidden="true" /></span>{/if}
				<div class="text">
					<p class="message">{item.message}</p>
					{#if item.description}<p class="description">{item.description}</p>{/if}
				</div>
				{#if item.undo}
					{@const undo = item.undo}
					<Button variant="ghost" size="sm" onclick={() => act(item, undo)}>{m.ui_undo()}</Button>
				{:else if item.action}
					{@const action = item.action}
					<Button variant="ghost" size="sm" onclick={() => act(item, action.onclick)}>
						{action.label}
					</Button>
				{/if}
				<button
					class="close"
					type="button"
					aria-label={m.ui_dismiss()}
					onclick={() => toast.dismiss(item.id)}
				>
					<IconX aria-hidden="true" />
				</button>
			</li>
		{/each}
	</ol>
</section>

<style>
	.toaster {
		position: fixed;
		inset: auto 0 calc(var(--toast-offset, 16px) + var(--safe-bottom)) 0;
		margin: 0 auto;
		inline-size: min(380px, calc(100vw - 24px));
		overflow: visible;
		background: none;
		pointer-events: none;
	}

	@media (width >= 1024px) {
		.toaster {
			inset-inline: auto 24px;
			inset-block-end: 24px;
		}
	}

	.stack {
		position: relative;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.toast {
		position: absolute;
		inset: auto 0 0;
		display: flex;
		align-items: center;
		gap: 10px;
		min-block-size: 52px;
		padding: 8px 8px 8px 14px;
		border-radius: var(--r-md);
		background: var(--surface-raised);
		color: var(--text);
		box-shadow: var(--elev-3), var(--inner-light);
		pointer-events: auto;
		touch-action: pan-y;
		transform: translateX(var(--dx)) translateY(var(--y)) scale(var(--scale));
		transform-origin: center bottom;
		transition:
			transform 400ms var(--ease-out),
			opacity 400ms var(--ease-out);
	}

	@starting-style {
		.toast {
			transform: translateY(calc(100% + 16px));
			opacity: 0;
		}
	}

	.toast:first-child {
		position: relative;
	}

	/* Collapsed, the cards behind take the front card's height so only their top edge shows. */
	.stack:not(.expanded) .behind {
		block-size: var(--front-h);
		overflow: hidden;
	}

	.stack:not(.expanded) .behind > * {
		opacity: 0;
	}

	.hidden {
		opacity: 0;
		pointer-events: none;
	}

	.dragging {
		transition: none;
	}

	.glyph {
		display: grid;
		color: var(--toast-icon, var(--text-muted));
	}

	.glyph :global(svg) {
		inline-size: var(--icon);
		block-size: var(--icon);
	}

	.success {
		--toast-icon: var(--success);
	}

	.info {
		--toast-icon: var(--info);
	}

	.warning {
		--toast-icon: var(--warning);
	}

	.danger {
		--toast-icon: var(--danger);
	}

	.text {
		flex: 1;
		min-inline-size: 0;
	}

	.message {
		font: var(--type-ui-strong);
	}

	.description {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.close {
		display: grid;
		place-items: center;
		flex-shrink: 0;
		inline-size: 28px;
		block-size: 28px;
		border-radius: var(--r-pill);
		color: var(--text-subtle);
	}

	.close :global(svg) {
		inline-size: 16px;
		block-size: 16px;
	}

	@media (hover: hover) {
		.close:hover {
			background: var(--state-hover);
			color: var(--text);
		}
	}
</style>
