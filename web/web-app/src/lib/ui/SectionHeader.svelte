<script lang="ts">
	import type { Snippet } from 'svelte';
	import IconArrowRight from '~icons/ph/arrow-right';
	import { internalHref, type Href } from './types.ts';

	interface Props {
		title: string;
		description?: string;
		level?: 2 | 3;
		/** "Vedi tutti" style link on the right. */
		link?: { href: Href; label: string };
		/** Anything else on the right (a filter, a count). */
		actions?: Snippet;
		/** label: small uppercase heading for groups inside a page section. */
		variant?: 'title' | 'label';
		id?: string;
	}

	let { title, description, level = 2, link, actions, variant = 'title', id }: Props = $props();

	const href = $derived(internalHref(link?.href));
</script>

<div class={['section-header', variant]}>
	<div class="titles">
		<svelte:element this={`h${level}`} class="title" {id}>{title}</svelte:element>
		{#if description}<p class="description">{description}</p>{/if}
	</div>
	{#if actions}<div class="actions">{@render actions()}</div>{/if}
	{#if link && href}
		<a class="link" {href}>
			{link.label}
			<IconArrowRight aria-hidden="true" />
		</a>
	{/if}
</div>

<style>
	.section-header {
		display: flex;
		align-items: flex-end;
		justify-content: space-between;
		gap: 12px;
	}

	.titles {
		display: grid;
		gap: 2px;
		min-inline-size: 0;
	}

	.title {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.label .title {
		color: var(--text-muted);
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
	}

	.description {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.actions {
		display: flex;
		gap: 8px;
	}

	.link {
		display: inline-flex;
		align-items: center;
		gap: 4px;
		flex-shrink: 0;
		padding: 4px 6px;
		margin: -4px -6px;
		border-radius: var(--r-xs);
		color: var(--accent-text);
		font: var(--type-ui-strong);
		text-decoration: none;
	}

	.link :global(svg) {
		inline-size: 16px;
		block-size: 16px;
		transition: transform var(--dur-base) var(--spring-pop);
	}

	@media (hover: hover) {
		.link:hover :global(svg) {
			transform: translateX(3px);
		}
	}
</style>
