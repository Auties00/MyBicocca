<script lang="ts">
	import type { Snippet } from 'svelte';
	import type { ResolvedPathname } from '$app/types';
	import IconArrowLeft from '~icons/ph/arrow-left';

	interface Props {
		title: string;
		subtitle?: string;
		back: { href: ResolvedPathname; label: string };
		leading?: Snippet;
		actions?: Snippet;
	}

	let { title, subtitle, back, leading, actions }: Props = $props();
</script>

<header class="header">
	<a class="back" href={back.href}>
		<IconArrowLeft aria-hidden="true" />
		<span>{back.label}</span>
	</a>
	<div class="identity">
		{#if leading}{@render leading()}{/if}
		<div class="names">
			<h1 class="title" tabindex="-1">{title}</h1>
			{#if subtitle}<p class="subtitle">{subtitle}</p>{/if}
		</div>
	</div>
	{#if actions}<div class="actions">{@render actions()}</div>{/if}
</header>

<style>
	.header {
		display: grid;
		gap: var(--sp-2);
	}

	.back {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		justify-self: start;
		max-inline-size: 100%;
		min-block-size: 32px;
		margin-inline-start: -8px;
		padding: 4px 10px 4px 8px;
		border-radius: var(--r-pill);
		color: var(--text-muted);
		font: var(--type-ui);
		text-decoration: none;
		touch-action: manipulation;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.back span {
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.back :global(svg) {
		flex: none;
		inline-size: 16px;
		block-size: 16px;
	}

	@media (hover: hover) {
		.back:hover {
			background: var(--state-hover);
			color: var(--text);
			transition-duration: 0s;
		}
	}

	.back:active {
		background: var(--state-press);
	}

	.identity {
		display: flex;
		align-items: center;
		gap: var(--sp-3);
		min-inline-size: 0;
	}

	.names {
		display: grid;
		gap: 2px;
		min-inline-size: 0;
	}

	.title {
		font: var(--type-title-l);
		letter-spacing: var(--track-title-l);
		overflow-wrap: anywhere;
		text-wrap: balance;
		outline: none;
	}

	.subtitle {
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}

	.actions {
		display: flex;
		flex-wrap: wrap;
		gap: var(--sp-2);
		margin-block-start: var(--sp-1);
	}
</style>
