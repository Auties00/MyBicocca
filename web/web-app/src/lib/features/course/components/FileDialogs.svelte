<script lang="ts">
	import IconDownloadSimple from '~icons/ph/download-simple';
	import IconEye from '~icons/ph/eye';
	import IconMicrosoftExcelLogo from '~icons/ph/microsoft-excel-logo';
	import IconMicrosoftPowerpointLogo from '~icons/ph/microsoft-powerpoint-logo';
	import IconMicrosoftWordLogo from '~icons/ph/microsoft-word-logo';
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import { toDataError, type DataError } from '#lib/data/errors.ts';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { shapePath } from '#lib/features/elearning/shapes.ts';
	import { formatBytes } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Banner from '#lib/ui/Banner.svelte';
	import Button from '#lib/ui/Button.svelte';
	import Overlay from '#lib/ui/Overlay.svelte';
	import Switch from '#lib/ui/Switch.svelte';
	import type { OfficeApp } from '../files.ts';
	import { classifyFile, viewableKind, type ViewableKind } from '../files.ts';
	import { officeHandoffAvailable, openInOffice } from '../file/office.ts';
	import { getFileOpener, type FileRequest } from '../file/opener.svelte.ts';
	import { fileOpenPreferences } from '../file/preference.svelte.ts';
	import { fileIcon } from './icons.ts';
	import { kindLabel } from './content/moduleMeta.ts';

	const opener = getFileOpener();

	const kindNames: Record<ViewableKind, () => string> = {
		pdf: m.course_kinds_pdf,
		image: m.course_kinds_image,
		video: m.course_kinds_video,
		audio: m.course_kinds_audio,
		html: m.course_kinds_html,
		text: m.course_kinds_text,
		zip: m.course_kinds_zip
	};
	const apps: Record<
		OfficeApp,
		{ name: string; document: () => string; icon: typeof IconMicrosoftWordLogo }
	> = {
		word: { name: 'Word', document: m.course_office_word, icon: IconMicrosoftWordLogo },
		excel: { name: 'Excel', document: m.course_office_excel, icon: IconMicrosoftExcelLogo },
		powerpoint: {
			name: 'PowerPoint',
			document: m.course_office_powerpoint,
			icon: IconMicrosoftPowerpointLogo
		}
	};

	/** How long Office has to take over before the page offers the download instead. */
	const HANDOFF_MS = 1500;

	let remember = $state(false);
	let failure = $state<DataError>();
	let opening = $state(false);
	let missed = $state(false);
	const handoff = officeHandoffAvailable();

	const asked = $derived(opener.asking);
	const office = $derived(opener.office);

	const askedKind = $derived(asked && classifyFile(asked.file.name, asked.file.mime));
	const viewable = $derived(askedKind && viewableKind(askedKind));
	const blob = shapePath('cookie6');

	function subtitle(request: FileRequest | null): string | undefined {
		if (request === null) return undefined;
		const kind = classifyFile(request.file.name, request.file.mime);
		const parts = [
			kind.type === 'office' ? apps[kind.app].document() : kindLabel(kind, request.file.name),
			request.file.size === null ? null : formatBytes(request.file.size)
		];
		return parts.filter((part) => part).join(' · ');
	}

	function closeAsking() {
		opener.askingOpen = false;
		failure = undefined;
		remember = false;
	}

	function closeOffice() {
		opener.officeOpen = false;
		failure = undefined;
		missed = false;
	}

	function choose(choice: 'view' | 'download') {
		const request = opener.asking;
		if (request === null) return;
		if (remember && viewable) fileOpenPreferences.set(viewable, choice);
		if (choice === 'download') void save(request, closeAsking);
		else {
			closeAsking();
			opener.view(request);
		}
	}

	async function save(request: FileRequest, done: () => void) {
		failure = await opener.download(request.file);
		if (failure === undefined) done();
	}

	/* No browser says whether a scheme has a handler: Office took over if the page lost focus. */
	async function launch(request: FileRequest, app: OfficeApp) {
		const scope = session.current;
		if (scope === undefined) return;
		opening = true;
		failure = undefined;
		missed = false;
		let left = false;
		const leave = () => (left = true);
		window.addEventListener('blur', leave, { once: true });
		try {
			await openInOffice(scope, request.file, app);
			await new Promise((resolve) => setTimeout(resolve, HANDOFF_MS));
			if (left || !document.hasFocus()) closeOffice();
			else missed = true;
		} catch (error) {
			failure = toDataError(error);
		} finally {
			window.removeEventListener('blur', leave);
			opening = false;
		}
	}
</script>

<Overlay
	open={opener.askingOpen}
	title={asked?.file.name ?? ''}
	subtitle={subtitle(asked)}
	size="sm"
	onclose={closeAsking}
>
	{#if asked && askedKind}
		{@const Glyph = fileIcon(askedKind)}
		<div class="hero">
			<span class="shape" aria-hidden="true">
				<svg viewBox="0 0 100 100"><path d={blob} /></svg>
				<Glyph />
			</span>
			<p class="question">{m.course_open_question()}</p>
		</div>
		{#if viewable}
			<div class="remember">
				<Switch
					bind:checked={remember}
					label={m.course_remember_choice({ kind: kindNames[viewable]() })}
					description={m.course_remember_hint()}
				/>
			</div>
		{/if}
		{#if failure}
			<Banner
				tone="danger"
				role="alert"
				message={errorMessage(failure)}
				title={m.course_download_failed()}
			/>
		{/if}
	{/if}
	{#snippet footer()}
		<Button
			icon={IconDownloadSimple}
			loading={asked !== null && opener.downloading.has(asked.file.url)}
			onclick={() => choose('download')}
		>
			{m.course_download()}
		</Button>
		<Button variant="solid" icon={IconEye} onclick={() => choose('view')}>
			{m.course_view_here()}
		</Button>
	{/snippet}
</Overlay>

<Overlay
	open={opener.officeOpen}
	title={office?.request.file.name ?? ''}
	subtitle={subtitle(office?.request ?? null)}
	size="sm"
	onclose={closeOffice}
>
	{#if office}
		{@const app = apps[office.app]}
		<div class="hero">
			<span class={['shape', office.app]} aria-hidden="true">
				<svg viewBox="0 0 100 100"><path d={blob} /></svg>
				<app.icon />
			</span>
			{#if handoff}
				<p class="question">{m.course_office_readonly({ app: app.name })}</p>
				<p class="note">{m.course_office_token_note({ app: app.name })}</p>
			{:else}
				<p class="question">{m.course_office_download_only({ app: app.name })}</p>
			{/if}
		</div>
		{#if failure}
			<Banner tone="danger" role="alert" message={errorMessage(failure)} />
		{:else if missed}
			<Banner tone="warning" role="status" message={m.course_office_missed({ app: app.name })} />
		{/if}
	{/if}
	{#snippet footer()}
		{#if office}
			{@const request = office.request}
			{@const downloadFirst = !handoff || missed}
			<Button
				variant={downloadFirst ? 'solid' : 'tonal'}
				icon={IconDownloadSimple}
				loading={opener.downloading.has(request.file.url)}
				onclick={() => void save(request, closeOffice)}
			>
				{m.course_download()}
			</Button>
			{#if handoff}
				<Button
					variant={downloadFirst ? 'tonal' : 'solid'}
					icon={IconArrowSquareOut}
					loading={opening}
					onclick={() => void launch(request, office.app)}
				>
					{m.course_office_open({ app: apps[office.app].name })}
				</Button>
			{/if}
		{/if}
	{/snippet}
</Overlay>

<style>
	.hero {
		display: grid;
		justify-items: center;
		gap: 12px;
		padding-block: 8px 20px;
		text-align: center;
	}

	.shape {
		position: relative;
		display: grid;
		place-items: center;
		inline-size: 96px;
		block-size: 96px;
		color: var(--on-container);
	}

	.shape svg {
		position: absolute;
		inset: 0;
		inline-size: 100%;
		block-size: 100%;
		fill: var(--container);
	}

	.shape > :global(svg:last-child) {
		position: relative;
		inline-size: 40px;
		block-size: 40px;
	}

	.word {
		color: var(--on-info-container);
	}

	.word svg {
		fill: var(--info-container);
	}

	.excel {
		color: var(--on-success-container);
	}

	.excel svg {
		fill: var(--success-container);
	}

	.powerpoint {
		color: var(--on-danger-container);
	}

	.powerpoint svg {
		fill: var(--danger-container);
	}

	.question {
		max-inline-size: 34ch;
		font: var(--type-body-strong);
		text-wrap: balance;
	}

	.note {
		max-inline-size: 42ch;
		color: var(--text-muted);
		font: var(--type-meta);
		text-wrap: pretty;
	}

	.remember {
		padding: 12px 14px;
		border-radius: var(--r-md);
		background: var(--surface-sunken);
	}
</style>
