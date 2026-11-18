<script lang="ts" module>
	/**
	 * success when the write went through; neutral when something was undone; info when nothing
	 * needed doing; warning or danger when it did not go through.
	 */
	export type OutcomeTone = 'success' | 'neutral' | 'info' | 'warning' | 'danger';
</script>

<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { Icon } from './types.ts';

	interface Props {
		/** What happened: "Exam booked", "Booking cancelled". */
		title: string;
		icon: Icon;
		tone?: OutcomeTone;
		/** What it means now, in a sentence or two. */
		body?: string;
		/** Under the title, before the body: what was booked, when. */
		children?: Snippet;
		/** Where to go next. The first is usually the solid one. */
		actions?: Snippet;
		/** 3 when it sits under a pane's own heading. */
		level?: 2 | 3;
		/** The heading, for a dialog's `returnFocus`. */
		heading?: HTMLElement;
	}

	let {
		title,
		icon: Glyph,
		tone = 'success',
		body,
		children,
		actions,
		level = 2,
		heading = $bindable()
	}: Props = $props();

	const bodyId = $props.id();

	/*
	 * An outcome takes the place of what asked for it, usually as the dialog that confirmed the
	 * write closes and hands focus back to an opener that is gone. A frame later its heading takes
	 * the focus, which announces it (so it is no live region as well), and it comes into view.
	 */
	function arrive(panel: HTMLElement) {
		const frame = requestAnimationFrame(() => {
			heading?.focus({ preventScroll: true });
			panel.scrollIntoView({ block: 'nearest' });
		});
		return () => cancelAnimationFrame(frame);
	}
</script>

<section class={['outcome', tone]} {@attach arrive}>
	<span class="seal"><Glyph aria-hidden="true" /></span>
	<svelte:element
		this={`h${level}`}
		class="title"
		tabindex="-1"
		aria-describedby={body ? bodyId : undefined}
		bind:this={heading}
	>
		{title}
	</svelte:element>
	{@render children?.()}
	{#if body}<p class="body" id={bodyId}>{body}</p>{/if}
	{#if actions}<div class="actions">{@render actions()}</div>{/if}
</section>

<style>
	.outcome {
		display: grid;
		justify-items: start;
		gap: var(--sp-2);
		max-inline-size: var(--w-reading);
		padding: var(--sp-6) var(--sp-5) var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.seal {
		display: grid;
		place-items: center;
		inline-size: 56px;
		block-size: 56px;
		margin-block-end: var(--sp-2);
		border-radius: 18px;
		box-shadow: var(--inner-light);
	}

	.success .seal {
		background: var(--success-container);
		color: var(--on-success-container);
	}

	.neutral .seal {
		background: var(--surface-sunken);
		color: var(--text-muted);
	}

	.info .seal {
		background: var(--info-container);
		color: var(--on-info-container);
	}

	.warning .seal {
		background: var(--warning-container);
		color: var(--on-warning-container);
	}

	.danger .seal {
		background: var(--danger-container);
		color: var(--on-danger-container);
	}

	.seal :global(svg) {
		inline-size: 28px;
		block-size: 28px;
	}

	/* The UI face: the page's title is the view's serif line. */
	.title {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
		text-wrap: balance;
		outline: none;
	}

	.body {
		max-inline-size: 52ch;
		color: var(--text-muted);
		text-wrap: pretty;
	}

	.actions {
		display: flex;
		flex-wrap: wrap;
		gap: var(--sp-2);
		margin-block-start: var(--sp-3);
	}

	@media (prefers-reduced-motion: no-preference) {
		.seal {
			animation: pop var(--dur-slow) var(--spring-pop) both;
		}
	}

	@keyframes pop {
		from {
			opacity: 0;
			transform: scale(0.6);
		}
	}

	@supports (corner-shape: squircle) {
		.outcome {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}

		.seal {
			corner-shape: squircle;
			border-radius: 26px;
		}
	}
</style>
