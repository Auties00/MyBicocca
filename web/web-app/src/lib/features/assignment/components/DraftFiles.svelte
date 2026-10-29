<script lang="ts">
	import { flip } from 'svelte/animate';
	import { prefersReducedMotion } from 'svelte/motion';
	import { fade } from 'svelte/transition';
	import IconX from '~icons/ph/x';
	import { formatBytes } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import IconButton from '#lib/ui/IconButton.svelte';
	import { draftName, draftSize } from '../draft.ts';
	import type { DraftFile } from '../writes.ts';
	import FileChip from './FileChip.svelte';

	interface Props {
		files: readonly DraftFile[];
		disabled: boolean;
		label: string;
		onremove: (key: string) => void;
	}

	let { files, disabled, label, onremove }: Props = $props();

	const motion = $derived(prefersReducedMotion.current ? 0 : 1);
</script>

<ul class="files" aria-label={label}>
	{#each files as file (file.key)}
		{@const name = draftName(file)}
		{@const size = draftSize(file)}
		<li class="file" animate:flip={{ duration: 200 * motion }} in:fade={{ duration: 150 * motion }}>
			<FileChip {name} />
			<span class="text">
				<span class="name">{name}</span>
				<span class="meta">
					{#if size !== null}<span>{formatBytes(size)}</span>{/if}
					<span class={['origin', file.kind]}>
						{file.kind === 'picked' ? m.assignment_editor_new() : m.assignment_editor_handed_in()}
					</span>
				</span>
			</span>
			<IconButton
				icon={IconX}
				label={m.assignment_editor_remove_file({ name })}
				size="sm"
				{disabled}
				onclick={() => onremove(file.key)}
			/>
		</li>
	{/each}
</ul>

<style>
	.files {
		display: grid;
		gap: 2px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.file {
		display: grid;
		grid-template-columns: 44px minmax(0, 1fr) auto;
		align-items: center;
		gap: 14px;
		padding: 10px 8px 10px 10px;
		border-radius: var(--r-2xs);
		background: var(--surface);
		box-shadow: var(--inner-light);
	}

	.file:first-child {
		border-start-start-radius: var(--r-lg);
		border-start-end-radius: var(--r-lg);
	}

	.file:last-child {
		border-end-start-radius: var(--r-lg);
		border-end-end-radius: var(--r-lg);
	}

	.text {
		display: grid;
		gap: 2px;
		min-inline-size: 0;
	}

	.name {
		overflow: hidden;
		font: var(--type-ui-strong);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.meta {
		display: flex;
		flex-wrap: wrap;
		gap: 4px 10px;
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.origin.picked {
		color: var(--course-ink);
		font-weight: 600;
	}
</style>
