<script lang="ts">
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import { statusParts } from '../labels.ts';
	import type { RoomStatus } from '../status.ts';

	interface Props {
		/** Undefined while the day's bookings load. */
		status: RoomStatus | undefined;
		size?: 'sm' | 'lg';
	}

	let { status, size = 'sm' }: Props = $props();

	const kind = $derived(status?.kind ?? 'checking');
	const parts = $derived(statusParts(status));
	/* A small line sketches its words while the bookings load: the cell keeps its height. */
	const sketch = $derived(kind === 'checking' && size === 'sm');
</script>

<!-- The word and the time are apart, so a narrow cell can stack them; the time never breaks. -->
<span class={['status', kind, size]}>
	<span class="state">
		<span class="dot" aria-hidden="true"></span>
		{#if sketch}
			<span class="sr-only">{parts.word}</span>
			<span class="sketch"><Skeleton variant="text" width="4.5em" /></span>
		{:else}
			<span class="word">{parts.word}</span>
		{/if}
	</span>
	{#if sketch}
		<span class="detail sketch" aria-hidden="true"><Skeleton variant="text" width="6.5em" /></span>
	{:else if parts.detail}
		<span class="detail">{parts.detail}</span>
	{/if}
</span>

<style>
	.status {
		display: inline-flex;
		flex-wrap: wrap;
		align-items: baseline;
		column-gap: 0.3em;
		min-inline-size: 0;
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}

	.state {
		display: inline-flex;
		align-items: center;
		gap: 7px;
	}

	.sketch {
		display: flex;
	}

	.dot {
		flex: none;
		inline-size: 8px;
		block-size: 8px;
		border-radius: 50%;
		background: var(--text-subtle);
	}

	.free {
		color: var(--success);
	}

	.free .dot {
		background: var(--success-solid);
	}

	.busy .dot {
		background: var(--danger-solid);
	}

	.closed .dot {
		background: transparent;
		box-shadow: inset 0 0 0 2px var(--text-subtle);
	}

	.checking .dot {
		background: var(--track);
		animation: pulse 1.2s var(--ease-in-out) infinite alternate;
	}

	.lg {
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.lg .state {
		gap: 10px;
	}

	.lg .dot {
		inline-size: 12px;
		block-size: 12px;
	}

	.lg.busy {
		color: var(--text);
	}

	@keyframes pulse {
		to {
			opacity: 0.35;
		}
	}

	@media (prefers-reduced-motion: reduce) {
		.checking .dot {
			animation: none;
		}
	}
</style>
