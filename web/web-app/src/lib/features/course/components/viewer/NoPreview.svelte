<script lang="ts">
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import IconDownloadSimple from '~icons/ph/download-simple';
	import { formatBytes } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import type { CourseFile } from '../../content.ts';
	import { officeHandoffAvailable } from '../../file/office.ts';
	import { getFileOpener } from '../../file/opener.svelte.ts';
	import type { FileKind } from '../../files.ts';
	import { fileIcon } from '../icons.ts';

	interface Props {
		name: string;
		kind: FileKind;
		size: number;
		/** The Moodle file, which an Office app can open from its address. */
		office: CourseFile | undefined;
		ondownload: () => void;
	}

	let { name, kind, size, office, ondownload }: Props = $props();

	const opener = getFileOpener();
	const Glyph = $derived(fileIcon(kind));
	const app = $derived(
		kind.type !== 'office' || !officeHandoffAvailable()
			? null
			: kind.app === 'word'
				? 'Word'
				: kind.app === 'excel'
					? 'Excel'
					: 'PowerPoint'
	);
</script>

<div class="none">
	<span class="glyph" aria-hidden="true"><Glyph /></span>
	<p class="name">{name}</p>
	<p class="meta">{formatBytes(size)}</p>
	<p class="hint">{m.course_viewer_no_preview()}</p>
	<div class="actions">
		<Button
			variant={app && office ? 'tonal' : 'solid'}
			icon={IconDownloadSimple}
			onclick={ondownload}
		>
			{m.course_download()}
		</Button>
		{#if app && office}
			{@const file = office}
			<Button variant="solid" icon={IconArrowSquareOut} onclick={() => opener.open({ file })}>
				{m.course_office_open({ app })}
			</Button>
		{/if}
	</div>
</div>

<style>
	.none {
		display: grid;
		place-content: center;
		justify-items: center;
		gap: 6px;
		block-size: 100%;
		padding: var(--sp-6);
		text-align: center;
	}

	.glyph {
		display: grid;
		place-items: center;
		inline-size: 96px;
		block-size: 96px;
		margin-block-end: 10px;
		border-radius: 30%;
		background: var(--container);
		color: var(--on-container);
	}

	.glyph :global(svg) {
		inline-size: 44px;
		block-size: 44px;
	}

	.name {
		max-inline-size: 36ch;
		font: var(--type-title-s);
		overflow-wrap: anywhere;
	}

	.meta,
	.hint {
		color: var(--text-muted);
		font: var(--type-meta);
	}

	.hint {
		max-inline-size: 40ch;
		margin-block-start: 6px;
	}

	.actions {
		display: flex;
		flex-wrap: wrap;
		justify-content: center;
		gap: 8px;
		margin-block-start: 16px;
	}
</style>
