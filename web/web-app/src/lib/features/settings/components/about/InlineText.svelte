<script lang="ts">
	import { parseInline, type InlineSegment } from '../../releases/notes.ts';

	interface Props {
		/** One line of a release note's inline Markdown. */
		text: string;
	}

	let { text }: Props = $props();

	const segments = $derived(parseInline(text));

	const styles = (segment: InlineSegment) => ({
		bold: segment.bold,
		italic: segment.italic,
		strike: segment.strike,
		underline: segment.underline
	});
</script>

{#each segments as segment, index (index)}
	{#if segment.br}<br />{:else if segment.href}<a
			class={['link', styles(segment)]}
			href={segment.href}
			rel="external noopener noreferrer"
			target="_blank">{segment.text}</a
		>{:else if segment.code}<code class={styles(segment)}>{segment.text}</code>{:else}<span
			class={styles(segment)}>{segment.text}</span
		>{/if}
{/each}

<style>
	.bold {
		font-weight: 650;
	}

	.italic {
		font-style: italic;
	}

	.strike {
		text-decoration: line-through;
	}

	.underline {
		text-decoration: underline;
		text-underline-offset: 0.15em;
	}

	.strike.underline {
		text-decoration: underline line-through;
	}

	code {
		padding: 0.1em 0.35em;
		border-radius: var(--r-2xs);
		background: var(--surface-sunken);
		font-family: ui-monospace, 'Cascadia Code', 'SF Mono', Menlo, Consolas, monospace;
		font-size: 0.85em;
		overflow-wrap: anywhere;
	}

	.link {
		color: var(--accent-text);
		font-weight: 550;
		text-decoration: underline;
		text-decoration-color: color-mix(in oklch, currentColor 35%, transparent);
		text-underline-offset: 0.18em;
	}

	@media (hover: hover) {
		.link:hover {
			text-decoration-color: currentColor;
		}
	}
</style>
