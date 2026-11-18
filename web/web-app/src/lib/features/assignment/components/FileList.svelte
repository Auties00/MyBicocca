<script lang="ts">
	import IconDotsThreeVertical from '~icons/ph/dots-three-vertical';
	import IconDownloadSimple from '~icons/ph/download-simple';
	import IconSquaresFour from '~icons/ph/squares-four';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import type { CourseFile } from '#lib/features/course/content.ts';
	import { getFileOpener } from '#lib/features/course/file/opener.svelte.ts';
	import { attachmentHref } from '#lib/features/course/links.ts';
	import { formatBytes } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import IconButton from '#lib/ui/IconButton.svelte';
	import Menu from '#lib/ui/Menu.svelte';
	import Spinner from '#lib/ui/Spinner.svelte';
	import FileChip from './FileChip.svelte';

	interface Props {
		files: readonly CourseFile[];
		courseId: number;
		/** The list's accessible name. */
		label: string;
	}

	let { files, courseId, label }: Props = $props();

	const opener = getFileOpener();
</script>

<ul class="files" aria-label={label}>
	{#each files as file (file.url)}
		{@const href = attachmentHref(courseId, file)}
		{@const request = { file, href }}
		{@const failure = opener.failed?.url === file.url ? opener.failed.error : null}
		<li class="file">
			{#if opener.downloading.has(file.url)}
				<span class="busy"><Spinner size={20} /></span>
			{:else}
				<FileChip name={file.name} />
			{/if}
			<span class="text">
				<a class="name" {href} onclick={(event) => opener.click(event, request)}>{file.name}</a>
				{#if failure}
					<span class="meta failure" role="status"
						>{m.course_download_failed()} · {errorMessage(failure)}</span
					>
				{:else if file.size !== null}
					<span class="meta">{formatBytes(file.size)}</span>
				{/if}
			</span>
			<span class="more">
				<Menu
					label={m.course_more_actions({ name: file.name })}
					placement="bottom-end"
					items={[
						{
							label: m.course_open_with(),
							icon: IconSquaresFour,
							onselect: () => opener.open(request, true)
						},
						{
							label: m.course_download(),
							icon: IconDownloadSimple,
							onselect: () => void opener.download(file)
						}
					]}
				>
					{#snippet trigger(props)}
						<IconButton
							{...props}
							icon={IconDotsThreeVertical}
							label={m.course_more_actions({ name: file.name })}
							size="sm"
						/>
					{/snippet}
				</Menu>
			</span>
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
		position: relative;
		display: grid;
		grid-template-columns: 44px minmax(0, 1fr) auto;
		align-items: center;
		gap: 14px;
		padding: 10px 8px 10px 10px;
		border-radius: var(--r-2xs);
		background: var(--surface-sunken);
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	.file:first-child {
		border-start-start-radius: var(--r-lg);
		border-start-end-radius: var(--r-lg);
	}

	.file:last-child {
		border-end-start-radius: var(--r-lg);
		border-end-end-radius: var(--r-lg);
	}

	@media (hover: hover) {
		.file:hover {
			background: var(--surface-hover);
			transition-duration: 0s;
		}
	}

	.busy {
		display: grid;
		place-items: center;
		inline-size: 44px;
		block-size: 44px;
		color: var(--course-ink);
	}

	.text {
		display: grid;
		gap: 2px;
		min-inline-size: 0;
	}

	.name {
		overflow: hidden;
		color: var(--text);
		font: var(--type-ui-strong);
		text-decoration: none;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	/* The whole tile opens the file; the menu sits above the stretched link. */
	.name::after {
		content: '';
		position: absolute;
		inset: 0;
	}

	.name:focus-visible {
		outline: none;
	}

	.file:has(.name:focus-visible) {
		z-index: 1;
		outline: 2px solid var(--focus);
		outline-offset: 2px;
	}

	.meta {
		color: var(--text-muted);
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	.failure {
		color: var(--danger);
	}

	.more {
		position: relative;
		z-index: 1;
	}
</style>
