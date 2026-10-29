<script lang="ts">
	import type { Range } from '../../search/matcher.ts';

	interface Props {
		text: string;
		/** Ascending, non-overlapping `[start, end)` ranges to mark. */
		ranges: readonly Range[];
	}

	let { text, ranges }: Props = $props();

	const parts = $derived.by(() => {
		const out: { text: string; hit: boolean }[] = [];
		let at = 0;
		for (const [start, end] of ranges) {
			if (start > at) out.push({ text: text.slice(at, start), hit: false });
			out.push({ text: text.slice(start, end), hit: true });
			at = end;
		}
		if (at < text.length) out.push({ text: text.slice(at), hit: false });
		return out;
	});
</script>

{#each parts as part, index (index)}{#if part.hit}<mark>{part.text}</mark
		>{:else}{part.text}{/if}{/each}

<style>
	mark {
		border-radius: 3px;
		background: var(--mark, color-mix(in oklch, var(--accent) 22%, transparent));
		color: inherit;
		box-decoration-break: clone;
		-webkit-box-decoration-break: clone;
	}
</style>
