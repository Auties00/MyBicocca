<script lang="ts">
	import type { Snippet } from 'svelte';

	interface Props {
		/** `ListRow`s. */
		children: Snippet;
		/** Heading shown as the group's first segment. */
		title?: string;
		description?: string;
		level?: 2 | 3 | 4;
		/** sm: a smaller title, for dense lists and lists inside a pane. */
		size?: 'md' | 'sm';
		/** Right side of the header (a link, a count, a badge), centred on the title's first line. */
		actions?: Snippet;
		/** Accessible name when there is no visible title. */
		label?: string;
		/** Skip rendering work for off-screen rows; use for long lists (≥ 50 rows). */
		lazy?: boolean;
	}

	let {
		children,
		title,
		description,
		level = 2,
		size = 'md',
		actions,
		label,
		lazy = false
	}: Props = $props();

	const headingId = $props.id();
</script>

<section
	class={['group', size, { titled: !!title, lazy }]}
	aria-labelledby={title ? headingId : undefined}
	aria-label={title ? undefined : label}
>
	{#if title}
		<div class="head">
			<div class="heading">
				<svelte:element this={`h${level}`} id={headingId} class="title">{title}</svelte:element>
				{#if description}<p class="description">{description}</p>{/if}
			</div>
			{#if actions}<div class="actions">{@render actions()}</div>{/if}
		</div>
	{/if}
	<ul class="rows" role="list">
		{@render children()}
	</ul>
</section>

<style>
	.group {
		--outer: var(--r-lg);
		--inner: var(--r-2xs);
		/* The title's line box (its font's size × line height), for the actions to centre on. */
		--title-line: calc(1.25rem * 1.3);
		display: grid;
		gap: 2px;
		container-type: inline-size;
	}

	.head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 12px;
		padding: 18px 16px 14px;
		border-radius: var(--outer) var(--outer) var(--inner) var(--inner);
		background: var(--surface);
		box-shadow: var(--inner-light);
	}

	.title {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.sm {
		--title-line: calc(1.0625rem * 1.35);
	}

	.sm .head {
		padding: 14px 16px 10px;
	}

	.sm .title {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.description {
		margin-block-start: 2px;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.actions {
		display: flex;
		align-items: center;
		flex: none;
		gap: 8px;
		block-size: var(--title-line);
	}

	.rows {
		display: grid;
		gap: 2px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.rows > :global(li) {
		border-radius: var(--inner);
	}

	.group:not(.titled) .rows > :global(li:first-child) {
		border-start-start-radius: var(--outer);
		border-start-end-radius: var(--outer);
	}

	.rows > :global(li:last-child) {
		border-end-start-radius: var(--outer);
		border-end-end-radius: var(--outer);
	}

	.lazy .rows > :global(li) {
		content-visibility: auto;
		contain-intrinsic-size: auto 64px;
	}
</style>
