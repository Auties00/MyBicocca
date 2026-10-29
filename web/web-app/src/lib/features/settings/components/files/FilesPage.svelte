<script lang="ts">
	import IconFilePdf from '~icons/ph/file-pdf';
	import IconImage from '~icons/ph/image';
	import IconFilmStrip from '~icons/ph/film-strip';
	import IconMusicNotes from '~icons/ph/music-notes';
	import IconBrowser from '~icons/ph/browser';
	import IconFileText from '~icons/ph/file-text';
	import IconFileZip from '~icons/ph/file-zip';
	import IconArrowCounterClockwise from '~icons/ph/arrow-counter-clockwise';
	import IconCaretUpDown from '~icons/ph/caret-up-down';
	import IconEye from '~icons/ph/eye';
	import IconDownloadSimple from '~icons/ph/download-simple';
	import IconQuestion from '~icons/ph/question';
	import {
		fileOpenPreferences,
		type OpenChoice
	} from '#lib/features/course/file/preference.svelte.ts';
	import type { ViewableKind } from '#lib/features/course/files.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import { haptics } from '#lib/ui/haptics.svelte.ts';
	import IconChip from '#lib/ui/IconChip.svelte';
	import Menu from '#lib/ui/Menu.svelte';
	import Overlay from '#lib/ui/Overlay.svelte';
	import type { Icon } from '#lib/ui/types.ts';
	import SectionPage from '../SectionPage.svelte';
	import OpenChoiceList, { type OpenOption } from './OpenChoiceList.svelte';
	import { compact } from '#lib/ui/media.ts';

	type Choice = OpenChoice | 'ask';

	interface Kind {
		kind: ViewableKind;
		icon: Icon;
		label: () => string;
		formats: () => string;
	}

	/** In Android's order. Office documents and unknown files are not listed: they never preview. */
	const kinds: readonly Kind[] = [
		{
			kind: 'pdf',
			icon: IconFilePdf,
			label: m.settings_files_pdf,
			formats: m.settings_files_pdf_formats
		},
		{
			kind: 'image',
			icon: IconImage,
			label: m.settings_files_image,
			formats: () => 'PNG, JPG, GIF, SVG, WebP'
		},
		{
			kind: 'video',
			icon: IconFilmStrip,
			label: m.settings_files_video,
			formats: () => 'MP4, WebM, MOV, MKV'
		},
		{
			kind: 'audio',
			icon: IconMusicNotes,
			label: m.settings_files_audio,
			formats: () => 'MP3, M4A, WAV, OGG, FLAC'
		},
		{ kind: 'html', icon: IconBrowser, label: m.settings_files_html, formats: () => 'HTML' },
		{
			kind: 'text',
			icon: IconFileText,
			label: m.settings_files_text,
			formats: m.settings_files_text_formats
		},
		{ kind: 'zip', icon: IconFileZip, label: m.settings_files_zip, formats: () => 'ZIP, JAR' }
	];

	/** How each choice reads on its row. */
	const values: Record<Choice, () => string> = {
		ask: m.settings_files_ask,
		view: m.settings_files_view,
		download: m.settings_files_download
	};

	const options: readonly OpenOption<Choice>[] = [
		{
			value: 'view',
			label: m.settings_files_view_label(),
			description: m.settings_files_view_description(),
			icon: IconEye
		},
		{
			value: 'download',
			label: m.settings_files_download_label(),
			description: m.settings_files_download_description(),
			icon: IconDownloadSimple
		},
		{
			value: 'ask',
			label: m.settings_files_ask_label(),
			description: m.settings_files_ask_description(),
			icon: IconQuestion
		}
	];

	// Phones pick in a bottom sheet, as Android's chooser does; wider screens in a menu by the row.

	/** The kind the sheet asks about, kept while the sheet slides away. */
	let sheetFor = $state<Kind>(kinds[0]);
	let sheetOpen = $state(false);
	let resetDone = $state(false);
	let list = $state<HTMLElement>();

	const anySet = $derived(Object.keys(fileOpenPreferences.choices).length > 0);
	const current = (kind: ViewableKind): Choice => fileOpenPreferences.get(kind) ?? 'ask';

	function choose(kind: ViewableKind, choice: Choice) {
		sheetOpen = false;
		if (choice === current(kind)) return;
		haptics.tap();
		resetDone = false;
		fileOpenPreferences.set(kind, choice === 'ask' ? undefined : choice);
	}

	function openSheet(item: Kind) {
		sheetFor = item;
		sheetOpen = true;
	}

	function reset() {
		haptics.tap();
		for (const { kind } of kinds) fileOpenPreferences.set(kind, undefined);
		resetDone = true;
		// The button leaves with the last saved choice: focus moves to the first row it reset.
		list?.querySelector<HTMLElement>('.kind')?.focus();
	}
</script>

{#snippet row(item: Kind)}
	{@const choice = current(item.kind)}
	<IconChip icon={item.icon} tone="accent" />
	<span class="text">
		<span class="label">{item.label()}</span>
		<span class="formats">{item.formats()}</span>
	</span>
	<span class={['value', { set: choice !== 'ask' }]}>
		{#if choice === 'view'}<IconEye aria-hidden="true" />{/if}
		{#if choice === 'download'}<IconDownloadSimple aria-hidden="true" />{/if}
		{values[choice]()}
		<IconCaretUpDown class="caret" aria-hidden="true" />
	</span>
{/snippet}

<SectionPage title={m.settings_files_title()} subtitle={m.settings_files_page_subtitle()}>
	<!-- As on Android: each kind shows its current choice; the row opens the choices. -->
	<ul class="kinds" role="list" bind:this={list}>
		{#each kinds as item (item.kind)}
			<li>
				{#if compact.current}
					<button
						type="button"
						class="kind"
						aria-haspopup="dialog"
						aria-expanded={sheetOpen && sheetFor.kind === item.kind}
						onclick={() => openSheet(item)}
					>
						{@render row(item)}
					</button>
				{:else}
					<Menu
						label={m.settings_files_choice_label({ kind: item.label() })}
						placement="bottom-end"
						width="min(320px, calc(100vw - 32px))"
						items={options.map((option) => ({
							label: option.label,
							description: option.description,
							radio: true,
							checked: option.value === current(item.kind),
							onselect: () => choose(item.kind, option.value)
						}))}
					>
						{#snippet trigger(props)}
							<button type="button" class="kind" {...props}>{@render row(item)}</button>
						{/snippet}
					</Menu>
				{/if}
			</li>
		{/each}
	</ul>

	<div class="footer">
		<ul class="notes" role="list">
			<li>{m.settings_files_note_office()}</li>
			<li>{m.settings_files_note_other()}</li>
			<li class="pointer">{m.settings_files_note_shift()}</li>
		</ul>
		{#if anySet}
			<Button variant="outline" icon={IconArrowCounterClockwise} onclick={reset}>
				{m.settings_files_reset()}
			</Button>
		{/if}
		<p class="sr-only" role="status">{resetDone ? m.settings_files_reset_done() : ''}</p>
	</div>
</SectionPage>

{#if compact.current}
	<Overlay bind:open={sheetOpen} title={sheetFor.label()} subtitle={sheetFor.formats()} size="sm">
		<OpenChoiceList
			label={m.settings_files_choice_label({ kind: sheetFor.label() })}
			{options}
			value={current(sheetFor.kind)}
			onchoose={(choice) => choose(sheetFor.kind, choice)}
		/>
	</Overlay>
{/if}

<style>
	.kinds {
		display: grid;
		gap: 2px;
		margin: 0;
		padding: 0;
		list-style: none;
	}

	.kind {
		display: flex;
		align-items: center;
		gap: 12px;
		inline-size: 100%;
		min-block-size: 64px;
		padding: 10px 12px 10px 16px;
		border-radius: var(--r-2xs);
		background: var(--surface);
		box-shadow: var(--inner-light);
		color: var(--text);
		text-align: start;
		cursor: pointer;
		-webkit-tap-highlight-color: transparent;
		transition: background-color var(--dur-fast) var(--ease-out);
	}

	li:first-child .kind {
		border-start-start-radius: var(--r-lg);
		border-start-end-radius: var(--r-lg);
	}

	li:last-child .kind {
		border-end-start-radius: var(--r-lg);
		border-end-end-radius: var(--r-lg);
	}

	@media (hover: hover) {
		.kind:hover {
			background: color-mix(in oklch, var(--surface), var(--text) 4%);
			transition-duration: 0s;
		}
	}

	.kind[aria-expanded='true'] {
		background: color-mix(in oklch, var(--surface), var(--text) 6%);
	}

	.kind:focus-visible {
		outline-offset: -2px;
	}

	.text {
		display: grid;
		flex: 1;
		gap: 1px;
		min-inline-size: 0;
	}

	.label {
		font: var(--type-body-strong);
		font-size: 0.9375rem;
	}

	.formats {
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
		text-wrap: pretty;
	}

	/* "Ask" is the default and stays quiet; a saved choice reads in the accent with its glyph. */
	.value {
		display: inline-flex;
		flex-shrink: 0;
		align-items: center;
		gap: 6px;
		padding: 6px 4px 6px 10px;
		color: var(--text-muted);
		font: var(--type-ui);
		white-space: nowrap;
	}

	.value.set {
		color: var(--accent-text);
		font: var(--type-ui-strong);
	}

	.value :global(svg) {
		inline-size: var(--icon-sm);
		block-size: var(--icon-sm);
	}

	.value :global(.caret) {
		color: var(--text-subtle);
	}

	.footer {
		display: grid;
		gap: var(--sp-4);
		justify-items: start;
	}

	.notes {
		display: grid;
		gap: 6px;
		margin: 0;
		padding-inline-start: 20px;
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
	}

	.notes li::marker {
		color: var(--text-subtle);
	}

	@media not (pointer: fine) {
		.pointer {
			display: none;
		}
	}
</style>
