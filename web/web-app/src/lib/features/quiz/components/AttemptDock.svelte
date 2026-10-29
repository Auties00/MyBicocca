<script lang="ts">
	import type { Snippet } from 'svelte';
	import IconCloudSlash from '~icons/ph/cloud-slash';
	import IconListNumbers from '~icons/ph/list-numbers';
	import IconWarning from '~icons/ph/warning-circle-fill';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import type { SaveState } from '../attempt.svelte.ts';

	interface Props {
		/** Where the student is: "6 / 10", or "6" when the page count isn't known. */
		position: string;
		onnavigator: () => void;
		saveState: SaveState;
		retrying: boolean;
		onretry: () => void;
		/** Previous, Next or Finish. */
		children: Snippet;
	}

	let { position, onnavigator, saveState, retrying, onretry, children }: Props = $props();
</script>

<!-- Phones and tablets: the page moves stay under the thumb, and on phones, where the bar only
     has room for an icon, a failed save says so here, with its Retry. -->
<div class="dock">
	{#if saveState === 'error' || saveState === 'offline'}
		<div class={['strip', saveState]} role="alert">
			{#if saveState === 'error'}
				<IconWarning aria-hidden="true" />
				<span class="text">{m.quiz_save_failed_long()}</span>
				<Button size="sm" variant="ghost" loading={retrying} onclick={onretry}>
					{m.common_retry()}
				</Button>
			{:else}
				<IconCloudSlash aria-hidden="true" />
				<span class="text">{m.quiz_save_offline_long()}</span>
			{/if}
		</div>
	{/if}
	<nav class="moves" aria-label={m.quiz_pages_label()}>
		<Button
			variant="tonal"
			icon={IconListNumbers}
			label={m.quiz_navigator_open({ position })}
			onclick={onnavigator}
		>
			{position}
		</Button>
		{@render children()}
	</nav>
</div>

<style>
	.dock {
		position: sticky;
		inset-block-end: 0;
		z-index: var(--z-sticky);
		display: grid;
		gap: 8px;
		padding: 10px var(--gutter) calc(10px + var(--safe-bottom));
		border-block-start: 1px solid var(--hairline);
		background: var(--veil);
		backdrop-filter: var(--veil-filter);
	}

	.moves {
		display: flex;
		align-items: center;
		gap: 8px;
	}

	.strip {
		display: flex;
		align-items: center;
		gap: 8px;
		min-block-size: 40px;
		padding: 4px 4px 4px 12px;
		border-radius: var(--r-md);
		font: var(--type-meta);
		font-weight: 550;
	}

	.strip :global(svg) {
		inline-size: 18px;
		block-size: 18px;
		flex-shrink: 0;
	}

	.text {
		flex: 1;
		min-inline-size: 0;
	}

	.error {
		background: var(--danger-container);
		color: var(--on-danger-container);
	}

	.offline {
		background: var(--warning-container);
		color: var(--on-warning-container);
	}

	@media (width >= 600px) {
		.strip {
			display: none;
		}
	}

	@media (width >= 1024px) {
		.dock {
			display: none;
		}
	}
</style>
