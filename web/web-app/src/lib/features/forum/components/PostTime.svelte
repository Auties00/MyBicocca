<script lang="ts">
	import { now } from '#lib/data/clock.svelte.ts';
	import { formatDateTime, formatRecent } from '#lib/format/dates.ts';

	interface Props {
		/** Epoch seconds, as Moodle sends times. */
		at: number;
	}

	let { at }: Props = $props();

	const date = $derived(new Date(at * 1000));
	const text = $derived(formatRecent(date, now.current));
</script>

<time datetime={date.toISOString()} title={formatDateTime(date)}>{text}</time>
