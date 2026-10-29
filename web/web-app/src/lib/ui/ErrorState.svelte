<script lang="ts">
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconCloudSlash from '~icons/ph/cloud-slash';
	import IconCloudWarning from '~icons/ph/cloud-warning-duotone';
	import IconWarning from '~icons/ph/warning';
	import { network } from '#lib/data/network.svelte.ts';
	import { formatRelative } from '#lib/format/dates.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from './Button.svelte';

	interface Props {
		/** What failed, in a full sentence ("Non è stato possibile caricare gli esami."). */
		message: string;
		/** Page variant only: a short heading above the message. */
		title?: string;
		/** Shows a Retry button. */
		onretry?: () => void;
		retrying?: boolean;
		/**
		 * page: nothing to show, the error replaces the content.
		 * stale: data from a previous load stays visible; this is a slim notice above it. Offline,
		 * where the shell's band already says so and a retry can't work, it only dates the data.
		 */
		variant?: 'page' | 'stale';
		/** When the visible (stale) data was last loaded: "updated 3 h ago". */
		updatedAt?: Date;
	}

	let { message, title, onretry, retrying = false, variant = 'page', updatedAt }: Props = $props();

	const updated = $derived(
		updatedAt ? m.ui_updated_ago({ time: formatRelative(updatedAt) }) : undefined
	);
</script>

{#if variant === 'stale'}
	{#if !network.online}
		{#if updatedAt}
			<p class="offline">
				<IconCloudSlash aria-hidden="true" />
				{m.ui_offline_data_from({ time: formatRelative(updatedAt) })}
			</p>
		{/if}
	{:else}
		<div class="stale" role="status">
			<IconWarning class="stale-glyph" aria-hidden="true" />
			<p class="stale-text">
				{message}
				{#if updated}<span class="updated">{updated}</span>{/if}
			</p>
			{#if onretry}
				<Button
					variant="ghost"
					size="sm"
					icon={IconArrowClockwise}
					loading={retrying}
					onclick={onretry}
				>
					{m.common_retry()}
				</Button>
			{/if}
		</div>
	{/if}
{:else}
	<div class="error" role="alert">
		<span class="glyph"><IconCloudWarning aria-hidden="true" /></span>
		<p class="title">{title ?? m.ui_error_title()}</p>
		<p class="message">{message}</p>
		{#if updated}<p class="updated">{updated}</p>{/if}
		{#if onretry}
			<div class="action">
				<Button variant="tonal" icon={IconArrowClockwise} loading={retrying} onclick={onretry}>
					{m.common_retry()}
				</Button>
			</div>
		{/if}
	</div>
{/if}

<style>
	.error {
		display: grid;
		justify-items: center;
		gap: 6px;
		padding: var(--sp-8) var(--sp-4);
		text-align: center;
	}

	.glyph {
		display: grid;
		margin-block-end: 10px;
		color: var(--danger);
	}

	.glyph :global(svg) {
		inline-size: 40px;
		block-size: 40px;
	}

	.title {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.message {
		max-inline-size: 46ch;
		color: var(--text-muted);
		font-size: 0.9375rem;
	}

	.updated {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.action {
		margin-block-start: 14px;
	}

	/* One row at every width: the retry stays beside the text, which wraps. */
	.stale {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr) auto;
		align-items: center;
		gap: var(--sp-3);
		min-block-size: 44px;
		padding: 6px 6px 6px 14px;
		border-radius: var(--r-md);
		background: var(--warning-container);
		color: var(--on-warning-container);
		box-shadow: inset 0 0 0 1px color-mix(in oklch, var(--warning) 14%, transparent);
	}

	.stale :global(.stale-glyph) {
		inline-size: var(--icon);
		block-size: var(--icon);
		color: var(--warning);
	}

	.stale-text {
		padding-block: 4px;
		font: var(--type-ui);
		font-weight: 400;
	}

	.stale-text .updated {
		display: block;
		color: inherit;
		opacity: 0.8;
	}

	.offline {
		display: flex;
		align-items: center;
		gap: 6px;
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.offline :global(svg) {
		flex: none;
		inline-size: 16px;
		block-size: 16px;
	}
</style>
