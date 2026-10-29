<script lang="ts">
	import { afterNavigate, goto } from '$app/navigation';
	import type { ResolvedPathname } from '$app/types';
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import IconCircleHalf from '~icons/ph/circle-half';
	import IconDownloadSimple from '~icons/ph/download-simple';
	import IconFileDashed from '~icons/ph/file-dashed-duotone';
	import IconMagnifyingGlassMinus from '~icons/ph/magnifying-glass-minus';
	import IconMagnifyingGlassPlus from '~icons/ph/magnifying-glass-plus';
	import IconMoon from '~icons/ph/moon';
	import IconShareNetwork from '~icons/ph/share-network';
	import IconSun from '~icons/ph/sun';
	import IconTextAlignLeft from '~icons/ph/text-align-left';
	import IconArrowsIn from '~icons/ph/arrows-in-simple';
	import IconArrowsOut from '~icons/ph/arrows-out-simple';
	import IconX from '~icons/ph/x';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { query } from '#lib/data/query.svelte.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { formatBytes } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import Immersive from '#lib/ui/Immersive.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import Spinner from '#lib/ui/Spinner.svelte';
	import { toast } from '#lib/ui/toast.svelte.ts';
	import type { Icon } from '#lib/ui/types.ts';
	import { displayName, findModule, moduleFiles, moduleTarget } from '../../content.ts';
	import { getCourse } from '../../course.svelte.ts';
	import { theme as appTheme } from '#lib/ui/theme.svelte.ts';
	import { FileOpener, fileMime, setFileOpener } from '../../file/opener.svelte.ts';
	import { openPdfInTab } from '../../file/pdf.ts';
	import { canShareFile, openInTab, saveFile, shareFile } from '../../file/save.ts';
	import { extractZipEntry, readZip, type ZipEntry } from '../../file/zip.ts';
	import { classifyFile, mimeFor, type FileKind } from '../../files.ts';
	import { attachmentHref, coursePath, moduleFileHref } from '../../links.ts';
	import { fileQuery } from '../../queries.ts';
	import type { ViewerRequest } from '../../viewer.ts';
	import FileDialogs from '../FileDialogs.svelte';
	import { kindLabel } from '../content/moduleMeta.ts';
	import HtmlView, { type PageTheme } from './HtmlView.svelte';
	import ImageView from './ImageView.svelte';
	import MediaView from './MediaView.svelte';
	import NoPreview from './NoPreview.svelte';
	import PdfView from './PdfView.svelte';
	import TextView from './TextView.svelte';
	import ZipView from './ZipView.svelte';

	interface Props {
		request: ViewerRequest | null;
	}

	let { request }: Props = $props();

	const course = getCourse();
	setFileOpener(new FileOpener(() => session.current));

	const located = $derived.by(() => {
		if (request === null) return { status: 'missing' } as const;
		if (request.by === 'address') {
			return { status: 'found', file: request.file, title: request.file.name } as const;
		}
		const outline = course.outline;
		if (outline === undefined) {
			return course.contents.status === 'error'
				? ({ status: 'failed' } as const)
				: ({ status: 'loading' } as const);
		}
		const found = findModule(outline, request.cmId);
		if (found === undefined) return { status: 'missing' } as const;
		const target = moduleTarget(found.module, course.language);
		const files =
			target.kind === 'file'
				? [target.file]
				: target.kind === 'folder'
					? target.files
					: moduleFiles(found.module, course.language);
		const file =
			files.find((item) => item.name === request.name && item.folder === request.folder) ??
			files.find((item) => item.name === request.name);
		if (file === undefined) return { status: 'missing' } as const;
		const page = target.kind === 'file' && target.file.mime === 'text/html';
		return {
			status: 'found',
			file,
			title: page ? displayName(found.module.name, course.language) : file.name
		} as const;
	});

	const file = $derived(located.status === 'found' ? located.file : undefined);
	const bytes = query(() => {
		const scope = session.current;
		return scope && file && fileQuery(scope, file);
	});
	const kind = $derived(file && classifyFile(file.name, file.mime));
	const gate = new LoadingGate(
		() =>
			located.status === 'loading' ||
			(file !== undefined && bytes.data === undefined && bytes.status === 'pending')
	);

	/* An archive's entry opens here too, extracted in memory. */
	const archive = $derived.by((): ZipEntry[] | 'unreadable' | undefined => {
		if (kind?.type !== 'zip' || bytes.data === undefined) return undefined;
		try {
			const entries = readZip(bytes.data);
			return entries.length > 0 ? entries : 'unreadable';
		} catch {
			return 'unreadable';
		}
	});
	const entry = $derived(
		Array.isArray(archive) && request?.entry
			? archive.find((item) => item.path === request.entry)
			: undefined
	);
	let extracted = $state<{ path: string; bytes: Uint8Array } | { path: string; failed: true }>();

	$effect(() => {
		const data = bytes.data;
		const wanted = entry;
		if (data === undefined || wanted === undefined || extracted?.path === wanted.path) return;
		let live = true;
		extractZipEntry(data, wanted).then(
			(content) => live && (extracted = { path: wanted.path, bytes: content }),
			() => live && (extracted = { path: wanted.path, failed: true })
		);
		return () => {
			live = false;
		};
	});

	/** What the stage shows: the file, or the archive entry the address names. */
	const shown = $derived.by(() => {
		if (file === undefined || bytes.data === undefined || kind === undefined) return undefined;
		if (entry === undefined) {
			return {
				name: file.name,
				kind,
				mime: fileMime(file, kind),
				bytes: bytes.data,
				size: bytes.data.length
			};
		}
		if (extracted?.path !== entry.path || 'failed' in extracted) return undefined;
		const entryKind = classifyFile(entry.name, null);
		return {
			name: entry.name,
			kind: entryKind,
			mime: mimeFor(entryKind, entry.name),
			bytes: extracted.bytes,
			size: entry.size
		};
	});
	const entryFailed = $derived(
		entry !== undefined && extracted?.path === entry.path && 'failed' in extracted
	);

	const title = $derived(
		entry?.name ??
			(located.status === 'found' ? located.title : request?.by === 'module' ? request.name : '')
	);
	const subtitle = $derived.by(() => {
		const name = entry?.name ?? file?.name;
		const shownKind = entry ? classifyFile(entry.name, null) : kind;
		const size = entry?.size ?? file?.size ?? bytes.data?.length;
		if (name === undefined || shownKind === undefined) return undefined;
		const parts = [kindLabel(shownKind, name), size ? formatBytes(size) : null];
		if (entry && file) parts.push(m.course_viewer_in_archive({ name: file.name }));
		return parts.filter((part) => part).join(' · ');
	});

	/* Tools of the kinds that have them; line wrapping and the page theme are remembered on this
	   device. */
	const WRAP_KEY = 'mybicocca:viewer-wrap';
	const THEME_KEY = 'mybicocca:viewer-theme';
	let wrap = $state(read(WRAP_KEY) !== 'false');
	let scale = $state(1);
	let actual = $state(false);
	let theme = $state<PageTheme>(pageTheme(read(THEME_KEY)));
	const themes: Record<PageTheme, { next: PageTheme; icon: Icon; label: () => string }> = {
		system: { next: 'light', icon: IconCircleHalf, label: m.course_viewer_theme_system },
		light: { next: 'dark', icon: IconSun, label: m.course_viewer_theme_light },
		dark: { next: 'system', icon: IconMoon, label: m.course_viewer_theme_dark }
	};
	const ZOOMS = [0.5, 0.67, 0.8, 1, 1.25, 1.5, 2, 3];
	let zoom = $state(ZOOMS.indexOf(1));
	const darkPages = $derived(
		theme === 'dark' || (theme === 'system' && appTheme.resolved === 'dark')
	);

	function pageTheme(value: string | null): PageTheme {
		return value === 'light' || value === 'dark' ? value : 'system';
	}

	function read(key: string): string | null {
		try {
			return localStorage.getItem(key);
		} catch {
			return null;
		}
	}

	function remember(key: string, value: string) {
		try {
			localStorage.setItem(key, value);
		} catch {
			// The choice still holds for this visit.
		}
	}

	function toggleWrap() {
		wrap = !wrap;
		remember(WRAP_KEY, String(wrap));
	}

	function cycleTheme() {
		theme = themes[theme].next;
		remember(THEME_KEY, theme);
	}

	/** Kinds the browser shows in a tab of its own; HTML and SVG would run in this origin there. */
	function opensInTab(target: FileKind, mime: string): boolean {
		if (target.type === 'image') return !mime.includes('svg');
		return (
			target.type === 'pdf' ||
			target.type === 'video' ||
			target.type === 'audio' ||
			target.type === 'text'
		);
	}

	const shareable = $derived(shown !== undefined && canShareFile(shown.name, shown.mime));

	async function share() {
		if (shown === undefined) return;
		try {
			await shareFile(shown.bytes, shown.name, shown.mime);
		} catch {
			toast.show({ message: m.course_share_failed(), tone: 'danger' });
		}
	}

	/* Closing returns where the viewer was opened from, and an archive's entry to its list; a
	   viewer opened on its own closes to the course's content. */
	let fromApp = false;
	let fromArchive = false;
	afterNavigate(({ from, to, type }) => {
		if (type === 'enter' || from === null) return;
		const inViewer = from.url.pathname.endsWith('/file');
		fromApp ||= !inViewer;
		fromArchive =
			inViewer &&
			type !== 'popstate' &&
			!from.url.searchParams.has('entry') &&
			to?.url.searchParams.has('entry') === true;
	});

	function close() {
		if (fromArchive || fromApp) history.back();
		else void goto(coursePath(course.id, 'content'), { replace: true });
	}

	function keydown(event: KeyboardEvent) {
		if (event.key !== 'Escape' || event.defaultPrevented) return;
		if (document.querySelector('dialog[open]') !== null) return;
		close();
	}

	function entryHref(item: ZipEntry): ResolvedPathname {
		if (request === null || file === undefined) return coursePath(course.id, 'content');
		return request.by === 'module'
			? moduleFileHref(course.id, request.cmId, file, item.path)
			: attachmentHref(course.id, file, item.path);
	}
</script>

<svelte:head>
	<title>{title} · {m.app_name()}</title>
</svelte:head>

<svelte:window onkeydown={keydown} />
<Immersive />

<div class="viewer" data-course={course.slot}>
	<header class="bar">
		<IconButton icon={IconX} label={m.course_viewer_close()} onclick={close} shortcut={['esc']} />
		<div class="titles">
			<h1 class="title">{title}</h1>
			{#if subtitle}<p class="subtitle">{subtitle}</p>{/if}
		</div>
		{#if shown}
			<div class="tools">
				{#if shown.kind.type === 'text'}
					<IconButton
						icon={IconMagnifyingGlassMinus}
						label={m.course_viewer_smaller()}
						disabled={scale <= 0.7}
						onclick={() => (scale = Math.max(0.7, scale - 0.15))}
					/>
					<IconButton
						icon={IconMagnifyingGlassPlus}
						label={m.course_viewer_larger()}
						disabled={scale >= 2.2}
						onclick={() => (scale = Math.min(2.2, scale + 0.15))}
					/>
					<IconButton
						icon={IconTextAlignLeft}
						label={wrap ? m.course_viewer_wrap_off() : m.course_viewer_wrap_on()}
						pressed={wrap}
						onclick={toggleWrap}
					/>
				{:else if shown.kind.type === 'image'}
					<IconButton
						icon={actual ? IconArrowsIn : IconArrowsOut}
						label={actual ? m.course_image_fit() : m.course_image_actual()}
						pressed={actual}
						onclick={() => (actual = !actual)}
					/>
				{:else if shown.kind.type === 'pdf'}
					<IconButton
						icon={IconMagnifyingGlassMinus}
						label={m.course_pdf_zoom_out()}
						disabled={zoom === 0}
						onclick={() => (zoom = Math.max(0, zoom - 1))}
					/>
					<IconButton
						icon={IconMagnifyingGlassPlus}
						label={m.course_pdf_zoom_in()}
						disabled={zoom === ZOOMS.length - 1}
						onclick={() => (zoom = Math.min(ZOOMS.length - 1, zoom + 1))}
					/>
				{/if}
				{#if shown.kind.type === 'html' || shown.kind.type === 'pdf'}
					{@const current = themes[theme]}
					<IconButton
						icon={current.icon}
						label={m.course_viewer_theme({ theme: current.label() })}
						onclick={cycleTheme}
					/>
				{/if}
				{#if shareable}
					<IconButton
						icon={IconShareNetwork}
						label={m.course_share()}
						onclick={() => void share()}
					/>
				{/if}
				{#if opensInTab(shown.kind, shown.mime)}
					{@const target = shown}
					<IconButton
						icon={IconArrowSquareOut}
						label={m.course_viewer_new_tab()}
						onclick={() =>
							target.kind.type === 'pdf'
								? openPdfInTab(target.bytes, target.name)
								: openInTab(
										target.bytes,
										target.kind.type === 'text' ? 'text/plain;charset=utf-8' : target.mime
									)}
					/>
				{/if}
				<span class="download">
					<Button
						variant="solid"
						icon={IconDownloadSimple}
						label={m.course_download_named({ name: shown.name })}
						onclick={() => shown && saveFile(shown.bytes, shown.name, shown.mime)}
					>
						<span class="download-label">{m.course_download()}</span>
					</Button>
				</span>
			</div>
		{/if}
	</header>

	<div class="stage" aria-busy={shown === undefined && !bytes.error}>
		{#if located.status === 'missing' || archive === 'unreadable' || (request?.entry && Array.isArray(archive) && entry === undefined)}
			<div class="center">
				<EmptyState
					icon={IconFileDashed}
					title={archive === 'unreadable'
						? m.course_zip_unreadable()
						: m.course_viewer_missing_title()}
					body={archive === 'unreadable' ? undefined : m.course_viewer_missing_body()}
				>
					{#snippet action()}
						<Button href={coursePath(course.id, 'content')}>{m.course_viewer_to_content()}</Button>
					{/snippet}
				</EmptyState>
			</div>
		{:else if located.status === 'failed' && course.contents.error}
			<div class="center">
				<ErrorState
					message={errorMessage(course.contents.error)}
					onretry={course.contents.refresh}
					retrying={course.contents.refreshing}
				/>
			</div>
		{:else if bytes.status === 'error' && bytes.error}
			<div class="center">
				<ErrorState
					title={m.course_download_failed()}
					message={errorMessage(bytes.error)}
					onretry={bytes.refresh}
					retrying={bytes.refreshing}
				/>
			</div>
		{:else if entryFailed}
			<div class="center">
				<ErrorState message={m.course_zip_unreadable()} />
			</div>
		{:else if shown === undefined}
			<div class="center" role="status">
				{#if gate.visible}
					<Spinner size={40} />
					<p class="loading">
						{m.course_viewer_downloading()}
						{#if file?.size}<span>{formatBytes(file.size)}</span>{/if}
					</p>
				{/if}
			</div>
		{:else if Array.isArray(archive) && entry === undefined}
			<ZipView entries={archive} name={shown.name} hrefOf={entryHref} />
		{:else if shown.kind.type === 'pdf'}
			{@const target = shown}
			{#key target.bytes}
				<PdfView
					bytes={target.bytes}
					name={target.name}
					dark={darkPages}
					zoom={ZOOMS[zoom]}
					ondownload={() => saveFile(target.bytes, target.name, target.mime)}
				/>
			{/key}
		{:else if shown.kind.type === 'image'}
			<ImageView
				bytes={shown.bytes}
				name={shown.name}
				mime={shown.mime}
				{actual}
				ontoggle={() => (actual = !actual)}
			/>
		{:else if shown.kind.type === 'text'}
			<TextView bytes={shown.bytes} name={shown.name} {wrap} {scale} />
		{:else if shown.kind.type === 'html'}
			<HtmlView bytes={shown.bytes} name={shown.name} {theme} />
		{:else if shown.kind.type === 'video' || shown.kind.type === 'audio'}
			<MediaView bytes={shown.bytes} name={shown.name} mime={shown.mime} kind={shown.kind.type} />
		{:else}
			{@const target = shown}
			<NoPreview
				name={target.name}
				kind={target.kind}
				size={target.size}
				office={entry === undefined && file !== undefined ? file : undefined}
				ondownload={() => saveFile(target.bytes, target.name, target.mime)}
			/>
		{/if}
	</div>
</div>

<FileDialogs />

<style>
	.viewer {
		display: grid;
		grid-template-rows: auto minmax(0, 1fr);
		block-size: 100dvh;
		background: var(--bg);
	}

	.bar {
		display: flex;
		align-items: center;
		gap: 8px;
		min-block-size: calc(var(--topbar-h) + var(--safe-top));
		padding: var(--safe-top) 12px 0 8px;
		background: var(--surface);
		box-shadow: 0 1px 0 var(--hairline);
	}

	.titles {
		flex: 1;
		display: grid;
		min-inline-size: 0;
	}

	.title {
		overflow: hidden;
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.subtitle {
		overflow: hidden;
		color: var(--text-muted);
		font: var(--type-meta);
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.tools {
		display: flex;
		align-items: center;
		gap: 2px;
	}

	.download {
		margin-inline-start: 6px;
	}

	.stage {
		position: relative;
		min-block-size: 0;
		overflow: hidden;
	}

	.center {
		display: grid;
		place-content: center;
		justify-items: center;
		gap: 14px;
		block-size: 100%;
		padding: var(--sp-6);
	}

	.loading {
		display: grid;
		justify-items: center;
		color: var(--text-muted);
		font: var(--type-ui);
	}

	.loading span {
		font: var(--type-meta);
		font-variant-numeric: tabular-nums;
	}

	/* Phones: the title keeps the bar; the tools float at the bottom, the download last. */
	@media (width < 600px) {
		.tools {
			position: fixed;
			inset: auto 12px calc(16px + var(--safe-bottom));
			z-index: var(--z-raised);
			justify-content: center;
			gap: 4px;
			margin-inline: auto;
			inline-size: fit-content;
			padding: 6px;
			border-radius: var(--r-pill);
			background: var(--surface-raised);
			box-shadow: var(--elev-3), var(--inner-light);
		}

		.download-label {
			display: none;
		}
	}
</style>
