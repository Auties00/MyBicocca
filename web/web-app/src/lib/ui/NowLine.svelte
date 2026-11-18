<script lang="ts">
	import { formatTime } from '#lib/format/dates.ts';
	import { m } from '#lib/paraglide/messages.js';

	interface Props {
		now: Date;
	}

	let { now }: Props = $props();

	const time = $derived(formatTime(now));
</script>

<li class="now" aria-label={m.ui_now_at({ time })}>
	<span class="time" aria-hidden="true">{time}</span>
	<span class="line" aria-hidden="true"></span>
</li>

<style>
	.now {
		display: grid;
		grid-template-columns: 48px minmax(0, 1fr);
		gap: 12px;
		align-items: center;
		margin-block: -2px;
	}

	.time {
		color: var(--accent-text);
		font: 600 0.75rem / 1 var(--font-ui);
		font-variant-numeric: tabular-nums;
		text-align: end;
	}

	.line {
		position: relative;
		block-size: 2px;
		border-radius: 1px;
		background: var(--accent);
	}

	.line::before {
		content: '';
		position: absolute;
		inset-block-start: -4px;
		inset-inline-start: -5px;
		inline-size: 10px;
		block-size: 10px;
		border-radius: 50%;
		background: var(--accent);
	}
</style>
