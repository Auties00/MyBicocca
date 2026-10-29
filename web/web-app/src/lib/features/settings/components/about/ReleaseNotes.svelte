<script lang="ts">
	import IconInfo from '~icons/ph/info';
	import IconLightbulb from '~icons/ph/lightbulb';
	import IconMegaphone from '~icons/ph/megaphone';
	import IconWarning from '~icons/ph/warning';
	import IconWarningOctagon from '~icons/ph/warning-octagon';
	import { m } from '#lib/paraglide/messages.js';
	import type { Icon } from '#lib/ui/types.ts';
	import type { CalloutKind, ReleaseBlock } from '../../releases/notes.ts';
	import InlineText from './InlineText.svelte';

	interface Props {
		blocks: readonly ReleaseBlock[];
	}

	let { blocks }: Props = $props();

	const callouts: Record<CalloutKind, { icon: Icon; label: () => string }> = {
		note: { icon: IconInfo, label: m.settings_release_note },
		tip: { icon: IconLightbulb, label: m.settings_release_tip },
		important: { icon: IconMegaphone, label: m.settings_release_important },
		warning: { icon: IconWarning, label: m.settings_release_warning },
		caution: { icon: IconWarningOctagon, label: m.settings_release_caution }
	};
</script>

<div class="notes">
	{#each blocks as block, index (index)}
		{#if block.type === 'heading'}
			<!-- Whatever their Markdown level, the notes' sections sit right under the card's h2. -->
			<h3 class={['heading', `level-${block.level}`]}><InlineText text={block.text} /></h3>
		{:else if block.type === 'paragraph'}
			<p class="paragraph"><InlineText text={block.text} /></p>
		{:else if block.type === 'list'}
			<ul class="list">
				{#each block.items as item, itemIndex (itemIndex)}
					<li><InlineText text={item.text} /></li>
				{/each}
			</ul>
		{:else}
			{@const callout = callouts[block.kind]}
			<aside class={['callout', block.kind]}>
				<p class="callout-title">
					<callout.icon aria-hidden="true" />
					{callout.label()}
				</p>
				<p class="callout-body">
					{#each block.text.split('\n') as line, lineIndex (lineIndex)}
						{#if lineIndex > 0}<br />{/if}<InlineText text={line} />
					{/each}
				</p>
			</aside>
		{/if}
	{/each}
</div>

<style>
	.notes {
		display: grid;
		gap: 10px;
		font: var(--type-body);
		font-size: 0.9375rem;
		line-height: 1.55;
		overflow-wrap: break-word;
	}

	.heading {
		margin-block-start: 10px;
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.heading:first-child {
		margin-block-start: 0;
	}

	.level-1,
	.level-2 {
		font: var(--type-title-m);
		letter-spacing: var(--track-title-m);
	}

	.paragraph {
		color: var(--text-muted);
	}

	.list {
		display: grid;
		gap: 6px;
		margin: 0;
		padding-inline-start: 1.25em;
	}

	.list li::marker {
		color: var(--accent-text);
	}

	.callout {
		--tone: var(--info);
		display: grid;
		gap: 4px;
		padding: 12px 14px;
		border-inline-start: 3px solid var(--tone);
		border-radius: var(--r-2xs) var(--r-md) var(--r-md) var(--r-2xs);
		background: color-mix(in oklch, var(--tone) 8%, var(--surface));
	}

	.tip {
		--tone: var(--success);
	}

	.important {
		--tone: var(--accent-text);
	}

	.warning {
		--tone: var(--warning);
	}

	.caution {
		--tone: var(--danger);
	}

	.callout-title {
		display: flex;
		align-items: center;
		gap: 6px;
		color: var(--tone);
		font: var(--type-ui-strong);
	}

	.callout-title :global(svg) {
		inline-size: var(--icon-sm);
		block-size: var(--icon-sm);
	}
</style>
