<script lang="ts">
	import type { ResolvedPathname } from '$app/types';
	import IconCaretRight from '~icons/ph/caret-right';
	import { formatBytes } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import type { ZipEntry } from '../../file/zip.ts';
	import { classifyFile } from '../../files.ts';
	import { fileIcon } from '../icons.ts';

	interface Props {
		entries: readonly ZipEntry[];
		name: string;
		hrefOf: (entry: ZipEntry) => ResolvedPathname;
	}

	let { entries, name, hrefOf }: Props = $props();

	const total = $derived(entries.reduce((sum, entry) => sum + entry.size, 0));
</script>

<div class="zip">
	<div class="column">
		<p class="summary">
			{m.course_zip_summary({ count: entries.length, size: formatBytes(total) })}
		</p>
		<ul class="entries" aria-label={name}>
			{#each entries as entry (entry.path)}
				{@const Glyph = fileIcon(classifyFile(entry.name, null))}
				<li>
					<a href={hrefOf(entry)}>
						<span class="icon" aria-hidden="true"><Glyph /></span>
						<span class="text">
							<span class="name">{entry.name}</span>
							<span class="meta">
								{[entry.folder || null, formatBytes(entry.size)].filter((part) => part).join(' · ')}
							</span>
						</span>
						<IconCaretRight class="chevron" aria-hidden="true" />
					</a>
				</li>
			{/each}
		</ul>
	</div>
</div>

<style>
	.zip {
		block-size: 100%;
		overflow: auto;
		padding: var(--sp-5) var(--gutter) 96px;
		overscroll-behavior: contain;
	}

	.column {
		display: grid;
		gap: 12px;
		max-inline-size: var(--w-reading);
		margin-inline: auto;
	}

	.summary {
		padding-inline: 6px;
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.entries {
		display: grid;
		gap: 2px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	a {
		display: grid;
		grid-template-columns: 40px minmax(0, 1fr) auto;
		align-items: center;
		gap: 12px;
		padding: 10px 14px 10px 10px;
		border-radius: 5px;
		background: var(--surface);
		color: inherit;
		text-decoration: none;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	li:first-child a {
		border-start-start-radius: var(--r-md);
		border-start-end-radius: var(--r-md);
	}

	li:last-child a {
		border-end-start-radius: var(--r-md);
		border-end-end-radius: var(--r-md);
	}

	@media (hover: hover) {
		a:hover {
			background: var(--surface-hover);
			transition-duration: 0s;
		}
	}

	a:focus-visible {
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

	.name {
		overflow: hidden;
		font: var(--type-ui-strong);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.meta {
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	a :global(.chevron) {
		inline-size: var(--icon-sm);
		block-size: var(--icon-sm);
		color: var(--text-subtle);
	}
</style>
