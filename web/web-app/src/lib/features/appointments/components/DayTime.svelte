<script lang="ts">
	interface Props {
		day: string;
		time: string;
	}

	let { day, time }: Props = $props();
</script>

<!--
	"Thursday 29 October · 11:00–11:20" on one line when it fits; otherwise the time goes under the
	day whole, and its separator, pushed past the start of the line, is clipped away.
-->
<span class="day-time"
	><span class="parts"><span>{day}</span><span class="time">{time}</span></span></span
>

<style>
	.day-time {
		--gap: 0.85em;
		display: block;
		overflow-x: clip;
	}

	.parts {
		display: flex;
		flex-wrap: wrap;
		margin-inline-start: calc(-1 * var(--gap));
	}

	.parts > span {
		position: relative;
		padding-inline-start: var(--gap);
	}

	.time {
		white-space: nowrap;
	}

	.time::before {
		content: '·' / ', ';
		position: absolute;
		inset-inline-start: 0;
		inline-size: var(--gap);
		text-align: center;
	}
</style>
