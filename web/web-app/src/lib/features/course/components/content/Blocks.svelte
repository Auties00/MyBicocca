<script lang="ts">
	import IconCaretDown from '~icons/ph/caret-down';
	import IconNote from '~icons/ph/note';
	import { m } from '#lib/paraglide/messages.js';
	import type { ContentBlock } from '../../content.ts';
	import type { ProgressMap } from '../../video/progress.ts';
	import TeacherHtml from '../TeacherHtml.svelte';
	import Blocks from './Blocks.svelte';
	import ModuleRow from './ModuleRow.svelte';

	interface Props {
		blocks: readonly ContentBlock[];
		courseId: number;
		language: string;
		progress: ProgressMap;
		lastSeen: number | undefined;
		now: Date;
		/** Subsections nest one level down. */
		level?: number;
	}

	let { blocks, courseId, language, progress, lastSeen, now, level = 0 }: Props = $props();
</script>

<div class="blocks">
	{#each blocks as block (block.key)}
		{#if block.kind === 'group'}
			<section class="group" aria-label={block.title ?? undefined}>
				{#if block.title}
					<h4 class="group-title">
						<span class="leaf" aria-hidden="true"></span>
						<span class="label">{block.title}</span>
						<span class="count">{block.modules.length}</span>
					</h4>
				{/if}
				<ul class="modules" role="list">
					{#each block.modules as module (module.id)}
						<ModuleRow
							{module}
							{courseId}
							{language}
							progress={progress[module.id]}
							lastSeen={lastSeen === module.id}
							{now}
						/>
					{/each}
				</ul>
			</section>
		{:else if block.kind === 'note'}
			<aside class="note">
				<IconNote class="note-icon" aria-hidden="true" />
				<TeacherHtml html={block.html} size="sm" />
			</aside>
		{:else}
			<details class="subsection" style:--level={level}>
				<summary>
					<span class="sub-title">{block.title}</span>
					<IconCaretDown class="chevron" aria-hidden="true" />
				</summary>
				<div class="sub-body">
					{#if block.summary}
						<TeacherHtml html={block.summary} size="sm" />
					{/if}
					{#if block.blocks.length > 0}
						<Blocks
							blocks={block.blocks}
							{courseId}
							{language}
							{progress}
							{lastSeen}
							{now}
							level={level + 1}
						/>
					{:else if !block.summary}
						<p class="nothing">{m.course_no_resources()}</p>
					{/if}
				</div>
			</details>
		{/if}
	{/each}
</div>

<style>
	.blocks {
		display: grid;
		gap: 14px;
	}

	.group {
		display: grid;
		gap: 8px;
	}

	.group-title {
		display: flex;
		align-items: center;
		gap: 8px;
		padding-inline: 6px;
		font: var(--type-overline);
		letter-spacing: var(--track-overline);
		text-transform: uppercase;
		color: var(--text-muted);
	}

	.leaf {
		inline-size: 8px;
		block-size: 8px;
		border-radius: 0 50%;
		background: var(--gold);
	}

	.label {
		min-inline-size: 0;
		overflow-wrap: anywhere;
	}

	.count {
		color: var(--text-subtle);
		font-variant-numeric: tabular-nums;
	}

	.modules {
		display: grid;
		gap: 2px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.note {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr);
		gap: 12px;
		padding: 14px 16px;
		border-radius: var(--r-md);
		background: color-mix(in oklch, var(--gold-container) 70%, transparent);
	}

	.note :global(.note-icon) {
		inline-size: var(--icon);
		block-size: var(--icon);
		margin-block-start: 1px;
		color: var(--gold);
	}

	.subsection {
		border-radius: var(--r-md);
		background: var(--row-bg);
		interpolate-size: allow-keywords;
	}

	summary {
		display: flex;
		align-items: center;
		gap: 12px;
		min-block-size: var(--control-h);
		padding: 10px 14px;
		border-radius: var(--r-md);
		cursor: pointer;
		list-style: none;
		font: var(--type-ui-strong);
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	summary::-webkit-details-marker {
		display: none;
	}

	@media (hover: hover) {
		summary:hover {
			background: var(--row-hover);
			transition-duration: 0s;
		}
	}

	.sub-title {
		flex: 1;
		min-inline-size: 0;
	}

	summary :global(.chevron) {
		inline-size: var(--icon-sm);
		block-size: var(--icon-sm);
		color: var(--text-muted);
		transition: transform var(--dur-spring) var(--spring-standard);
	}

	.subsection[open] summary :global(.chevron) {
		transform: rotate(180deg);
	}

	.sub-body {
		display: grid;
		gap: 12px;
		padding: 0 8px 10px;
		--row-bg: var(--surface);
	}

	.nothing {
		padding: 6px 8px;
		color: var(--text-muted);
		font: var(--type-meta);
	}
</style>
