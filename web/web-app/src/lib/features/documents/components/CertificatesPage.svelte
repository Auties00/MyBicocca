<script lang="ts">
	import IconArrowSquareOut from '~icons/ph/arrow-square-out';
	import IconFileText from '~icons/ph/file-text-duotone';
	import IconInfo from '~icons/ph/info';
	import { m } from '#lib/paraglide/messages.js';
	import { shellHref } from '#lib/shell/nav.ts';
	import Button from '#lib/ui/Button.svelte';
	import EmptyState from '#lib/ui/EmptyState.svelte';
	import { CERTIFICATES_URL } from '../certificates.ts';
	import { Certificates, certificateHref, provideCertificates } from '../certificates.svelte.ts';
	import { hasPdfViewer, previewsWhole, type OpenMode } from '../certificateView.ts';
	import CertificateList from './CertificateList.svelte';
	import CertificatePreview from './CertificatePreview.svelte';
	import CertificatesOverview from './CertificatesOverview.svelte';
	import DocumentsLayout from './DocumentsLayout.svelte';
	import ListSkeleton from './ListSkeleton.svelte';
	import { compact } from '#lib/ui/media.ts';

	interface Props {
		/** The open certificate, from the URL. */
		open?: string;
	}

	let { open }: Props = $props();

	/*
	 * Beside the list only where a page of the PDF gets about 560 px: below, it would be too small
	 * to read, and the preview takes the page instead. Without a preview, the list is all there is.
	 */
	const PREVIEW_MIN = 560;
	const NO_PREVIEW = 1e5;

	const certificates = new Certificates();
	provideCertificates(certificates);

	const viewer = hasPdfViewer();
	const whole = previewsWhole();
	const mode: OpenMode = $derived(!viewer ? 'save' : compact.current || !whole ? 'tab' : 'preview');
	const empty = $derived(certificates.query.data !== undefined && certificates.list.length === 0);
</script>

<svelte:head>
	{#if open === undefined}
		<title>{m.shell_service_certificates()} · {m.app_name()}</title>
	{/if}
</svelte:head>

<DocumentsLayout
	title={m.shell_service_certificates()}
	subtitle={m.shell_service_certificates_desc()}
	query={certificates.query}
	{empty}
	failedTitle={m.documents_certificates_load_failed()}
	selected={open === undefined ? undefined : certificateHref(open)}
	hrefs={certificates.hrefs}
	home={shellHref('services/certificates')}
	detailMin={mode === 'preview' ? PREVIEW_MIN : NO_PREVIEW}
	listLabel={m.documents_certificates_list_label()}
	detailLabel={m.documents_certificates_preview_label()}
>
	{#snippet note()}
		<p class="note">
			<IconInfo aria-hidden="true" />
			<span>{m.documents_certificates_note()}</span>
		</p>
	{/snippet}
	{#snippet list(current)}
		<CertificateList {certificates} {mode} selected={current} />
	{/snippet}
	{#snippet skeleton()}
		<ListSkeleton rows={[62, 74, 80, 80]} lines={[2, 1]} />
	{/snippet}
	{#snippet emptyState()}
		<EmptyState
			size="page"
			icon={IconFileText}
			title={m.documents_certificates_empty_title()}
			body={m.documents_certificates_empty_body()}
		>
			{#snippet action()}
				<Button href={CERTIFICATES_URL} trailingIcon={IconArrowSquareOut}>
					{m.documents_certificates_portal()}
				</Button>
			{/snippet}
		</EmptyState>
	{/snippet}
	{#if open !== undefined}
		{#key open}
			<CertificatePreview key={open} {mode} />
		{/key}
	{:else}
		<CertificatesOverview />
	{/if}
</DocumentsLayout>

<style>
	.note {
		display: flex;
		align-items: flex-start;
		gap: var(--sp-2);
		max-inline-size: var(--w-reading);
		margin-block-start: calc(var(--page-gap) * -0.5);
		color: var(--text-muted);
		font: var(--type-meta);
		letter-spacing: var(--track-meta);
		text-wrap: pretty;
	}

	.note :global(svg) {
		flex: none;
		inline-size: 18px;
		block-size: 18px;
		margin-block-start: 1px;
		color: var(--accent-text);
	}
</style>
