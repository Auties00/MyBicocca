<script lang="ts">
	import { untrack } from 'svelte';
	import IconArrowClockwise from '~icons/ph/arrow-clockwise';
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import IconDownloadSimple from '~icons/ph/download-simple';
	import IconFileDashed from '~icons/ph/file-dashed-duotone';
	import IconFilePdf from '~icons/ph/file-pdf-duotone';
	import IconShareNetwork from '~icons/ph/share-network';
	import { errorMessage } from '#lib/data/errorMessage.ts';
	import { session } from '#lib/data/session.svelte.ts';
	import { formatBytes } from '#lib/format/numbers.ts';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Button from '#lib/ui/Button.svelte';
	import DetailHeader from '#lib/ui/DetailHeader.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import ErrorState from '#lib/ui/ErrorState.svelte';
	import IconButton from '#lib/ui/IconButton.svelte';
	import { LoadingGate } from '#lib/ui/loadingGate.svelte.ts';
	import PageHeader from '#lib/ui/PageHeader.svelte';
	import Skeleton from '#lib/ui/Skeleton.svelte';
	import Spinner from '#lib/ui/Spinner.svelte';
	import { certificateFiles } from '../certificateFiles.svelte.ts';
	import { useCertificates } from '../certificates.svelte.ts';
	import {
		canShareFiles,
		certificateLine,
		certificateName,
		saveFile,
		shareFile,
		viewerUrl,
		type OpenMode
	} from '../certificateView.ts';
	import { fitToWindow } from '../fitToWindow.ts';
	import { useDocumentsView } from '../view.ts';
	import { joinParts } from '../text.ts';

	interface Props {
		/** The certificate's key, from the URL. */
		key: string;
		mode: OpenMode;
	}

	let { key, mode }: Props = $props();

	const certificates = useCertificates();
	const view = useDocumentsView();
	const query = certificates.query;
	const loaded = $derived(query.data !== undefined);
	const gate = new LoadingGate(() => query.status === 'pending');
	const updatedAt = $derived(query.updatedAt ? new Date(query.updatedAt) : undefined);
	const home = shellHref('services/certificates');

	const certificate = $derived(certificates.find(key));
	const share = canShareFiles();
	const waiting = $derived(!loaded && query.status === 'pending');
	const name = $derived(
		certificate ? certificateName(certificate) : waiting ? '' : m.shell_service_certificates()
	);
	const state = $derived(certificate && certificateFiles.state(certificate.key));
	const file = $derived(state?.status === 'ready' ? state.file : undefined);
	const pending = $derived(state === undefined || state.status === 'pending');
	const subtitle = $derived.by(() => {
		if (!certificate) return undefined;
		const size = file && m.documents_certificates_size({ size: formatBytes(file.blob.size) });
		return joinParts([certificateLine(certificate), size]);
	});
	/* A page of the PDF wants about 560 px: a phone gets the file and what to do with it instead. */
	const inline = $derived(mode === 'preview');

	/* Opening a certificate generates it, once per visit. */
	$effect(() => {
		const scope = session.current;
		const current = certificate;
		if (!scope || !current || certificateFiles.state(current.key) !== undefined) return;
		untrack(() => certificateFiles.generate(scope, current)).catch(() => {
			// The page shows the failure.
		});
	});

	function regenerate() {
		const scope = session.current;
		if (!scope || !certificate) return;
		certificateFiles.regenerate(scope, certificate).catch(() => {
			// The page shows the failure.
		});
	}

	/* A blob URL is no link the kit's buttons take: it opens from a click. */
	function openTab() {
		if (file) window.open(viewerUrl(file.url, false), '_blank', 'noopener');
	}

	const announcement = $derived.by(() => {
		if (state?.status === 'failed') {
			return `${m.documents_certificates_failed()}. ${errorMessage(state.error)}`;
		}
		if (file) {
			return `${m.documents_certificates_ready()}, ${m.documents_certificates_size({ size: formatBytes(file.blob.size) })}`;
		}
		return certificate ? m.documents_certificates_generating() : '';
	});
</script>

<svelte:head>
	<title>{name ? `${name} · ` : ''}{m.shell_service_certificates()} · {m.app_name()}</title>
</svelte:head>

{#snippet actions()}
	<Button
		variant="solid"
		icon={IconDownloadSimple}
		disabled={!file}
		onclick={() => file && saveFile(file.url, file.name)}
	>
		{m.documents_certificates_download()}
	</Button>
	<IconButton
		icon={IconArrowSquareOut}
		label={m.documents_certificates_open_tab()}
		disabled={!file}
		onclick={openTab}
	/>
	{#if share}
		<IconButton
			icon={IconShareNetwork}
			label={m.documents_certificates_share()}
			disabled={!file}
			onclick={() => file && shareFile(file.blob, file.name, name)}
		/>
	{/if}
	<IconButton
		icon={IconArrowClockwise}
		label={m.documents_certificates_regenerate()}
		disabled={pending}
		onclick={regenerate}
	/>
{/snippet}

{#if !view.wide}
	<PageHeader
		title={name}
		titleLang={certificate ? 'it' : undefined}
		{subtitle}
		back={{ href: home, label: m.shell_service_certificates() }}
		actions={inline && certificate ? actions : undefined}
		loading={waiting && { eyebrow: false }}
	/>
	{#if query.error && loaded}
		<div class="stale">
			<ErrorState
				variant="stale"
				message={errorMessage(query.error)}
				{updatedAt}
				onretry={query.refresh}
				retrying={query.refreshing}
			/>
		</div>
	{/if}
{/if}

{#if loaded && !certificate}
	<EmptyState
		size="page"
		icon={IconFileDashed}
		title={m.documents_certificates_not_found_title()}
		body={m.documents_certificates_not_found_body()}
	>
		{#snippet action()}
			<Button href={home}>{m.documents_certificates_back()}</Button>
		{/snippet}
	</EmptyState>
{:else if !certificate}
	<div aria-busy="true">
		{#if gate.visible}
			<div class="preview">
				<DetailHeader title="" loading={{ eyebrow: false }} />
				{#if inline}
					<div class="paper" {@attach fitToWindow}><Skeleton height="100%" radius="0" /></div>
				{:else}
					<div class="paper compact"><Skeleton height="100%" radius="0" /></div>
				{/if}
			</div>
		{/if}
	</div>
{:else}
	<article class="preview">
		<DetailHeader title={name} titleLang="it" {subtitle} {actions} />

		{#if inline}
			{#if state?.status === 'failed'}
				<div class="paper failed" {@attach fitToWindow}>
					<ErrorState
						title={m.documents_certificates_failed()}
						message={errorMessage(state.error)}
						onretry={regenerate}
					/>
				</div>
			{:else if file}
				<iframe
					class="paper"
					src={viewerUrl(file.url, true)}
					title={m.documents_certificates_preview_label()}
					{@attach fitToWindow}
				></iframe>
			{:else}
				<div class="paper waiting" {@attach fitToWindow}>
					<Spinner size={28} />
					<p>{m.documents_certificates_generating()}</p>
				</div>
			{/if}
		{:else}
			<!-- Too narrow for a page of the PDF: the file, and what to do with it. -->
			<section class="file" aria-label={m.documents_certificates_file_label()}>
				{#if state?.status === 'failed'}
					<ErrorState
						title={m.documents_certificates_failed()}
						message={errorMessage(state.error)}
						onretry={regenerate}
					/>
				{:else}
					<div class="file-head">
						<span class="glyph" aria-hidden="true"><IconFilePdf /></span>
						<p class="file-status">
							{#if file}
								{m.documents_certificates_ready()}
							{:else}
								<Spinner size={18} />
								{m.documents_certificates_generating()}
							{/if}
						</p>
					</div>
					<div class="file-actions">
						{#if mode === 'tab'}
							<Button
								variant="solid"
								full
								icon={IconArrowSquareOut}
								disabled={!file}
								onclick={openTab}
							>
								{m.documents_certificates_open()}
							</Button>
						{/if}
						<Button
							variant={mode === 'tab' ? 'tonal' : 'solid'}
							full
							icon={IconDownloadSimple}
							disabled={!file}
							onclick={() => file && saveFile(file.url, file.name)}
						>
							{m.documents_certificates_download()}
						</Button>
						{#if share}
							<Button
								full
								icon={IconShareNetwork}
								disabled={!file}
								onclick={() => file && shareFile(file.blob, file.name, name)}
							>
								{m.documents_certificates_share()}
							</Button>
						{/if}
						<Button
							variant="ghost"
							full
							icon={IconArrowClockwise}
							disabled={pending}
							onclick={regenerate}
						>
							{m.documents_certificates_regenerate()}
						</Button>
					</div>
				{/if}
			</section>
		{/if}
		<p class="sr-only" role="status">{announcement}</p>
	</article>
{/if}

<style>
	.stale {
		margin-block-start: calc(var(--page-gap) * -0.5);
	}

	.preview {
		display: grid;
		gap: var(--sp-4);
	}

	/* A sheet of paper down to the window's edge, never so short that a page can't be read. */
	.paper {
		display: block;
		inline-size: 100%;
		block-size: max(400px, var(--fit-block, 100dvh - var(--topbar-h) - var(--safe-top) - 320px));
		border: 0;
		border-radius: var(--r-lg);
		background: var(--surface-sunken);
		box-shadow: var(--elev-1), var(--inner-light);
		overflow: hidden;
	}

	.paper.compact {
		block-size: 280px;
	}

	iframe.paper:focus-visible,
	iframe.paper:focus {
		outline: 2px solid var(--focus);
		outline-offset: 3px;
	}

	.waiting {
		display: grid;
		place-content: center;
		justify-items: center;
		gap: var(--sp-3);
		color: var(--text-muted);
	}

	.failed {
		display: grid;
		place-content: center;
		padding: var(--sp-6);
		background: var(--surface);
	}

	.file {
		display: grid;
		gap: var(--sp-5);
		padding: var(--sp-5);
		border-radius: var(--r-lg);
		background: var(--surface);
		box-shadow: var(--elev-1), var(--inner-light);
	}

	.file-head {
		display: flex;
		align-items: center;
		gap: var(--sp-4);
	}

	.glyph {
		display: grid;
		place-items: center;
		inline-size: 56px;
		block-size: 56px;
		border-radius: var(--r-md);
		background: var(--container);
		color: var(--accent-text);
	}

	.glyph :global(svg) {
		inline-size: 30px;
		block-size: 30px;
	}

	.file-status {
		display: flex;
		align-items: center;
		gap: var(--sp-2);
		font: var(--type-title-s);
		letter-spacing: var(--track-title-s);
	}

	.file-actions {
		display: grid;
		gap: var(--sp-2);
	}

	@media (prefers-reduced-motion: no-preference) {
		iframe.paper {
			animation: arrive var(--dur-base) var(--ease-out);
		}
	}

	@keyframes arrive {
		from {
			opacity: 0;
			transform: translateY(6px);
		}
	}

	@supports (corner-shape: squircle) {
		.file {
			corner-shape: squircle;
			border-radius: calc(var(--r-lg) * 1.5);
		}
	}
</style>
