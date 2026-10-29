<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { Icon } from '#lib/ui/types.ts';

	interface Props {
		icon: Icon;
		title: string;
		body: string;
		/** accent: a call to act (verify the email); plain: nothing here yet. */
		tone?: 'accent' | 'plain';
		/** The heading, for moving focus to it. */
		heading?: HTMLHeadingElement;
		action?: Snippet;
	}

	let { icon: Glyph, title, body, tone = 'plain', heading = $bindable(), action }: Props = $props();
</script>

<div class={['note', tone]}>
	<span class="icon"><Glyph aria-hidden="true" /></span>
	<div class="text">
		<h3 class="title" tabindex="-1" bind:this={heading}>{title}</h3>
		<p class="body">{body}</p>
	</div>
	{#if action}<div class="action">{@render action()}</div>{/if}
</div>

<style>
	/* A section's own state, in the UI face: the page title stays the view's one serif line. */
	.note {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		align-items: center;
		gap: var(--sp-3) var(--sp-4);
		min-block-size: 128px;
		padding: var(--sp-5);
		border-radius: var(--r-lg);
	}

	.accent {
		background: var(--container);
		color: var(--on-container);
		box-shadow: var(--inner-light);
	}

	.plain {
		box-shadow: inset 0 0 0 1px var(--hairline);
	}

	.icon {
		display: grid;
		place-items: center;
		inline-size: 48px;
		block-size: 48px;
		border-radius: 16px;
		background: var(--surface);
		color: var(--accent-text);
		box-shadow: var(--elev-1);
	}

	.plain .icon {
		background: var(--surface-sunken);
		box-shadow: none;
	}

	.icon :global(svg) {
		inline-size: 24px;
		block-size: 24px;
	}

	.text {
		display: grid;
		gap: 4px;
	}

	.title {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
		outline: none;
	}

	.body {
		font: var(--type-ui);
		opacity: 0.85;
		text-wrap: pretty;
	}

	.plain .body {
		color: var(--text-muted);
		opacity: 1;
	}

	.action {
		grid-column: 1 / -1;
		justify-self: start;
	}
</style>
