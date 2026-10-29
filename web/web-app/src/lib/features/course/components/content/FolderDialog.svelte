<script lang="ts">
	import IconDownloadSimple from '~icons/ph/download-simple';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { formatBytes } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import IconButton from '#lib/ui/IconButton.svelte';
	import Overlay from '#lib/ui/Overlay.svelte';
	import type { CourseFile } from '../../content.ts';
	import { getFileOpener } from '../../file/opener.svelte.ts';
	import { classifyFile } from '../../files.ts';
	import { moduleFileHref } from '../../links.ts';
	import { fileIcon } from '../icons.ts';
	import { kindLabel } from './moduleMeta.ts';

	interface Props {
		open: boolean;
		title: string;
		courseId: number;
		cmId: number;
		files: readonly CourseFile[];
		onclose: () => void;
	}

	let { open, title, courseId, cmId, files, onclose }: Props = $props();

	const opener = getFileOpener();
</script>

<Overlay
	{open}
	{title}
	subtitle={m.course_folder_count({ count: files.length })}
	wide="dialog"
	size="md"
	{onclose}
>
	<ul class="files" role="list">
		{#each files as file (file.url)}
			{@const kind = classifyFile(file.name, file.mime)}
			{@const Glyph = fileIcon(kind)}
			{@const href = moduleFileHref(courseId, cmId, file)}
			{@const failed = opener.failed?.url === file.url ? opener.failed.error : null}
			<li class="file">
				<span class="icon" aria-hidden="true"><Glyph /></span>
				<span class="text">
					<a
						class="name"
						{href}
						title={file.name}
						onclick={(event) => opener.click(event, { file, href })}
					>
						{file.name}
					</a>
					<span class="meta">
						{#if failed}
							<span class="failed">{m.course_download_failed()} · {errorMessage(failed)}</span>
						{:else}
							{[
								kindLabel(kind, file.name),
								file.size === null ? null : formatBytes(file.size),
								file.folder === '/' ? null : file.folder.replace(/^\/|\/$/g, '')
							]
								.filter((part) => part)
								.join(' · ')}
						{/if}
					</span>
				</span>
				<span class="action">
					<IconButton
						icon={IconDownloadSimple}
						label={m.course_download_named({ name: file.name })}
						loading={opener.downloading.has(file.url)}
						onclick={() => void opener.download(file)}
					/>
				</span>
			</li>
		{/each}
	</ul>
</Overlay>

<style>
	.files {
		display: grid;
		gap: 2px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.file {
		position: relative;
		display: grid;
		grid-template-columns: 40px minmax(0, 1fr) auto;
		align-items: center;
		gap: 12px;
		padding: 8px 6px 8px 8px;
		border-radius: var(--r-md);
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	@media (hover: hover) {
		.file:has(.name:hover) {
			background: var(--state-hover);
			transition-duration: 0s;
		}
	}

	.file:has(.name:focus-visible) {
		outline: 2px solid var(--focus);
		outline-offset: -2px;
	}

	.icon {
		display: grid;
		place-items: center;
		inline-size: 40px;
		block-size: 40px;
		border-radius: 12px;
		background: var(--info-container);
		color: var(--on-info-container);
	}

	.icon :global(svg) {
		inline-size: var(--icon);
		block-size: var(--icon);
	}

	.text {
		display: grid;
		gap: 2px;
		min-inline-size: 0;
	}

	/* Folder files often differ only at the end of long names: two lines keep the endings. */
	.name {
		display: -webkit-box;
		overflow: hidden;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		color: var(--text);
		font: var(--type-ui-strong);
		text-decoration: none;
		overflow-wrap: anywhere;
		outline: none;
	}

	.name::after {
		content: '';
		position: absolute;
		inset: 0;
		border-radius: inherit;
	}

	.meta {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.failed {
		color: var(--danger);
	}

	.action {
		position: relative;
		z-index: var(--z-raised);
	}
</style>
