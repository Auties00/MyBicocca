<script lang="ts">
	import IconCheckCircle from '~icons/ph/check-circle-fill';
	import IconUploadSimple from '~icons/ph/upload-simple';
	import IconWarningCircle from '~icons/ph/warning-circle-fill';
	import IconX from '~icons/ph/x';
	import { fileIcon } from '#lib/features/course/components/icons.ts';
	import { classifyFile } from '#lib/features/course/files.ts';
	import { formatBytes } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import IconButton from '#lib/ui/IconButton.svelte';
	import Spinner from '#lib/ui/Spinner.svelte';
	import { admitFiles, type AttachmentLimits, type AttachmentProblem } from '../forum.ts';
	import type { UploadState } from '../writes.ts';

	interface Existing {
		name: string;
		size: number;
	}

	interface Props {
		limits: AttachmentLimits;
		/** The files the post already has (when editing). */
		existing?: readonly Existing[];
		/** Names of the existing files to keep. */
		kept?: string[];
		files?: File[];
		/** Upload state of each new file while sending. */
		states?: readonly UploadState[];
		disabled?: boolean;
	}

	let {
		limits,
		existing = [],
		kept = $bindable([]),
		files = $bindable([]),
		states = [],
		disabled = false
	}: Props = $props();

	const id = $props.id();
	let input = $state<HTMLInputElement>();
	let problems = $state<AttachmentProblem[]>([]);
	let over = $state(false);

	const count = $derived(kept.length + files.length);
	const full = $derived(count >= limits.maxFiles);
	const hint = $derived(
		limits.maxBytes > 0
			? m.forum_attachments_limits({ count: limits.maxFiles, size: formatBytes(limits.maxBytes) })
			: m.forum_attachments_count_limit({ count: limits.maxFiles })
	);

	function add(list: FileList | null) {
		if (list === null || list.length === 0 || disabled) return;
		const result = admitFiles([...list], count, limits);
		files = [...files, ...result.accepted];
		problems = result.problems;
	}

	function problemText(problem: AttachmentProblem): string {
		return problem.kind === 'too_big'
			? m.forum_attachment_too_big({ name: problem.name, size: formatBytes(problem.max) })
			: m.forum_attachment_too_many({ count: problem.max });
	}

	function drop(event: DragEvent) {
		event.preventDefault();
		over = false;
		add(event.dataTransfer?.files ?? null);
	}

	function dragover(event: DragEvent) {
		if (!event.dataTransfer?.types.includes('Files') || disabled) return;
		event.preventDefault();
		event.dataTransfer.dropEffect = full ? 'none' : 'copy';
		over = true;
	}
</script>

{#snippet fileRow(name: string, size: number, state: UploadState | undefined, remove: () => void)}
	{@const Glyph = fileIcon(classifyFile(name, null))}
	<li class={['file', state]}>
		<Glyph class="kind" aria-hidden="true" />
		<span class="name">{name}</span>
		<span class="size">{formatBytes(size)}</span>
		{#if state === 'uploading'}
			<Spinner size={18} label={m.forum_attachment_uploading({ name })} />
		{:else if state === 'done'}
			<IconCheckCircle class="done" aria-label={m.forum_attachment_uploaded({ name })} role="img" />
		{:else if state === 'failed'}
			<IconWarningCircle
				class="failed"
				aria-label={m.forum_attachment_failed({ name })}
				role="img"
			/>
		{:else}
			<IconButton
				icon={IconX}
				size="sm"
				label={m.forum_attachment_remove({ name })}
				{disabled}
				onclick={remove}
			/>
		{/if}
	</li>
{/snippet}

<div class="attachments" role="group" aria-labelledby="{id}-label" aria-describedby="{id}-hint">
	<div class="head">
		<span class="label" id="{id}-label">{m.forum_attachments()}</span>
		<span class="hint" id="{id}-hint">{hint}</span>
	</div>

	{#if kept.length > 0 || files.length > 0}
		<ul class="files">
			{#each existing.filter((file) => kept.includes(file.name)) as file (file.name)}
				{@render fileRow(file.name, file.size, undefined, () => {
					kept = kept.filter((name) => name !== file.name);
					problems = [];
				})}
			{/each}
			{#each files as file, index (file)}
				{@render fileRow(file.name, file.size, states[index], () => {
					files = files.filter((other) => other !== file);
					problems = [];
				})}
			{/each}
		</ul>
	{/if}

	{#if !full}
		<div
			class={['drop', { over }]}
			role="presentation"
			ondragover={dragover}
			ondragleave={() => (over = false)}
			ondrop={drop}
		>
			<button type="button" class="pick" {disabled} onclick={() => input?.click()}>
				<IconUploadSimple aria-hidden="true" />
				{m.forum_attachments_pick()}
			</button>
			<span class="pointer">{m.forum_attachments_drop()}</span>
			<input
				bind:this={input}
				class="sr-only"
				type="file"
				multiple
				tabindex="-1"
				aria-hidden="true"
				{disabled}
				onchange={(event) => {
					add(event.currentTarget.files);
					event.currentTarget.value = '';
				}}
			/>
		</div>
	{/if}

	{#if problems.length > 0 && !disabled}
		<ul class="problems" role="alert">
			{#each problems as problem, index (index)}
				<li>
					<IconWarningCircle aria-hidden="true" />
					{problemText(problem)}
				</li>
			{/each}
		</ul>
	{/if}
</div>

<style>
	.attachments {
		display: grid;
		gap: 8px;
	}

	.head {
		display: flex;
		flex-wrap: wrap;
		align-items: baseline;
		justify-content: space-between;
		gap: 4px 12px;
	}

	.label {
		font: var(--type-ui-strong);
	}

	.hint {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.files {
		display: grid;
		gap: 4px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.file {
		display: grid;
		grid-template-columns: auto minmax(0, 1fr) auto auto;
		align-items: center;
		gap: 10px;
		min-block-size: 44px;
		padding: 4px 4px 4px 12px;
		border-radius: var(--r-sm);
		background: var(--surface-sunken);
		font: var(--type-ui);
	}

	.file :global(.kind) {
		inline-size: 20px;
		block-size: 20px;
		color: var(--text-muted);
	}

	.name {
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.size {
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.file :global(.done),
	.file :global(.failed) {
		inline-size: 20px;
		block-size: 20px;
		margin-inline: 8px;
	}

	.file :global(.done) {
		color: var(--success);
	}

	.file :global(.failed) {
		color: var(--danger);
	}

	.uploading,
	.done {
		padding-inline-end: 10px;
	}

	.drop {
		display: flex;
		align-items: center;
		gap: 10px;
		padding: 10px 12px;
		border: 1.5px dashed var(--border);
		border-radius: var(--r-md);
		color: var(--text-muted);
		font: var(--type-ui);
		transition:
			background-color var(--dur-fast) var(--ease-out),
			border-color var(--dur-fast) var(--ease-out);
	}

	.over {
		border-color: var(--course-bar, var(--accent));
		background: var(--course-container, var(--container));
		color: var(--text);
	}

	.pick {
		display: inline-flex;
		align-items: center;
		gap: 8px;
		min-block-size: 36px;
		padding: 6px 14px 6px 10px;
		border-radius: var(--r-pill);
		background: var(--surface);
		box-shadow: inset 0 0 0 1px var(--hairline-strong);
		color: var(--text);
		font: var(--type-ui-strong);
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.pick :global(svg) {
		inline-size: 18px;
		block-size: 18px;
		color: var(--course-ink, var(--accent-text));
	}

	@media (hover: hover) {
		.pick:hover:not(:disabled) {
			background: var(--surface-hover);
		}
	}

	.pick:focus-visible {
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	@media (pointer: coarse) {
		.pointer {
			display: none;
		}

		.pick {
			min-block-size: 44px;
		}
	}

	.problems {
		display: grid;
		gap: 4px;
		margin: 0;
		padding: 0;
		list-style: none;
		color: var(--danger);
		font: var(--type-meta);
	}

	.problems li {
		display: flex;
		align-items: flex-start;
		gap: 6px;
	}

	.problems :global(svg) {
		inline-size: 16px;
		block-size: 16px;
		flex-shrink: 0;
		margin-block-start: 1px;
	}
</style>
