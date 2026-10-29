<script lang="ts">
	import IconCloudArrowUp from '~icons/ph/cloud-arrow-up';
	import IconCloudCheck from '~icons/ph/cloud-check';
	import IconCloudSlash from '~icons/ph/cloud-slash';
	import IconWarning from '~icons/ph/warning-circle-fill';
	import { m } from '#lib/paraglide/messages.js';
	import type { SaveState } from '../attempt.svelte.ts';

	interface Props {
		state: SaveState;
		onretry: () => void;
	}

	let { state, onretry }: Props = $props();

	const text = $derived(
		{
			idle: undefined,
			saving: m.quiz_saving(),
			saved: m.quiz_saved(),
			pending: m.quiz_unsaved(),
			offline: m.quiz_save_offline(),
			error: m.quiz_save_failed()
		}[state]
	);
</script>

<!-- On phones only the icon shows, its words left to screen readers; a failure also shows in
     the dock, with its Retry. -->
<span class={['save', state]} role="status">
	{#if state === 'saving' || state === 'pending'}
		<IconCloudArrowUp aria-hidden="true" />
	{:else if state === 'saved'}
		<IconCloudCheck aria-hidden="true" />
	{:else if state === 'offline'}
		<IconCloudSlash aria-hidden="true" />
	{:else if state === 'error'}
		<IconWarning aria-hidden="true" />
	{/if}
	{#if text}<span class="text">{text}</span>{/if}
	{#if state === 'error'}
		<button type="button" class="retry" onclick={onretry}>{m.common_retry()}</button>
	{/if}
</span>

<style>
	.save {
		display: inline-flex;
		align-items: center;
		gap: 6px;
		min-inline-size: 0;
		color: var(--text-muted);
		font: var(--type-meta);
		white-space: nowrap;
	}

	.save :global(svg) {
		inline-size: 18px;
		block-size: 18px;
		flex-shrink: 0;
	}

	.text {
		position: absolute;
		inline-size: 1px;
		block-size: 1px;
		overflow: hidden;
		clip-path: inset(50%);
		white-space: nowrap;
	}

	.retry {
		display: none;
	}

	.offline {
		color: var(--warning);
		font-weight: 550;
	}

	.error {
		color: var(--danger);
		font-weight: 550;
	}

	@media (width >= 600px) {
		.save :global(svg) {
			inline-size: 16px;
			block-size: 16px;
		}

		.text {
			position: static;
			inline-size: auto;
			block-size: auto;
			overflow: hidden;
			clip-path: none;
			text-overflow: ellipsis;
		}

		.retry {
			display: inline-block;
			padding: 2px 6px;
			border: 0;
			border-radius: var(--r-xs);
			background: none;
			color: var(--accent-text);
			font: var(--type-ui-strong);
			font-size: 0.8125rem;
			cursor: pointer;
		}
	}

	@media (hover: hover) {
		.retry:hover {
			background: var(--state-hover);
		}
	}
</style>
