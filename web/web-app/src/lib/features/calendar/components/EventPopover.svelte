<script lang="ts">
	import type { MoodleEnrolledCourse } from '@mybicocca/data-api/elearning';
	import { on } from 'svelte/events';
	import { m } from '#lib/paraglide/messages.js';
	import { displayTitle } from '../format.ts';
	import type { CalendarEvent } from '../model.ts';
	import DetailPanel from './DetailPanel.svelte';

	interface Props {
		/** The open event's id: the card carrying it is the anchor. Null when closed. */
		eventId: string | null;
		event: CalendarEvent | undefined;
		courses: readonly MoodleEnrolledCourse[] | undefined;
		/** Closes the details; `refocus` returns focus to the event's card. */
		onclose: (refocus: boolean) => void;
	}

	let { eventId, event, courses, onclose }: Props = $props();

	const anchored = typeof CSS !== 'undefined' && CSS.supports('anchor-name', '--a');

	let surface = $state<HTMLElement>();
	let x = $state<number>();
	let y = $state<number>();

	const card = (id: string) =>
		document.querySelector<HTMLElement>(`[data-event="${CSS.escape(id)}"]`);

	/* Without CSS anchor positioning: beside the card, inside the window. */
	function place(id: string) {
		const rect = card(id)?.getBoundingClientRect();
		if (!rect || !surface) return;
		const width = surface.offsetWidth;
		const right = rect.right + 8 + width <= window.innerWidth;
		x = right ? rect.right + 8 : Math.max(8, rect.left - 8 - width);
		y = Math.min(Math.max(8, rect.top), window.innerHeight - surface.offsetHeight - 8);
	}

	/* Never without its anchor: with no card on screen it would float to the window's corner. */
	$effect(() => {
		if (!surface) return;
		const shown = surface.matches(':popover-open');
		const id = event ? eventId : null;
		const source = id ? card(id) : null;
		if (id && source) {
			if (!shown) {
				surface.showPopover({ source });
				surface.focus({ preventScroll: true });
			}
			if (!anchored) place(id);
		} else if (shown) {
			surface.hidePopover();
		}
	});

	/* Light dismiss by hand: a click on another event swaps the details instead of closing them. */
	function dismiss(element: HTMLElement) {
		return on(
			document,
			'pointerdown',
			(pointer) => {
				if (!eventId || !(pointer.target instanceof Node)) return;
				if (element.contains(pointer.target)) return;
				if (pointer.target instanceof Element && pointer.target.closest('[data-event]')) return;
				onclose(false);
			},
			{ capture: true }
		);
	}

	/* Escape closes the details wherever focus went, the body included. */
	function keydown(key: KeyboardEvent) {
		if (key.key !== 'Escape' || key.defaultPrevented || !eventId) return;
		if (!surface?.matches(':popover-open')) return;
		key.preventDefault();
		onclose(true);
	}

	const focusable =
		'a[href], button:not([disabled]), input:not([disabled]), select, textarea, [tabindex]:not([tabindex="-1"])';

	/**
	 * The details sit at the end of the document, so Tab carries on from the event's card: back
	 * to it before the first control, on to what follows it after the last.
	 */
	function tabbing(key: KeyboardEvent) {
		if (key.key !== 'Tab' || !surface || !eventId) return;
		const source = card(eventId);
		const inside = [...surface.querySelectorAll<HTMLElement>(focusable)].filter((element) =>
			element.checkVisibility()
		);
		const active = document.activeElement;
		if (!source) return;
		if (key.shiftKey && (active === surface || active === inside[0])) {
			key.preventDefault();
			source.focus();
		} else if (!key.shiftKey && (inside.length === 0 || active === inside[inside.length - 1])) {
			const order = [...document.querySelectorAll<HTMLElement>(focusable)].filter(
				(element) => !surface?.contains(element) && element.checkVisibility()
			);
			const after = order[order.indexOf(source) + 1];
			if (after) {
				key.preventDefault();
				after.focus();
			}
		}
	}
</script>

<svelte:document onkeydown={keydown} />

<div
	bind:this={surface}
	class={['popover', { manual: !anchored }]}
	popover="manual"
	role="dialog"
	aria-label={event ? displayTitle(event) : m.calendar_detail_title_fallback()}
	tabindex="-1"
	onkeydown={tabbing}
	style:--x={x === undefined ? undefined : `${x}px`}
	style:--y={y === undefined ? undefined : `${y}px`}
	{@attach dismiss}
>
	{#if eventId && event}
		<DetailPanel {event} {courses} onclose={() => onclose(true)} dense />
	{/if}
</div>

<style>
	.popover {
		position: fixed;
		inset: auto;
		margin: 8px;
		inline-size: min(380px, calc(100vw - 32px));
		max-block-size: min(600px, calc(100dvh - 32px));
		overflow: hidden;
		flex-direction: column;
		padding: 0;
		border-radius: var(--r-lg);
		background: var(--surface-raised);
		color: var(--text);
		box-shadow: var(--elev-overlay), var(--inner-light);
		position-anchor: --calendar-event;
		position-area: inline-end span-block-end;
		position-try-fallbacks:
			flip-inline,
			block-end span-inline-end,
			block-start span-inline-end;
		opacity: 0;
		transform: scale(0.97);
		transition:
			opacity 120ms var(--ease-out),
			transform 120ms var(--ease-out),
			overlay 120ms allow-discrete,
			display 120ms allow-discrete;
		outline: none;
	}

	.popover:popover-open {
		display: flex;
		opacity: 1;
		transform: none;
		transition-duration: 200ms;
		transition-timing-function: var(--ease-out-expo);
	}

	@starting-style {
		.popover:popover-open {
			opacity: 0;
			transform: scale(0.97);
		}
	}

	.manual {
		margin: 0;
		inset: var(--y) auto auto var(--x);
	}
</style>
